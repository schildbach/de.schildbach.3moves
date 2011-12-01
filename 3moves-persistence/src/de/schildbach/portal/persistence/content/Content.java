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

package de.schildbach.portal.persistence.content;

import java.net.InetAddress;
import java.util.Date;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.Lob;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Transient;

import org.hibernate.annotations.Sort;
import org.hibernate.annotations.SortType;
import org.hibernate.annotations.Type;
import org.hibernate.validator.NotNull;

import de.schildbach.persistence.DomainObject;
import de.schildbach.portal.persistence.user.User;

/**
 * @author Andreas Schildbach
 */
@Entity
@Table(name = Content.TABLE_NAME)
public class Content extends DomainObject implements Comparable<Content>
{
	public static final String PROPERTY_PARENT = "parent";
	public static final String PROPERTY_TAG = "tag";
	public static final String PROPERTY_NAME = "name";
	public static final String PROPERTY_CREATED_BY = "createdBy";
	public static final String PROPERTY_CREATED_AT = "createdAt";

	public static final String TABLE_NAME = "content";

	private int id;
	private Content parent;
	private boolean root;
	private String tag;
	private String name;
	private String contentType;
	private byte[] content;
	private User createdBy;
	private InetAddress createdByIP;
	private Date createdAt;
	private User inheritedChangedBy;
	private Date inheritedChangedAt;
	private int readCount;

	private SortedSet<Content> childs;

	protected Content()
	{
	}

	public Content(Date createdAt, User createdBy, InetAddress createdByIP)
	{
		this.setCreatedAt(createdAt);
		this.setCreatedBy(createdBy);
		this.setCreatedByIP(createdByIP);
		this.setInheritedChangedAt(createdAt);
		this.setInheritedChangedBy(createdBy);

		// defaults
		this.setRoot(false);
		this.setChilds(new TreeSet<Content>());
	}

	@Override
	public boolean equals(Object o)
	{
		if (o == this)
			return true;
		if (!(o instanceof Content))
			return false;
		final Content other = (Content) o;
		return this.getId() == other.getId();
	}

	@Override
	public int hashCode()
	{
		return this.getId();
	}

	public int compareTo(Content other)
	{
		return other.getInheritedChangedAt().compareTo(this.getInheritedChangedAt());
	}

	@Transient
	public String getMainContentType()
	{
		return getContentType().substring(0, getContentType().indexOf('/'));
	}

	@Transient
	public Content getRootContent()
	{
		if (this.isRoot())
			return this;
		return this.getParent().getRootContent();
	}

	@Transient
	public int getDepth()
	{
		if (this.isRoot())
			return 0;
		return 1 + getParent().getDepth();
	}

	@Transient
	public Iterator<Content> getDepthFirstIterator() // Tiefensuche
	{
		return new DepthFirstIterator(this);
	}

	@Transient
	public List<Content> getPath()
	{
		List<Content> list;
		if (this.isRoot())
		{
			list = new LinkedList<Content>();
		}
		else
		{
			list = this.getParent().getPath();
		}
		list.add(this);
		return list;
	}

	@Transient
	public boolean isLastSibling()
	{
		if (this.getParent() == null)
			return true;
		return this.getParent().getChilds().last().equals(this);
	}

	@Transient
	public Date getChildsCreatedAt()
	{
		Date createdAt = getCreatedAt();
		for (Content child : getChilds())
		{
			Date childsCreatedAt = child.getChildsCreatedAt();
			if (childsCreatedAt.after(createdAt))
				createdAt = childsCreatedAt;
		}
		return createdAt;
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

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "parent_id", nullable = true)
	public Content getParent()
	{
		return this.parent;
	}

	private void setParent(Content parent)
	{
		this.parent = parent;
	}

	@NotNull
	@Column(name = "is_root", nullable = false)
	public boolean isRoot()
	{
		return this.root;
	}

	public void setRoot(boolean root)
	{
		this.root = root;
	}

	@Column(name = "tag", nullable = true, length = 32)
	public String getTag()
	{
		return this.tag;
	}

	public void setTag(String tag)
	{
		this.tag = tag;
	}

	@Column(name = "name", nullable = true, length = 128)
	public String getName()
	{
		return this.name;
	}

	public void setName(String name)
	{
		this.name = name;
	}

	@Column(name = "content_type", nullable = true, length = 64)
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
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "created_by", nullable = false, updatable = false)
	public User getCreatedBy()
	{
		return this.createdBy;
	}

	private void setCreatedBy(User createdBy)
	{
		this.createdBy = createdBy;
	}

	@Column(name = "created_by_ip", updatable = false, length = 16)
	@Type(type = "inetaddress")
	public InetAddress getCreatedByIP()
	{
		return createdByIP;
	}

	private void setCreatedByIP(InetAddress createdByIP)
	{
		this.createdByIP = createdByIP;
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

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "inherited_changed_by" /* , nullable = false */)
	public User getInheritedChangedBy()
	{
		return this.inheritedChangedBy;
	}

	public void setInheritedChangedBy(User inheritedChangedBy)
	{
		this.inheritedChangedBy = inheritedChangedBy;
	}

	@NotNull
	@Column(name = "inherited_changed_at", nullable = false)
	@Temporal(TemporalType.TIMESTAMP)
	public Date getInheritedChangedAt()
	{
		return this.inheritedChangedAt;
	}

	public void setInheritedChangedAt(Date inheritedChangedAt)
	{
		this.inheritedChangedAt = inheritedChangedAt;
	}

	@NotNull
	@Column(name = "read_count", nullable = false)
	public int getReadCount()
	{
		return this.readCount;
	}

	public void setReadCount(int readCount)
	{
		this.readCount = readCount;
	}

	@OneToMany(mappedBy = Content.PROPERTY_PARENT, fetch = FetchType.LAZY)
	@Sort(type = SortType.NATURAL)
	public SortedSet<Content> getChilds()
	{
		return this.childs;
	}

	private void setChilds(SortedSet<Content> childs)
	{
		this.childs = childs;
	}

	public void addChild(Content child)
	{
		child.setParent(this);
		getChilds().add(child);
	}

	public void removeChild(Content child)
	{
		child.setParent(null);
		getChilds().remove(child);
	}

	private static class DepthFirstIterator implements Iterator<Content>
	{
		private List<Content> queue = new LinkedList<Content>();

		private DepthFirstIterator(Content node)
		{
			this.queue.add(node);
		}

		public boolean hasNext()
		{
			return !this.queue.isEmpty();
		}

		public Content next()
		{
			Content node = this.queue.remove(0);
			this.queue.addAll(0, node.getChilds());
			return node;
		}

		public void remove()
		{
			throw new UnsupportedOperationException();
		}
	}
}
