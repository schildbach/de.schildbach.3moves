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

package de.schildbach.portal.persistence;

import java.util.Properties;

import net.sf.ehcache.CacheManager;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.cache.Cache;
import org.hibernate.cache.CacheException;
import org.hibernate.cache.CacheProvider;
import org.hibernate.cache.EhCache;
import org.hibernate.cache.EhCacheProvider;
import org.hibernate.cache.Timestamper;
import org.springframework.beans.factory.annotation.Required;

/**
 * @author Andreas Schildbach
 */
public class InjectableEhCacheProvider implements CacheProvider
{
	private static final Log log = LogFactory.getLog(EhCacheProvider.class);

	private CacheManager cacheManager;
	
	@Required
	public void setCacheManager(CacheManager cacheManager)
	{
		this.cacheManager = cacheManager;
	}

	/**
	 * Builds a Cache.
	 * <p>
	 * Even though this method provides properties, they are not used. Properties for EHCache are
	 * specified in the ehcache.xml file. Configuration will be read from ehcache.xml for a cache
	 * declaration where the name attribute matches the name parameter in this builder.
	 * 
	 * @param name
	 *            the name of the cache. Must match a cache configured in ehcache.xml
	 * @param properties
	 *            not used
	 * @return a newly built cache will be built and initialised
	 * @throws CacheException
	 *             inter alia, if a cache of the same name already exists
	 */
	public Cache buildCache(String name, Properties properties) throws CacheException
	{
		try
		{
			net.sf.ehcache.Cache cache = cacheManager.getCache(name);
			if (cache == null)
			{
				log.warn("Could not find configuration [" + name + "]; using defaults.");
				cacheManager.addCache(name);
				cache = cacheManager.getCache(name);
				log.debug("started EHCache region: " + name);
			}
			return new EhCache(cache);
		}
		catch (net.sf.ehcache.CacheException e)
		{
			throw new CacheException(e);
		}
	}

	/**
	 * Returns the next timestamp.
	 */
	public long nextTimestamp()
	{
		return Timestamper.next();
	}

	public boolean isMinimalPutsEnabledByDefault()
	{
		return false;
	}

	public void start(Properties properties) throws CacheException
	{
	}

	public void stop()
	{
	}
}
