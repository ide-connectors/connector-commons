package com.atlassian.theplugin.commons.crucible.api.model;

import static junitx.framework.Assert.*;
import junit.framework.TestCase;

/**
 * Created by IntelliJ IDEA.
 * User: pniewiadomski
 * Date: 2009-07-23
 * Time: 16:28:48
 * To change this template use File | Settings | File Templates.
 */
public class CrucibleProjectTest extends TestCase {

	public void testEquals() {
		CrucibleProject p1 = new CrucibleProject(null, null, null);
		CrucibleProject p2 = new CrucibleProject("1", "2", "3");
		CrucibleProject p3 = new CrucibleProject("2", "X", "3");
		CrucibleProject p4 = new CrucibleProject("2", "X", "3");
		CrucibleProject p5 = new CrucibleProject("1", "2", "X");

		assertFalse(p1.equals(null));
		assertTrue(p1.equals(p1));

		assertFalse(p1.equals(p2));
		assertNotEquals(p1.hashCode(), p2.hashCode());

		assertFalse(p2.equals(p1));
		assertNotEquals(p2.hashCode(), p1.hashCode());
		assertFalse(p1.equals(p3));
		assertNotEquals(p1.hashCode(), p2.hashCode());
		assertFalse(p5.equals(p1));
		assertNotEquals(p5.hashCode(), p1.hashCode());

		assertTrue(p4.equals(p3));
		assertEquals(p4.hashCode(), p3.hashCode());
		assertTrue(p3.equals(p4));
		assertEquals(p3.hashCode(), p4.hashCode());
	}
}
