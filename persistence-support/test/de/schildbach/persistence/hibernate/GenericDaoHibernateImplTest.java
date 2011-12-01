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

package de.schildbach.persistence.hibernate;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import org.hibernate.SessionFactory;
import org.hibernate.cfg.AnnotationConfiguration;
import org.junit.Test;
import org.springframework.orm.ObjectRetrievalFailureException;

import de.schildbach.persistence.hibernate.model.EntityA;
import de.schildbach.persistence.hibernate.model.EntityB;
import de.schildbach.persistence.hibernate.model.EntityC;

/**
 * @author Andreas Schildbach
 */
public class GenericDaoHibernateImplTest
{
	@Test
	public void testIntegration()
	{
		AnnotationConfiguration cfg = new AnnotationConfiguration();
		cfg.addAnnotatedClass(EntityA.class);
		cfg.addAnnotatedClass(EntityB.class);
		cfg.addAnnotatedClass(EntityC.class);
		cfg.setProperty("hibernate.connection.driver_class", "org.hsqldb.jdbcDriver");
		cfg.setProperty("hibernate.connection.url", "jdbc:hsqldb:mem:testdb");
		cfg.setProperty("hibernate.connection.username", "sa");
		cfg.setProperty("hibernate.dialect", "org.hibernate.dialect.HSQLDialect");
		cfg.setProperty("hibernate.hbm2ddl.auto", "create");

		EntityADao dao = new EntityADao(cfg.buildSessionFactory());

		EntityA a = new EntityA();

		int idA = dao.create(a);

		EntityB b = new EntityB();

		int idB = dao.create(b);

		a = dao.read(idA);

		assertNotNull(a);
		assertTrue(a.getClass().equals(EntityA.class));
		assertEquals(idA, a.getId());

		EntityA b2 = dao.read(idB);

		assertNotNull(b2);
		assertTrue(b2.getClass().equals(EntityB.class));
		assertEquals(idB, b2.getId());

		try
		{
			dao.read(-1);
			fail();
		}
		catch (ObjectRetrievalFailureException x)
		{
		}

		b = dao.read(EntityB.class, idB);
		assertNotNull(b);
		assertTrue(b.getClass().equals(EntityB.class));
		assertEquals(idB, b.getId());
	}

	private static class EntityADao extends GenericDaoHibernateImpl<EntityA, Integer>
	{
		public EntityADao(SessionFactory sessionFactory)
		{
			setSessionFactory(sessionFactory);
		}
	}
}
