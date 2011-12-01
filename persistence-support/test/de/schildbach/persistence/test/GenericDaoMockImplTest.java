/*
 * Copyright 2007 the original author or authors.
 * 
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation, version 2.1.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
 */

package de.schildbach.persistence.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;

/**
 * @author Andreas Schildbach
 */
public class GenericDaoMockImplTest
{
	private EntityDaoAutoIncrementKey dao;

	@Before
	public void setup() throws Exception
	{
		dao = new EntityDaoAutoIncrementKey();
	}

	@Test
	public void test()
	{
		Entity a = new Entity("Andreas");
		dao.create(a);
		Entity b = new Entity("Barbara");
		dao.create(b);

		assertEquals(2, dao.count());

		Entity a2 = dao.read(a.getId());
		assertEquals(a, a2);
		assertNotSame(b, a2);
		Entity b2 = dao.read(b.getId());
		assertEquals(b, b2);
		assertNotSame(a, b2);
		Entity a3 = dao.findByName(a.getName());
		assertEquals(a, a3);
		assertNotSame(b, a3);
		Entity b3 = dao.findByName(b.getName());
		assertEquals(b, b3);
		assertNotSame(a, b3);

		dao.delete(a);
		dao.delete(b);

		assertEquals(0, dao.count());
	}

	@Test(expected = IllegalArgumentException.class)
	public void shouldThrowExceptionWhenCreatingNullObject()
	{
		dao.create(null);
	}

	@Test(expected = RuntimeException.class)
	public void shouldThrowExceptionOnDuplicateKey()
	{
		EntityDaoAssignedKey dao = new EntityDaoAssignedKey();

		Entity a = new Entity("Andreas");
		dao.create(a);
		Entity a2 = new Entity("Andreas");

		dao.create(a2);
	}

	@Test(expected = IllegalArgumentException.class)
	public void shouldThrowExceptionWhenReadingNull()
	{
		dao.read(null);
	}

	@Test(expected = IllegalArgumentException.class)
	public void shouldThrowExceptionWhenReadingNull2()
	{
		dao.read(Entity.class, null);
	}

	@Test(expected = IllegalArgumentException.class)
	public void shouldThrowExceptionWhenReadingNull3()
	{
		dao.read(null, 1);
	}

	@Test(expected = IllegalArgumentException.class)
	public void shouldThrowExceptionOnDeletingNull()
	{
		dao.delete(null);
	}

	@Test(expected = RuntimeException.class)
	public void shouldThrowExceptionWhenDeletingNonExistentObject()
	{
		Entity a = new Entity("Andreas");
		dao.create(a);
		dao.delete(a);

		dao.delete(a);
	}

	@Test
	public void testResetShouldRemoveEverything()
	{
		Entity a = new Entity("Andreas");
		dao.create(a);
		assertFalse(dao.data().isEmpty());
		dao.reset();
		assertTrue(dao.data().isEmpty());
	}

	private static class EntityDaoAutoIncrementKey extends GenericDaoMockImpl<Entity, Integer>
	{
		private int id = 0;

		@Override
		protected Integer generateId(Entity persistentObject)
		{
			int newId = id++;
			persistentObject.setId(newId);
			return newId;
		}

		public Entity findByName(String name)
		{
			for (Entity e : data())
			{
				if (name.equals(e.getName()))
					return e;
			}

			return null;
		}

		public int count()
		{
			return data().size();
		}
	}

	private static class EntityDaoAssignedKey extends GenericDaoMockImpl<Entity, String>
	{
		@Override
		protected String generateId(Entity persistentObject)
		{
			return persistentObject.getName();
		}
	}

	private static class Entity
	{
		private int id;
		private String name;

		public Entity(String name)
		{
			this.setName(name);
		}

		public int getId()
		{
			return id;
		}

		public void setId(int id)
		{
			this.id = id;
		}

		public String getName()
		{
			return name;
		}

		public void setName(String name)
		{
			this.name = name;
		}
	}
}
