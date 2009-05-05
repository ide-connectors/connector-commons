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

import com.atlassian.connector.commons.misc.IntRanges;

import java.util.List;

import org.jetbrains.annotations.Nullable;

public interface VersionedComment extends Comment {
	PermId getPermId();

	PermId getReviewItemId();

	boolean isToLineInfo();

	int getToStartLine();

	int getToEndLine();

	boolean isFromLineInfo();	

	int getFromStartLine();

	int getFromEndLine();

	/**
	 * @return precise information about lines this comment references in "from" revision 
	 */
	@Nullable
	IntRanges getFromLineRanges();

	/**
	 * @return precise information about lines this comment references in "to" revision
	 */
	@Nullable
	IntRanges getToLineRanges();
}