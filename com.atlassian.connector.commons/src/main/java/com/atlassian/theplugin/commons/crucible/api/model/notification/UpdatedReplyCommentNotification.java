package com.atlassian.theplugin.commons.crucible.api.model.notification;

import com.atlassian.theplugin.commons.crucible.api.model.Comment;
import com.atlassian.theplugin.commons.crucible.api.model.Review;
import com.atlassian.theplugin.commons.crucible.api.model.User;

public class UpdatedReplyCommentNotification extends AbstractUpdatedCommentNotification {
	private final Comment reply;
	private final boolean wasDraft;

	public UpdatedReplyCommentNotification(final Review review, final Comment reply,
			final User user, final boolean isDraft, final boolean wasDraft) {
		super(review, user, isDraft, wasDraft);
		this.reply = reply;
		this.wasDraft = wasDraft;
	}

	@Override
	public CrucibleNotificationType getType() {
		return CrucibleNotificationType.UPDATED_REPLY;
	}

	@Override
	public String getPresentationMessage() {
		return "Reply updated by " + reply.getAuthor().getDisplayName();
	}

	public boolean isWasDraft() {
		return wasDraft;
	}
}
