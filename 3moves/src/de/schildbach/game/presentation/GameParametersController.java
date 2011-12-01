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

import java.util.Date;
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
import de.schildbach.portal.persistence.game.GameHelper;
import de.schildbach.portal.persistence.game.GameState;
import de.schildbach.portal.persistence.game.GameVisitor;
import de.schildbach.portal.persistence.game.SingleGame;
import de.schildbach.portal.service.game.GameRulesHelper;
import de.schildbach.presentation.DateUtils;
import de.schildbach.web.RequestTime;

/**
 * @author Andreas Schildbach
 */
@Controller
public class GameParametersController extends ParameterizableViewController
{
	private GameViewHelper gameViewHelper;

	@Required
	public void setGameViewHelper(GameViewHelper gameViewHelper)
	{
		this.gameViewHelper = gameViewHelper;
	}

	@Override
	protected ModelAndView handleRequestInternal(HttpServletRequest request, HttpServletResponse response) throws Exception
	{
		final Map<String, Object> model = new HashMap<String, Object>();

		Date now = RequestTime.get();

		String cssClass = request.getParameter("class");
		model.put("css_class", cssClass);

		Game game = gameViewHelper.getGame(request);
		GameHelper helper = new GameHelper(game);

		GameState state = game.getState();

		model.put("owner", game.getOwner());

		if (state == GameState.FORMING)
		{
			int minPlayers = GameRulesHelper.minPlayers(game);
			int maxPlayers = GameRulesHelper.maxPlayers(game);

			if (minPlayers != 2 || maxPlayers != 2)
			{
				if (minPlayers == maxPlayers)
					model.put("number_of_players", minPlayers);
				else
					model.put("number_of_players", minPlayers + " - " + maxPlayers);
			}
		}

		if ((state == GameState.FORMING || state == GameState.READY) && game.getOrderType() != null)
			model.put("order_type", game.getOrderType());

		if (state == GameState.FORMING || state == GameState.UNACCOMPLISHED)
			model.put("created_at", game.getCreatedAt());

		if ((state == GameState.FORMING || state == GameState.READY) && game.getReadyAt() != null)
		{
			model.put("ready_at", game.getReadyAt());
			if (now.after(game.getReadyAt()))
				model.put("ready_asap", true);
			else
				model.put("diff_ready_in", DateUtils.dateDiff(now, game.getReadyAt(), request.getLocale()));
		}

		if (state != GameState.RUNNING)
		{
			model.put("show_start_at", true);
			model.put("start_at", game.getStartedAt());
			if ((state == GameState.FORMING || state == GameState.READY) && game.getStartedAt() != null)
				model.put("diff_starting_in", DateUtils.dateDiff(now, game.getStartedAt(), request.getLocale()));
		}

		if (state == GameState.RUNNING)
			model.put("duration", DateUtils.dateDiff(game.getStartedAt(), now, request.getLocale()));

		if (state == GameState.FINISHED)
		{
			model.put("finish_at", game.getFinishedAt());
			model.put("duration", DateUtils.dateDiff(game.getStartedAt(), game.getFinishedAt(), request.getLocale()));
		}

		if (state == GameState.UNACCOMPLISHED)
			model.put("unaccomplish_at", game.getFinishedAt());

		game.accept(new GameVisitor()
		{
			public void visit(SingleGame game)
			{
				model.put("aid", game.getAid());
				model.put("clock_constraint_label", "clock_constraint");
				model.put("clock_constraint", game.getClockConstraint());
				model.put("rating_label", "rating");
				if (game.getRating() != null && game.getRating().ratingClass().equals("ELO"))
					model.put("rating", "elo");
			}

			public void visit(GameGroup group)
			{
				model.put("aid", group.getChildAid());
				model.put("clock_constraint_label", "child_clock_constraint");
				model.put("clock_constraint", group.getChildClockConstraint());
				model.put("rating_label", "child_rating");
				if (group.getChildRating() != null && group.getChildRating().ratingClass().equals("ELO"))
					model.put("rating", "elo");
			}
		});

		if (helper.isSingleGame() && (state == GameState.RUNNING || state == GameState.FINISHED))
			model.put("last_active_at", helper.getSingleGame().getLastActiveAt());

		if (helper.isSingleGame() && state == GameState.RUNNING)
			model.put("last_reminder_at", helper.getSingleGame().getLastReminderAt());

		return new ModelAndView(getViewName(), model);
	}
}
