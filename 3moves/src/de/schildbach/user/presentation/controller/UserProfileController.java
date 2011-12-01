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

package de.schildbach.user.presentation.controller;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.awt.image.RenderedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.security.Principal;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.ServletRequestBindingException;
import org.springframework.web.bind.ServletRequestUtils;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.multiaction.MultiActionController;

import de.schildbach.portal.persistence.user.SubjectRelation;
import de.schildbach.portal.persistence.user.User;
import de.schildbach.portal.service.exception.NotAuthorizedException;
import de.schildbach.portal.service.user.UserService;
import de.schildbach.presentation.DateUtils;
import de.schildbach.presentation.ImageOperations;
import de.schildbach.user.presentation.PermissionHelper;

/**
 * @author Andreas Schildbach
 */
@Controller
public class UserProfileController extends MultiActionController
{
	@SuppressWarnings("unused")
	private static final Log LOG = LogFactory.getLog(UserProfileController.class);

	private static final int SHOW_SECONDS_THRESHOLD = 60*60*1000;

	private UserService userService;
	private ImageOperations imageOperations;
	
	private static final String PHOTO_SESSION_ATTRIBUTE = "photo";

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

	public ModelAndView player_bar(HttpServletRequest request, HttpServletResponse response) throws ServletRequestBindingException
	{
		Map<String, Object> model = new HashMap<String, Object>();

		Principal remoteUser = request.getUserPrincipal();
		boolean isUser = remoteUser != null;

		User user = userService.user(request.getParameter("user"));
		model.put("username", user.getName());
		boolean isSelf = remoteUser != null && user.equals(remoteUser);

		boolean isFriend = false;
		if(isUser)
		{
			SubjectRelation relation = userService.subjectRelation(remoteUser.getName(), user.getName());
			isFriend = relation != null && relation.isFriend() && relation.getConfirmed() != null && relation.getConfirmed().booleanValue();
		}
			
		StringBuilder location = new StringBuilder();
		if(user.getCity() != null && PermissionHelper.checkPermission(user.getCityPermission(), isSelf, isUser, isFriend))
			location.append(user.getCity()).append(", ");
		if(user.getCountry() != null && PermissionHelper.checkPermission(user.getCountryPermission(), isSelf, isUser, isFriend))
			location.append(user.getCountry()).append(", ");
		if(location.length() > 0)
			location.setLength(location.length()-2);
		model.put("location", location.toString());
		
		model.put("languages", user.getLanguages());

		model.put("color", request.getParameter("color"));
		
		if(request.getParameter("clock") != null && request.getParameter("clock").length() > 0)
		{
			long clock = ServletRequestUtils.getLongParameter(request, "clock");
			model.put("clock", DateUtils.dateDiffShort(clock, request.getLocale(), clock < SHOW_SECONDS_THRESHOLD));
		}

		return new ModelAndView("player_bar.jspx", model);
	}

	public ModelAndView photo_upload(HttpServletRequest request, HttpServletResponse response)
	{
		HttpSession session = request.getSession();
		return new ModelAndView("photo_upload.jspx", "show_photo", Boolean.valueOf(session.getAttribute(PHOTO_SESSION_ATTRIBUTE) != null));
	}

	public ModelAndView uploaded_photo(HttpServletRequest request, HttpServletResponse response) throws IOException
	{
		HttpSession session = request.getSession();

		int height = Integer.parseInt(request.getParameter("height"));
		if(height > 128)
			throw new IllegalArgumentException("height > 128");

		BufferedImage photo = (BufferedImage) session.getAttribute(PHOTO_SESSION_ATTRIBUTE);

		RenderedImage image = scale(photo, height);

		response.setContentType("image/png");
		imageOperations.write(response.getOutputStream(), image, "png");

		return null;
	}
	
	private BufferedImage scale(BufferedImage image, int height)
	{
		// adjust aspect ratio to 3:4
		float aspectRatio = 3f/4f;
		if((float) image.getWidth()/(float) image.getHeight() != aspectRatio)
			image = imageOperations.adjustAspectRatio(image, aspectRatio, Color.WHITE);

		// scale to correct height
		if(height != image.getHeight())
			image = imageOperations.scale(image, height);
		
		return image;
	}

	public ModelAndView upload_photo(HttpServletRequest request, HttpServletResponse response) throws IOException
	{
		HttpSession session = request.getSession();
		MultipartHttpServletRequest multipartRequest = (MultipartHttpServletRequest) request;
		MultipartFile file = multipartRequest.getFile(PHOTO_SESSION_ATTRIBUTE);
		if (file.getSize() > 0)
		{
			BufferedImage photo = imageOperations.read(file.getInputStream());
			if (photo == null)
				return new ModelAndView("photo_upload.dof?message=unknown_format");
			session.setAttribute(PHOTO_SESSION_ATTRIBUTE, photo);
			LOG.info("file upload: size=" + file.getSize() + ", type=" + file.getContentType() + ", dimension=" + photo.getWidth() + "x"
					+ photo.getHeight());
		}
		return new ModelAndView("user_profile_photo_upload");
	}

	public ModelAndView discard_uploaded_photo(HttpServletRequest request, HttpServletResponse response)
	{
		HttpSession session = request.getSession();
		session.removeAttribute(PHOTO_SESSION_ATTRIBUTE);
		return new ModelAndView("user_profile");
	}
	
	public ModelAndView accept_uploaded_photo(HttpServletRequest request, HttpServletResponse response) throws IOException
	{
		Principal user = request.getUserPrincipal();
		if(user == null)
			throw new NotAuthorizedException();
		
		HttpSession session = request.getSession();

		BufferedImage photo = (BufferedImage) session.getAttribute(PHOTO_SESSION_ATTRIBUTE);
		
		RenderedImage scaledPhoto = scale(photo, 128);

		ByteArrayOutputStream out = new ByteArrayOutputStream();
		imageOperations.write(out, scaledPhoto, "png");

		userService.setPhotoData(user.getName(), out.toByteArray());

		session.removeAttribute(PHOTO_SESSION_ATTRIBUTE);
		
		return new ModelAndView("user_profile");
	}
	
	public ModelAndView clear_photo(HttpServletRequest request, HttpServletResponse response)
	{
		Principal user = request.getUserPrincipal();
		if(user == null)
			throw new NotAuthorizedException();
		
		userService.clearPhoto(user.getName());

		return new ModelAndView("user_profile");
	}
}
