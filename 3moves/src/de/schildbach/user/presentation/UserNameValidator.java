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

import java.util.regex.Pattern;

import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

/**
 * @author Andreas Schildbach
 */
public class UserNameValidator implements Validator
{
	private static final Pattern ALLOWED_CHARS = Pattern.compile("[0-9a-zA-Z_]*");
	private static final Pattern DOUBLE_UNDERSCORE = Pattern.compile(".*__.*");

	@SuppressWarnings("unchecked")
	public boolean supports(Class clazz)
	{
		return clazz.isAssignableFrom(String.class);
	}

	public void validate(Object obj, Errors errors)
	{
		String name = (String) obj;

		if (name == null)
		{
			errors.rejectValue(null, "missing");
		}
		else
		{
			int length = name.length();
			if (length < 3 || length > 12)
				errors.rejectValue(null, "invalid_length");
			if (!ALLOWED_CHARS.matcher(name).matches())
				errors.rejectValue(null, "invalid_characters");
			char firstChar = name.charAt(0);
			if (!Character.isLetter(firstChar))
				errors.rejectValue(null, "invalid_first_character");
			char lastChar = name.charAt(length - 1);
			if (!Character.isLetter(lastChar) && !Character.isDigit(lastChar))
				errors.rejectValue(null, "invalid_last_character");
			if (DOUBLE_UNDERSCORE.matcher(name).matches())
				errors.rejectValue(null, "double_underscore");
		}
	}
}
