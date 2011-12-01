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
public class EmailValidator implements Validator
{
	private static final Pattern PATTERN = Pattern.compile("^[A-Za-z0-9\\._-]+[@]([A-Za-z0-9_-]+([.][A-Za-z0-9_-]+)+[A-Za-z])$");

	@SuppressWarnings("unchecked")
	public boolean supports(Class clazz)
	{
		return clazz.isAssignableFrom(String.class);
	}

	public void validate(Object obj, Errors errors)
	{
		String email = (String) obj;

		if (!PATTERN.matcher(email).matches())
			errors.rejectValue(null, "invalid");
	}
}
