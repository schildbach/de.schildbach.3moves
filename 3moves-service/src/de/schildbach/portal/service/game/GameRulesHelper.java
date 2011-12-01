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

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Random;

import de.schildbach.game.Board;
import de.schildbach.game.Game;
import de.schildbach.game.GamePosition;
import de.schildbach.game.GameRules;
import de.schildbach.game.checkers.CheckersRules;
import de.schildbach.game.chess.ChessRules;
import de.schildbach.game.common.ChessLikeRules;
import de.schildbach.game.common.ChessLikeRules.CheckState;
import de.schildbach.game.dragonchess.DragonchessRules;
import de.schildbach.game.go.GoRules;
import de.schildbach.portal.persistence.game.GameConditionalMoves;
import de.schildbach.portal.persistence.game.GameGroup;
import de.schildbach.portal.persistence.game.GameHelper;
import de.schildbach.portal.persistence.game.Rules;
import de.schildbach.portal.persistence.game.SingleGame;

/**
 * Contains glue logic between persistence and game.
 * 
 * @author Andreas Schildbach
 */
public class GameRulesHelper
{
	private static Map<Rules, GameRules> rulesMap = new HashMap<Rules, GameRules>();

	/**
	 * start new game
	 */
	public static Game newGame(Rules rules)
	{
		GameRules gameRules = rules(rules);
		if (rulesForceInitialBoard(rules))
		{
			int boardId = new Random().nextInt(960);
			return gameRules.newGame(gameRules.formatBoard(Chess960Helper.getBoardById(boardId)));
		}
		else
		{
			return gameRules.newGame(null);
		}
	}

	/**
	 * start new game with an initial history
	 */
	public static Game newGame(Rules rules, String initialHistory)
	{
		Game game = newGame(rules);
		rules(rules).executeMoves(game, initialHistory, Locale.ENGLISH);
		return game;
	}

	/**
	 * load game from database record
	 */
	public static Game game(SingleGame singleGame)
	{
		return rules(singleGame).unmarshal(singleGame.getInitialBoardNotation(), singleGame.getMarshalledGame());
	}

	/**
	 * load game from conditional moves
	 */
	public static Game game(GameConditionalMoves conditionalMoves)
	{
		SingleGame singleGame = (SingleGame) conditionalMoves.getGamePlayer().getGame();
		return rules(singleGame).unmarshal(singleGame.getInitialBoardNotation(), conditionalMoves.getMarshalledMoves());
	}

	/**
	 * load game from database record, but use alternate history
	 */
	public static Game game(SingleGame singleGame, String history)
	{
		GameRules rules = rules(singleGame);
		Game game = rules.newGame(singleGame.getInitialBoardNotation());
		rules.executeMoves(game, history, Locale.ENGLISH);
		return game;
	}

	public static Game gameFromInitialHistory(SingleGame singleGame)
	{
		GameRules rules = rules(singleGame);
		Game game = rules.newGame(singleGame.getInitialBoardNotation());
		rules.executeMoves(game, singleGame.getInitialHistoryNotation(), Locale.ENGLISH);
		return game;
	}

	public static Game gameFromChildInitialHistory(GameGroup gameGroup)
	{
		GameRules rules = rulesFromChildRules(gameGroup);
		Game game = rules.newGame(null);
		rules.executeMoves(game, gameGroup.getChildInitialHistory(), Locale.ENGLISH);
		return game;
	}

	public static void update(SingleGame singleGame, Game game)
	{
		GameRules rules = GameRulesHelper.rules(singleGame);

		// update primary notation
		singleGame.setMarshalledGame(rules.marshal(game));

		// update secondary notations
		singleGame.setHistoryNotation(rules.formatGame(game, Locale.ENGLISH));
		singleGame.setPositionNotation(rules.formatPosition(game.getActualPosition()));
		singleGame.setLastMoveNotation(game.getLastMove() != null ? rules.formatMove(game.getLastMove()) : null);
	}

	public static GameRules rules(SingleGame singleGame)
	{
		return rules(singleGame.getRules());
	}

	public static GameRules rulesFromChildRules(GameGroup gameGroup)
	{
		return rules(gameGroup.getChildRules());
	}

	/**
	 * caching factory for game rules
	 */
	public static GameRules rules(Rules rules)
	{
		if (rulesForceInitialBoard(rules))
			rules = Rules.CHESS;

		synchronized (rulesMap)
		{
			GameRules gameRules = rulesMap.get(rules);

			if (gameRules == null)
			{
				gameRules = newRules(rules);
				rulesMap.put(rules, gameRules);
			}

			return gameRules;
		}
	}

	private static GameRules newRules(Rules rules)
	{
		String[] s = rules.name().split("_", 2);
		String variant = s.length > 1 ? s[1] : null;

		if ("CHESS".equals(s[0]))
			return new ChessRules(variant != null ? ChessRules.Variant.valueOf(variant) : null);
		else if ("CHECKERS".equals(s[0]))
			return new CheckersRules(variant != null ? CheckersRules.Variant.valueOf(variant) : null);
		else if ("DRAGONCHESS".equals(s[0]))
			return new DragonchessRules();
		else if ("GO".equals(s[0]))
			return new GoRules(variant != null ? GoRules.Variant.valueOf(variant) : null);
		else
			throw new IllegalArgumentException("illegal rules: " + s[0]);
	}

	public static boolean rulesForceInitialBoard(Rules rules)
	{
		return rules == Rules.CHESS_960;
	}

	public static List<String> specialAttributes(Game game, GameRules rules)
	{
		List<String> attributes = new LinkedList<String>();

		if (rules instanceof ChessLikeRules)
		{
			ChessLikeRules chessLikeRules = (ChessLikeRules) rules;

			GamePosition position = game.getActualPosition();
			Board initialBoard = game.getInitialPosition().getBoard();

			CheckState checkState = chessLikeRules.checkState(position, initialBoard);
			if (checkState != null)
				attributes.add(checkState.name().toLowerCase());
		}

		if (rules instanceof CheckersRules)
		{
			if (rules.allowedMoves(game).isEmpty())
				attributes.add("cannot_move");
		}

		return attributes;
	}

	public static boolean isBeta(SingleGame singleGame)
	{
		return isBeta(singleGame.getRules());
	}

	public static boolean isBeta(GameGroup gameGroup)
	{
		return isBeta(gameGroup.getChildRules());
	}

	private static boolean isBeta(Rules rules)
	{
		return rules == Rules.GO_CAPTURE;
	}

	public static int minPlayers(de.schildbach.portal.persistence.game.Game game)
	{
		GameHelper helper = new GameHelper(game);
		if (helper.isSingleGame())
			return 2;
		else
			return helper.getGameGroup().getMinPlayers();
	}

	public static int maxPlayers(de.schildbach.portal.persistence.game.Game game)
	{
		GameHelper helper = new GameHelper(game);
		if (helper.isSingleGame())
			return 2;
		else
			return helper.getGameGroup().getMaxPlayers();
	}
}
