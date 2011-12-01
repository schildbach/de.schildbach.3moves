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

package de.schildbach.user.presentation;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.Errors;

/**
 * @author Andreas Schildbach
 */
public class UserNameValidatorTest
{
	private UserNameValidator validator = new UserNameValidator();

	@Test
	public void shouldSucceed()
	{
		assertFalse(validate("Name").hasErrors());
		assertFalse(validate("abc_123").hasErrors());
	}

	@Test
	public void shouldFail()
	{
		assertTrue(validate("_name").hasErrors());
		assertTrue(validate("9name").hasErrors());
		assertTrue(validate("name_").hasErrors());
		assertTrue(validate("na__me").hasErrors());
		assertTrue(validate("näim").hasErrors());
	}

	private Errors validate(Object bean)
	{
		BeanPropertyBindingResult errors = new BeanPropertyBindingResult(bean, "name");
		validator.validate(bean, errors);
		return errors;
	}
}
