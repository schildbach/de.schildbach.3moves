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

package de.schildbach.portal.service.user;

import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.dao.DataRetrievalFailureException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import de.schildbach.portal.persistence.user.Gender;
import de.schildbach.portal.persistence.user.Role;
import de.schildbach.portal.persistence.user.User;
import de.schildbach.portal.persistence.user.UserDao;
import de.schildbach.portal.persistence.user.UserRole;
import de.schildbach.portal.service.exception.ApplicationException;
import de.schildbach.portal.service.user.exception.ExistingUserException;
import de.schildbach.portal.service.user.exception.InvalidLoginException;
import de.schildbach.web.RequestTime;

/**
 * @author Andreas Schildbach
 */
@Transactional
@Service
public class UserAuthenticationServiceImpl implements UserAuthenticationService
{
	@SuppressWarnings("unused")
	private static final Log LOG = LogFactory.getLog(UserAuthenticationServiceImpl.class);

	private UserDao userDao;

	@Required
	public void setUserDao(UserDao userDao)
	{
		this.userDao = userDao;
	}

	public void registerUser(String username, String password, Gender gender, Locale language, TimeZone timeZone, Date referredAt,
			String referredFrom, String referredTo) throws ExistingUserException
	{
		Date now = RequestTime.get();

		// check if username already taken
		// TODO: check all subject classes (e.g. system subjects)
		if (userDao.findUserCaseInsensitive(username, false) != null)
		{
			LOG.info("failed to register user: \"" + username + "\" already exists");
			throw new ExistingUserException(username);
		}

		// create bare user object
		User user = new User(now, username, language);

		// populate with parameters
		user.setPassword(password);
		user.setGender(gender);
		user.setTimeZone(timeZone);
		user.setScreenResolution(1024);
		user.setReferredAt(referredAt);
		user.setReferredFrom(referredFrom);
		user.setReferredTo(referredTo);

		// populate with defaults
		user.setFullNamePermission("friend");
		user.setAgePermission("user");
		user.setCityPermission("all");
		user.setCountryPermission("all");
		user.setOccupationPermission("user");
		user.setIsActiveNotification(true);

		// persist user
		userDao.create(user);

		// add user role to user
		user.getUserRoles().add(new UserRole(user, Role.USER));

		// log success
		LOG.info("registered user \"" + username + "\", gender=" + gender);
		if (referredFrom != null)
			LOG.info("user referred from: " + referredFrom);
	}

	public User loginByName(String name, String password, String ip, String useragent)
	{
		Date now = RequestTime.get();

		// try to load user
		User user = userDao.findUserCaseInsensitive(name, true);

		return internalLoginUserManually(name, password, ip, useragent, now, user);
	}

	public User loginByEmail(String email, String password, String ip, String useragent)
	{
		Date now = RequestTime.get();

		// try to load user
		User user = userDao.findUserByEmail(email);

		return internalLoginUserManually(email, password, ip, useragent, now, user);
	}

	public User loginByOpenId(String openId, String ip, String useragent)
	{
		Date now = RequestTime.get();

		// try to load user
		User user = userDao.findUserByOpenId(openId);

		// does user exist?
		if (user == null)
		{
			if (LOG.isInfoEnabled())
				LOG.info("someone from " + ip + " tried to login as \"" + openId + "\", but the user did not exist");

			return null;
		}

		// authentication already happened in the presentation layer

		// do internal
		internalLoginUser(user, now, ip, useragent);

		user.setLastLoginMethod("manual");

		// login successful
		if (LOG.isInfoEnabled())
			LOG.info("user \"" + user.getName() + "\" manually logged in from " + ip);

		return user;
	}

	private User internalLoginUserManually(String identification, String password, String ip, String useragent, Date now, User user)
	{
		// does user exist?
		if (user == null)
		{
			if (LOG.isInfoEnabled())
				LOG.info("someone from " + ip + " tried to login as \"" + identification + "\", but the user did not exist");

			return null;
		}

		// check password
		if (!user.checkPassword(password))
		{
			if (LOG.isInfoEnabled())
				LOG.info("someone from " + ip + " tried to login as \"" + identification + "\", but the password was incorrect");

			return null;
		}

		// do internal
		internalLoginUser(user, now, ip, useragent);

		user.setLastLoginMethod("manual");

		// login successful
		if (LOG.isInfoEnabled())
			LOG.info("user \"" + user.getName() + "\" manually logged in from " + ip);

		return user;
	}

	public User loginAutomatically(String username, String ip, String useragent)
	{
		Date now = RequestTime.get();

		// try to load user
		User user;
		try
		{
			user = userDao.loadUser(username, true);
		}
		catch (DataRetrievalFailureException x)
		{
			// user does not exist
			throw new InvalidLoginException(username);
		}

		// do internal
		internalLoginUser(user, now, ip, useragent);

		user.setLastLoginMethod("automatic");

		// login successful
		if (LOG.isInfoEnabled())
			LOG.info("user \"" + user.getName() + "\" automatically logged in from " + ip);

		return user;
	}

	public User turnIntoUser(String oldUserName, String newUserName, String ip)
	{
		// try to load user
		User newUser = userDao.findUserCaseInsensitive(newUserName, true);

		// does user exist?
		if (newUser == null)
		{
			if (LOG.isInfoEnabled())
				LOG.info("\"" + oldUserName + "\" from " + ip + " tried to turn into \"" + newUserName + "\", but the user did not exist");

			return null;
		}

		// 'turn into' successful
		if (LOG.isInfoEnabled())
			LOG.info("\"" + oldUserName + "\" from " + ip + " turned into \"" + newUser.getName() + "\"");

		return newUser;
	}

	private void internalLoginUser(User user, Date at, String ip, String useragent)
	{
		// update fields
		user.setNumberOfLogins(user.getNumberOfLogins() + 1);
		user.setLastLoginAt(at);
		user.setLastIP(ip);
		user.setLastUserAgent(useragent);
	}

	public void logoutManually(String username, int onlineTime)
	{
		Date now = RequestTime.get();

		// load user
		User user = userDao.read(User.class, username);

		// update fields
		user.setLastLogoutAt(now);
		user.setLastLogoutMethod("manual");
		user.setTotalOnlineTime(user.getTotalOnlineTime() + onlineTime);

		// logout successful
		LOG.info("user \"" + username + "\" manually logged out");
	}

	// TODO @Transactional(propagation = Propagation.NESTED), because logout can happen inbetween
	public void logoutAutomatically(String username, int onlineTime)
	{
		Date now = RequestTime.get();

		// load user
		User user = userDao.read(User.class, username);

		// update fields
		user.setLastLogoutAt(now);
		user.setLastLogoutMethod("automatic");
		user.setTotalOnlineTime(user.getTotalOnlineTime() + onlineTime);

		// logout successful
		LOG.info("user \"" + username + "\" automatically logged out");
	}

	public void changePassword(String username, String oldPassword, String newPassword)
	{
		// load user
		User user = userDao.read(User.class, username);

		// check old password
		if (!user.checkPassword(oldPassword))
			throw new ApplicationException("wrong password");

		// check new password
		if (newPassword == null)
			throw new NullPointerException("newPassword is null");

		// change password
		user.setPassword(newPassword);
	}

	public void setOpenId(String username, String openId)
	{
		// load user
		User user = userDao.read(User.class, username);

		// set openid
		user.setOpenId(openId);
	}
}
