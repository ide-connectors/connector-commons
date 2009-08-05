package com.atlassian.theplugin.commons.crucible.api.model.notification;

import static com.atlassian.theplugin.commons.util.MiscUtil.isModified;
import com.atlassian.theplugin.commons.crucible.ValueNotYetInitialized;
import com.atlassian.theplugin.commons.crucible.api.model.Comment;
import com.atlassian.theplugin.commons.crucible.api.model.CrucibleAction;
import com.atlassian.theplugin.commons.crucible.api.model.CrucibleFileInfo;
import com.atlassian.theplugin.commons.crucible.api.model.GeneralComment;
import com.atlassian.theplugin.commons.crucible.api.model.Review;
import com.atlassian.theplugin.commons.crucible.api.model.Reviewer;
import com.atlassian.theplugin.commons.crucible.api.model.VersionedComment;
import com.atlassian.theplugin.commons.util.MiscUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * This class is NOT thread-safe!
 */
public class ReviewDifferenceProducer {
	private final Review oldReview;
	private final Review newReview;
	private final List<CrucibleNotification> notifications = new ArrayList<CrucibleNotification>();
	private boolean shortEqual;
	private boolean filesEqual;
	private int changes;

	public ReviewDifferenceProducer(@NotNull final Review oldReview, @NotNull final Review newReview) {
		this.oldReview = oldReview;
		this.newReview = newReview;
	}

	public boolean isShortEqual() {
		return shortEqual;
	}

	public boolean isFilesEqual() {
		return filesEqual;
	}

	public int getCommentChangesCount() {
		return changes;
	}

	public List<CrucibleNotification> getDiff() {
		notifications.clear();
		if (isModified(oldReview.getDescription(), newReview.getDescription())) {
			notifications.add(new BasisReviewDetailsChangedNotification(newReview,
					CrucibleNotificationType.STATEMENT_OF_OBJECTIVES_CHANGED, "Statement of Objectives has been changed"));
		}
		if (isModified(oldReview.getName(), newReview.getName())) {
			notifications.add(new BasisReviewDetailsChangedNotification(newReview, CrucibleNotificationType.NAME_CHANGED,
					"Review name has been changed"));
		}

		if (isModified(oldReview.getModerator(), newReview.getModerator())) {
			notifications.add(new BasisReviewDetailsChangedNotification(newReview, CrucibleNotificationType.MODERATOR_CHANGED,
					"Moderator has changed"));
		}

		if (isModified(oldReview.getAuthor(), newReview.getAuthor())) {
			notifications.add(new BasisReviewDetailsChangedNotification(newReview, CrucibleNotificationType.AUTHOR_CHANGED,
					"Author has changed"));
		}

		if (isModified(oldReview.getSummary(), newReview.getSummary())) {
			notifications.add(new BasisReviewDetailsChangedNotification(newReview, CrucibleNotificationType.SUMMARY_CHANGED,
					"Summary has been changed"));
		}

		if (isModified(oldReview.getProjectKey(), newReview.getProjectKey())) {
			notifications.add(new BasisReviewDetailsChangedNotification(newReview, CrucibleNotificationType.PROJECT_CHANGED,
					"Project has been changed"));
		}

		processReviewers();

		shortEqual = isShortContentEqual();
		if (!shortEqual) {
			notifications.add(new ReviewDataChangedNotification(newReview));
		}
		filesEqual = areFilesEqual();
		// check comments status
		try {
			changes = checkComments(oldReview, newReview, true);
		} catch (ValueNotYetInitialized valueNotYetInitialized) {
			//all is it correct
		}

		return notifications;
	}

