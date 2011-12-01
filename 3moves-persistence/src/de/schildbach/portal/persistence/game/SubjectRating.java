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

package de.schildbach.portal.persistence.game;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import org.hibernate.annotations.NaturalId;
import org.hibernate.annotations.Proxy;

import de.schildbach.persistence.DomainObject;
import de.schildbach.portal.persistence.user.Subject;
import de.schildbach.portal.persistence.user.UserTitle;
import de.schildbach.portal.persistence.user.UserTitleVisitor;

/**
 * @author Andreas Schildbach
 */
@Entity
@Table(name = SubjectRating.TABLE_NAME)
@Proxy(lazy = false)
public class SubjectRating extends DomainObject implements UserTitle, Comparable<SubjectRating>
{
	public static final String PROPERTY_SUBJECT = "subject";
	public static final String PROPERTY_RATING = "rating";
	public static final String PROPERTY_INDEX = "index";

	public static final String TABLE_NAME = "subject_ratings";

	private int id;
	private Subject subject;
	private Rating rating;
	private String value;
	private String lastValue;
	private Date lastModifiedAt;
	private Integer index;

	protected SubjectRating()
	{
	}

	public SubjectRating(Subject subject, Rating rating, String value, Date lastModifiedAt)
	{
		this.setSubject(subject);
		this.setRating(rating);
		this.setValue(value);
		this.setLastModifiedAt(lastModifiedAt);
	}

	@Override
	public boolean equals(Object o)
	{
		if (o == this)
			return true;
		if (!(o instanceof SubjectRating))
			return false;
		final SubjectRating other = (SubjectRating) o;
		if (!this.getSubject().equals(other.getSubject()))
			return false;
		if (!this.getRating().equals(other.getRating()))
			return false;
		return true;
	}

	@Override
	public int hashCode()
	{
		return getSubject().hashCode() + 29 * getRating().hashCode();
	}

	public int compareTo(SubjectRating other)
	{
		if (this.getRating() != other.getRating())
			return this.getRating().compareTo(other.getRating());
		return 0;
	}

	public void accept(UserTitleVisitor visitor)
	{
		visitor.visit(this);
	}

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
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

	@NaturalId
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "subject_name", nullable = false, updatable = false)
	public Subject getSubject()
	{
		return this.subject;
	}

	private void setSubject(Subject subject)
	{
		this.subject = subject;
	}

	@NaturalId
	@Column(name = "rating", nullable = false, updatable = false)
	@Enumerated(EnumType.STRING)
	public Rating getRating()
	{
		return this.rating;
	}

	private void setRating(Rating rating)
	{
		this.rating = rating;
	}

	@Column(name = "value", nullable = false)
	public String getValue()
	{
		return this.value;
	}

	public void setValue(String value)
	{
		this.value = value;
	}

	@Column(name = "last_value", nullable = true)
	public String getLastValue()
	{
		return this.lastValue;
	}

	public void setLastValue(String lastValue)
	{
		this.lastValue = lastValue;
	}

	@Column(name = "last_modified_at", nullable = false)
	@Temporal(TemporalType.TIMESTAMP)
	public Date getLastModifiedAt()
	{
		return this.lastModifiedAt;
	}

	public void setLastModifiedAt(Date lastModifiedAt)
	{
		this.lastModifiedAt = lastModifiedAt;
	}

	@Column(name = "`index`", nullable = true)
	public Integer getIndex()
	{
		return this.index;
	}

	public void setIndex(Integer index)
	{
		this.index = index;
	}
}
