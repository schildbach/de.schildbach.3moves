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

import java.util.TimeZone;

import org.junit.Before;
import org.junit.Test;

/**
 * @author Andreas Schildbach
 */
public class TimeZoneEditorTest
{
	private static final String GMT = "GMT";
	private static final String EMPTY = "";

	private TimeZoneEditor editor;

	@Before
	public void setup()
	{
		editor = new TimeZoneEditor(true);
	}

	@Test
	public void convertGMT()
	{
		editor.setAsText(GMT);
		assertEquals(TimeZone.getTimeZone(GMT), editor.getValue());
		assertEquals(GMT, editor.getAsText());
	}

	@Test
	public void convertNull()
	{
		editor.setAsText(EMPTY);
		assertNull(editor.getValue());
		assertEquals(EMPTY, editor.getAsText());
	}
}
