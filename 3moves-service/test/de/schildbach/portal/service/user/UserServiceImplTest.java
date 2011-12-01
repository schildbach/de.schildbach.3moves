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

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.text.DateFormat;
import java.util.Date;
import java.util.Locale;

import org.junit.Before;
import org.junit.Test;

import de.schildbach.portal.persistence.user.User;
import de.schildbach.portal.service.Iso8601Format;
import de.schildbach.portal.service.mock.persistence.SubjectRelationDaoMock;
import de.schildbach.portal.service.mock.persistence.UserDaoMock;
import de.schildbach.web.RequestTime;

/**
 * @author Andreas Schildbach
 */
public class UserServiceImplTest
{
	private static final String USER = "user";
	private static final String OTHER = "other";
	private static final Locale LOCALE = Locale.ENGLISH;

	private UserServiceImpl userService;

	@Before
	public void setup() throws Exception
	{
		UserDaoMock userDao = new UserDaoMock();

		userService = new UserServiceImpl();
		userService.setUserDao(userDao);
		userService.setSubjectRelationDao(new SubjectRelationDaoMock());

		// add several users
		Date now = Iso8601Format.parseDateTime("2000-01-01 12:00:00");
		userDao.create(new User(now, USER, LOCALE));
		userDao.create(new User(now, OTHER, LOCALE));
	}

	@Test
	public void testValidateEMailAddress()
	{
		assertTrue("should be valid", userService.validateEMailAddress("support@3moves.net"));
		assertFalse("should be invalid", userService.validateEMailAddress("@3moves.net"));
		assertFalse("should be invalid", userService.validateEMailAddress("support@"));
		// assertFalse("should be invalid",
		// userService.validateEMailAddress("support@gibtsnicht.local"));
	}

