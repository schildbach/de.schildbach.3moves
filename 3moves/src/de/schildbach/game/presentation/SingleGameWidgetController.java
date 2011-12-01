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
import org.springframework.web.bind.ServletRequestUtils;
import org.springframework.web.servlet.HandlerMapping;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.ParameterizableViewController;

import de.schildbach.game.Game;
import de.schildbach.game.GamePosition;
import de.schildbach.game.GameRules;
import de.schildbach.game.presentation.board.BaseBoardController;
import de.schildbach.portal.persistence.game.SingleGame;
import de.schildbach.portal.service.game.GameRulesHelper;
import de.schildbach.portal.service.game.GameService;

/**
 * @author Andreas Schildbach
 */
@Controller
public class SingleGameWidgetController extends ParameterizableViewController
{
	@SuppressWarnings("unused")
	protected static final Log LOG = LogFactory.getLog(SingleGameWidgetController.class);

	private GameService gameService;

	@Required
	public void setGameService(GameService gameService)
	{
		this.gameService = gameService;
	}

	@Override
	protected ModelAndView handleRequestInternal(HttpServletRequest request, HttpServletResponse response) throws Exception
	{
		// parameters
		int gameId = Integer.parseInt((String) request.getAttribute(HandlerMapping.PATH_WITHIN_HANDLER_MAPPING_ATTRIBUTE));
		Integer move = ServletRequestUtils.getIntParameter(request, "move");

		// gather data
		SingleGame singleGame = (SingleGame) gameService.game(gameId);
		GameRules rules = GameRulesHelper.rules(singleGame);
		Game game = GameRulesHelper.game(singleGame);
		GamePosition position = (move == null) ? game.getActualPosition() : game.getPosition(move);

		// model for included view
		Map<String, Object> model = new HashMap<String, Object>();
		model.put(BaseBoardController.REQUEST_ATTRIBUTE_GAME_BOARD, position.getBoard());
		model.put(BaseBoardController.REQUEST_ATTRIBUTE_GAME_BOARD_GEOMETRY, rules.getBoardGeometry());
		model.put(BaseBoardController.REQUEST_ATTRIBUTE_GAME_BOARD_PIECESET, rules.getPieceSet());
		return new ModelAndView(getViewName(), model);
	}
}
