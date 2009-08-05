package com.atlassian.theplugin.commons.crucible.api.model.notification;

import com.atlassian.theplugin.commons.crucible.api.model.Review;
import com.atlassian.theplugin.commons.crucible.api.model.User;

public class UpdatedVersionedCommentNotification extends AbstractUpdatedCommentNotification {

	private final boolean wasDraft;

	public UpdatedVersionedCommentNotification(final Review review, final User user,
			final boolean isDraft, final boolean wasDraft) {
		super(review, user, isDraft, wasDraft);
		this.wasDraft = wasDraft;
	}

	@Override
	public CrucibleNotificationType getType() {
		return CrucibleNotificationType.UPDATED_VERSIONED_COMMENT;
	}

	@Override
	public String getPresentationMessage() {
		return "Comment updated by " + getAuthor().getDisplayName();
	}

	public boolean isWasDraft() {
		return wasDraft;
	}
}
