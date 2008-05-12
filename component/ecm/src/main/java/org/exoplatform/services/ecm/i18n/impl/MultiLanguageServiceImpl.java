/*
 * Copyright (C) 2003-2008 eXo Platform SAS.
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
package org.exoplatform.services.ecm.i18n.impl;

import java.util.List;
import java.util.Map;

import javax.jcr.Node;
import javax.jcr.Value;

import org.exoplatform.services.ecm.core.JcrItemInput;
import org.exoplatform.services.ecm.i18n.MultiLanguageService;

/**
 * Created by The eXo Platform SAS
 * Author : Ly Dinh Quang
 *          quang.ly@exoplatform.com
 *			    xxx5669@yahoo.com
 * May 7, 2008  
 */
public class MultiLanguageServiceImpl implements MultiLanguageService {

  public String getDefaultLanguage(Node document) throws Exception {
    return null;
  }

  public void addLanguage(Node document, Map<String, JcrItemInput> inputs, String language,
      boolean isDefault) throws Exception {    
  }

  public void addLanguage(Node document, Map<String, JcrItemInput> inputs, String language,
      boolean isDefault, String nodeType) throws Exception {   
  }

  public List<String> getAvailableLanguages(Node document) throws Exception {
    return null;
  }

  public Node getLanguage(Node node, String language) throws Exception {
    return null;
  }

  public void setDefault(Node node, String language) throws Exception {    
  }

  public void addFileLanguage(Node node, Value value, String mimeType, String language,
      boolean isDefault) throws Exception {
    // TODO Auto-generated method stub
    
  }

  public void addFileLanguage(Node node, String language, Map<String, JcrItemInput> mappings,
      boolean isDefault) throws Exception {
    // TODO Auto-generated method stub
    
  }  
}