	private boolean isShortContentEqual() {
		return !stateChanged()
				&& areGeneralCommentsEqual()
				&& areActionsEqual()
				&& oldReview.isAllowReviewerToJoin() == newReview.isAllowReviewerToJoin()
				&& oldReview.getMetricsVersion() == newReview.getMetricsVersion()
				&& areObjectsEqual(oldReview.getCloseDate(), newReview.getCloseDate())
				&& areObjectsEqual(oldReview.getCreateDate(), newReview.getCreateDate())
				&& areObjectsEqual(oldReview.getCreator(), newReview.getCreator())
				&& areObjectsEqual(oldReview.getParentReview(), newReview.getParentReview())
				&& areObjectsEqual(oldReview.getRepoName(), newReview.getRepoName())
				&& areTransitionsEqual();
	}

	private boolean areFilesEqual() {
		Set<CrucibleFileInfo> l = null;
		Set<CrucibleFileInfo> r = null;
		try {
			l = oldReview.getFiles();
		} catch (ValueNotYetInitialized e) {	/* ignore */ }
		try {
			r = newReview.getFiles();
		} catch (ValueNotYetInitialized e) { /* ignore */ }

		if (l == null && r == null) {
			return true;
		}
		if (l == null || r == null) {
			return false;
		}
		boolean areFilesEqual = l.equals(r);
		if (!areFilesEqual) {
			for (CrucibleFileInfo crucibleFileInfo : r) {
				if (!l.contains(crucibleFileInfo)) {
					notifications.add(new NewReviewItemNotification(newReview));
				}
			}
			for (CrucibleFileInfo crucibleFileInfo : l) {
				if (!r.contains(crucibleFileInfo)) {
					notifications.add(new RemovedReviewItemNotification(oldReview));
				}
			}
		}
		return areFilesEqual;
	}

	private boolean areGeneralCommentsEqual() {
		List<GeneralComment> l = null;
		List<GeneralComment> r = null;
		try {
			l = oldReview.getGeneralComments();
		} catch (ValueNotYetInitialized e) {	/* ignore */ }
		try {
			r = newReview.getGeneralComments();
		} catch (ValueNotYetInitialized e) { /* ignore */ }
		return areObjectsEqual(l, r);
	}

	private boolean areActionsEqual() {
		Set<CrucibleAction> l = null;
		Set<CrucibleAction> r = null;
		try {
			l = oldReview.getActions();
		} catch (ValueNotYetInitialized e) {	/* ignore */ }
		try {
			r = newReview.getActions();
		} catch (ValueNotYetInitialized e) { /* ignore */ }
		return areObjectsEqual(l, r);
	}

	private boolean areTransitionsEqual() {
		EnumSet<CrucibleAction> l = null;
		EnumSet<CrucibleAction> r = null;
		try {
			l = oldReview.getTransitions();
		} catch (ValueNotYetInitialized e) {	/* ignore */ }
		try {
			r = newReview.getTransitions();
		} catch (ValueNotYetInitialized e) { /* ignore */ }
		return areObjectsEqual(l, r);
	}

	private static <T> boolean areObjectsEqual(T oldReview, T newReview) {
		return MiscUtil.isEqual(oldReview, newReview);
	}

	private boolean stateChanged() {
		if (!MiscUtil.isEqual(oldReview.getState(), newReview.getState())) {
			notifications.add(new ReviewStateChangedNotification(oldReview, newReview.getState()));
			return true;
		}
		return false;
	}

	@Nullable
	private Collection<String> buildReviewerSet(@Nullable Set<Reviewer> reviewers) {
		if (reviewers == null) {
			return null;
		}
		final Set<String> res = new HashSet<String>(reviewers.size() * 2);
		for (Reviewer reviewer : reviewers) {
			res.add(reviewer.getUserName());
		}
		return res;
	}

