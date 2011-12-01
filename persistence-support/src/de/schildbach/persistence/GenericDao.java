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

package de.schildbach.persistence;

import java.io.Serializable;

/**
 * @author Andreas Schildbach
 */
public interface GenericDao<T, PK extends Serializable>
{
	/** Persist the newInstance object into database */
	PK create(T newInstance);

	/** Retrieve an object that was previously persisted to the database using the indicated id as primary key */
	T read(PK id);

	/** Retrieve an object, this time allowing inheritance */
	<TT extends T> TT read(Class<TT> type, PK id);

	/** Remove an object from persistent storage in the database */
	void delete(T persistentObject);
}
