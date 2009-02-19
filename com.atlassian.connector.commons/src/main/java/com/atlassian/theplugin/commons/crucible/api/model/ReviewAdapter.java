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

package com.atlassian.theplugin.commons.crucible.api.model;

import com.atlassian.theplugin.commons.cfg.CrucibleServerCfg;
import com.atlassian.theplugin.commons.crucible.CrucibleReviewListener;
import com.atlassian.theplugin.commons.crucible.CrucibleServerFacade;
import com.atlassian.theplugin.commons.crucible.CrucibleServerFacadeImpl;
import com.atlassian.theplugin.commons.crucible.ValueNotYetInitialized;
import com.atlassian.theplugin.commons.crucible.api.model.notification.CrucibleNotification;
import com.atlassian.theplugin.commons.crucible.api.model.notification.ReviewDifferenceProducer;
import com.atlassian.theplugin.commons.exception.ServerPasswordNotProvidedException;
import com.atlassian.theplugin.commons.remoteapi.RemoteApiException;

import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.EnumSet;

import org.jetbrains.annotations.NotNull;

public class ReviewAdapter {
	private Review review;

	private CrucibleServerCfg server;

	private static final int HASHCODE_MAGIC = 31;

	private CrucibleServerFacade facade;

	private Collection<CrucibleReviewListener> listeners = new HashSet<CrucibleReviewListener>();

	private Collection<CrucibleReviewListener> getListeners() {
		return listeners;
	}

	public ReviewAdapter(Review review, CrucibleServerCfg server) {
		this.review = review;
		this.server = server;

		facade = CrucibleServerFacadeImpl.getInstance();
	}

	public ReviewAdapter(ReviewAdapter reviewAdapter) {
		this(reviewAdapter.review, reviewAdapter.server);
	}

	public boolean isCompleted() {
		return review.isCompleted();
	}

	public void setFacade(CrucibleServerFacade newFacade) {
		facade = newFacade;
	}

	public User getAuthor() {
		return review.getAuthor();
	}

	public User getCreator() {
		return review.getCreator();
	}

	public String getDescription() {
		return review.getDescription();
	}

	public User getModerator() {
		return review.getModerator();
	}

	public String getName() {
		return review.getName();
	}

	public PermId getParentReview() {
		return review.getParentReview();
	}

	public PermId getPermId() {
		return review.getPermId();
	}

	public String getProjectKey() {
		return review.getProjectKey();
	}

	public CrucibleProject getCrucibleProject() {
		return review.getCrucibleProject();
	}

	public String getRepoName() {
		return review.getRepoName();
	}

	public State getState() {
		return review.getState();
	}

	public boolean isAllowReviewerToJoin() {
		return review.isAllowReviewerToJoin();
	}

	public int getMetricsVersion() {
		return review.getMetricsVersion();
	}

	public Date getCreateDate() {
		return review.getCreateDate();
	}

	public Date getCloseDate() {
		return review.getCloseDate();
	}

	public String getSummary() {
		return review.getSummary();
	}

	public Set<Reviewer> getReviewers() throws ValueNotYetInitialized {
		return review.getReviewers();
	}

	public List<GeneralComment> getGeneralComments() throws ValueNotYetInitialized {
		return review.getGeneralComments();
	}

	public EnumSet<CrucibleAction> getTransitions() throws ValueNotYetInitialized {
		return review.getTransitions();
	}

	public EnumSet<CrucibleAction> getActions() throws ValueNotYetInitialized {
		return review.getActions();
	}

	public CrucibleFileInfo getFileByPermId(PermId id) throws ValueNotYetInitialized {
		return review.getFileByPermId(id);
	}

	public CrucibleServerCfg getServer() {
		return server;
	}

	@NotNull
	public String getReviewUrl() {
		String baseUrl = server.getUrl();
		while (baseUrl.length() > 0 && baseUrl.charAt(baseUrl.length() - 1) == '/') {
			// quite ineffective, I know ...
			baseUrl = baseUrl.substring(0, baseUrl.length() - 1);
		}
		return baseUrl + "/cru/" + getPermId().getId();
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (o == null || getClass() != o.getClass()) {
			return false;
		}

		ReviewAdapter that = (ReviewAdapter) o;

		return !(review != null ? !review.equals(that.review) : that.review != null)
				&& !(server != null ? !server.equals(that.server) : that.server != null);
	}

