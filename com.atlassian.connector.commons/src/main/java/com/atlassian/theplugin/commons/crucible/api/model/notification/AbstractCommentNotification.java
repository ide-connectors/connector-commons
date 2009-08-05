package com.atlassian.theplugin.commons.crucible.api.model.notification;

import com.atlassian.theplugin.commons.crucible.api.model.Review;
import com.atlassian.theplugin.commons.crucible.api.model.User;

public abstract class AbstractCommentNotification extends AbstractReviewNotification {
	private final User author;
	private final boolean draft;

	public AbstractCommentNotification(final Review review, final User author, final boolean isDraft) {
		super(review);

		this.author = author;
		this.draft = isDraft;
	}

	public User getAuthor() {
		return author;
	}

	public boolean isDraft() {
		return draft;
	}
}
