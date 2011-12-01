/*
 * Copyright 2007 the original author or authors.
 * 
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation, version 2.1.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
 */

package de.schildbach.persistence.hibernate;

import java.io.Serializable;
import java.lang.reflect.ParameterizedType;

import org.springframework.orm.hibernate3.support.HibernateDaoSupport;

import de.schildbach.persistence.GenericDao;

/**
 * @author Andreas Schildbach
 */
public abstract class GenericDaoHibernateImpl<T, PK extends Serializable> extends HibernateDaoSupport implements GenericDao<T, PK>
{
	private Class<T> type;

	@SuppressWarnings("unchecked")
	public GenericDaoHibernateImpl()
	{
		this.type = (Class<T>) ((ParameterizedType) getClass().getGenericSuperclass()).getActualTypeArguments()[0];
	}

	@SuppressWarnings("unchecked")
	public PK create(T o)
	{
		return (PK) getHibernateTemplate().save(o);
	}

	@SuppressWarnings("unchecked")
	public T read(PK id)
	{
		return (T) getHibernateTemplate().load(type, id);
	}

	@SuppressWarnings("unchecked")
	public <TT extends T> TT read(Class<TT> type, PK id)
	{
		return (TT) getHibernateTemplate().load(type, id);
	}

	public void delete(T o)
	{
		getHibernateTemplate().delete(o);
	}
}
