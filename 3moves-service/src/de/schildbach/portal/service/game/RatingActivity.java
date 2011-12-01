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

import java.util.Date;
import java.util.List;

import de.schildbach.portal.persistence.game.Aid;
import de.schildbach.portal.persistence.game.Rating;
import de.schildbach.portal.persistence.game.Rules;
import de.schildbach.portal.persistence.game.SubjectRating;

/**
 * @author Andreas Schildbach
 */
public interface RatingActivity
{
	/**
	 * Returns the initial rating for a rating class.
	 * 
	 * @param ratingClass
	 *            rating class for the initial rating
	 * @return initial rating
	 */
	String initialRating(RatingClass ratingClass);

	/**
	 * Compare two ratings.
	 * 
	 * @param ratingClass
	 *            rating class of the ratings to be compared
	 * @param rating1
	 *            the first rating to be compared
	 * @param rating2
	 *            the second rating to be compared
	 * @return a negative integer, zero, or a positive integer as the first argument is less than, equal to, or greater
	 *         than the second
	 */
	int compare(RatingClass ratingClass, String rating1, String rating2);

	void adjustRatings(Date at, RatingClass ratingClass, SubjectRating[] ratings, String[] baseRatings, float[] scores);

	void sortRatings(RatingClass ratingClass, List<SubjectRating> ratings);

	Rating assembleRating(Rules rules, RatingClass ratingClass, Aid aid);
}
