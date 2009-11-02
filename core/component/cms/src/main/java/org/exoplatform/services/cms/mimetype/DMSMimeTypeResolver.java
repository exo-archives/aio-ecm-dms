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

import java.util.Iterator;
import java.util.Properties;

import org.exoplatform.commons.utils.MimeTypeResolver;
import org.exoplatform.container.configuration.ConfigurationManager;
import org.exoplatform.container.xml.InitParams;
import org.picocontainer.Startable;

/**
 * Created by The eXo Platform SARL
 * Author : Hoang Van Hung
 *          hunghvit@gmail.com
 * Nov 2, 2009  
 */
public class DMSMimeTypeResolver implements Startable {
  
  private String resource;
  
  private Properties dmsmimeTypes       = new Properties();

  private static MimeTypeResolver mimeTypes       = new MimeTypeResolver();
  
  private ConfigurationManager configuration_;
  
  public String getResource() {
    return resource;
  }

  public void setResource(String resource) {
    this.resource = resource;
  }

  public DMSMimeTypeResolver(ConfigurationManager configuration, InitParams initParams) throws Exception {
    resource = initParams.getValueParam("resource").getValue();
    configuration_ = configuration;
  }
  
  public void start() {
    try {
      dmsmimeTypes.load(configuration_.getInputStream(resource));
    } catch (Exception e) {
      throw new InternalError("Unable to load mimetypes: " + e.toString());
    }
  }

  public void stop() {
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
