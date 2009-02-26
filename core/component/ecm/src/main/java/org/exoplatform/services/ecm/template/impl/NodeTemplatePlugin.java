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

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.exoplatform.commons.utils.IOUtil;
import org.exoplatform.container.component.BaseComponentPlugin;
import org.exoplatform.container.configuration.ConfigurationManager;
import org.exoplatform.container.xml.InitParams;
import org.exoplatform.container.xml.ObjectParameter;
import org.exoplatform.services.ecm.template.TemplateEntry;
import org.exoplatform.services.ecm.template.impl.TemplateConfig.NodeType;
import org.exoplatform.services.ecm.template.impl.TemplateConfig.Template;

/**
 * Created by The eXo Platform SAS
 * Author : TAN DUNG DANG
 *          dzungdev@gmail.com
 * May 19, 2008  
 */
public class NodeTemplatePlugin extends BaseComponentPlugin {

  private ConfigurationManager configManager_ ;
  private Iterator<ObjectParameter> predefinedTemplates_ ;
  private String repository_ ; 

  public NodeTemplatePlugin(ConfigurationManager configManager, InitParams params) {
    this.configManager_ = configManager ;
    this.predefinedTemplates_ = params.getObjectParamIterator();
    this.repository_ = params.getValueParam("repository").getValue() ;
  }

  public String getRepository() {
    return repository_ ; 
  }

  public List<TemplateEntry> getTemplateEntries() throws Exception { 
    List<TemplateEntry> templateEntries = new ArrayList<TemplateEntry>() ;
    for(;predefinedTemplates_.hasNext();) {
      TemplateConfig templateConfig = (TemplateConfig)predefinedTemplates_.next().getObject() ;
      List<NodeType> nodetypes = templateConfig.getNodeTypes();
      for(NodeType nodeType: nodetypes) {
        List<Template> dialogs = nodeType.getDialogs();
        for(Template dialog:dialogs) {
          TemplateEntry entry = createTemplateEntry(nodeType, dialog, true) ;
          templateEntries.add(entry) ;
        }
        List<Template> views = nodeType.getViews();
        for(Template view: views) {
          TemplateEntry entry = createTemplateEntry(nodeType, view, false) ;
          templateEntries.add(entry) ;
        }
      }
    } 
    return templateEntries ; 
  }

  private TemplateEntry createTemplateEntry(NodeType nodeType, Template template, boolean isDialog) throws Exception {
    TemplateEntry entry = new TemplateEntry() ;
    entry.setNodeTypeName(nodeType.getNodeTypeName()) ;
    entry.setLabel(nodeType.getLabel()) ;
    entry.setDocumentTemplate(nodeType.getDocumentTemplate()) ;
    entry.setTemplateName(template.getName()) ;
    entry.setDialog(isDialog) ;
    entry.setAccessPermissions(template.getAccessPermissions()) ;
    InputStream templateStream = configManager_.getInputStream(template.getTemplateFilePath()) ;
    String templateData = IOUtil.getStreamContentAsString(templateStream) ;
    entry.setTemplateData(templateData) ;
    return entry ;
  }
}
