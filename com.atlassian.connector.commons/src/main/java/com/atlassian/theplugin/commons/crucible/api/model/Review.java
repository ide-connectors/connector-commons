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

import com.atlassian.theplugin.commons.crucible.ValueNotYetInitialized;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

public class Review {
	private Set<Reviewer> reviewers;
	private List<GeneralComment> generalComments;
	private EnumSet<CrucibleAction> transitions;
	private EnumSet<CrucibleAction> actions;
	private User author;
	private User creator;
	private String description;
	private User moderator;
	private String name;
	private PermId parentReview;
	private PermId permId;
	private String projectKey;
	private String repoName;
	private State state;
	private boolean allowReviewerToJoin;
	private int metricsVersion;
	private Date createDate;
	private Date closeDate;
	private String summary;
	private final String serverUrl;
	private Set<CrucibleFileInfo> files;

	public void setReviewers(Set<Reviewer> reviewers) {
		this.reviewers = reviewers;
	}

	public void setGeneralComments(List<GeneralComment> generalComments) {
		this.generalComments = generalComments;
	}

	/**
	 * Removes comment from the model
	 *
	 * @param generalComment comment to be removed
	 */
	public void removeGeneralComment(final GeneralComment generalComment) {
		if (!generalComment.isReply()) {
			generalComments.remove(generalComment);
		} else {
			for (GeneralComment comment : generalComments) {
				if (comment.getReplies().remove(generalComment)) {
					return;
				}
			}
		}
	}

	public void removeVersionedComment(final VersionedComment comment, final CrucibleFileInfo file)
			throws ValueNotYetInitialized {

		CrucibleFileInfo f = getFileByPermId(file.getPermId());

		if (!comment.isReply()) {
			if (f != null) {
				f.getVersionedComments().remove(comment);
			}
		} else {
			if (f != null) {
				for (VersionedComment versionedComment : f.getVersionedComments()) {
					if (versionedComment.getReplies().remove(comment)) {
						return;
					}
				}
			}
		}
	}

	public void setTransitions(Collection<CrucibleAction> transitions) {
		// as EnumSet.copyOf does not work for empty collections we use such 2-phase approach
		this.transitions = EnumSet.noneOf(CrucibleAction.class);
		this.transitions.addAll(transitions);
	}

	public void setActions(Set<CrucibleAction> actions) {
		// as EnumSet.copyOf does not work for empty collections we use such 2-phase approach
		this.actions = EnumSet.noneOf(CrucibleAction.class);
		this.actions.addAll(actions);
	}

	public void setAuthor(@NotNull final User author) {
		this.author = author;
	}

	public Review(@NotNull String serverUrl) {
		this.serverUrl = serverUrl;
//		reviewItems = new ArrayList<CrucibleReviewItemInfo>();
	}

	public Review(@NotNull String serverUrl, @NotNull String projectKey, @NotNull User author, @NotNull User moderator) {
		this.serverUrl = serverUrl;
		this.projectKey = projectKey;
		this.author = author;
		this.moderator = moderator;
	}

	@NotNull
	public String getServerUrl() {
		return serverUrl;
	}

	public Set<Reviewer> getReviewers() throws ValueNotYetInitialized {
		if (reviewers == null) {
			throw new ValueNotYetInitialized("Object trasferred only partially");
		}
		return reviewers;
	}

	public List<GeneralComment> getGeneralComments() throws ValueNotYetInitialized {
		if (generalComments == null) {
			throw new ValueNotYetInitialized("Object trasferred only partially");
		}
		return generalComments;
	}


	public EnumSet<CrucibleAction> getTransitions() throws ValueNotYetInitialized {
		if (transitions == null) {
			throw new ValueNotYetInitialized("Object trasferred only partially");
		}
		return transitions;
	}

	public EnumSet<CrucibleAction> getActions() throws ValueNotYetInitialized {
		if (actions == null) {
			throw new ValueNotYetInitialized("Object trasferred only partially");
		}
		return actions;
	}

