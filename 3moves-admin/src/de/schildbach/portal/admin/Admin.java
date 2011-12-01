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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.Iterator;
import java.util.Locale;

import org.apache.commons.lang.ObjectUtils;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;

import de.schildbach.game.GameMove;
import de.schildbach.game.GamePosition;
import de.schildbach.game.GameRules;
import de.schildbach.game.GameRules.FormatGameArrayElement;
import de.schildbach.game.exception.ParseException;
import de.schildbach.portal.persistence.game.GameDao;
import de.schildbach.portal.persistence.game.GamePlayer;
import de.schildbach.portal.persistence.game.SingleGame;
import de.schildbach.portal.service.game.GameRulesHelper;

/**
 * @author Andreas Schildbach
 */
public class Admin
{
	private static final String[] CONTEXT_LOCATIONS = { "classpath:de/schildbach/portal/admin/integrationContext.xml",
			"classpath:de/schildbach/portal/persistence/dataAccessObjectContext.xml",
			"classpath:de/schildbach/portal/persistence/sessionFactoryContext.xml" };

	public static void main(String[] args) throws Exception
	{
		ClassPathXmlApplicationContext context = new ClassPathXmlApplicationContext(CONTEXT_LOCATIONS);

		GameDao gameDao = (GameDao) context.getBean("gameDao");
		PlatformTransactionManager tm = (PlatformTransactionManager) context.getBean("transactionManager");

		BufferedReader input = new BufferedReader(new InputStreamReader(System.in));

		int gameId = 0;

		while (true)
		{
			System.out.print("> ");
			String line = input.readLine();
			if (line.length() == 0)
				continue;

			char firstChar = Character.toLowerCase(line.charAt(0));
			if (firstChar == 'x')
			{
				break;
			}
			else
			{
				if (firstChar == 'd')
				{
					if (gameId == 0)
					{
						System.out.println("select a game first!");
					}
					else
					{
						TransactionStatus transaction = tm.getTransaction(new DefaultTransactionDefinition());

						SingleGame game = gameDao.read(SingleGame.class, gameId);
						System.out.println("details for " + game.getId());
						printActual(game);

						tm.rollback(transaction);
					}
				}
				else if (firstChar == 'h')
				{
					if (gameId == 0)
					{
						System.out.println("select a game first!");
					}
					else
					{
						TransactionStatus transaction = tm.getTransaction(new DefaultTransactionDefinition());

						SingleGame game = gameDao.read(SingleGame.class, gameId);
						System.out.println("history for " + game.getId());
						printHistory(game);

						tm.rollback(transaction);
					}
				}
				else if (firstChar == 'r')
				{
					if (gameId == 0)
					{
						System.out.println("select a game first!");
					}
					else
					{
						TransactionStatus transaction = tm.getTransaction(new DefaultTransactionDefinition());

						SingleGame game = gameDao.read(SingleGame.class, gameId);
						System.out.println("repair " + game.getId());
						repair(game);

						tm.commit(transaction);
					}
				}
				else if (firstChar == 't')
				{
					if (gameId == 0)
					{
						System.out.println("select a game first!");
					}
					else
					{
						TransactionStatus transaction = tm.getTransaction(new DefaultTransactionDefinition());

						SingleGame game = gameDao.read(SingleGame.class, gameId);
						System.out.println("truncate " + game.getId());
						truncate(game);

						tm.commit(transaction);
					}
				}
				else
				{
					TransactionStatus transaction = tm.getTransaction(new DefaultTransactionDefinition());

					try
					{
						gameId = Integer.parseInt(line);
						printActual(gameDao.read(SingleGame.class, gameId));
					}
					catch (NumberFormatException x)
					{
						System.out.println("unknown command");
					}

					tm.rollback(transaction);
				}
			}
		}

		System.out.println("exit");
	}

