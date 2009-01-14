package com.atlassian.theplugin.commons.crucible.api.model.notification;

import com.atlassian.theplugin.commons.crucible.api.model.Comment;
import com.atlassian.theplugin.commons.crucible.api.model.ReviewAdapter;
import com.atlassian.theplugin.commons.crucible.api.model.User;

public class RemovedReplyCommentNotification extends AbstractCommentNotification {
	private final Comment reply;

	public RemovedReplyCommentNotification(final ReviewAdapter review, final Comment reply, final User user,
			final boolean isDraft) {
		super(review, user, isDraft);
		this.reply = reply;
	}

	public CrucibleNotificationType getType() {
		return CrucibleNotificationType.REMOVED_REPLY;
	}

	public String getPresentationMessage() {
		return "Reply removed by " + reply.getAuthor().getDisplayName();
	}
}
