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

package de.schildbach.presentation;

import java.text.MessageFormat;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.PropertyResourceBundle;
import java.util.ResourceBundle;
import java.util.TimeZone;

/**
 * @author Andreas Schildbach
 */
public class DateUtils
{
	private static final String RESOURCEBUNDLE_NAME = "utils";

	public static String dateDiff(Date d1, Date d2, Locale locale)
	{
		if (d1 == null || d2 == null)
			return "";

		return dateDiff(Math.abs(d2.getTime() - d1.getTime()), locale);
	}

	public static String dateDiff(long milliseconds, Locale locale)
	{
		ResourceBundle bundle = PropertyResourceBundle.getBundle(RESOURCEBUNDLE_NAME, locale);

		if (milliseconds == 0)
			return "";
		if (milliseconds < 1000)
			return MessageFormat.format(bundle.getString("milliseconds"), new Long(milliseconds));

		long seconds = milliseconds / 1000;

		if (seconds == 1)
			return bundle.getString("second");
		if (seconds < 60)
			return MessageFormat.format(bundle.getString("seconds"), new Long(seconds));

		long minutes = seconds / 60;
		seconds %= 60;

		if (minutes <= 1)
			return bundle.getString("minute") + (seconds == 0 ? "" : " " + bundle.getString("and") + " " + dateDiff(seconds * 1000, locale));
		if (minutes < 10)
			return MessageFormat.format(bundle.getString("minutes")
					+ (seconds == 0 ? "" : " " + bundle.getString("and") + " " + dateDiff(seconds * 1000, locale)), new Long(minutes));
		if (minutes < 60)
			return MessageFormat.format(bundle.getString("minutes"), new Long(minutes));

		long hours = minutes / 60;
		minutes %= 60;

		if (hours <= 1)
			return bundle.getString("hour") + (minutes == 0 ? "" : " " + bundle.getString("and") + " " + dateDiff(minutes * 1000 * 60, locale));
		if (hours <= 10)
			return MessageFormat.format(bundle.getString("hours"), new Long(hours))
					+ (minutes == 0 ? "" : " " + bundle.getString("and") + " " + dateDiff(minutes * 1000 * 60, locale));
		if (hours < 24)
			return MessageFormat.format(bundle.getString("hours"), new Long(hours));

		long days = hours / 24;
		hours %= 24;

		if (days <= 1)
			return bundle.getString("day") + (hours == 0 ? "" : " " + bundle.getString("and") + " " + dateDiff(hours * 1000 * 60 * 60, locale));
		if (days < 7)
			return MessageFormat.format(bundle.getString("days"), new Long(days))
					+ (hours == 0 ? "" : " " + bundle.getString("and") + " " + dateDiff(hours * 1000 * 60 * 60, locale));

		long weeks = days / 7;
		days %= 7;

		if (weeks <= 1)
			return bundle.getString("week") + (days == 0 ? "" : " " + bundle.getString("and") + " " + dateDiff(days * 1000 * 60 * 60 * 24, locale));
		if (weeks < 4)
			return MessageFormat.format(bundle.getString("weeks"), new Long(weeks))
					+ (days == 0 ? "" : " " + bundle.getString("and") + " " + dateDiff(days * 1000 * 60 * 60 * 24, locale));
		return MessageFormat.format(bundle.getString("weeks"), new Long(weeks));
	}

	public static String dateDiffShort(Date d1, Date d2, Locale locale)
	{
		return dateDiffShort(d1, d2, locale, true);
	}

	public static String dateDiffShort(Date d1, Date d2, Locale locale, boolean hasSeconds)
	{
		if (d1 == null || d2 == null)
			return "";

		return dateDiffShort(Math.abs(d2.getTime() - d1.getTime()), locale, hasSeconds);
	}

	public static String dateDiffShort(long milliseconds, Locale locale)
	{
		return dateDiffShort(milliseconds, locale, true);
	}

	public static String dateDiffShort(long milliseconds, Locale locale, boolean hasSeconds)
	{
		ResourceBundle bundle = PropertyResourceBundle.getBundle(RESOURCEBUNDLE_NAME, locale);

		long seconds = milliseconds / 1000;
		long minutes = seconds / 60;
		seconds %= 60;
		long hours = minutes / 60;
		minutes %= 60;
		long days = hours / 24;
		hours %= 24;
		long weeks = days / 7;
		days %= 7;

		String format;
		if (weeks == 0)
		{
			if (days == 0)
			{
				if (hours == 0)
				{
					if (minutes == 0)
					{
						format = "seconds";
					}
					else
					{
						format = "minutes";
					}
				}
				else
				{
					format = "hours";
				}
			}
			else
			{
				format = "days";
			}
		}
		else
		{
			format = "weeks";
		}

		return MessageFormat.format(bundle.getString("short_" + format + (hasSeconds ? "_s" : "")), seconds, minutes, hours, days, weeks);
	}

	public static List<TimeZone> getAvailableTimeZones()
	{
		String[] timeZoneIDs = TimeZone.getAvailableIDs();
		List<TimeZone> timeZones = new LinkedList<TimeZone>();
		for (int i = 0; i < timeZoneIDs.length; i++)
		{
			if (timeZoneIDs[i].length() > 3 && !timeZoneIDs[i].startsWith("Etc/") && !timeZoneIDs[i].startsWith("SystemV/")
					&& !timeZoneIDs[i].startsWith("CST") && !timeZoneIDs[i].startsWith("EST") && !timeZoneIDs[i].startsWith("MST")
					&& !timeZoneIDs[i].startsWith("PST") && !timeZoneIDs[i].startsWith("NZ-CHAT") && !timeZoneIDs[i].startsWith("W-SU"))
			{
				timeZones.add(TimeZone.getTimeZone(timeZoneIDs[i]));
			}
		}
		Collections.sort(timeZones, new Comparator<TimeZone>()
		{
			public int compare(TimeZone tz1, TimeZone tz2)
			{
				return tz1.getID().compareTo(tz2.getID());
			}
		});
		return timeZones;
	}

	public static int calculateAge(Date now, Date birthday)
	{
		int age = 0;
		Calendar calendar = new GregorianCalendar();
		while (true)
		{
			calendar.setTime(birthday);
			calendar.add(Calendar.YEAR, age + 1);
			if (calendar.getTime().after(now))
				return age;
			age++;
		}
	}
}
