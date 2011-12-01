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

package de.schildbach.portal.service;

import static junit.framework.Assert.assertNotNull;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import de.schildbach.portal.service.content.ContentService;
import de.schildbach.portal.service.game.GameEventService;
import de.schildbach.portal.service.game.GameService;
import de.schildbach.portal.service.mail.MailService;
import de.schildbach.portal.service.user.PresenceService;
import de.schildbach.portal.service.user.UserEventService;
import de.schildbach.portal.service.user.UserService;

/**
 * @author Andreas Schildbach
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "classpath:de/schildbach/portal/service/testContext.xml",
		"classpath:de/schildbach/portal/message/messageContext.xml", "classpath:de/schildbach/portal/service/serviceContext.xml" })
public class ContextIntegrationTest
{
	@Autowired
	private UserService userService;

	@Autowired
	private UserEventService userEventService;

	@Autowired
	private PresenceService presenceService;

	@Autowired
	private MailService mailService;

	@Autowired
	private ContentService contentService;

	@Autowired
	private GameService gameService;

	@Autowired
	private GameEventService gameEventService;

	@Test
	public void test()
	{
		assertNotNull(userService);
		assertNotNull(userEventService);
		assertNotNull(presenceService);
		assertNotNull(mailService);
		assertNotNull(contentService);
		assertNotNull(gameService);
		assertNotNull(gameEventService);
	}
}
