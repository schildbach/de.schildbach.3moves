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

package de.schildbach.game.presentation.rating;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Required;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.ServletRequestUtils;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.ParameterizableViewController;

import de.schildbach.portal.persistence.game.Rating;
import de.schildbach.portal.service.game.GameService;

/**
 * @author Andreas Schildbach
 */
@Controller
public class ToplistSidebarController extends ParameterizableViewController
{
	private static final String SESSION_ATTRIBUTE_CURRENT_RATING = ToplistSidebarController.class.getName() + ".currentRating";

	private GameService gameService;

	@Required
	public void setGameService(GameService gameService)
	{
		this.gameService = gameService;
	}

	@Override
	protected ModelAndView handleRequestInternal(HttpServletRequest request, HttpServletResponse response) throws Exception
	{
		int max = ServletRequestUtils.getIntParameter(request, "max", 5);

		HttpSession session = request.getSession(false);
		Rating rating = getCurrentRating(session);
		if (rating == null)
			rating = newCurrentRating();
		rating = nextCurrentRating(rating);
		setCurrentRating(session, rating);

		Map<String, Object> model = new HashMap<String, Object>();
		model.put("rating", rating);
		if (rating != null)
			model.put("subject_ratings", gameService.ratingsToplist(rating, max));
		return new ModelAndView(getViewName(), model);
	}

	private Rating newCurrentRating()
	{
		int i = new Random().nextInt(Rating.values().length);

		return Rating.values()[i];
	}

	private Rating nextCurrentRating(Rating currentRating)
	{
		if (currentRating == null)
			return null;

		int i = currentRating.ordinal();

		i++;

		if (i >= Rating.values().length)
			i = 0;

		return Rating.values()[i];
	}

	private Rating getCurrentRating(HttpSession session)
	{
		if (session == null)
			return null;

		return (Rating) session.getAttribute(SESSION_ATTRIBUTE_CURRENT_RATING);
	}

	private void setCurrentRating(HttpSession session, Rating rating)
	{
		if (session != null && rating != null)
		{
			session.setAttribute(SESSION_ATTRIBUTE_CURRENT_RATING, rating);
		}
	}
}
