/**
 * Copyright (C) 2008 Atlassian
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.atlassian.theplugin.commons.crucible.api.model.notification;

import com.atlassian.theplugin.commons.crucible.api.model.GeneralComment;
import com.atlassian.theplugin.commons.crucible.api.model.Review;
import com.atlassian.theplugin.commons.crucible.api.model.User;

public class NewGeneralCommentNotification extends AbstractCommentNotification {
	private final GeneralComment comment;

	public NewGeneralCommentNotification(final Review review, final GeneralComment comment, final User user,
			final boolean isDraft) {
		super(review, user, isDraft);
		this.comment = comment;
	}

	@Override
	public CrucibleNotificationType getType() {
		return CrucibleNotificationType.NEW_GENERAL_COMMENT;
	}

	@Override
	public String getPresentationMessage() {
		return "New general comment added by " + comment.getAuthor().getDisplayName();
	}
}