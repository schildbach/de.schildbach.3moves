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

import de.schildbach.portal.persistence.game.Aid;
import de.schildbach.portal.persistence.game.Rating;
import de.schildbach.portal.persistence.game.Rules;

/**
 * @author Andreas Schildbach
 */
public class RatingActivityImplTest
{
	private RatingActivityImpl ratingActivity = new RatingActivityImpl();

	@Test
	public void testAdjust()
	{
		assertEquals(0, ratingActivity.adjustValue(1500, 0, 1));
		assertEquals(-16, ratingActivity.adjustValue(1500, 0, 0.5f));
		assertEquals(-32, ratingActivity.adjustValue(1500, 0, 0));

		assertEquals(+16, ratingActivity.adjustValue(1500, 1500, 1));
		assertEquals(0, ratingActivity.adjustValue(1500, 1500, 0.5f));
		assertEquals(-16, ratingActivity.adjustValue(1500, 1500, 0));

		assertEquals(+32, ratingActivity.adjustValue(1500, 10000, 1));
		assertEquals(+16, ratingActivity.adjustValue(1500, 10000, 0.5f));
		assertEquals(0, ratingActivity.adjustValue(1500, 10000, 0));

		assertEquals(+15, ratingActivity.adjustValue(1516, 1500, 1));
		assertEquals(-1, ratingActivity.adjustValue(1516, 1500, 0.5f));
		assertEquals(-17, ratingActivity.adjustValue(1516, 1500, 0));

		assertEquals(+15, ratingActivity.adjustValue(1532, 1500, 1));
		assertEquals(-1, ratingActivity.adjustValue(1532, 1500, 0.5f));
		assertEquals(-17, ratingActivity.adjustValue(1532, 1500, 0));

		assertEquals(+13, ratingActivity.adjustValue(1564, 1500, 1));
		assertEquals(-3, ratingActivity.adjustValue(1564, 1500, 0.5f));
		assertEquals(-19, ratingActivity.adjustValue(1564, 1500, 0));

		// k-factor 16
		assertEquals(0, ratingActivity.adjustValue(2500, 0, 1));
		assertEquals(-8, ratingActivity.adjustValue(2500, 0, 0.5f));
		assertEquals(-16, ratingActivity.adjustValue(2500, 0, 0));
	}

	@Test
	public void assembleRating()
	{
		assertEquals(Rating.CHESS_SUICIDE_NONE_ELO, ratingActivity.assembleRating(Rules.CHESS_SUICIDE, RatingClass.ELO, Aid.NONE));
		assertEquals(Rating.CHESS_SUICIDE_COMPUTER_ELO, ratingActivity.assembleRating(Rules.CHESS_SUICIDE, RatingClass.ELO, Aid.COMPUTER));
	}
}
