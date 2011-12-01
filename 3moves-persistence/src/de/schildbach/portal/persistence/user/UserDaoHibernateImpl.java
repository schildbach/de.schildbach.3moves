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

package de.schildbach.portal.persistence.user;

import java.util.List;

import org.hibernate.Criteria;
import org.hibernate.FetchMode;
import org.hibernate.Hibernate;
import org.hibernate.criterion.DetachedCriteria;
import org.hibernate.criterion.Junction;
import org.hibernate.criterion.MatchMode;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;
import org.springframework.dao.support.DataAccessUtils;
import org.springframework.stereotype.Repository;

import de.schildbach.persistence.hibernate.GenericDaoHibernateImpl;

/**
 * @author Andreas Schildbach
 */
@Repository
public class UserDaoHibernateImpl extends GenericDaoHibernateImpl<Subject, String> implements UserDao
{
	public User loadUser(String name, boolean fetchRoles)
	{
		User user = (User) getHibernateTemplate().load(User.class, name);
		if (fetchRoles)
			Hibernate.initialize(user.getUserRoles());
		return user;
	}

	public Subject findSubject(Class<? extends Subject> subjectClass, String name)
	{
		return (Subject) getHibernateTemplate().get(subjectClass, name);
	}

	public User findUserCaseInsensitive(String userName, boolean fetchRoles)
	{
		DetachedCriteria criteria = DetachedCriteria.forClass(User.class);

		criteria.add(Restrictions.eq(Subject.PROPERTY_NAME, userName).ignoreCase());
		if (fetchRoles)
			criteria.setFetchMode(User.PROPERTY_USER_ROLES, FetchMode.JOIN);
		criteria.setResultTransformer(Criteria.DISTINCT_ROOT_ENTITY);

		return (User) DataAccessUtils.uniqueResult(getHibernateTemplate().findByCriteria(criteria));
	}

	public User findUserByOpenId(String openId)
	{
		DetachedCriteria criteria = DetachedCriteria.forClass(User.class);

		criteria.add(Restrictions.eq(User.PROPERTY_OPEN_ID, openId));
		criteria.setFetchMode(User.PROPERTY_USER_ROLES, FetchMode.JOIN);
		criteria.setResultTransformer(Criteria.DISTINCT_ROOT_ENTITY);

		return (User) DataAccessUtils.uniqueResult(getHibernateTemplate().findByCriteria(criteria));
	}

	public User findUserByEmail(String email)
	{
		DetachedCriteria criteria = DetachedCriteria.forClass(User.class);

		criteria.add(Restrictions.eq(User.PROPERTY_EMAIL, email));
		criteria.setFetchMode(User.PROPERTY_USER_ROLES, FetchMode.JOIN);
		criteria.setResultTransformer(Criteria.DISTINCT_ROOT_ENTITY);

		return (User) DataAccessUtils.uniqueResult(getHibernateTemplate().findByCriteria(criteria));
	}

	@SuppressWarnings("unchecked")
	public List<User> findUsers(String nameFilter, Role role, String orderBy, int maxResults)
	{
		DetachedCriteria criteria = DetachedCriteria.forClass(User.class);

		if (nameFilter != null)
		{
			Junction or = Restrictions.disjunction();
			// MySQL specific
			or.add(Restrictions.sqlRestriction(Subject.COLUMN_NAME + " regexp ?", nameFilter, Hibernate.STRING));
			or.add(Restrictions.sqlRestriction(User.COLUMN_FULL_NAME + " regexp ?", nameFilter, Hibernate.STRING));
			criteria.add(or);
		}
		if (role != null)
			criteria.createCriteria(User.PROPERTY_USER_ROLES).add(Restrictions.eq(UserRole.PROPERTY_ROLE, role));
		if (orderBy != null)
		{
			if (orderBy.charAt(0) != '!')
				criteria.addOrder(Order.asc(orderBy));
			else
				criteria.addOrder(Order.desc(orderBy.substring(1)));
		}

		return getHibernateTemplate().findByCriteria(criteria, 0, maxResults);
	}

	public UserRole loadUserRole(String name)
	{
		return (UserRole) getHibernateTemplate().load(UserRole.class, name);
	}

	@SuppressWarnings("unchecked")
	public List<UserRole> findUserRoles()
	{
		DetachedCriteria criteria = DetachedCriteria.forClass(UserRole.class);

		return getHibernateTemplate().findByCriteria(criteria);
	}

	@SuppressWarnings("unchecked")
	public List<String> findUserNames(String nameStart, int maxResults)
	{
		DetachedCriteria criteria = DetachedCriteria.forClass(User.class);

		criteria.setProjection(Projections.property(User.PROPERTY_NAME));
		criteria.add(Restrictions.like(User.PROPERTY_NAME, nameStart, MatchMode.START));

		return getHibernateTemplate().findByCriteria(criteria, 0, maxResults);
	}
}
