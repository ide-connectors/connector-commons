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

/**
 *
 */
public enum CrucibleNotificationType {
	NEW_REVIEW,
	NOT_VISIBLE_REVIEW,
	NEW_REVIEW_ITEM,
	REMOVED_REVIEW_ITEM,
	NEW_GENERAL_COMMENT,
	NEW_VERSIONED_COMMENT,
	NEW_REPLY,
	UPDATED_GENERAL_COMMENT,
	UPDATED_VERSIONED_COMMENT,
	UPDATED_REPLY,
	REMOVED_GENERAL_COMMENT,
	REMOVED_VERSIONED_COMMENT,
	REMOVED_REPLY,
	REVIEWER_COMPLETED,
	REVIEW_COMPLETED,
	EXCEPTION_RAISED,
	REVIEW_STATE_CHANGED,
	REVIEW_DATA_CHANGED
}
