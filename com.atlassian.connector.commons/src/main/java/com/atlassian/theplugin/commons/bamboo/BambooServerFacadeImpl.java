/**
 * Copyright (C) 2008 Atlassian
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.atlassian.theplugin.commons.bamboo;

import com.atlassian.theplugin.commons.ServerType;
import com.atlassian.theplugin.commons.bamboo.api.AutoRenewBambooSession;
import com.atlassian.theplugin.commons.bamboo.api.BambooSession;
import com.atlassian.theplugin.commons.bamboo.api.BambooSessionImpl;
import com.atlassian.theplugin.commons.bamboo.api.LoginBambooSession;
import com.atlassian.theplugin.commons.cfg.SubscribedPlan;
import com.atlassian.theplugin.commons.exception.ServerPasswordNotProvidedException;
import com.atlassian.theplugin.commons.remoteapi.*;
import com.atlassian.theplugin.commons.remoteapi.rest.HttpSessionCallback;
import com.atlassian.theplugin.commons.remoteapi.rest.HttpSessionCallbackImpl;
import com.atlassian.theplugin.commons.util.Logger;
import org.jetbrains.annotations.NotNull;

import java.util.*;

/**
 * Class used for communication wiht Bamboo Server.
 *
 * @author sginter + others Date: Jan 15, 2008
 */
public final class BambooServerFacadeImpl implements BambooServerFacade {
	private final Map<String, BambooSession> sessions = new WeakHashMap<String, BambooSession>();

	private final Logger loger;

	private static BambooServerFacadeImpl instance;

	private final BambooSessionFactory bambooSessionFactory;

	private HttpSessionCallback callback;

	public BambooServerFacadeImpl(Logger loger, @NotNull BambooSessionFactory factory) {
		this.loger = loger;
		this.callback = new HttpSessionCallbackImpl();
		this.bambooSessionFactory = factory;
	}

	public static synchronized BambooServerFacade getInstance(Logger loger) {
		if (instance == null) {
			instance = new BambooServerFacadeImpl(loger, new SimpleBambooSessionFactory());
		}

		return instance;
	}

	public ServerType getServerType() {
		return ServerType.BAMBOO_SERVER;
	}

	public boolean isBamboo2(final BambooServerData serverData) {
		BambooSession session;
		try {
			session = getSession(serverData);
			if (session != null && session.getBamboBuildNumber() > 0) {
				return true;
			}

		} catch (RemoteApiException e) {
			//not important == false
		}
		return false;
	}

	public boolean isBamboo2M9(final BambooServerData bambooServerData) {
		{
			BambooSession session;
			try {
				session = getSession(bambooServerData);
				if (session != null && session.getBamboBuildNumber() >= 1313) {
					return true;
				}

			} catch (RemoteApiException e) {
				//not important == false
			}
			return false;
		}
	}

	// package scope for test purposes
	synchronized BambooSession getSession(BambooServerData server) throws RemoteApiException {
		// @todo old server will stay on map - remove them !!!
		String key = server.getUserName() + server.getUrl() + server.getPassword() + server.getServerId();
		BambooSession session = sessions.get(key);
		if (session == null) {
			session = bambooSessionFactory.createSession(server, callback);
			sessions.put(key, session);
		}
		if (!session.isLoggedIn()) {
			session.login(server.getUserName(), server.getPassword().toCharArray());
		}
		return session;
	}

	/**
	 * Test connection to Bamboo server.
	 *
	 * @param serverData The configuration for the server that we want to test the connectio for
	 * @throws RemoteApiException on failed login
	 * @see RemoteApiLoginFailedException
	 */
	public void testServerConnection(ServerData serverData) throws RemoteApiException {
		ProductSession apiHandler = bambooSessionFactory.createLoginSession(serverData, callback);
		apiHandler.login(serverData.getUserName(), serverData.getPassword().toCharArray());
		apiHandler.logout();
	}

	/**
	 * List projects defined on Bamboo server.
	 *
	 * @param bambooServer Bamboo server information
	 * @return list of projects or null on error
	 * @throws ServerPasswordNotProvidedException
	 *          when invoked for Server that has not had the password set yet
	 */
	public Collection<BambooProject> getProjectList(BambooServerData bambooServer) throws ServerPasswordNotProvidedException,
			RemoteApiException {
		try {
			return getSession(bambooServer).listProjectNames();
		} catch (RemoteApiException e) {
			loger.error("Bamboo exception: " + e.getMessage(), e);
			throw e;
		}
	}

