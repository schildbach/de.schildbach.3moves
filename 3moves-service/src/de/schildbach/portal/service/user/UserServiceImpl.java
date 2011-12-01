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

import java.text.MessageFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.PropertyResourceBundle;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.TimeZone;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import de.schildbach.portal.message.user.MessageDao;
import de.schildbach.portal.persistence.user.Image;
import de.schildbach.portal.persistence.user.ImageDao;
import de.schildbach.portal.persistence.user.RelationType;
import de.schildbach.portal.persistence.user.Role;
import de.schildbach.portal.persistence.user.Subject;
import de.schildbach.portal.persistence.user.SubjectRelation;
import de.schildbach.portal.persistence.user.SubjectRelationDao;
import de.schildbach.portal.persistence.user.User;
import de.schildbach.portal.persistence.user.UserDao;
import de.schildbach.portal.persistence.user.UserHolidays;
import de.schildbach.portal.persistence.user.UserRole;
import de.schildbach.portal.persistence.user.UserTitle;
import de.schildbach.portal.service.exception.ApplicationException;
import de.schildbach.portal.service.exception.NotAuthorizedException;
import de.schildbach.portal.service.user.bo.OwnProfileCommand;
import de.schildbach.portal.service.user.bo.Permission;
import de.schildbach.util.TextWrapper;
import de.schildbach.web.RequestTime;

/**
 * @author Andreas Schildbach
 */
@Transactional
@Service
public class UserServiceImpl implements UserService, ApplicationContextAware
{
	@SuppressWarnings("unused")
	private static final Log LOG = LogFactory.getLog(UserServiceImpl.class);

	private static final String RESOURCEBUNDLE_NAME = "user";
	private static final int EFFECTIVE_TERMS_VERSION = 1;
	private static final int MILLISECONDS_PER_DAY = 24 * 60 * 60 * 1000;
	private static final int IS_USER_ACTIVE_DELAY_FIELD = Calendar.MONTH;
	private static final int IS_USER_ACTIVE_DELAY_VALUE = 1;

	private UserDao userDao;
	private SubjectRelationDao subjectRelationDao;
	private ImageDao imageDao;
	private MessageDao messageDao;
	private ApplicationContext ctx;

	private Set<UserTitleParticipant> userTitleParticipants = new HashSet<UserTitleParticipant>();

	private Pattern eMailPattern;

	public UserServiceImpl()
	{
		eMailPattern = Pattern.compile("^[A-Za-z0-9\\._-]+[@]([A-Za-z0-9_-]+([.][A-Za-z0-9_-]+)+[A-Za-z])$");
	}

	@Required
	public void setUserDao(UserDao userDao)
	{
		this.userDao = userDao;
	}

	@Required
	public void setSubjectRelationDao(SubjectRelationDao subjectRelationDao)
	{
		this.subjectRelationDao = subjectRelationDao;
	}

	@Required
	public void setImageDao(ImageDao imageDao)
	{
		this.imageDao = imageDao;
	}

	@Required
	public void setMessageDao(MessageDao messageDao)
	{
		this.messageDao = messageDao;
	}

	@Required
	public void setApplicationContext(ApplicationContext applicationContext)
	{
		this.ctx = applicationContext;
	}

	public UserNameStatus checkUserName(String name)
	{
		if (name == null)
			return UserNameStatus.AVAILABLE;

		Subject subject = userDao.findUserCaseInsensitive(name, false);
		if (subject == null)
			return UserNameStatus.AVAILABLE;

		if (subject.getName().equalsIgnoreCase(name) && !subject.getName().equals(name))
			return UserNameStatus.REGISTERED_BUT_MISSPELLED;

		return UserNameStatus.REGISTERED;
	}

	public String correctUserName(String name)
	{
		Subject user = userDao.findUserCaseInsensitive(name, false);
		return user.getName();
	}

	public List<String> suggestUserNames(String name)
	{
		return userDao.findUserNames(name, 100);
	}

	public User user(String username)
	{
		return (User) userDao.findSubject(User.class, username);
	}

	public Subject subject(String subjectname)
	{
		return userDao.read(subjectname);
	}

	public List<User> search(String nameFilter, Role roleFilter, String orderBy, int maxResults)
	{
		return userDao.findUsers(nameFilter, roleFilter, orderBy, maxResults);
	}

	public boolean validateEMailAddress(String email)
	{
		if (email == null)
			return false;

		Matcher matcher = eMailPattern.matcher(email);

		// check pattern
		if (!matcher.matches())
			return false;

		return true;
	}

