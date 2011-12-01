package de.schildbach.portal.service.mock.persistence;

import java.util.LinkedList;
import java.util.List;

import de.schildbach.persistence.test.GenericDaoMockImpl;
import de.schildbach.portal.persistence.game.Rating;
import de.schildbach.portal.persistence.game.SubjectRating;
import de.schildbach.portal.persistence.game.SubjectRatingDao;
import de.schildbach.portal.persistence.game.SubjectRatingHistory;
import de.schildbach.portal.persistence.user.Subject;

/**
 * @author Andreas Schildbach
 */
public class SubjectRatingDaoMock extends GenericDaoMockImpl<SubjectRating, Integer> implements SubjectRatingDao
{
	private int id = 0;
	private List<SubjectRatingHistory> histories = new LinkedList<SubjectRatingHistory>();

	@Override
	protected Integer generateId(SubjectRating persistentObject)
	{
		return id++;
	}

	public SubjectRating findRating(Subject subject, Rating rating)
	{
		for (SubjectRating subjectRating : data())
		{
			if (subjectRating.getSubject() == subject && subjectRating.getRating() == rating)
				return subjectRating;
		}

		return null;
	}

	public List<SubjectRating> findRatingsForSubject(Subject subject)
	{
		List<SubjectRating> ratingsForSubject = new LinkedList<SubjectRating>();

		for (SubjectRating rating : data())
			if (rating.getSubject() == subject)
				ratingsForSubject.add(rating);

		return ratingsForSubject;
	}

	public List<SubjectRating> findRatings(Rating rating, Integer minIndex, Integer maxIndex, String orderBy)
	{
		throw new UnsupportedOperationException();
	}

	public void save(SubjectRatingHistory history)
	{
		histories.add(history);
	}

	public List<SubjectRatingHistory> findRatingHistory(Subject subject, Rating rating, String orderBy)
	{
		throw new UnsupportedOperationException();
	}
}
