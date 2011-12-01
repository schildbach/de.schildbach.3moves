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

package de.schildbach.game.presentation.job;

import java.io.PrintWriter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.stereotype.Controller;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.AbstractController;

import de.schildbach.portal.persistence.game.Rating;
import de.schildbach.portal.service.game.GameService;

/**
 * @author Andreas Schildbach
 */
@Controller
public class UpdateRatingIndexController extends AbstractController
{
	@SuppressWarnings("unused")
	private static final Log LOG = LogFactory.getLog(UpdateRatingIndexController.class);

	private GameService gameService;

	@Required
	public void setGameService(GameService gameService)
	{
		this.gameService = gameService;
	}

	@Override
	protected ModelAndView handleRequestInternal(HttpServletRequest request, HttpServletResponse response) throws Exception
	{
		for (Rating rating : Rating.values())
			updateRatingIndex(rating, response.getWriter());

		return null;
	}

	private void updateRatingIndex(Rating rating, PrintWriter writer)
	{
		long start = System.currentTimeMillis();
		int count = gameService.updateRatingIndex(rating);
		long end = System.currentTimeMillis();

		String message = "update_rating_index: updated " + count + " " + rating + ", took " + (end - start) + " ms";
		LOG.info(message);
		writer.println(message);
	}
}
