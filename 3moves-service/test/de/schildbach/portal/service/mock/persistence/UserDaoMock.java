package de.schildbach.portal.service.mock.persistence;

import java.util.LinkedList;
import java.util.List;

import de.schildbach.persistence.test.GenericDaoMockImpl;
import de.schildbach.portal.persistence.user.Role;
import de.schildbach.portal.persistence.user.Subject;
import de.schildbach.portal.persistence.user.User;
import de.schildbach.portal.persistence.user.UserDao;

/**
 * @author Andreas Schildbach
 */
public class UserDaoMock extends GenericDaoMockImpl<Subject, String> implements UserDao
{
	@Override
	protected String generateId(Subject persistentObject)
	{
		return persistentObject.getName();
	}

	public Subject findSubject(Class<? extends Subject> subjectClass, String name)
	{
		return read(User.class, name);
	}

	public User loadUser(String name, boolean fetchRoles)
	{
		return read(User.class, name);
	}

	public User findUserCaseInsensitive(String name, boolean fetchRoles)
	{
		return read(User.class, name); // not quite exact
	}

	public User findUserByOpenId(String openId)
	{
		throw new UnsupportedOperationException();
	}

	public User findUserByEmail(String email)
	{
		throw new UnsupportedOperationException();
	}

	public List<User> findUsers(String nameFilter, Role role, String orderBy, int maxResults)
	{
		throw new UnsupportedOperationException();
	}

	public List<String> findUserNames(String nameStart, int maxResults)
	{
		return new LinkedList<String>();
	}
}