	public Date isNewCutoffDate()
	{
		Calendar calendar = new GregorianCalendar();
		calendar.setTime(RequestTime.get());
		calendar.add(Calendar.WEEK_OF_YEAR, -1);
		return calendar.getTime();
	}

	public void sendPasswordReminder(String username, String email, String password)
	{
		// load user
		User user = userDao.read(User.class, username);

		ResourceBundle bundle = PropertyResourceBundle.getBundle(RESOURCEBUNDLE_NAME, user.getLocale());
		String subject = bundle.getString("mail_subject_welcome");
		String text = MessageFormat.format(bundle.getString("mail_text_welcome"), username, username, password);

		messageDao.sendEmail(null, null, email, subject, text);
	}

	public void addUserRole(String loggedInUserName, String username, Role role)
	{
		// load user
		User loggedInUser = userDao.loadUser(loggedInUserName, true);

		// is admin?
		if (!loggedInUser.isUserInRole(Role.ADMIN))
			throw new NotAuthorizedException();

		// load objects
		User user = userDao.loadUser(username, true);

		// is user not yet in role?
		if (user.isUserInRole(role))
			throw new ApplicationException("user already got role");

		user.getUserRoles().add(new UserRole(user, role));

		// user role add successful
		LOG.info("user \"" + loggedInUserName + "\" added role \"" + role + "\" to user \"" + username + "\"");
	}

	public void removeUserRole(String loggedInUserName, String username, Role role)
	{
		// load user
		User loggedInUser = userDao.loadUser(loggedInUserName, true);

		// is admin?
		if (!loggedInUser.isUserInRole(Role.ADMIN))
			throw new NotAuthorizedException();

		// load objects
		User user = userDao.loadUser(username, true);

		// is user in role?
		UserRole userRole = user.findUserRole(role);
		if (userRole == null)
			throw new ApplicationException("user is not in role");

		// clear title if necessary
		if (userRole.equals(user.getTitle()))
			user.setTitle(null);

		// remove user role from user
		user.getUserRoles().remove(userRole);

		// user role remove successful
		LOG.info("user \"" + loggedInUserName + "\" removed role \"" + role + "\" from user \"" + username + "\"");
	}

	public String[] standardLanguages()
	{
		return new String[] { "de", "en", "fr", "es" };
	}

	public OwnProfileCommand ownProfileForUpdate(String username)
	{
		// load user
		User user = userDao.read(User.class, username);

		// populate profile
		OwnProfileCommand profile = new OwnProfileCommand();
		profile.setFullName(user.getFullName());
		profile.setFullNamePermission(Permission.valueOf(user.getFullNamePermission().toUpperCase()));
		profile.setGender(user.getGender());
		profile.setBirthday(user.getBirthday());
		profile.setAgePermission(Permission.valueOf(user.getAgePermission().toUpperCase()));
		profile.setCity(user.getCity());
		profile.setCityPermission(Permission.valueOf(user.getCityPermission().toUpperCase()));
		profile.setCountry(user.getCountry());
		profile.setCountryPermission(Permission.valueOf(user.getCountryPermission().toUpperCase()));
		profile.setOccupation(user.getOccupation());
		profile.setOccupationPermission(Permission.valueOf(user.getOccupationPermission().toUpperCase()));
		profile.setDescription(user.getDescription());

		// languages
		List<String> languages = new LinkedList<String>();
		if (user.getLanguages() != null)
			languages.addAll(Arrays.asList(user.getLanguages().split(",")));
		for (String language : standardLanguages())
		{
			if (languages.remove(language))
			{
				profile.getLanguages().add(language);
			}
		}
		profile.setOtherLanguages(join(languages, ','));

		return profile;
	}

	private String join(Collection<String> parts, char delim)
	{
		StringBuilder str = new StringBuilder();
		for (String part : parts)
		{
			str.append(part);
			str.append(delim);
		}
		if (str.length() > 0)
			str.setLength(str.length() - 1);
		return str.toString();
	}

