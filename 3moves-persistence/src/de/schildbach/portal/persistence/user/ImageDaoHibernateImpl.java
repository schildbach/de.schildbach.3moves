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
import org.springframework.stereotype.Repository;

import de.schildbach.persistence.hibernate.GenericDaoHibernateImpl;

/**
 * @author Andreas Schildbach
 */
@Repository
public class ImageDaoHibernateImpl extends GenericDaoHibernateImpl<Image, Integer> implements ImageDao
{
	@SuppressWarnings("unchecked")
	public List<Image> findImages(User owner)
	{
		DetachedCriteria criteria = DetachedCriteria.forClass(Image.class);

		criteria.add(Restrictions.eq(Image.PROPERTY_OWNER, owner));

		return getHibernateTemplate().findByCriteria(criteria);
	}
}
