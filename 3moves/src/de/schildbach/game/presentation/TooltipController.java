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

package de.schildbach.game.presentation;

import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.stereotype.Controller;
import org.springframework.web.servlet.HandlerMapping;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.ParameterizableViewController;

import de.schildbach.game.Coordinate;
import de.schildbach.game.Game;
import de.schildbach.game.GameMove;
import de.schildbach.game.GamePosition;
import de.schildbach.game.GameRules;
import de.schildbach.game.presentation.board.BaseBoardController;
import de.schildbach.game.presentation.board.AbstractBoardUIState.Cursor;
import de.schildbach.portal.persistence.game.GameGroup;
import de.schildbach.portal.persistence.game.GameState;
import de.schildbach.portal.persistence.game.GameVisitor;
import de.schildbach.portal.persistence.game.SingleGame;
import de.schildbach.portal.service.game.GameRulesHelper;
import de.schildbach.portal.service.game.GameService;

/**
 * @author Andreas Schildbach
 */
@Controller
public class TooltipController extends ParameterizableViewController
{
	@SuppressWarnings("unused")
	protected static final Log LOG = LogFactory.getLog(TooltipController.class);

	private GameService gameService;

	@Required
	public void setGameService(GameService gameService)
	{
		this.gameService = gameService;
	}

	@Override
	protected ModelAndView handleRequestInternal(HttpServletRequest request, HttpServletResponse response) throws Exception
	{
		final Map<String, Object> model = new HashMap<String, Object>();

		// parameters
		int gameId = Integer.parseInt((String) request.getAttribute(HandlerMapping.PATH_WITHIN_HANDLER_MAPPING_ATTRIBUTE));

		// gather data
		gameService.game(gameId).accept(new GameVisitor()
		{
			public void visit(SingleGame singleGame)
			{
				Game game;
				if (singleGame.getState() == GameState.FORMING || singleGame.getState() == GameState.UNACCOMPLISHED)
					game = GameRulesHelper.gameFromInitialHistory(singleGame);
				else
					game = GameRulesHelper.game(singleGame);
				GamePosition position = game.getActualPosition();
				GameRules rules = GameRulesHelper.rules(singleGame);

				// last move
				Map<Coordinate, Cursor> cursors = new HashMap<Coordinate, Cursor>();
				GameMove lastMove = game.getLastMove();
				if (lastMove != null)
				{
					for (String clickable : rules.clickables(lastMove))
					{
						Coordinate coordinate = rules.getBoardGeometry().locateCoordinate(clickable);
						if (coordinate != null)
							cursors.put(coordinate, Cursor.LAST);
					}
				}

				// model for included view
				model.put(BaseBoardController.REQUEST_ATTRIBUTE_GAME_BOARD, position.getBoard());
				model.put(BaseBoardController.REQUEST_ATTRIBUTE_GAME_BOARD_GEOMETRY, rules.getBoardGeometry());
				model.put(BaseBoardController.REQUEST_ATTRIBUTE_GAME_BOARD_PIECESET, rules.getPieceSet());
				model.put(BaseBoardController.REQUEST_ATTRIBUTE_GAME_BOARD_CURSORS, cursors);

				model.put("show_board", true);
			}

			public void visit(GameGroup gameGroup)
			{
				if (gameGroup.getState() == GameState.FORMING || gameGroup.getState() == GameState.UNACCOMPLISHED)
				{
					Game game = GameRulesHelper.gameFromChildInitialHistory(gameGroup);
					GamePosition position = game.getActualPosition();
					GameRules rules = GameRulesHelper.rulesFromChildRules(gameGroup);

					// model for included view
					model.put(BaseBoardController.REQUEST_ATTRIBUTE_GAME_BOARD, position.getBoard());
					model.put(BaseBoardController.REQUEST_ATTRIBUTE_GAME_BOARD_GEOMETRY, rules.getBoardGeometry());
					model.put(BaseBoardController.REQUEST_ATTRIBUTE_GAME_BOARD_PIECESET, rules.getPieceSet());

					model.put("show_board", true);
				}
				else
				{
					model.put("games", gameGroup.getChildGames());
				}
			}
		});

		return new ModelAndView(getViewName(), model);
	}
}
