package com.atlassian.theplugin.commons.crucible.api.model;

import junit.framework.TestCase;
import static junitx.framework.Assert.assertNotEquals;

/**
 * Created by IntelliJ IDEA.
 * User: pniewiadomski
 * Date: 2009-07-24
 * Time: 09:26:45
 * To change this template use File | Settings | File Templates.
 */
public class ReviewerTest extends TestCase {

	public void testEquals() {
		Reviewer r1 = new Reviewer("u", "d", false);
		Reviewer r2 = new Reviewer("u", "d", true);
		Reviewer r3 = new Reviewer("d", "d", true);
		Reviewer r4 = new Reviewer("u", null, false);
		Reviewer r5 = new Reviewer("u", null, false);

		assertNotEquals(r1, r3);
		assertNotEquals(r1.hashCode(), r3.hashCode());

		assertNotEquals(r2, r3);
		assertNotEquals(r2.hashCode(), r3.hashCode());

		assertEquals(r1, r2);
		assertEquals(r2, r1);
		assertEquals(r1.hashCode(), r2.hashCode());

		assertNotEquals(r1, r4);

		assertEquals(r4, r5);
		assertEquals(r4.hashCode(), r5.hashCode());
	}

}
