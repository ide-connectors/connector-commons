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

package com.atlassian.theplugin.commons.crucible.api.model;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

/**
 * Contains almost all review data which can be transferred quite cheaply by Crucible for (e.g. while doing queries returning
 * many reviews).
 *
 * The only thing which is not returned (as of Crucible 1.6.x) are files ({@link CrucibleFileInfo}).
 *
 * @author wseliga
 */
public class BasicReview {
	private Set<Reviewer> reviewers;
	private List<Comment> generalComments;
	private EnumSet<CrucibleAction> transitions = EnumSet.<CrucibleAction> noneOf(CrucibleAction.class);
	private EnumSet<CrucibleAction> actions = EnumSet.<CrucibleAction> noneOf(CrucibleAction.class);
	@NotNull
	private User author;
	private User creator;
	private String description;
	@Nullable
	private User moderator;
	private String name;
	/** this field seems to be not initialized by ACC at all */
	@Nullable
	private PermId parentReview;
	private PermId permId;
	@NotNull
	private String projectKey;
	private String repoName;
	private State state;
	private boolean allowReviewerToJoin;
	private int metricsVersion;
	private Date createDate;
	private Date closeDate;
	private String summary;
	private final String serverUrl;

	public BasicReview(@NotNull String serverUrl, @NotNull String projectKey, @NotNull User author, @Nullable User moderator) {
		this.serverUrl = serverUrl;
		this.projectKey = projectKey;
		this.author = author;
		this.moderator = moderator;
	}

	public void setReviewers(Set<Reviewer> reviewers) {
		this.reviewers = reviewers;
	}

	public void setGeneralComments(@NotNull List<Comment> generalComments) {
		this.generalComments = generalComments;
	}

	/**
	 * Removes comment from the model
	 *
	 * @param generalComment
	 *            comment to be removed
	 */
	public void removeGeneralComment(final Comment generalComment) {
		if (!generalComment.isReply()) {
			generalComments.remove(generalComment);
		} else {
			for (Comment comment : generalComments) {
				if (comment.getReplies().remove(generalComment)) {
					return;
				}
			}
		}
	}


	public void setTransitions(@NotNull Collection<CrucibleAction> transitions) {
		// as EnumSet.copyOf does not work for empty collections we use such 2-phase approach
		this.transitions = EnumSet.noneOf(CrucibleAction.class);
		this.transitions.addAll(transitions);
	}

	public void setActions(@NotNull Set<CrucibleAction> actions) {
		// as EnumSet.copyOf does not work for empty collections we use such 2-phase approach
		this.actions = EnumSet.noneOf(CrucibleAction.class);
		this.actions.addAll(actions);
	}

	public void setAuthor(@NotNull final User author) {
		this.author = author;
	}

	@NotNull
	public String getServerUrl() {
		return serverUrl;
	}

	public Set<Reviewer> getReviewers() {
		return reviewers;
	}

	@NotNull
	public List<Comment> getGeneralComments() {
		if (generalComments == null) {
			return Collections.emptyList();
		}
		return generalComments;
	}

	public EnumSet<CrucibleAction> getTransitions() {
		return transitions;
	}

	@NotNull
	public EnumSet<CrucibleAction> getActions() {
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

	@NotNull
	public User getAuthor() {
		return author;
	}

	public User getCreator() {
		return creator;
	}

	public void setCreator(User value) {
		this.creator = value;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String value) {
		this.description = value;
	}

	public User getModerator() {
		return moderator;
	}

	public void setModerator(User value) {
		this.moderator = value;
	}

	public String getName() {
		return name;
	}

	public void setName(String value) {
		this.name = value;
	}

	@Nullable
	public PermId getParentReview() {
		return parentReview;
	}

	public void setParentReview(PermId value) {
		this.parentReview = value;
	}

	@Nullable
	public PermId getPermId() {
		return permId;
	}

	public void setPermId(PermId value) {
		this.permId = value;
	}

	@NotNull
	public String getProjectKey() {
		return projectKey;
	}

	public void setProjectKey(@NotNull String value) {
		this.projectKey = value;
	}

	@Nullable
	public String getRepoName() {
		return repoName;
	}

	public void setRepoName(String value) {
		this.repoName = value;
	}

	@Nullable
	public State getState() {
		return state;
	}

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

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (o == null || getClass() != o.getClass()) {
			return false;
		}

		BasicReview that = (BasicReview) o;

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

	/**
	 * @return total number of versioned comments including replies (for all files)
	 */
	public int getNumberOfVersionedComments() {
		return 0;
		// !!! @fixme wseliga refactoring
		// for (CrucibleFileInfo file : getFiles()) {
		// num += file.getNumberOfComments();
		// }
		// return num;
	}

	public int getNumberOfVersionedComments(final String userName) {
		// !!! @fixme wseliga refactoring
		int num = 0;
		// for (CrucibleFileInfo file : getFiles()) {
		// num += file.getNumberOfComments(userName);
		// }
		return num;
	}

	public int getNumberOfGeneralComments(final String userName) {
		int num = 0;
		for (Comment comment : getGeneralComments()) {
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

	public int getNumberOfUnreadComments() {
		List<Comment> allComments = new ArrayList<Comment>();
		allComments.addAll(getGeneralComments());
		// !!! @fixme wseliga refactoring
		// for (CrucibleFileInfo file : getFiles()) {
		// allComments.addAll(file.getVersionedComments());
		// }
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

	public int getNumberOfVersionedCommentsDefects() {
		int num = 0;
		// !!! @fixme wseliga refactoring
		// for (CrucibleFileInfo file : getFiles()) {
		// num += file.getNumberOfCommentsDefects();
		// }
		return num;
	}

	public int getNumberOfVersionedCommentsDefects(final String userName) {
		int num = 0;
		// !!! @fixme wseliga refactoring
		// for (CrucibleFileInfo file : getFiles()) {
		// num += file.getNumberOfCommentsDefects(userName);
		// }
		return num;
	}

	public int getNumberOfVersionedCommentsDrafts() {
		int num = 0;
		// !!! @fixme wseliga refactoring
		// for (CrucibleFileInfo file : getFiles()) {
		// num += file.getNumberOfCommentsDrafts();
		// }
		return num;
	}

	public int getNumberOfVersionedCommentsDrafts(final String userName) {
		int num = 0;
		// !!! @fixme wseliga refactoring
		// for (CrucibleFileInfo file : getFiles()) {
		// num += file.getNumberOfCommentsDrafts(userName);
		// }
		return num;
	}

	public int getNumberOfGeneralCommentsDrafts() {
		int num = 0;
		for (Comment comment : getGeneralComments()) {
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

	public int getNumberOfGeneralCommentsDrafts(final String userName) {
		int num = 0;
		for (Comment comment : getGeneralComments()) {
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

	public int getNumberOfGeneralCommentsDefects() {
		int num = 0;
		for (Comment comment : getGeneralComments()) {
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

	public int getNumberOfGeneralCommentsDefects(final String userName) {
		int num = 0;
		for (Comment comment : getGeneralComments()) {
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

	public int getNumberOfGeneralComments() {
		int num = getGeneralComments().size();
		for (Comment comment : getGeneralComments()) {
			num += comment.getReplies().size();
		}
		return num;
	}

}