	/**
	 * List plans defined on Bamboo server.
	 *
	 * @param bambooServer Bamboo server information
	 * @return list of plans
	 * @throws com.atlassian.theplugin.commons.exception.ServerPasswordNotProvidedException
	 *          when invoked for Server that has not had the password set yet
	 */
	public Collection<BambooPlan> getPlanList(BambooServerData bambooServer) throws ServerPasswordNotProvidedException,
			RemoteApiException {
		BambooSession api = getSession(bambooServer);
		List<BambooPlan> plans = api.listPlanNames();
		try {
			List<String> favPlans = api.getFavouriteUserPlans();
			for (String fav : favPlans) {
				for (ListIterator<BambooPlan> it = plans.listIterator(); it.hasNext();) {
					final BambooPlan plan = it.next();
					if (plan.getKey().equalsIgnoreCase(fav)) {
						it.set(plan.withFavourite(true));
						break;
					}
				}
			}
		} catch (RemoteApiException e) {
			// lack of favourite info is not a blocker here
		}
		return plans;
	}

	/**
	 * List details on subscribed plans.
	 * <p/>
	 * <p/>
	 * Returns info on all subscribed plans including information about failed attempt.
	 * <p/>
	 * <p/>
	 * Throws ServerPasswordNotProvidedException when invoked for Server that has not had the password set, when the
	 * server returns a meaningful exception response.
	 *
	 * @param bambooServer Bamboo server information
	 * @return results on subscribed builds
	 * @throws com.atlassian.theplugin.commons.exception.ServerPasswordNotProvidedException
	 *          when invoked for Server that has not had the password set yet
	 * @see com.atlassian.theplugin.commons.bamboo.api.BambooSessionImpl#login(String, char[])
	 */
	public Collection<BambooBuild> getSubscribedPlansResults(BambooServerData bambooServer,
			final Collection<SubscribedPlan> plans, boolean isUseFavourities, int timezoneOffset)
			throws ServerPasswordNotProvidedException {
		Collection<BambooBuild> builds = new ArrayList<BambooBuild>();

//		String connectionErrorMessage;
		Throwable connectionError;
		BambooSession api = null;
		try {
			api = getSession(bambooServer);
//			connectionErrorMessage = "";
			connectionError = null;
		} catch (RemoteApiLoginFailedException e) {
			// TODO wseliga used to be bambooServer.getIsConfigInitialized() here
			if (bambooServer.getPassword().length() > 0) {
				loger.error("Bamboo login exception: " + e.getMessage());
				// todo if there is a login error we should not proceed and rethrow exception
//				connectionErrorMessage = e.getMessage();
				connectionError = e;
			} else {
				throw new ServerPasswordNotProvidedException(e);
			}
		} catch (RemoteApiException e) {
			loger.error("Bamboo exception: " + e.getMessage());
//			connectionErrorMessage = e.getMessage();
			connectionError = e;
		}

		Collection<BambooPlan> plansForServer = null;
		try {
			plansForServer = getPlanList(bambooServer);
		} catch (RemoteApiException e) {
			// can go further, no disabled info will be available
			loger.warn("Cannot fetch plan list from Bamboo server [" + bambooServer.getName() + "]");
		}

		if (isUseFavourities) {
			if (plansForServer != null) {
				for (BambooPlan bambooPlan : plansForServer) {
					if (bambooPlan.isFavourite()) {
						if (api != null && api.isLoggedIn()) {
							try {
								BambooBuild buildInfo = api.getLatestBuildForPlan(bambooPlan.getKey(),
										bambooPlan.isEnabled(), timezoneOffset);
								builds.add(buildInfo);
							} catch (RemoteApiException e) {
								// go ahead, there are other builds
								loger.warn("Cannot fetch latest build for plan [" + bambooPlan.getKey()
										+ "] from Bamboo server [" + bambooServer.getName() + "]");
							}
						} else {
							builds.add(constructBuildErrorInfo(bambooServer, bambooPlan.getKey(),
									bambooPlan.getName(), connectionError == null ? ""
									: connectionError.getMessage(), connectionError));
						}
					}
				}
			}
		} else {
			for (SubscribedPlan plan : plans) {
				if (api != null && api.isLoggedIn()) {
					try {
						final Boolean isEnabled = plansForServer != null ? BambooSessionImpl.isPlanEnabled(
								plansForServer, plan.getKey()) : null;
						BambooBuild buildInfo = api.getLatestBuildForPlan(plan.getKey(), isEnabled != null ? isEnabled
								: true, timezoneOffset);
						builds.add(buildInfo);
					} catch (RemoteApiException e) {
						// go ahead, there are other builds
						// todo what about any error info
					}
				} else {
					builds.add(constructBuildErrorInfo(bambooServer, plan.getKey(), null, connectionError == null ? ""
							: connectionError.getMessage(), connectionError));
				}
			}
		}

		return builds;
	}


