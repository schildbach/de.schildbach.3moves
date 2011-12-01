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

package de.schildbach.portal.persistence.mail;

import java.util.List;

import org.hibernate.criterion.DetachedCriteria;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;
import org.springframework.dao.support.DataAccessUtils;
import org.springframework.stereotype.Repository;

import de.schildbach.persistence.hibernate.GenericDaoHibernateImpl;
import de.schildbach.portal.persistence.user.User;

/**
 * @author Andreas Schildbach
 */
@Repository
public class MailDaoHibernateImpl extends GenericDaoHibernateImpl<Mail, Integer> implements MailDao
{
	@SuppressWarnings("unchecked")
	public List<Mail> findMails(User sender, User recipient, Boolean deletedBySender, Boolean deletedByRecipient, String orderBy)
	{
		DetachedCriteria criteria = DetachedCriteria.forClass(Mail.class);

		if (sender != null)
			criteria.add(Restrictions.eq(Mail.PROPERTY_SENDER, sender));
		if (recipient != null)
			criteria.add(Restrictions.eq(Mail.PROPERTY_RECIPIENT, recipient));
		if (deletedBySender != null)
			criteria.add(Restrictions.eq(Mail.PROPERTY_DELETED_BY_SENDER, deletedBySender));
		if (deletedByRecipient != null)
			criteria.add(Restrictions.eq(Mail.PROPERTY_DELETED_BY_RECIPIENT, deletedByRecipient));
		if (orderBy != null)
		{
			if (orderBy.charAt(0) != '!')
				criteria.addOrder(Order.asc(orderBy));
			else
				criteria.addOrder(Order.desc(orderBy.substring(1)));
		}

		return getHibernateTemplate().findByCriteria(criteria);
	}

	public int countUnreadMail(User user)
	{
		DetachedCriteria criteria = DetachedCriteria.forClass(Mail.class);

		criteria.add(Restrictions.eq(Mail.PROPERTY_RECIPIENT, user));
		criteria.add(Restrictions.eq(Mail.PROPERTY_DELETED_BY_RECIPIENT, false));
		criteria.add(Restrictions.eq(Mail.PROPERTY_READ, false));
		criteria.setProjection(Projections.rowCount());

		return DataAccessUtils.intResult(getHibernateTemplate().findByCriteria(criteria));
	}
}