	public boolean isCompleted() {

		for (Reviewer reviewer : reviewers) {
			if (!reviewer.isCompleted()) {
				return false;
			}
		}
		return true;
	}

	/**
	 * Gets the value of the author property.
	 *
	 * @return possible object is
	 *         {@link com.atlassian.theplugin.commons.crucible.api.model.User }
	 */
	@NotNull
	public User getAuthor() {
		return author;
	}

	/**
	 * Gets the value of the creator property.
	 *
	 * @return possible object is
	 *         {@link com.atlassian.theplugin.commons.crucible.api.model.User }
	 */
	public User getCreator() {
		return creator;
	}

	/**
	 * Sets the value of the creator property.
	 *
	 * @param value allowed object is
	 *              {@link com.atlassian.theplugin.commons.crucible.api.model.User }
	 */
	public void setCreator(User value) {
		this.creator = value;
	}

	/**
	 * Gets the value of the description property.
	 *
	 * @return possible object is
	 *         {@link String }
	 */
	public String getDescription() {
		return description;
	}

	/**
	 * Sets the value of the description property.
	 *
	 * @param value allowed object is
	 *              {@link String }
	 */
	public void setDescription(String value) {
		this.description = value;
	}

	/**
	 * Gets the value of the moderator property.
	 *
	 * @return possible object is
	 *         {@link com.atlassian.theplugin.commons.crucible.api.model.User }
	 * @remark moderator can be null since Crucible 2.2M1
	 */
	public User getModerator() {
		return moderator;
	}

	/**
	 * Sets the value of the moderator property.
	 *
	 * @param value allowed object is
	 *              {@link com.atlassian.theplugin.commons.crucible.api.model.User }
	 * @remark moderator can be null since Crucible 2.2M1
	 */
	public void setModerator(User value) {
		this.moderator = value;
	}

	/**
	 * Gets the value of the name property.
	 *
	 * @return possible object is
	 *         {@link String }
	 */
	public String getName() {
		return name;
	}

	/**
	 * Sets the value of the name property.
	 *
	 * @param value allowed object is
	 *              {@link String }
	 */
	public void setName(String value) {
		this.name = value;
	}

	/**
	 * Gets the value of the parentReview property.
	 *
	 * @return possible object is
	 *         {@link com.atlassian.theplugin.commons.crucible.api.model.PermId }
	 */
	@Nullable
	public PermId getParentReview() {
		return parentReview;
	}

	/**
	 * Sets the value of the parentReview property.
	 *
	 * @param value allowed object is
	 *              {@link com.atlassian.theplugin.commons.crucible.api.model.PermId }
	 */
	public void setParentReview(PermId value) {
		this.parentReview = value;
	}

	/**
	 * Gets the value of the permId property.
	 *
	 * @return possible object is
	 *         {@link com.atlassian.theplugin.commons.crucible.api.model.PermId }
	 */
	@Nullable
	public PermId getPermId() {
		return permId;
	}

	/**
	 * Sets the value of the permId property.
	 *
	 * @param value allowed object is
	 *              {@link com.atlassian.theplugin.commons.crucible.api.model.PermId }
	 */
	public void setPermId(PermId value) {
		this.permId = value;
	}

	/**
	 * Gets the value of the projectKey property.
	 *
	 * @return possible object is
	 *         {@link String }
	 */
	@NotNull
	public String getProjectKey() {
		return projectKey;
	}

	/**
	 * Sets the value of the projectKey property.
	 *
	 * @param value allowed object is
	 *              {@link String }
	 */
	public void setProjectKey(@NotNull String value) {
		this.projectKey = value;
	}

	/**
	 * Gets the value of the repoName property.
	 *
	 * @return possible object is
	 *         {@link String }
	 */
	@Nullable
	public String getRepoName() {
		return repoName;
	}

	/**
	 * Sets the value of the repoName property.
	 *
	 * @param value allowed object is
	 *              {@link String }
	 */
	public void setRepoName(String value) {
		this.repoName = value;
	}

