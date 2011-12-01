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

package de.schildbach.portal.service.game;

import java.text.DateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.logging.LogManager;

import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.AbstractJUnit4SpringContextTests;

import de.schildbach.portal.persistence.game.SubjectRatingDao;
import de.schildbach.portal.persistence.user.User;
import de.schildbach.portal.service.Iso8601Format;
import de.schildbach.portal.service.mock.persistence.GameDaoMock;
import de.schildbach.portal.service.mock.persistence.UserDaoMock;
import de.schildbach.portal.service.user.UserEventServiceMock;
import de.schildbach.portal.service.user.UserService;

/**
 * @author Andreas Schildbach
 */
@ContextConfiguration(locations = "classpath:de/schildbach/portal/service/game/applicationContext.xml")
public abstract class AbstractGameServiceImplTest extends AbstractJUnit4SpringContextTests
{
	private static final Locale LOCALE = Locale.ENGLISH;

	@Autowired
	protected GameService gameService;

	@Autowired
	protected GameEventService gameEventService;

	@Autowired
	protected UserService userService;

	@Autowired
	protected UserEventServiceMock userEventService;

	@Autowired
	protected GameDaoMock gameDao;

	@Autowired
	protected SubjectRatingDao subjectRatingDao;

	@Autowired
	protected UserDaoMock userDao;

	protected Date createdAt;
	protected Date readyAt;
	protected Date startAt;

	@BeforeClass
	public static void setupLogging() throws Exception
	{
		LogManager.getLogManager().readConfiguration(AbstractGameServiceImplTest.class.getResourceAsStream("logging.properties"));
	}

	@Before
	public void setup() throws Exception
	{
		// dates
		DateFormat format = Iso8601Format.newDateTimeFormat();
		createdAt = format.parse("2004-03-03 12:00:00");
		readyAt = format.parse("2004-03-03 13:00:00");
		startAt = format.parse("2004-03-03 14:00:00");

		// add several users
		userDao.create(new User(createdAt, "owner", LOCALE));
		userDao.create(new User(createdAt, "user1", LOCALE));
		userDao.create(new User(createdAt, "user2", LOCALE));
		userDao.create(new User(createdAt, "user3", LOCALE));
		userDao.create(new User(createdAt, "user4", LOCALE));
		userDao.create(new User(createdAt, "unknown", LOCALE));
	}

	@After
	public void teardown()
	{
		gameEventService.processCollectedGameEvents();
		userEventService.reset();
		userDao.reset();
		gameDao.reset();
	}
}
