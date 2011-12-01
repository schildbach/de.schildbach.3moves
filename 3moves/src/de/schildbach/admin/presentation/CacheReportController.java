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

package de.schildbach.admin.presentation;

import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import net.sf.ehcache.CacheManager;
import net.sf.ehcache.Ehcache;

import org.springframework.beans.factory.annotation.Required;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.context.request.WebRequest;

import de.schildbach.portal.persistence.user.Role;
import de.schildbach.portal.service.exception.NotAuthorizedException;

/**
 * @author Andreas Schildbach
 */
@Controller
public class CacheReportController
{
	private CacheManager cacheManager;
	private String view;
	private String viewKeys;

	@Required
	public void setCacheManager(CacheManager cacheManager)
	{
		this.cacheManager = cacheManager;
	}

	@Required
	public void setView(String view)
	{
		this.view = view;
	}

	@Required
	public void setViewKeys(String viewKeys)
	{
		this.viewKeys = viewKeys;
	}

	@RequestMapping(method = RequestMethod.GET)
	public String handleRequest(WebRequest request, Model model)
	{
		if (!request.isUserInRole(Role.ADMIN.name()))
			throw new NotAuthorizedException();

		boolean mem = request.getParameter("mem") != null;

		Map<Ehcache, Long> memoryUsages = new HashMap<Ehcache, Long>();
		Set<Ehcache> caches = new TreeSet<Ehcache>(new NameComparator());
		for (String cacheName : cacheManager.getCacheNames())
		{
			Ehcache cache = cacheManager.getEhcache(cacheName);
			caches.add(cache);
			if (mem)
				memoryUsages.put(cache, cache.calculateInMemorySize());
		}

		if (mem)
			model.addAttribute("memory_usages", memoryUsages);
		model.addAttribute("caches", caches);
		model.addAttribute("row_classes", new String[] { "dark", "light" });

		return view;
	}

	@RequestMapping(method = RequestMethod.GET, params = "keys")
	public String handleRequestKeys(WebRequest request, @RequestParam
	String keys, Model model)
	{
		if (!request.isUserInRole(Role.ADMIN.name()))
			throw new NotAuthorizedException();

		model.addAttribute("keys", cacheManager.getEhcache(keys).getKeys());

		return viewKeys;
	}

	private class NameComparator implements Comparator<Ehcache>
	{
		public int compare(Ehcache c1, Ehcache c2)
		{
			return c1.getName().compareTo(c2.getName());
		}
	}

}
