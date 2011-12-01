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

package de.schildbach.presentation;

import java.beans.PropertyEditorSupport;
import java.util.Map;

import org.springframework.util.ObjectUtils;

/**
 * @author Andreas Schildbach
 * @deprecated use de.schildbach.web.editors.GenericMappingEditor instead
 */
public class GenericMappingPropertyEditor<T> extends PropertyEditorSupport
{
	private Map<String, T> mapping;

	public GenericMappingPropertyEditor(Map<String, T> mapping)
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
