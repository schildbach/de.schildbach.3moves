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

package de.schildbach.util;

import java.util.StringTokenizer;

/**
 * @author Andreas Schildbach
 */
public class TextWrapper
{
	public static String wrap(String text, int margin)
	{
		text = text.replaceAll("\n\n", "\n \n");

		StringBuilder result = new StringBuilder();
		for (StringTokenizer tLine = new StringTokenizer(text, "\n"); tLine.hasMoreTokens();)
		{
			String line = tLine.nextToken();
			StringBuilder lineBuffer = new StringBuilder();
			for (StringTokenizer tWord = new StringTokenizer(line, " "); tWord.hasMoreTokens();)
			{
				String word = tWord.nextToken();
				if (lineBuffer.length() == 0)
					lineBuffer.append(word);
				else if (lineBuffer.length() + word.length() < margin)
					lineBuffer.append(" " + word);
				else
				{
					result.append(lineBuffer);
					result.append("\n");
					lineBuffer.setLength(0);
					lineBuffer.append(word);
				}
			}
			result.append(lineBuffer);
			result.append("\n");
		}

		return result.toString();
	}
}
