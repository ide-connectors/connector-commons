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

package com.atlassian.theplugin.commons.crucible.api.model;

public interface CustomFilter extends CrucibleFilter {
	public static final String AUTHOR = "author";
	public static final String CREATOR = "creator";
	public static final String MODERATOR = "moderator";
	public static final String REVIEWER = "reviewer";
	public static final String PROJECT = "projectKey";
	public static final String STATES = "states";
	public static final String COMPLETE = "complete";
	public static final String ORROLES = "orRoles";
	public static final String ALLCOMPLETE = "allReviewersComplete";

	String getTitle();

	String[] getState();

	String getAuthor();

	String getModerator();

	String getCreator();

	String getReviewer();

	Boolean isComplete();

	Boolean isAllReviewersComplete();

	String getProjectKey();

	Boolean isOrRoles();

	boolean isEnabled();

	String getServerUid();

	String getStates();
}