	/**
	 * Gets the value of the state property.
	 *
	 * @return possible object is
	 *         {@link com.atlassian.theplugin.commons.crucible.api.model.State }
	 */
	@Nullable
	public State getState() {
		return state;
	}

	/**
	 * Sets the value of the state property.
	 *
	 * @param value allowed object is
	 *              {@link com.atlassian.theplugin.commons.crucible.api.model.State }
	 */
	public void setState(State value) {
		this.state = value;
	}

	public boolean isAllowReviewerToJoin() {
		return allowReviewerToJoin;
	}

	public void setAllowReviewerToJoin(boolean allowReviewerToJoin) {
		this.allowReviewerToJoin = allowReviewerToJoin;
	}

	public int getMetricsVersion() {
		return metricsVersion;
	}

	public void setMetricsVersion(int metricsVersion) {
		this.metricsVersion = metricsVersion;
	}

	public Date getCreateDate() {
		return createDate;
	}

	public void setCreateDate(Date createDate) {
		this.createDate = createDate;
	}

	public Date getCloseDate() {
		return closeDate;
	}

	public void setCloseDate(Date closeDate) {
		this.closeDate = closeDate;
	}

	public void setFilesAndVersionedComments(final Set<CrucibleFileInfo> aFiles, List<VersionedComment> commentList) {
		this.files = aFiles;
//		this.versionedComments = commentList;

		if (files != null && commentList != null) {
			for (VersionedComment comment : commentList) {
				for (CrucibleFileInfo f : aFiles) {
					if (f.getPermId().equals(comment.getReviewItemId())) {
						f.addComment(comment);
					}
				}
			}
		}
	}

//	public List<VersionedComment> getVersionedComments() throws ValueNotYetInitialized {

	//		return files;
	//			throw new ValueNotYetInitialized("Object trasferred only partially");
//		if (versionedComments == null) {
//		}