	public void updateOwnProfile(String username, OwnProfileCommand profile)
	{
		// load user
		User user = userDao.read(User.class, username);

		// update user
		user.setFullName(profile.getFullName());
		user.setFullNamePermission(profile.getFullNamePermission().name().toLowerCase());
		user.setGender(profile.getGender());
		user.setBirthday(profile.getBirthday());
		user.setAgePermission(profile.getAgePermission().name().toLowerCase());
		user.setCity(profile.getCity());
		user.setCityPermission(profile.getCityPermission().name().toLowerCase());
		user.setCountry(profile.getCountry());
		user.setCountryPermission(profile.getCountryPermission().name().toLowerCase());
		user.setOccupation(profile.getOccupation());
		user.setOccupationPermission(profile.getOccupationPermission().name().toLowerCase());
		user.setDescription(profile.getDescription());

		// languages
		StringBuilder languages = new StringBuilder();
		for (String language : standardLanguages())
		{
			if (profile.getLanguages().contains(language))
			{
				languages.append(language);
				languages.append(",");
			}
		}
		if (profile.getOtherLanguages() != null)
			languages.append(profile.getOtherLanguages());
		else if (languages.length() > 0)
			languages.setLength(languages.length() - 1);
		user.setLanguages(languages.toString());
	}

	public void changeLocale(String username, Locale locale, TimeZone timezone)
	{
		// load user
		User user = userDao.read(User.class, username);

		// change fields
		user.setLocale(locale);
		user.setTimeZone(timezone);
	}

	public void setNotifications(String username, boolean isActive)
	{
		// load user
		User user = userDao.read(User.class, username);

		// set notifications
		user.setIsActiveNotification(isActive);
	}

	public void setGameOptions(String username, boolean autoMove)
	{
		// load user
		User user = userDao.read(User.class, username);

		// set notifications
		user.setAutoMove(autoMove);
	}

	public void setScreenResolution(String username, int screenResolution)
	{
		// load user
		User user = userDao.read(User.class, username);

		// change screen resolution
		user.setScreenResolution(screenResolution);
	}

	public boolean setPassword(String username, String oldPassword, String newPassword)
	{
		// load user
		User user = userDao.read(User.class, username);

		// verify old password
		if (user.hasPassword() && !user.checkPassword(oldPassword))
		{
			LOG.info("user \"" + username + "\" failed to change his password");
			return false;
		}

		// set password
		user.setPassword(newPassword);

		LOG.info("user \"" + username + "\" changed password");

		return true;
	}

	public void changeReferredFrom(String username, String referredFrom)
	{
		// load user
		User user = userDao.read(User.class, username);

		// change fields
		if (user.getReferredFrom() == null)
			user.setReferredFrom(referredFrom);
	}

	public void becomeFanOf(String subjectName, String targetSubjectName)
	{
		addUserRelation(subjectName, targetSubjectName, RelationType.FRIEND);
	}

	public void ban(String subjectName, String targetSubjectName)
	{
		addUserRelation(subjectName, targetSubjectName, RelationType.BANNED);
	}

	private void addUserRelation(String subjectName, String targetSubjectName, RelationType type)
	{
		Date now = RequestTime.get();

		// load users
		User user = userDao.read(User.class, subjectName);
		User friend = userDao.findUserCaseInsensitive(targetSubjectName, false);
		if (friend == null)
			throw new ApplicationException("invalid_user");

		// relation to yourself?
		if (friend.equals(user))
			throw new ApplicationException("must_not_relate_to_yourself");

		// add friend
		SubjectRelation relation = internalAddOrUpdateRelation(now, user, friend, type);

		// bi-directional auto-confirm
		SubjectRelation reverseRelation = subjectRelationDao.findSubjectRelation(friend, user);
		if (reverseRelation != null)
		{
			if (reverseRelation.isFriend())
			{
				relation.setConfirmed(Boolean.TRUE);

				if (type == RelationType.FRIEND)
					reverseRelation.setConfirmed(Boolean.TRUE);
				else if (type == RelationType.BANNED)
					reverseRelation.setConfirmed(Boolean.FALSE);
			}
			else if (reverseRelation.isBanned())
			{
				if (type == RelationType.FRIEND)
					relation.setConfirmed(Boolean.FALSE);
			}
		}
	}

	private SubjectRelation internalAddOrUpdateRelation(Date at, User source, User target, RelationType type)
	{
		SubjectRelation relation = subjectRelationDao.findSubjectRelation(source, target);
		if (relation == null)
		{
			relation = new SubjectRelation(at, source, target, type);
			subjectRelationDao.create(relation);
		}
		else
		{
			if (!relation.getType().equals(type))
			{
				relation.setType(type);
				relation.setConfirmed(null);
			}
		}
		return relation;
	}

