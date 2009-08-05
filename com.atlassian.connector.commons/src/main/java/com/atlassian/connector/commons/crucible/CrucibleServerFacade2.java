/*******************************************************************************
 * Copyright (c) 2008 Atlassian and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Atlassian - initial API and implementation
 ******************************************************************************/

package com.atlassian.connector.commons.crucible;

import com.atlassian.connector.commons.api.ConnectionCfg;
import com.atlassian.theplugin.commons.crucible.api.UploadItem;
import com.atlassian.theplugin.commons.crucible.api.model.Comment;
import com.atlassian.theplugin.commons.crucible.api.model.CrucibleFileInfo;
import com.atlassian.theplugin.commons.crucible.api.model.CrucibleProject;
import com.atlassian.theplugin.commons.crucible.api.model.CustomFieldDef;
import com.atlassian.theplugin.commons.crucible.api.model.CustomFilter;
import com.atlassian.theplugin.commons.crucible.api.model.GeneralComment;
import com.atlassian.theplugin.commons.crucible.api.model.PermId;
import com.atlassian.theplugin.commons.crucible.api.model.PredefinedFilter;
import com.atlassian.theplugin.commons.crucible.api.model.Repository;
import com.atlassian.theplugin.commons.crucible.api.model.Review;
import com.atlassian.theplugin.commons.crucible.api.model.Reviewer;
import com.atlassian.theplugin.commons.crucible.api.model.SvnRepository;
import com.atlassian.theplugin.commons.crucible.api.model.User;
import com.atlassian.theplugin.commons.crucible.api.model.VersionedComment;
import com.atlassian.theplugin.commons.exception.ServerPasswordNotProvidedException;
import com.atlassian.theplugin.commons.remoteapi.ProductServerFacade;
import com.atlassian.theplugin.commons.remoteapi.RemoteApiException;
import com.atlassian.theplugin.commons.remoteapi.rest.HttpSessionCallback;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import java.util.Collection;
import java.util.List;
import java.util.Set;

public interface CrucibleServerFacade2 extends ProductServerFacade {
	// CrucibleVersionInfo getServerVersion(CrucibleServerCfg server)
	// throws RemoteApiException, ServerPasswordNotProvidedException;

	Review createReview(ConnectionCfg server, Review review) throws RemoteApiException, ServerPasswordNotProvidedException;

	Review createReviewFromRevision(ConnectionCfg server, Review review, List<String> revisions) throws RemoteApiException,
		ServerPasswordNotProvidedException;

	Review addRevisionsToReview(ConnectionCfg server, PermId permId, String repository, List<String> revisions)
		throws RemoteApiException, ServerPasswordNotProvidedException;

	Review addPatchToReview(ConnectionCfg server, PermId permId, String repository, String patch) throws RemoteApiException,
		ServerPasswordNotProvidedException;

	// CrucibleFileInfo addItemToReview(CrucibleServerCfg server, Review review, NewReviewItem newItem)
	// throws RemoteApiException, ServerPasswordNotProvidedException;

	List<Reviewer> getReviewers(ConnectionCfg server, PermId permId) throws RemoteApiException,
		ServerPasswordNotProvidedException;

	void addReviewers(ConnectionCfg server, PermId permId, Set<String> userName) throws RemoteApiException,
		ServerPasswordNotProvidedException;

	void removeReviewer(ConnectionCfg server, PermId permId, String userName) throws RemoteApiException,
		ServerPasswordNotProvidedException;

	/**
	 * Convenience method for setting reviewers for a review. Please keep in mind that it involves at least 3 remote calls to
	 * Crucible server: getReview(), addReviewers() and N times removeReviewer(). This method is not atomic, so it may fail and
	 * leave reviewers in partially updated state After this method is complete, reviewers for selected review will be equal to
	 * this as given by <code>usernames</code>. Reviewers which are in <code>usernames</code> and are also present in the review
	 * itself are left intact - i.e. the method does gurantee to leave them intact even if some problems occur during execution.
	 *
	 * @param server
	 *            Crucible server to connect to
	 * @param permId
	 *            id of review
	 * @param usernames
	 *            usernames of reviewers
	 * @throws RemoteApiException
	 *             in case of some connection problems or malformed responses
	 * @throws ServerPasswordNotProvidedException
	 *             when password was not provided
	 */
	void setReviewers(@NotNull ConnectionCfg server, @NotNull PermId permId, @NotNull Collection<String> usernames)
		throws RemoteApiException, ServerPasswordNotProvidedException;

	Review approveReview(ConnectionCfg server, PermId permId) throws RemoteApiException, ServerPasswordNotProvidedException;

	Review submitReview(ConnectionCfg server, PermId permId) throws RemoteApiException, ServerPasswordNotProvidedException;

	Review summarizeReview(ConnectionCfg server, PermId permId) throws RemoteApiException, ServerPasswordNotProvidedException;

	Review abandonReview(ConnectionCfg server, PermId permId) throws RemoteApiException, ServerPasswordNotProvidedException;

	Review closeReview(ConnectionCfg server, PermId permId, String summary) throws RemoteApiException,
		ServerPasswordNotProvidedException;

	Review recoverReview(ConnectionCfg server, PermId permId) throws RemoteApiException, ServerPasswordNotProvidedException;

	Review reopenReview(ConnectionCfg server, PermId permId) throws RemoteApiException, ServerPasswordNotProvidedException;

	// Review rejectReview(CrucibleServerCfg server, PermId permId)
	// throws RemoteApiException, ServerPasswordNotProvidedException;

	void completeReview(ConnectionCfg server, PermId permId, boolean complete) throws RemoteApiException,
		ServerPasswordNotProvidedException;