	@Test
	public void testHolidays() throws Exception
	{
		DateFormat dateFormat = Iso8601Format.newDateFormat();
		DateFormat dateTimeFormat = Iso8601Format.newDateTimeFormat();
		RequestTime.set(dateFormat.parse("2004-01-01"));

		// 1 month is too much
		assertFalse(userService.canAddHolidays(USER, dateFormat.parse("2004-02-01"), dateFormat.parse("2004-03-01")));

		// 10 days is ok
		assertTrue(userService.canAddHolidays(USER, dateFormat.parse("2004-02-01"), dateFormat.parse("2004-02-10")));

		// 2 days is ok
		assertTrue(userService.canAddHolidays(USER, dateFormat.parse("2004-02-01"), dateFormat.parse("2004-02-02")));

		// 1 days is too less
		assertFalse(userService.canAddHolidays(USER, dateFormat.parse("2004-02-01"), dateFormat.parse("2004-02-01")));

		// can't add on same day
		RequestTime.set(dateFormat.parse("2004-02-01"));
		assertFalse(userService.canAddHolidays(USER, dateFormat.parse("2004-02-01"), dateFormat.parse("2004-02-02")));

		// add 10 days in february
		RequestTime.set(dateFormat.parse("2004-01-01"));
		userService.addHolidays(USER, dateFormat.parse("2004-02-01"), dateFormat.parse("2004-02-10"));

		// is user in holidays?
		RequestTime.set(dateTimeFormat.parse("2004-01-31 00:00:00"));
		assertFalse(userService.isUserInHolidays(USER));
		RequestTime.set(dateTimeFormat.parse("2004-01-31 08:00:00"));
		assertFalse(userService.isUserInHolidays(USER));
		RequestTime.set(dateTimeFormat.parse("2004-02-01 00:00:00"));
		assertTrue(userService.isUserInHolidays(USER));
		RequestTime.set(dateTimeFormat.parse("2004-02-01 08:00:00"));
		assertTrue(userService.isUserInHolidays(USER));
		RequestTime.set(dateTimeFormat.parse("2004-02-02 00:00:00"));
		assertTrue(userService.isUserInHolidays(USER));
		RequestTime.set(dateTimeFormat.parse("2004-02-02 08:00:00"));
		assertTrue(userService.isUserInHolidays(USER));
		RequestTime.set(dateTimeFormat.parse("2004-02-09 00:00:00"));
		assertTrue(userService.isUserInHolidays(USER));
		RequestTime.set(dateTimeFormat.parse("2004-02-09 08:00:00"));
		assertTrue(userService.isUserInHolidays(USER));
		RequestTime.set(dateTimeFormat.parse("2004-02-10 00:00:00"));
		assertTrue(userService.isUserInHolidays(USER));
		RequestTime.set(dateTimeFormat.parse("2004-02-10 08:00:00"));
		assertTrue(userService.isUserInHolidays(USER));
		RequestTime.set(dateTimeFormat.parse("2004-02-11 00:00:00"));
		assertFalse(userService.isUserInHolidays(USER));
		RequestTime.set(dateTimeFormat.parse("2004-02-11 08:00:00"));
		assertFalse(userService.isUserInHolidays(USER));

		// 10 days is ok
		assertTrue(userService.canAddHolidays(USER, dateFormat.parse("2004-03-01"), dateFormat.parse("2004-03-10")));

		// 12 days is not ok (22 days altogether)
		assertFalse(userService.canAddHolidays(USER, dateFormat.parse("2004-03-01"), dateFormat.parse("2004-03-12")));

		// overlap
		assertFalse(userService.canAddHolidays(USER, dateFormat.parse("2004-01-31"), dateFormat.parse("2004-02-11")));
		assertFalse(userService.canAddHolidays(USER, dateFormat.parse("2004-02-02"), dateFormat.parse("2004-02-09")));
		assertFalse(userService.canAddHolidays(USER, dateFormat.parse("2004-01-30"), dateFormat.parse("2004-02-02")));
		assertFalse(userService.canAddHolidays(USER, dateFormat.parse("2004-02-05"), dateFormat.parse("2004-02-15")));

		// can't remove on first day or later
		RequestTime.set(dateFormat.parse("2004-02-01"));
		assertFalse(userService.canRemoveHolidays(USER, dateFormat.parse("2004-02-01")));
		RequestTime.set(dateFormat.parse("2004-02-02"));
		assertFalse(userService.canRemoveHolidays(USER, dateFormat.parse("2004-02-01")));
		RequestTime.set(dateFormat.parse("2004-02-10"));
		assertFalse(userService.canRemoveHolidays(USER, dateFormat.parse("2004-02-01")));
		RequestTime.set(dateFormat.parse("2004-02-11"));
		assertFalse(userService.canRemoveHolidays(USER, dateFormat.parse("2004-02-01")));

		// can remove before first day
		RequestTime.set(dateFormat.parse("2004-01-31"));
		assertTrue(userService.canRemoveHolidays(USER, dateFormat.parse("2004-02-01")));
		RequestTime.set(dateFormat.parse("2004-01-01"));
		assertTrue(userService.canRemoveHolidays(USER, dateFormat.parse("2004-02-01")));

		// remove
		userService.removeHolidays(USER, dateFormat.parse("2004-02-01"));

		// is user in holidays?
		RequestTime.set(dateFormat.parse("2004-01-31"));
		assertFalse(userService.isUserInHolidays(USER));
		RequestTime.set(dateFormat.parse("2004-02-01"));
		assertFalse(userService.isUserInHolidays(USER));
		RequestTime.set(dateFormat.parse("2004-02-02"));
		assertFalse(userService.isUserInHolidays(USER));
		RequestTime.set(dateFormat.parse("2004-02-09"));
		assertFalse(userService.isUserInHolidays(USER));
		RequestTime.set(dateFormat.parse("2004-02-10"));
		assertFalse(userService.isUserInHolidays(USER));
		RequestTime.set(dateFormat.parse("2004-02-11"));
		assertFalse(userService.isUserInHolidays(USER));
	}

	@Test
	public void areFriends()
	{
		assertFalse(userService.areFriends(USER, OTHER));
		assertFalse(userService.areFriends(OTHER, USER));
		userService.becomeFanOf(OTHER, USER);
		assertFalse(userService.areFriends(USER, OTHER));
		assertFalse(userService.areFriends(OTHER, USER));
		userService.becomeFanOf(USER, OTHER);
		assertTrue(userService.areFriends(USER, OTHER));
		assertTrue(userService.areFriends(OTHER, USER));
		userService.removeSubjectRelation(OTHER, USER);
		assertFalse(userService.areFriends(USER, OTHER));
		assertFalse(userService.areFriends(OTHER, USER));
	}

	@Test
	public void isBannedBy()
	{
		assertFalse(userService.isBannedBy(USER, OTHER));
		userService.ban(OTHER, USER);
		assertTrue(userService.isBannedBy(USER, OTHER));
		userService.removeSubjectRelation(OTHER, USER);
		assertFalse(userService.isBannedBy(USER, OTHER));
	}
}
