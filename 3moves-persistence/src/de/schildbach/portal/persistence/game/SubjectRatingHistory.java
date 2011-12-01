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

/**
 * @author Andreas Schildbach
 */
@Entity
@Table(name = "subject_rating_history")
@Proxy(lazy = false)
public class SubjectRatingHistory extends DomainObject
{
	public static final String PROPERTY_CREATED_AT = "createdAt";
	public static final String PROPERTY_SUBJECT = "subject";
	public static final String PROPERTY_RATING = "rating";

	private int id;
	private Date createdAt;
	private Subject subject;
	private Rating rating;
	private Date date;
	private String value;
	private GamePlayer player;

	protected SubjectRatingHistory()
	{
	}

	public SubjectRatingHistory(Date createdAt, Subject subject, Rating rating, Date date, String value, GamePlayer player)
	{
		this.setCreatedAt(createdAt);
		this.setSubject(subject);
		this.setRating(rating);
		this.setDate(date);
		this.setValue(value);
		this.setPlayer(player);
	}

	@Override
	public boolean equals(Object o)
	{
		if (o == this)
			return true;
		if (!(o instanceof SubjectRatingHistory))
			return false;
		final SubjectRatingHistory other = (SubjectRatingHistory) o;
		if (!this.getSubject().equals(other.getSubject()))
			return false;
		if (!this.getRating().equals(other.getRating()))
			return false;
		if (!this.getDate().equals(other.getDate()))
			return false;
		return true;
	}

	@Override
	public int hashCode()
	{
		return getSubject().hashCode() + 29 * getRating().hashCode() + 29 * 29 * getDate().hashCode();
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

	@Column(name = "created_at", nullable = false, updatable = false)
	@Temporal(TemporalType.TIMESTAMP)
	public Date getCreatedAt()
	{
		return createdAt;
	}

	private void setCreatedAt(Date createdAt)
	{
		this.createdAt = createdAt;
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

	@NaturalId
	@Column(name = "date", nullable = false, updatable = false)
	@Temporal(TemporalType.TIMESTAMP)
	public Date getDate()
	{
		return this.date;
	}

	private void setDate(Date date)
	{
		this.date = date;
	}

	@Column(name = "value", nullable = false, updatable = false)
	public String getValue()
	{
		return this.value;
	}

	private void setValue(String value)
	{
		this.value = value;
	}

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "player_id", nullable = true, updatable = false)
	public GamePlayer getPlayer()
	{
		return player;
	}

	private void setPlayer(GamePlayer player)
	{
		this.player = player;
	}
}
