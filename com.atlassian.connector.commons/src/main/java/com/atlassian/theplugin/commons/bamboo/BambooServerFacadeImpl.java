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
import com.atlassian.theplugin.commons.SubscribedPlan;
import com.atlassian.theplugin.commons.bamboo.api.AutoRenewBambooSession;
import com.atlassian.theplugin.commons.bamboo.api.BambooSession;
import com.atlassian.theplugin.commons.cfg.BambooServerCfg;
import com.atlassian.theplugin.commons.cfg.ServerCfg;
import com.atlassian.theplugin.commons.cfg.ServerId;
import com.atlassian.theplugin.commons.exception.ServerPasswordNotProvidedException;
import com.atlassian.theplugin.commons.remoteapi.RemoteApiException;
import com.atlassian.theplugin.commons.remoteapi.RemoteApiLoginFailedException;
import com.atlassian.theplugin.commons.remoteapi.rest.HttpSessionCallback;
import com.atlassian.theplugin.commons.remoteapi.rest.HttpSessionCallbackImpl;
import com.atlassian.theplugin.commons.util.Logger;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.ListIterator;

import org.joda.time.DateTime;
import org.jetbrains.annotations.NotNull;


/**
 * Class used for communication wiht Bamboo Server.
 * @author sginter + others
 * Date: Jan 15, 2008
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

    private synchronized BambooSession getSession(BambooServerCfg server) throws RemoteApiException {
        // @todo old server will stay on map - remove them !!!
        String key = server.getUsername() + server.getUrl() + server.getPassword();
        BambooSession session = sessions.get(key);
        if (session == null) {
            session = bambooSessionFactory.createSession(server, callback);
            sessions.put(key, session);
        }
        if (!session.isLoggedIn()) {
            session.login(server.getUsername(), server.getPassword().toCharArray());
            try {
                if (session.getBamboBuildNumber() > 0) {
                    server.setIsBamboo2(true);
                } else {
                    server.setIsBamboo2(false);
                }
            } catch (RemoteApiException e) {
                // can not validate as Bamboo 2
                server.setIsBamboo2(false);
            }
        }
        return session;
    }

    /**
     * For testing Only
     * @see com.atlassian.theplugin.commons.remoteapi.ProductServerFacade#testServerConnection(java.lang.String,
	 * java.lang.String, java.lang.String)
     */
    public void testServerConnection(String url, String userName, String password) throws RemoteApiException {
    	BambooServerCfg serverCfg = new BambooServerCfg(url, new ServerId());
    	serverCfg.setUrl(url);
    	serverCfg.setUsername(userName);
    	serverCfg.setPassword(password);
    	testServerConnection(serverCfg);
    }
    
	/**
     * Test connection to Bamboo server.
     *
     * @param serverCfg The configuration for the server that we want to test the connectio for
     * 
     * @throws RemoteApiException on failed login
     * @see RemoteApiLoginFailedException
     */
    public void testServerConnection(ServerCfg serverCfg) throws RemoteApiException {
    	assert serverCfg instanceof BambooServerCfg;
    	BambooSession apiHandler = bambooSessionFactory.createSession((BambooServerCfg) serverCfg, callback);
        apiHandler.login(serverCfg.getUsername(), serverCfg.getPassword().toCharArray());
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
    public Collection<BambooProject> getProjectList(BambooServerCfg bambooServer) throws ServerPasswordNotProvidedException
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
    public Collection<BambooPlan> getPlanList(BambooServerCfg bambooServer)
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
    public Collection<BambooBuild> getSubscribedPlansResults(BambooServerCfg bambooServer)
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
                throw new ServerPasswordNotProvidedException();
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

        if (bambooServer.isUseFavourites()) {
            if (plansForServer != null) {
                for (BambooPlan bambooPlan : plansForServer) {
                    if (bambooPlan.isFavourite()) {
                        if (api != null && api.isLoggedIn()) {
                            try {
                                BambooBuildInfo buildInfo = api.getLatestBuildForPlan(bambooPlan.getPlanKey());
                                buildInfo.setServer(bambooServer);
                                buildInfo.setEnabled(bambooPlan.isEnabled());

								// now adjust the time for local caller time, as Bamboo servers always serves its local time
								// without the timezone info
								adjustBuildTimes(bambooServer, buildInfo);

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
            for (SubscribedPlan plan : bambooServer.getSubscribedPlans()) {
                if (api != null && api.isLoggedIn()) {
                    try {
                        BambooBuildInfo buildInfo = api.getLatestBuildForPlan(plan.getPlanId());
                        buildInfo.setEnabled(true);
                        buildInfo.setServer(bambooServer);
						adjustBuildTimes(bambooServer, buildInfo);

                        if (plansForServer != null) {
                            for (BambooPlan bambooPlan : plansForServer) {
                                if (plan.getPlanId().equals(bambooPlan.getPlanKey())) {
                                    buildInfo.setEnabled(bambooPlan.isEnabled());
                                }
                            }
                        }
                        builds.add(buildInfo);
                    } catch (RemoteApiException e) {
                        // go ahead, there are other builds
                    }
                } else {
                    builds.add(constructBuildErrorInfo(
                            bambooServer, plan.getPlanId(), null, connectionErrorMessage));
                }
            }
        }


        return builds;
    }

	private void adjustBuildTimes(final BambooServerCfg bambooServer, final BambooBuildInfo buildInfo) {
		final Date buildCompletedDate = buildInfo.getBuildCompletedDate();
		if (buildCompletedDate != null) {
			buildInfo.setBuildCompletedDate(new DateTime(buildCompletedDate.getTime())
					.plusHours(bambooServer.getTimezoneOffset()).toDate());

		}
	}

	/**
     * @param bambooServer server data
     * @param buildKey key of the build
     * @param buildNumber unique number of the build
     * @return build data
     * @throws ServerPasswordNotProvidedException
     *
     * @throws RemoteApiException
     */
    public BuildDetails getBuildDetails(BambooServerCfg bambooServer, String buildKey, String buildNumber)
            throws ServerPasswordNotProvidedException, RemoteApiException {
        try {
            BambooSession api = getSession(bambooServer);
            return api.getBuildResultDetails(buildKey, buildNumber);
        } catch (RemoteApiException e) {
            loger.info("Bamboo exception: " + e.getMessage());
            throw e;
        }
    }

    /**
	 * @param bambooServer server data
	 * @param buildKey key of the build
	 * @param buildNumber unique number of the build
     * @param buildLabel label to add to the build
     * @throws ServerPasswordNotProvidedException
     *
     * @throws RemoteApiException
     */
    public void addLabelToBuild(BambooServerCfg bambooServer, String buildKey, String buildNumber, String buildLabel)
            throws ServerPasswordNotProvidedException, RemoteApiException {
        try {
            BambooSession api = getSession(bambooServer);
            api.addLabelToBuild(buildKey, buildNumber, buildLabel);
        } catch (RemoteApiException e) {
            loger.info("Bamboo exception: " + e.getMessage());
            throw e;
        }
    }

    /**
	 * @param bambooServer server data
	 * @param buildKey key of the build
	 * @param buildNumber unique number of the build
     * @param buildComment user comment to add to the build
     * @throws ServerPasswordNotProvidedException
     *
     * @throws RemoteApiException
     */
    public void addCommentToBuild(BambooServerCfg bambooServer, String buildKey, String buildNumber, String buildComment)
            throws ServerPasswordNotProvidedException, RemoteApiException {
        try {
            BambooSession api = getSession(bambooServer);
            api.addCommentToBuild(buildKey, buildNumber, buildComment);
        } catch (RemoteApiException e) {
            loger.info("Bamboo exception: " + e.getMessage());
            throw e;
        }
    }

    /**
	 * Runs selected plan
	 * @param bambooServer server data
	 * @param buildKey key of the build
     * @throws ServerPasswordNotProvidedException
     *
     * @throws RemoteApiException
     */
    public void executeBuild(BambooServerCfg bambooServer, String buildKey)
            throws ServerPasswordNotProvidedException, RemoteApiException {
        try {
            BambooSession api = getSession(bambooServer);
            api.executeBuild(buildKey);
        } catch (RemoteApiException e) {
            loger.info("Bamboo exception: " + e.getMessage());
            throw e;
        }
    }

    public byte[] getBuildLogs(BambooServerCfg bambooServer, String buildKey, String buildNumber)
            throws ServerPasswordNotProvidedException, RemoteApiException {
        try {
            BambooSession api = getSession(bambooServer);
            return api.getBuildLogs(buildKey, buildNumber);
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
	 * @throws com.atlassian.theplugin.commons.remoteapi.RemoteApiException in case of some IO or similar problem
     */
    public Collection<String> getFavouritePlans(BambooServerCfg bambooServer)
            throws ServerPasswordNotProvidedException, RemoteApiException {
        try {
            return getSession(bambooServer).getFavouriteUserPlans();
        } catch (RemoteApiException e) {
            loger.error("Bamboo exception: " + e.getMessage());
            throw e;
        }
    }


    private BambooBuild constructBuildErrorInfo(BambooServerCfg server, String planKey, String planName, String message) {
        BambooBuildInfo buildInfo = new BambooBuildInfo(planKey, null, server.getUrl(), planName);

        buildInfo.setServer(server);
        buildInfo.setBuildState(BuildStatus.UNKNOWN.toString());
		buildInfo.setMessage(message);
		buildInfo.setPollingTime(new Date());

		return buildInfo;
	}
    
	public void setCallback(HttpSessionCallback callback) {
		this.callback = callback;
	}


	private static class SimpleBambooSessionFactory implements BambooSessionFactory {

		public BambooSession createSession(final BambooServerCfg serverCfg, final HttpSessionCallback callback)
				throws RemoteApiException {
			return new AutoRenewBambooSession(serverCfg, callback);
		}
	}
}