	public void setFriendConfirm(String username, String targetUsername, boolean confirm)
	{
		// load users
		User user = userDao.read(User.class, username);
		User target = userDao.read(User.class, targetUsername);

		// find "backward" relation
		SubjectRelation backwardRelation = subjectRelationDao.findSubjectRelation(user, target);

		// not allowed if target is listed as friend of user
		if (backwardRelation != null && backwardRelation.isFriend())
			throw new NotAuthorizedException("target is listed as friend of user");

		// find relation
		SubjectRelation relation = subjectRelationDao.findSubjectRelation(target, user);

		// is friendship?
		if (relation == null || !relation.isFriend())
			throw new NotAuthorizedException("is no friendship");

		// confirm friend
		relation.setConfirmed(confirm);
	}

	public void removeSubjectRelation(String subjectName, String targetSubjectName)
	{
		// load objects
		Subject subject = userDao.read(subjectName);
		Subject targetSubject = userDao.read(targetSubjectName);
		SubjectRelation relation = subjectRelationDao.findSubjectRelation(subject, targetSubject);

		// remove contact
		subjectRelationDao.delete(relation);
	}

	public boolean areFriends(String subjectName, String otherSubjectName)
	{
		// forward relation
		SubjectRelation relation = subjectRelation(subjectName, otherSubjectName);
		if (relation == null || relation.getType() != RelationType.FRIEND)
			return false;

		// reverse relation
		SubjectRelation reverseRelation = subjectRelation(otherSubjectName, subjectName);
		if (reverseRelation == null || reverseRelation.getType() != RelationType.FRIEND)
			return false;

		return true;
	}

	public boolean isBannedBy(String subjectName, String bannedBySubjectName)
	{
		SubjectRelation relation = subjectRelation(bannedBySubjectName, subjectName);

		if (relation != null)
			return relation.getType() == RelationType.BANNED;
		else
			return false;
	}

	public SubjectRelation subjectRelation(String subjectName, String targetSubjectName)
	{
		// load subjects
		Subject subject = userDao.read(subjectName);
		Subject targetSubject = userDao.read(targetSubjectName);

		// find relation
		return subjectRelationDao.findSubjectRelation(subject, targetSubject);
	}

	public List<SubjectRelation> friendsOfSubject(String subjectName)
	{
		// load subject
		Subject subject = userDao.read(subjectName);

		// find friends
		return subjectRelationDao.findSubjectRelationsBySource(subject, RelationType.FRIEND);
	}

	public List<SubjectRelation> fansOfSubject(String subjectName)
	{
		// load user
		Subject subject = userDao.read(subjectName);

		// find reverse relations to user
		List<SubjectRelation> relations = subjectRelationDao.findSubjectRelationsByTarget(subject);

		// filter only reverse friends that are not any forward relations of the friend
		for (Iterator<SubjectRelation> i = relations.iterator(); i.hasNext();)
		{
			SubjectRelation relation = i.next();
			if (!relation.isFriend() || subjectRelationDao.findSubjectRelation(relation.getTargetSubject(), relation.getSourceSubject()) != null)
				i.remove();
		}

		return relations;
	}

	public List<Subject> bannedBySubject(String subjectName)
	{
		// load subject
		Subject subject = userDao.read(subjectName);

		// find friends
		List<Subject> bannedSubjects = new LinkedList<Subject>();
		for (SubjectRelation relation : subjectRelationDao.findSubjectRelationsBySource(subject, RelationType.BANNED))
			bannedSubjects.add(relation.getTargetSubject());

		return bannedSubjects;
	}

	public Map<String, String> userRelations(String subjectName)
	{
		// load subject
		Subject subject = userDao.read(subjectName);

		// find relations
		List<SubjectRelation> relations = subjectRelationDao.findSubjectRelationsBySource(subject, null);

		// build result map
		Map<String, String> result = new HashMap<String, String>();
		for (SubjectRelation relation : relations)
			result.put(relation.getTargetSubject().getName(), relation.getType().name().toLowerCase());
		result.put(subjectName, "me");

		return result;
	}

	public void requestEmailValidation(String username, String email, String key)
	{
		// load user
		User user = userDao.read(User.class, username);
		Locale locale = user.getLocale();

		// request validation
		String link = ctx.getMessage("url.validate_email", new Object[] { key }, locale);
		String subject = ctx.getMessage("validate_email.subject", new Object[0], locale);
		String text = ctx.getMessage("validate_email.text", new Object[] { user.getName(), email, link }, locale);

		// send message
		if (LOG.isInfoEnabled())
			LOG.info("requesting address validation by " + user.getName() + " via email " + email);
		messageDao.sendEmail(null, null, email, subject, text);
	}

