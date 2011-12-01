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

package de.schildbach.portal.persistence.user;

import java.security.Principal;
import java.util.Date;
import java.util.Locale;
import java.util.Set;
import java.util.TimeZone;
import java.util.TreeSet;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Transient;

import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.Sort;
import org.hibernate.annotations.SortType;
import org.hibernate.validator.NotNull;

import de.schildbach.portal.persistence.game.SubjectRating;

/**
 * @author Andreas Schildbach
 */
@Entity
@DiscriminatorValue("user")
public class User extends Subject implements Principal
{
	public static final String PROPERTY_OPEN_ID = "openId";
	public static final String PROPERTY_EMAIL = "email";
	public static final String PROPERTY_USER_ROLES = "userRoles";
	public static final String COLUMN_FULL_NAME = "full_name";

	// account
	private String password;
	private String openId;

	// profile
	private String email;
	private String xmpp;
	private String fullName;
	private String fullNamePermission;
	private Gender gender;
	private Date birthday;
	private String agePermission;
	private String city;
	private String cityPermission;
	private String country;
	private String countryPermission;
	private String occupation;
	private String occupationPermission;
	private String languages;
	private String description;

	// title
	private UserRole roleTitle;
	private SubjectRating ratingTitle;

	// settings
	private Locale locale;
	private TimeZone timeZone;
	private Integer screenResolution;
	private boolean isActiveNotification;

	// statistics
	private Date referredAt;
	private String referredFrom;
	private String referredTo;
	private Date lastLoginAt;
	private String lastLoginMethod;
	private Date lastLogoutAt;
	private String lastLogoutMethod;
	private int numberOfLogins;
	private int totalOnlineTime;
	private String lastIP;
	private String lastUserAgent;
	private int acceptedTerms;
	private Date acceptedTermsAt;

	// inverse ends
	private Set<UserRole> userRoles;
	private Set<UserHolidays> userHolidays;

	protected User()
	{
	}

	public User(Date createdAt, String userName, Locale locale)
	{
		super(createdAt, userName);
		this.setLocale(locale);

		// defaults
		this.setUserRoles(new TreeSet<UserRole>());
		this.setUserHolidays(new TreeSet<UserHolidays>());
	}

	@Override
	public void accept(SubjectVisitor visitor)
	{
		visitor.visit(this);
	}

	@Override
	public String toString()
	{
		return super.toString().replaceAll("password=[^,]*", "password=***");
	}

	@Column(name = "password", length = 16)
	@SuppressWarnings("unused")
	private String getPassword()
	{
		return this.password;
	}

	public void setPassword(String password)
	{
		this.password = password;
	}

	public boolean hasPassword()
	{
		return this.password != null;
	}

	public boolean checkPassword(String password)
	{
		return hasPassword() && password.equals(this.password);
	}

	@Column(name = "open_id", length = 128)
	public String getOpenId()
	{
		return openId;
	}

	public void setOpenId(String openId)
	{
		this.openId = openId;
	}

	@Column(name = "email", length = 64)
	public String getEmail()
	{
		return email;
	}

	public void setEmail(String email)
	{
		this.email = email;
	}

	@Column(name = "xmpp", length = 64)
	public String getXmpp()
	{
		return xmpp;
	}

	public void setXmpp(String xmpp)
	{
		this.xmpp = xmpp;
	}

	@Column(name = COLUMN_FULL_NAME, length = 64)
	public String getFullName()
	{
		return this.fullName;
	}

	public void setFullName(String fullName)
	{
		this.fullName = fullName;
	}

	@NotNull
	@Column(name = "full_name_permission")
	public String getFullNamePermission()
	{
		return this.fullNamePermission;
	}

	public void setFullNamePermission(String fullNamePermission)
	{
		this.fullNamePermission = fullNamePermission;
	}

	@Column(name = "gender")
	@Enumerated(EnumType.STRING)
	public Gender getGender()
	{
		return this.gender;
	}

	public void setGender(Gender gender)
	{
		this.gender = gender;
	}

	@Column(name = "birthday")
	@Temporal(TemporalType.DATE)
	public Date getBirthday()
	{
		return this.birthday;
	}

	public void setBirthday(Date birthday)
	{
		this.birthday = birthday;
	}

