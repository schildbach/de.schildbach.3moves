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
import javax.persistence.Lob;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import org.hibernate.validator.NotNull;

import de.schildbach.persistence.DomainObject;

/**
 * @author Andreas Schildbach
 */
@Entity
@Table(name = Image.TABLE_NAME)
public class Image extends DomainObject
{
	public static final String PROPERTY_OWNER = "owner";

	public static final String TABLE_NAME = "images";

	private int id;
	private Date createdAt;
	private User owner;
	private byte[] original;

	protected Image()
	{
	}

	public Image(Date createdAt, User owner, byte[] original)
	{
		this.setCreatedAt(createdAt);
		this.setOwner(owner);
		this.setOriginal(original);
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

	@NotNull
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "owner", nullable = false, updatable = false)
	public User getOwner()
	{
		return this.owner;
	}

	private void setOwner(User owner)
	{
		this.owner = owner;
	}

	@Column(name = "original", nullable = true)
	@Lob
	public byte[] getOriginal()
	{
		return this.original;
	}

	private void setOriginal(byte[] original)
	{
		this.original = original;
	}
}
