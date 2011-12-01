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

import java.security.Principal;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Required;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.ServletRequestUtils;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.ParameterizableViewController;

import de.schildbach.portal.persistence.game.GamePlayer;
import de.schildbach.portal.persistence.game.SingleGame;
import de.schildbach.portal.service.exception.NotAuthorizedException;
import de.schildbach.portal.service.game.GameService;

/**
 * @author Andreas Schildbach
 */
@Controller
public class ActiveGamesSidebarController extends ParameterizableViewController
{
	private GameService gameService;
	private String linkGame;
	private String linkMore;

	@Required
	public void setGameService(GameService gameService)
	{
		this.gameService = gameService;
	}

	@Required
	public void setLinkMore(String linkMore)
	{
		this.linkMore = linkMore;
	}
	
	@Required
	public void setLinkGame(String linkGame)
	{
		this.linkGame = linkGame;
	}

	@Override
	protected ModelAndView handleRequestInternal(HttpServletRequest request, HttpServletResponse response) throws Exception
	{
		Principal user = request.getUserPrincipal();
		if(user == null)
			throw new NotAuthorizedException();
		
		int max = ServletRequestUtils.getIntParameter(request, "max", 10);

		List<Row> rows = new LinkedList<Row>();
		boolean hasMore = false;
		for(SingleGame game : gameService.activeSingleGames(user.getName(), max + 1))
		{
			if(rows.size() < max)
			{
				Row row = new Row(game.getId());
				List<String> opponents = new LinkedList<String>();
				for(GamePlayer opponent : game.getPlayers())
				{
					if(!opponent.equals(game.getActivePlayer()))
					{
						opponents.add(opponent.getSubject().getName());
					}
				}
				row.setOpponents(opponents);
				rows.add(row);
			}
			else
			{
				hasMore = true;
			}
		}

		Map<String, Object> model = new HashMap<String, Object>();
		model.put("show", !rows.isEmpty());
		model.put("rows", rows);
		model.put("link_game", request.getContextPath() + linkGame);
		model.put("has_more", hasMore);
		model.put("link_more", request.getContextPath() + linkMore);
		return new ModelAndView(getViewName(), model);
	}
	
	public static final class Row
	{
		private List<String> opponents;
		private int id;

		public Row(int id)
		{
			setId(id);
		}

		public List<String> getOpponents()
		{
			return opponents;
		}
		public void setOpponents(List<String> opponents)
		{
			this.opponents = opponents;
		}

		public int getId()
		{
			return id;
		}
		public void setId(int id)
		{
			this.id = id;
		}
	}
}
