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

package de.schildbach.portal.persistence.game;

import java.util.ArrayList;

/**
 * @author Andreas Schildbach
 */
public enum Rules
{
	CHESS, CHESS_960, CHESS_SUICIDE, CHESS_ANTIKING, DRAGONCHESS, CHECKERS, CHECKERS_SUICIDE, GO_CAPTURE;

	public String getMinorId()
	{
		String[] s = name().split("_", 2);
		if (s.length < 2)
			return null;
		else
			return s[1];
	}

	public static Rules[] fromMajorId(String majorId)
	{
		ArrayList<Rules> rules = new ArrayList<Rules>();

		for (Rules r : values())
			if (r.name().equals(majorId) || r.name().startsWith(majorId + "_"))
				rules.add(r);

		return rules.toArray(new Rules[0]);
	}
}
