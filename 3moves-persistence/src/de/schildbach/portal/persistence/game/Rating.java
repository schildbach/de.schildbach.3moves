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

/**
 * @author Andreas Schildbach
 */
public enum Rating
{
	CHESS_NONE_ELO, CHESS_COMPUTER_ELO, CHESS_960_NONE_ELO, CHESS_SUICIDE_NONE_ELO, CHESS_SUICIDE_COMPUTER_ELO, CHESS_ANTIKING_NONE_ELO, DRAGONCHESS_NONE_ELO, CHECKERS_NONE_ELO, CHECKERS_COMPUTER_ELO;

	public String ratingClass()
	{
		return "ELO";
	}
}
