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
package org.exoplatform.services.ecm.template;

import java.util.ArrayList;

/**
 * Created by The eXo Platform SAS
 * @author : Hoa.Pham
 *          hoa.pham@exoplatform.com
 * May 5, 2008  
 */
public class TemplateEntry {
  
  private String nodeTypeName ;
  private String label ; 
  private String templateName ; 
  private boolean isDialog ; 
  private boolean isDocumentTemplate ; 
  private ArrayList<String> accessPermissions; 
  private String templateData ;
  
  /**
   * @return the nodeTypeName
   */
  public String getNodeTypeName() { return nodeTypeName; }  
  /**
   * @param nodeTypeName the nodeTypeName to set
   */
  public void setNodeTypeName(String nodeTypeName) { this.nodeTypeName = nodeTypeName; }
  /**
   * @return the label
   */
  public String getLabel() { return label; }
  /**
   * @param label the label to set
   */
  public void setLabel(String label) { this.label = label; }
  /**
   * @return the templateName
   */
  public String getTemplateName() { return templateName; }
  /**
   * @param templateName the templateName to set
   */
  public void setTemplateName(String templateName) { this.templateName = templateName; }
  /**
   * @return the isDialog
   */
  public boolean isDialog() { return isDialog; }
  /**
   * @param isDialog the isDialog to set
   */
  public void setDialog(boolean isDialog) { this.isDialog = isDialog; }
  /**
   * @return the isDocumentTemplate
   */
  public boolean isDocumentTemplate() { return isDocumentTemplate; }
  /**
   * @param isDocumentTemplate the isDocumentTemplate to set
   */
  public void setDocumentTemplate(boolean isDocumentTemplate) { this.isDocumentTemplate = isDocumentTemplate; }
  /**
   * @return the accessPermissions
   */
  public ArrayList<String> getAccessPermissions() { return accessPermissions; }
  /**
   * @param accessPermissions the accessPermissions to set
   */
  public void setAccessPermissions(ArrayList<String> accessPermissions) { this.accessPermissions = accessPermissions; }
  /**
   * @return the templateData
   */
  public String getTemplateData() { return templateData; }
  /**
   * @param templateData the templateData to set
   */
  public void setTemplateData(String templateData) { this.templateData = templateData; }
  
}
