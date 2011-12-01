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

package de.schildbach.portal.persistence;

import static org.junit.Assert.assertEquals;

import java.util.Date;
import java.util.Locale;

import org.hibernate.SessionFactory;
import org.junit.Before;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.AbstractTransactionalJUnit4SpringContextTests;

import de.schildbach.portal.persistence.user.User;
import de.schildbach.portal.persistence.user.UserDao;

/**
 * @author Andreas Schildbach
 */
@ContextConfiguration(locations = { "classpath:de/schildbach/portal/persistence/dataAccessObjectContext.xml",
		"classpath:de/schildbach/portal/persistence/testSessionFactoryContext.xml",
		"classpath:de/schildbach/portal/persistence/testDataSourceContext.xml" })
public abstract class AbstractDaoTest extends AbstractTransactionalJUnit4SpringContextTests
{
	protected User user1;
	protected User user2;

	@Autowired
	private UserDao userDao;

	@Autowired
	private SessionFactory sessionFactory;

	@Before
	public void setupDatabase()
	{
		user1 = new User(new Date(), "user1", Locale.ENGLISH);
		defaultPermissions(user1);
		userDao.create(user1);

		user2 = new User(new Date(), "user2", Locale.ENGLISH);
		defaultPermissions(user2);
		userDao.create(user2);

		flushSession();

		assertEquals(2, countRowsInTable("subjects"));
	}

	protected final void defaultPermissions(User user)
	{
		// should be defaults
		user.setFullNamePermission("friend");
		user.setAgePermission("user");
		user.setCityPermission("user");
		user.setCountryPermission("user");
		user.setOccupationPermission("user");
	}

	protected final void flushSession()
	{
		sessionFactory.getCurrentSession().flush();
	}
}
