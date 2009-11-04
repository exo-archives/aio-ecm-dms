/***************************************************************************
 * Copyright (C) 2003-2009 eXo Platform SAS.
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
 *
 **************************************************************************/
package org.exoplatform.services.cms.mimetype;

import java.io.IOException;
import java.util.Iterator;
import java.util.Properties;

import org.exoplatform.commons.utils.MimeTypeResolver;

/**
 * Created by The eXo Platform SARL
 * Author : Hoang Van Hung
 *          hunghvit@gmail.com
 * Nov 2, 2009  
 */
public class DMSMimeTypeResolver {
  
  private Properties dmsmimeTypes       = new Properties();

  private static MimeTypeResolver mimeTypes       = new MimeTypeResolver();

  private static DMSMimeTypeResolver dmsMimeTypeResolver;
  
  
  private DMSMimeTypeResolver() throws IOException {
    dmsmimeTypes.load(getClass().getResourceAsStream("/conf/mimetype/mimetypes.properties"));
  }
  
  public static DMSMimeTypeResolver getInstance() throws IOException {
    if (dmsMimeTypeResolver == null) {
      synchronized (DMSMimeTypeResolver.class) {
        if (dmsMimeTypeResolver == null) {
          dmsMimeTypeResolver = new DMSMimeTypeResolver();    
        }
      }
    }
    return dmsMimeTypeResolver;
  }
  
  public String getMimeType(String filename) {
    String ext = filename.substring(filename.lastIndexOf(".") + 1);
    if (ext.equals("")) {
      ext = filename;
    }
    String mimeType = dmsmimeTypes.getProperty(ext.toLowerCase(), mimeTypes.getDefaultMimeType());
    if (mimeType == null || mimeType.length() == 0) return mimeTypes.getMimeType(filename);
    return mimeType;
  }

  public String getExtension(String mimeType) {
    if (mimeType.equals("") || mimeType.equals(mimeTypes.getDefaultMimeType()))
      return "";
    Iterator iterator = dmsmimeTypes.keySet().iterator();
    String ext = "";
    while (iterator.hasNext()) {
      String key = (String) iterator.next();
      String value = (String) dmsmimeTypes.get(key);
      if (value.equals(mimeType) && mimeType.endsWith(key))
        return key;
      if (value.equals(mimeType) && ext.equals(""))
        ext = new String(key);
      else if (value.equals(mimeType) && (!ext.equals("")))
        return ext;
    }
    return mimeTypes.getExtension(mimeType);
  }
  
  
}
