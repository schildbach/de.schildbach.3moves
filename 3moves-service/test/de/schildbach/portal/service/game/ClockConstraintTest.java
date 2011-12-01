/*
 * Copyright 2001-2011 the original author or authors.
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package de.schildbach.portal.service.game;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import de.schildbach.portal.service.game.ClockConstraint;

/**
 * @author Andreas Schildbach
 */
public class ClockConstraintTest
{
	@Test
	public void test2h()
	{
		ClockConstraint c = new ClockConstraint("2h");
		long[] expected = { 2 * 60 * 60 * 1000l, 2 * 60 * 60 * 1000l, 2 * 60 * 60 * 1000l, 2 * 60 * 60 * 1000l, 2 * 60 * 60 * 1000l,
				2 * 60 * 60 * 1000l, 2 * 60 * 60 * 1000l, 2 * 60 * 60 * 1000l, 2 * 60 * 60 * 1000l, 2 * 60 * 60 * 1000l, 2 * 60 * 60 * 1000l,
				2 * 60 * 60 * 1000l, 2 * 60 * 60 * 1000l, 2 * 60 * 60 * 1000l, 2 * 60 * 60 * 1000l, 2 * 60 * 60 * 1000l, 2 * 60 * 60 * 1000l,
				2 * 60 * 60 * 1000l, 2 * 60 * 60 * 1000l, 2 * 60 * 60 * 1000l, 2 * 60 * 60 * 1000l, 2 * 60 * 60 * 1000l, 2 * 60 * 60 * 1000l,
				2 * 60 * 60 * 1000l, 2 * 60 * 60 * 1000l, 2 * 60 * 60 * 1000l, 2 * 60 * 60 * 1000l, 2 * 60 * 60 * 1000l, 2 * 60 * 60 * 1000l,
				2 * 60 * 60 * 1000l, 2 * 60 * 60 * 1000l, 2 * 60 * 60 * 1000l, 2 * 60 * 60 * 1000l, 2 * 60 * 60 * 1000l, 2 * 60 * 60 * 1000l,
				2 * 60 * 60 * 1000l };
		long clock = 0;
		for (int i = 0; i < expected.length; i++)
		{
			clock = c.incrementClock(i + 1, clock);
			assertEquals("turn " + (i + 1), expected[i], clock);
		}
	}

	@Test
	public void test5d1d()
	{
		ClockConstraint c = new ClockConstraint("5d+1d*");
		long[] expected = { 5 * 24 * 60 * 60 * 1000l, 6 * 24 * 60 * 60 * 1000l, 7 * 24 * 60 * 60 * 1000l, 8 * 24 * 60 * 60 * 1000l,
				9 * 24 * 60 * 60 * 1000l, 10 * 24 * 60 * 60 * 1000l, 11 * 24 * 60 * 60 * 1000l, 12 * 24 * 60 * 60 * 1000l, 13 * 24 * 60 * 60 * 1000l,
				14 * 24 * 60 * 60 * 1000l, 15 * 24 * 60 * 60 * 1000l, 16 * 24 * 60 * 60 * 1000l, 17 * 24 * 60 * 60 * 1000l,
				18 * 24 * 60 * 60 * 1000l, 19 * 24 * 60 * 60 * 1000l };
		long clock = 0;
		for (int i = 0; i < expected.length; i++)
		{
			clock = c.incrementClock(i + 1, clock);
			assertEquals("turn " + (i + 1), expected[i], clock);
		}
	}

	@Test
	public void test5d14d1d()
	{
		ClockConstraint c = new ClockConstraint("5d+1d*:16d");
		long[] expected = { 5 * 24 * 60 * 60 * 1000l, 6 * 24 * 60 * 60 * 1000l, 7 * 24 * 60 * 60 * 1000l, 8 * 24 * 60 * 60 * 1000l,
				9 * 24 * 60 * 60 * 1000l, 10 * 24 * 60 * 60 * 1000l, 11 * 24 * 60 * 60 * 1000l, 12 * 24 * 60 * 60 * 1000l, 13 * 24 * 60 * 60 * 1000l,
				14 * 24 * 60 * 60 * 1000l, 15 * 24 * 60 * 60 * 1000l, 16 * 24 * 60 * 60 * 1000l, 16 * 24 * 60 * 60 * 1000l,
				16 * 24 * 60 * 60 * 1000l, 16 * 24 * 60 * 60 * 1000l };
		long clock = 0;
		for (int i = 0; i < expected.length; i++)
		{
			clock = c.incrementClock(i + 1, clock);
			assertEquals("turn " + (i + 1), expected[i], clock);
		}
	}