	private static void printActual(SingleGame hGame)
	{
		try
		{
			System.out.println("id:        " + hGame.getId());
			System.out.println("state:     " + hGame.getState() + (hGame.getResolution() != null ? ", " + hGame.getResolution() : ""));
			System.out.print("players:   ");
			for (GamePlayer player : hGame.getPlayers())
				System.out.print(player.getSubject().getName() + "(" + player.getId() + ") ");
			System.out.println();
			GamePlayer activePlayer = hGame.getActivePlayer();
			System.out.println("active:    " + (activePlayer != null ? activePlayer.getSubject().getName() : "-"));

			GameRules rules = GameRulesHelper.rules(hGame);
			String marshalledGame = hGame.getMarshalledGame();
			de.schildbach.game.Game game = rules.unmarshal(null, marshalledGame);

			String recalcMarshalledGame = rules.marshal(game);
			System.out.println("marshalled:" + marshalledGame);
			System.out.println("   recalc:" + recalcMarshalledGame);
			if (!ObjectUtils.equals(marshalledGame, recalcMarshalledGame))
				System.out.println(" !! diff !!");

			String historyNotation = hGame.getHistoryNotation();
			String recalcHistoryNotation = rules.formatGame(game, Locale.ENGLISH);
			System.out.println("history:   " + historyNotation);
			System.out.println("   recalc: " + recalcHistoryNotation);
			if (!ObjectUtils.equals(historyNotation, recalcHistoryNotation))
				System.out.println(" !! diff !!");

			String positionNotation = hGame.getPositionNotation();
			String recalcPositionNotation = rules.formatPosition(game.getActualPosition());
			System.out.println("position:  " + positionNotation);
			System.out.println("   recalc: " + recalcPositionNotation);
			if (!ObjectUtils.equals(positionNotation, recalcPositionNotation))
				System.out.println(" !! diff !!");

			String lastMoveNotation = hGame.getLastMoveNotation();
			String recalcLastMoveNotation = game.getLastMove() != null ? rules.formatMove(game.getLastMove()) : null;
			System.out.println("last move: " + lastMoveNotation);
			System.out.println("   recalc: " + recalcLastMoveNotation);
			if (!ObjectUtils.equals(lastMoveNotation, recalcLastMoveNotation))
				System.out.println(" !! diff !!");
		}
		catch (ParseException x)
		{
			System.out.println(x);
		}
	}

	private static void printHistory(SingleGame hGame)
	{
		GameRules rules = GameRulesHelper.rules(hGame);
		de.schildbach.game.Game game = rules.unmarshal(null, hGame.getMarshalledGame());

		Iterator<FormatGameArrayElement> formatElements = Arrays.asList(rules.formatGameArray(game, Locale.ENGLISH)).iterator();

		for (int i = 0; i < game.getSize(); i++)
		{
			GameMove move = game.getMove(i);
			GamePosition position = game.getPosition(i);
			FormatGameArrayElement formatElement;
			do
			{
				formatElement = formatElements.next();
			}
			while (formatElement.getIndex() == null);

			System.out.format("  %-16s %-16s %s\n", formatElement.getNotation(), rules.formatMove(move), rules.formatPosition(position));
		}
	}

	private static void repair(SingleGame hGame)
	{
		GameRules rules = GameRulesHelper.rules(hGame);
		String marshalledGame = hGame.getMarshalledGame();
		de.schildbach.game.Game game = rules.unmarshal(null, marshalledGame);

		String recalcMarshalledGame = rules.marshal(game);
		System.out.println("marshalled:" + marshalledGame);
		System.out.println("   recalc:" + recalcMarshalledGame);
		if (!ObjectUtils.equals(marshalledGame, recalcMarshalledGame))
		{
			if (ask("repair? "))
			{
				hGame.setMarshalledGame(recalcMarshalledGame);
				System.out.println("setting marshalled game to " + recalcMarshalledGame);
			}
		}

		String historyNotation = hGame.getHistoryNotation();
		String recalcHistoryNotation = rules.formatGame(game, Locale.ENGLISH);
		System.out.println("history:   " + historyNotation);
		System.out.println("   recalc: " + recalcHistoryNotation);
		if (!ObjectUtils.equals(historyNotation, recalcHistoryNotation))
		{
			if (ask("repair? "))
			{
				hGame.setHistoryNotation(recalcHistoryNotation);
				System.out.println("setting history to " + recalcHistoryNotation);
			}
		}

		String positionNotation = hGame.getPositionNotation();
		String recalcPositionNotation = rules.formatPosition(game.getActualPosition());
		System.out.println("position:  " + positionNotation);
		System.out.println("   recalc: " + recalcPositionNotation);
		if (!ObjectUtils.equals(positionNotation, recalcPositionNotation))
		{
			if (ask("repair? "))
			{
				hGame.setPositionNotation(recalcPositionNotation);
				System.out.println("setting position to " + recalcPositionNotation);
			}
		}

		String lastMoveNotation = hGame.getLastMoveNotation();
		String recalcLastMoveNotation = game.getLastMove() != null ? rules.formatMove(game.getLastMove()) : null;
		System.out.println("last move: " + lastMoveNotation);
		System.out.println("   recalc: " + recalcLastMoveNotation);
		if (!ObjectUtils.equals(lastMoveNotation, recalcLastMoveNotation))
		{
			if (ask("repair? "))
			{
				hGame.setLastMoveNotation(recalcLastMoveNotation);
				System.out.println("setting last move to " + recalcLastMoveNotation);
			}
		}
	}

