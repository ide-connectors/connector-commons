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
import com.atlassian.theplugin.commons.cfg.SubscribedPlan;
import com.atlassian.theplugin.commons.exception.ServerPasswordNotProvidedException;
import com.atlassian.theplugin.commons.remoteapi.RemoteApiException;
import com.atlassian.theplugin.commons.remoteapi.RemoteApiLoginFailedException;
import com.atlassian.theplugin.commons.remoteapi.ServerData;
import com.atlassian.theplugin.commons.remoteapi.rest.HttpSessionCallback;
import com.atlassian.theplugin.commons.remoteapi.rest.HttpSessionCallbackImpl;
import com.atlassian.theplugin.commons.util.Logger;
import org.jetbrains.annotations.NotNull;

import java.util.*;


/**
 * Class used for communication wiht Bamboo Server.
 *
 * @author sginter + others
 *         Date: Jan 15, 2008
 */
public final class BambooServerFacadeImpl implements BambooServerFacade {
	private Map<String, BambooSession> sessions = new WeakHashMap<String, BambooSession>();
	private Logger loger;

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

	public boolean isBamboo2(final ServerData serverData) {
		BambooSession session = null;
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

	private synchronized BambooSession getSession(ServerData server) throws RemoteApiException {
		// @todo old server will stay on map - remove them !!!
		String key = server.getUserName() + server.getUrl() + server.getPassword();
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
		BambooSession apiHandler = bambooSessionFactory.createSession(serverData, callback);
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
	public Collection<BambooProject> getProjectList(ServerData bambooServer) throws ServerPasswordNotProvidedException
			, RemoteApiException {
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
	public Collection<BambooPlan> getPlanList(ServerData bambooServer)
			throws ServerPasswordNotProvidedException, RemoteApiException {
		BambooSession api = getSession(bambooServer);
		List<BambooPlan> plans = api.listPlanNames();
		try {
			List<String> favPlans = api.getFavouriteUserPlans();
			for (String fav : favPlans) {
				for (ListIterator<BambooPlan> it = plans.listIterator(); it.hasNext();) {
					final BambooPlan plan = it.next();
					if (plan.getPlanKey().equalsIgnoreCase(fav)) {
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
	 * List details on subscribed plans.<p>
	 * <p/>
	 * Returns info on all subscribed plans including information about failed attempt.<p>
	 * <p/>
	 * Throws ServerPasswordNotProvidedException when invoked for Server that has not had the password set, when the server
	 * returns a meaningful exception response.
	 *
	 * @param bambooServer Bamboo server information
	 * @return results on subscribed builds
	 * @throws com.atlassian.theplugin.commons.exception.ServerPasswordNotProvidedException
	 *          when invoked for Server that has not had the password set yet
	 * @see com.atlassian.theplugin.commons.bamboo.api.BambooSessionImpl#login(String, char[])
	 */
	public Collection<BambooBuild> getSubscribedPlansResults(ServerData bambooServer, final Collection<SubscribedPlan> plans,
			boolean isUseFavourities, int timezoneOffset)
			throws ServerPasswordNotProvidedException {
		Collection<BambooBuild> builds = new ArrayList<BambooBuild>();

		String connectionErrorMessage;
		BambooSession api = null;
		try {
			api = getSession(bambooServer);
			connectionErrorMessage = "";
		} catch (RemoteApiLoginFailedException e) {
			// TODO wseliga used to be bambooServer.getIsConfigInitialized() here
			if (bambooServer.getPassword().length() > 0) {
				loger.error("Bamboo login exception: " + e.getMessage());
				connectionErrorMessage = e.getMessage();
			} else {
				throw new ServerPasswordNotProvidedException(e);
			}
		} catch (RemoteApiException e) {
			loger.error("Bamboo exception: " + e.getMessage());
			connectionErrorMessage = e.getMessage();
		}

		Collection<BambooPlan> plansForServer = null;
		try {
			plansForServer = getPlanList(bambooServer);
		} catch (RemoteApiException e) {
			// can go further, no disabled info will be available
		}

		if (isUseFavourities) {
			if (plansForServer != null) {
				for (BambooPlan bambooPlan : plansForServer) {
					if (bambooPlan.isFavourite()) {
						if (api != null && api.isLoggedIn()) {
							try {
								BambooBuild buildInfo = api.getLatestBuildForPlan(bambooPlan.getPlanKey(),
										bambooPlan.isEnabled(), timezoneOffset);
								builds.add(buildInfo);
							} catch (RemoteApiException e) {
								// go ahead, there are other builds
							}
						} else {
							builds.add(constructBuildErrorInfo(bambooServer, bambooPlan.getPlanKey(), bambooPlan.getPlanName(),
									connectionErrorMessage));
						}
					}
				}
			}
		} else {
			for (SubscribedPlan plan : plans) {
				if (api != null && api.isLoggedIn()) {
					try {
						final Boolean isEnabled = plansForServer != null
								? BambooSessionImpl.isPlanEnabled(plansForServer, plan.getKey()) : null;
						BambooBuild buildInfo = api.getLatestBuildForPlan(plan.getKey(),
								isEnabled != null ? isEnabled : true, timezoneOffset);
						builds.add(buildInfo);
					} catch (RemoteApiException e) {
						// go ahead, there are other builds
					}
				} else {
					builds.add(constructBuildErrorInfo(
							bambooServer, plan.getKey(), null, connectionErrorMessage));
				}
			}
		}


		return builds;
	}

	/**
	 * List history for provided plan.
	 * <p/>
	 * Returns last 15 builds on provided plan including information about failed attempt.<p>
	 * <p/>
	 * Throws ServerPasswordNotProvidedException when invoked for Server that has not had the password set, when the server
	 * returns a meaningful exception response.
	 *
	 * @param bambooServer   Bamboo server information
	 * @param planKey		key of the plan to query
	 * @param timezoneOffset
	 * @return results on history for plan
	 * @throws com.atlassian.theplugin.commons.exception.ServerPasswordNotProvidedException
	 *          when invoked for Server that has not had the password set yet
	 * @see com.atlassian.theplugin.commons.bamboo.api.BambooSessionImpl#login(String, char[])
	 */
	public Collection<BambooBuild> getRecentBuildsForPlans(ServerData bambooServer, String planKey, final int timezoneOffset)
			throws ServerPasswordNotProvidedException {
		Collection<BambooBuild> builds = new ArrayList<BambooBuild>();

		BambooSession api;
		try {
			api = getSession(bambooServer);
		} catch (RemoteApiLoginFailedException e) {
			// TODO wseliga used to be bambooServer.getIsConfigInitialized() here
			if (bambooServer.getPassword().length() > 0) {
				loger.error("Bamboo login exception: " + e.getMessage());
				builds.add(constructBuildErrorInfo(bambooServer, planKey, null, e.getMessage()));
				return builds;
			} else {
				throw new ServerPasswordNotProvidedException(e);
			}
		} catch (RemoteApiException e) {
			loger.error("Bamboo exception: " + e.getMessage());
			builds.add(constructBuildErrorInfo(bambooServer, planKey, null, e.getMessage()));
			return builds;
		}

		try {
			builds.addAll(api.getRecentBuildsForPlan(planKey, timezoneOffset));
		} catch (RemoteApiException e) {
			loger.error("Bamboo exception: " + e.getMessage());
			builds.add(constructBuildErrorInfo(bambooServer, planKey, null, e.getMessage()));
		}

		return builds;
	}

	/**
	 * List history for current user.<p>
	 * <p/>
	 * Returns last builds selected user including information about failed attempt.<p>
	 * <p/>
	 * Throws ServerPasswordNotProvidedException when invoked for Server that has not had the password set, when the server
	 * returns a meaningful exception response.
	 *
	 * @param bambooServer Bamboo server information
	 * @return results on history for plan
	 * @throws com.atlassian.theplugin.commons.exception.ServerPasswordNotProvidedException
	 *          when invoked for Server that has not had the password set yet
	 * @see com.atlassian.theplugin.commons.bamboo.api.BambooSessionImpl#login(String, char[])
	 */
	public Collection<BambooBuild> getRecentBuildsForUser(ServerData bambooServer, final int timezoneOffset)
			throws ServerPasswordNotProvidedException {
		Collection<BambooBuild> builds = new ArrayList<BambooBuild>();

		BambooSession api;
		try {
			api = getSession(bambooServer);
		} catch (RemoteApiLoginFailedException e) {
			// TODO wseliga used to be bambooServer.getIsConfigInitialized() here
			if (bambooServer.getPassword().length() > 0) {
				loger.error("Bamboo login exception: " + e.getMessage());
				builds.add(constructBuildErrorInfo(
						bambooServer, "", null, e.getMessage()));
				return builds;
			} else {
				throw new ServerPasswordNotProvidedException(e);
			}
		} catch (RemoteApiException e) {
			loger.error("Bamboo exception: " + e.getMessage());
			builds.add(constructBuildErrorInfo(
					bambooServer, "", null, e.getMessage()));
			return builds;
		}

		try {
			builds.addAll(api.getRecentBuildsForUser(timezoneOffset));
		} catch (RemoteApiException e) {
			loger.error("Bamboo exception: " + e.getMessage());
			builds.add(constructBuildErrorInfo(
					bambooServer, "", null, e.getMessage()));
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
	public BuildDetails getBuildDetails(ServerData bambooServer, @NotNull String planKey, int buildNumber)
			throws ServerPasswordNotProvidedException, RemoteApiException {
		try {
			BambooSession api = getSession(bambooServer);
			return api.getBuildResultDetails(planKey, buildNumber);
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
	public void addLabelToBuild(ServerData bambooServer, @NotNull String planKey, int buildNumber, String buildLabel)
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
	public void addCommentToBuild(ServerData bambooServer, @NotNull String planKey, int buildNumber, String buildComment)
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
	public void executeBuild(ServerData bambooServer, @NotNull String buildKey)
			throws ServerPasswordNotProvidedException, RemoteApiException {
		try {
			BambooSession api = getSession(bambooServer);
			api.executeBuild(buildKey);
		} catch (RemoteApiException e) {
			loger.info("Bamboo exception: " + e.getMessage());
			throw e;
		}
	}

	public String getBuildLogs(ServerData bambooServer, @NotNull String planKey, int buildNumber)
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
	public Collection<String> getFavouritePlans(ServerData bambooServer)
			throws ServerPasswordNotProvidedException, RemoteApiException {
		try {
			return getSession(bambooServer).getFavouriteUserPlans();
		} catch (RemoteApiException e) {
			loger.error("Bamboo exception: " + e.getMessage());
			throw e;
		}
	}


	private BambooBuild constructBuildErrorInfo(ServerData server, @NotNull String planKey,
			String planName, String message) {
		return new BambooBuildInfo.Builder(planKey, null, server, planName, null, BuildStatus.UNKNOWN)
				.errorMessage(message)
				.pollingTime(new Date())
				.build();
	}

	public void setCallback(HttpSessionCallback callback) {
		this.callback = callback;
	}


	private static class SimpleBambooSessionFactory implements BambooSessionFactory {

		public BambooSession createSession(final ServerData serverData, final HttpSessionCallback callback)
				throws RemoteApiException {
			return new AutoRenewBambooSession(serverData, callback);
		}
	}
}