	/**
	 * This is the new version of {@link #getSubscribedPlansResults(BambooServerData, java.util.Collection, boolean, int)}
	 * It returns info about 'building' or 'in queue' state.
	 * <p/>
	 * Throws ServerPasswordNotProvidedException when invoked for Server that has not had the password set, when the
	 * server returns a meaningful exception response.
	 *
	 * @param bambooServer	 Bamboo server information
	 * @param plans
	 * @param isUseFavourities
	 * @param timezoneOffset
	 * @return results on subscribed builds
	 * @throws com.atlassian.theplugin.commons.exception.ServerPasswordNotProvidedException
	 *          when invoked for Server that has not had the password set yet
	 * @see com.atlassian.theplugin.commons.bamboo.api.BambooSessionImpl#login(String, char[])
	 */
	public Collection<BambooBuild> getSubscribedPlansResultsNew(BambooServerData bambooServer,
			final Collection<SubscribedPlan> plans, boolean isUseFavourities, int timezoneOffset)
			throws ServerPasswordNotProvidedException {
		Collection<BambooBuild> builds = new ArrayList<BambooBuild>();

//		String connectionErrorMessage;
		Throwable connectionError;
		BambooSession api = null;
		try {
			api = getSession(bambooServer);
			connectionError = null;
		} catch (RemoteApiLoginFailedException e) {
			if (bambooServer.getPassword().length() > 0) {
				loger.error("Bamboo login exception: " + e.getMessage());
				connectionError = e;
			} else {
				throw new ServerPasswordNotProvidedException(e);
			}
		} catch (RemoteApiException e) {
			loger.error("Bamboo exception: " + e.getMessage());
			connectionError = e;
		}

		Collection<BambooPlan> plansForServer = null;
		try {
			plansForServer = getPlanList(bambooServer);
		} catch (RemoteApiException e) {
			// can go further, no disabled info will be available
			loger.warn("Cannot fetch plan list from Bamboo server [" + bambooServer.getName() + "]");
		}

		if (isUseFavourities) {
			if (plansForServer != null) {
				for (BambooPlan bambooPlan : plansForServer) {
					if (bambooPlan.isFavourite()) {
						if (api != null && api.isLoggedIn()) {
							try {
								BambooBuild buildInfo = api.getLatestBuildForPlanNew(bambooPlan.getKey(),
										bambooPlan.isEnabled(), timezoneOffset);
								builds.add(buildInfo);
							} catch (RemoteApiException e) {
								// go ahead, there are other builds
								loger.warn("Cannot fetch latest build for plan [" + bambooPlan.getKey()
										+ "] from Bamboo server [" + bambooServer.getName() + "]");
							}
						} else {
							builds.add(constructBuildErrorInfo(bambooServer, bambooPlan.getKey(),
									bambooPlan.getName(), connectionError == null ? ""
									: connectionError.getMessage(), connectionError));
						}
					}
				}
			}
		} else {
			for (SubscribedPlan plan : plans) {
				if (api != null && api.isLoggedIn()) {
					try {
						final Boolean isEnabled = plansForServer != null ? BambooSessionImpl.isPlanEnabled(
								plansForServer, plan.getKey()) : null;
						BambooBuild buildInfo = api.getLatestBuildForPlanNew(plan.getKey(), isEnabled != null ? isEnabled
								: true, timezoneOffset);
						builds.add(buildInfo);
					} catch (RemoteApiException e) {
						// go ahead, there are other builds
						// go ahead, there are other builds
						loger.warn("Cannot fetch latest build for plan [" + plan.getKey()
								+ "] from Bamboo server [" + bambooServer.getName() + "]");
					}
				} else {
					builds.add(constructBuildErrorInfo(bambooServer, plan.getKey(), null, connectionError == null ? ""
							: connectionError.getMessage(), connectionError));
				}
			}
		}

		return builds;
	}

