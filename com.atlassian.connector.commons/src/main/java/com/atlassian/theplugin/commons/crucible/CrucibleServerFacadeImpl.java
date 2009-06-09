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
import com.atlassian.theplugin.commons.cfg.ServerId;
import com.atlassian.theplugin.commons.crucible.api.CrucibleLoginException;
import com.atlassian.theplugin.commons.crucible.api.CrucibleSession;
import com.atlassian.theplugin.commons.crucible.api.UploadItem;
import com.atlassian.theplugin.commons.crucible.api.model.*;
import com.atlassian.theplugin.commons.crucible.api.rest.CrucibleSessionImpl;
import com.atlassian.theplugin.commons.exception.ServerPasswordNotProvidedException;
import com.atlassian.theplugin.commons.remoteapi.RemoteApiException;
import com.atlassian.theplugin.commons.remoteapi.RemoteApiMalformedUrlException;
import com.atlassian.theplugin.commons.remoteapi.ServerData;
import com.atlassian.theplugin.commons.remoteapi.rest.HttpSessionCallback;
import com.atlassian.theplugin.commons.remoteapi.rest.HttpSessionCallbackImpl;
import com.atlassian.theplugin.commons.util.MiscUtil;
import com.atlassian.theplugin.commons.util.UrlUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class CrucibleServerFacadeImpl implements CrucibleServerFacade {
	private final Map<String, CrucibleSession> sessions = new HashMap<String, CrucibleSession>();

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

	protected synchronized CrucibleSession getSession(ServerData server) throws RemoteApiException,
			ServerPasswordNotProvidedException {
		String key = server.getUrl() + server.getUserName() + server.getPassword();
		CrucibleSession session = sessions.get(key);
		if (session == null) {
			try {
				session = new CrucibleSessionImpl(server, callback);
				// workaround for ACC-31
				if (!session.isLoggedIn()) {
					session.login();
				}
				sessions.put(key, session);
			} catch (RemoteApiMalformedUrlException e) {
				if (server.getPassword().length() > 0 || !UrlUtil.isUrlValid(server.getUrl())) {
					throw e;
				} else {
					// this is probably never thrown
					// todo remove it
					throw new ServerPasswordNotProvidedException(e);
				}
			}
		}
		return session;
	}

	private <T extends CommentBean> void fixUserName(ServerData server, T comment) {
		User u = comment.getAuthor();
		if (u.getDisplayName() == null || u.getDisplayName().length() == 0) {
			User newU = userCache.getUser(server, u.getUserName(), true);
			if (newU != null) {
				comment.setAuthor(newU);
			}
		}
	}

	@Nullable
	public String getDisplayName(@NotNull final ServerData server, @NotNull String username) {
		final User user = userCache.getUser(server, username, true);
		return user != null ? user.getDisplayName() : null;
	}

	public CrucibleProject getProject(@NotNull final ServerData server, @NotNull final String projectKey)
			throws RemoteApiException, ServerPasswordNotProvidedException {
		final List<CrucibleProject> projects = getProjects(server);
		for (CrucibleProject project : projects) {
			if (project.getKey().equals(projectKey)) {
				return project;
			}
		}
		return null;
	}

	public boolean checkContentUrlAvailable(final ServerData server) throws RemoteApiException,
			ServerPasswordNotProvidedException {
		CrucibleSession session = getSession(server);
		return session.checkContentUrlAvailable();
	}

	public Review createReviewFromUpload(@NotNull final ServerData server, @NotNull final Review review,
			@NotNull final Collection<UploadItem> uploadItems) throws RemoteApiException,
			ServerPasswordNotProvidedException {
		CrucibleSession session = getSession(server);
		return session.createReviewFromUpload(review, uploadItems);
	}

	public byte[] getFileContent(@NotNull final ServerData server, @NotNull final String contentUrl)
			throws RemoteApiException, ServerPasswordNotProvidedException {
		CrucibleSession session = getSession(server);
		return session.getFileContent(contentUrl);
	}

	/**
	 * For testing Only
	 */
	public void testServerConnection(String url, String userName, String password) throws RemoteApiException {
		ServerData serverData = new ServerData("unknown", (new ServerId()).toString(), userName, password, url);
		testServerConnection(serverData);
	}

	/**
	 * @param serverCfg The configuration for the server that we want to test the connection for
	 * @throws com.atlassian.theplugin.commons.crucible.api.CrucibleException
	 *                            if Crucible version is not supported
	 * @throws RemoteApiException if it's not possible to authenticate user on specified server
	 */
	public void testServerConnection(ServerData serverCfg) throws RemoteApiException {
		final CrucibleSession session = new CrucibleSessionImpl(serverCfg, callback);
		session.login();
		try {
			session.getServerVersion();
		} catch (RemoteApiException e) {
			// getServerVersion tries to login again due to https://studio.atlassian.com/browse/ACC-31
			// if it fails it will throw RemoteApiLoginException which doesn't have Cause
			if (e.getCause() != null && e.getCause().getMessage() != null
					&& e.getCause().getMessage().startsWith("HTTP 500")) {
				throw new CrucibleLoginException("Atlassian IntelliJ Connector detected a Crucible version older\n"
						+ "than 1.6. Unfortunately, the plugin will not\n" + "work with this version of Crucible");
			}

			throw e;
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
	public Review createReview(ServerData server, Review review) throws RemoteApiException,
			ServerPasswordNotProvidedException {
		CrucibleSession session = getSession(server);
		return session.createReview(review);
	}

	public Review createReviewFromRevision(ServerData server, Review review, List<String> revisions)
			throws RemoteApiException, ServerPasswordNotProvidedException {
		CrucibleSession session = getSession(server);
		Review newReview = null;
		newReview = session.createReviewFromRevision(review, revisions);

		return newReview;
	}

	public Review addRevisionsToReview(ServerData server, PermId permId, String repository, List<String> revisions)
			throws RemoteApiException, ServerPasswordNotProvidedException {
		CrucibleSession session = getSession(server);
		Review review = null;
		if (!revisions.isEmpty()) {
			review = session.addRevisionsToReview(permId, repository, revisions);
		}
		return review;
	}

	public Review addPatchToReview(ServerData server, PermId permId, String repository, String patch)
			throws RemoteApiException, ServerPasswordNotProvidedException {
		CrucibleSession session = getSession(server);
		Review review = session.addPatchToReview(permId, repository, patch);
		return review;
	}

	public Review addItemsToReview(ServerData server, PermId permId, Collection<UploadItem> items)
			throws RemoteApiException, ServerPasswordNotProvidedException {
		CrucibleSession session = getSession(server);
		session.addItemsToReview(permId, items);
		return session.getReview(permId);
	}

	public void addReviewers(ServerData server, PermId permId, Set<String> userNames) throws RemoteApiException,
			ServerPasswordNotProvidedException {
		CrucibleSession session = getSession(server);
		session.addReviewers(permId, userNames);
	}

	public void removeReviewer(ServerData server, PermId permId, String userName) throws RemoteApiException,
			ServerPasswordNotProvidedException {
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

	public void setReviewers(@NotNull final ServerData server, @NotNull final PermId permId,
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

	public Review approveReview(ServerData server, PermId permId) throws RemoteApiException,
			ServerPasswordNotProvidedException {
		CrucibleSession session = getSession(server);
		return session.approveReview(permId);
	}

	public Review submitReview(ServerData server, PermId permId) throws RemoteApiException,
			ServerPasswordNotProvidedException {
		CrucibleSession session = getSession(server);
		return session.submitReview(permId);
	}

	public Review summarizeReview(ServerData server, PermId permId) throws RemoteApiException,
			ServerPasswordNotProvidedException {
		CrucibleSession session = getSession(server);
		return session.summarizeReview(permId);
	}

	public Review abandonReview(ServerData server, PermId permId) throws RemoteApiException,
			ServerPasswordNotProvidedException {
		CrucibleSession session = getSession(server);
		return session.abandonReview(permId);
	}

	public Review closeReview(ServerData server, PermId permId, String summary) throws RemoteApiException,
			ServerPasswordNotProvidedException {
		CrucibleSession session = getSession(server);
		return session.closeReview(permId, summary);
	}

	public Review recoverReview(ServerData server, PermId permId) throws RemoteApiException,
			ServerPasswordNotProvidedException {
		CrucibleSession session = getSession(server);
		return session.recoverReview(permId);
	}

	public Review reopenReview(ServerData server, PermId permId) throws RemoteApiException,
			ServerPasswordNotProvidedException {
		CrucibleSession session = getSession(server);
		return session.reopenReview(permId);
	}

//	public Review rejectReview(CrucibleServerCfg server, PermId permId)
//			throws RemoteApiException, ServerPasswordNotProvidedException {
//		CrucibleSession session = getSession(server);
//		return session.rejectReview(permId);
//	}

	public void completeReview(ServerData server, PermId permId, boolean complete) throws RemoteApiException,
			ServerPasswordNotProvidedException {
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
	public Review createReviewFromPatch(ServerData server, Review review, String patch) throws RemoteApiException,
			ServerPasswordNotProvidedException {
		CrucibleSession session = getSession(server);
		return session.createReviewFromPatch(review, patch);
	}

	public Set<CrucibleFileInfo> getFiles(ServerData server, PermId permId) throws RemoteApiException,
			ServerPasswordNotProvidedException {
		CrucibleSession session = getSession(server);
		return session.getFiles(permId);
	}

	/**
	 * Add info about files, versioned comments and general comments to the review
	 *
	 * @param reviewItem review to fill with details
	 * @throws RemoteApiException
	 * @throws ServerPasswordNotProvidedException
	 *
	 */
	public void getDetailsForReview(final ReviewAdapter reviewItem)
			throws RemoteApiException, ServerPasswordNotProvidedException {

		reviewItem.setGeneralComments(getGeneralComments(reviewItem.getServerData(), reviewItem.getPermId()));

		List<VersionedComment> comments;
		comments = getVersionedComments(reviewItem.getServerData(), reviewItem.getPermId());

		Set<CrucibleFileInfo> files;
		files = getFiles(reviewItem.getServerData(), reviewItem.getPermId());

		reviewItem.setFilesAndVersionedComments(files, comments);
	}

	//	public List<Comment> getComments(CrucibleServerCfg server, PermId permId)
//			throws RemoteApiException, ServerPasswordNotProvidedException {
//		CrucibleSession session = getSession(server);
//		return session.getComments(permId);
//	}

	public List<GeneralComment> getGeneralComments(ServerData server, PermId permId) throws RemoteApiException,
			ServerPasswordNotProvidedException {
		CrucibleSession session = getSession(server);
		return session.getGeneralComments(permId);
	}

	public List<VersionedComment> getVersionedComments(ServerData server, PermId permId) throws RemoteApiException,
			ServerPasswordNotProvidedException {
		CrucibleSession session = getSession(server);
		return session.getAllVersionedComments(permId);
	}

	public List<VersionedComment> getVersionedComments(ServerData server, PermId permId, PermId reviewItemId)
			throws RemoteApiException, ServerPasswordNotProvidedException {
		CrucibleSession session = getSession(server);
		return session.getVersionedComments(permId, reviewItemId);
	}

	public List<GeneralComment> getReplies(ServerData server, PermId permId, PermId commentId)
			throws RemoteApiException, ServerPasswordNotProvidedException {
		CrucibleSession session = getSession(server);
		return session.getReplies(permId, commentId);
	}

	public GeneralComment addGeneralComment(ServerData server, PermId permId, GeneralComment comment)
			throws RemoteApiException, ServerPasswordNotProvidedException {
		CrucibleSession session = getSession(server);
		GeneralCommentBean newComment = (GeneralCommentBean) session.addGeneralComment(permId, comment);
		if (newComment != null) {
			fixUserName(server, newComment);
		}
		return newComment;
	}

	public VersionedComment addVersionedComment(ServerData server, PermId permId, PermId riId, VersionedComment comment)
			throws RemoteApiException, ServerPasswordNotProvidedException {
		CrucibleSession session = getSession(server);
		VersionedCommentBean newComment = (VersionedCommentBean) session.addVersionedComment(permId, riId, comment);
		if (newComment != null) {
			fixUserName(server, newComment);
		}
		return newComment;
	}

	public void updateComment(ServerData server, PermId id, Comment comment) throws RemoteApiException,
			ServerPasswordNotProvidedException {
		CrucibleSession session = getSession(server);
		session.updateComment(id, comment);
	}

	public void publishComment(ServerData server, PermId reviewId, PermId commentId) throws RemoteApiException,
			ServerPasswordNotProvidedException {
		CrucibleSession session = getSession(server);
		session.publishComment(reviewId, commentId);
	}

	public void publishAllCommentsForReview(ServerData server, PermId reviewId) throws RemoteApiException,
			ServerPasswordNotProvidedException {
		CrucibleSession session = getSession(server);
		session.publishComment(reviewId, null);
	}

	public GeneralComment addGeneralCommentReply(ServerData server, PermId id, PermId cId, GeneralComment comment)
			throws RemoteApiException, ServerPasswordNotProvidedException {
		CrucibleSession session = getSession(server);
		GeneralCommentBean newReply = (GeneralCommentBean) session.addGeneralCommentReply(id, cId, comment);
		if (newReply != null) {
			fixUserName(server, newReply);
		}
		return newReply;
	}

	public VersionedComment addVersionedCommentReply(ServerData server, PermId id, PermId cId, VersionedComment comment)
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

	public void removeComment(ServerData server, PermId id, Comment comment) throws RemoteApiException,
			ServerPasswordNotProvidedException {
		CrucibleSession session = getSession(server);
		session.removeComment(id, comment);
	}

	public List<User> getUsers(ServerData server) throws RemoteApiException, ServerPasswordNotProvidedException {
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
	public List<CrucibleProject> getProjects(ServerData server) throws RemoteApiException,
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
	public List<Repository> getRepositories(ServerData server) throws RemoteApiException,
			ServerPasswordNotProvidedException {
		CrucibleSession session = getSession(server);
		return session.getRepositories();
	}

	public SvnRepository getRepository(ServerData server, String repoName) throws RemoteApiException,
			ServerPasswordNotProvidedException {
		CrucibleSession session = getSession(server);
		return session.getRepository(repoName);
	}

	public List<CustomFieldDef> getMetrics(ServerData server, int version) throws RemoteApiException,
			ServerPasswordNotProvidedException {
		CrucibleSession session = getSession(server);
		return session.getMetrics(version);
	}

	/**
	 * @param server server object with Url, Login and Password to connect to
	 * @return List of reviews (empty list in case there is no review)
	 */
	public List<Review> getAllReviews(ServerData server) throws RemoteApiException, ServerPasswordNotProvidedException {
		CrucibleSession session = getSession(server);
		return session.getAllReviews();
	}

	public List<Review> getReviewsForFilter(ServerData server, PredefinedFilter filter) throws RemoteApiException,
			ServerPasswordNotProvidedException {
		CrucibleSession session = getSession(server);
		return session.getReviewsForFilter(filter);
	}

	public List<Review> getReviewsForCustomFilter(ServerData server, CustomFilter filter) throws RemoteApiException,
			ServerPasswordNotProvidedException {
		CrucibleSession session = getSession(server);
		return session.getReviewsForCustomFilter(filter);
	}

	public Review getReview(ServerData server, PermId permId) throws RemoteApiException,
			ServerPasswordNotProvidedException {
		CrucibleSession session = getSession(server);
		return session.getReview(permId);
	}

	public List<Reviewer> getReviewers(ServerData server, PermId permId) throws RemoteApiException,
			ServerPasswordNotProvidedException {
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
