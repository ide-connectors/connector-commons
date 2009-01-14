package com.atlassian.theplugin.commons.crucible.api.model.notification;

import com.atlassian.theplugin.commons.crucible.api.model.ReviewAdapter;
import com.atlassian.theplugin.commons.crucible.api.model.User;

public class UpdatedVersionedCommentNotification extends AbstractUpdatedCommentNotification {

	private final boolean wasDraft;

	public UpdatedVersionedCommentNotification(final ReviewAdapter review, final User user,
			final boolean isDraft, final boolean wasDraft) {
		super(review, user, isDraft, wasDraft);
		this.wasDraft = wasDraft;
	}

	public CrucibleNotificationType getType() {
		return CrucibleNotificationType.UPDATED_VERSIONED_COMMENT;
	}

	public String getPresentationMessage() {
		return "Comment updated by " + getAuthor().getDisplayName();
	}

	public boolean isWasDraft() {
		return wasDraft;
	}
}