	public void requestXmppValidation(String username, String xmpp, String key)
	{
		// load user
		User user = userDao.read(User.class, username);
		Locale locale = user.getLocale();

		// request validation
		String link = ctx.getMessage("url.validate_xmpp", new Object[] { key }, locale);
		String subject = ctx.getMessage("validate_xmpp.subject", new Object[0], locale);
		String text = ctx.getMessage("validate_xmpp.text", new Object[] { user.getName(), xmpp, link }, locale);

		// send message
		if (LOG.isInfoEnabled())
			LOG.info("requesting address validation by " + user.getName() + " via xmpp " + xmpp);
		messageDao.sendInstantMessage("xmpp", xmpp, subject, text);
	}

	public void setEmail(String username, String email)
	{
		// load user
		User user = userDao.read(User.class, username);

		// set email
		user.setEmail(email);
	}

	public void setXmpp(String username, String xmpp)
	{
		// load user
		User user = userDao.read(User.class, username);

		// set xmpp
		user.setXmpp(xmpp);
	}

	public boolean isUserActive(String username)
	{
		Date now = RequestTime.get();

		// load user
		User user = userDao.read(User.class, username);
		if (user.getLastLoginAt() == null)
			return false;

		//
		Calendar calendar = new GregorianCalendar();
		calendar.setTime(now);
		calendar.add(IS_USER_ACTIVE_DELAY_FIELD, -IS_USER_ACTIVE_DELAY_VALUE);
		return user.getLastLoginAt().after(calendar.getTime());
	}

	public boolean canAddHolidays(String username, Date beginAt, Date endAt)
	{
		Date now = RequestTime.get();

		// begin < end?
		if (beginAt.compareTo(endAt) >= 0)
			return false;

		// begin > current date?
		if (beginAt.compareTo(now) <= 0)
			return false;

		// length >= 2 days?
		if (UserHolidays.getLength(beginAt, endAt) < 2 * MILLISECONDS_PER_DAY)
			return false;

		// load user
		User user = userDao.read(User.class, username);

		// accumulate length and check for overlap
		Calendar calendar = new GregorianCalendar();
		calendar.setTime(endAt);
		calendar.add(Calendar.YEAR, -1);
		Date refDate = calendar.getTime();

		long accumLength = 0;
		for (UserHolidays holidays : user.getUserHolidays())
		{
			if (overlap(holidays.getBeginAt(), holidays.getEndAt(), beginAt, endAt))
				return false;

			if (holidays.getBeginAt().compareTo(refDate) > 0)
				accumLength += holidays.getLength();
		}

		// accumulated length in the last year <= 21?
		if (accumLength + UserHolidays.getLength(beginAt, endAt) > 21 * MILLISECONDS_PER_DAY)
			return false;

		return true;
	}

	public void addHolidays(String username, Date beginAt, Date endAt)
	{
		Date now = RequestTime.get();

		// can add holidays?
		if (!canAddHolidays(username, beginAt, endAt))
			throw new NotAuthorizedException();

		// load user
		User user = userDao.read(User.class, username);

		// new holidays
		UserHolidays holidays = new UserHolidays(now, user, beginAt, endAt);

		// add holidays
		user.getUserHolidays().add(holidays);
	}

	private boolean overlap(Date lower1, Date upper1, Date lower2, Date upper2)
	{
		if (between(lower1, lower2, upper2))
			return true;
		if (between(lower2, lower1, upper1))
			return true;
		return false;
	}

	private boolean between(Date date, Date lower, Date upper)
	{
		return date.compareTo(lower) >= 0 && date.compareTo(upper) < 0;
	}

	public boolean isUserInHolidays(String username)
	{
		Date now = RequestTime.get();

		// load user
		User user = userDao.read(User.class, username);

		Calendar calendar = new GregorianCalendar();
		for (UserHolidays holidays : user.getUserHolidays())
		{
			calendar.setTime(holidays.getEndAt());
			calendar.add(Calendar.DAY_OF_YEAR, 1);
			if (between(now, holidays.getBeginAt(), calendar.getTime()))
				return true;
		}

		return false;
	}

	public boolean canRemoveHolidays(String username, Date begin)
	{
		Date now = RequestTime.get();

		if (now.compareTo(begin) >= 0)
			return false;

		return true;
	}

