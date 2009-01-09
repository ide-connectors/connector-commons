package com.atlassian.theplugin.commons.crucible.api.model.notification;

import com.atlassian.theplugin.commons.crucible.ValueNotYetInitialized;
import com.atlassian.theplugin.commons.crucible.api.model.*;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class ReviewDifferenceProducer {
	private final ReviewAdapter oldReview;
	private final ReviewAdapter newReview;
	private final List<CrucibleNotification> notifications = new ArrayList<CrucibleNotification>();
	private boolean shortEqual;
	private boolean filesEqual;

	public ReviewDifferenceProducer(@NotNull final ReviewAdapter oldReview, @NotNull final ReviewAdapter newReview) {
		this.oldReview = oldReview;
		this.newReview = newReview;
	}

	public boolean isShortEqual() {
		return shortEqual;
	}

	public boolean isFilesEqual() {
		return filesEqual;
	}

	public List<CrucibleNotification> getDiff() {
		shortEqual = isShortContentEqual();
		filesEqual = areFilesEqual();
		// check comments status
		try {
			checkComments(oldReview, newReview, true);
		} catch (ValueNotYetInitialized valueNotYetInitialized) {
			//all is it correct
		}

		return notifications;
	}

	private boolean isShortContentEqual() {
		return !stateChanged()
				&& !reviewersStatusChanged()
				&& areGeneralCommentsEqual()
				&& areActionsEqual()
				&& oldReview.isAllowReviewerToJoin() == newReview.isAllowReviewerToJoin()
				&& oldReview.getMetricsVersion() == newReview.getMetricsVersion()
				&& areObjectsEqual(oldReview.getAuthor(), newReview.getAuthor())
				&& areObjectsEqual(oldReview.getCloseDate(), newReview.getCloseDate())
				&& areObjectsEqual(oldReview.getCreateDate(), newReview.getCreateDate())
				&& areObjectsEqual(oldReview.getCreator(), newReview.getCreator())
				&& areObjectsEqual(oldReview.getDescription(), newReview.getDescription())
				&& areObjectsEqual(oldReview.getModerator(), newReview.getModerator())
				&& areObjectsEqual(oldReview.getName(), newReview.getName())
				&& areObjectsEqual(oldReview.getParentReview(), newReview.getParentReview())
				&& areObjectsEqual(oldReview.getProjectKey(), newReview.getProjectKey())
				&& areObjectsEqual(oldReview.getRepoName(), newReview.getRepoName())
				&& areObjectsEqual(oldReview.getSummary(), newReview.getSummary())
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
		Set<Action> l = null;
		Set<Action> r = null;
		try {
			l = oldReview.getActions();
		} catch (ValueNotYetInitialized e) {	/* ignore */ }
		try {
			r = newReview.getActions();
		} catch (ValueNotYetInitialized e) { /* ignore */ }
		return areObjectsEqual(l, r);
	}

	private boolean areTransitionsEqual() {
		List<Action> l = null;
		List<Action> r = null;
		try {
			l = oldReview.getTransitions();
		} catch (ValueNotYetInitialized e) {	/* ignore */ }
		try {
			r = newReview.getTransitions();
		} catch (ValueNotYetInitialized e) { /* ignore */ }
		return areObjectsEqual(l, r);
	}

	private static boolean areObjectsEqual(Object oldReview, Object newReview) {
		if (oldReview == null && newReview == null) {
			return true;
		}
		if (oldReview == null || newReview == null) {
			return false;
		}
		return oldReview.equals(newReview);
	}

	private boolean stateChanged() {
		if (!oldReview.getState().equals(newReview.getState())) {
			notifications.add(new ReviewStateChangedNotification(oldReview, newReview.getState()));
			return true;
		}
		return false;
	}

	private boolean reviewersStatusChanged() {
		boolean change = false;
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

		if (oldReviewers == null && newReviewers == null) {
			return false;
		}
		if (oldReviewers == null || newReviewers == null) {
			return true;
		}
		if (oldReviewers.size() != newReviewers.size()) {
			return true;
		}

		for (Reviewer reviewer : newReviewers) {
			for (Reviewer oldReviewer : oldReviewers) {
				if (reviewer.getUserName().equals(oldReviewer.getUserName())) {
					if (reviewer.isCompleted() != oldReviewer.isCompleted()) {
						notifications.add(new ReviewerCompletedNotification(newReview, reviewer));
						change = true;
						atLeastOneChanged = true;
					}
				}
			}
			if (!reviewer.isCompleted()) {
				allCompleted = false;
			}
		}
		if (allCompleted && atLeastOneChanged) {
			change = true;
			notifications.add(new ReviewCompletedNotification(newReview));
		}
		return change;
	}

	private void checkGeneralReplies(ReviewAdapter review, GeneralComment oldComment, GeneralComment newComment) {
		for (GeneralComment reply : newComment.getReplies()) {
			GeneralComment existingReply = null;
			if (oldComment != null) {
				for (GeneralComment oldReply : oldComment.getReplies()) {
					if (reply.getPermId().getId().equals(oldReply.getPermId().getId())) {
						existingReply = oldReply;
						break;
					}
				}
				if ((existingReply == null) || !existingReply.getMessage().equals(reply.getMessage())) {
					if (existingReply == null) {
						notifications.add(new NewReplyCommentNotification(review, newComment, reply));
					} else {
						notifications.add(new UpdatedReplyCommentNotification(review, newComment, reply));
					}
				}
			}
		}

		if (oldComment != null) {
			List<GeneralComment> deletedGen = getDeletedComments(
					oldComment.getReplies(), newComment.getReplies());
			for (GeneralComment gc : deletedGen) {
				notifications.add(new RemovedReplyCommentNotification(review, gc));
			}
		}
	}

	private void checkVersionedReplies(ReviewAdapter review, final PermId filePermId, VersionedComment oldComment,
			VersionedComment newComment) {
		for (VersionedComment reply : newComment.getReplies()) {
			VersionedComment existingReply = null;
			if (oldComment != null) {
				for (VersionedComment oldReply : oldComment.getReplies()) {
					if (reply.getPermId().getId().equals(oldReply.getPermId().getId())) {
						existingReply = oldReply;
						break;
					}
				}
				if ((existingReply == null) || !existingReply.getMessage().equals(reply.getMessage())) {
					if (existingReply == null) {
						notifications.add(new NewReplyCommentNotification(review, newComment, reply));
					} else {
						notifications.add(new UpdatedReplyCommentNotification(review, newComment, reply));
					}
				}
			}
		}

		if (oldComment != null) {
			List<VersionedComment> deletedVcs = getDeletedComments(
					oldComment.getReplies(), newComment.getReplies());
			for (VersionedComment vc : deletedVcs) {
				notifications.add(new RemovedReplyCommentNotification(review, vc));
			}
		}
	}

	private void checkComments(final ReviewAdapter oldReviewAdapter, final ReviewAdapter newReviewAdapter,
			final boolean checkFiles)
			throws ValueNotYetInitialized {
		for (GeneralComment comment : newReviewAdapter.getGeneralComments()) {
			GeneralComment existing = null;
			for (GeneralComment oldComment : oldReviewAdapter.getGeneralComments()) {
				if (comment.getPermId().getId().equals(oldComment.getPermId().getId())) {
					existing = oldComment;
					break;
				}
			}

			if ((existing == null)
					|| !existing.getMessage().equals(comment.getMessage())
					|| existing.isDefectRaised() != comment.isDefectRaised()) {
				if (existing == null) {
					notifications.add(new NewGeneralCommentNotification(newReviewAdapter, comment));
				} else {
					notifications.add(new UpdatedGeneralCommentNotification(newReviewAdapter, comment));
				}
			}
			checkGeneralReplies(newReviewAdapter, existing, comment);
		}

		List<GeneralComment> deletedGen = getDeletedComments(
				oldReviewAdapter.getGeneralComments(), newReviewAdapter.getGeneralComments());
		for (GeneralComment gc : deletedGen) {
			notifications.add(new RemovedGeneralCommentNotification(newReviewAdapter, gc));
		}

		if (checkFiles) {
			for (CrucibleFileInfo fileInfo : newReviewAdapter.getFiles()) {
				for (VersionedComment comment : fileInfo.getVersionedComments()) {
					VersionedComment existing = null;
					for (CrucibleFileInfo oldFile : oldReviewAdapter.getFiles()) {
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
							|| existing.isDefectRaised() != comment.isDefectRaised()) {
						if (existing == null) {
							notifications.add(new NewVersionedCommentNotification(newReviewAdapter, comment));
						} else {
							notifications.add(new UpdatedVersionedCommentNotification(newReviewAdapter, comment));
						}
					}
					checkVersionedReplies(newReviewAdapter, fileInfo.getPermId(), existing, comment);
				}
			}

			for (CrucibleFileInfo oldFile : oldReviewAdapter.getFiles()) {
				for (CrucibleFileInfo newFile : newReviewAdapter.getFiles()) {
					if (oldFile.getPermId().equals(newFile.getPermId())) {
						List<VersionedComment> oldVersionedComments = new ArrayList<VersionedComment>();
						List<VersionedComment> newVersionedComments = new ArrayList<VersionedComment>();
						oldVersionedComments.addAll(oldFile.getVersionedComments());
						newVersionedComments.addAll(newFile.getVersionedComments());
						List<VersionedComment> deletedVcs = getDeletedComments(
								oldVersionedComments, newVersionedComments);
						for (VersionedComment vc : deletedVcs) {
							notifications.add(new RemovedVersionedCommentNotification(newReviewAdapter, vc));
						}
					}
				}
			}
		}
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
