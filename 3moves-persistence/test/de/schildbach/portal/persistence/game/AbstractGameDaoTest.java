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

package de.schildbach.portal.persistence.game;

import static org.junit.Assert.assertEquals;

import java.util.Date;

import org.junit.Before;
import org.springframework.beans.factory.annotation.Autowired;

import de.schildbach.portal.persistence.AbstractDaoTest;

/**
 * @author Andreas Schildbach
 */
public abstract class AbstractGameDaoTest extends AbstractDaoTest
{
	protected Game game;

	@Autowired
	private GameDao gameDao;

	@Before
	public void setupGame()
	{
		game = gameDao.newSingleGame(user1, new Date(), Rules.CHESS, "", Aid.NONE);
		gameDao.create(game);

		flushSession();

		assertEquals(1, countRowsInTable("games"));
	}
}
