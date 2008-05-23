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
package org.exoplatform.services.ecm.metadata;

import java.util.List;

import javax.jcr.nodetype.NodeType;

import org.exoplatform.services.ecm.template.TemplateEntry;
import org.exoplatform.services.jcr.ext.common.SessionProvider;

/**
 * Created by The eXo Platform SAS
 * @author : Hoa.Pham
 *          hoa.pham@exoplatform.com
 * May 21, 2008  
 */
public interface MetadataManagerService {
  
  public final String METADATA_TEMPLATE_REGISTRY = "exo:services/exo:ecm/exo:metadata".intern() ;
  final public String DIALOG_TYPE = "dialogs".intern();
  final public String VIEW_TYPE = "views".intern();
  
  public void addMetadataTemplate(TemplateEntry entry, String repository, SessionProvider sessionProvider) throws Exception;
  public void removeMetadataTemplateType(String nodetype, String repository, SessionProvider sessionProvider) throws Exception;  
  public List<NodeType> getAllMetadataNodeType(String repository) throws Exception ;  
  
  public String getMetadataPath(String metadataType, boolean isDialog, String repository, SessionProvider sessionProvider) throws Exception;
  
  public List<String> getMetadataPaths(String metadataType, boolean isDialog, String repository, SessionProvider sessionProvider) throws Exception;
  
  public boolean isManagedNodeType(String metadataType, String repository, SessionProvider sessionProvider) throws Exception;
}
