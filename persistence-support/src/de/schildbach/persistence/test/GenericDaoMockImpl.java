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

package de.schildbach.persistence.test;

import java.io.Serializable;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import de.schildbach.persistence.GenericDao;

/**
 * @author Andreas Schildbach
 */
public abstract class GenericDaoMockImpl<T, PK extends Serializable> implements GenericDao<T, PK>
{
	private Map<PK, T> data = new HashMap<PK, T>();

	protected abstract PK generateId(T persistentObject);

	protected final Collection<T> data()
	{
		return Collections.unmodifiableCollection(data.values());
	}

	public final PK create(T newInstance)
	{
		if (newInstance == null)
			throw new IllegalArgumentException("newInstance: " + newInstance);

		PK id = generateId(newInstance);

		if (data.containsKey(id))
			throw new RuntimeException("duplicate key: " + id);

		data.put(id, newInstance);

		return id;
	}

	public final T read(PK id)
	{
		if (id == null)
			throw new IllegalArgumentException("id: " + id);

		T persistentObject = data.get(id);

		if (persistentObject == null)
			throw new RuntimeException(id + " not found");

		return persistentObject;
	}

	@SuppressWarnings("unchecked")
	public final <TT extends T> TT read(Class<TT> type, PK id)
	{
		if (type == null)
			throw new IllegalArgumentException("type: " + id);
		if (id == null)
			throw new IllegalArgumentException("id: " + id);

		T persistentObject = data.get(id);

		if (persistentObject == null)
			throw new RuntimeException(id + " not found");

		if (!type.isAssignableFrom(persistentObject.getClass()))
			throw new RuntimeException(id + " is not of required type: " + type.getName());

		return (TT) persistentObject;
	}

	public final void delete(T persistentObject)
	{
		if (persistentObject == null)
			throw new IllegalArgumentException("persistentObject: " + persistentObject);

		for (Iterator<T> i = data.values().iterator(); i.hasNext();)
		{
			T value = i.next();
			if (value.equals(persistentObject))
			{
				i.remove();
				return;
			}
		}

		throw new RuntimeException(persistentObject + " not found");
	}

	public void reset()
	{
		data.clear();
	}
}
