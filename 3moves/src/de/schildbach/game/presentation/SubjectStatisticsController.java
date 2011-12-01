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
import java.security.Principal;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Required;
import org.springframework.beans.propertyeditors.StringTrimmerEditor;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindException;
import org.springframework.validation.Errors;
import org.springframework.web.bind.ServletRequestDataBinder;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.SimpleFormController;

import de.schildbach.portal.service.exception.NotAuthorizedException;
import de.schildbach.portal.service.game.GameService;

/**
 * @author Andreas Schildbach
 */
@Controller
public class SubjectStatisticsController extends SimpleFormController
{
	private static final int[] VALID_WINDOWS = new int[] { 3, 6, 12 };
	private static final int DEFAULT_WINDOW = 6;

	private GameService gameService;

	@Required
	public void setGameService(GameService gameService)
	{
		this.gameService = gameService;
	}

	public SubjectStatisticsController()
	{
		setBindOnNewForm(true);
	}

	@Override
	protected Object formBackingObject(HttpServletRequest request) throws Exception
	{
		Command command = new Command();
		command.setWindow(DEFAULT_WINDOW);
		return command;
	}

	@Override
	protected void initBinder(HttpServletRequest request, ServletRequestDataBinder binder) throws Exception
	{
		binder.registerCustomEditor(String.class, new StringTrimmerEditor(true));
	}

	@Override
	protected Map<String, Object> referenceData(HttpServletRequest request, Object commandObj, Errors errors) throws Exception
	{
		Principal user = request.getUserPrincipal();
		if (user == null)
			throw new NotAuthorizedException();

		Command command = (Command) commandObj;

		String subject = command.getSubject();
		if (subject == null)
			subject = user.getName();

		Map<String, Object> model = new HashMap<String, Object>();
		model.put("windows", VALID_WINDOWS);
		model.put("subject", subject);
		model.put("stats", gameService.gameStatisticsForSubject(subject, command.isAgainstMe() ? user.getName() : null, command.getWindow()));
		model.put("self", subject.equals(user.getName()));
		return model;
	}

	@Override
	protected ModelAndView onSubmit(HttpServletRequest request, HttpServletResponse response, Object commandObj, BindException errors)
			throws Exception
	{
		return showForm(request, response, errors);
	}

	public static class Command implements Serializable
	{
		private String subject;
		private boolean againstMe;
		private int window;

		public void setSubject(String subject)
		{
			this.subject = subject;
		}

		public String getSubject()
		{
			return subject;
		}

		public void setAgainstMe(boolean againstMe)
		{
			this.againstMe = againstMe;
		}

		public boolean isAgainstMe()
		{
			return againstMe;
		}

		public void setWindow(int window)
		{
			this.window = window;
		}

		public int getWindow()
		{
			return window;
		}
	}
}