	//	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (o == null || getClass() != o.getClass()) {
			return false;
		}

		Review that = (Review) o;

		return !(permId != null ? !permId.equals(that.permId) : that.permId != null);
	}

	@Override
	public int hashCode() {
		int result;
		result = (permId != null ? permId.hashCode() : 0);
		return result;
	}

	public String getSummary() {
		return this.summary;
	}

	public void setSummary(String summary) {
		this.summary = summary;
	}

	@Nullable
	public CrucibleFileInfo getFileByPermId(PermId id) throws ValueNotYetInitialized {
//		List<CrucibleFileInfo> lFiles = CrucibleFileInfoManager.getInstance().getFiles(this);
		for (CrucibleFileInfo f : getFiles()) {
			if (f.getPermId().equals(id)) {
				return f;
			}
		}
		return null;
	}

	public Set<CrucibleFileInfo> getFiles() throws ValueNotYetInitialized {
		if (files == null) {
			throw new ValueNotYetInitialized("Files haven't been downloaded yet");
		}
		return files;
	}

	/**
	 * @return total number of versioned comments including replies (for all files)
	 */
	public int getNumberOfVersionedComments() throws ValueNotYetInitialized {
		int num = 0;
		for (CrucibleFileInfo file : getFiles()) {
			num += file.getNumberOfComments();
		}
		return num;
	}

	public int getNumberOfVersionedComments(final String userName) throws ValueNotYetInitialized {
		int num = 0;
		for (CrucibleFileInfo file : getFiles()) {
			num += file.getNumberOfComments(userName);
		}
		return num;
	}

	public int getNumberOfGeneralComments(final String userName) throws ValueNotYetInitialized {
		int num = 0;
		for (GeneralComment comment : getGeneralComments()) {
			if (comment.getAuthor().getUsername().equals(userName)) {
				++num;
			}
			for (Comment reply : comment.getReplies()) {
				if (reply.getAuthor().getUsername().equals(userName)) {
					++num;
				}
			}
		}
		return num;
	}


    public int getNumberOfUnreadComments() throws ValueNotYetInitialized {
        List<Comment> allComments = new ArrayList<Comment>();
        allComments.addAll(getGeneralComments());
        for (CrucibleFileInfo file : getFiles()) {
            allComments.addAll(file.getVersionedComments());
        }
        int result = 0;

        for (Comment comment : allComments) {
            if (comment.getReadState() == Comment.ReadState.UNREAD
                    || comment.getReadState() == Comment.ReadState.LEAVE_UNREAD) {
                ++result;
            }
            for (Comment reply : comment.getReplies()) {
                if (reply.getReadState() == Comment.ReadState.UNREAD
                        || reply.getReadState() == Comment.ReadState.LEAVE_UNREAD) {
                    ++result;
                }
            }
        }
        return result;
    }

	public void setFiles(final Set<CrucibleFileInfo> files) {
		this.files = files;
	}

	public int getNumberOfVersionedCommentsDefects() throws ValueNotYetInitialized {
		int num = 0;
		for (CrucibleFileInfo file : getFiles()) {
			num += file.getNumberOfCommentsDefects();
		}
		return num;
	}

	public int getNumberOfVersionedCommentsDefects(final String userName) throws ValueNotYetInitialized {
		int num = 0;
		for (CrucibleFileInfo file : getFiles()) {
			num += file.getNumberOfCommentsDefects(userName);
		}
		return num;
	}

	public int getNumberOfVersionedCommentsDrafts() throws ValueNotYetInitialized {
		int num = 0;
		for (CrucibleFileInfo file : getFiles()) {
			num += file.getNumberOfCommentsDrafts();
		}
		return num;
	}

	public int getNumberOfVersionedCommentsDrafts(final String userName) throws ValueNotYetInitialized {
		int num = 0;
		for (CrucibleFileInfo file : getFiles()) {
			num += file.getNumberOfCommentsDrafts(userName);
		}
		return num;
	}

	public int getNumberOfGeneralCommentsDrafts() throws ValueNotYetInitialized {
		int num = 0;
		for (GeneralComment comment : getGeneralComments()) {
			if (comment.isDraft()) {
				++num;
			}
			for (Comment reply : comment.getReplies()) {
				if (reply.isDraft()) {
					++num;
				}
			}
		}
		return num;
	}

	public int getNumberOfGeneralCommentsDrafts(final String userName) throws ValueNotYetInitialized {
		int num = 0;
		for (GeneralComment comment : getGeneralComments()) {
			if (comment.isDraft() && comment.getAuthor().getUsername().equals(userName)) {
				++num;
			}
			for (Comment reply : comment.getReplies()) {
				if (reply.isDraft() && reply.getAuthor().getUsername().equals(userName)) {
					++num;
				}
			}
		}
		return num;
	}

	public int getNumberOfGeneralCommentsDefects() throws ValueNotYetInitialized {
		int num = 0;
		for (GeneralComment comment : getGeneralComments()) {
			if (comment.isDefectRaised()) {
				++num;
			}
			for (Comment reply : comment.getReplies()) {
				if (reply.isDefectRaised()) {
					++num;
				}
			}
		}
		return num;
	}

	public int getNumberOfGeneralCommentsDefects(final String userName) throws ValueNotYetInitialized {
		int num = 0;
		for (GeneralComment comment : getGeneralComments()) {
			if (comment.isDefectRaised() && comment.getAuthor().getUsername().equals(userName)) {
				++num;
			}
			for (Comment reply : comment.getReplies()) {
				if (reply.isDefectRaised() && reply.getAuthor().getUsername().equals(userName)) {
					++num;
				}
			}
		}
		return num;
	}

	public int getNumberOfGeneralComments() throws ValueNotYetInitialized {
		int num = getGeneralComments().size();
		for (GeneralComment comment : getGeneralComments()) {
			num += comment.getReplies().size();
		}
		return num;
	}

}