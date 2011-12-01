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

import static junit.framework.Assert.assertNotNull;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import de.schildbach.portal.persistence.content.ContentDao;
import de.schildbach.portal.persistence.game.GameDao;
import de.schildbach.portal.persistence.game.SubjectRatingDao;
import de.schildbach.portal.persistence.mail.MailDao;
import de.schildbach.portal.persistence.user.ImageDao;
import de.schildbach.portal.persistence.user.SubjectRelationDao;
import de.schildbach.portal.persistence.user.UserDao;

/**
 * @author Andreas Schildbach
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "classpath:de/schildbach/portal/persistence/testContext.xml",
		"classpath:de/schildbach/portal/persistence/dataAccessObjectContext.xml" })
public class ContextIntegrationTest
{
	@Autowired
	private UserDao userDao;

	@Autowired
	private SubjectRelationDao subjectRelationDao;

	@Autowired
	private ImageDao imageDao;

	@Autowired
	private MailDao mailDao;

	@Autowired
	private GameDao gameDao;

	@Autowired
	private SubjectRatingDao subjectRatingDao;

	@Autowired
	private ContentDao contentDao;

	@Test
	public void test()
	{
		assertNotNull(userDao);
		assertNotNull(subjectRelationDao);
		assertNotNull(imageDao);
		assertNotNull(mailDao);
		assertNotNull(gameDao);
		assertNotNull(subjectRatingDao);
		assertNotNull(contentDao);
	}
}
