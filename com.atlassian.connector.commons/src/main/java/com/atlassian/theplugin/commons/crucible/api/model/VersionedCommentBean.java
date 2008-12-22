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

public class VersionedCommentBean extends CommentBean implements VersionedComment {
	private PermId reviewItemId;
	private int fromStartLine = 0;
	private int fromEndLine = 0;
	private boolean fromLineInfo = false;
	private int toStartLine = 0;
	private int toEndLine = 0;
	private boolean toLineInfo = false;
	private List<VersionedComment> replies = new ArrayList<VersionedComment>();

	public VersionedCommentBean(VersionedComment bean) {
		super(bean);
		if (bean.isFromLineInfo()) {
			setFromLineInfo(true);
			setFromStartLine(bean.getFromStartLine());
			setFromEndLine(bean.getFromEndLine());
		}
		if (bean.isToLineInfo()) {
			setToLineInfo(true);
			setToStartLine(bean.getToStartLine());
			setToEndLine(bean.getToEndLine());
		}
		setReplies(bean.getReplies());
	}

	public VersionedCommentBean() {
		super();
	}



	public PermId getReviewItemId() {
		return reviewItemId;
	}

	public void setReviewItemId(PermId reviewItemId) {
		this.reviewItemId = reviewItemId;
	}

	public int getFromStartLine() {
		return fromStartLine;
	}

	public void setFromStartLine(int startLine) {
		this.fromStartLine = startLine;
	}

	public int getFromEndLine() {
		return fromEndLine;
	}

	public List<VersionedComment> getReplies() {
		return replies;
	}

	public void setReplies(List<VersionedComment> replies) {
		this.replies = replies;
	}

	public void addReply(VersionedComment comment) {
		replies.add(comment);
	}

	public void setFromEndLine(int endLine) {
		this.fromEndLine = endLine;
	}

	public int getToStartLine() {
		return toStartLine;
	}

	public void setToStartLine(int startLine) {
		this.toStartLine = startLine;
	}

	public int getToEndLine() {
		return toEndLine;
	}

	public void setToEndLine(int endLine) {
		this.toEndLine = endLine;
	}

	public boolean isFromLineInfo() {
		return fromLineInfo;
	}

	public void setFromLineInfo(boolean fromLineInfo) {
		this.fromLineInfo = fromLineInfo;
	}

	public boolean isToLineInfo() {
		return toLineInfo;
	}

	public void setToLineInfo(boolean toLineInfo) {
		this.toLineInfo = toLineInfo;
	}

/*
	public boolean isReviewChanged(Comment other) {
		return !deepEquals(other);

	}

	public boolean  deepEquals(Object o) {
		if (this == o) {
			return true;
		}
		if (o == null || getClass() != o.getClass()) {
			return false;
		}
		if (!super.equals(o)) {
			return false;
		}

		VersionedCommentBean that = (VersionedCommentBean) o;

		if (fromEndLine != that.fromEndLine) {
			return false;
		}
		if (fromLineInfo != that.fromLineInfo) {
			return false;
		}
		if (fromStartLine != that.fromStartLine) {
			return false;
		}
		if (toEndLine != that.toEndLine) {
			return false;
		}
		if (toLineInfo != that.toLineInfo) {
			return false;
		}
		if (toStartLine != that.toStartLine) {
			return false;
		}
		if (!replies.equals(that.replies)) {
			return false;
		}
		if (!reviewItemId.equals(that.reviewItemId)) {
			return false;
		}

		return true;
	}*/

}