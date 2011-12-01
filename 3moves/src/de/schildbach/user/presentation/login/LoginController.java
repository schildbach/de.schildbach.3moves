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

package de.schildbach.user.presentation.login;

import java.io.Serializable;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openid4java.OpenIDException;
import org.openid4java.consumer.ConsumerManager;
import org.openid4java.consumer.VerificationResult;
import org.openid4java.discovery.DiscoveryInformation;
import org.openid4java.discovery.Identifier;
import org.openid4java.message.AuthRequest;
import org.openid4java.message.ParameterList;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.beans.propertyeditors.StringTrimmerEditor;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.SessionAttributes;
import org.springframework.web.bind.support.SessionStatus;

import de.schildbach.portal.persistence.user.User;
import de.schildbach.portal.service.user.UserAuthenticationService;
import de.schildbach.presentation.ReferrerFilter;
import de.schildbach.user.presentation.AuthenticationHelper;
import de.schildbach.user.presentation.Environment;
import de.schildbach.user.presentation.authentication.IdentifierHelper;
import de.schildbach.user.presentation.authentication.IdentifierHelper.IdentifierType;
import de.schildbach.web.RequestTime;

/**
 * @author Andreas Schildbach
 */
@Controller
@SessionAttributes("command")
public class LoginController
{
	@SuppressWarnings("unused")
	private static final Log LOG = LogFactory.getLog(LoginController.class);

	private UserAuthenticationService userAuthenticationService;
	private AuthenticationHelper authenticationHelper;
	private ConsumerManager consumerManager;
	private Environment environment;
	private String view;
	private String successView;

	private static final String SESSION_ATTRIBUTE_DISCOVERY_INFORMATION = LoginController.class.getName() + ".discoveryInformation";

	@Required
	public void setUserAuthenticationService(UserAuthenticationService userAuthenticationService)
	{
		this.userAuthenticationService = userAuthenticationService;
	}

	@Required
	public void setAuthenticationHelper(AuthenticationHelper authenticationHelper)
	{
		this.authenticationHelper = authenticationHelper;
	}

	@Required
	public void setConsumerManager(ConsumerManager consumerManager)
	{
		this.consumerManager = consumerManager;
	}

