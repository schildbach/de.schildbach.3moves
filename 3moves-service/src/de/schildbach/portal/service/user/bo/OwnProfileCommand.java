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

package de.schildbach.portal.service.user.bo;

import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import de.schildbach.portal.persistence.user.Gender;

/**
 * @author Andreas Schildbach
 */
public class OwnProfileCommand
{
	private String fullName;
	private Permission fullNamePermission;
	private Gender gender;
	private Date birthday;
	private Permission agePermission;
	private String city;
	private Permission cityPermission;
	private String country;
	private Permission countryPermission;
	private String occupation;
	private Permission occupationPermission;
	private Set<String> languages = new HashSet<String>();
	private String otherLanguages;
	private String description;

	public final Permission getAgePermission()
	{
		return agePermission;
	}

	public final void setAgePermission(Permission agePermission)
	{
		this.agePermission = agePermission;
	}

	public final Date getBirthday()
	{
		return birthday;
	}

	public final void setBirthday(Date birthday)
	{
		this.birthday = birthday;
	}

	public final String getCity()
	{
		return city;
	}

	public final void setCity(String city)
	{
		this.city = city;
	}

	public final Permission getCityPermission()
	{
		return cityPermission;
	}

	public final void setCityPermission(Permission cityPermission)
	{
		this.cityPermission = cityPermission;
	}

	public final String getCountry()
	{
		return country;
	}

	public final void setCountry(String country)
	{
		this.country = country;
	}

	public final Permission getCountryPermission()
	{
		return countryPermission;
	}

	public final void setCountryPermission(Permission countryPermission)
	{
		this.countryPermission = countryPermission;
	}

	public final String getDescription()
	{
		return description;
	}

	public final void setDescription(String description)
	{
		this.description = description;
	}

	public final String getFullName()
	{
		return fullName;
	}

	public final void setFullName(String fullName)
	{
		this.fullName = fullName;
	}

	public final Permission getFullNamePermission()
	{
		return fullNamePermission;
	}

	public final void setFullNamePermission(Permission fullNamePermission)
	{
		this.fullNamePermission = fullNamePermission;
	}

	public final Gender getGender()
	{
		return gender;
	}

	public final void setGender(Gender gender)
	{
		this.gender = gender;
	}

	public final String getOccupation()
	{
		return occupation;
	}

	public final void setOccupation(String occupation)
	{
		this.occupation = occupation;
	}

	public final Permission getOccupationPermission()
	{
		return occupationPermission;
	}

	public final void setOccupationPermission(Permission occupationPermission)
	{
		this.occupationPermission = occupationPermission;
	}

	public final Set<String> getLanguages()
	{
		return languages;
	}

	public final void setLanguages(Set<String> languages)
	{
		this.languages = languages;
	}

	public final String getOtherLanguages()
	{
		return otherLanguages;
	}

	public final void setOtherLanguages(String otherLanguages)
	{
		this.otherLanguages = otherLanguages;
	}
}
