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
import java.util.Locale;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Required;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.ServletRequestUtils;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.ParameterizableViewController;

import de.schildbach.game.Game;
import de.schildbach.game.GameRules;
import de.schildbach.portal.persistence.game.GameState;
import de.schildbach.portal.persistence.game.SingleGame;
import de.schildbach.portal.service.game.GameRulesHelper;
import de.schildbach.portal.service.game.GameService;

/**
 * @author Andreas Schildbach
 */
@Controller
public class NotationController extends ParameterizableViewController
{
	private GameService gameService;

	@Required
	public void setGameService(GameService gameService)
	{
		this.gameService = gameService;
	}

	@Override
	protected ModelAndView handleRequestInternal(HttpServletRequest request, HttpServletResponse response) throws Exception
	{
		int gameId = ServletRequestUtils.getRequiredIntParameter(request, "game_id");
		SingleGame singleGame = (SingleGame) gameService.game(gameId);
		GameRules rules = GameRulesHelper.rules(singleGame);
		Game game = GameRulesHelper.game(singleGame);

		Map<String, Object> model = new HashMap<String, Object>();
		model.put("game", singleGame);
		model.put("result", getResultString(singleGame));
		model.put("notation_array", rules.formatGame(game, Locale.ENGLISH));
		return new ModelAndView(getViewName(), model);
	}

	private String getResultString(SingleGame game)
	{
		if (game.getState() == GameState.RUNNING)
			return "*";
		if (game.getResolution() == null)
			return "?";
		if (game.getResolution().equals("draw"))
			return "1/2-1/2";
		if (game.getWinner() == null)
			return "?";
		if (game.getWinner().equals(game.getPlayers().get(0)))
			return "1-0";
		if (game.getWinner().equals(game.getPlayers().get(1)))
			return "0-1";
		throw new IllegalStateException();
	}

}
