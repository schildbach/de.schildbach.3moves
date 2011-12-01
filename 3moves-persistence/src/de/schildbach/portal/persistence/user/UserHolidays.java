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

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Transient;

import org.hibernate.annotations.NaturalId;
import org.hibernate.validator.NotNull;

import de.schildbach.persistence.DomainObject;

/**
 * @author Andreas Schildbach
 */
@Entity
@Table(name = UserHolidays.TABLE_NAME)
public class UserHolidays extends DomainObject implements Comparable<UserHolidays>
{
	public static final String PROPERTY_USER = "user";

	public static final String TABLE_NAME = "user_holidays";

	private int id;
	private Date createdAt;
	private User user;
	private Date beginAt;
	private Date endAt;

	protected UserHolidays()
	{
	}

	public UserHolidays(Date createdAt, User user, Date beginAt, Date endAt)
	{
		this.setCreatedAt(createdAt);
		this.setUser(user);
		this.setBeginAt(beginAt);
		this.setEndAt(endAt);
	}

	@Override
	public boolean equals(Object o)
	{
		if (o == this)
			return true;
		if (!(o instanceof UserHolidays))
			return false;
		final UserHolidays other = (UserHolidays) o;
		if (!other.getUser().equals(this.getUser()))
			return false;
		if (!other.getBeginAt().equals(this.getBeginAt()))
			return false;
		return true;
	}

	@Override
	public int hashCode()
	{
		return this.getUser().hashCode() + 29 * this.getBeginAt().hashCode();
	}

	public int compareTo(UserHolidays other)
	{
		return this.getBeginAt().compareTo(other.getBeginAt());
	}

	public static long getLength(Date beginAt, Date endAt)
	{
		final int MILLISECONDS_PER_DAY = 24 * 60 * 60 * 1000;
		return ((endAt.getTime() - beginAt.getTime()) + MILLISECONDS_PER_DAY);
	}

	@Transient
	public long getLength()
	{
		return getLength(this.getBeginAt(), this.getEndAt());
	}

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@NotNull
	@Column(name = "id", nullable = false, updatable = false)
	public int getId()
	{
		return this.id;
	}

	@SuppressWarnings("unused")
	private void setId(int id)
	{
		this.id = id;
	}

	@NotNull
	@Column(name = "created_at", nullable = false, updatable = false)
	@Temporal(TemporalType.TIMESTAMP)
	public Date getCreatedAt()
	{
		return this.createdAt;
	}

	private void setCreatedAt(Date createdAt)
	{
		this.createdAt = createdAt;
	}

	@NaturalId
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "user_name", nullable = false, updatable = false)
	public User getUser()
	{
		return user;
	}

	private void setUser(User user)
	{
		this.user = user;
	}

	@NaturalId
	@NotNull
	@Column(name = "begin_at", nullable = false, updatable = false)
	@Temporal(TemporalType.DATE)
	public Date getBeginAt()
	{
		return this.beginAt;
	}

	private void setBeginAt(Date beginAt)
	{
		this.beginAt = beginAt;
	}

	@NotNull
	@Column(name = "end_at", nullable = false, updatable = false)
	@Temporal(TemporalType.DATE)
	public Date getEndAt()
	{
		return this.endAt;
	}

	private void setEndAt(Date endAt)
	{
		this.endAt = endAt;
	}
}
