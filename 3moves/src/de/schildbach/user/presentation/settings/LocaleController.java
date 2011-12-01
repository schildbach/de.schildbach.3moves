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

package de.schildbach.user.presentation.settings;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Required;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.ServletRequestDataBinder;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.SessionAttributes;
import org.springframework.web.bind.support.SessionStatus;

import de.schildbach.portal.persistence.user.User;
import de.schildbach.portal.service.user.UserService;
import de.schildbach.presentation.DateUtils;
import de.schildbach.presentation.TimeZoneEditor;
import de.schildbach.user.presentation.AuthenticationHelper;
import de.schildbach.user.presentation.Environment;

/**
 * @author Andreas Schildbach
 */
@Controller
@SessionAttributes("command")
public class LocaleController
{
	private UserService userService;
	private AuthenticationHelper authenticationHelper;
	private String view;
	private String successView;
	private List<Locale> availableLocales;
	private Environment environment;

	@Required
	public void setUserService(UserService userService)
	{
		this.userService = userService;
	}

	@Required
	public void setAuthenticationHelper(AuthenticationHelper authenticationHelper)
	{
		this.authenticationHelper = authenticationHelper;
	}

	@Required
	public void setView(String view)
	{
		this.view = view;
	}

	@Required
	public void setSuccessView(String successView)
	{
		this.successView = successView;
	}

	@Required
	public void setAvailableLocales(List<Locale> availableLocales)
	{
		this.availableLocales = availableLocales;
	}

	@Required
	public void setEnvironment(Environment environment)
	{
		this.environment = environment;
	}

	@InitBinder
	public void initBinder(HttpServletRequest request, ServletRequestDataBinder binder)
	{
		binder.registerCustomEditor(TimeZone.class, new TimeZoneEditor(true));
	}

	@ModelAttribute("available_locales")
	public List<Locale> availableLocales()
	{
		return availableLocales;
	}

	@ModelAttribute("available_timezones")
	public List<TimeZone> availableTimezones()
	{
		return DateUtils.getAvailableTimeZones();
	}

	@ModelAttribute("timezone_display_names")
	public Map<TimeZone, String> timezoneDisplayNames(Locale locale)
	{
		Map<TimeZone, String> names = new HashMap<TimeZone, String>();
		for (TimeZone tz : availableTimezones())
			names.put(tz, tz.getDisplayName(false, TimeZone.SHORT, locale));
		return names;
	}

	@RequestMapping(method = RequestMethod.GET)
	public String setupForm(User user, ModelMap model)
	{
		Command command = new Command();
		command.setLocale(user.getLocale());
		command.setTimeZone(user.getTimeZone());

		model.addAttribute(command);
		return view;
	}

	@RequestMapping(method = RequestMethod.POST)
	public String processSubmit(User user, @ModelAttribute Command command, HttpSession session, SessionStatus status)
	{
		// change locale
		userService.changeLocale(user.getName(), command.getLocale(), command.getTimeZone());
		environment.setLocale(command.getLocale());
		authenticationHelper.refreshTimeZone(session, command.getTimeZone());

		// refresh user
		authenticationHelper.refreshLoggedInUser(userService.user(user.getName()));

		status.setComplete();

		return successView;
	}

	public static class Command implements Serializable
	{
		private Locale locale;
		private TimeZone timeZone;

		public Locale getLocale()
		{
			return locale;
		}

		public void setLocale(Locale locale)
		{
			this.locale = locale;
		}

		public TimeZone getTimeZone()
		{
			return timeZone;
		}

		public void setTimeZone(TimeZone timeZone)
		{
			this.timeZone = timeZone;
		}
	}
}
