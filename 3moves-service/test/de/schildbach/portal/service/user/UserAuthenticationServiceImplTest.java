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

package de.schildbach.portal.service.user;

import static org.junit.Assert.assertNull;

import java.util.Date;
import java.util.Locale;

import org.junit.Before;
import org.junit.Test;

import de.schildbach.portal.persistence.user.User;
import de.schildbach.portal.service.Iso8601Format;
import de.schildbach.portal.service.exception.ApplicationException;
import de.schildbach.portal.service.mock.persistence.UserDaoMock;

/**
 * @author Andreas Schildbach
 */
public class UserAuthenticationServiceImplTest
{
	private static final String USER = "user";
	private static final String PASSWORD = "pw";
	private static final Locale LOCALE = Locale.ENGLISH;

	private UserAuthenticationServiceImpl userAuthenticationService;

	@Before
	public void setup() throws Exception
	{
		UserDaoMock userDao = new UserDaoMock();

		userAuthenticationService = new UserAuthenticationServiceImpl();
		userAuthenticationService.setUserDao(userDao);

		// add several users
		Date now = Iso8601Format.parseDateTime("2000-01-01 12:00:00");
		userDao.create(new User(now, USER, LOCALE));
	}

	@Test
	public void shouldDenyLoginWithoutPassword()
	{
		assertNull(userAuthenticationService.loginByName(USER, PASSWORD, null, null));
		assertNull(userAuthenticationService.loginByName(USER, null, null, null));
	}

	@Test(expected = ApplicationException.class)
	public void shouldDenyLoginWithWrongPassword()
	{
		userAuthenticationService.changePassword(USER, null, PASSWORD);
		userAuthenticationService.loginByName(USER, "wrongpw", null, null);
	}

	@Test(expected = ApplicationException.class)
	public void shouldAllowLoginWithCorrectPassword()
	{
		userAuthenticationService.changePassword(USER, null, PASSWORD);
		userAuthenticationService.loginByName(USER, PASSWORD, null, null);
	}
}
