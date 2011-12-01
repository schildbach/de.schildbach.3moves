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

import org.springframework.beans.factory.annotation.Required;
import org.springframework.stereotype.Controller;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.ParameterizableViewController;

import de.schildbach.portal.persistence.game.Game;
import de.schildbach.portal.persistence.game.GameGroup;
import de.schildbach.portal.persistence.game.GameState;
import de.schildbach.portal.persistence.game.GameVisitor;
import de.schildbach.portal.persistence.game.SingleGame;

/**
 * @author Andreas Schildbach
 */
@Controller
public class GameHeaderController extends ParameterizableViewController
{
	private GameViewHelper gameViewHelper;
	private String linkGame;

	@Required
	public void setGameViewHelper(GameViewHelper gameViewHelper)
	{
		this.gameViewHelper = gameViewHelper;
	}

	@Required
	public void setLinkGame(String linkGame)
	{
		this.linkGame = linkGame;
	}

	@Override
	protected ModelAndView handleRequestInternal(HttpServletRequest request, HttpServletResponse response) throws Exception
	{
		Game game = gameViewHelper.getGame(request);

		final Map<String, Object> model = new HashMap<String, Object>();
		game.accept(new GameVisitor()
		{
			public void visit(SingleGame game)
			{
				model.put("header_key", "game");
				model.put("game_tag", game.getRules());
				if (game.getParentGame() != null)
					model.put("parent_game_id", game.getParentGame().getId());
			}

			public void visit(GameGroup group)
			{
				model.put("header_key", "tournament");
				model.put("game_tag", group.getChildRules());
			}
		});
		model.put("game_id", game.getId());
		model.put("name", game.getName());
		if (game.getPlayers().size() == 2 && game.getState() != GameState.FORMING)
		{
			model.put("show_players", true);
			model.put("players", game.getPlayers());
		}
		model.put("state", game.getState());
		if (game.getState() == GameState.FINISHED)
		{
			model.put("resolution", game.getResolution());
			model.put("winner", game.getWinner());
		}

		model.put("link_game", request.getContextPath() + linkGame);
		return new ModelAndView(getViewName(), model);
	}
}