	@NotNull
	@Column(name = "age_permission")
	public String getAgePermission()
	{
		return this.agePermission;
	}

	public void setAgePermission(String agePermission)
	{
		this.agePermission = agePermission;
	}

	@Column(name = "city", length = 48)
	public String getCity()
	{
		return this.city;
	}

	public void setCity(String city)
	{
		this.city = city;
	}

	@NotNull
	@Column(name = "city_permission")
	public String getCityPermission()
	{
		return this.cityPermission;
	}

	public void setCityPermission(String cityPermission)
	{
		this.cityPermission = cityPermission;
	}

	@Column(name = "country", length = 48)
	public String getCountry()
	{
		return this.country;
	}

	public void setCountry(String country)
	{
		this.country = country;
	}

	@NotNull
	@Column(name = "country_permission")
	public String getCountryPermission()
	{
		return this.countryPermission;
	}

	public void setCountryPermission(String countryPermission)
	{
		this.countryPermission = countryPermission;
	}

	@Column(name = "occupation", length = 64)
	public String getOccupation()
	{
		return this.occupation;
	}

	public void setOccupation(String occupation)
	{
		this.occupation = occupation;
	}

	@NotNull
	@Column(name = "occupation_permission")
	public String getOccupationPermission()
	{
		return this.occupationPermission;
	}

	public void setOccupationPermission(String occupationPermission)
	{
		this.occupationPermission = occupationPermission;
	}

	@Column(name = "languages", length = 128)
	public String getLanguages()
	{
		return this.languages;
	}

	public void setLanguages(String languages)
	{
		this.languages = languages;
	}

	@Column(name = "description")
	public String getDescription()
	{
		return this.description;
	}

	public void setDescription(String description)
	{
		this.description = description;
	}

	@Transient
	public UserTitle getTitle()
	{
		if (getRoleTitle() != null)
			return getRoleTitle();
		if (getRatingTitle() != null)
			return getRatingTitle();
		return null;
	}

