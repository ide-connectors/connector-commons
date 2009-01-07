package com.atlassian.theplugin.commons.crucible.api.model.notification;

import com.atlassian.theplugin.commons.crucible.api.model.GeneralComment;
import com.atlassian.theplugin.commons.crucible.api.model.ReviewAdapter;

public class RemovedGeneralCommentNotification extends AbstractReviewNotification {
	private final GeneralComment comment;

	public RemovedGeneralCommentNotification(ReviewAdapter review, GeneralComment comment) {
		super(review);
		this.comment = comment;
	}

	public CrucibleNotificationType getType() {
		return CrucibleNotificationType.DELETED_GENERAL_COMMENT;
	}

	public String getPresentationMessage() {
		return "General comment removed by " + comment.getAuthor().getDisplayName();
	}
}
