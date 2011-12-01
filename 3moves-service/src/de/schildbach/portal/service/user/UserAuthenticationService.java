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

import de.schildbach.portal.persistence.user.Gender;
import de.schildbach.portal.persistence.user.User;
import de.schildbach.portal.service.user.exception.ExistingUserException;

/**
 * @author Andreas Schildbach
 */
public interface UserAuthenticationService
{
	void registerUser(String username, String password, Gender gender, Locale language, TimeZone timeZone, Date referredAt, String referredFrom,
			String referredTo) throws ExistingUserException;

	User loginByName(String username, String password, String ip, String useragent);

	User loginByEmail(String email, String password, String ip, String useragent);

	User loginByOpenId(String openId, String ip, String useragent);

	User loginAutomatically(String username, String ip, String useragent);

	User turnIntoUser(String oldUserName, String newUserName, String ip);

	void logoutManually(String username, int onlineTime);

	void logoutAutomatically(String username, int onlineTime);

	void changePassword(String username, String oldPassword, String newPassword);

	void setOpenId(String username, String openId);
}
