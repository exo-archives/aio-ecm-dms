/*
 * Copyright (C) 2003-2007 eXo Platform SAS.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Affero General Public License
 * as published by the Free Software Foundation; either version 3
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, see<http://www.gnu.org/licenses/>.
 */
package org.exoplatform.services.cms.impl;

import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;

import javax.imageio.ImageIO;
import javax.jcr.Node;

import com.sun.image.codec.jpeg.JPEGCodec;
import com.sun.image.codec.jpeg.JPEGEncodeParam;
import com.sun.image.codec.jpeg.JPEGImageEncoder;

/**
 * Created by The eXo Platform SARL
 * Author : Dang Van Minh
 *          minh.dang@exoplatform.com
 * Oct 15, 2008 2:18:02 PM
 */
/**
 * Process images class
 */
public class ImageUtils {

  /**
   * Return the image which resized
   * @param contentNode Node hold the input data
   * @param width Width of thumbnail will be resized
   * @param height Height of thumbnail will be resized
   * @return InputStream
   * @throws Exception
   */  
  public static InputStream scaleImage(Node contentNode, int width, int height) throws Exception {
    Image image = ImageIO.read(contentNode.getProperty("jcr:data").getStream());
    int thumbWidth = width;
    int thumbHeight = height;        
    // Make sure the aspect ratio is maintained, so the image is not skewed
    double thumbRatio = (double)thumbWidth / (double)thumbHeight;
    int imageWidth = image.getWidth(null);
    int imageHeight = image.getHeight(null);
    double imageRatio = (double)imageWidth / (double)imageHeight;
    if (thumbRatio < imageRatio) {
      thumbHeight = (int)(thumbWidth / imageRatio);
    } else {
      thumbWidth = (int)(thumbHeight * imageRatio);
    }
    // Draw the scaled image
    BufferedImage thumbImage = new BufferedImage(thumbWidth, 
        thumbHeight, BufferedImage.TYPE_INT_RGB);
    Graphics2D graphics2D = thumbImage.createGraphics();
    graphics2D.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
        RenderingHints.VALUE_INTERPOLATION_BILINEAR);
    graphics2D.drawImage(image, 0, 0, thumbWidth, thumbHeight, null);

    // Write the scaled image to the outputstream
    ByteArrayOutputStream out = new ByteArrayOutputStream();
    JPEGImageEncoder encoder = JPEGCodec.createJPEGEncoder(out);
    JPEGEncodeParam param = encoder.getDefaultJPEGEncodeParam(thumbImage);
    int quality = 100; // Use between 1 and 100, with 100 being highest quality
    quality = Math.max(0, Math.min(quality, 100));
    param.setQuality(quality / 100.0f, false);
    encoder.setJPEGEncodeParam(param);
    encoder.encode(thumbImage);        
    ImageIO.write(thumbImage, "JPEG" , out); 

    // Read the outputstream into the inputstream for the return value
    ByteArrayInputStream bis = new ByteArrayInputStream(out.toByteArray());        
    return bis;       
  }

}