	@Override
	public int hashCode() {
		int result;
		result = (review != null ? review.hashCode() : 0);
		result = HASHCODE_MAGIC * result + (server != null ? server.hashCode() : 0);
		return result;
	}

	public void addReviewListener(CrucibleReviewListener listener) {
		if (!getListeners().contains(listener)) {
			listeners.add(listener);
		}
	}

	public boolean removeReviewListener(CrucibleReviewListener listener) {
		return listeners.remove(listener);
	}

	public void setGeneralComments(final List<GeneralComment> generalComments) {
		review.setGeneralComments(generalComments);
	}

	public void addGeneralComment(final GeneralCommentBean comment)
			throws ValueNotYetInitialized, RemoteApiException, ServerPasswordNotProvidedException {

		GeneralComment newComment = facade.addGeneralComment(getServer(), review.getPermId(), comment);

		if (newComment != null) {
			review.getGeneralComments().add(newComment);

			// notify listeners
			for (CrucibleReviewListener listener : getListeners()) {
				listener.createdOrEditedGeneralComment(this, newComment);
			}
		}
	}

	public void addGeneralCommentReply(final GeneralComment parentComment, final GeneralCommentBean replyComment)
			throws RemoteApiException, ServerPasswordNotProvidedException, ValueNotYetInitialized {

		GeneralComment newReply = facade.addGeneralCommentReply(
				getServer(), getPermId(), parentComment.getPermId(), replyComment);

		if (newReply != null) {
			for (GeneralComment comment : review.getGeneralComments()) {
				if (comment.equals(parentComment)) {
					comment.getReplies().add(newReply);
					break;
				}
			}

			// notify listeners
			for (CrucibleReviewListener listener : getListeners()) {
				listener.createdOrEditedGeneralCommentReply(this, parentComment, newReply);
			}
		}
	}

	/**
	 * Removes general review comment from the server and model.
	 * It SHOULD NOT be called from the EVENT DISPATCH THREAD as it calls facade method.
	 *
	 * @param generalComment Comment to be removed
	 * @throws com.atlassian.theplugin.commons.exception.ServerPasswordNotProvidedException
	 *          in case password is missing
	 * @throws com.atlassian.theplugin.commons.remoteapi.RemoteApiException
	 *          in case of communication problem
	 */
	public synchronized void removeGeneralComment(final GeneralComment generalComment)
			throws RemoteApiException, ServerPasswordNotProvidedException {

		// remove comment from the server
		facade.removeComment(getServer(), review.getPermId(), generalComment);

		// remove comment from the model
		this.review.removeGeneralComment(generalComment);

		// notify listeners
		for (CrucibleReviewListener listener : getListeners()) {
			listener.removedComment(this, generalComment);
		}
	}


	public void addVersionedComment(final CrucibleFileInfo file, final VersionedCommentBean newComment)
			throws RemoteApiException, ServerPasswordNotProvidedException {

		VersionedComment newVersionedComment = facade.addVersionedComment(getServer(), getPermId(),
				file.getPermId(), newComment);
		if (newVersionedComment != null) {
			List<VersionedComment> comments;
			comments = file.getVersionedComments();

			if (comments == null) {
				comments = facade.getVersionedComments(getServer(), getPermId(), file.getPermId());
				file.setVersionedComments(comments);
			} else {
				comments.add(newVersionedComment);
			}

			// notify listeners
			for (CrucibleReviewListener listener : getListeners()) {
				listener.createdOrEditedVersionedComment(this, file.getPermId(), newVersionedComment);
			}
		}
	}

	public void addVersionedCommentReply(final CrucibleFileInfo file, final VersionedComment parentComment,
			final VersionedCommentBean nComment)
			throws RemoteApiException, ServerPasswordNotProvidedException {

		VersionedComment newComment = facade.addVersionedCommentReply(
				getServer(), getPermId(), parentComment.getPermId(), nComment);

		if (newComment != null) {
			parentComment.getReplies().add(newComment);

			// notify listeners
			for (CrucibleReviewListener listener : getListeners()) {
				listener.createdOrEditedVersionedCommentReply(this, file.getPermId(), parentComment, newComment);
			}
		}
	}

