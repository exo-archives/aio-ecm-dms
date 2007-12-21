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
package org.exoplatform.services.cms.scripts;

import java.util.List;

import javax.jcr.Node;

import org.exoplatform.services.jcr.ext.common.SessionProvider;

public interface ScriptService {
  
  public Node getECMScriptHome(String repository,SessionProvider provider) throws Exception ;  
  
  public Node getCBScriptHome(String repository,SessionProvider provider) throws Exception ;    
  
  public List<Node> getECMActionScripts(String repository,SessionProvider provider) throws Exception ;
  
  public List<Node> getECMInterceptorScripts(String repository,SessionProvider provider) throws Exception;
  
  public List<Node> getECMWidgetScripts(String repository,SessionProvider provider) throws Exception ;
  
  public CmsScript getScript(String scriptPath, String repository) throws Exception;
  
  public String getBaseScriptPath() throws Exception ;  
  
  public String getScriptAsText(String scriptPath, String repository) throws Exception;        
  
  public void addScript(String name, String text, String repository,SessionProvider provider) throws Exception;
  
  public void removeScript(String scriptPath, String repository,SessionProvider provider) throws Exception;
  
  public Node getScriptNode(String scriptName, String repository,SessionProvider provider) throws Exception; 
  
  public void initRepo(String repository) throws Exception ;
}
