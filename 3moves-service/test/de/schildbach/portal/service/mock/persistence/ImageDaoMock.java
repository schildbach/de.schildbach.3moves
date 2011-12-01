package de.schildbach.portal.service.mock.persistence;

import java.util.LinkedList;
import java.util.List;

import de.schildbach.persistence.test.GenericDaoMockImpl;
import de.schildbach.portal.persistence.user.Image;
import de.schildbach.portal.persistence.user.ImageDao;
import de.schildbach.portal.persistence.user.User;

/**
 * @author Andreas Schildbach
 */
public class ImageDaoMock extends GenericDaoMockImpl<Image, Integer> implements ImageDao
{
	private int id = 0;

	@Override
	protected Integer generateId(Image persistentObject)
	{
		return id++;
	}

	public List<Image> findImages(User owner)
	{
		List<Image> images = new LinkedList<Image>();

		for (Image image : data())
		{
			if (image.getOwner().equals(owner))
				images.add(image);
		}

		return images;
	}
}
