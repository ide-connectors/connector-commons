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

import java.util.List;

public class GeneralCommentBean extends CommentBean implements GeneralComment {
	private static final int HASH_INT = 31;

	public GeneralCommentBean() {
		super();
	}

	public GeneralCommentBean(final GeneralComment bean) {
		super(bean);
	}

	@Override
	protected Comment createReplyBean(Comment reply) {
		return new GeneralCommentBean((GeneralComment) reply);
	}

	@Override
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

		if (getReplies() != null ? !getReplies().equals(that.getReplies()) : that.getReplies() != null) {
			return false;
		}

		return true;
	}

	@Override
	public int hashCode() {
		int result = super.hashCode();
		result = HASH_INT * result + (getReplies() != null ? getReplies().hashCode() : 0);
		return result;
	}

	@SuppressWarnings("unchecked")
	@Deprecated
	public List<GeneralComment> getReplies2() {
		// wseliga: I don't know how to make it compilable with these casts.
		// We are somewhat guaranteed that all replies will be here really of VersionedComment type, so I dare cast
		//noinspection RedundantCast
		return (List<GeneralComment>) (List<?>) getReplies();
	}

}
