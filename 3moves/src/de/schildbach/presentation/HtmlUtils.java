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

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Andreas Schildbach
 */
public class HtmlUtils
{
	public String convertToHtml(String text)
	{
		StringBuilder buffer = new StringBuilder(text);
		for (int pos = 0; pos < buffer.length();)
		{
			String replace = null;
			switch (buffer.charAt(pos))
			{
				case '&':
					replace = "&amp;";
					break;

				case '<':
					replace = "&lt;";
					break;

				case '>':
					replace = "&gt;";
					break;

				case '\n':
					replace = "<br/>";
					break;
			}
			if (replace != null)
			{
				buffer.replace(pos, pos + 1, replace);
				pos += replace.length();
			}
			else
			{
				pos++;
			}
		}

		return buffer.toString();
	}

	private static final String PREFIX = "(?:^|(\\s|[\\(\\[\\{\\<\\\"\\']))";

	private static final Pattern PATTERN_GAME = Pattern.compile(PREFIX + "(game|spiel|partie|match|turnier|tournament) +(\\d+)(?:$|(\\W))",
			Pattern.CASE_INSENSITIVE);

	public String linkGames(String text, String contextPath)
	{
		Matcher m = PATTERN_GAME.matcher(text.toString());
		return m.replaceAll("$1<a class=\"gametooltip\" href=\"" + contextPath + "/game/view_game.html?id=$3\" name=\"$3\">$2 $3</a>$4");
	}

	private static final Pattern PATTERN_EMAIL = Pattern.compile(PREFIX + "([_a-z0-9-]+(\\.[_a-z0-9-]+)*@[a-z0-9-]+(\\.[a-z0-9-]+)*)",
			Pattern.CASE_INSENSITIVE);

	public String linkEMail(String text)
	{
		Matcher m = PATTERN_EMAIL.matcher(text.toString());
		return m.replaceAll("$1<a href=\"mailto:$2\">$2</a>");
	}

	private static final Pattern PATTERN_URL = Pattern.compile("(http(?:s)?://(?:\\S*)\\.(?:\\S*))", Pattern.CASE_INSENSITIVE);

	public String linkUrls(String text)
	{
		Matcher m = PATTERN_URL.matcher(text.toString());
		return m.replaceAll("<a href=\"$1\">$1</a>");
	}
}