	/**
	 * Removes file comment from the server and model.
	 * It SHOULD NOT be called from the EVENT DISPATCH THREAD as it calls facade method.
	 *
	 * @param versionedComment Comment to be removed
	 * @param file			 file containing the comment
	 * @throws com.atlassian.theplugin.commons.exception.ServerPasswordNotProvidedException
	 *          in case password is missing
	 * @throws com.atlassian.theplugin.commons.remoteapi.RemoteApiException
	 *          in case of communication problem
	 * @throws com.atlassian.theplugin.commons.crucible.ValueNotYetInitialized
	 *
	 */
	public void removeVersionedComment(final VersionedComment versionedComment, final CrucibleFileInfo file)
			throws RemoteApiException, ServerPasswordNotProvidedException, ValueNotYetInitialized {

		// remove comment from the server
		facade.removeComment(getServer(), review.getPermId(), versionedComment);

		// remove comment from the model
		review.removeVersionedComment(versionedComment, file);

		// notify listeners
		for (CrucibleReviewListener listener : getListeners()) {
			listener.removedComment(this, versionedComment);
		}
	}

	public void editGeneralComment(final GeneralComment comment) throws RemoteApiException, ServerPasswordNotProvidedException {

		facade.updateComment(getServer(), getPermId(), comment);
		// notify listeners
		for (CrucibleReviewListener listener : getListeners()) {
			listener.createdOrEditedGeneralComment(this, comment);
		}
	}

	public void editVersionedComment(final CrucibleFileInfo file, final VersionedComment comment)
			throws RemoteApiException, ServerPasswordNotProvidedException {
		facade.updateComment(getServer(), getPermId(), comment);

		// notify listeners
		for (CrucibleReviewListener listener : getListeners()) {
			listener.createdOrEditedVersionedComment(this, file.getPermId(), comment);
		}
	}

	public void publishGeneralComment(final GeneralComment comment)
			throws RemoteApiException, ServerPasswordNotProvidedException {
		facade.publishComment(getServer(), getPermId(), comment.getPermId());

		((GeneralCommentBean) comment).setDraft(false);

//dirty hack - probably remote api should return new comment info
//				if (comment instanceof VersionedCommentBean) {
//					((VersionedCommentBean) comment).setDraft(false);
//				}

		// notify listeners
		for (CrucibleReviewListener listener : getListeners()) {
			listener.publishedGeneralComment(this, comment);
		}
	}

	public void publisVersionedComment(final CrucibleFileInfo file, final VersionedComment comment)
			throws RemoteApiException, ServerPasswordNotProvidedException {
		facade.publishComment(getServer(), getPermId(), comment.getPermId());
		//dirty hack - probably remote api should return new comment info
		//if (comment instanceof VersionedCommentBean) {
		((VersionedCommentBean) comment).setDraft(false);
		//}
		// notify listeners
		for (CrucibleReviewListener listener : getListeners()) {
			listener.publishedVersionedComment(this, file.getPermId(), comment);
		}
	}

	public void setFilesAndVersionedComments(final Set<CrucibleFileInfo> files, final List<VersionedComment> comments) {
		review.setFilesAndVersionedComments(files, comments);
	}

