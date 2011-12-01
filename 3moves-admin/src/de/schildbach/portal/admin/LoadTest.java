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

import java.util.Arrays;
import java.util.List;
import java.util.Locale;

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
import de.schildbach.portal.service.game.GameRulesHelper;

/**
 * @author Andreas Schildbach
 */
public class LoadTest
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

		loadTest();

		tm.rollback(transaction);
	}

	private static void loadTest()
	{
		System.out.println("Preparing LoadTest...");
		List<Object[]> dump = gameDao.dumpSingleGames(null, 0);

		int lineCount = 0;

		System.out.println("Starting LoadTest...");
		long start = System.currentTimeMillis();

		for (Object[] entry : dump)
		{
			// print life marker
			lineCount++;
			if (lineCount % 1000 == 0)
			{
				System.out.println(lineCount + " games testing... [rules: " + GameRules.instanceCount + ", geometries: "
						+ BoardGeometry.instanceCount + ", coordinates: " + Coordinate.instanceCount + ", boards: " + Board.instanceCount
						+ ", moves: " + GameMove.instanceCount + ", pieceSets: " + PieceSet.instanceCount + ", pieces: " + Piece.instanceCount
						+ ", time: " + (System.currentTimeMillis() - start) + " ms]");
			}

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

				String recalculatedMarshalledGame = rules.marshal(game);
				if (!marshalledGame.equals(recalculatedMarshalledGame))
				{
					error(id, "marshalled game mismatch:\n  " + marshalledGame + "\n  " + recalculatedMarshalledGame, entry);
				}

				String recalculatedHistoryNotation = rules.formatGame(game, Locale.ENGLISH);
				if (!historyNotation.equals(recalculatedHistoryNotation))
				{
					error(id, "history mismatch:\n  " + historyNotation + "\n  " + recalculatedHistoryNotation, entry);
				}

				String recalculatedPositionNotation = rules.formatPosition(game.getActualPosition());
				if (!positionNotation.equals(recalculatedPositionNotation))
				{
					error(id, "actual position mismatch:\n  " + positionNotation + "\n  " + recalculatedPositionNotation, entry);
				}

				String recalculatedLastMoveNotation = game.getLastMove() != null ? rules.formatMove(game.getLastMove()) : null;
				if (!ObjectUtils.equals(lastMoveNotation, recalculatedLastMoveNotation))
				{
					error(id, "last move mismatch:\n  " + lastMoveNotation + "\n  " + recalculatedLastMoveNotation, entry);
				}
			}
			catch (RuntimeException x)
			{
				error(id, x, entry);
			}
		}

		System.out.println("Finished LoadTest in " + (System.currentTimeMillis() - start) / 1000 + " seconds");
	}

	private static void error(int id, String message, Object[] entry)
	{
		System.out.println(message);
		System.out.println("    " + Arrays.toString(entry));
	}

	private static void error(int id, Throwable x, Object[] entry)
	{
		error(id, x.getClass().getName(), entry);
		x.printStackTrace();
	}
}
