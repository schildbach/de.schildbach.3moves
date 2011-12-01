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

import de.schildbach.portal.persistence.game.SubjectRating;

/**
 * @author Andreas Schildbach
 */
public class UserTitleHelper implements UserTitleVisitor
{
	private UserRole role;
	private SubjectRating rating;

	public UserTitleHelper(UserTitle title)
	{
		title.accept(this);
	}

	public void visit(UserRole role)
	{
		this.role = role;
	}

	public void visit(SubjectRating rating)
	{
		this.rating = rating;
	}

	public boolean isUserRole()
	{
		return this.role != null;
	}

	public boolean isSubjectRating()
	{
		return this.rating != null;
	}

	public UserRole getUserRole()
	{
		return role;
	}

	public SubjectRating getSubjectRating()
	{
		return rating;
	}
}
