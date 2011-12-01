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

package de.schildbach.user.presentation.authentication;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import de.schildbach.user.presentation.authentication.IdentifierHelper.IdentifierType;

/**
 * @author Andreas Schildbach
 */
public class IdentifierHelperTest
{
	@Test
	public void shouldBeName()
	{
		assertEquals(IdentifierType.NAME, IdentifierHelper.determineIdentifierType("goonie"));
		assertEquals(IdentifierType.NAME, IdentifierHelper.determineIdentifierType("the_master12"));
		assertEquals(IdentifierType.NAME, IdentifierHelper.determineIdentifierType("Test_Test"));
		assertEquals(IdentifierType.NAME, IdentifierHelper.determineIdentifierType("987chris654"));
	}

	@Test
	public void shouldBeEMail()
	{
		assertEquals(IdentifierType.EMAIL, IdentifierHelper.determineIdentifierType("mail@example.com"));
		assertEquals(IdentifierType.EMAIL, IdentifierHelper.determineIdentifierType("Test.Test@example.com"));
	}

	@Test
	public void shouldBeOpenId()
	{
		assertEquals(IdentifierType.OPEN_ID, IdentifierHelper.determineIdentifierType("http://example.com/"));
		assertEquals(IdentifierType.OPEN_ID, IdentifierHelper.determineIdentifierType("=example.com"));
		assertEquals(IdentifierType.OPEN_ID, IdentifierHelper.determineIdentifierType("@freeid*goonie"));
	}
}