	@Required
	public void setEnvironment(Environment environment)
	{
		this.environment = environment;
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

	@InitBinder
	protected void initBinder(WebDataBinder binder)
	{
		binder.registerCustomEditor(String.class, new StringTrimmerEditor(true));
	}

	@ModelAttribute("command")
	public Command createCommand()
	{
		return new Command();
	}

	@RequestMapping(method = RequestMethod.GET)
	public String setupForm()
	{
		return view;
	}

	@RequestMapping(method = RequestMethod.POST)
	public String onSubmit(HttpServletRequest request, @ModelAttribute Command command, BindingResult result, HttpSession session)
	{
		String identifier = command.getIdentifier();

		if (identifier == null)
		{
			result.rejectValue("identifier", "missing", "");
			return view;
		}

		IdentifierType type = IdentifierHelper.determineIdentifierType(identifier);
		String password = command.getPassword();

		if (type != IdentifierType.OPEN_ID && password == null)
		{
			result.rejectValue("password", "missing", "");
			return view;
		}

		String userAgent = request.getHeader("user-agent");
		String ip = request.getRemoteAddr();

		User user = null;
		if (type == IdentifierType.NAME)
		{
			user = userAuthenticationService.loginByName(identifier, password, ip, userAgent);
		}
		else if (type == IdentifierType.EMAIL)
		{
			user = userAuthenticationService.loginByEmail(identifier, password, ip, userAgent);
		}
		else if (type == IdentifierType.OPEN_ID)
		{
			try
			{
				// attempt to associate with the OpenID provider and retrieve one service endpoint for
				// authentication
				DiscoveryInformation discovered = consumerManager.associate(consumerManager.discover(identifier));

				// store
				session.setAttribute(SESSION_ATTRIBUTE_DISCOVERY_INFORMATION, discovered);

				// obtain a AuthRequest message to be sent to the OpenID provider
				AuthRequest authReq = consumerManager.authenticate(discovered, request.getRequestURL().toString());

				// GET HTTP-redirect to the OpenID Provider endpoint redirect-URL usually limited ~2048 bytes
				String redirectUrl = "redirect:" + authReq.getDestinationUrl(true);
				LOG.info("redirecting to: " + redirectUrl);

				return redirectUrl;
			}
			catch (OpenIDException e)
			{
				LOG.error("handled exception", e);
				result.rejectValue("identifier", "error");
				return view;
			}
		}

		if (user == null)
		{
			result.reject("failed", "");
			return view;
		}

		return internalLoginUser(request.getRemoteUser(), session, userAgent, ip, user);
	}

	@RequestMapping(method = RequestMethod.GET, params = "openid.mode")
	public String processAuthenticationResponse(@ModelAttribute Command command, BindingResult result, SessionStatus status,
			HttpServletRequest request, HttpSession session)
	{
		try
		{
			// retrieve the previously stored discovery information
			DiscoveryInformation discovered = (DiscoveryInformation) session.getAttribute(SESSION_ATTRIBUTE_DISCOVERY_INFORMATION);
			session.removeAttribute(SESSION_ATTRIBUTE_DISCOVERY_INFORMATION);

			// extract the receiving URL from the HTTP request
			StringBuffer receivingURL = request.getRequestURL();
			String queryString = request.getQueryString();
			if (queryString != null && queryString.length() > 0)
				receivingURL.append("?").append(request.getQueryString());

			// verify the response
			VerificationResult verification = consumerManager.verify(receivingURL.toString(), new ParameterList(request.getParameterMap()),
					discovered);

			// examine the verification result and extract the verified identifier
			Identifier verified = verification.getVerifiedId();
			if (verified != null)
			{
				// success
				String userAgent = request.getHeader("user-agent");
				String ip = request.getRemoteAddr();
				User user = userAuthenticationService.loginByOpenId(verified.getIdentifier(), ip, userAgent);
				if (user == null)
				{
					result.reject("failed", "");
					return view;
				}
				String viewName = internalLoginUser(request.getRemoteUser(), request.getSession(), userAgent, ip, user);
				status.setComplete();
				return viewName;
			}
			else
			{
				result.rejectValue("identifier", "unverified");
				return view;
			}
		}
		catch (OpenIDException e)
		{
			LOG.error("handled exception", e);
			result.rejectValue("identifier", "error");
			return view;
		}
	}

	@RequestMapping(method = RequestMethod.HEAD)
	public void handleHead()
	{
	}

	private String internalLoginUser(String remoteUser, HttpSession session, String userAgent, String ip, User user)
	{
		if (user.getLocale() != null)
			environment.setLocale(user.getLocale());
		if (user.getScreenResolution() != null)
			environment.setScreenResolution(user.getScreenResolution());

		authenticationHelper.login(remoteUser, session, RequestTime.get(), user, ip, userAgent);

		// point user to terms of usage
		if (user.getAcceptedTerms() == 0)
			return "user_terms";

		String internalReferrer = (String) session.getAttribute(ReferrerFilter.REFERRER);
		LOG.debug("got " + ReferrerFilter.REFERRER + " attribute \"" + internalReferrer + "\" from session " + session.getId());
		if (internalReferrer != null)
			return "redirect:" + internalReferrer;
		else
			return successView;
	}

	public static class Command implements Serializable
	{
		private String identifier;
		private String password;

		public final String getIdentifier()
		{
			return identifier;
		}

		public final void setIdentifier(String identifier)
		{
			this.identifier = identifier;
		}

		public final String getPassword()
		{
			return password;
		}

		public final void setPassword(String password)
		{
			this.password = password;
		}
	}
}
