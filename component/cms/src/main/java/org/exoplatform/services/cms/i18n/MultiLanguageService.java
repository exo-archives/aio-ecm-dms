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
package org.exoplatform.services.cms.i18n;

import java.util.List;
import java.util.Map;

import javax.jcr.Node;
import javax.jcr.Value;


/**
 * Author : Hung Nguyen Quang
 *          nguyenkequanghung@yahoo.com
 */

public interface MultiLanguageService {
  
  final static public String LANGUAGES = "languages" ;
  final static public String EXO_LANGUAGE = "exo:language" ;
  final static public String COMMENTS = "comments".intern() ;
  
  
  public List<String> getSupportedLanguages(Node node) throws Exception ;
  public void setDefault(Node node, String language, String repositoryName) throws Exception ;
  public void addLanguage(Node node, Map inputs, String language, boolean isDefault) throws Exception ;
  public void addLanguage(Node node, Map inputs, String language, boolean isDefault, String nodeType) throws Exception ;
  public void addFileLanguage(Node node, String fileName, Value value, String mimeType, String language, String repositoryName, boolean isDefault) throws Exception ;
  public void addFileLanguage(Node node, String language, Map mappings, boolean isDefault) throws Exception ;
  public String getDefault(Node node) throws Exception ;
  public Node getLanguage(Node node, String language) throws Exception ;
  
}
