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

import org.hibernate.criterion.DetachedCriteria;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;
import org.springframework.dao.support.DataAccessUtils;
import org.springframework.stereotype.Repository;

import de.schildbach.persistence.hibernate.GenericDaoHibernateImpl;
import de.schildbach.portal.persistence.user.Subject;

/**
 * @author Andreas Schildbach
 */
@Repository
public class SubjectRatingDaoHibernateImpl extends GenericDaoHibernateImpl<SubjectRating, Integer> implements SubjectRatingDao
{
	public SubjectRating findRating(Subject subject, Rating rating)
	{
		DetachedCriteria criteria = DetachedCriteria.forClass(SubjectRating.class);

		criteria.add(Restrictions.naturalId().set(SubjectRating.PROPERTY_SUBJECT, subject).set(SubjectRating.PROPERTY_RATING, rating));

		criteria.getExecutableCriteria(getSession()).setCacheable(true);

		return (SubjectRating) DataAccessUtils.uniqueResult(getHibernateTemplate().findByCriteria(criteria));
	}

	@SuppressWarnings("unchecked")
	public List<SubjectRating> findRatingsForSubject(Subject subject)
	{
		DetachedCriteria criteria = DetachedCriteria.forClass(SubjectRating.class);

		criteria.add(Restrictions.eq(SubjectRating.PROPERTY_SUBJECT, subject));

		return getHibernateTemplate().findByCriteria(criteria);
	}

	@SuppressWarnings("unchecked")
	public List<SubjectRating> findRatings(Rating rating, Integer minIndex, Integer maxIndex, String orderBy)
	{
		DetachedCriteria criteria = DetachedCriteria.forClass(SubjectRating.class);

		criteria.add(Restrictions.eq(SubjectRating.PROPERTY_RATING, rating));
		if (minIndex != null || maxIndex != null)
			criteria.add(Restrictions.isNotNull(SubjectRating.PROPERTY_INDEX));
		if (minIndex != null)
			criteria.add(Restrictions.ge(SubjectRating.PROPERTY_INDEX, minIndex));
		if (maxIndex != null)
			criteria.add(Restrictions.le(SubjectRating.PROPERTY_INDEX, maxIndex));
		if (orderBy != null)
		{
			if (orderBy.charAt(0) != '!')
				criteria.addOrder(Order.asc(orderBy));
			else
				criteria.addOrder(Order.desc(orderBy.substring(1)));
		}

		return getHibernateTemplate().findByCriteria(criteria);
	}

	public void save(SubjectRatingHistory history)
	{
		getHibernateTemplate().save(history);
	}

	@SuppressWarnings("unchecked")
	public List<SubjectRatingHistory> findRatingHistory(Subject subject, Rating rating, String orderBy)
	{
		DetachedCriteria criteria = DetachedCriteria.forClass(SubjectRatingHistory.class);

		criteria.add(Restrictions.eq(SubjectRatingHistory.PROPERTY_SUBJECT, subject));
		criteria.add(Restrictions.eq(SubjectRatingHistory.PROPERTY_RATING, rating));
		if (orderBy != null)
		{
			if (orderBy.charAt(0) != '!')
				criteria.addOrder(Order.asc(orderBy));
			else
				criteria.addOrder(Order.desc(orderBy.substring(1)));
		}

		return getHibernateTemplate().findByCriteria(criteria);
	}
}
