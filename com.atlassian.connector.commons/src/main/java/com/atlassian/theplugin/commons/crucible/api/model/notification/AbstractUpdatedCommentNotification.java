package com.atlassian.theplugin.commons.crucible.api.model.notification;

import com.atlassian.theplugin.commons.crucible.api.model.ReviewAdapter;
import com.atlassian.theplugin.commons.crucible.api.model.User;

public abstract class AbstractUpdatedCommentNotification extends AbstractCommentNotification {
	private final boolean wasDraft;

	public AbstractUpdatedCommentNotification(final ReviewAdapter review, final User user,
			final boolean isDraft, final boolean wasDraft) {
		super(review, user, isDraft);
		this.wasDraft = wasDraft;
	}

	public boolean wasDraft() {
		return wasDraft;
	}
}