	private void processReviewers() {
		boolean allCompleted = true;
		boolean atLeastOneChanged = false;

		Set<Reviewer> oldReviewers = null;
		Set<Reviewer> newReviewers = null;
		try {
			oldReviewers = oldReview.getReviewers();
		} catch (ValueNotYetInitialized e) {	/* ignore */ }
		try {
			newReviewers = newReview.getReviewers();
		} catch (ValueNotYetInitialized e) { /* ignore */ }

		final Collection<String> oldR = buildReviewerSet(oldReviewers);
		final Collection<String> newR = buildReviewerSet(newReviewers);

		if (MiscUtil.isModified(oldR, newR)) {
			notifications.add(new BasisReviewDetailsChangedNotification(newReview, CrucibleNotificationType.REVIEWERS_CHANGED,
					"Reviewers have been changed"));
		}

		if (oldReviewers == null || newReviewers == null) {
			return;
		}

		for (Reviewer reviewer : newReviewers) {
			for (Reviewer oldReviewer : oldReviewers) {
				if (reviewer.getUserName().equals(oldReviewer.getUserName())) {
					if (reviewer.isCompleted() != oldReviewer.isCompleted()) {
						notifications.add(new ReviewerCompletedNotification(newReview, reviewer));
						atLeastOneChanged = true;
					}
				}
			}
			if (!reviewer.isCompleted()) {
				allCompleted = false;
			}
		}
		if (allCompleted && atLeastOneChanged) {
			notifications.add(new ReviewCompletedNotification(newReview));
		}
	}

	private int checkGeneralReplies(Review review, GeneralComment oldComment, GeneralComment newComment) {
		int replyChanges = 0;
		for (Comment reply : newComment.getReplies()) {
			GeneralComment existingReply = null;
			if (oldComment != null) {
				for (Comment oldReply : oldComment.getReplies()) {
					if (reply.getPermId().getId().equals(oldReply.getPermId().getId())) {
						existingReply = (GeneralComment) oldReply;
						break;
					}
				}
				if ((existingReply == null)
						|| !existingReply.getMessage().equals(reply.getMessage())
						|| existingReply.isDraft() != reply.isDraft()) {
					if (existingReply == null) {
						replyChanges++;
						notifications.add(new NewReplyCommentNotification(review, newComment, reply, reply.getAuthor(),
								reply.isDraft()));
					} else {
						replyChanges++;
						notifications.add(new UpdatedReplyCommentNotification(review, reply, reply.getAuthor(),
								reply.isDraft(), existingReply.isDraft()));
					}
				}
			}
		}

		if (oldComment != null) {
			List<Comment> deletedGen = getDeletedComments(
					oldComment.getReplies(), newComment.getReplies());
			for (Comment gc : deletedGen) {
				replyChanges++;
				notifications.add(new RemovedReplyCommentNotification(review, gc, gc.getAuthor(), gc.isDraft()));
			}
		}
		return replyChanges;
	}

	private int checkVersionedReplies(Review review, VersionedComment oldComment, VersionedComment newComment) {
		int replyChanges = 0;
		for (Comment reply : newComment.getReplies()) {
			VersionedComment existingReply = null;
			if (oldComment != null) {
				for (Comment oldReply : oldComment.getReplies()) {
					if (reply.getPermId().getId().equals(oldReply.getPermId().getId())) {
						existingReply = (VersionedComment) oldReply;
						break;
					}
				}
				if ((existingReply == null)
						|| !existingReply.getMessage().equals(reply.getMessage())
						|| existingReply.isDraft() != reply.isDraft()) {
					if (existingReply == null) {
						replyChanges++;
						notifications.add(new NewReplyCommentNotification(review, newComment, reply, reply.getAuthor(),
								reply.isDraft()));
					} else {
						replyChanges++;
						notifications.add(new UpdatedReplyCommentNotification(review, reply, reply.getAuthor(),
								reply.isDraft(), existingReply.isDraft()));

					}
				}
			}
		}

		if (oldComment != null) {
			List<Comment> deletedVcs = getDeletedComments(oldComment.getReplies(), newComment.getReplies());
			for (Comment vc : deletedVcs) {
				replyChanges++;
				notifications.add(new RemovedReplyCommentNotification(review, vc, vc.getAuthor(), vc.isDraft()));
			}
		}
		return replyChanges;
	}

