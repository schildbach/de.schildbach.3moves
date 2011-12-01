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

import java.security.Principal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Required;
import org.springframework.stereotype.Controller;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.ParameterizableViewController;

import de.schildbach.portal.persistence.game.Rating;
import de.schildbach.portal.persistence.game.SubjectRating;
import de.schildbach.portal.service.game.GameService;

/**
 * @author Andreas Schildbach
 */
@Controller
public class RatingListController extends ParameterizableViewController
{
	private GameService gameService;

	@Required
	public void setGameService(GameService gameService)
	{
		this.gameService = gameService;
	}

	@Override
	public ModelAndView handleRequestInternal(HttpServletRequest request, HttpServletResponse response)
	{
		Map<String, Object> model = new HashMap<String, Object>();

		Principal user = request.getUserPrincipal();

		Rating rating = Rating.valueOf(request.getParameter("rating"));

		model.put("rating", rating);
		List<SubjectRating> ratings = gameService.ratingsToplist(rating, 25, request.getRemoteUser(), 25);
		model.put("subject_ratings", ratings);
		Map<SubjectRating, Boolean> highlighted = new HashMap<SubjectRating, Boolean>();
		for (SubjectRating subjectRating : ratings)
			highlighted.put(subjectRating, subjectRating.getSubject().equals(user));
		model.put("highlighted", highlighted);

		return new ModelAndView(getViewName(), model);
	}
}
