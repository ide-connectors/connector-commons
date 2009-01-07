package com.atlassian.theplugin.commons.crucible.api.model.notification;

import com.atlassian.theplugin.commons.crucible.api.model.ReviewAdapter;
import com.atlassian.theplugin.commons.crucible.api.model.VersionedComment;

public class UpdatedVersionedCommentNotification extends AbstractReviewNotification {

	private final VersionedComment comment;

	public UpdatedVersionedCommentNotification(ReviewAdapter review, VersionedComment comment) {
		super(review);
		this.comment = comment;
	}

	public CrucibleNotificationType getType() {
		return CrucibleNotificationType.UPDATED_VERSIONED_COMMENT;
	}

	public String getPresentationMessage() {
		return "Comment updated by " + comment.getAuthor().getDisplayName();
	}
}
