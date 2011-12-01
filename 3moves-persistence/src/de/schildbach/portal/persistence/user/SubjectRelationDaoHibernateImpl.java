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

import org.hibernate.criterion.DetachedCriteria;
import org.hibernate.criterion.Restrictions;
import org.springframework.dao.support.DataAccessUtils;
import org.springframework.stereotype.Repository;

import de.schildbach.persistence.hibernate.GenericDaoHibernateImpl;

/**
 * @author Andreas Schildbach
 */
@Repository
public class SubjectRelationDaoHibernateImpl extends GenericDaoHibernateImpl<SubjectRelation, Integer> implements SubjectRelationDao
{
	public SubjectRelation findSubjectRelation(Subject source, Subject target)
	{
		DetachedCriteria criteria = DetachedCriteria.forClass(SubjectRelation.class);

		criteria.add(Restrictions.naturalId().set(SubjectRelation.PROPERTY_SOURCE_SUBJECT, source).set(SubjectRelation.PROPERTY_TARGET_SUBJECT,
				target));

		criteria.getExecutableCriteria(getSession()).setCacheable(true);

		return (SubjectRelation) DataAccessUtils.uniqueResult(getHibernateTemplate().findByCriteria(criteria));
	}

	@SuppressWarnings("unchecked")
	public List<SubjectRelation> findSubjectRelationsBySource(Subject source, RelationType type)
	{
		DetachedCriteria criteria = DetachedCriteria.forClass(SubjectRelation.class);

		criteria.add(Restrictions.eq(SubjectRelation.PROPERTY_SOURCE_SUBJECT, source));
		if (type != null)
			criteria.add(Restrictions.eq(SubjectRelation.PROPERTY_TYPE, type));

		return getHibernateTemplate().findByCriteria(criteria);
	}

	@SuppressWarnings("unchecked")
	public List<SubjectRelation> findSubjectRelationsByTarget(Subject target)
	{
		DetachedCriteria criteria = DetachedCriteria.forClass(SubjectRelation.class);

		criteria.add(Restrictions.eq(SubjectRelation.PROPERTY_TARGET_SUBJECT, target));

		return getHibernateTemplate().findByCriteria(criteria);
	}
}
