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

package de.schildbach.game.presentation.forming;

import java.io.Serializable;
import java.security.Principal;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.codec.binary.Hex;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.beans.propertyeditors.StringTrimmerEditor;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindException;
import org.springframework.validation.Errors;
import org.springframework.web.bind.ServletRequestDataBinder;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.SimpleFormController;

import de.schildbach.game.presentation.GameInvitationClientData;
import de.schildbach.portal.persistence.user.User;
import de.schildbach.portal.service.exception.NotAuthorizedException;
import de.schildbach.portal.service.game.GameService;
import de.schildbach.portal.service.game.exception.InvalidTargetSubjectException;
import de.schildbach.portal.service.user.UserService;
import de.schildbach.user.presentation.EmailValidator;
import de.schildbach.util.TextWrapper;
import de.schildbach.web.RequestTime;
import de.schildbach.web.crypto.EncryptionHelper;

/**
 * @author Andreas Schildbach
 */
@Controller
public class InviteUserController extends SimpleFormController
{
	private static final String REQUEST_PARAMETER_ID = "game_id";

	private EncryptionHelper gameInvitationEncryptionHelper;
	private EmailValidator emailValidator;
	private GameService gameService;
	private UserService userService;

	@Required
	public void setGameInvitationEncryptionHelper(EncryptionHelper gameInvitationEncryptionHelper)
	{
		this.gameInvitationEncryptionHelper = gameInvitationEncryptionHelper;
	}

	@Required
	public void setEmailValidator(EmailValidator emailValidator)
	{
		this.emailValidator = emailValidator;
	}

	@Required
	public void setGameService(GameService gameService)
	{
		this.gameService = gameService;
	}

	@Required
	public void setUserService(UserService userService)
	{
		this.userService = userService;
	}

	@Override
	protected boolean isFormSubmission(HttpServletRequest request)
	{
		Principal user = request.getUserPrincipal();
		if (user == null)
			throw new NotAuthorizedException();

		return request.getParameter(REQUEST_PARAMETER_ID) == null;
	}

	@Override
	protected Object formBackingObject(HttpServletRequest request) throws Exception
	{
		int gameId = Integer.parseInt(request.getParameter(REQUEST_PARAMETER_ID));
		Command command = new Command(gameId);

		// refresh user
		User user = userService.user(request.getUserPrincipal().getName());

		// populate form
		if (user.getEmail() != null)
			command.setFromAddr(user.getEmail());
		if (user.getFullName() != null)
			command.setFromName(user.getFullName());

		// provide texts
		String[] text = gameService.invitationText(user.getLocale(), command.getGameId());
		command.setSubject(text[0]);
		command.setText(text[1]);

		return command;
	}

	@Override
	protected void initBinder(HttpServletRequest request, ServletRequestDataBinder binder) throws Exception
	{
		binder.setAllowedFields(new String[] { "username", "fromName", "fromAddr", "toAddr", "subject", "text" });
		binder.registerCustomEditor(String.class, new StringTrimmerEditor(true));
	}

	@Override
	protected Map<String, Object> referenceData(final HttpServletRequest request, Object commandObj, Errors errors) throws Exception
	{
		Command command = (Command) commandObj;

		final Map<String, Object> model = new HashMap<String, Object>();

		// refresh user
		User user = userService.user(request.getUserPrincipal().getName());

		// provide static text
		String[] text = gameService.invitationText(user.getLocale(), command.getGameId());
		model.put("static_text", TextWrapper.wrap(text[2], 80));

		return model;
	}

	@Override
	protected ModelAndView onSubmit(HttpServletRequest request, HttpServletResponse response, Object commandObj, BindException errors)
			throws Exception
	{
		Command command = (Command) commandObj;

		if (request.getParameter("submit_known") != null)
		{
			if (command.getUsername() == null)
				errors.rejectValue("username", "missing");

			if (!errors.hasErrors())
			{
				try
				{
					gameService.inviteSubjectToGame(request.getRemoteUser(), command.getGameId(), command.getUsername());
				}
				catch (InvalidTargetSubjectException x)
				{
					errors.rejectValue("username", "unknown");
				}
			}
		}
		else if (request.getParameter("submit_unknown") != null)
		{
			if (command.getFromName() == null)
				errors.rejectValue("fromName", "missing");
			if (command.getFromAddr() == null)
				errors.rejectValue("fromAddr", "missing");

			errors.pushNestedPath("fromAddr");
			emailValidator.validate(command.getFromAddr(), errors);
			errors.popNestedPath();

			if (command.getToAddr() == null)
				errors.rejectValue("toAddr", "missing");

			errors.pushNestedPath("toAddr");
			emailValidator.validate(command.getToAddr(), errors);
			errors.popNestedPath();

			if (command.getSubject() == null)
				errors.rejectValue("subject", "missing");

			if (!errors.hasErrors())
			{
				// generate key
				Date now = RequestTime.get();
				GameInvitationClientData data = new GameInvitationClientData(now, request.getRemoteUser(), command.getGameId());
				String key = new String(Hex.encodeHex(gameInvitationEncryptionHelper.encryptToClient(data)));

				gameService.inviteEMailToGame(request.getRemoteUser(), command.getGameId(), command.getFromName(), command.getFromAddr(), command
						.getToAddr(), command.getSubject(), command.getText(), key);
			}
		}
		else
		{
			throw new IllegalArgumentException();
		}

		if (errors.hasErrors())
			return showForm(request, response, errors);
		else
			return new ModelAndView("game", "id", command.getGameId());
	}

	public static class Command implements Serializable
	{
		private int gameId;
		private String username;
		private String toAddr;
		private String fromAddr;
		private String fromName;
		private String subject;
		private String text;

		public Command(int gameId)
		{
			this.setGameId(gameId);
		}

		public int getGameId()
		{
			return gameId;
		}

		private void setGameId(int gameId)
		{
			this.gameId = gameId;
		}

		public void setUsername(String username)
		{
			this.username = username;
		}

		public String getUsername()
		{
			return username;
		}

		public void setToAddr(String toAddr)
		{
			this.toAddr = toAddr;
		}

		public String getToAddr()
		{
			return toAddr;
		}

		public void setFromAddr(String fromAddr)
		{
			this.fromAddr = fromAddr;
		}

		public String getFromAddr()
		{
			return fromAddr;
		}

		public void setFromName(String fromName)
		{
			this.fromName = fromName;
		}

		public String getFromName()
		{
			return fromName;
		}

		public void setSubject(String subject)
		{
			this.subject = subject;
		}

		public String getSubject()
		{
			return subject;
		}

		public void setText(String text)
		{
			this.text = text;
		}

		public String getText()
		{
			return text;
		}
	}
}
