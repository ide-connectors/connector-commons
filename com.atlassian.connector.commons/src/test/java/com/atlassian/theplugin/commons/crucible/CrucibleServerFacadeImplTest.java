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
package com.atlassian.theplugin.commons.crucible;

import com.atlassian.theplugin.commons.ServerType;
import com.atlassian.theplugin.commons.cfg.ServerCfg;
import com.atlassian.theplugin.commons.cfg.ServerIdImpl;
import com.atlassian.theplugin.commons.crucible.api.CrucibleSession;
import com.atlassian.theplugin.commons.crucible.api.model.*;
import com.atlassian.theplugin.commons.exception.ServerPasswordNotProvidedException;
import com.atlassian.theplugin.commons.remoteapi.RemoteApiException;
import com.atlassian.theplugin.commons.remoteapi.ServerData;
import com.atlassian.theplugin.commons.util.MiscUtil;
import junit.framework.TestCase;
import org.easymock.EasyMock;

import java.util.ArrayList;

public class CrucibleServerFacadeImplTest extends TestCase {

	private static final ServerData SERVER_DATA = new ServerData(new ServerCfg(true, "crucible", new ServerIdImpl()) {
		public ServerType getServerType() {
			return null;
		}

		public ServerCfg getClone() {
			return null;
		}
	}, "", "");

	public void testSetReviewers() throws RemoteApiException, ServerPasswordNotProvidedException {
		final CrucibleSession mock = EasyMock.createNiceMock(CrucibleSession.class);
		final CrucibleServerFacadeImpl crucibleServerFacade = new CrucibleServerFacadeImpl(null) {
			@Override
			protected CrucibleSession getSession(final ServerData server)
					throws RemoteApiException, ServerPasswordNotProvidedException {
				return mock;
			}
		};
		Review review = new Review(SERVER_DATA.getUrl());
		review.setPermId(new PermId("CR-123"));
		review.setReviewers(MiscUtil.<Reviewer>buildHashSet());
		final ArrayList<String> newReviewers = MiscUtil.buildArrayList("wseliga", "mwent");
		EasyMock.expect(mock.getReview(review.getPermId())).andReturn(review);
		mock.addReviewers(review.getPermId(), MiscUtil.buildHashSet(newReviewers));

		EasyMock.replay(mock);
		crucibleServerFacade.setReviewers(SERVER_DATA, review.getPermId(), newReviewers);
		EasyMock.verify(mock);

		EasyMock.reset(mock);
		review.setReviewers(MiscUtil.<Reviewer>buildHashSet(
				new Reviewer("wseliga", true), new Reviewer("jgorycki", false), new Reviewer("sginter", true)));
		final ArrayList<String> newReviewers2 = MiscUtil.buildArrayList("jgorycki", "mwent", "pmaruszak");
		EasyMock.expect(mock.getReview(review.getPermId())).andReturn(review);
		mock.addReviewers(review.getPermId(), MiscUtil.buildHashSet("mwent", "pmaruszak"));
		EasyMock.expectLastCall().once();
		mock.removeReviewer(review.getPermId(), "sginter");
		EasyMock.expectLastCall().once();
		mock.removeReviewer(review.getPermId(), "wseliga");
		EasyMock.expectLastCall().once();
		EasyMock.replay(mock);
		crucibleServerFacade.setReviewers(SERVER_DATA, review.getPermId(), newReviewers2);
		EasyMock.verify(mock);
	}
}
