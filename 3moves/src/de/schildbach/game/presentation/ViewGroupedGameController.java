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

import java.io.Serializable;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Required;
import org.springframework.stereotype.Controller;
import org.springframework.validation.Errors;
import org.springframework.web.servlet.mvc.SimpleFormController;

import de.schildbach.portal.persistence.game.Game;
import de.schildbach.portal.persistence.game.GameGroup;
import de.schildbach.portal.persistence.game.GamePlayer;
import de.schildbach.portal.persistence.game.SingleGame;
import de.schildbach.portal.persistence.user.Subject;
import de.schildbach.util.CompareUtils;

/**
 * @author Andreas Schildbach
 */
@Controller
public class ViewGroupedGameController extends SimpleFormController
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
	protected boolean isFormSubmission(HttpServletRequest request)
	{
		return request.getParameter(ViewGameController.REQUEST_PARAMETER_ID) == null;
	}

	@Override
	protected Object formBackingObject(HttpServletRequest request) throws Exception
	{
		return new Command();
	}

	@Override
	protected Map<String, Object> referenceData(HttpServletRequest request, Object commandObj, Errors errors) throws Exception
	{
		GameGroup hGroup = (GameGroup) gameViewHelper.getGame(request);

		// sort players by points (descending) and by position (ascending)
		List<GamePlayer> players = new LinkedList<GamePlayer>(hGroup.getPlayers());
		Collections.sort(players, new Comparator<GamePlayer>()
		{
			public int compare(GamePlayer player0, GamePlayer player1)
			{
				int pointsCompare = compareWithNull(player0.getPoints(), player1.getPoints());
				if (pointsCompare != 0)
					return -(pointsCompare);
				else
					return CompareUtils.compare(player0.getPosition(), player1.getPosition());
			}

			private <T extends Comparable<T>> int compareWithNull(T o1, T o2)
			{
				if (o1 == null && o2 == null)
					return 0;
				if (o1 == null && o2 != null)
					return -1;
				if (o1 != null && o2 == null)
					return 1;
				return o1.compareTo(o2);
			}
		});

		// build matrix
		int dimension = players.size();
		List<Subject> subjects = new LinkedList<Subject>();
		for (GamePlayer player : players)
			subjects.add(player.getSubject());

		Game[][] matrix = new Game[dimension][];
		for (int i = 0; i < dimension; i++)
			matrix[i] = new Game[dimension];

		for (SingleGame child : hGroup.getChildGames())
		{
			int y = subjects.indexOf(child.getPlayers().get(0).getSubject());
			int x = subjects.indexOf(child.getPlayers().get(1).getSubject());
			matrix[x][y] = child;
		}

		Map<String, Object> model = new HashMap<String, Object>();
		model.put("game_id", hGroup.getId());
		model.put("players", players);
		model.put("matrix", matrix);
		model.put("child_games", hGroup.getChildGames());
		model.put("link_game", request.getContextPath() + linkGame);
		return model;
	}

	@Override
	protected boolean isFormChangeRequest(HttpServletRequest request)
	{
		return true;
	}

	public static class Command implements Serializable
	{
	}
}
