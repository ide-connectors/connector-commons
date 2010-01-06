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

import com.atlassian.theplugin.commons.exception.IncorrectVersionException;
import com.spartez.util.junit3.IAction;
import com.spartez.util.junit3.TestUtil;
import junit.framework.TestCase;

public class CrucibleVersionInfoTest extends TestCase {

	private CrucibleVersionInfoBean crucibleVersion;

	@Override
	protected void setUp() throws Exception {
		super.setUp();

		crucibleVersion = new CrucibleVersionInfoBean();
	}

	public void testIsVersionOrGreater() {
		try {
			crucibleVersion.setReleaseNumber("1.6");
			assertFalse(crucibleVersion.isVersion2OrGreater());
			assertFalse(crucibleVersion.isVersion21OrGreater());

			crucibleVersion.setReleaseNumber("1.6.6.1");
			assertFalse(crucibleVersion.isVersion2OrGreater());
			assertFalse(crucibleVersion.isVersion21OrGreater());

			crucibleVersion.setReleaseNumber("2.1");
			assertTrue(crucibleVersion.isVersion2OrGreater());
			assertTrue(crucibleVersion.isVersion21OrGreater());

			crucibleVersion.setReleaseNumber("2.1.0");
			assertTrue(crucibleVersion.isVersion2OrGreater());
			assertTrue(crucibleVersion.isVersion21OrGreater());

			crucibleVersion.setReleaseNumber("2.2");
			assertTrue(crucibleVersion.isVersion2OrGreater());
			assertTrue(crucibleVersion.isVersion21OrGreater());

			crucibleVersion.setReleaseNumber("2.2.0");
			assertTrue(crucibleVersion.isVersion2OrGreater());
			assertTrue(crucibleVersion.isVersion21OrGreater());

			crucibleVersion.setReleaseNumber("2.2.0.M1");
			assertTrue(crucibleVersion.isVersion2OrGreater());
			assertTrue(crucibleVersion.isVersion21OrGreater());

			crucibleVersion.setReleaseNumber("3.0");
			assertTrue(crucibleVersion.isVersion2OrGreater());
			assertTrue(crucibleVersion.isVersion21OrGreater());

			crucibleVersion.setReleaseNumber("3");
			assertTrue(crucibleVersion.isVersion2OrGreater());

		} catch (IncorrectVersionException e) {
			fail(e.getMessage());
		}

		crucibleVersion.setReleaseNumber("3");
		TestUtil.assertThrows(IncorrectVersionException.class, new IAction() {
			public void run() throws Throwable {
				crucibleVersion.isVersion21OrGreater();
			}
		});
	}

}
