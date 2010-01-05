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
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * User: lguminski
 * Date: Jul 16, 2008
 * Time: 11:42:30 PM
 */
public abstract class CommentBean implements Comment {
	private PermId permId;
	private String message = null;
	private boolean draft = false;
	private boolean deleted = false;
	private boolean defectRaised = false;
	private boolean defectApproved = false;
	private User author = null;
	private Date createDate = new Date();
    private ReadState readState;

    private List<Comment> replies = new ArrayList<Comment>();

	private boolean isReply = false;

	private final Map<String, CustomField> customFields;
	private static final int HASH_INT = 31;

	public CommentBean() {
		super();
		customFields = new HashMap<String, CustomField>();
	}

	public CommentBean(Comment bean) {
		this();
		setPermId(bean.getPermId());
		setMessage(bean.getMessage());
		setDraft(bean.isDraft());
		setCreateDate(bean.getCreateDate());
		setDefectApproved(bean.isDefectApproved());
		setDefectRaised(bean.isDefectRaised());
		setDeleted(bean.isDeleted());
		setAuthor(bean.getAuthor());
		setAuthor(bean.getAuthor());
		setReply(bean.isReply());
        setReadState(bean.getReadState());

		if (bean.getCustomFields() != null) {
			for (Map.Entry<String, CustomField> entry : bean.getCustomFields().entrySet()) {
				getCustomFields().put(entry.getKey(), new CustomFieldBean(entry.getValue()));
			}
		}

        if (bean.getReplies() != null) {
            for (Comment reply : bean.getReplies()) {
                replies.add(createReplyBean(reply));
            }
        }
	}

    protected abstract Comment createReplyBean(Comment reply);

    public PermId getPermId() {
		return permId;
	}

	public void setPermId(PermId permId) {
		this.permId = permId;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public boolean isDraft() {
		return draft;
	}

	public void setDraft(boolean draft) {
		this.draft = draft;
	}

	public boolean isDeleted() {
		return deleted;
	}

	public void setDeleted(boolean deleted) {
		this.deleted = deleted;
	}

	public boolean isDefectRaised() {
		return defectRaised;
	}

	public void setDefectRaised(boolean defectRaised) {
		this.defectRaised = defectRaised;
	}

	public boolean isDefectApproved() {
		return defectApproved;
	}

	public boolean isReply() {
		return isReply;
	}

	public void setReply(boolean reply) {
		isReply = reply;
	}

    public void setReplies(List<Comment> replies) {
        this.replies = replies;
    }

    public void addReply(Comment comment) {
        replies.add(comment);
    }

    public List<Comment> getReplies() {
        return replies;
    }

	public void setDefectApproved(boolean defectApproved) {
		this.defectApproved = defectApproved;
	}

	public User getAuthor() {
		return author;
	}

	public void setAuthor(User author) {
		this.author = author;
	}

	public Date getCreateDate() {
		return new Date(createDate.getTime());
	}

	public void setCreateDate(Date createDate) {
		if (createDate != null) {
			this.createDate = new Date(createDate.getTime());
		}
	}

	public Map<String, CustomField> getCustomFields() {
		return customFields;
	}

    public ReadState getReadState() {
        return readState;
    }

    public void setReadState(ReadState readState) {
        this.readState = readState;
    }

    @Override
	public String toString() {
		return getMessage();
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (o == null || getClass() != o.getClass()) {
			return false;
		}

		CommentBean that = (CommentBean) o;

		if (defectApproved != that.defectApproved) {
			return false;
		}
		if (defectRaised != that.defectRaised) {
			return false;
		}
		if (deleted != that.deleted) {
			return false;
		}
		if (draft != that.draft) {
			return false;
		}
		if (isReply != that.isReply) {
			return false;
		}
		if (author != null ? !author.equals(that.author) : that.author != null) {
			return false;
		}
		if (createDate != null ? !createDate.equals(that.createDate) : that.createDate != null) {
			return false;
		}
		if (customFields != null ? !customFields.equals(that.customFields) : that.customFields != null) {
			return false;
		}
		if (message != null ? !message.equals(that.message) : that.message != null) {
			return false;
		}
		if (permId != null ? !permId.equals(that.permId) : that.permId != null) {
			return false;
		}
        if (readState != null ? !readState.equals(that.readState) : that.readState != null) {
            return false;
        }

		return true;
	}

	@Override
	public int hashCode() {
		int result;
		result = (permId != null ? permId.hashCode() : 0);
		result = HASH_INT * result + (message != null ? message.hashCode() : 0);
		result = HASH_INT * result + (draft ? 1 : 0);
		result = HASH_INT * result + (deleted ? 1 : 0);
		result = HASH_INT * result + (defectRaised ? 1 : 0);
		result = HASH_INT * result + (defectApproved ? 1 : 0);
		result = HASH_INT * result + (author != null ? author.hashCode() : 0);
		result = HASH_INT * result + (createDate != null ? createDate.hashCode() : 0);
		result = HASH_INT * result + (isReply ? 1 : 0);
		result = HASH_INT * result + (customFields != null ? customFields.hashCode() : 0);
		result = HASH_INT * result + (readState != null ? readState.ordinal() : 0);
		return result;
	}

	public int getNumReplies() {
		if (replies == null) {
			return 0;
		}
		int res = replies.size();
		for (Comment reply : replies) {
			res += reply.getNumReplies();
		}
		return res;
	}

	public int getNumberOfUnreadReplies() {
		if (replies == null) {
			return 0;
		}

		int counter = 0;
		for (Comment reply : replies) {
			if (reply.isEffectivelyUnread()) {
				++counter;
			}
			counter += reply.getNumberOfUnreadReplies();
		}
		return counter;
	}

	public int getNumberOfDraftReplies() {
		if (replies == null) {
			return 0;
		}

		int counter = 0;
		for (Comment reply : replies) {
			if (reply.isDraft()) {
				++counter;
			}
			counter += reply.getNumberOfDraftReplies();
		}
		return counter;
	}

	public boolean isEffectivelyUnread() {
		return (getReadState() == Comment.ReadState.UNREAD || getReadState() == Comment.ReadState.LEAVE_UNREAD);
	}

}
