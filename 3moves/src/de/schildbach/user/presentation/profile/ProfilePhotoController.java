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

package de.schildbach.user.presentation.profile;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.IOException;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Required;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.ModelAndView;

import de.schildbach.portal.persistence.user.Image;
import de.schildbach.portal.service.user.UserService;
import de.schildbach.presentation.ImageOperations;

/**
 * @author Andreas Schildbach
 */
@Controller
public class ProfilePhotoController
{
	private UserService userService;
	private ImageOperations imageOperations;

	@Required
	public void setUserService(UserService userService)
	{
		this.userService = userService;
	}

	@Required
	public void setImageOperations(ImageOperations imageOperations)
	{
		this.imageOperations = imageOperations;
	}

	@RequestMapping
	public ModelAndView handleRequestInternal(@RequestParam("user")
	String user, @RequestParam("height")
	int height, WebRequest request, HttpServletResponse response) throws Exception
	{
		if (height > 128)
			throw new IllegalArgumentException("height > 128");

		// retrieve photo data
		Image image = userService.photo(user, 128);

		BufferedImage photo;
		if (image != null)
		{
			// modified?
			if (request.checkNotModified(image.getCreatedAt().getTime()))
				return null;

			// decode image
			photo = imageOperations.read(image.getOriginal());

			// scale to required size
			if (height != photo.getHeight())
				photo = imageOperations.scale(photo, height);
		}
		else
		{
			// deliver white image
			photo = imageOperations.emptyImage(height, Color.WHITE);
		}

		// write image to stream
		response.setContentType("image/png");
		writeImage(photo, response.getOutputStream());

		return null;
	}

	private void writeImage(BufferedImage photo, ServletOutputStream outputStream)
	{
		try
		{
			imageOperations.write(outputStream, photo, "png");
		}
		catch (IOException x)
		{
			// swallow i/o exceptions
		}
	}
}
