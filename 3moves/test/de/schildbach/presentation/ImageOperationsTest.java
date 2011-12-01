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

package de.schildbach.presentation;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.Rectangle2D.Float;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

import javax.imageio.ImageIO;

import org.junit.Test;

/**
 * @author Andreas Schildbach
 */
public class ImageOperationsTest
{
	private ImageOperations imageOperations = new ImageOperations();

	@Test
	public void test() throws IOException
	{
		BufferedImage image = new BufferedImage(200, 100, BufferedImage.TYPE_INT_RGB);
		Graphics2D g = (Graphics2D) image.getGraphics();
		g.setPaint(Color.RED);
		g.fill(new Float(0, 0, image.getWidth(), image.getHeight()));

		image = imageOperations.adjustAspectRatio(image, 3f / 4f, Color.WHITE);

		ImageIO.write(image, "png", new ByteArrayOutputStream() /* new File("output.png") */);
	}

	@Test
	public void test2() throws IOException
	{
		BufferedImage image = new BufferedImage(200, 100, BufferedImage.TYPE_INT_RGB);
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		imageOperations.write(baos, image, "png");
		BufferedImage image2 = imageOperations.read(baos.toByteArray());
		imageOperations.scale(image2, 50);
		imageOperations.adjustAspectRatio(image2, 1.0f, Color.BLACK);
	}
}
