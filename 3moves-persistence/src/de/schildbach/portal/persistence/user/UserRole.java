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

import org.hibernate.annotations.NaturalId;
import org.hibernate.validator.NotNull;

import de.schildbach.persistence.DomainObject;

/**
 * @author Andreas Schildbach
 */
@Entity
@Table(name = UserRole.TABLE_NAME)
public class UserRole extends DomainObject implements UserTitle, Comparable<UserRole>
{
	public static final String PROPERTY_USER = "user";
	public static final String PROPERTY_ROLE = "role";

	public static final String TABLE_NAME = "user_roles";

	private int id;
	private User user;
	private Role role;

	protected UserRole()
	{
	}

	public UserRole(User user, Role role)
	{
		this.setUser(user);
		this.setRole(role);
	}

	@Override
	public boolean equals(Object o)
	{
		if (o == this)
			return true;
		if (!(o instanceof UserRole))
			return false;
		final UserRole other = (UserRole) o;
		if (!other.getUser().equals(this.getUser()))
			return false;
		if (!other.getRole().equals(this.getRole()))
			return false;
		return true;
	}

	@Override
	public int hashCode()
	{
		return this.getUser().hashCode() + 29 * this.getRole().hashCode();
	}

	public int compareTo(UserRole other)
	{
		return this.getRole().compareTo(other.getRole());
	}

	public void accept(UserTitleVisitor visitor)
	{
		visitor.visit(this);
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

	@NaturalId
	@NotNull
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "user_name", nullable = false, updatable = false)
	public User getUser()
	{
		return this.user;
	}

	private void setUser(User user)
	{
		this.user = user;
	}

	@NaturalId
	@NotNull
	@Column(name = "role", nullable = false, updatable = false)
	@Enumerated(EnumType.STRING)
	public Role getRole()
	{
		return this.role;
	}

	private void setRole(Role role)
	{
		this.role = role;
	}
}
