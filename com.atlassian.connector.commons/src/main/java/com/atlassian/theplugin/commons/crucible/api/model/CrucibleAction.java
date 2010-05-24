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

import com.atlassian.theplugin.commons.util.MiscUtil;
import java.util.Map;

public final class CrucibleAction {
	private static Map<String, CrucibleAction> nameMap = MiscUtil.buildHashMap();

	public static final CrucibleAction VIEW = new CrucibleAction("View review", "action:viewReview");
	public static final CrucibleAction CREATE = new CrucibleAction("Create review", "action:createReview");
	public static final CrucibleAction ABANDON = new CrucibleAction("Abandon review", "action:abandonReview");
	public static final CrucibleAction SUBMIT = new CrucibleAction("Submit review", "action:submitReview");
	public static final CrucibleAction APPROVE = new CrucibleAction("Approve review", "action:approveReview");
	public static final CrucibleAction REJECT = new CrucibleAction("Reject review", "action:rejectReview");
	public static final CrucibleAction SUMMARIZE = new CrucibleAction("Summarize review", "action:summarizeReview");
	public static final CrucibleAction CLOSE = new CrucibleAction("Close review", "action:closeReview");
	public static final CrucibleAction REOPEN = new CrucibleAction("Reopen review", "action:reopenReview");
	public static final CrucibleAction RECOVER = new CrucibleAction("Recover review", "action:recoverReview");
	public static final CrucibleAction COMPLETE = new CrucibleAction("Complete review", "action:completeReview");
	public static final CrucibleAction UNCOMPLETE = new CrucibleAction("Uncomplete review", "action:uncompleteReview");
	public static final CrucibleAction COMMENT = new CrucibleAction("Comment", "action:commentOnReview");
	public static final CrucibleAction MODIFY_FILES = new CrucibleAction("Modify files", "action:modifyReviewFiles");
	public static final CrucibleAction DELETE = new CrucibleAction("Delete review", "action:deleteReview");
	public static final CrucibleAction REOPEN_SNIPPET = new CrucibleAction("Reopen snippet", "action:reopenSnippet");
	public static final CrucibleAction CLOSE_SNIPPET = new CrucibleAction("Close snippet", "action:closeSnippet");
	public static final CrucibleAction CREATE_SNIPPET = new CrucibleAction("Create snippet", "action:createSnippet");

    private final String displayName;

    private final String actionName;

    CrucibleAction(String dName, String aName) {
        displayName = dName;
        actionName = aName;
		nameMap.put(aName, this);
    }

	public String displayName() {
        return displayName;
    }

    public String actionName() {
        return actionName;
    }

    public static CrucibleAction fromValue(String v) {
		final CrucibleAction crucibleAction = nameMap.get(v);
		if (crucibleAction != null) {
			return crucibleAction;
		}
        throw new IllegalArgumentException(v);
    }

}