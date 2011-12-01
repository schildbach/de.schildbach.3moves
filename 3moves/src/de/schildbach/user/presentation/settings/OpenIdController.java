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

import javax.servlet.http.HttpServletRequest;

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
import org.springframework.ui.Model;
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
import de.schildbach.portal.service.user.UserService;

/**
 * @author Andreas Schildbach
 */
@Controller
@SessionAttributes( { "command", "open_id_state" })
public class OpenIdController
{
	@SuppressWarnings("unused")
	private static final Log LOG = LogFactory.getLog(OpenIdController.class);

	private UserAuthenticationService userAuthenticationService;
	private UserService userService;
	private ConsumerManager consumerManager;
	private String view;
	private String successView;

	@Required
	public void setUserAuthenticationService(UserAuthenticationService userAuthenticationService)
	{
		this.userAuthenticationService = userAuthenticationService;
	}

	@Required
	public void setUserService(UserService userService)
	{
		this.userService = userService;
	}

	@Required
	public void setConsumerManager(ConsumerManager consumerManager)
	{
		this.consumerManager = consumerManager;
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
	public void initBinder(WebDataBinder binder)
	{
		binder.registerCustomEditor(String.class, new StringTrimmerEditor(true));
	}

	@RequestMapping(method = RequestMethod.GET)
	public String setupForm(User user, Model model)
	{
		Command command = new Command();
		command.setOpenId(userService.user(user.getName()).getOpenId());
		model.addAttribute(command);

		model.addAttribute("open_id_state", new OpenIdState());

		return view;
	}

	@RequestMapping(method = RequestMethod.POST, params = "submit")
	public String processSubmit(User user, @ModelAttribute Command command, BindingResult result,
			@ModelAttribute("open_id_state") OpenIdState openIdState, SessionStatus status, HttpServletRequest request)
	{
		try
		{
			if (command.getOpenId() == null)
			{
				// clear
				userAuthenticationService.setOpenId(user.getName(), null);
				status.setComplete();
				return successView;
			}
			else if (command.getOpenId().equals(user.getOpenId()))
			{
				// no change
				status.setComplete();
				return successView;
			}
			else
			{
				// attempt to associate with the OpenID provider and retrieve one service endpoint for authentication
				DiscoveryInformation discovered = consumerManager.associate(consumerManager.discover(command.getOpenId()));

				// store the discovery information in the user's session
				openIdState.setDiscoveryInformation(discovered);

				// obtain a AuthRequest message to be sent to the OpenID provider
				AuthRequest authReq = consumerManager.authenticate(discovered, request.getRequestURL().toString());

				// GET HTTP-redirect to the OpenID Provider endpoint redirect-URL usually limited ~2048 bytes
				String redirectUrl = "redirect:" + authReq.getDestinationUrl(true);
				LOG.info("redirecting user \"" + user.getName() + "\" to: " + redirectUrl);

				return redirectUrl;
			}
		}
		catch (OpenIDException e)
		{
			LOG.error("handled exception", e);
			result.rejectValue("openId", "error");
			return view;
		}
	}

	@RequestMapping(method = RequestMethod.POST, params = "cancel")
	public String processSubmit(SessionStatus status)
	{
		status.setComplete();
		return successView;
	}

	@RequestMapping(method = RequestMethod.GET, params = "openid.mode")
	public String processAuthenticationResponse(User user, @ModelAttribute Command command, BindingResult result,
			@ModelAttribute("open_id_state") OpenIdState openIdState, SessionStatus status, HttpServletRequest request)
	{
		try
		{
			// retrieve the previously stored discovery information
			DiscoveryInformation discovered = openIdState.getDiscoveryInformation();

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
				userAuthenticationService.setOpenId(user.getName(), verified.getIdentifier());
				status.setComplete();
				return successView;
			}
			else
			{
				result.rejectValue("openId", "unverified");
				return view;
			}
		}
		catch (OpenIDException e)
		{
			LOG.error("handled exception", e);
			result.rejectValue("openId", "error");
			return view;
		}
	}

	@RequestMapping(method = RequestMethod.HEAD)
	public void handleHead()
	{
	}

	public static class Command implements Serializable
	{
		private String openId;

		public void setOpenId(String openId)
		{
			this.openId = openId;
		}

		public String getOpenId()
		{
			return openId;
		}
	}

	public static class OpenIdState implements Serializable
	{
		private DiscoveryInformation discoveryInformation;

		public void setDiscoveryInformation(DiscoveryInformation discoveryInformation)
		{
			this.discoveryInformation = discoveryInformation;
		}

		public DiscoveryInformation getDiscoveryInformation()
		{
			return discoveryInformation;
		}
	}
}
