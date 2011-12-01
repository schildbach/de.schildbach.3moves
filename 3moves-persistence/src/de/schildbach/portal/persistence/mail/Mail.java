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

package de.schildbach.portal.persistence.mail;

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
import de.schildbach.portal.persistence.user.Subject;

/**
 * @author Andreas Schildbach
 */
@Entity
@Table(name = Mail.TABLE_NAME)
public class Mail extends DomainObject
{
	public static final String PROPERTY_SENDER = "sender";
	public static final String PROPERTY_RECIPIENT = "recipient";
	public static final String PROPERTY_CREATED_AT = "createdAt";
	public static final String PROPERTY_READ = "read";
	public static final String PROPERTY_DELETED_BY_SENDER = "deletedBySender";
	public static final String PROPERTY_DELETED_BY_RECIPIENT = "deletedByRecipient";

	public static final String TABLE_NAME = "mails";

	private int id;
	private Subject sender;
	private Subject recipient;
	private Date createdAt;
	private String subject;
	private String contentType;
	private byte[] content;
	private boolean read;
	private boolean important;
	private boolean repliedTo;
	private boolean deletedBySender;
	private boolean deletedByRecipient;

	protected Mail()
	{
	}

	public Mail(Date createdAt, Subject sender, Subject recipient)
	{
		this.setCreatedAt(createdAt);
		this.setSender(sender);
		this.setRecipient(recipient);

		// defaults
		this.setRead(false);
		this.setDeletedBySender(false);
		this.setDeletedByRecipient(false);
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
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "sender", nullable = false, updatable = false)
	public Subject getSender()
	{
		return this.sender;
	}

	public void setSender(Subject sender)
	{
		this.sender = sender;
	}

	@NotNull
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "recipient", nullable = false, updatable = false)
	public Subject getRecipient()
	{
		return this.recipient;
	}

	public void setRecipient(Subject recipient)
	{
		this.recipient = recipient;
	}

	@Column(name = "subject", nullable = true, length = 128)
	public String getSubject()
	{
		return this.subject;
	}

	public void setSubject(String subject)
	{
		this.subject = subject;
	}

	@NotNull
	@Column(name = "content_type", nullable = false, length = 64)
	public String getContentType()
	{
		return this.contentType;
	}

	public void setContentType(String contentType)
	{
		this.contentType = contentType;
	}

	@Column(name = "content", nullable = true)
	@Lob
	public byte[] getContent()
	{
		return this.content;
	}

	public void setContent(byte[] content)
	{
		this.content = content;
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
	@Column(name = "is_read", nullable = false)
	public boolean isRead()
	{
		return this.read;
	}

	public void setRead(boolean read)
	{
		this.read = read;
	}

	@NotNull
	@Column(name = "is_important", nullable = false)
	public boolean isImportant()
	{
		return this.important;
	}

	public void setImportant(boolean important)
	{
		this.important = important;
	}

	@NotNull
	@Column(name = "is_replied_to", nullable = false)
	public boolean isRepliedTo()
	{
		return this.repliedTo;
	}

	public void setRepliedTo(boolean repliedTo)
	{
		this.repliedTo = repliedTo;
	}

	@NotNull
	@Column(name = "is_deleted_by_sender", nullable = false)
	public boolean isDeletedBySender()
	{
		return this.deletedBySender;
	}

	public void setDeletedBySender(boolean deletedBySender)
	{
		this.deletedBySender = deletedBySender;
	}

	@NotNull
	@Column(name = "is_deleted_by_recipient", nullable = false)
	public boolean isDeletedByRecipient()
	{
		return this.deletedByRecipient;
	}

	public void setDeletedByRecipient(boolean deletedByRecipient)
	{
		this.deletedByRecipient = deletedByRecipient;
	}
}
