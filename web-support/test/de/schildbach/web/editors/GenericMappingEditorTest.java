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

import java.util.HashMap;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;

/**
 * @author Andreas Schildbach
 */
public class GenericMappingEditorTest
{
	private static final String TEXT1 = "text1";
	private static final String TEXT2 = "text2";
	private static final String TEXT3 = "text3";

	private GenericMappingEditor<Integer> editor;

	@Before
	public void setup()
	{
		Map<String, Integer> mapping = new HashMap<String, Integer>();
		mapping.put(TEXT1, 1);
		mapping.put(TEXT2, 2);
		mapping.put(TEXT3, null);

		editor = new GenericMappingEditor<Integer>(mapping);
	}

	@Test
	public void test1()
	{
		editor.setAsText(TEXT1);
		assertEquals(1, editor.getValue());
		assertEquals(TEXT1, editor.getAsText());
	}

	@Test
	public void test2()
	{
		editor.setAsText(TEXT2);
		assertEquals(2, editor.getValue());
		assertEquals(TEXT2, editor.getAsText());
	}

	@Test
	public void test3()
	{
		editor.setAsText(TEXT3);
		assertNull(editor.getValue());
		assertEquals(TEXT3, editor.getAsText());
	}

	@Test(expected = IllegalArgumentException.class)
	public void testIllegalSet()
	{
		editor.setAsText("illegal");
	}

	@Test(expected = IllegalStateException.class)
	public void testIllegalGet()
	{
		editor.setValue(-1);
		editor.getAsText();
	}
}
