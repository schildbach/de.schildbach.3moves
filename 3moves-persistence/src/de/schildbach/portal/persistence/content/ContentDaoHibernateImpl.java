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

package de.schildbach.portal.persistence.content;

import java.util.List;

import org.hibernate.criterion.DetachedCriteria;
import org.hibernate.criterion.MatchMode;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;
import org.springframework.dao.support.DataAccessUtils;
import org.springframework.stereotype.Repository;

import de.schildbach.persistence.hibernate.GenericDaoHibernateImpl;
import de.schildbach.portal.persistence.user.User;

/**
 * @author Andreas Schildbach
 */
@Repository
public class ContentDaoHibernateImpl extends GenericDaoHibernateImpl<Content, Integer> implements ContentDao
{
	public Content findContentByTag(String tag)
	{
		DetachedCriteria criteria = DetachedCriteria.forClass(Content.class);

		criteria.add(Restrictions.eq(Content.PROPERTY_TAG, tag));

		return (Content) DataAccessUtils.uniqueResult(getHibernateTemplate().findByCriteria(criteria));
	}

	@SuppressWarnings("unchecked")
	public List<Content> findContents(Content parent, User createdBy, String nameStart, String orderBy, int maxResults)
	{
		DetachedCriteria criteria = DetachedCriteria.forClass(Content.class);

		if (parent != null)
			criteria.add(Restrictions.eq(Content.PROPERTY_PARENT, parent));
		if (createdBy != null)
			criteria.add(Restrictions.eq(Content.PROPERTY_CREATED_BY, createdBy));
		if (nameStart != null)
			criteria.add(Restrictions.like(Content.PROPERTY_NAME, nameStart, MatchMode.START));
		if (orderBy != null)
		{
			if (orderBy.charAt(0) != '!')
				criteria.addOrder(Order.asc(orderBy));
			else
				criteria.addOrder(Order.desc(orderBy.substring(1)));
		}

		return getHibernateTemplate().findByCriteria(criteria, 0, maxResults);
	}
}
