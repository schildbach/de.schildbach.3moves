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

package de.schildbach.portal.persistence.user;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Date;
import java.util.Locale;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import de.schildbach.portal.persistence.AbstractDaoTest;

/**
 * @author Andreas Schildbach
 */
public class UserDaoTest extends AbstractDaoTest
{
	private static final String USERNAME = "user";
	private static final String SYSTEMACCOUNTNAME = "system";

	@Autowired
	private UserDao userDao;

	@Test
	public void shouldCreateReadAndDeleteUser()
	{
		User user = new User(new Date(), USERNAME, Locale.ENGLISH);
		defaultPermissions(user);

		int count = countRowsInTable(Subject.TABLE_NAME);

		String name = userDao.create(user);
		flushSession();

		assertEquals(++count, countRowsInTable(Subject.TABLE_NAME));
		assertEquals(USERNAME, name);

		user = userDao.read(User.class, USERNAME);

		userDao.delete(user);
		flushSession();

		assertEquals(--count, countRowsInTable(Subject.TABLE_NAME));
	}

	@Test
	public void shouldCreateReadAndDeleteSystemAccount()
	{
		SystemAccount systemAccount = new SystemAccount(new Date(), SYSTEMACCOUNTNAME);

		int count = countRowsInTable(Subject.TABLE_NAME);

		String name = userDao.create(systemAccount);
		flushSession();

		assertEquals(++count, countRowsInTable(Subject.TABLE_NAME));
		assertEquals(SYSTEMACCOUNTNAME, name);

		systemAccount = userDao.read(SystemAccount.class, SYSTEMACCOUNTNAME);

		userDao.delete(systemAccount);
		flushSession();

		assertEquals(--count, countRowsInTable(Subject.TABLE_NAME));
	}

	@Test
	public void dateShouldNotBeSqlSubclass()
	{
		User user = new User(new Date(), USERNAME, Locale.ENGLISH);
		defaultPermissions(user);
		userDao.create(user);
		flushSession(); // FIXME this should force a new session

		user = userDao.read(User.class, USERNAME);
		Date createdAt = user.getCreatedAt();
		assertTrue(createdAt instanceof Date);
		assertFalse(createdAt instanceof java.sql.Date);
		assertFalse(createdAt instanceof java.sql.Time);
		assertFalse(createdAt instanceof java.sql.Timestamp);
	}

	@Test
	public void shouldAutoCreateAndDeleteHolidays()
	{
		user1.getUserHolidays().add(new UserHolidays(new Date(), user1, new Date(), new Date()));

		int countUserHolidays = countRowsInTable(UserHolidays.TABLE_NAME);
		flushSession();

		assertEquals(++countUserHolidays, countRowsInTable(UserHolidays.TABLE_NAME));

		user1.getUserHolidays().remove(user1.getUserHolidays().iterator().next());
		flushSession();

		assertEquals(--countUserHolidays, countRowsInTable(UserHolidays.TABLE_NAME));
	}

	@Test
	public void shouldAutoCreateAndDeleteRole()
	{
		user1.getUserRoles().add(new UserRole(user1, Role.USER));

		int count = countRowsInTable(UserRole.TABLE_NAME);
		flushSession();

		assertEquals(++count, countRowsInTable(UserRole.TABLE_NAME));

		user1.getUserRoles().remove(user1.getUserRoles().iterator().next());
		flushSession();

		assertEquals(--count, countRowsInTable(UserRole.TABLE_NAME));
	}
}
