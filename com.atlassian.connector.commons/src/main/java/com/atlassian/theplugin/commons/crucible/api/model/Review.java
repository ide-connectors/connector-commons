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

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class Review extends BasicReview {
	private Set<CrucibleFileInfo> files;

	public Review(@NotNull String serverUrl, @NotNull String projectKey, @NotNull User author, @Nullable User moderator) {
		super(serverUrl, projectKey, author, moderator);
	}

	public void removeVersionedComment(final VersionedComment comment, final CrucibleFileInfo file) {

		CrucibleFileInfo f = getFileByPermId(file.getPermId());

		if (!comment.isReply()) {
			if (f != null) {
				f.getVersionedComments().remove(comment);
			}
		} else {
			if (f != null) {
				for (VersionedComment versionedComment : f.getVersionedComments()) {
					if (versionedComment.getReplies().remove(comment)) {
						return;
					}
				}
			}
		}
	}


	public void setFilesAndVersionedComments(final Collection<CrucibleFileInfo> aFiles, List<VersionedComment> commentList) {
		this.files = aFiles != null ? new HashSet<CrucibleFileInfo>(aFiles) : null;

		if (files != null && commentList != null) {
			for (VersionedComment comment : commentList) {
				comment.getCrucibleFileInfo().addComment(comment);
			}
		}
	}


	@Nullable
	public CrucibleFileInfo getFileByPermId(PermId id) {
		for (CrucibleFileInfo f : getFiles()) {
			if (f.getPermId().equals(id)) {
				return f;
			}
		}
		return null;
	}

	public Set<CrucibleFileInfo> getFiles() {
		return files;
	}

	public void setFiles(final Set<CrucibleFileInfo> files) {
		this.files = files;
	}

	/**
	 * @return total number of versioned comments including replies (for all files)
	 */
	@Override
	public int getNumberOfVersionedComments() {
		int num = 0;
		for (CrucibleFileInfo file : getFiles()) {
			num += file.getNumberOfComments();
		}
		return num;
	}

	@Override
	public int getNumberOfVersionedComments(final String userName) {
		int num = 0;
		for (CrucibleFileInfo file : getFiles()) {
			num += file.getNumberOfComments(userName);
		}
		return num;
	}


	@Override
	public int getNumberOfUnreadComments() {
        List<Comment> allComments = new ArrayList<Comment>();
        allComments.addAll(getGeneralComments());
        for (CrucibleFileInfo file : getFiles()) {
            allComments.addAll(file.getVersionedComments());
        }
        int result = 0;

        for (Comment comment : allComments) {
            if (comment.getReadState() == Comment.ReadState.UNREAD
                    || comment.getReadState() == Comment.ReadState.LEAVE_UNREAD) {
                ++result;
            }
            for (Comment reply : comment.getReplies()) {
                if (reply.getReadState() == Comment.ReadState.UNREAD
                        || reply.getReadState() == Comment.ReadState.LEAVE_UNREAD) {
                    ++result;
                }
            }
        }
        return result;
    }

	/*
	 * @Override public int getNumberOfVersionedCommentsDefects() { int num = 0; for (CrucibleFileInfo file : getFiles()) { num
	 * += file.getNumberOfCommentsDefects(); } return num; }
	 *
	 * @Override public int getNumberOfVersionedCommentsDefects(final String userName) { int num = 0; for (CrucibleFileInfo file
	 * : getFiles()) { num += file.getNumberOfCommentsDefects(userName); } return num; }
	 *
	 * @Override public int getNumberOfVersionedCommentsDrafts() { int num = 0; for (CrucibleFileInfo file : getFiles()) { num
	 * += file.getNumberOfCommentsDrafts(); } return num; }
	 *
	 * @Override public int getNumberOfVersionedCommentsDrafts(final String userName) { int num = 0; for (CrucibleFileInfo file
	 * : getFiles()) { num += file.getNumberOfCommentsDrafts(userName); } return num; }
	 */
}