	/*
	 public List<CrucibleNotification> fillReview(final ReviewAdapter newReview) {
		 return fillReview(newReview.review);
	 }
 */
	/**
	 * Copies all data from the parameter into itself
	 *
	 * @param newReview source of Review data
	 * @return
	 */
	public synchronized List<CrucibleNotification> fillReview(final ReviewAdapter newReview) {
		boolean reviewChanged = false;

		ReviewDifferenceProducer reviewDifferenceProducer = new ReviewDifferenceProducer(this, newReview);
		List<CrucibleNotification> differences = reviewDifferenceProducer.getDiff();
		review.setAuthor(newReview.getAuthor());
		review.setModerator(newReview.getModerator());
		review.setName(newReview.getName());
		review.setProjectKey(newReview.getProjectKey());
		review.setDescription(newReview.getDescription());
		review.setSummary(newReview.getSummary());
		try {
			review.setReviewers(newReview.getReviewers());
		} catch (ValueNotYetInitialized valueNotYetInitialized) {
			// shame
		}
		if (!reviewDifferenceProducer.isShortEqual()) {
			try {
				setGeneralComments(newReview.getGeneralComments());
			} catch (ValueNotYetInitialized valueNotYetInitialized) {
				// shame
			}

			try {
				review.setActions(newReview.getActions());
			} catch (ValueNotYetInitialized valueNotYetInitialized) {
				// shame
			}
			review.setAllowReviewerToJoin(newReview.isAllowReviewerToJoin());
			review.setCloseDate(newReview.getCloseDate());
			review.setCreateDate(newReview.getCreateDate());
			review.setCreator(newReview.getCreator());
			review.setMetricsVersion(newReview.getMetricsVersion());
			review.setParentReview(newReview.getParentReview());
			review.setRepoName(newReview.getRepoName());
			review.setState(newReview.getState());
			try {
				review.setTransitions(newReview.getTransitions());
			} catch (ValueNotYetInitialized valueNotYetInitialized) {
				// shame
			}
			reviewChanged = true;
		}


		if (!reviewDifferenceProducer.isFilesEqual()) {
			try {
				setFiles(newReview.getFiles());
			} catch (ValueNotYetInitialized valueNotYetInitialized) {
				// shame
			}
			if (reviewDifferenceProducer.getCommentChangesCount() > 0) {
				reviewChanged = true;
			}
		}

		if (reviewChanged) {
			for (CrucibleReviewListener listener : getListeners()) {
				listener.reviewChanged(this, differences);
			}

		}

		return differences;
	}

	private void setFiles(final Set<CrucibleFileInfo> files) {
		review.setFiles(files);
	}

	public Set<CrucibleFileInfo> getFiles() throws ValueNotYetInitialized {
		if (review.getFiles() == null) {
			throw new ValueNotYetInitialized("Files collection is empty");
		}
		return review.getFiles();
	}

	/**
	 * @return total number of versioned comments including replies (for all files)
	 * @throws com.atlassian.theplugin.commons.crucible.ValueNotYetInitialized
	 *
	 */
	public int getNumberOfVersionedComments() throws ValueNotYetInitialized {
		return review.getNumberOfVersionedComments();
	}

	public int getNumberOfVersionedComments(final String userName) throws ValueNotYetInitialized {
		return review.getNumberOfVersionedComments(userName);
	}

	public int getNumberOfVersionedCommentsDefects() throws ValueNotYetInitialized {
		return review.getNumberOfVersionedCommentsDefects();
	}

	public int getNumberOfVersionedCommentsDefects(final String userName) throws ValueNotYetInitialized {
		return review.getNumberOfVersionedCommentsDefects(userName);
	}

	public int getNumberOfVersionedCommentsDrafts() throws ValueNotYetInitialized {
		return review.getNumberOfVersionedCommentsDrafts();
	}

	public int getNumberOfGeneralCommentsDrafts(final String userName) throws ValueNotYetInitialized {
		return review.getNumberOfGeneralCommentsDrafts(userName);
	}

	public int getNumberOfGeneralComments() throws ValueNotYetInitialized {
		return review.getNumberOfGeneralComments();
	}

	public int getNumberOfGeneralComments(final String userName) throws ValueNotYetInitialized {
		return review.getNumberOfGeneralComments(userName);
	}

	public int getNumberOfGeneralCommentsDefects() throws ValueNotYetInitialized {
		return review.getNumberOfGeneralCommentsDefects();
	}

	public int getNumberOfGeneralCommentsDefects(final String userName) throws ValueNotYetInitialized {
		return review.getNumberOfGeneralCommentsDefects(userName);
	}

	public int getNumberOfGeneralCommentsDrafts() throws ValueNotYetInitialized {
		return review.getNumberOfGeneralCommentsDrafts();
	}

	public int getNumberOfVersionedCommentsDrafts(final String userName) throws ValueNotYetInitialized {
		return review.getNumberOfVersionedCommentsDrafts(userName);
	}

	@Override
	public String toString() {
		return review.getPermId().getId() + ": " + review.getName() + " (" + server.getName() + ')';
	}
}
