package com.atlassian.theplugin.commons.crucible.api.model.notification;

import com.atlassian.theplugin.commons.crucible.api.model.ReviewAdapter;
import com.atlassian.theplugin.commons.crucible.api.model.VersionedComment;

public class RemovedVersionedCommentNotification extends AbstractReviewNotification {
	private final VersionedComment comment;

	public RemovedVersionedCommentNotification(ReviewAdapter review, VersionedComment comment) {
		super(review);
		this.comment = comment;
	}

	public CrucibleNotificationType getType() {
		return CrucibleNotificationType.REMOVED_VERSIONED_COMMENT;
	}

	public String getPresentationMessage() {
		return "Comment removed by " + comment.getAuthor().getDisplayName();
	}
}
