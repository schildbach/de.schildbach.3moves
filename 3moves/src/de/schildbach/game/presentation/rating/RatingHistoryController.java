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

import java.io.Serializable;
import java.security.Principal;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Required;
import org.springframework.beans.propertyeditors.StringTrimmerEditor;
import org.springframework.stereotype.Controller;
import org.springframework.validation.Errors;
import org.springframework.web.bind.ServletRequestDataBinder;
import org.springframework.web.servlet.mvc.SimpleFormController;

import de.schildbach.portal.persistence.game.Rating;
import de.schildbach.portal.persistence.game.SubjectRating;
import de.schildbach.portal.service.exception.NotAuthorizedException;
import de.schildbach.portal.service.game.GameService;

/**
 * @author Andreas Schildbach
 */
@Controller
public class RatingHistoryController extends SimpleFormController
{
	private GameService gameService;

	@Required
	public void setGameService(GameService gameService)
	{
		this.gameService = gameService;
	}

	public RatingHistoryController()
	{
		setBindOnNewForm(true);
	}

	@Override
	protected Object formBackingObject(HttpServletRequest request) throws Exception
	{
		return new Command();
	}

	@Override
	protected Map<String, Object> referenceData(HttpServletRequest request, Object commandObj, Errors errors) throws Exception
	{
		Principal loggedInUser = request.getUserPrincipal();
		if (loggedInUser == null)
			throw new NotAuthorizedException();

		Command command = (Command) commandObj;
		Map<String, Object> model = new HashMap<String, Object>();
		model.put("subject", command.getSubject());
		model.put("rating", command.getRating());
		List<Rating> ratingOptions = new LinkedList<Rating>();
		for (SubjectRating subjectRating : gameService.ratingsForSubject(command.getSubject()))
			ratingOptions.add(subjectRating.getRating());
		model.put("rating_options", ratingOptions);
		model.put("rating_history", gameService.ratingHistory(command.getSubject(), command.getRating()));
		return model;
	}

	@Override
	protected void initBinder(HttpServletRequest request, ServletRequestDataBinder binder) throws Exception
	{
		binder.registerCustomEditor(String.class, new StringTrimmerEditor(true));
	}

	@Override
	protected boolean isFormSubmission(HttpServletRequest arg0)
	{
		return true;
	}

	@Override
	protected boolean isFormChangeRequest(HttpServletRequest arg0)
	{
		return true;
	}

	public static class Command implements Serializable
	{
		private String subject;
		private Rating rating;

		public String getSubject()
		{
			return subject;
		}

		public void setSubject(String subject)
		{
			this.subject = subject;
		}

		public Rating getRating()
		{
			return rating;
		}

		public void setRating(Rating rating)
		{
			this.rating = rating;
		}
	}
}
