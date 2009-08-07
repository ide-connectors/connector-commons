package com.atlassian.theplugin.commons.crucible.api.model.notification;

import com.atlassian.theplugin.commons.crucible.api.model.Review;
import com.atlassian.theplugin.commons.crucible.api.model.User;
import com.atlassian.theplugin.commons.crucible.api.model.Comment;

/**
 * User: kalamon
 * Date: Aug 7, 2009
 * Time: 11:24:31 AM
 */
public class CommentReadUnreadStateChangedNotification extends AbstractCommentNotification {
    private Comment.ReadState state;

    public CommentReadUnreadStateChangedNotification(final Review review,
                                                     final Comment.ReadState state, 
                                                     final User author, final boolean isDraft) {
        super(review, author, isDraft);
        this.state = state;
    }

    public CrucibleNotificationType getType() {
        return CrucibleNotificationType.COMMENT_READ_UNREAD_STATE_CHANGED;
    }

    public String getPresentationMessage() {
        return "Comment state changed to " + state;
    }
}
