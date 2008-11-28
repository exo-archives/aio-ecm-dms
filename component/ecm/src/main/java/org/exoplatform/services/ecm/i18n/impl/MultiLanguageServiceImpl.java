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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.PathNotFoundException;
import javax.jcr.Value;
import javax.jcr.nodetype.NodeType;
import javax.jcr.nodetype.PropertyDefinition;
import javax.jcr.version.Version;

import org.exoplatform.services.ecm.core.JcrItemInput;
import org.exoplatform.services.ecm.core.NodeService;
import org.exoplatform.services.ecm.i18n.MultiLanguageService;
import org.exoplatform.services.ecm.vote.VoteService;

/**
 * Created by The eXo Platform SAS
 * Author : Ly Dinh Quang
 *          quang.ly@exoplatform.com
 *          xxx5669@yahoo.com
 * May 7, 2008  
 */
public class MultiLanguageServiceImpl implements MultiLanguageService {
  final static public String JCRCONTENT     = "jcr:content";
  final static public String JCRDATA        = "jcr:data";
  final static public String JCRMIMETYPE    = "jcr:mimeType";
  final static public String NTUNSTRUCTURED = "nt:unstructured";  
  final static String VOTER_PROP = "exo:voter".intern() ;  
  final static String VOTING_RATE_PROP = "exo:votingRate".intern() ;
  final static String VOTE_TOTAL_PROP = "exo:voteTotal".intern() ; 
  final static String VOTE_TOTAL_LANG_PROP = "exo:voteTotalOfLang".intern() ;
  final static String NODE = "/node/" ;
  final static String NODE_LANGUAGE = "/node/languages/" ;
  final static String CONTENT_PATH = "/node/jcr:content/" ;
  
  private VoteService voteService_;
  private NodeService nodeService_ ;
  public MultiLanguageServiceImpl(VoteService voteService, NodeService nodeService) {
    voteService_ = voteService;
    nodeService_ = nodeService ;
  }
    
  public Node getLanguage(Node node, String language) throws Exception {
    if (node.getVersionHistory().hasVersionLabel(language)) {
      Version languageVersion = node.getVersionHistory().getVersionByLabel(language);
      node.restore(languageVersion, true);
      return node;
    } 
    return null;
  }
  
  public void addLanguage(Node document, Map<String, JcrItemInput> inputs, String language, boolean isDefault) throws Exception {
    if (!document.isNodeType("mix:versionable")) {     
      document.addMixin("mix:versionable");
      document.getSession().save();
    }        
    if (document.getVersionHistory().hasVersionLabel(language)) {     
      Version languageVersion = document.getVersionHistory().getVersionByLabel(language);
      Version baseVersion = document.getBaseVersion();
      document.restore(languageVersion, true);
      Node newLanguage = nodeService_.addNode(document.getParent(), document.getPrimaryNodeType().getName(), inputs, false);
      if(!isDefault) {
        document.restore(baseVersion, true);       
      }
    } else {           
      //create new version for that node
      Version versionRoot = document.checkin();
      document.checkout();      
      Node newLanguage = nodeService_.addNode(document.getParent(), document.getPrimaryNodeType().getName(), inputs, false);
      Version newLangVersion = newLanguage.checkin() ;
      newLanguage.checkout() ;
      newLanguage.getVersionHistory().addVersionLabel(newLangVersion.getName(), language, true) ;
      if(!isDefault) {
        document.restore(versionRoot, true) ;
      }
    }
  }  
  
  
  
  
  
  
  
  
  /**
   * Will be update later
   */
    
  public void addFileLanguage(Node node, Value value, String mimeType, String language,
      boolean isDefault) throws Exception {
    // TODO Auto-generated method stub

  }

  public void addFileLanguage(Node node, String language, Map<String, JcrItemInput> mappings,
      boolean isDefault) throws Exception {
    // TODO Auto-generated method stub

  }
  
  public void addLanguage(Node document, Map<String, JcrItemInput> inputs, String language,
      boolean isDefault, String nodeType) throws Exception {
    // TODO Auto-generated method stub

  }

  public List<String> getAvailableLanguages(Node document) throws Exception {
    // TODO Auto-generated method stub
    return null;
  }

  public String getDefault(Node node) throws Exception {
    String defaultValue = null;
    try {
      defaultValue = node.getProperty("exo:language").getString();
    } catch (PathNotFoundException e) {
    }
    return defaultValue;
  }