	private static void truncate(SingleGame hGame)
	{
		// TODO this doesn't really make sense any longer, as long as unmarshalGame() does not return any fine-grained
		// parse errors

		GameRules rules = GameRulesHelper.rules(hGame);
		de.schildbach.game.Game game = rules.newGame(null);
		String error = rules.executeMoves(game, hGame.getHistoryNotation(), Locale.ENGLISH);
		if (error == null)
		{
			System.out.println("nothing to truncate!");
			return;
		}

		System.out.println("Error: could not parse first move of " + error);

		String recalcMarshalledGame = rules.marshal(game);
		String recalcHistoryNotation = rules.formatGame(game, Locale.ENGLISH);
		String recalcPositionNotation = rules.formatPosition(game.getActualPosition());
		String recalcLastMoveNotation = game.getLastMove() != null ? rules.formatMove(game.getLastMove()) : null;

		System.out.println("persisted: " + hGame.getHistoryNotation());
		System.out.println("truncated: " + recalcHistoryNotation);

		if (ask("repair? "))
		{
			hGame.setMarshalledGame(recalcMarshalledGame);
			System.out.println("setting marshalled game to " + recalcMarshalledGame);
			hGame.setHistoryNotation(recalcHistoryNotation);
			System.out.println("setting history to " + recalcHistoryNotation);
			hGame.setPositionNotation(recalcPositionNotation);
			System.out.println("setting position to " + recalcPositionNotation);
			hGame.setLastMoveNotation(recalcLastMoveNotation);
			System.out.println("setting last move to " + recalcLastMoveNotation);
		}
	}

//  private void recalculatePoints()
//	{
//		game.accept(new GameVisitor()
//		{
//			public void visit(SingleGame singleGame)
//			{
//				if (singleGame.getState() == GameState.FINISHED)
//				{
//					GameRules rules = GameRules.getRules(singleGame.getRules(), singleGame.getVariant());
//					de.schildbach.game.Game game = rules.newGame(singleGame.getHistoryNotation(), Locale.ENGLISH);
//					float[] points;
//					if (singleGame.getWinner() != null)
//						if (singleGame.getResolution() == GameResolution.WIN)
//							points = rules.points(game);
//						else
//							points = rules.pointsForWin(singleGame.getWinner().getPosition());
//					else
//						points = rules.pointsForDraw();
//					persistPoints(singleGame, points);
//				}
//			}
//
//			public void visit(GameGroup hGroup)
//			{
//				for (SingleGame singleGame : hGroup.getChildGames())
//				{
//					if (singleGame.getState() == GameState.FINISHED)
//					{
//						GameRules rules = GameRules.getRules(singleGame.getRules(), singleGame.getVariant());
//						de.schildbach.game.Game game = rules.newGame(singleGame.getHistoryNotation(), Locale.ENGLISH);
//						float[] points;
//						if (singleGame.getWinner() != null)
//							if (singleGame.getResolution() == GameResolution.WIN)
//								points = rules.points(game);
//							else
//								points = rules.pointsForWin(singleGame.getWinner().getPosition());
//						else
//							points = rules.pointsForDraw();
//						persistPoints(singleGame, points);
//					}
//				}
//				float[] points = determineGameGroupPoints(hGroup);
//				persistPoints(hGroup, points);
//			}
//		});
//	}

	private static boolean ask(String string)
	{
		System.out.print(string);
		BufferedReader input = new BufferedReader(new InputStreamReader(System.in));
		try
		{
			String line = input.readLine();
			return line.equals("y");
		}
		catch (IOException e)
		{
			System.out.println(e);
			return false;
		}
	}
}
