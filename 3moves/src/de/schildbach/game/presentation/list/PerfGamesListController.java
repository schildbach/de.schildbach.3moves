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

package de.schildbach.game.presentation.list;

import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.stereotype.Controller;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.ParameterizableViewController;

import de.schildbach.portal.persistence.game.Aid;
import de.schildbach.portal.persistence.game.Game;
import de.schildbach.portal.persistence.game.GamePlayer;
import de.schildbach.portal.persistence.game.Rating;
import de.schildbach.portal.persistence.game.Rules;
import de.schildbach.portal.persistence.game.SingleGame;
import de.schildbach.portal.persistence.user.User;
import de.schildbach.web.RequestTime;

/**
 * @author Andreas Schildbach
 */
@Controller
public class PerfGamesListController extends ParameterizableViewController
{
	@Override
	protected ModelAndView handleRequestInternal(HttpServletRequest request, HttpServletResponse response) throws Exception
	{
		Date now = RequestTime.get();

		User user1 = new User(now, "user1", Locale.ENGLISH);
		User user2 = new User(now, "user2", Locale.ENGLISH);
		User owner = new User(now, "owner", Locale.ENGLISH);

		List<Game> games = new LinkedList<Game>();
		for (int i = 0; i < 40; i++)
		{
			SingleGame game = new SingleGame(owner, now, Rules.CHESS, null, Rating.CHESS_COMPUTER_ELO, Aid.NONE, null);
			game.setId(i);
			game.setTurn(new Integer(i));
			game.addPlayer(new GamePlayer(game, user1, now));
			game.addPlayer(new GamePlayer(game, user2, now));
			game.setLastActiveAt(now);
			game.setLastMoveNotation("move");
			game.setClockConstraint("clock");
			games.add(game);
		}

		return new ModelAndView(getViewName(), "games", games);
	}
}
