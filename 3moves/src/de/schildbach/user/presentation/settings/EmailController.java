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
import java.security.GeneralSecurityException;
import java.util.Date;

import javax.servlet.http.HttpSession;

import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.binary.Hex;
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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.SessionAttributes;
import org.springframework.web.bind.support.SessionStatus;

import de.schildbach.portal.persistence.user.User;
import de.schildbach.portal.service.user.UserService;
import de.schildbach.user.presentation.AddressClientData;
import de.schildbach.user.presentation.EmailValidator;
import de.schildbach.web.RequestTime;
import de.schildbach.web.crypto.EncryptionHelper;

/**
 * @author Andreas Schildbach
 */
@Controller
@SessionAttributes("command")
public class EmailController
{
	private EmailValidator emailValidator;
	private EncryptionHelper addressValidationEncryptionHelper;
	private UserService userService;
	private String view;
	private String successView;
	private String validateView;

	@Required
	public void setEmailValidator(EmailValidator emailValidator)
	{
		this.emailValidator = emailValidator;
	}

	@Required
	public void setAddressValidationEncryptionHelper(EncryptionHelper addressValidationEncryptionHelper)
	{
		this.addressValidationEncryptionHelper = addressValidationEncryptionHelper;
	}

	@Required
	public void setUserService(UserService userService)
	{
		this.userService = userService;
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
	public void setValidateView(String validateView)
	{
		this.validateView = validateView;
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
		command.setEmail(userService.user(user.getName()).getEmail());
		model.addAttribute(command);
		return view;
	}

	@RequestMapping(method = RequestMethod.POST, params = "submit")
	public String processSubmit(User user, @ModelAttribute Command command, BindingResult result, SessionStatus status, HttpSession session)
			throws GeneralSecurityException
	{
		if (command.getEmail() == null)
		{
			// clear
			userService.setEmail(user.getName(), null);
			status.setComplete();
			return successView;
		}
		else if (command.getEmail().equals(user.getEmail()))
		{
			// no change
			status.setComplete();
			return successView;
		}
		else
		{
			result.pushNestedPath("email");
			emailValidator.validate(command.getEmail(), result);
			result.popNestedPath();

			if (result.hasErrors())
			{
				return view;
			}
			else
			{
				Date now = RequestTime.get();
				AddressClientData data = new AddressClientData(now, user.getName(), "email", command.getEmail());
				String key = new String(Hex.encodeHex(addressValidationEncryptionHelper.encryptToClient(data)));
				userService.requestEmailValidation(user.getName(), command.getEmail(), key);

				session.setAttribute(SettingsController.MESSAGE_ATTRIBUTE, "success");

				status.setComplete();
				return successView;
			}
		}
	}

	@RequestMapping(method = RequestMethod.POST, params = "cancel")
	public String processSubmit(SessionStatus status)
	{
		status.setComplete();
		return successView;
	}

	@RequestMapping(method = RequestMethod.GET, params = "key")
	public String validateEmail(@RequestParam("key") String key, Model model)
	{
		final int KEY_EXPIRY_TIME = 14 * 24 * 60 * 60 * 1000;

		Date now = RequestTime.get();

		model.addAttribute("transport", "email");

		try
		{
			AddressClientData data = decryptAddressClientData(key.trim());

			model.addAttribute("username", data.getUsername());

			if (data.getTransport().equals("email"))
			{
				if (now.getTime() - data.getCreatedAt().getTime() > KEY_EXPIRY_TIME)
				{
					model.addAttribute("expired", true);
				}
				else
				{
					userService.setEmail(data.getUsername(), data.getAddress());
					model.addAttribute("address", data.getAddress());
					model.addAttribute("success", true);
				}
			}
		}
		catch (DecoderException e)
		{
		}
		catch (GeneralSecurityException x)
		{
		}

		return validateView;
	}

	private AddressClientData decryptAddressClientData(String urlEncodedKey) throws DecoderException, GeneralSecurityException
	{
		// decrypt key
		byte[] key = Hex.decodeHex(urlEncodedKey.toCharArray());
		return addressValidationEncryptionHelper.decryptFromClient(key, AddressClientData.class);
	}

	public static class Command implements Serializable
	{
		private String email;

		public String getEmail()
		{
			return email;
		}

		public void setEmail(String email)
		{
			this.email = email;
		}
	}
}
