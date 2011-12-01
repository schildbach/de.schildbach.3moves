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

import java.util.List;

import de.schildbach.persistence.GenericDao;
import de.schildbach.portal.persistence.user.Subject;

/**
 * @author Andreas Schildbach
 */
public interface SubjectRatingDao extends GenericDao<SubjectRating, Integer>
{
	SubjectRating findRating(Subject subject, Rating rating);

	List<SubjectRating> findRatingsForSubject(Subject subject);

	List<SubjectRating> findRatings(Rating rating, Integer minIndex, Integer maxIndex, String orderBy);

	void save(SubjectRatingHistory history);

	List<SubjectRatingHistory> findRatingHistory(Subject subject, Rating rating, String orderBy);
}