	public void setTitle(UserTitle title)
	{
		setRoleTitle(null);
		setRatingTitle(null);

		if (title != null)
		{
			title.accept(new UserTitleVisitor()
			{
				public void visit(UserRole userRole)
				{
					setRoleTitle(userRole);
				}

				public void visit(SubjectRating subjectRating)
				{
					setRatingTitle(subjectRating);
				}
			});
		}
	}

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "role_title")
	private UserRole getRoleTitle()
	{
		return roleTitle;
	}

	private void setRoleTitle(UserRole titleRole)
	{
		this.roleTitle = titleRole;
	}

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "rating_title")
	private SubjectRating getRatingTitle()
	{
		return ratingTitle;
	}

	private void setRatingTitle(SubjectRating titleRating)
	{
		this.ratingTitle = titleRating;
	}

	@NotNull
	@Column(name = "locale")
	public Locale getLocale()
	{
		return this.locale;
	}

	public void setLocale(Locale locale)
	{
		this.locale = locale;
	}

	@Column(name = "timezone")
	public TimeZone getTimeZone()
	{
		return this.timeZone;
	}

	public void setTimeZone(TimeZone timeZone)
	{
		this.timeZone = timeZone;
	}

	@Column(name = "screen_resolution")
	public Integer getScreenResolution()
	{
		return this.screenResolution;
	}

	public void setScreenResolution(Integer screenResolution)
	{
		this.screenResolution = screenResolution;
	}

	@NotNull
	@Column(name = "is_active_notification")
	public boolean getIsActiveNotification()
	{
		return this.isActiveNotification;
	}

	public void setIsActiveNotification(boolean isActiveNotification)
	{
		this.isActiveNotification = isActiveNotification;
	}

	@Column(name = "referred_at")
	@Temporal(TemporalType.TIMESTAMP)
	public Date getReferredAt()
	{
		return this.referredAt;
	}

	public void setReferredAt(Date referredAt)
	{
		this.referredAt = referredAt;
	}

	@Column(name = "referrer_from", length = 255)
	public String getReferredFrom()
	{
		return this.referredFrom;
	}

	public void setReferredFrom(String referredFrom)
	{
		this.referredFrom = referredFrom;
	}

	@Column(name = "referred_to", length = 128)
	public String getReferredTo()
	{
		return this.referredTo;
	}

	public void setReferredTo(String referredTo)
	{
		this.referredTo = referredTo;
	}

	@Column(name = "last_login_at")
	@Temporal(TemporalType.TIMESTAMP)
	public Date getLastLoginAt()
	{
		return this.lastLoginAt;
	}

	public void setLastLoginAt(Date lastLoginAt)
	{
		this.lastLoginAt = lastLoginAt;
	}

	@Column(name = "last_login_method")
	public String getLastLoginMethod()
	{
		return this.lastLoginMethod;
	}

	public void setLastLoginMethod(String lastLoginMethod)
	{
		this.lastLoginMethod = lastLoginMethod;
	}

	@Column(name = "last_logout_at")
	@Temporal(TemporalType.TIMESTAMP)
	public Date getLastLogoutAt()
	{
		return this.lastLogoutAt;
	}

	public void setLastLogoutAt(Date lastLogoutAt)
	{
		this.lastLogoutAt = lastLogoutAt;
	}

	@Column(name = "last_logout_method")
	public String getLastLogoutMethod()
	{
		return this.lastLogoutMethod;
	}

	public void setLastLogoutMethod(String lastLogoutMethod)
	{
		this.lastLogoutMethod = lastLogoutMethod;
	}

	@NotNull
	@Column(name = "number_of_logins")
	public int getNumberOfLogins()
	{
		return this.numberOfLogins;
	}

	public void setNumberOfLogins(int numberOfLogins)
	{
		this.numberOfLogins = numberOfLogins;
	}

	@NotNull
	@Column(name = "total_online_time")
	public int getTotalOnlineTime()
	{
		return this.totalOnlineTime;
	}

	public void setTotalOnlineTime(int totalOnlineTime)
	{
		this.totalOnlineTime = totalOnlineTime;
	}

	@Column(name = "last_ip", length = 16)
	public String getLastIP()
	{
		return this.lastIP;
	}

	public void setLastIP(String lastIP)
	{
		this.lastIP = lastIP;
	}

	@Column(name = "last_user_agent", length = 255)
	public String getLastUserAgent()
	{
		return this.lastUserAgent;
	}

	public void setLastUserAgent(String lastUserAgent)
	{
		this.lastUserAgent = lastUserAgent;
	}

	@Column(name = "accepted_terms")
	public int getAcceptedTerms()
	{
		return this.acceptedTerms;
	}

	public void setAcceptedTerms(int acceptedTerms)
	{
		this.acceptedTerms = acceptedTerms;
	}

	@Column(name = "accepted_terms_at")
	@Temporal(TemporalType.TIMESTAMP)
	public Date getAcceptedTermsAt()
	{
		return this.acceptedTermsAt;
	}

	public void setAcceptedTermsAt(Date acceptedTermsAt)
	{
		this.acceptedTermsAt = acceptedTermsAt;
	}

	@OneToMany(mappedBy = UserRole.PROPERTY_USER, fetch = FetchType.LAZY, cascade = CascadeType.ALL)
	@Cascade(org.hibernate.annotations.CascadeType.DELETE_ORPHAN)
	@Sort(type = SortType.NATURAL)
	public Set<UserRole> getUserRoles()
	{
		return this.userRoles;
	}

	private void setUserRoles(Set<UserRole> userRoles)
	{
		this.userRoles = userRoles;
	}

	// TODO this should not be here
	public UserRole findUserRole(Role role)
	{
		for (UserRole userRole : this.getUserRoles())
			if (userRole.getRole() == role)
				return userRole;

		return null;
	}

	// TODO this should not be here
	public boolean isUserInRole(Role role)
	{
		return findUserRole(role) != null;
	}

	@OneToMany(mappedBy = UserHolidays.PROPERTY_USER, fetch = FetchType.LAZY, cascade = CascadeType.ALL)
	@Cascade(org.hibernate.annotations.CascadeType.DELETE_ORPHAN)
	@Sort(type = SortType.NATURAL)
	public Set<UserHolidays> getUserHolidays()
	{
		return this.userHolidays;
	}

	private void setUserHolidays(Set<UserHolidays> userHolidays)
	{
		this.userHolidays = userHolidays;
	}
}