	/**
	 * List history for provided plan.
	 * <p/>
	 * Returns last 15 builds on provided plan including information about failed attempt.
	 * <p/>
	 * <p/>
	 * Throws ServerPasswordNotProvidedException when invoked for Server that has not had the password set, when the
	 * server returns a meaningful exception response.
	 *
	 * @param bambooServer   Bamboo server information
	 * @param planKey		key of the plan to query
	 * @param timezoneOffset
	 * @return results on history for plan
	 * @throws com.atlassian.theplugin.commons.exception.ServerPasswordNotProvidedException
	 *          when invoked for Server that has not had the password set yet
	 * @see com.atlassian.theplugin.commons.bamboo.api.BambooSessionImpl#login(String, char[])
	 */
	public Collection<BambooBuild> getRecentBuildsForPlans(BambooServerData bambooServer, String planKey,
			final int timezoneOffset) throws ServerPasswordNotProvidedException {
		Collection<BambooBuild> builds = new ArrayList<BambooBuild>();

		BambooSession api;
		try {
			api = getSession(bambooServer);
		} catch (RemoteApiLoginFailedException e) {
			// TODO wseliga used to be bambooServer.getIsConfigInitialized() here
			if (bambooServer.getPassword().length() > 0) {
				loger.error("Bamboo login exception: " + e.getMessage());
				builds.add(constructBuildErrorInfo(bambooServer, planKey, null, e.getMessage(), e));
				return builds;
			} else {
				throw new ServerPasswordNotProvidedException(e);
			}
		} catch (RemoteApiException e) {
			loger.error("Bamboo exception: " + e.getMessage());
			builds.add(constructBuildErrorInfo(bambooServer, planKey, null, e.getMessage(), e));
			return builds;
		}

		try {
			builds.addAll(api.getRecentBuildsForPlan(planKey, timezoneOffset));
		} catch (RemoteApiException e) {
			loger.error("Bamboo exception: " + e.getMessage());
			builds.add(constructBuildErrorInfo(bambooServer, planKey, null, e.getMessage(), e));
		}

		return builds;
	}

	/**
	 * List history for current user.
	 * <p/>
	 * <p/>
	 * Returns last builds selected user including information about failed attempt.
	 * <p/>
	 * <p/>
	 * Throws ServerPasswordNotProvidedException when invoked for Server that has not had the password set, when the
	 * server returns a meaningful exception response.
	 *
	 * @param bambooServer Bamboo server information
	 * @return results on history for plan
	 * @throws com.atlassian.theplugin.commons.exception.ServerPasswordNotProvidedException
	 *          when invoked for Server that has not had the password set yet
	 * @see com.atlassian.theplugin.commons.bamboo.api.BambooSessionImpl#login(String, char[])
	 */
	public Collection<BambooBuild> getRecentBuildsForUser(BambooServerData bambooServer, final int timezoneOffset)
			throws ServerPasswordNotProvidedException {
		Collection<BambooBuild> builds = new ArrayList<BambooBuild>();

		BambooSession api;
		try {
			api = getSession(bambooServer);
		} catch (RemoteApiLoginFailedException e) {
			// TODO wseliga used to be bambooServer.getIsConfigInitialized() here
			if (bambooServer.getPassword().length() > 0) {
				loger.error("Bamboo login exception: " + e.getMessage());
				builds.add(constructBuildErrorInfo(bambooServer, "", null, e.getMessage(), e));
				return builds;
			} else {
				throw new ServerPasswordNotProvidedException(e);
			}
		} catch (RemoteApiException e) {
			loger.error("Bamboo exception: " + e.getMessage());
			builds.add(constructBuildErrorInfo(bambooServer, "", null, e.getMessage(), e));
			return builds;
		}

		try {
			builds.addAll(api.getRecentBuildsForUser(timezoneOffset));
		} catch (RemoteApiException e) {
			loger.error("Bamboo exception: " + e.getMessage());
			builds.add(constructBuildErrorInfo(bambooServer, "", null, e.getMessage(), e));
		}

		return builds;
	}

	/**
	 * @param bambooServer server data
	 * @param planKey	  key of the build
	 * @param buildNumber  unique number of the build
	 * @return build data
	 * @throws ServerPasswordNotProvidedException
	 *
	 * @throws RemoteApiException
	 */
	public BuildDetails getBuildDetails(BambooServerData bambooServer, @NotNull String planKey, int buildNumber)
			throws ServerPasswordNotProvidedException, RemoteApiException {
		try {
			BambooSession api = getSession(bambooServer);
			return api.getBuildResultDetails(planKey, buildNumber);
		} catch (RemoteApiException e) {
			loger.info("Bamboo exception: " + e.getMessage());
			throw e;
		}
	}

