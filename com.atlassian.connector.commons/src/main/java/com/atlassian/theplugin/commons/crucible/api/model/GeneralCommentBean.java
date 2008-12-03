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

import java.util.ArrayList;
import java.util.List;

public class GeneralCommentBean extends CommentBean implements GeneralComment {
	private List<GeneralComment> replies = new ArrayList<GeneralComment>();
	private static final int HASH_INT = 31;

	public List<GeneralComment> getReplies() {
		return replies;
	}

	public void setReplies(List<GeneralComment> replies) {
		this.replies = replies;
	}

	public void addReply(GeneralComment comment) {
		replies.add(comment);
	}

	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (o == null || getClass() != o.getClass()) {
			return false;
		}
		if (!super.equals(o)) {
			return false;
		}

		GeneralCommentBean that = (GeneralCommentBean) o;

		if (replies != null ? !replies.equals(that.replies) : that.replies != null) {
			return false;
		}

		return true;
	}

	public int hashCode() {
		int result = super.hashCode();
		result = HASH_INT * result + (replies != null ? replies.hashCode() : 0);
		return result;
	}
}
