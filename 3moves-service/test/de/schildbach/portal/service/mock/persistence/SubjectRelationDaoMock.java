package de.schildbach.portal.service.mock.persistence;

import java.util.List;

import de.schildbach.persistence.test.GenericDaoMockImpl;
import de.schildbach.portal.persistence.user.RelationType;
import de.schildbach.portal.persistence.user.Subject;
import de.schildbach.portal.persistence.user.SubjectRelation;
import de.schildbach.portal.persistence.user.SubjectRelationDao;

/**
 * @author Andreas Schildbach
 */
public class SubjectRelationDaoMock extends GenericDaoMockImpl<SubjectRelation, Integer> implements SubjectRelationDao
{
	private int id = 0;

	@Override
	protected Integer generateId(SubjectRelation persistentObject)
	{
		return id++;
	}

	public SubjectRelation findSubjectRelation(Subject source, Subject target)
	{
		for (SubjectRelation relation : data())
			if (relation.getSourceSubject().equals(source) && relation.getTargetSubject().equals(target))
				return relation;

		return null;
	}

	public List<SubjectRelation> findSubjectRelationsBySource(Subject source, RelationType type)
	{
		throw new UnsupportedOperationException();
	}

	public List<SubjectRelation> findSubjectRelationsByTarget(Subject target)
	{
		throw new UnsupportedOperationException();
	}

	public void removeSubjectRelation(SubjectRelation relation)
	{
		throw new UnsupportedOperationException();
	}
}
