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

import java.security.Principal;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Required;
import org.springframework.stereotype.Controller;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.multiaction.MultiActionController;
import org.springframework.web.servlet.view.RedirectView;

import de.schildbach.portal.persistence.user.User;
import de.schildbach.portal.service.exception.NotAuthorizedException;
import de.schildbach.portal.service.user.UserService;
import de.schildbach.presentation.ReferrerFilter;

/**
 * @author Andreas Schildbach
 */
@Controller
public class UserTermsController extends MultiActionController
{
	private UserService userService;
	
	@Required
	public void setUserService(UserService userService)
	{
		this.userService = userService;
	}

	public ModelAndView terms(HttpServletRequest request, HttpServletResponse response)
	{
		Map<String, Object> model = new HashMap<String, Object>();

		int effectiveTermsVersion = userService.effectiveTermsVersion();
		model.put("effective_terms_version", new Integer(effectiveTermsVersion));

		Principal user = request.getUserPrincipal();
		if (user != null)
		{
			// refresh user
			User hUser = userService.user(user.getName());

			int acceptedTerms = hUser.getAcceptedTerms();
			model.put("accepted_terms", acceptedTerms);
			model.put("accepted_terms_at", hUser.getAcceptedTermsAt());
			model.put("can_accept_terms", acceptedTerms < effectiveTermsVersion);
			model.put("effective_terms_accepted", acceptedTerms == effectiveTermsVersion);
		}

		return new ModelAndView("terms.jspx", model);
	}

	public ModelAndView accept_terms(HttpServletRequest request, HttpServletResponse response)
	{
		Principal user = request.getUserPrincipal();
		if(user == null)
			throw new NotAuthorizedException();

		int version = Integer.parseInt(request.getParameter("version"));
	
		// accept terms
		userService.acceptTerms(user.getName(), version);
		
		// redirect to last displayed page if available
		HttpSession session = request.getSession(false);
		if(session != null)
		{
			String internalReferrer = (String) session.getAttribute(ReferrerFilter.REFERRER);
			if(internalReferrer != null)
				return new ModelAndView(new RedirectView(internalReferrer));
		}

		// else display user terms again
		return new ModelAndView("user_terms");
	}
}
