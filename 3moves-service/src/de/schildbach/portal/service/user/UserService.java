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
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;

import de.schildbach.portal.persistence.user.Image;
import de.schildbach.portal.persistence.user.Role;
import de.schildbach.portal.persistence.user.Subject;
import de.schildbach.portal.persistence.user.SubjectRelation;
import de.schildbach.portal.persistence.user.User;
import de.schildbach.portal.persistence.user.UserTitle;
import de.schildbach.portal.service.user.bo.OwnProfileCommand;

/**
 * @author Andreas Schildbach
 */
public interface UserService
{
	/**
	 * @param user
	 *            name to check
	 * @return status of checked user name
	 */
	UserNameStatus checkUserName(String name);

	/**
	 * @param name
	 *            name to correct
	 * @return correct spelling mistakes in subject name
	 */
	String correctUserName(String name);

	List<String> suggestUserNames(String name);

	/**
	 * @param username
	 *            case sensitive user name
	 * @return user object or null if not found
	 */
	User user(String username);

	/**
	 * @param subjectname
	 *            case sensitive subject name
	 * @return subject object or null if not found
	 */
	Subject subject(String subjectname);

	List<User> search(String nameFilter, Role roleFilter, String orderBy, int maxResults);

	boolean validateEMailAddress(String email);

	Date isNewCutoffDate();

	// read/write

	void sendPasswordReminder(String username, String email, String password);

	void addUserRole(String loggedInUserName, String username, Role role);

	void removeUserRole(String loggedInUserName, String username, Role role);

	String[] standardLanguages();

	OwnProfileCommand ownProfileForUpdate(String username);

	void updateOwnProfile(String username, OwnProfileCommand profile);

	void changeLocale(String username, Locale locale, TimeZone timezone);

	void setNotifications(String username, boolean isActive);

	void setGameOptions(String username, boolean autoMove);

	void setScreenResolution(String username, int screenResolution);

	boolean setPassword(String username, String oldPassword, String newPassword);

	void changeReferredFrom(String username, String referredFrom);

	void requestEmailValidation(String username, String address, String key);

	void requestXmppValidation(String username, String xmpp, String key);

	void setEmail(String username, String email);

	void setXmpp(String username, String xmpp);

	boolean isUserActive(String username);

	// relations:

	void becomeFanOf(String subjectName, String targetSubjectName);

	void ban(String subjectName, String targetSubjectName);

	void removeSubjectRelation(String subjectName, String targetSubjectName);

	void setFriendConfirm(String username, String friendUsername, boolean confirm);

	boolean areFriends(String subjectName, String otherSubjectName);

	boolean isBannedBy(String subjectName, String bannedBySubjectName);

	SubjectRelation subjectRelation(String subjectName, String targetSubjectName);

	List<SubjectRelation> friendsOfSubject(String subjectName);

	List<SubjectRelation> fansOfSubject(String subjectName);

	List<Subject> bannedBySubject(String subjectName);

	Map<String, String> userRelations(String subjectName);

	// holidays:

	boolean canAddHolidays(String username, Date beginAt, Date endAt);

	void addHolidays(String username, Date beginAt, Date endAt);

	boolean isUserInHolidays(String username);

	boolean canRemoveHolidays(String username, Date beginAt);

	void removeHolidays(String username, Date beginAt);

	// terms:

	int effectiveTermsVersion();

	void acceptTerms(String username, int version);

	// invite friends:

	String[] inviteFriendText(String username);

	void inviteFriend(String username, String fromName, String fromAddr, String toAddr, String subject, String text);

	// photos:

	void setPhotoData(String username, byte[] photoData);

	Image photo(String username, int height);

	boolean hasPhoto(String username);

	void clearPhoto(String username);

	// title:

	void addUserTitleParticipant(UserTitleParticipant participant);

	List<UserTitle> userTitles(String username);

	void setTitle(String username, UserTitle title);
}
