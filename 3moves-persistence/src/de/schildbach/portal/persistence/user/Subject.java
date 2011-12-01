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
import javax.persistence.DiscriminatorColumn;
import javax.persistence.DiscriminatorType;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import org.hibernate.annotations.Proxy;
import org.hibernate.validator.NotNull;

import de.schildbach.persistence.DomainObject;

/**
 * @author Andreas Schildbach
 */
@Entity
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@Table(name = Subject.TABLE_NAME)
@DiscriminatorColumn(name = "class", discriminatorType = DiscriminatorType.STRING)
@Proxy(lazy = false)
public abstract class Subject extends DomainObject implements Comparable<Subject>
{
	public static final String PROPERTY_NAME = "name";
	public static final String COLUMN_NAME = "name";

	public static final String TABLE_NAME = "subjects";

	private String name;
	private Date createdAt;
	private boolean autoMove;

	protected Subject()
	{
	}

	protected Subject(Date createdAt, String name)
	{
		this.setCreatedAt(createdAt);
		this.setName(name);
	}

	@Override
	public boolean equals(Object o)
	{
		if (o == this)
			return true;
		if (!(o instanceof Subject))
			return false;
		final Subject other = (Subject) o;
		return getName().toLowerCase().equals(other.getName().toLowerCase());
	}

	@Override
	public int hashCode()
	{
		return getName().toLowerCase().hashCode();
	}

	public int compareTo(Subject other)
	{
		return getName().toLowerCase().compareTo(other.getName().toLowerCase());
	}

	public void accept(SubjectVisitor visitor)
	{
		throw new UnsupportedOperationException();
	}

	@Id
	@NotNull
	@Column(name = COLUMN_NAME, nullable = false, updatable = false)
	public String getName()
	{
		return this.name;
	}

	private void setName(String name)
	{
		this.name = name;
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

	@NotNull
	@Column(name = "auto_move", nullable = false)
	public boolean isAutoMove()
	{
		return this.autoMove;
	}

	public void setAutoMove(boolean autoMove)
	{
		this.autoMove = autoMove;
	}
}