	private int checkComments(final Review oldReview, final Review newReview,
			final boolean checkFiles)
			throws ValueNotYetInitialized {
		int commentChanges = 0;

		for (GeneralComment comment : newReview.getGeneralComments()) {
			GeneralComment existing = null;
			for (GeneralComment oldComment : oldReview.getGeneralComments()) {
				if (comment.getPermId().getId().equals(oldComment.getPermId().getId())) {
					existing = oldComment;
					break;
				}
			}

			if ((existing == null)
					|| !existing.getMessage().equals(comment.getMessage())
					|| existing.isDefectRaised() != comment.isDefectRaised()
					|| existing.isDraft() != comment.isDraft()) {
				if (existing == null) {
					commentChanges++;
					notifications.add(new NewGeneralCommentNotification(newReview, comment, comment.getAuthor(),
							comment.isDraft()));
				} else {
					commentChanges++;
					notifications.add(new UpdatedGeneralCommentNotification(newReview, comment.getAuthor(),
							comment.isDraft(), existing.isDraft()));
				}
			}
			commentChanges += checkGeneralReplies(newReview, existing, comment);
		}

		List<GeneralComment> deletedGen = getDeletedComments(
				oldReview.getGeneralComments(), newReview.getGeneralComments());
		for (GeneralComment gc : deletedGen) {
			commentChanges++;
			notifications.add(new RemovedGeneralCommentNotification(newReview, gc, gc.getAuthor(), gc.isDraft()));
		}

		if (checkFiles) {
			for (CrucibleFileInfo fileInfo : newReview.getFiles()) {
				for (VersionedComment comment : fileInfo.getVersionedComments()) {
					VersionedComment existing = null;
					for (CrucibleFileInfo oldFile : oldReview.getFiles()) {
						if (oldFile.getPermId().equals(fileInfo.getPermId())) {
							for (VersionedComment oldComment : oldFile.getVersionedComments()) {
								if (comment.getPermId().getId().equals(oldComment.getPermId().getId())) {
									existing = oldComment;
									break;
								}
							}
						}
					}
					if ((existing == null)
							|| !existing.getMessage().equals(comment.getMessage())
							|| existing.isDefectRaised() != comment.isDefectRaised()
							|| existing.isDraft() != comment.isDraft()) {
						if (existing == null) {
							commentChanges++;
							notifications
									.add(new NewVersionedCommentNotification(newReview, comment, comment.getAuthor(),
											comment.isDraft()));
						} else {
							commentChanges++;
							notifications
									.add(new UpdatedVersionedCommentNotification(newReview, comment.getAuthor(),
											comment.isDraft(), existing.isDraft()));
						}
					}
					commentChanges += checkVersionedReplies(newReview, existing, comment);
				}
			}

			for (CrucibleFileInfo oldFile : oldReview.getFiles()) {
				for (CrucibleFileInfo newFile : newReview.getFiles()) {
					if (oldFile.getPermId().equals(newFile.getPermId())) {
						List<VersionedComment> oldVersionedComments = new ArrayList<VersionedComment>();
						List<VersionedComment> newVersionedComments = new ArrayList<VersionedComment>();
						oldVersionedComments.addAll(oldFile.getVersionedComments());
						newVersionedComments.addAll(newFile.getVersionedComments());
						List<VersionedComment> deletedVcs = getDeletedComments(
								oldVersionedComments, newVersionedComments);
						for (VersionedComment vc : deletedVcs) {
							commentChanges++;
							notifications.add(new RemovedVersionedCommentNotification(newReview, vc, vc.getAuthor(),
									vc.isDraft()));
						}
					}
				}
			}
		}
		if (commentChanges > 0) {
			filesEqual = false;
		}
		return commentChanges;
	}

	private <T extends Comment> List<T> getDeletedComments(List<T> org, List<T> modified) {
		List<T> deletedList = new ArrayList<T>();

		for (T corg : org) {
			boolean found = false;
			for (T cnew : modified) {
				if (cnew.getPermId().equals(corg.getPermId())) {
					found = true;
					break;
				}
			}
			if (!found) {
				deletedList.add(corg);
			}
		}

		return deletedList;
	}
}
