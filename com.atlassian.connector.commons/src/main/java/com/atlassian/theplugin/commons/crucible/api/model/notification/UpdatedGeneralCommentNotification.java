package com.atlassian.theplugin.commons.crucible.api.model.notification;

import com.atlassian.theplugin.commons.crucible.api.model.ReviewAdapter;
import com.atlassian.theplugin.commons.crucible.api.model.User;

public class UpdatedGeneralCommentNotification extends AbstractUpdatedCommentNotification {
	public UpdatedGeneralCommentNotification(final ReviewAdapter review, final User user,
			final boolean isDraft, final boolean wasDraft) {
		super(review, user, isDraft, wasDraft);
	}

	public CrucibleNotificationType getType() {
		return CrucibleNotificationType.UPDATED_GENERAL_COMMENT;
	}

	public String getPresentationMessage() {
		return "General comment updated by " + getAuthor().getDisplayName();
	}
}
