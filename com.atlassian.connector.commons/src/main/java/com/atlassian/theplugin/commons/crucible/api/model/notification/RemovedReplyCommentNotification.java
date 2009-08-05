package com.atlassian.theplugin.commons.crucible.api.model.notification;

import com.atlassian.theplugin.commons.crucible.api.model.Comment;
import com.atlassian.theplugin.commons.crucible.api.model.Review;
import com.atlassian.theplugin.commons.crucible.api.model.User;

public class RemovedReplyCommentNotification extends AbstractCommentNotification {
	private final Comment reply;

	public RemovedReplyCommentNotification(final Review review, final Comment reply, final User user,
			final boolean isDraft) {
		super(review, user, isDraft);
		this.reply = reply;
	}

	@Override
	public CrucibleNotificationType getType() {
		return CrucibleNotificationType.REMOVED_REPLY;
	}

	@Override
	public String getPresentationMessage() {
		return "Reply removed by " + reply.getAuthor().getDisplayName();
	}
}
