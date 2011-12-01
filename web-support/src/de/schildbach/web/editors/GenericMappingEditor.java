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

package de.schildbach.web.editors;

import java.beans.PropertyEditorSupport;
import java.util.Map;

import org.springframework.util.ObjectUtils;

/**
 * @author Andreas Schildbach
 */
public class GenericMappingEditor<T> extends PropertyEditorSupport
{
	private Map<String, T> mapping;

	public GenericMappingEditor(Map<String, T> mapping)
	{
		this.mapping = mapping;
	}

	@SuppressWarnings("unchecked")
	@Override
	public String getAsText()
	{
		T value = (T) getValue();

		for (Map.Entry<String, T> m : mapping.entrySet())
			if (ObjectUtils.nullSafeEquals(m.getValue(), value))
				return m.getKey();

		throw new IllegalStateException("unknown value: " + value);
	}

	@Override
	public void setAsText(String text) throws IllegalArgumentException
	{
		T value = mapping.get(text);

		if (value != null)
		{
			setValue(value);
		}
		else
		{
			if (mapping.containsKey(text))
				setValue(null);
			else
				throw new IllegalArgumentException(text);
		}
	}
}