	public void removeHolidays(String username, Date beginAt)
	{
		// can remove holidays?
		if (!canRemoveHolidays(username, beginAt))
			throw new NotAuthorizedException();

		// load user
		User user = userDao.read(User.class, username);

		// find holidays
		UserHolidays holidays = findUserHolidays(user, beginAt);

		// remove holidays
		user.getUserHolidays().remove(holidays);
	}

	private UserHolidays findUserHolidays(User user, Date beginAt)
	{
		for (UserHolidays holidays : user.getUserHolidays())
		{
			if (holidays.getBeginAt().getTime() == beginAt.getTime())
				return holidays;
		}
		return null;
	}

	public int effectiveTermsVersion()
	{
		return EFFECTIVE_TERMS_VERSION;
	}

	public void acceptTerms(String username, int version)
	{
		Date now = RequestTime.get();

		// load user
		User user = userDao.read(User.class, username);

		if (user.getAcceptedTerms() == version)
			throw new NotAuthorizedException("version already accepted");

		if (version != EFFECTIVE_TERMS_VERSION)
			throw new NotAuthorizedException("version not effective");

		// accept terms
		user.setAcceptedTerms(version);
		user.setAcceptedTermsAt(now);
	}

	public String[] inviteFriendText(String username)
	{
		// load objects
		User user = userDao.read(User.class, username);
		Locale locale = user.getLocale();

		// locate resources
		String baseUrl = ctx.getMessage("url.base", null, locale);
		String fromName = user.getFullName();

		// provide mail subject and text
		final String[] text = new String[3];
		String prefix = "invite_friends.";
		text[0] = ctx.getMessage(prefix + "subject", new Object[] { fromName.length() > 0 ? fromName : "<dein name>" }, locale);
		text[1] = ctx.getMessage(prefix + "text", null, locale);
		text[2] = ctx.getMessage(prefix + "static_text", new Object[] { baseUrl }, locale);

		return text;
	}

	public void inviteFriend(String username, String fromName, String fromAddr, String toAddr, String subject, String text)
	{
		// load objects
		User user = userDao.read(User.class, username);
		Locale locale = user.getLocale();

		// locate resources
		String baseUrl = ctx.getMessage("url.base", null, locale);
		String prefix = "invite_friends.";
		String staticText = ctx.getMessage(prefix + "static_text", new Object[] { baseUrl }, locale);

		// append static text
		text += "\n\n" + TextWrapper.wrap(staticText, 80);

		// send email
		messageDao.sendEmail(fromName, fromAddr, toAddr, subject, text);
	}

	// photos:

	public void setPhotoData(String username, byte[] photoData)
	{
		Date now = RequestTime.get();

		// load user
		User user = userDao.read(User.class, username);

		// clear photo(s)
		for (Image image : imageDao.findImages(user))
		{
			imageDao.delete(image);
		}

		// create new photo
		Image image = new Image(now, user, photoData);
		imageDao.create(image);
	}

	public Image photo(String username, int height)
	{
		// load user
		User user = userDao.read(User.class, username);

		// get photo data
		List<Image> images = imageDao.findImages(user);
		if (images.isEmpty())
			return null;
		Image image = images.get(0);

		// return data
		if (height == 128)
			return image;
		else
			return null;
	}

	public boolean hasPhoto(String username)
	{
		// load user
		User user = userDao.read(User.class, username);

		// has photo?
		boolean hasPhoto = !imageDao.findImages(user).isEmpty();

		return hasPhoto;
	}

	public void clearPhoto(String username)
	{
		// load user
		User user = userDao.read(User.class, username);

		// clear photo(s)
		for (Image hibernateImage : imageDao.findImages(user))
		{
			imageDao.delete(hibernateImage);
		}
	}

	public void addUserTitleParticipant(UserTitleParticipant participant)
	{
		userTitleParticipants.add(participant);
	}

	public List<UserTitle> userTitles(String username)
	{
		// load user
		User user = userDao.loadUser(username, true);

		LinkedList<UserTitle> titles = new LinkedList<UserTitle>();
		titles.addAll(user.getUserRoles());
		for (UserTitleParticipant participant : userTitleParticipants)
			titles.addAll(participant.userTitles(user));

		return titles;
	}

	public void setTitle(String username, UserTitle title)
	{
		// load user
		User user = userDao.read(User.class, username);

		// todo security

		// set title
		user.setTitle(title);

		// log
		LOG.info("user " + user.getName() + " is setting his title to " + title);
	}
}
