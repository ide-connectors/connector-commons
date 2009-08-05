package com.atlassian.theplugin.commons.crucible.api.model.notification;

import com.atlassian.theplugin.commons.crucible.api.model.GeneralComment;
import com.atlassian.theplugin.commons.crucible.api.model.Review;
import com.atlassian.theplugin.commons.crucible.api.model.User;

public class RemovedGeneralCommentNotification extends AbstractCommentNotification {
	private final GeneralComment comment;

	public RemovedGeneralCommentNotification(final Review review, final GeneralComment comment, final User user,
			final boolean isDraft) {
		super(review, user, isDraft);
		this.comment = comment;
	}

	@Override
	public CrucibleNotificationType getType() {
		return CrucibleNotificationType.REMOVED_GENERAL_COMMENT;
	}

	@Override
	public String getPresentationMessage() {
		return "General comment removed by " + comment.getAuthor().getDisplayName();
	}
}
