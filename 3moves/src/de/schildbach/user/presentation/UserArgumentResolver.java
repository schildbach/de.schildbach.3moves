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

package de.schildbach.user.presentation;

import org.springframework.beans.factory.annotation.Required;
import org.springframework.core.MethodParameter;
import org.springframework.web.bind.support.WebArgumentResolver;
import org.springframework.web.context.request.NativeWebRequest;

import de.schildbach.portal.persistence.user.User;
import de.schildbach.portal.service.exception.NotAuthorizedException;
import de.schildbach.portal.service.user.UserService;

/**
 * @author Andreas Schildbach
 */
public class UserArgumentResolver implements WebArgumentResolver
{
	private UserService userService;

	@Required
	public void setUserService(UserService userService)
	{
		this.userService = userService;
	}

	public Object resolveArgument(MethodParameter methodParameter, NativeWebRequest webRequest) throws Exception
	{
		if (User.class.isAssignableFrom(methodParameter.getParameterType()))
		{
			String remoteUser = webRequest.getRemoteUser();

			if (remoteUser == null)
				throw new NotAuthorizedException();

			return userService.user(remoteUser);
		}

		return WebArgumentResolver.UNRESOLVED;
	}
}
