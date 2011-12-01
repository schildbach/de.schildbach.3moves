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
import javax.persistence.Transient;

import org.hibernate.annotations.NaturalId;
import org.hibernate.validator.NotNull;

import de.schildbach.persistence.DomainObject;

/**
 * @author Andreas Schildbach
 */
@Entity
@Table(name = SubjectRelation.TABLE_NAME)
public class SubjectRelation extends DomainObject
{
	public static final String PROPERTY_TYPE = "type";
	public static final String PROPERTY_SOURCE_SUBJECT = "sourceSubject";
	public static final String PROPERTY_TARGET_SUBJECT = "targetSubject";

	public static final String TABLE_NAME = "subject_relations";

	private int id;
	private RelationType type;
	private Subject sourceSubject;
	private Subject targetSubject;
	private Boolean confirmed;
	private Date createdAt;

	protected SubjectRelation()
	{
	}

	public SubjectRelation(Date createdAt, Subject sourceSubject, Subject targetSubject, RelationType type)
	{
		this.setCreatedAt(createdAt);
		this.setSourceSubject(sourceSubject);
		this.setTargetSubject(targetSubject);
		this.setType(type);
	}

	@Override
	public boolean equals(Object o)
	{
		if (o == this)
			return true;
		if (!(o instanceof SubjectRelation))
			return false;
		final SubjectRelation other = (SubjectRelation) o;
		if (!other.getSourceSubject().equals(this.getSourceSubject()))
			return false;
		if (!other.getTargetSubject().equals(this.getTargetSubject()))
			return false;
		return true;
	}

	@Override
	public int hashCode()
	{
		return this.getSourceSubject().hashCode() + 29 * this.getTargetSubject().hashCode();
	}

	@Transient
	public boolean isFriend()
	{
		return this.getType() == RelationType.FRIEND;
	}

	@Transient
	public boolean isBanned()
	{
		return this.getType() == RelationType.BANNED;
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
	@Column(name = "type", nullable = false)
	@Enumerated(EnumType.STRING)
	public RelationType getType()
	{
		return this.type;
	}

	public void setType(RelationType type)
	{
		this.type = type;
	}

	@NaturalId
	@NotNull
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "source_subject_name", nullable = false, updatable = false)
	public Subject getSourceSubject()
	{
		return this.sourceSubject;
	}

	private void setSourceSubject(Subject sourceSubject)
	{
		this.sourceSubject = sourceSubject;
	}

	@NaturalId
	@NotNull
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "target_subject_name", nullable = false, updatable = false)
	public Subject getTargetSubject()
	{
		return this.targetSubject;
	}

	private void setTargetSubject(Subject targetSubject)
	{
		this.targetSubject = targetSubject;
	}

	@Column(name = "confirmed", nullable = true)
	public Boolean getConfirmed()
	{
		return this.confirmed;
	}

	public void setConfirmed(Boolean confirmed)
	{
		this.confirmed = confirmed;
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
}
