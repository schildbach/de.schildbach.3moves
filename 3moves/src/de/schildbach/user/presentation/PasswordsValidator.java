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

import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

/**
 * @author Andreas Schildbach
 */
public class PasswordsValidator implements Validator
{
	@SuppressWarnings("unchecked")
	public boolean supports(Class clazz)
	{
		return clazz.isAssignableFrom(String[].class);
	}

	public void validate(Object obj, Errors errors)
	{
		String[] passwords = (String[]) obj;

		if (passwords[0] == null)
		{
			errors.rejectValue(null, "missing"); // should be bound to index 0
		}
		else
		{
			if (!passwords[0].matches("[0-9a-zA-Z]*"))
				errors.rejectValue(null, "invalid_characters"); // should be bound to index 0
			if (passwords[0].length() < 3 || passwords[0].length() > 16)
				errors.rejectValue(null, "invalid_length"); // should be bound to index 0
		}

		if (passwords[1] == null)
		{
			errors.rejectValue(null, "missing"); // should be bound to index 1
		}

		if (passwords[0] != null && passwords[1] != null && !passwords[0].equals(passwords[1]))
		{
			errors.rejectValue(null, "passwords_mismatch");
			errors.rejectValue(null, "passwords_mismatch");
		}
	}
}