	List<Review> getAllReviews(ConnectionCfg server) throws RemoteApiException, ServerPasswordNotProvidedException;

	List<Review> getReviewsForFilter(ConnectionCfg server, PredefinedFilter filter) throws RemoteApiException,
		ServerPasswordNotProvidedException;

	List<Review> getReviewsForCustomFilter(ConnectionCfg server, CustomFilter filter) throws RemoteApiException,
		ServerPasswordNotProvidedException;

	Review getReview(ConnectionCfg server, PermId permId) throws RemoteApiException, ServerPasswordNotProvidedException;

	// List<Review> getAllReviewsForFile(CrucibleServerCfg server, String repoName, String path)
	// throws RemoteApiException, ServerPasswordNotProvidedException;

	Review createReviewFromPatch(ConnectionCfg server, Review review, String patch) throws RemoteApiException,
		ServerPasswordNotProvidedException;

	Set<CrucibleFileInfo> getFiles(ConnectionCfg server, PermId permId) throws RemoteApiException,
		ServerPasswordNotProvidedException;

	// List<Comment> getComments(CrucibleServerCfg server, PermId permId)
	// throws RemoteApiException, ServerPasswordNotProvidedException;

	List<GeneralComment> getGeneralComments(ConnectionCfg server, PermId permId) throws RemoteApiException,
		ServerPasswordNotProvidedException;

	List<VersionedComment> getVersionedComments(ConnectionCfg server, PermId permId) throws RemoteApiException,
		ServerPasswordNotProvidedException;

	List<VersionedComment> getVersionedComments(ConnectionCfg server, PermId permId, PermId reviewItemId)
		throws RemoteApiException, ServerPasswordNotProvidedException;

	// List<GeneralComment> getReplies(CrucibleServerCfg server, PermId permId, PermId commentId)
	// throws RemoteApiException, ServerPasswordNotProvidedException;

	GeneralComment addGeneralComment(ConnectionCfg server, PermId permId, GeneralComment comment) throws RemoteApiException,
		ServerPasswordNotProvidedException;

	VersionedComment addVersionedComment(ConnectionCfg server, PermId permId, PermId riId, VersionedComment comment)
		throws RemoteApiException, ServerPasswordNotProvidedException;

	void updateComment(ConnectionCfg server, PermId id, Comment comment) throws RemoteApiException,
		ServerPasswordNotProvidedException;

	void publishComment(ConnectionCfg server, PermId reviewId, PermId commentId) throws RemoteApiException,
		ServerPasswordNotProvidedException;

	void publishAllCommentsForReview(ConnectionCfg server, PermId reviewId) throws RemoteApiException,
		ServerPasswordNotProvidedException;

	GeneralComment addGeneralCommentReply(ConnectionCfg server, PermId id, PermId cId, GeneralComment comment)
		throws RemoteApiException, ServerPasswordNotProvidedException;

	VersionedComment addVersionedCommentReply(ConnectionCfg server, PermId id, PermId cId, VersionedComment comment)
		throws RemoteApiException, ServerPasswordNotProvidedException;

	// void updateReply(CrucibleServerCfg server, PermId id, PermId cId, PermId rId, GeneralComment comment)
	// throws RemoteApiException, ServerPasswordNotProvidedException;

	void removeComment(ConnectionCfg server, PermId id, Comment comment) throws RemoteApiException,
		ServerPasswordNotProvidedException;

	List<User> getUsers(ConnectionCfg server) throws RemoteApiException, ServerPasswordNotProvidedException;

	List<CrucibleProject> getProjects(ConnectionCfg server) throws RemoteApiException, ServerPasswordNotProvidedException;

	List<Repository> getRepositories(ConnectionCfg server) throws RemoteApiException, ServerPasswordNotProvidedException;

	SvnRepository getRepository(ConnectionCfg server, String repoName) throws RemoteApiException,
		ServerPasswordNotProvidedException;

	List<CustomFieldDef> getMetrics(ConnectionCfg server, int version) throws RemoteApiException,
		ServerPasswordNotProvidedException;

	void setCallback(HttpSessionCallback callback);

	@Nullable
	String getDisplayName(@NotNull final ConnectionCfg server, @NotNull String username);

	@Nullable
	CrucibleProject getProject(@NotNull final ConnectionCfg server, @NotNull final String projectKey)
		throws RemoteApiException, ServerPasswordNotProvidedException;

	boolean checkContentUrlAvailable(ConnectionCfg server) throws RemoteApiException, ServerPasswordNotProvidedException;

	Review createReviewFromUpload(ConnectionCfg server, Review review, Collection<UploadItem> uploadItems)
		throws RemoteApiException, ServerPasswordNotProvidedException;

	byte[] getFileContent(@NotNull ConnectionCfg server, final String contentUrl) throws RemoteApiException,
		ServerPasswordNotProvidedException;

	Review addItemsToReview(ConnectionCfg server, PermId permId, Collection<UploadItem> items) throws RemoteApiException,
		ServerPasswordNotProvidedException;

	/**
	 * Adds info about files, versioned comments and general comments to the review
	 * 
	 * @param reviewItem
	 *            review to fill with details
	 * @throws RemoteApiException
	 * @throws ServerPasswordNotProvidedException
	 * 
	 */
	void fillDetailsForReview(@NotNull ConnectionCfg server, @NotNull Review review) throws RemoteApiException,
			ServerPasswordNotProvidedException;

	// void getDetailsForReview(final ReviewAdapter reviewItem) throws RemoteApiException, ServerPasswordNotProvidedException;
}
