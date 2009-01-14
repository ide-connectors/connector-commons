package com.atlassian.theplugin.commons.crucible.api.model.notification;

import com.atlassian.theplugin.commons.crucible.api.model.ReviewAdapter;
import com.atlassian.theplugin.commons.crucible.api.model.User;
import com.atlassian.theplugin.commons.crucible.api.model.VersionedComment;

public class RemovedVersionedCommentNotification extends AbstractCommentNotification {
	private final VersionedComment comment;

	public RemovedVersionedCommentNotification(final ReviewAdapter review, final VersionedComment comment, final User user,
			final boolean isDraft) {
		super(review, user, isDraft);
		this.comment = comment;
	}

	public CrucibleNotificationType getType() {
		return CrucibleNotificationType.REMOVED_VERSIONED_COMMENT;
	}

	public String getPresentationMessage() {
		return "Comment removed by " + comment.getAuthor().getDisplayName();
	}
}
