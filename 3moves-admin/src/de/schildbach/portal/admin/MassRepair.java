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

import java.util.LinkedList;
import java.util.List;

import org.apache.commons.lang.ObjectUtils;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;

import de.schildbach.game.Board;
import de.schildbach.game.BoardGeometry;
import de.schildbach.game.Coordinate;
import de.schildbach.game.GameMove;
import de.schildbach.game.GameRules;
import de.schildbach.game.Piece;
import de.schildbach.game.PieceSet;
import de.schildbach.portal.persistence.game.GameDao;
import de.schildbach.portal.persistence.game.GameResolution;
import de.schildbach.portal.persistence.game.GameState;
import de.schildbach.portal.persistence.game.Rules;
import de.schildbach.portal.persistence.game.SingleGame;
import de.schildbach.portal.service.game.GameRulesHelper;

/**
 * @author Andreas Schildbach
 */
public class MassRepair
{
	private static final String[] CONTEXT_LOCATIONS = { "classpath:de/schildbach/portal/admin/integrationContext.xml",
			"classpath:de/schildbach/portal/persistence/dataAccessObjectContext.xml",
			"classpath:de/schildbach/portal/persistence/sessionFactoryContext.xml" };

	private static GameDao gameDao;

	public static void main(String[] args) throws Exception
	{
		ClassPathXmlApplicationContext context = new ClassPathXmlApplicationContext(CONTEXT_LOCATIONS);

		gameDao = (GameDao) context.getBean("gameDao");
		PlatformTransactionManager tm = (PlatformTransactionManager) context.getBean("transactionManager");

		TransactionStatus transaction = tm.getTransaction(new DefaultTransactionDefinition());

		List<Integer> gamesToRepair = gamesToRepair();
		System.out.println(gamesToRepair.size() + " games to repair");

		for (int id : gamesToRepair)
		{
			repair(id);
		}

		System.out.println("finishing...");

		tm.rollback(transaction);

		System.out.println("finished");
	}

	private static void repair(int id)
	{
		SingleGame hGame = gameDao.read(SingleGame.class, id);
		GameRules rules = GameRulesHelper.rules(hGame);
		de.schildbach.game.Game game = rules.unmarshal(null, hGame.getMarshalledGame());

		System.out.println("== " + hGame.getId() + " ==");
		String lastMoveNotation = hGame.getLastMoveNotation();
		System.out.println("last move:       " + lastMoveNotation);
		if (game.getLastMove() == null || lastMoveNotation == null)
		{
			System.out.println("SKIPPING: null");
			return;
		}
		String expectedLastMoveNotation = rules.formatMove(game.getLastMove());
		System.out.println("expected move:   " + expectedLastMoveNotation);
		if (expectedLastMoveNotation.length() != 7 || lastMoveNotation.length() != 7)
		{
			System.out.println("SKIPPING: len != 7");
			return;
		}
		if (expectedLastMoveNotation.equals(lastMoveNotation))
		{
			System.out.println("SKIPPING: nothing to do");
			return;
		}
		if (lastMoveNotation.charAt(3) != 'x' || expectedLastMoveNotation.charAt(3) != '-')
		{
			System.out.println("SKIPPING: will not try to repair");
			return;
		}

		StringBuilder repaired = new StringBuilder(lastMoveNotation);
		repaired.setCharAt(3, '-');
		System.out.println("trying to repair: " + repaired);

		if (expectedLastMoveNotation.equals(repaired.toString()))
		{
			System.out.println("SUCCEEDED! writing...");
			hGame.setLastMoveNotation(rules.formatMove(game.getLastMove()));
		}
		else
		{
			System.out.println("FAILED!");
		}
	}

	private static List<Integer> gamesToRepair()
	{
		System.out.println("preparing...");
		List<Integer> gamesToRepair = new LinkedList<Integer>();

		List<Object[]> dump = gameDao.dumpSingleGames(null, 0);
		System.out.println("starting...");

		int lineCount = 0;

		for (Object[] entry : dump)
		{
			// print life marker
			lineCount++;
			if (lineCount % 1000 == 0)
				System.out.println(lineCount + " games testing... [rules: " + GameRules.instanceCount + ", geometries: "
						+ BoardGeometry.instanceCount + ", coordinates: " + Coordinate.instanceCount + ", boards: " + Board.instanceCount
						+ ", moves: " + GameMove.instanceCount + ", pieceSets: " + PieceSet.instanceCount + ", pieces: " + Piece.instanceCount + "]");

			int id = (Integer) entry[0];
			GameRules rules = GameRulesHelper.rules(Rules.valueOf((String) entry[1]));
			String initialBoardNotation = (String) entry[2];
			String marshalledGame = (String) entry[3];
			String historyNotation = (String) entry[4];
			String positionNotation = (String) entry[5];
			String lastMoveNotation = (String) entry[6];
			GameState state = (GameState) entry[7];
			GameResolution resolution = (GameResolution) entry[8];

			try
			{
				de.schildbach.game.Game game = rules.unmarshal(initialBoardNotation, marshalledGame);

				String recalculatedLastMoveNotation = game.getLastMove() != null ? rules.formatMove(game.getLastMove()) : null;
				if (!ObjectUtils.equals(lastMoveNotation, recalculatedLastMoveNotation))
				{
					error(id, "last move mismatch:\n  " + lastMoveNotation + "\n  " + recalculatedLastMoveNotation, entry);
					gamesToRepair.add(id);
				}
			}
			catch (RuntimeException x)
			{
				error(id, x, entry);
			}
		}

		return gamesToRepair;
	}

	private static void error(int id, String message, Object[] entry)
	{
		String line = id + ": " + message;
		System.out.println(line);
	}

	private static void error(int id, Throwable x, Object[] entry)
	{
		error(id, x.getClass().getName(), entry);
		x.printStackTrace();
	}
}
