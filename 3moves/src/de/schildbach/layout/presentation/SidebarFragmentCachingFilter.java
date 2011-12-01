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

package de.schildbach.layout.presentation;

import java.security.Principal;

import javax.servlet.http.HttpServletRequest;

import net.sf.ehcache.CacheManager;
import net.sf.ehcache.constructs.web.filter.PageFragmentCachingFilter;

import org.springframework.beans.factory.annotation.Required;

import de.schildbach.portal.persistence.user.Role;

/**
 * @author Andreas Schildbach
 */
public class SidebarFragmentCachingFilter extends PageFragmentCachingFilter
{
	private CacheManager cacheManager;
	private String cacheName;
	private boolean cacheKeyUserSensitive = true;
	private boolean cacheKeyRoleSensitive = false;

	@Required
	public void setCacheManager(CacheManager cacheManager)
	{
		this.cacheManager = cacheManager;
	}

	@Required
	public void setCacheName(String cacheName)
	{
		this.cacheName = cacheName;
	}

	public void setCacheKeyUserSensitive(boolean cacheKeyUserSensitive)
	{
		this.cacheKeyUserSensitive = cacheKeyUserSensitive;
	}

	public void setCacheKeyRoleSensitive(boolean cacheKeyRoleSensitive)
	{
		this.cacheKeyRoleSensitive = cacheKeyRoleSensitive;
	}

	@Override
	protected String calculateKey(HttpServletRequest request)
	{
		Principal user = request.getUserPrincipal();

		StringBuilder key = new StringBuilder();

		if (user != null)
		{
			if (cacheKeyUserSensitive)
			{
				// append user
				key.append(user.getName());
				key.append("_");
			}

			if (cacheKeyRoleSensitive)
			{
				// append roles
				for (Role role : Role.values())
				{
					String roleName = role.name();
					if (request.isUserInRole(roleName))
					{
						key.append(roleName);
						key.append("_");
					}
				}
			}
		}

		// append language
		key.append(request.getLocale());

		return key.toString();
	}

	@Override
	protected CacheManager getCacheManager()
	{
		return cacheManager;
	}

	@Override
	protected String getCacheName()
	{
		return cacheName;
	}
}
