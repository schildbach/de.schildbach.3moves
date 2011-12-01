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

package de.schildbach.user.presentation.controller;

import java.net.InetAddress;
import java.security.Principal;
import java.text.DateFormat;
import java.text.ParseException;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.stereotype.Controller;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.multiaction.MultiActionController;

import de.schildbach.portal.persistence.user.User;
import de.schildbach.portal.persistence.user.UserHolidays;
import de.schildbach.portal.service.exception.NotAuthorizedException;
import de.schildbach.portal.service.user.PresenceService;
import de.schildbach.portal.service.user.UserService;
import de.schildbach.portal.service.user.bo.UserSession;
import de.schildbach.user.presentation.PermanentLoginClientData;
import de.schildbach.web.RequestTime;
import de.schildbach.web.crypto.EncryptedCookieHelper;

/**
 * @author Andreas Schildbach
 */
@Controller
public class UserController extends MultiActionController
{
	@SuppressWarnings("unused")
	private static final Log LOG = LogFactory.getLog(UserController.class);

	private UserService userService;
	private PresenceService presenceService;
	private EncryptedCookieHelper permanentLoginCookieHelper;

	@Required
	public void setUserService(UserService userService)
	{
		this.userService = userService;
	}

	@Required
	public void setPresenceService(PresenceService presenceService)
	{
		this.presenceService = presenceService;
	}

	@Required
	public void setPermanentLoginCookieHelper(EncryptedCookieHelper permanentLoginCookieHelper)
	{
		this.permanentLoginCookieHelper = permanentLoginCookieHelper;
	}

	public ModelAndView edit_user_holidays(HttpServletRequest request, HttpServletResponse response)
	{
		Principal user = request.getUserPrincipal();
		if (user == null)
			throw new NotAuthorizedException();

		// refresh user
		User hUser = userService.user(user.getName());

		Map<UserHolidays, Boolean> canRemoveMap = new HashMap<UserHolidays, Boolean>();
		for (UserHolidays holidays : hUser.getUserHolidays())
		{
			canRemoveMap.put(holidays, userService.canRemoveHolidays(user.getName(), holidays.getBeginAt()));
		}

		Map<String, Object> model = new HashMap<String, Object>();

		model.put("holidays_list", hUser.getUserHolidays());
		model.put("can_remove", canRemoveMap);

		return new ModelAndView("edit_user_holidays.jspx", model);
	}

	public ModelAndView add_user_holidays(HttpServletRequest request, HttpServletResponse response)
	{
		Date now = RequestTime.get();

		Principal user = request.getUserPrincipal();
		if (user == null)
			throw new NotAuthorizedException();

		DateFormat format = DateFormat.getDateInstance(DateFormat.MEDIUM, request.getLocale());

		Date beginAt;
		try
		{
			beginAt = format.parse(request.getParameter("begin_at").trim());
		}
		catch (ParseException x)
		{
			return new ModelAndView("user_holidays", "error", "unparsable_begin_at");
		}

		Date endAt;
		try
		{
			endAt = format.parse(request.getParameter("end_at").trim());
		}
		catch (ParseException x)
		{
			return new ModelAndView("user_holidays", "error", "unparsable_end_at");
		}

		if (beginAt.after(endAt) || beginAt.equals(endAt))
			return new ModelAndView("user_holidays", "error", "begin_after_end");

		if (beginAt.before(now))
			return new ModelAndView("user_holidays", "error", "begin_before_now");

		if (!userService.canAddHolidays(user.getName(), beginAt, endAt))
			return new ModelAndView("user_holidays", "error", "failure");

		userService.addHolidays(user.getName(), beginAt, endAt);

		return new ModelAndView("user_holidays");
	}

	public ModelAndView remove_user_holidays(HttpServletRequest request, HttpServletResponse response)
	{
		Principal user = request.getUserPrincipal();
		if (user == null)
			throw new NotAuthorizedException();

		Date beginAt = new Date(Long.parseLong(request.getParameter("begin")));

		userService.removeHolidays(user.getName(), beginAt);

		return new ModelAndView("user_holidays");
	}

	public ModelAndView permanent_login(HttpServletRequest request, HttpServletResponse response)
	{
		Map<String, Object> model = new HashMap<String, Object>();

		try
		{
			PermanentLoginClientData encryptedCookie = permanentLoginCookieHelper.getEncryptedCookie(request, PermanentLoginClientData.class);
			if (encryptedCookie != null)
			{
				if (request.getUserPrincipal().getName().equals(encryptedCookie.getUsername()))
					model.put("is_enabled", true);
			}
		}
		catch (Exception x)
		{
			LOG.warn("could not decrypt permanent login cookie", x);
		}

		return new ModelAndView("permanent_login.jspx", model);
	}

	public ModelAndView set_automatic_login(HttpServletRequest request, HttpServletResponse response) throws Exception
	{
		Principal user = request.getUserPrincipal();
		if (user == null)
			throw new NotAuthorizedException();

		permanentLoginCookieHelper.setEncryptedCookie(response, new PermanentLoginClientData(RequestTime.get(), user.getName()), request
				.getContextPath());

		LOG.info("user " + user.getName() + " enabled automatic login");

		return new ModelAndView("user_automatic_login", "check", "true");
	}

	public ModelAndView clear_automatic_login(HttpServletRequest request, HttpServletResponse response)
	{
		permanentLoginCookieHelper.clearCookie(response, request.getContextPath());

		LOG.info("user " + request.getRemoteUser() + " disabled automatic login");

		return new ModelAndView("user_automatic_login");
	}

	public ModelAndView security_sidebar(HttpServletRequest request, HttpServletResponse response)
	{
		Map<String, Object> model = new HashMap<String, Object>();

		Principal user = request.getUserPrincipal();
		if (user == null)
			return null;

		int numLogins = presenceService.numberOfSimultaneousLogins(user.getName());
		if (numLogins >= 2)
			model.put("show_duplicate_session_warning", true);

		return new ModelAndView("security_sidebar.jspx", model);
	}

	public ModelAndView duplicate_sessions(HttpServletRequest request, HttpServletResponse response)
	{
		Map<String, Object> model = new HashMap<String, Object>();

		Principal user = request.getUserPrincipal();
		if (user == null)
			throw new NotAuthorizedException();

		HttpSession mySession = request.getSession(false);
		String mySessionId = mySession != null ? mySession.getId() : null;

		List<Map<String, Object>> list = new LinkedList<Map<String, Object>>();
		for (UserSession session : presenceService.sessionsOfUser(user.getName()))
		{
			Map<String, Object> item = new HashMap<String, Object>();
			item.put("is_self", session.getSessionId().equals(mySessionId));
			InetAddress address = session.getLoggedInFrom();
			item.put("remote_addr", address.getHostAddress());
			item.put("remote_host", address.getCanonicalHostName());
			item.put("user_agent", session.getUserAgent());
			item.put("logged_in_at", session.getLoggedInAt());
			list.add(item);
		}
		model.put("list", list);
		model.put("user_name", user.getName());

		return new ModelAndView("duplicate_sessions.jspx", model);
	}
}