  public List<String> getSupportedLanguages(Node node) throws Exception {
    List<String> listLanguage = new ArrayList<String>();
    String defaultLanguage = getDefault(node);
    if (defaultLanguage != null) {
      listLanguage.add(defaultLanguage);
    }
    try {
      Node languageNode = node.getNode(LANGUAGES);
      NodeIterator iterate = languageNode.getNodes();
      while (iterate.hasNext()) {
        String nodeName = iterate.nextNode().getName();
        listLanguage.add(nodeName);
      }
    } catch (PathNotFoundException e) {
    }
    return listLanguage;
  }

  public void setDefault(Node node, String language) throws Exception {
    String defaultLanguage = getDefault(node);
    Node languageNode = null;
    if (!defaultLanguage.trim().equalsIgnoreCase(language)) {
      languageNode = node.getNode(LANGUAGES);
      Node selectedLangNode = languageNode.getNode(language); // Node will be setdefault
      Node newLang = languageNode.addNode(defaultLanguage);
      PropertyDefinition[] properties = node.getPrimaryNodeType().getPropertyDefinitions();
      for (PropertyDefinition pro : properties) {
        if (!pro.isProtected()) {
          String propertyName = pro.getName();
          if (node.hasProperty(propertyName)) {
            if (node.getProperty(propertyName).getDefinition().isMultiple()) {
              newLang.setProperty(propertyName, node.getProperty(propertyName).getValues());
            } else {
              newLang.setProperty(propertyName, node.getProperty(propertyName).getValue());
            }
          }
          if (selectedLangNode.hasProperty(propertyName)) {
            if (selectedLangNode.getProperty(propertyName).getDefinition().isMultiple()) {
              node.setProperty(propertyName, selectedLangNode.getProperty(propertyName).getValues());
            } else {
              node.setProperty(propertyName, selectedLangNode.getProperty(propertyName).getValue());
            }
          }
        }
      }

      String childNodeName = getChildNodeType(node);
      if (childNodeName != null) {
        processWithDataChildNode(node, selectedLangNode, languageNode, defaultLanguage, getChildNodeType(node));
      }

      setMixin(node, newLang);            
      node.setProperty(EXO_LANGUAGE, language);
      setCommentNode(node, newLang, selectedLangNode);
      selectedLangNode.remove();      
      node.getSession().save();
    }
  }

  private void setMixin(Node node, Node newLang) throws Exception {
    NodeType[] mixins = node.getMixinNodeTypes();
    for (NodeType mixin : mixins) {
      if (!mixin.getName().equals("exo:actionable")) {
        newLang.addMixin(mixin.getName());
        for (PropertyDefinition def : mixin.getPropertyDefinitions()) {
          if (!def.isProtected() && def.isMandatory() && !def.isAutoCreated()) {
            String propertyName = def.getName();
            if (def.isMultiple()) {
              newLang.setProperty(propertyName, node.getProperty(propertyName).getValues());
            } else {
              newLang.setProperty(propertyName, node.getProperty(propertyName).getValue());
            }
          }
        }
      }
    }
  }

  private String getChildNodeType(Node node) throws Exception {
    if (node.hasNodes()) {
      NodeIterator iterate = node.getNodes();
      while (iterate.hasNext()) {
        Node childNode = iterate.nextNode();
        if (childNode.getPrimaryNodeType().getName().equals("nt:resource"))
          return childNode.getName();
      }
    }
    return null;
  }

  private void processWithDataChildNode(Node node, Node selectedLangNode, Node languagesNode,
      String defaultLanguage, String nodeType) throws Exception {
    Node tempNode = node.addNode("temp", "nt:unstructured");
    node.getSession().move(node.getNode(nodeType).getPath(), tempNode.getPath() + "/" + nodeType);
    node.getSession().move(selectedLangNode.getNode(nodeType).getPath(), node.getPath() + "/" + nodeType);
    node.getSession().move(tempNode.getNode(nodeType).getPath(), languagesNode.getPath() + "/" + 
        defaultLanguage + "/" + nodeType);
    tempNode.remove();
  }    
  
  public boolean hasMixin(Node node, String nodeTypeName) throws Exception {
    NodeType[] mixinTypes = node.getMixinNodeTypes() ; 
    for(NodeType nodeType : mixinTypes) {
      if(nodeType.getName().equals(nodeTypeName)) return true ;
    }
    return false ;
  }    
  
  private void setCommentNode(Node node, Node newLang, Node selectedLangNode) throws Exception {
    if(node.hasNode(COMMENTS)) {
      node.getSession().move(node.getPath() + "/" + COMMENTS, newLang.getPath() + "/" + COMMENTS) ;
    }
    if(selectedLangNode != null && selectedLangNode.hasNode(COMMENTS)) {
      node.getSession().move(selectedLangNode.getPath() + "/" + COMMENTS, node.getPath() + "/" + COMMENTS) ;
    }
  }
}