	@Test
	public void test30d45d10()
	{
		ClockConstraint c = new ClockConstraint("30d/10*:30d");
		long[] expected = { 30 * 24 * 60 * 60 * 1000l, 30 * 24 * 60 * 60 * 1000l, 30 * 24 * 60 * 60 * 1000l, 30 * 24 * 60 * 60 * 1000l,
				30 * 24 * 60 * 60 * 1000l, 30 * 24 * 60 * 60 * 1000l, 30 * 24 * 60 * 60 * 1000l, 30 * 24 * 60 * 60 * 1000l,
				30 * 24 * 60 * 60 * 1000l, 30 * 24 * 60 * 60 * 1000l, 30 * 24 * 60 * 60 * 1000l, 30 * 24 * 60 * 60 * 1000l,
				30 * 24 * 60 * 60 * 1000l, 30 * 24 * 60 * 60 * 1000l, 30 * 24 * 60 * 60 * 1000l, 30 * 24 * 60 * 60 * 1000l,
				30 * 24 * 60 * 60 * 1000l, 30 * 24 * 60 * 60 * 1000l, 30 * 24 * 60 * 60 * 1000l, 30 * 24 * 60 * 60 * 1000l,
				30 * 24 * 60 * 60 * 1000l, 30 * 24 * 60 * 60 * 1000l, 30 * 24 * 60 * 60 * 1000l, 30 * 24 * 60 * 60 * 1000l,
				30 * 24 * 60 * 60 * 1000l, 30 * 24 * 60 * 60 * 1000l, 30 * 24 * 60 * 60 * 1000l, 30 * 24 * 60 * 60 * 1000l, 30 * 24 * 60 * 60 * 1000l };
		long clock = 0;
		for (int i = 0; i < expected.length; i++)
		{
			clock = c.incrementClock(i + 1, clock);
			assertEquals("turn " + (i + 1), expected[i], clock);
		}
	}

	@Test
	public void test30d10()
	{
		ClockConstraint c = new ClockConstraint("30d/10*");
		long[] expected = { 30 * 24 * 60 * 60 * 1000l, 30 * 24 * 60 * 60 * 1000l, 30 * 24 * 60 * 60 * 1000l, 30 * 24 * 60 * 60 * 1000l,
				30 * 24 * 60 * 60 * 1000l, 30 * 24 * 60 * 60 * 1000l, 30 * 24 * 60 * 60 * 1000l, 30 * 24 * 60 * 60 * 1000l,
				30 * 24 * 60 * 60 * 1000l, 30 * 24 * 60 * 60 * 1000l, 60 * 24 * 60 * 60 * 1000l, 60 * 24 * 60 * 60 * 1000l,
				60 * 24 * 60 * 60 * 1000l, 60 * 24 * 60 * 60 * 1000l, 60 * 24 * 60 * 60 * 1000l, 60 * 24 * 60 * 60 * 1000l,
				60 * 24 * 60 * 60 * 1000l, 60 * 24 * 60 * 60 * 1000l, 60 * 24 * 60 * 60 * 1000l, 60 * 24 * 60 * 60 * 1000l,
				90 * 24 * 60 * 60 * 1000l, 90 * 24 * 60 * 60 * 1000l, 90 * 24 * 60 * 60 * 1000l, 90 * 24 * 60 * 60 * 1000l,
				90 * 24 * 60 * 60 * 1000l, 90 * 24 * 60 * 60 * 1000l, 90 * 24 * 60 * 60 * 1000l, 90 * 24 * 60 * 60 * 1000l,
				90 * 24 * 60 * 60 * 1000l, 90 * 24 * 60 * 60 * 1000l };
		long clock = 0;
		for (int i = 0; i < expected.length; i++)
		{
			clock = c.incrementClock(i + 1, clock);
			assertEquals("turn " + (i + 1), expected[i], clock);
		}
	}
}
