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

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;

import org.hibernate.type.TimestampType;

/**
 * @author Andreas Schildbach
 */
public class DateTimeType extends TimestampType
{
	@Override
	public Object get(ResultSet rs, String name) throws SQLException
	{
		Timestamp timestamp = (Timestamp) super.get(rs, name);
		if(timestamp != null)
			return new java.util.Date(timestamp.getTime());
		else
			return null;
	}
}
