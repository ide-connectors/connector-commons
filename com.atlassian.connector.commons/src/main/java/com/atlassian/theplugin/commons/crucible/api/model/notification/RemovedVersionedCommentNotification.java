package com.atlassian.theplugin.commons.crucible.api.model.notification;

import com.atlassian.theplugin.commons.crucible.api.model.Review;
import com.atlassian.theplugin.commons.crucible.api.model.User;
import com.atlassian.theplugin.commons.crucible.api.model.VersionedComment;

public class RemovedVersionedCommentNotification extends AbstractCommentNotification {
	private final VersionedComment comment;

	public RemovedVersionedCommentNotification(final Review review, final VersionedComment comment, final User user,
			final boolean isDraft) {
		super(review, user, isDraft);
		this.comment = comment;
	}

	@Override
	public CrucibleNotificationType getType() {
		return CrucibleNotificationType.REMOVED_VERSIONED_COMMENT;
	}

	@Override
	public String getPresentationMessage() {
		return "Comment removed by " + comment.getAuthor().getDisplayName();
	}
}