	public BambooBuild getBuildForPlanAndNumber(BambooServerData bambooServer, @NotNull String planKey,
			final int buildNumber, final int timezoneOffset)
			throws ServerPasswordNotProvidedException, RemoteApiException {
		try {
			BambooSession api = getSession(bambooServer);
			return api.getBuildForPlanAndNumber(planKey, buildNumber, timezoneOffset);
		} catch (RemoteApiException e) {
			loger.info("Bamboo exception: " + e.getMessage());
			throw e;
		}
	}

	/**
	 * @param bambooServer server data
	 * @param planKey	  key of the build
	 * @param buildNumber  unique number of the build
	 * @param buildLabel   label to add to the build
	 * @throws ServerPasswordNotProvidedException
	 *
	 * @throws RemoteApiException
	 */
	public void addLabelToBuild(BambooServerData bambooServer, @NotNull String planKey, int buildNumber, String buildLabel)
			throws ServerPasswordNotProvidedException, RemoteApiException {
		try {
			BambooSession api = getSession(bambooServer);
			api.addLabelToBuild(planKey, buildNumber, buildLabel);
		} catch (RemoteApiException e) {
			loger.info("Bamboo exception: " + e.getMessage());
			throw e;
		}
	}

	/**
	 * @param bambooServer server data
	 * @param planKey	  key of the build
	 * @param buildNumber  unique number of the build
	 * @param buildComment user comment to add to the build
	 * @throws ServerPasswordNotProvidedException
	 *
	 * @throws RemoteApiException
	 */
	public void addCommentToBuild(BambooServerData bambooServer, @NotNull String planKey, int buildNumber, String buildComment)
			throws ServerPasswordNotProvidedException, RemoteApiException {
		try {
			BambooSession api = getSession(bambooServer);
			api.addCommentToBuild(planKey, buildNumber, buildComment);
		} catch (RemoteApiException e) {
			loger.info("Bamboo exception: " + e.getMessage());
			throw e;
		}
	}

	/**
	 * Runs selected plan
	 *
	 * @param bambooServer server data
	 * @param buildKey	 key of the build
	 * @throws ServerPasswordNotProvidedException
	 *
	 * @throws RemoteApiException
	 */
	public void executeBuild(BambooServerData bambooServer, @NotNull String buildKey)
			throws ServerPasswordNotProvidedException, RemoteApiException {
		try {
			BambooSession api = getSession(bambooServer);
			api.executeBuild(buildKey);
		} catch (RemoteApiException e) {
			loger.info("Bamboo exception: " + e.getMessage());
			throw e;
		}
	}

	public String getBuildLogs(BambooServerData bambooServer, @NotNull String planKey, int buildNumber)
			throws ServerPasswordNotProvidedException, RemoteApiException {
		try {
			BambooSession api = getSession(bambooServer);
			return api.getBuildLogs(planKey, buildNumber);
		} catch (RemoteApiException e) {
			loger.info("Bamboo exception: " + e.getMessage());
			throw e;
		}
	}

	/**
	 * List plans defined on Bamboo server.
	 *
	 * @param bambooServer Bamboo server information
	 * @return list of plans or null on error
	 * @throws com.atlassian.theplugin.commons.exception.ServerPasswordNotProvidedException
	 *          when invoked for Server that has not had the password set yet
	 * @throws com.atlassian.theplugin.commons.remoteapi.RemoteApiException
	 *          in case of some IO or similar problem
	 */
	public Collection<String> getFavouritePlans(BambooServerData bambooServer) throws ServerPasswordNotProvidedException,
			RemoteApiException {
		try {
			return getSession(bambooServer).getFavouriteUserPlans();
		} catch (RemoteApiException e) {
			loger.error("Bamboo exception: " + e.getMessage());
			throw e;
		}
	}

	private BambooBuild constructBuildErrorInfo(BambooServerData server, @NotNull String planKey, String planName,
			String message, Throwable exception) {
		return new BambooBuildInfo.Builder(planKey, null, server, planName, null, BuildStatus.UNKNOWN).errorMessage(
				message, exception).pollingTime(new Date()).build();
	}

	public void setCallback(HttpSessionCallback callback) {
		this.callback = callback;
	}

	private static class SimpleBambooSessionFactory implements BambooSessionFactory {

		public BambooSession createSession(final BambooServerData serverData, final HttpSessionCallback callback)
				throws RemoteApiException {
			return new AutoRenewBambooSession(serverData, callback);
		}

		public ProductSession createLoginSession(final ServerData serverData, final HttpSessionCallback callback)
				throws RemoteApiMalformedUrlException {
			return new LoginBambooSession(serverData, callback);
		}
	}
}
