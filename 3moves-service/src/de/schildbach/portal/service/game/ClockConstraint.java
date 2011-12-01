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

import java.io.Serializable;
import java.util.LinkedList;
import java.util.List;
import java.util.StringTokenizer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import de.schildbach.game.exception.ParseException;

/**
 * @author Andreas Schildbach
 */
public class ClockConstraint implements Serializable
{
	private String id;
	private int[] turns;
	private long[] increments;
	private long maximum;
	private boolean infinite = false;

	private static Pattern globalPattern = Pattern.compile("([^\\*]+)(\\*)?(?:\\:(\\w+))?");
	private static Pattern pattern = Pattern.compile("(\\w+)(?:/(\\d+))?");

	public ClockConstraint(String id) throws ParseException
	{
		this.id = id;

		Matcher matcher = globalPattern.matcher(id);
		matcher.matches();

		if (matcher.group(2) != null)
			infinite = true;

		if (matcher.group(3) != null)
			maximum = toMillis(matcher.group(3));

		StringTokenizer t = new StringTokenizer(matcher.group(1), "+");

		int numTokens = t.countTokens();

		turns = new int[numTokens];
		increments = new long[numTokens];

		for (int i = 0; i < numTokens; i++)
		{
			String element = t.nextToken();
			matcher = pattern.matcher(element);
			if (!matcher.matches())
				throw new ParseException(id, "can't parse");

			increments[i] = toMillis(matcher.group(1));
			turns[i] = matcher.group(2) != null ? Integer.parseInt(matcher.group(2)) : 1;
		}
	}

	private long toMillis(String text)
	{
		long unit;
		switch (text.charAt(text.length() - 1))
		{
			case 's':
				unit = 1000;
				break;
			case 'm':
				unit = 1000 * 60;
				break;
			case 'h':
				unit = 1000 * 60 * 60;
				break;
			case 'd':
				unit = 1000 * 60 * 60 * 24;
				break;
			default:
				throw new IllegalArgumentException(text);
		}
		text = text.substring(0, text.length() - 1);
		int value = Integer.parseInt(text);
		return value * unit;
	}

	@Override
	public boolean equals(Object obj)
	{
		return ((ClockConstraint) obj).id.equals(this.id);
	}

	@Override
	public int hashCode()
	{
		return this.id.hashCode();
	}

	public String getId()
	{
		return id;
	}

	public long incrementClock(int turn, long clock)
	{
		int t = 1;
		int i = 0;

		while (true)
		{
			if (turn == t)
			{
				if (maximum > 0)
					return Math.min(clock + increments[i], maximum);
				else
					return clock + increments[i];
			}

			t += turns[i];

			if (turn < t)
				return clock;

			if (i < turns.length - 1)
			{
				i++;
			}
			else
			{
				if (!infinite)
					return clock;
			}
		}
	}

	public static List<ClockConstraint> getAvailableClockConstraints()
	{
		List<ClockConstraint> result = new LinkedList<ClockConstraint>();
		result.add(new ClockConstraint("2h"));
		result.add(new ClockConstraint("5d+1d*:16d"));
		result.add(new ClockConstraint("5d+1d*"));
		result.add(new ClockConstraint("30d/10*:30d"));
		result.add(new ClockConstraint("30d/10*"));
		return result;
	}

	public static String getDefaultClockContraint()
	{
		return "5d+1d*:16d";
	}

	@Override
	public String toString()
	{
		StringBuilder str = new StringBuilder(this.getClass().getName() + "[");
		for (int i = 0; i < turns.length; i++)
			str.append(turns[i] + "," + increments[i] + ":");
		if (infinite)
			str.append("infinite,");
		if (maximum > 0)
			str.append("maximum=" + maximum + ",");
		str.setCharAt(str.length() - 1, ']');
		return str.toString();
	}
}
