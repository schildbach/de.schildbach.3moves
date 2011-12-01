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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import org.junit.Before;
import org.junit.Test;

/**
 * @author Andreas Schildbach
 */
public class EnumEditorTest
{
	private static final String VALUE1 = "VALUE1";
	private static final String VALUE2 = "VALUE2";
	private static final String EMPTY = "";

	private EnumEditor editor;

	private enum Enum
	{
		VALUE1, VALUE2
	}

	@Before
	public void setup()
	{
		editor = new EnumEditor(Enum.class, true);
	}

	@Test
	public void convertValue1()
	{
		editor.setAsText(VALUE1);
		assertEquals(Enum.VALUE1, editor.getValue());
		assertEquals(VALUE1, editor.getAsText());
	}

	@Test
	public void convertValue2()
	{
		editor.setAsText(VALUE2);
		assertEquals(Enum.VALUE2, editor.getValue());
		assertEquals(VALUE2, editor.getAsText());
	}

	@Test
	public void convertNull()
	{
		editor.setAsText(EMPTY);
		assertNull(editor.getValue());
		assertEquals(EMPTY, editor.getAsText());
	}
}
