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

package de.schildbach.portal.admin;

import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;

import de.schildbach.portal.persistence.game.Game;
import de.schildbach.portal.persistence.game.GameDao;
import de.schildbach.portal.persistence.game.GameGroup;
import de.schildbach.portal.persistence.game.GamePlayer;
import de.schildbach.portal.persistence.game.GameVisitor;
import de.schildbach.portal.persistence.game.SingleGame;

/**
 * @author Andreas Schildbach
 */
public class GameTree
{
	private static final String[] CONTEXT_LOCATIONS = { "classpath:de/schildbach/portal/admin/integrationContext.xml",
			"classpath:de/schildbach/portal/persistence/dataAccessObjectContext.xml",
			"classpath:de/schildbach/portal/persistence/sessionFactoryContext.xml" };

	public static void main(String[] args) throws Exception
	{
		ClassPathXmlApplicationContext context = new ClassPathXmlApplicationContext(CONTEXT_LOCATIONS);

		GameDao gameDao = (GameDao) context.getBean("gameDao");
		PlatformTransactionManager tm = (PlatformTransactionManager) context.getBean("transactionManager");

		TransactionStatus transaction = tm.getTransaction(new DefaultTransactionDefinition());

		Game game = gameDao.read(Integer.parseInt(args[0]));

		print(game);

		tm.rollback(transaction);
	}

	private static void print(Game game)
	{
		game.accept(new GameVisitor()
		{
			public void visit(SingleGame singleGame)
			{
				GameGroup parent = singleGame.getParentGame();
				if (parent != null)
					print(parent);
				else
					System.out.println(singleGame.getId() + " [" + players(singleGame) + "]");
			}

			public void visit(GameGroup group)
			{
				System.out.println(group.getId() + " [" + players(group) + "] state:" + group.getState());

				for (SingleGame game : group.getChildGames())
				{
					System.out.println("  " + singleGame(game));
				}
			}
		});
	}

	private static String singleGame(SingleGame game)
	{
		StringBuilder str = new StringBuilder(game.getId() + " [" + players(game) + "] state:" + game.getState());
		str.append(" active:");
		if (game.getActivePlayer() != null)
			str.append(game.getActivePlayer().getSubject().getName());
		else
			str.append("<null>");
		return str.toString();
	}

	private static String players(Game game)
	{
		StringBuilder players = new StringBuilder();
		for (GamePlayer player : game.getPlayers())
			players.append(player.getSubject().getName()).append(",");
		players.setLength(players.length() - 1);
		return players.toString();
	}
}