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

package com.atlassian.theplugin.commons.crucible;

import com.atlassian.theplugin.commons.ServerType;
import com.atlassian.theplugin.commons.cfg.CrucibleServerCfg;
import com.atlassian.theplugin.commons.cfg.ServerCfg;
import com.atlassian.theplugin.commons.cfg.ServerId;
import com.atlassian.theplugin.commons.crucible.api.CrucibleLoginException;
import com.atlassian.theplugin.commons.crucible.api.CrucibleSession;
import com.atlassian.theplugin.commons.crucible.api.UploadItem;
import com.atlassian.theplugin.commons.crucible.api.model.*;
import com.atlassian.theplugin.commons.crucible.api.rest.CrucibleSessionImpl;
import com.atlassian.theplugin.commons.exception.ServerPasswordNotProvidedException;
import com.atlassian.theplugin.commons.remoteapi.RemoteApiException;
import com.atlassian.theplugin.commons.remoteapi.rest.HttpSessionCallback;
import com.atlassian.theplugin.commons.remoteapi.rest.HttpSessionCallbackImpl;
import com.atlassian.theplugin.commons.util.MiscUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class CrucibleServerFacadeImpl implements CrucibleServerFacade {
	private Map<String, CrucibleSession> sessions = new HashMap<String, CrucibleSession>();

	private static CrucibleServerFacadeImpl instance;
	private CrucibleUserCache userCache;
	private HttpSessionCallback callback;

	protected CrucibleServerFacadeImpl(CrucibleUserCache userCache) {
		this.userCache = userCache;
		this.callback = new HttpSessionCallbackImpl();
	}

	public static synchronized CrucibleServerFacade getInstance() {
		if (instance == null) {
			instance = new CrucibleServerFacadeImpl(CrucibleUserCacheImpl.getInstance());
		}
		return instance;
	}

	public void setUserCache(CrucibleUserCache newCache) {
		userCache = newCache;
	}

	public ServerType getServerType() {
		return ServerType.CRUCIBLE_SERVER;
	}

	protected synchronized CrucibleSession getSession(CrucibleServerCfg server)
			throws RemoteApiException, ServerPasswordNotProvidedException {
		String key = server.getUrl() + server.getCurrentUsername() + server.getCurrentPassword();
		CrucibleSession session = sessions.get(key);
		if (session == null) {
			try {
				session = new CrucibleSessionImpl(server, callback);
				sessions.put(key, session);
			} catch (RemoteApiException e) {
				if (server.getCurrentPassword().length() > 0) {
					throw e;
				} else {
					throw new ServerPasswordNotProvidedException();
				}
			}
		}
		if (!session.isLoggedIn()) {
			session.login();
		}
		return session;
	}

	private <T extends CommentBean> void fixUserName(CrucibleServerCfg server, T comment) {
		User u = comment.getAuthor();
		if (u.getDisplayName() == null || u.getDisplayName().length() == 0) {
			User newU = userCache.getUser(server, u.getUserName(), true);
			if (newU != null) {
				comment.setAuthor(newU);
			}
		}
	}

	@Nullable
	public String getDisplayName(@NotNull final CrucibleServerCfg server, @NotNull String username) {
		final User user = userCache.getUser(server, username, true);
		return user != null ? user.getDisplayName() : null;
	}

	public CrucibleProject getProject(@NotNull final CrucibleServerCfg server, @NotNull final String projectKey)
			throws RemoteApiException, ServerPasswordNotProvidedException {
		final List<CrucibleProject> projects = getProjects(server);
		for (CrucibleProject project : projects) {
			if (project.getKey().equals(projectKey)) {
				return project;
			}
		}
		return null;
	}

	public boolean checkContentUrlAvailable(final CrucibleServerCfg server)
			throws RemoteApiException, ServerPasswordNotProvidedException {
		CrucibleSession session = getSession(server);
		return session.checkContentUrlAvailable();
	}

	public Review createReviewFromUpload(@NotNull final CrucibleServerCfg server, @NotNull final Review review,
			@NotNull final Collection<UploadItem> uploadItems)
			throws RemoteApiException, ServerPasswordNotProvidedException {
		CrucibleSession session = getSession(server);
		return session.createReviewFromUpload(review, uploadItems);
	}

	public byte[] getFileContent(@NotNull final CrucibleServerCfg server, @NotNull final String contentUrl)
			throws RemoteApiException, ServerPasswordNotProvidedException {
		CrucibleSession session = getSession(server);
		return session.getFileContent(contentUrl);
	}

	/**
	 * For testing Only
	 */
	public void testServerConnection(String url, String userName, String password) throws RemoteApiException {
		CrucibleServerCfg serverCfg = new CrucibleServerCfg(url, new ServerId());
		serverCfg.setUrl(url);
		serverCfg.setUsername(userName);
		serverCfg.setPassword(password);
		testServerConnection(serverCfg);
	}

	/**
	 * @param serverCfg The configuration for the server that we want to test the connectio for
	 * @throws com.atlassian.theplugin.commons.crucible.api.CrucibleException
	 *
	 */
	public void testServerConnection(ServerCfg serverCfg) throws RemoteApiException {
		assert serverCfg instanceof CrucibleServerCfg;
		final CrucibleSession session = new CrucibleSessionImpl(serverCfg, callback);
		session.login();
		try {
			session.getServerVersion();
		} catch (RemoteApiException e) {
			if (e.getCause().getMessage().startsWith("HTTP 500")) {
				throw new CrucibleLoginException(
						"Atlassian IntelliJ Connector detected a Crucible version older\n"
								+ "than 1.6. Unfortunately, the plugin will not\n"
								+ "work with this version of Crucible");
			}
		}
		session.logout();
	}

//	public CrucibleVersionInfo getServerVersion(CrucibleServerCfg server)
//			throws RemoteApiException, ServerPasswordNotProvidedException {
//		CrucibleSession session = getSession(server);
//		return session.getServerVersion();
//	}

	/**
	 * Creates new review in Crucible
	 *
	 * @param server
	 * @param review data for new review to create (some fields have to be set e.g. projectKey)
	 * @return created revew date
	 * @throws com.atlassian.theplugin.commons.crucible.api.CrucibleException
	 *          in case of createReview error or CrucibleLoginException in case of login error
	 */
	public Review createReview(CrucibleServerCfg server, Review review)
			throws RemoteApiException, ServerPasswordNotProvidedException {
		CrucibleSession session = getSession(server);
		return session.createReview(review);
	}

	public Review createReviewFromRevision(
			CrucibleServerCfg server,
			Review review,
			List<String> revisions) throws RemoteApiException, ServerPasswordNotProvidedException {
		CrucibleSession session = getSession(server);
		Review newReview = null;
		newReview = session.createReviewFromRevision(review, revisions);

		return newReview;
	}

	public Review addRevisionsToReview(
			CrucibleServerCfg server,
			PermId permId,
			String repository,
			List<String> revisions) throws RemoteApiException, ServerPasswordNotProvidedException {
		CrucibleSession session = getSession(server);
		Review review = null;
		if (!revisions.isEmpty()) {
			review = session.addRevisionsToReview(permId, repository, revisions);
		}
		return review;
	}

	public Review addPatchToReview(
			CrucibleServerCfg server,
			PermId permId,
			String repository,
			String patch) throws RemoteApiException, ServerPasswordNotProvidedException {
		CrucibleSession session = getSession(server);
		Review review = session.addPatchToReview(permId, repository, patch);
		return review;
	}

	public Review addItemsToReview(
			CrucibleServerCfg server,
			PermId permId,
			Collection<UploadItem> items) throws RemoteApiException, ServerPasswordNotProvidedException {
		CrucibleSession session = getSession(server);
		session.addItemsToReview(permId, items);
		return session.getReview(permId, true);
	}

	public void addReviewers(
			CrucibleServerCfg server,
			PermId permId,
			Set<String> userNames) throws RemoteApiException, ServerPasswordNotProvidedException {
		CrucibleSession session = getSession(server);
		session.addReviewers(permId, userNames);
	}

	public void removeReviewer(
			CrucibleServerCfg server,
			PermId permId,
			String userName) throws RemoteApiException, ServerPasswordNotProvidedException {
		CrucibleSession session = getSession(server);
		session.removeReviewer(permId, userName);
	}

	private boolean contains(Set<Reviewer> reviewers, String username) {
		for (Reviewer reviewer : reviewers) {
			if (reviewer.getUserName().equals(username)) {
				return true;
			}
		}
		return false;
	}

	public void setReviewers(@NotNull final CrucibleServerCfg server, @NotNull final PermId permId,
			@NotNull final Collection<String> aUsernames) throws RemoteApiException, ServerPasswordNotProvidedException {
		final Set<String> reviewersForAdd = MiscUtil.buildHashSet();
		final Set<String> reviewersForRemove = MiscUtil.buildHashSet();
		final Review review = getReview(server, permId);
		// removing potential duplicates
		final Set<String> usernames = MiscUtil.buildHashSet(aUsernames);

		try {
			for (String username : usernames) {
				if (!contains(review.getReviewers(), username)) {
					reviewersForAdd.add(username);
				}
			}

			for (Reviewer reviewer : review.getReviewers()) {
				if (!usernames.contains(reviewer.getUserName())) {
					reviewersForRemove.add(reviewer.getUserName());
				}
			}


			if (!reviewersForAdd.isEmpty()) {
				addReviewers(server, permId, reviewersForAdd);
			}
			if (!reviewersForRemove.isEmpty()) {
				for (String reviewer : reviewersForRemove) {
					removeReviewer(server, permId, reviewer);
				}
			}
		} catch (ValueNotYetInitialized e) {
			throw new RemoteApiException(e);
		}
	}

	public Review approveReview(
			CrucibleServerCfg server,
			PermId permId) throws RemoteApiException, ServerPasswordNotProvidedException {
		CrucibleSession session = getSession(server);
		return session.approveReview(permId);
	}

	public Review submitReview(
			CrucibleServerCfg server,
			PermId permId) throws RemoteApiException, ServerPasswordNotProvidedException {
		CrucibleSession session = getSession(server);
		return session.submitReview(permId);
	}

	public Review summarizeReview(CrucibleServerCfg server, PermId permId)
			throws RemoteApiException, ServerPasswordNotProvidedException {
		CrucibleSession session = getSession(server);
		return session.summarizeReview(permId);
	}

	public Review abandonReview(CrucibleServerCfg server, PermId permId)
			throws RemoteApiException, ServerPasswordNotProvidedException {
		CrucibleSession session = getSession(server);
		return session.abandonReview(permId);
	}

	public Review closeReview(CrucibleServerCfg server, PermId permId, String summary)
			throws RemoteApiException, ServerPasswordNotProvidedException {
		CrucibleSession session = getSession(server);
		return session.closeReview(permId, summary);
	}

	public Review recoverReview(CrucibleServerCfg server, PermId permId)
			throws RemoteApiException, ServerPasswordNotProvidedException {
		CrucibleSession session = getSession(server);
		return session.recoverReview(permId);
	}

	public Review reopenReview(CrucibleServerCfg server, PermId permId)
			throws RemoteApiException, ServerPasswordNotProvidedException {
		CrucibleSession session = getSession(server);
		return session.reopenReview(permId);
	}

//	public Review rejectReview(CrucibleServerCfg server, PermId permId)
//			throws RemoteApiException, ServerPasswordNotProvidedException {
//		CrucibleSession session = getSession(server);
//		return session.rejectReview(permId);
//	}

	public void completeReview(CrucibleServerCfg server, PermId permId, boolean complete)
			throws RemoteApiException, ServerPasswordNotProvidedException {
		CrucibleSession session = getSession(server);
		session.completeReview(permId, complete);
	}

	/**
	 * Creates new review in Crucible
	 *
	 * @param server
	 * @param review data for new review to create (some fields have to be set e.g. projectKey)
	 * @param patch  patch to assign with the review
	 * @return created revew date
	 * @throws com.atlassian.theplugin.commons.crucible.api.CrucibleException
	 *          in case of createReview error or CrucibleLoginException in case of login error
	 */
	public Review createReviewFromPatch(CrucibleServerCfg server, Review review, String patch)
			throws RemoteApiException, ServerPasswordNotProvidedException {
		CrucibleSession session = getSession(server);
		return session.createReviewFromPatch(review, patch);
	}

	public Set<CrucibleFileInfo> getFiles(CrucibleServerCfg server, PermId permId)
			throws RemoteApiException, ServerPasswordNotProvidedException {
		CrucibleSession session = getSession(server);
		return session.getFiles(permId);
	}

//	public List<Comment> getComments(CrucibleServerCfg server, PermId permId)
//			throws RemoteApiException, ServerPasswordNotProvidedException {
//		CrucibleSession session = getSession(server);
//		return session.getComments(permId);
//	}

	public List<GeneralComment> getGeneralComments(CrucibleServerCfg server, PermId permId)
			throws RemoteApiException, ServerPasswordNotProvidedException {
		CrucibleSession session = getSession(server);
		return session.getGeneralComments(permId);
	}

	public List<VersionedComment> getVersionedComments(CrucibleServerCfg server, PermId permId)
			throws RemoteApiException, ServerPasswordNotProvidedException {
		CrucibleSession session = getSession(server);
		return session.getAllVersionedComments(permId);
	}

	public List<VersionedComment> getVersionedComments(CrucibleServerCfg server, PermId permId, PermId reviewItemId)
			throws RemoteApiException, ServerPasswordNotProvidedException {
		CrucibleSession session = getSession(server);
		return session.getVersionedComments(permId, reviewItemId);
	}

	public List<GeneralComment> getReplies(CrucibleServerCfg server, PermId permId, PermId commentId)
			throws RemoteApiException, ServerPasswordNotProvidedException {
		CrucibleSession session = getSession(server);
		return session.getReplies(permId, commentId);
	}

	public GeneralComment addGeneralComment(CrucibleServerCfg server, PermId permId, GeneralComment comment)
			throws RemoteApiException, ServerPasswordNotProvidedException {
		CrucibleSession session = getSession(server);
		GeneralCommentBean newComment = (GeneralCommentBean) session.addGeneralComment(permId, comment);
		if (newComment != null) {
			fixUserName(server, newComment);
		}
		return newComment;
	}

	public VersionedComment addVersionedComment(CrucibleServerCfg server, PermId permId, PermId riId, VersionedComment comment)
			throws RemoteApiException, ServerPasswordNotProvidedException {
		CrucibleSession session = getSession(server);
		VersionedCommentBean newComment = (VersionedCommentBean) session.addVersionedComment(permId, riId, comment);
		if (newComment != null) {
			fixUserName(server, newComment);
		}
		return newComment;
	}

	public void updateComment(CrucibleServerCfg server, PermId id, Comment comment)
			throws RemoteApiException, ServerPasswordNotProvidedException {
		CrucibleSession session = getSession(server);
		session.updateComment(id, comment);
	}

	public void publishComment(CrucibleServerCfg server, PermId reviewId, PermId commentId)
			throws RemoteApiException, ServerPasswordNotProvidedException {
		CrucibleSession session = getSession(server);
		session.publishComment(reviewId, commentId);
	}

	public void publishAllCommentsForReview(CrucibleServerCfg server, PermId reviewId)
			throws RemoteApiException, ServerPasswordNotProvidedException {
		CrucibleSession session = getSession(server);
		session.publishComment(reviewId, null);
	}

	public GeneralComment addGeneralCommentReply(CrucibleServerCfg server, PermId id, PermId cId, GeneralComment comment)
			throws RemoteApiException, ServerPasswordNotProvidedException {
		CrucibleSession session = getSession(server);
		GeneralCommentBean newReply = (GeneralCommentBean) session.addGeneralCommentReply(id, cId, comment);
		if (newReply != null) {
			fixUserName(server, newReply);
		}
		return newReply;
	}

	public VersionedComment addVersionedCommentReply(CrucibleServerCfg server, PermId id, PermId cId, VersionedComment comment)
			throws RemoteApiException, ServerPasswordNotProvidedException {
		CrucibleSession session = getSession(server);
		VersionedCommentBean newReply = (VersionedCommentBean) session.addVersionedCommentReply(id, cId, comment);
		if (newReply != null) {
			fixUserName(server, newReply);
		}
		return newReply;
	}

//	public void updateReply(CrucibleServerCfg server, PermId id, PermId cId, PermId rId, GeneralComment comment)
//			throws RemoteApiException, ServerPasswordNotProvidedException {
//		CrucibleSession session = getSession(server);
//		session.updateReply(id, cId, rId, comment);
//	}

	public void removeComment(CrucibleServerCfg server, PermId id, Comment comment)
			throws RemoteApiException, ServerPasswordNotProvidedException {
		CrucibleSession session = getSession(server);
		session.removeComment(id, comment);
	}

	public List<User> getUsers(CrucibleServerCfg server) throws RemoteApiException, ServerPasswordNotProvidedException {
		CrucibleSession session = getSession(server);
		return session.getUsers();
	}

	/**
	 * Retrieves list of projects defined on Crucible server
	 *
	 * @param server
	 * @return
	 * @throws RemoteApiException
	 * @throws ServerPasswordNotProvidedException
	 *
	 */
	public List<CrucibleProject> getProjects(CrucibleServerCfg server) throws RemoteApiException,
			ServerPasswordNotProvidedException {
		CrucibleSession session = getSession(server);
		return session.getProjectsFromCache();
	}


	/**
	 * Retrieves list of repositories defined on Crucible server
	 *
	 * @param server
	 * @return
	 * @throws RemoteApiException
	 * @throws com.atlassian.theplugin.commons.exception.ServerPasswordNotProvidedException
	 *
	 */
	public List<Repository> getRepositories(CrucibleServerCfg server)
			throws RemoteApiException, ServerPasswordNotProvidedException {
		CrucibleSession session = getSession(server);
		return session.getRepositories();
	}

	public SvnRepository getRepository(CrucibleServerCfg server, String repoName)
			throws RemoteApiException, ServerPasswordNotProvidedException {
		CrucibleSession session = getSession(server);
		return session.getRepository(repoName);
	}

	public List<CustomFieldDef> getMetrics(CrucibleServerCfg server, int version)
			throws RemoteApiException, ServerPasswordNotProvidedException {
		CrucibleSession session = getSession(server);
		return session.getMetrics(version);
	}

	/**
	 * @param server server object with Url, Login and Password to connect to
	 * @return List of reviews (empty list in case there is no review)
	 */
	public List<Review> getAllReviews(CrucibleServerCfg server) throws RemoteApiException, ServerPasswordNotProvidedException {
		CrucibleSession session = getSession(server);
		return session.getAllReviews(true);
	}

	public List<Review> getReviewsForFilter(CrucibleServerCfg server, PredefinedFilter filter)
			throws RemoteApiException, ServerPasswordNotProvidedException {
		CrucibleSession session = getSession(server);
		return session.getReviewsForFilter(filter, true);
	}

	public List<Review> getReviewsForCustomFilter(CrucibleServerCfg server, CustomFilter filter)
			throws RemoteApiException, ServerPasswordNotProvidedException {
		CrucibleSession session = getSession(server);
		return session.getReviewsForCustomFilter(filter, true);
	}

	public Review getReview(CrucibleServerCfg server, PermId permId)
			throws RemoteApiException, ServerPasswordNotProvidedException {
		CrucibleSession session = getSession(server);
		return session.getReview(permId, true);
	}

	public List<Reviewer> getReviewers(CrucibleServerCfg server, PermId permId)
			throws RemoteApiException, ServerPasswordNotProvidedException {
		CrucibleSession session = getSession(server);
		return session.getReviewers(permId);
	}

//	public List<Review> getAllReviewsForFile(CrucibleServerCfg server, String repoName, String path)
//			throws RemoteApiException, ServerPasswordNotProvidedException {
//		CrucibleSession session = getSession(server);
//		return session.getAllReviewsForFile(repoName, path, true);
//	}

	public void setCallback(HttpSessionCallback callback) {
		this.callback = callback;
	}
}
