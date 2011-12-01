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

import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import de.schildbach.portal.persistence.game.Aid;
import de.schildbach.portal.persistence.game.Rating;
import de.schildbach.portal.persistence.game.Rules;
import de.schildbach.portal.persistence.game.SubjectRating;

/**
 * @author Andreas Schildbach
 */
public class RatingActivityImpl implements RatingActivity
{
	@SuppressWarnings("unused")
	private static final Log LOG = LogFactory.getLog(RatingActivityImpl.class);

	private static final int TWO = 2;
	private static final int INITIAL_ELO_RATING = 1500;

	public String initialRating(RatingClass ratingClass)
	{
		if (ratingClass == RatingClass.ELO)
			return "" + INITIAL_ELO_RATING;
		else
			throw new IllegalArgumentException("can only handle elo");
	}

	public int compare(RatingClass ratingClass, String rating1, String rating2)
	{
		if (ratingClass == RatingClass.ELO)
			return new Integer(rating1).compareTo(new Integer(rating2));
		else
			throw new IllegalArgumentException("can only handle elo");
	}

	public void adjustRatings(Date at, RatingClass ratingClass, SubjectRating[] ratings, String[] baseRatings, float[] scores)
	{
		if (ratings.length != TWO || baseRatings.length != TWO || scores.length != TWO)
			throw new IllegalArgumentException("can only handle adjusts of " + TWO + " ratings");
		if (ratingClass == RatingClass.ELO)
		{
			// convert rating values to ints
			int[] baseRatingsInt = new int[TWO];
			for (int i = 0; i < TWO; i++)
				baseRatingsInt[i] = Integer.parseInt(baseRatings[i]);

			// adjust rating values
			for (int i = 0; i < TWO; i++)
			{
				int opponent = 1 - i;
				ratings[i].setLastValue(ratings[i].getValue());
				int newRating = Integer.parseInt(ratings[i].getValue()) + adjustValue(baseRatingsInt[i], baseRatingsInt[opponent], scores[i]);
				ratings[i].setValue(String.valueOf(newRating));
				ratings[i].setLastModifiedAt(at);
				LOG.info("rating change: class=" + ratingClass + ",subject=" + ratings[i].getSubject().getName() + ",base=" + baseRatingsInt[i]
						+ ",score=" + scores[i] + ",last=" + ratings[i].getLastValue() + ",new=" + newRating);
			}
		}
		else
			throw new IllegalArgumentException("can only handle elo");
	}

	protected int adjustValue(int oldRating, int oldOpponentRating, float score)
	{
		double expectedScore = 1d / (1d + Math.pow(10d, (oldOpponentRating - oldRating) / 400d));
		return (int) Math.round(kFactor(oldRating) * (score - expectedScore));
	}

	private double kFactor(int rating)
	{
		if (rating < 2400)
			return 32d;
		else
			return 16d;
	}

	public void sortRatings(RatingClass ratingClass, List<SubjectRating> ratings)
	{
		Comparator<SubjectRating> comparator;

		if (ratingClass == RatingClass.ELO)
		{
			comparator = new Comparator<SubjectRating>()
			{
				public int compare(SubjectRating r1, SubjectRating r2)
				{
					return new Integer(r2.getValue()).compareTo(new Integer(r1.getValue()));
				}
			};
		}
		else
			throw new IllegalArgumentException("can only handle elo");

		Collections.sort(ratings, comparator);
	}

	public Rating assembleRating(Rules rules, RatingClass ratingClass, Aid aid)
	{
		StringBuilder name = new StringBuilder(rules.name());

		name.append('_').append(aid.name());

		if (ratingClass == RatingClass.ELO)
			name.append('_').append(ratingClass.name());
		else
			throw new IllegalArgumentException("can only handle elo");

		return Rating.valueOf(name.toString());
	}
}
