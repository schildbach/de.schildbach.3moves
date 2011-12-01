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

/**
 * @author Andreas Schildbach
 * @deprecated use de.schildbach.web.editors.EnumEditor instead
 */
@SuppressWarnings("unchecked")
public class EnumEditor extends PropertyEditorSupport
{
	private Class enumType;
	private boolean emptyAsNull;

	public EnumEditor(Class enumType, boolean emptyAsNull)
	{
		this.enumType = enumType;
		this.emptyAsNull = emptyAsNull;
	}

	@Override
	public void setAsText(String text) throws IllegalArgumentException
	{
		Enum<?> value = null;
		if (!emptyAsNull || text.length() > 0)
			value = Enum.valueOf(enumType, text);
		setValue(value);
	}

	@Override
	public String getAsText()
	{
		if (!emptyAsNull || getValue() != null)
			return ((Enum<?>) getValue()).name();
		else
			return "";
	}
}
