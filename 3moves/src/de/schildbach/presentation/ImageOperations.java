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
import java.awt.RenderingHints;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D.Float;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;
import java.awt.image.ConvolveOp;
import java.awt.image.Kernel;
import java.awt.image.RenderedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import javax.imageio.ImageIO;

/**
 * @author Andreas Schildbach
 */
public class ImageOperations
{
	public BufferedImage adjustAspectRatio(BufferedImage srcImage, float aspectRatio, Color color)
	{
		int currentWidth = srcImage.getWidth();
		int currentHeight = srcImage.getHeight();

		int shouldBeHeight = (int) (currentWidth / aspectRatio);
		int shouldBeWidth = (int) (currentHeight * aspectRatio);

		int missingHeight = shouldBeHeight - currentHeight;
		if (missingHeight < 0)
			missingHeight = 0;

		int missingWidth = shouldBeWidth - currentWidth;
		if (missingWidth < 0)
			missingWidth = 0;

		int newWidth = currentWidth + missingWidth;
		int newHeight = currentHeight + missingHeight;

		BufferedImage dstImage = new BufferedImage(newWidth, newHeight, BufferedImage.TYPE_INT_RGB);
		Graphics2D g = (Graphics2D) dstImage.getGraphics();
		g.setPaint(color);
		g.fill(new Float(0, 0, newWidth, newHeight));

		int xOffset = missingWidth / 2;
		int yOffset = missingHeight / 2;

		for (int x = 0; x < currentWidth; x++)
			for (int y = 0; y < currentHeight; y++)
				dstImage.setRGB(x + xOffset, y + yOffset, srcImage.getRGB(x, y));

		return dstImage;
	}

	public BufferedImage scale(BufferedImage srcImage, int height)
	{
		// determine scaling factor
		float scale = (float) height/srcImage.getHeight();
		
		// blur image
		if (scale <= 0.5)
		{
			int radius = (int) Math.floor(1 / scale);
			srcImage = blur(srcImage, radius);
		}

		// scale image
		AffineTransformOp op = new AffineTransformOp(AffineTransform.getScaleInstance(scale, scale), AffineTransformOp.TYPE_BILINEAR);
		BufferedImage dstImage = op.filter(srcImage, null);

		return dstImage;
	}
	
	private BufferedImage blur(BufferedImage srcImage, int radius)
	{
		// construct kernel
		int klen = Math.max(radius, 2); // minimum radius is 2
		int ksize = klen * klen;
		float f = 1f / ksize; // kernel is constant 1/k
		float[] kern = new float[ksize];
		for (int i = 0; i < ksize; i++)
			kern[i] = f;
		Kernel blur = new Kernel(klen, klen, kern);

		// blur with convolve operation
		RenderingHints renderingHints = new RenderingHints(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
		ConvolveOp blurOp = new ConvolveOp(blur, ConvolveOp.EDGE_NO_OP, renderingHints);
		BufferedImage dstImage = blurOp.filter(srcImage, null);
		
		return dstImage;
	}

	public BufferedImage read(byte[] data) throws IOException
	{
		return ImageIO.read(new ByteArrayInputStream(data));
	}
	
	public BufferedImage read(InputStream is) throws IOException
	{
		return ImageIO.read(is);
	}

	public void write(OutputStream os, RenderedImage photo, String formatName) throws IOException
	{
		ImageIO.write(photo, formatName, os);
	}

	public BufferedImage emptyImage(int height, Color color)
	{
		BufferedImage photo = new BufferedImage(height*3/4, height, BufferedImage.TYPE_INT_RGB);
		Graphics2D g = (Graphics2D) photo.getGraphics();
		g.setPaint(color);
		g.fill(new Float(0, 0, photo.getWidth(), photo.getHeight()));
		return photo;
	}
}
