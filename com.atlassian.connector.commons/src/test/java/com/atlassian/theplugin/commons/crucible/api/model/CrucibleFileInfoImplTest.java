/*******************************************************************************
 * Copyright (c) 2008 Atlassian and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Atlassian - initial API and implementation
 ******************************************************************************/

package com.atlassian.theplugin.commons.crucible.api.model;

import com.atlassian.theplugin.commons.crucible.api.model.Comment.ReadState;
import junit.framework.TestCase;

public class CrucibleFileInfoImplTest extends TestCase {

	private CrucibleFileInfoImpl prepareCrucibleFileInfo() {
		PermId permId1 = new PermId("1");
		CrucibleFileInfoImpl cfi = new CrucibleFileInfoImpl(null, null, permId1);
		VersionedCommentBean vc1 = new VersionedCommentBean();
		VersionedCommentBean rpl1 = new VersionedCommentBean();
		rpl1.setDraft(true);
		VersionedCommentBean rpl2 = new VersionedCommentBean();
		rpl2.setDraft(false);
		VersionedCommentBean rpl3 = new VersionedCommentBean();
		rpl3.setDraft(true);
		VersionedCommentBean rpl4 = new VersionedCommentBean();
		rpl4.setReadState(ReadState.UNREAD);
		rpl4.setDraft(true);
		vc1.addReply(rpl1);
		vc1.addReply(rpl2);
		rpl2.addReply(rpl3);
		rpl2.addReply(rpl4);
		cfi.addComment(vc1);
		VersionedCommentBean vc2 = new VersionedCommentBean();
		VersionedCommentBean rpl5 = new VersionedCommentBean();
		rpl5.setReadState(ReadState.UNREAD);
		vc2.addReply(rpl5);
		cfi.addComment(vc2);
		return cfi;
	}

	public void testGetNumberOfUnreadComments() {
		CrucibleFileInfoImpl cfi = prepareCrucibleFileInfo();
		assertEquals(2, cfi.getNumberOfUnreadComments());
	}

	public void testGetNumberOfComments() {
		CrucibleFileInfoImpl cfi = prepareCrucibleFileInfo();
		assertEquals(7, cfi.getNumberOfComments());
	}

	public void testGetNumberOfDraftComments() {
		CrucibleFileInfoImpl cfi = prepareCrucibleFileInfo();
		assertEquals(3, cfi.getNumberOfCommentsDrafts());
	}

}
