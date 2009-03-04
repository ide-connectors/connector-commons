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

package com.atlassian.theplugin.commons.crucible.api;

import com.atlassian.theplugin.commons.crucible.api.model.Comment;
import com.atlassian.theplugin.commons.crucible.api.model.CrucibleAction;
import com.atlassian.theplugin.commons.crucible.api.model.CrucibleFileInfo;
import com.atlassian.theplugin.commons.crucible.api.model.CrucibleProject;
import com.atlassian.theplugin.commons.crucible.api.model.CrucibleVersionInfo;
import com.atlassian.theplugin.commons.crucible.api.model.CustomFieldDef;
import com.atlassian.theplugin.commons.crucible.api.model.CustomFilter;
import com.atlassian.theplugin.commons.crucible.api.model.GeneralComment;
import com.atlassian.theplugin.commons.crucible.api.model.PermId;
import com.atlassian.theplugin.commons.crucible.api.model.PredefinedFilter;
import com.atlassian.theplugin.commons.crucible.api.model.Repository;
import com.atlassian.theplugin.commons.crucible.api.model.Review;
import com.atlassian.theplugin.commons.crucible.api.model.ReviewItemContentType;
import com.atlassian.theplugin.commons.crucible.api.model.Reviewer;
import com.atlassian.theplugin.commons.crucible.api.model.State;
import com.atlassian.theplugin.commons.crucible.api.model.SvnRepository;
import com.atlassian.theplugin.commons.crucible.api.model.User;
import com.atlassian.theplugin.commons.crucible.api.model.VersionedComment;
import com.atlassian.theplugin.commons.remoteapi.RemoteApiException;
import com.atlassian.theplugin.commons.remoteapi.RemoteApiLoginException;

import java.util.List;
import java.util.Set;


public interface CrucibleSession {
	void login() throws RemoteApiLoginException;

	void logout();

	CrucibleVersionInfo getServerVersion() throws RemoteApiException;

	Review createReview(Review review) throws RemoteApiException;

	Review createReviewFromPatch(Review review, String patch) throws RemoteApiException;

	Review createReviewFromRevision(Review review, List<String> revisions) throws RemoteApiException;

	List<CrucibleAction> getAvailableActions(PermId permId) throws RemoteApiException;

	List<CrucibleAction> getAvailableTransitions(PermId permId) throws RemoteApiException;

	Review addRevisionsToReview(PermId permId, String repository, List<String> revisions) throws RemoteApiException;

	Review addPatchToReview(PermId permId, String repository, String patch) throws RemoteApiException;

	void addReviewers(PermId permId, Set<String> userNames) throws RemoteApiException;

	void removeReviewer(PermId permId, String userNames) throws RemoteApiException;

	Review approveReview(PermId permId) throws RemoteApiException;

	Review submitReview(PermId permId) throws RemoteApiException;

	Review abandonReview(PermId permId) throws RemoteApiException;

	Review closeReview(PermId permId, String summary) throws RemoteApiException;

	Review recoverReview(PermId permId) throws RemoteApiException;

	Review reopenReview(PermId permId) throws RemoteApiException;

	Review rejectReview(PermId permId) throws RemoteApiException;

	Review summarizeReview(PermId permId) throws RemoteApiException;

	void completeReview(PermId permId, boolean complete) throws RemoteApiException;

	List<Review> getReviewsInStates(List<State> arg1, boolean details) throws RemoteApiException;

	List<Review> getAllReviews(boolean details) throws RemoteApiException;

	List<Review> getReviewsForFilter(PredefinedFilter filter, boolean details) throws RemoteApiException;

	List<Review> getReviewsForCustomFilter(CustomFilter filter, boolean details) throws RemoteApiException;

	List<Review> getAllReviewsForFile(String repoName, String path, boolean details) throws RemoteApiException;

	Review getReview(PermId permId, boolean details) throws RemoteApiException;

	List<Reviewer> getReviewers(PermId arg1) throws RemoteApiException;

	List<User> getUsers() throws RemoteApiException;

	List<CrucibleProject> getProjects() throws RemoteApiException;

	List<Repository> getRepositories() throws RemoteApiException;

	SvnRepository getRepository(String repoName) throws RemoteApiException;

	Set<CrucibleFileInfo> getFiles(PermId id) throws RemoteApiException;

//	List<Comment> getComments(PermId id) throws RemoteApiException;

	List<GeneralComment> getGeneralComments(PermId id) throws RemoteApiException;

	List<VersionedComment> getAllVersionedComments(PermId id) throws RemoteApiException;

	List<VersionedComment> getVersionedComments(PermId id, PermId reviewItemId) throws RemoteApiException;

	List<GeneralComment> getReplies(PermId id, PermId commentId) throws RemoteApiException;

	GeneralComment addGeneralComment(PermId id, GeneralComment comment) throws RemoteApiException;

	VersionedComment addVersionedComment(PermId id, PermId riId, VersionedComment comment) throws RemoteApiException;

	void removeComment(PermId id, Comment comment) throws RemoteApiException;

	void updateComment(PermId id, Comment comment) throws RemoteApiException;

	void publishComment(PermId reviewId, PermId commentId) throws RemoteApiException;

	GeneralComment addGeneralCommentReply(PermId id, PermId cId, GeneralComment comment) throws RemoteApiException;

	VersionedComment addVersionedCommentReply(PermId id, PermId cId, VersionedComment comment) throws RemoteApiException;

	void updateReply(PermId id, PermId cId, PermId rId, GeneralComment comment) throws RemoteApiException;

	List<CustomFieldDef> getMetrics(int version) throws RemoteApiException;

	boolean isLoggedIn();

//	CrucibleFileInfo addItemToReview(Review review, NewReviewItem item) throws RemoteApiException;

	List<CrucibleProject> getProjectsFromCache() throws RemoteApiException;

	Review createReviewFromUpload(Review review, UploadItem[] uploadItems) throws RemoteApiException;

	String getFileContent(CrucibleFileInfo file, ReviewItemContentType type) throws RemoteApiException;

	boolean checkContentUrlAvailable();
}
