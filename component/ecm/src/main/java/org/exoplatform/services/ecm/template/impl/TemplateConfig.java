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
package org.exoplatform.services.ecm.template.impl;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by The eXo Platform SAS
 * @author : Hoa.Pham
 *          hoa.pham@exoplatform.com
 * May 5, 2008  
 */
public class TemplateConfig {

  private List<NodeType> nodeTypes = new ArrayList<NodeType>(3);  

  public List<NodeType> getNodeTypes() {   return this.nodeTypes; }
  public void setNodeTypes(List<NodeType> list) {  this.nodeTypes = list; }

  static public class Template {

    private String name;
    private String templateFilePath;    
    private ArrayList<String> accessPermissions;

    public String getName() {return name ; }
    public void setName(String name) { this.name = name ;}                    

    public String getTemplateFilePath() { return templateFilePath; }
    public void setTemplateFilePath(String templateFilePath) { this.templateFilePath = templateFilePath; }

    public ArrayList<String> getAccessPermissions() { return this.accessPermissions ; }
    public void setAccessPermissions(ArrayList<String> acp) {this.accessPermissions = acp; }

  }

  static public class NodeType {    
    private String name;
    private String label;
    private boolean documentTemplate = true;
    private ArrayList<Template> dialogs = new ArrayList<Template>(3);
    private ArrayList<Template> views = new ArrayList<Template>(3);    

    public NodeType(){ }

    public String getNodeTypeName() { return name ; }
    public void setNodeTypeName(String s) { name = s ; }

    public String getLabel() { return label ; }
    public void setLabel(String s) { label = s ; }

    public ArrayList<Template> getDialogs() { return dialogs; }
    public void setDialogs(ArrayList<Template> dialogs) { this.dialogs = dialogs; }

    public ArrayList<Template> getViews() { return views; }
    public void setViews(ArrayList<Template> views) { this.views = views; }

    public boolean getDocumentTemplate() { return this.documentTemplate ; }    
    public void setDocumentTemplate( boolean b) { this.documentTemplate = b ; }
  }

}
