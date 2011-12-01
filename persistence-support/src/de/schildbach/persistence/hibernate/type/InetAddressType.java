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

package de.schildbach.persistence.hibernate.type;

import java.io.Serializable;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;

import org.hibernate.HibernateException;
import org.hibernate.usertype.UserType;

/**
 * @author Andreas Schildbach
 */
public class InetAddressType implements UserType
{
	private static final Class<InetAddress> CLASS = InetAddress.class;
	private static final int SQL_TYPE = Types.VARCHAR;

	public int[] sqlTypes()
	{
		return new int[] { SQL_TYPE };
	}

	public Class<InetAddress> returnedClass()
	{
		return CLASS;
	}

	public boolean equals(Object x, Object y) throws HibernateException
	{
		return x.equals(y);
	}

	public int hashCode(Object x) throws HibernateException
	{
		return x.hashCode();
	}

	public InetAddress nullSafeGet(ResultSet rs, String[] names, Object owner) throws HibernateException, SQLException
	{
		assert names.length == 1;
		String value = rs.getString(names[0]);
		if (rs.wasNull())
			return (InetAddress) null;
		try
		{
			return InetAddress.getByName(value);
		}
		catch (UnknownHostException x)
		{
			throw new HibernateException(x);
		}
	}

	public void nullSafeSet(PreparedStatement st, Object value, int index) throws HibernateException, SQLException
	{
		if (value == null)
			st.setNull(index, SQL_TYPE);
		else
			st.setString(index, ((InetAddress) value).getHostAddress());
	}

	public boolean isMutable()
	{
		return false;
	}

	public Object deepCopy(Object value) throws HibernateException
	{
		return value;
	}

	public Object assemble(Serializable cached, Object owner) throws HibernateException
	{
		return cached;
	}

	public Serializable disassemble(Object value) throws HibernateException
	{
		return (Serializable) value;
	}

	public Object replace(Object original, Object target, Object owner) throws HibernateException
	{
		return original;
	}
}
