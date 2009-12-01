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

import com.atlassian.connector.commons.api.ConnectionCfg;
import com.atlassian.connector.commons.api.HttpConnectionCfg;
import com.atlassian.connector.commons.crucible.CrucibleServerFacade2;
import com.atlassian.theplugin.commons.ServerType;
import com.atlassian.theplugin.commons.crucible.api.CrucibleLoginException;
import com.atlassian.theplugin.commons.crucible.api.CrucibleSession;
import com.atlassian.theplugin.commons.crucible.api.PathAndRevision;
import com.atlassian.theplugin.commons.crucible.api.UploadItem;
import com.atlassian.theplugin.commons.crucible.api.model.Comment;
import com.atlassian.theplugin.commons.crucible.api.model.CommentBean;
import com.atlassian.theplugin.commons.crucible.api.model.CrucibleFileInfo;
import com.atlassian.theplugin.commons.crucible.api.model.CrucibleProject;
import com.atlassian.theplugin.commons.crucible.api.model.CrucibleUserCache;
import com.atlassian.theplugin.commons.crucible.api.model.CrucibleVersionInfo;
import com.atlassian.theplugin.commons.crucible.api.model.CustomFieldDef;
import com.atlassian.theplugin.commons.crucible.api.model.CustomFilter;
import com.atlassian.theplugin.commons.crucible.api.model.GeneralComment;
import com.atlassian.theplugin.commons.crucible.api.model.GeneralCommentBean;
import com.atlassian.theplugin.commons.crucible.api.model.NewReviewItem;
import com.atlassian.theplugin.commons.crucible.api.model.PermId;
import com.atlassian.theplugin.commons.crucible.api.model.PredefinedFilter;
import com.atlassian.theplugin.commons.crucible.api.model.Repository;
import com.atlassian.theplugin.commons.crucible.api.model.Review;
import com.atlassian.theplugin.commons.crucible.api.model.Reviewer;
import com.atlassian.theplugin.commons.crucible.api.model.SvnRepository;
import com.atlassian.theplugin.commons.crucible.api.model.User;
import com.atlassian.theplugin.commons.crucible.api.model.VersionedComment;
import com.atlassian.theplugin.commons.crucible.api.model.VersionedCommentBean;
import com.atlassian.theplugin.commons.crucible.api.rest.CrucibleSessionImpl;
import com.atlassian.theplugin.commons.exception.ServerPasswordNotProvidedException;
import com.atlassian.theplugin.commons.remoteapi.RemoteApiException;
import com.atlassian.theplugin.commons.remoteapi.RemoteApiMalformedUrlException;
import com.atlassian.theplugin.commons.remoteapi.rest.HttpSessionCallback;
import com.atlassian.theplugin.commons.util.MiscUtil;
import com.atlassian.theplugin.commons.util.UrlUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class CrucibleServerFacadeImpl implements CrucibleServerFacade2 {
	private final Map<String, CrucibleSession> sessions = new HashMap<String, CrucibleSession>();

	private CrucibleUserCache userCache;

	private HttpSessionCallback callback;

	public CrucibleServerFacadeImpl(CrucibleUserCache userCache, @NotNull HttpSessionCallback callback) {
		this.userCache = userCache;
		this.callback = callback;
	}

	public void setUserCache(CrucibleUserCache newCache) {
		userCache = newCache;
	}

	public ServerType getServerType() {
		return ServerType.CRUCIBLE_SERVER;
	}

	public synchronized CrucibleSession getSession(ConnectionCfg server) throws RemoteApiException,
			ServerPasswordNotProvidedException {
		String key = server.getUrl() + server.getUsername() + server.getPassword();
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

	private void fixUserName(ConnectionCfg server, CommentBean comment) {
		User u = comment.getAuthor();
		if (u.getDisplayName() == null || u.getDisplayName().length() == 0) {
			User newU = userCache.getUser(this, server, u.getUsername(), true);
			if (newU != null) {
				comment.setAuthor(newU);
			}
		}
	}

	@Nullable
	public String getDisplayName(@NotNull final ConnectionCfg server, @NotNull String username) {
		final User user = userCache.getUser(this, server, username, true);
		return user != null ? user.getDisplayName() : null;
	}

	// this method (and the method above is broken wrt to its design
	// @todo eliminate user cache from here, do not swollow exception, etc.
	@Nullable
	public User getUser(@NotNull final ConnectionCfg server, String username) {
		return userCache.getUser(this, server, username, true);
	}


    /* @todo optimize to get single project instead loading all from crucible 2.0 only
    https://extranet.atlassian.com/crucible/rest-service/projects-v1/CR?expand=allowedReviewers */
	public CrucibleProject getProject(@NotNull final ConnectionCfg server, @NotNull final String projectKey)
			throws RemoteApiException, ServerPasswordNotProvidedException {
		final List<CrucibleProject> projects = getProjects(server);
		for (CrucibleProject project : projects) {
			if (project.getKey().equals(projectKey)) {
				return project;
			}
		}
		return null;
	}

	public boolean checkContentUrlAvailable(final ConnectionCfg server) throws RemoteApiException,
			ServerPasswordNotProvidedException {
		CrucibleSession session = getSession(server);
		return session.checkContentUrlAvailable();
	}

	public Review createReviewFromUpload(@NotNull final ConnectionCfg server, @NotNull final Review review,
			@NotNull final Collection<UploadItem> uploadItems) throws RemoteApiException,
			ServerPasswordNotProvidedException {
		CrucibleSession session = getSession(server);
		return session.createReviewFromUpload(review, uploadItems);
	}

	public byte[] getFileContent(@NotNull final ConnectionCfg server, @NotNull final String contentUrl)
			throws RemoteApiException, ServerPasswordNotProvidedException {
		CrucibleSession session = getSession(server);
		return session.getFileContent(contentUrl);
	}

	/**
	 * For testing Only
	 */
//	public void testServerConnection(String url, String userName, String password) throws RemoteApiException {
	// ConnectionCfg serverData = new ConnectionCfg("unknown", new ServerId(), userName, password, url);
//		testServerConnection(serverData);
//	}

	/**
	 * @param httpConnectionCfg The configuration for the server that we want to test the connection for
	 * @throws com.atlassian.theplugin.commons.crucible.api.CrucibleException
	 *                            if Crucible version is not supported
	 * @throws RemoteApiException if it's not possible to authenticate user on specified server
	 */
	public void testServerConnection(HttpConnectionCfg httpConnectionCfg) throws RemoteApiException {
	    testServerConnection(httpConnectionCfg.toConnectionCfg());
	}

    public void testServerConnection(ConnectionCfg connectionCfg) throws RemoteApiException {
       	final CrucibleSession session = new CrucibleSessionImpl(connectionCfg, callback);
		session.login();
		try {
			session.getServerVersion();
		} catch (RemoteApiException e) {
			// getServerVersion tries to login again due to https://studio.atlassian.com/browse/ACC-31
			// if it fails it will throw RemoteApiLoginException which doesn't have Cause
			if (e.getCause() != null && e.getCause().getMessage() != null
					&& e.getCause().getMessage().startsWith("HTTP 500")) {
				throw new CrucibleLoginException("Atlassian Connector for IntelliJ IDEA detected a Crucible version older\n"
						+ "than 1.6. Unfortunately, the plugin will not\n" + "work with this version of Crucible."
                        + "\nDetailed error message is\n" + e.getCause().getMessage() + "\n");
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
	 * @param server connection configuration
	 * @param review data for new review to create (some fields have to be set e.g. projectKey)
	 * @return created revew date
	 * @throws com.atlassian.theplugin.commons.crucible.api.CrucibleException
	 *          in case of createReview error or CrucibleLoginException in case of login error
	 */
	public Review createReview(ConnectionCfg server, Review review) throws RemoteApiException,
			ServerPasswordNotProvidedException {
		CrucibleSession session = getSession(server);
		return session.createReview(review);
	}

	public Review createReviewFromRevision(ConnectionCfg server, Review review, List<String> revisions)
			throws RemoteApiException, ServerPasswordNotProvidedException {
		CrucibleSession session = getSession(server);
		return session.createReviewFromRevision(review, revisions);
	}

	public Review addRevisionsToReview(ConnectionCfg server, PermId permId, String repository, List<String> revisions)
			throws RemoteApiException, ServerPasswordNotProvidedException {
		CrucibleSession session = getSession(server);
		Review review = null;
		if (!revisions.isEmpty()) {
			review = session.addRevisionsToReview(permId, repository, revisions);
		}
		return review;
	}

    public Review addFileRevisionsToReview(ConnectionCfg server, PermId permId, String repository,
                                           List<PathAndRevision> revisions)
            throws RemoteApiException, ServerPasswordNotProvidedException {

        CrucibleSession session = getSession(server);
        Review review = null;
        if (!revisions.isEmpty()) {
            review = session.addFileRevisionsToReview(permId, repository, revisions);
        }
        return review;
    }

	public void addFileToReview(ConnectionCfg server, PermId permId, NewReviewItem newReviewItem) throws RemoteApiException,
			ServerPasswordNotProvidedException {
		CrucibleSession session = getSession(server);
		session.addFileToReview(permId, newReviewItem);
	}

	public CrucibleVersionInfo getServerVersion(ConnectionCfg server) throws RemoteApiException,
			ServerPasswordNotProvidedException {
		CrucibleSession session = getSession(server);
		return session.getServerVersion();
	}

	public Review addPatchToReview(ConnectionCfg server, PermId permId, String repository, String patch)
			throws RemoteApiException, ServerPasswordNotProvidedException {
		return getSession(server).addPatchToReview(permId, repository, patch);
	}

	public Review addItemsToReview(ConnectionCfg server, PermId permId, Collection<UploadItem> items)
			throws RemoteApiException, ServerPasswordNotProvidedException {
		CrucibleSession session = getSession(server);
		session.addItemsToReview(permId, items);
		return session.getReview(permId);
	}

	public void addReviewers(ConnectionCfg server, PermId permId, Set<String> userNames) throws RemoteApiException,
			ServerPasswordNotProvidedException {
		CrucibleSession session = getSession(server);
		session.addReviewers(permId, userNames);
	}

	public void removeReviewer(ConnectionCfg server, PermId permId, String userName) throws RemoteApiException,
			ServerPasswordNotProvidedException {
		CrucibleSession session = getSession(server);
		session.removeReviewer(permId, userName);
	}

	private boolean contains(Set<Reviewer> reviewers, String username) {
		for (Reviewer reviewer : reviewers) {
			if (reviewer.getUsername().equals(username)) {
				return true;
			}
		}
		return false;
	}

	public void setReviewers(@NotNull final ConnectionCfg server, @NotNull final PermId permId,
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
				if (!usernames.contains(reviewer.getUsername())) {
					reviewersForRemove.add(reviewer.getUsername());
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

	public Review approveReview(ConnectionCfg server, PermId permId) throws RemoteApiException,
			ServerPasswordNotProvidedException {
		CrucibleSession session = getSession(server);
		return session.approveReview(permId);
	}

	public Review submitReview(ConnectionCfg server, PermId permId) throws RemoteApiException,
			ServerPasswordNotProvidedException {
		CrucibleSession session = getSession(server);
		return session.submitReview(permId);
	}

	public Review summarizeReview(ConnectionCfg server, PermId permId) throws RemoteApiException,
			ServerPasswordNotProvidedException {
		CrucibleSession session = getSession(server);
		return session.summarizeReview(permId);
	}

	public Review abandonReview(ConnectionCfg server, PermId permId) throws RemoteApiException,
			ServerPasswordNotProvidedException {
		CrucibleSession session = getSession(server);
		return session.abandonReview(permId);
	}

	public Review closeReview(ConnectionCfg server, PermId permId, String summary) throws RemoteApiException,
			ServerPasswordNotProvidedException {
		CrucibleSession session = getSession(server);
		return session.closeReview(permId, summary);
	}

	public Review recoverReview(ConnectionCfg server, PermId permId) throws RemoteApiException,
			ServerPasswordNotProvidedException {
		CrucibleSession session = getSession(server);
		return session.recoverReview(permId);
	}

	public Review reopenReview(ConnectionCfg server, PermId permId) throws RemoteApiException,
			ServerPasswordNotProvidedException {
		CrucibleSession session = getSession(server);
		return session.reopenReview(permId);
	}

//	public Review rejectReview(CrucibleServerCfg server, PermId permId)
//			throws RemoteApiException, ServerPasswordNotProvidedException {
//		CrucibleSession session = getSession(server);
//		return session.rejectReview(permId);
//	}

	public void completeReview(ConnectionCfg server, PermId permId, boolean complete) throws RemoteApiException,
			ServerPasswordNotProvidedException {
		CrucibleSession session = getSession(server);
		session.completeReview(permId, complete);
	}

	/**
	 * Creates new review in Crucible
	 *
	 * @param server connection configuration
	 * @param review data for new review to create (some fields have to be set e.g. projectKey)
	 * @param patch  patch to assign with the review
	 * @return created revew date
	 * @throws com.atlassian.theplugin.commons.crucible.api.CrucibleException
	 *          in case of createReview error or CrucibleLoginException in case of login error
	 */
	public Review createReviewFromPatch(ConnectionCfg server, Review review, String patch) throws RemoteApiException,
			ServerPasswordNotProvidedException {
		CrucibleSession session = getSession(server);
		return session.createReviewFromPatch(review, patch);
	}

	public Set<CrucibleFileInfo> getFiles(ConnectionCfg server, PermId permId) throws RemoteApiException,
			ServerPasswordNotProvidedException {
		CrucibleSession session = getSession(server);
		return session.getFiles(permId);
	}

	//	public List<Comment> getComments(CrucibleServerCfg server, PermId permId)
//			throws RemoteApiException, ServerPasswordNotProvidedException {
//		CrucibleSession session = getSession(server);
//		return session.getComments(permId);
//	}

	public void fillDetailsForReview(@NotNull ConnectionCfg server, @NotNull Review review) throws RemoteApiException,
			ServerPasswordNotProvidedException {
		review.setGeneralComments(getGeneralComments(server, review.getPermId()));
		List<VersionedComment> comments = getVersionedComments(server, review.getPermId());
		Set<CrucibleFileInfo> files;
		files = getFiles(server, review.getPermId());
		review.setFilesAndVersionedComments(files, comments);
	}

	public List<GeneralComment> getGeneralComments(ConnectionCfg server, PermId permId) throws RemoteApiException,
			ServerPasswordNotProvidedException {
		CrucibleSession session = getSession(server);
		return session.getGeneralComments(permId);
	}

	public List<VersionedComment> getVersionedComments(ConnectionCfg server, PermId permId) throws RemoteApiException,
			ServerPasswordNotProvidedException {
		CrucibleSession session = getSession(server);
		return session.getAllVersionedComments(permId);
	}

	public List<VersionedComment> getVersionedComments(ConnectionCfg server, PermId permId, PermId reviewItemId)
			throws RemoteApiException, ServerPasswordNotProvidedException {
		CrucibleSession session = getSession(server);
		return session.getVersionedComments(permId, reviewItemId);
	}

	public List<GeneralComment> getReplies(ConnectionCfg server, PermId permId, PermId commentId)
			throws RemoteApiException, ServerPasswordNotProvidedException {
		CrucibleSession session = getSession(server);
		return session.getReplies(permId, commentId);
	}

	public GeneralComment addGeneralComment(ConnectionCfg server, PermId permId, GeneralComment comment)
			throws RemoteApiException, ServerPasswordNotProvidedException {
		CrucibleSession session = getSession(server);
		GeneralCommentBean newComment = (GeneralCommentBean) session.addGeneralComment(permId, comment);
		if (newComment != null) {
			fixUserName(server, newComment);
		}
		return newComment;
	}

	public VersionedComment addVersionedComment(ConnectionCfg server, PermId permId, PermId riId, VersionedComment comment)
			throws RemoteApiException, ServerPasswordNotProvidedException {
		CrucibleSession session = getSession(server);
		VersionedCommentBean newComment = (VersionedCommentBean) session.addVersionedComment(permId, riId, comment);
		if (newComment != null) {
			fixUserName(server, newComment);
		}
		return newComment;
	}

	public void updateComment(ConnectionCfg server, PermId id, Comment comment) throws RemoteApiException,
			ServerPasswordNotProvidedException {
		CrucibleSession session = getSession(server);
		session.updateComment(id, comment);
	}

	public void publishComment(ConnectionCfg server, PermId reviewId, PermId commentId) throws RemoteApiException,
			ServerPasswordNotProvidedException {
		CrucibleSession session = getSession(server);
		session.publishComment(reviewId, commentId);
	}

	public void publishAllCommentsForReview(ConnectionCfg server, PermId reviewId) throws RemoteApiException,
			ServerPasswordNotProvidedException {
		CrucibleSession session = getSession(server);
		session.publishComment(reviewId, null);
	}

	public GeneralComment addGeneralCommentReply(ConnectionCfg server, PermId id, PermId cId, GeneralComment comment)
			throws RemoteApiException, ServerPasswordNotProvidedException {
		CrucibleSession session = getSession(server);
		GeneralCommentBean newReply = (GeneralCommentBean) session.addGeneralCommentReply(id, cId, comment);
		if (newReply != null) {
			fixUserName(server, newReply);
		}
		return newReply;
	}

	public VersionedComment addVersionedCommentReply(ConnectionCfg server, PermId id, PermId cId, VersionedComment comment)
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

	public void removeComment(ConnectionCfg server, PermId id, Comment comment) throws RemoteApiException,
			ServerPasswordNotProvidedException {
		CrucibleSession session = getSession(server);
		session.removeComment(id, comment);
	}

	public List<User> getUsers(ConnectionCfg server) throws RemoteApiException, ServerPasswordNotProvidedException {
		CrucibleSession session = getSession(server);
		return session.getUsers();
	}

	/**
	 * Retrieves list of projects defined on Crucible server
	 *
	 * @param server connection configuration
	 * @return
	 * @throws RemoteApiException
	 * @throws ServerPasswordNotProvidedException
	 *
	 */
	public List<CrucibleProject> getProjects(ConnectionCfg server) throws RemoteApiException,
			ServerPasswordNotProvidedException {
		CrucibleSession session = getSession(server);
		return session.getProjects();
	}

	/**
	 * Retrieves list of repositories defined on Crucible server
	 *
	 * @param server connection configuration
	 * @return
	 * @throws RemoteApiException
	 * @throws com.atlassian.theplugin.commons.exception.ServerPasswordNotProvidedException
	 *
	 */
	public List<Repository> getRepositories(ConnectionCfg server) throws RemoteApiException,
			ServerPasswordNotProvidedException {
		CrucibleSession session = getSession(server);
		return session.getRepositories();
	}

	public SvnRepository getRepository(ConnectionCfg server, String repoName) throws RemoteApiException,
			ServerPasswordNotProvidedException {
		CrucibleSession session = getSession(server);
		return session.getRepository(repoName);
	}

	public List<CustomFieldDef> getMetrics(ConnectionCfg server, int version) throws RemoteApiException,
			ServerPasswordNotProvidedException {
		CrucibleSession session = getSession(server);
		return session.getMetrics(version);
	}

	/**
	 * @param server server object with Url, Login and Password to connect to
	 * @return List of reviews (empty list in case there is no review)
	 */
	public List<Review> getAllReviews(ConnectionCfg server) throws RemoteApiException, ServerPasswordNotProvidedException {
		CrucibleSession session = getSession(server);
		return session.getAllReviews();
	}

	public List<Review> getReviewsForFilter(ConnectionCfg server, PredefinedFilter filter) throws RemoteApiException,
			ServerPasswordNotProvidedException {
		CrucibleSession session = getSession(server);
		return session.getReviewsForFilter(filter);
	}

	public List<Review> getReviewsForCustomFilter(ConnectionCfg server, CustomFilter filter) throws RemoteApiException,
			ServerPasswordNotProvidedException {
		CrucibleSession session = getSession(server);
		return session.getReviewsForCustomFilter(filter);
	}

	public Review getReview(ConnectionCfg server, PermId permId) throws RemoteApiException,
			ServerPasswordNotProvidedException {
		CrucibleSession session = getSession(server);
		return session.getReview(permId);
	}

	public List<Reviewer> getReviewers(ConnectionCfg server, PermId permId) throws RemoteApiException,
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

    public void markCommentRead(@NotNull ConnectionCfg server, PermId reviewId, PermId commentId)
            throws RemoteApiException, ServerPasswordNotProvidedException {
        CrucibleSession session = getSession(server);
        session.markCommentRead(reviewId, commentId);
    }

    public void markCommentLeaveUnread(@NotNull ConnectionCfg server, PermId reviewId, PermId commentId)
            throws RemoteApiException, ServerPasswordNotProvidedException {
        CrucibleSession session = getSession(server);
        session.markCommentLeaveRead(reviewId, commentId);
    }

    public void markAllCommentsRead(@NotNull ConnectionCfg server, PermId reviewId)
            throws RemoteApiException, ServerPasswordNotProvidedException {
        CrucibleSession session = getSession(server);
        session.markAllCommentsRead(reviewId);
    }
}
