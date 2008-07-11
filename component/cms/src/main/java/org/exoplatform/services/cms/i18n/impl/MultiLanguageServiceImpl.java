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
package org.exoplatform.services.cms.i18n.impl;

import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.PathNotFoundException;
import javax.jcr.Property;
import javax.jcr.PropertyIterator;
import javax.jcr.PropertyType;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.Value;
import javax.jcr.Workspace;
import javax.jcr.nodetype.NodeType;
import javax.jcr.nodetype.PropertyDefinition;

import org.exoplatform.commons.utils.ISO8601;
import org.exoplatform.services.cms.CmsService;
import org.exoplatform.services.cms.JcrInputProperty;
import org.exoplatform.services.cms.i18n.MultiLanguageService;
import org.exoplatform.services.jcr.impl.core.value.DateValue;
import org.exoplatform.services.jcr.impl.core.value.StringValue;

/**
 * @author Hung Nguyen Quang
 * @mail   nguyenkequanghung@yahoo.com
 */

public class MultiLanguageServiceImpl implements MultiLanguageService{
  final static public String  JCRCONTENT = "jcr:content";
  final static public String  JCRDATA = "jcr:data";
  final static public String  JCR_MIMETYPE = "jcr:mimeType";
  final static public String  NTUNSTRUCTURED = "nt:unstructured";
  final static public String  NTFILE = "nt:file";
  final static public String JCR_LASTMODIFIED = "jcr:lastModified" ;
  final static String VOTER_PROP = "exo:voter".intern() ;  
  final static String VOTING_RATE_PROP = "exo:votingRate".intern() ;
  final static String VOTE_TOTAL_PROP = "exo:voteTotal".intern() ; 
  final static String VOTE_TOTAL_LANG_PROP = "exo:voteTotalOfLang".intern() ;
  final static String NODE = "/node/" ;
  final static String NODE_LANGUAGE = "/node/languages/" ;
  final static String CONTENT_PATH = "/node/jcr:content/" ;
  final static String TEMP_NODE = "temp" ;
  private CmsService cmsService_ ;
  
  public MultiLanguageServiceImpl(CmsService cmsService) throws Exception {
    cmsService_ = cmsService ;
  }

  private void setPropertyValue(String propertyName, Node node, int requiredtype, Object value, boolean isMultiple) throws Exception {
    switch (requiredtype) {
    case PropertyType.STRING:
      if (value == null) {
        node.setProperty(propertyName, "");
      } else {
        if(isMultiple) {
          if (value instanceof String) node.setProperty(propertyName, new String[] { value.toString()});
          else if(value instanceof String[]) node.setProperty(propertyName, (String[]) value);
        } else {
          if(value instanceof StringValue) {
            StringValue strValue = (StringValue) value ;
            node.setProperty(propertyName, strValue.getString());
          } else {
            node.setProperty(propertyName, value.toString());
          }
        }
      }
      break;
    case PropertyType.BINARY:
      if (value == null) node.setProperty(propertyName, "");
      else if (value instanceof byte[]) node.setProperty(propertyName, new ByteArrayInputStream((byte[]) value));
      else if (value instanceof String) node.setProperty(propertyName, new ByteArrayInputStream((value.toString()).getBytes()));
      else if (value instanceof String[]) node.setProperty(propertyName, new ByteArrayInputStream((((String[]) value)).toString().getBytes()));      
      break;
    case PropertyType.BOOLEAN:
      if (value == null) node.setProperty(propertyName, false);
      else if (value instanceof String) node.setProperty(propertyName, new Boolean(value.toString()).booleanValue());
      else if (value instanceof String[]) node.setProperty(propertyName, (String[]) value);         
      break;
    case PropertyType.LONG:
      if (value == null || "".equals(value)) node.setProperty(propertyName, 0);
      else if (value instanceof String) node.setProperty(propertyName, new Long(value.toString()).longValue());
      else if (value instanceof String[]) node.setProperty(propertyName, (String[]) value);  
      break;
    case PropertyType.DOUBLE:
      if (value == null || "".equals(value)) node.setProperty(propertyName, 0);
      else if (value instanceof String) node.setProperty(propertyName, new Double(value.toString()).doubleValue());
      else if (value instanceof String[]) node.setProperty(propertyName, (String[]) value);        
      break;
    case PropertyType.DATE:      
      if (value == null) {        
        node.setProperty(propertyName, new GregorianCalendar());
      } else {
        if(isMultiple) {
          Session session = node.getSession() ;
          if (value instanceof String) {
            Value value2add = session.getValueFactory().createValue(ISO8601.parse((String) value));
            node.setProperty(propertyName, new Value[] {value2add});
          } else if (value instanceof String[]) {
            String[] values = (String[]) value;
            Value[] convertedCalendarValues = new Value[values.length];
            int i = 0;
            for (String stringValue : values) {
              Value value2add = session.getValueFactory().createValue(ISO8601.parse(stringValue));
              convertedCalendarValues[i] = value2add;
              i++;
            }
            node.setProperty(propertyName, convertedCalendarValues);
            session.logout();
          }
        } else {
          if(value instanceof String) {
            node.setProperty(propertyName, ISO8601.parse(value.toString()));
          } else if(value instanceof GregorianCalendar) {
            node.setProperty(propertyName, (GregorianCalendar) value);
          } else if(value instanceof DateValue) {
            DateValue dateValue = (DateValue) value ;
            node.setProperty(propertyName, dateValue.getDate());
          }
        }
      }
      break ;
    case PropertyType.REFERENCE :
      if (value == null) throw new RepositoryException("null value for a reference " + requiredtype);
      if(value instanceof Value[]) {
        node.setProperty(propertyName, (Value[]) value);
      } else if (value instanceof String) {
        Session session = node.getSession();
        if(session.getRootNode().hasNode((String)value)) {
          Node catNode = session.getRootNode().getNode((String)value);
          Value value2add = session.getValueFactory().createValue(catNode);
          node.setProperty(propertyName, new Value[] {value2add});          
        } else {
          node.setProperty(propertyName, (String) value);
        }
      }
      break ;
    }
  }
  
  private void setMixin(Node node, Node newLang) throws Exception {
    NodeType[] mixins = node.getMixinNodeTypes() ;
    for(NodeType mixin:mixins) {
      if(!mixin.getName().equals("exo:actionable")) {
        if(newLang.canAddMixin(mixin.getName())) {
          newLang.addMixin(mixin.getName()) ;
          for(PropertyDefinition def: mixin.getPropertyDefinitions()) {
            if(!def.isProtected()) {
              String propName = def.getName() ;
              if(def.isMandatory() && !def.isAutoCreated()) {
                if(def.isMultiple()) {
                  newLang.setProperty(propName,node.getProperty(propName).getValues()) ;
                } else {
                  newLang.setProperty(propName,node.getProperty(propName).getValue()) ; 
                }
              }        
            }
          }
        }
      }
    }
  }
  
  private Node addNewFileNode(String fileName, Node newLanguageNode, Value value, Object lastModified, String mimeType, String repositoryName) throws Exception {
    Map<String,JcrInputProperty> inputProperties = new HashMap<String,JcrInputProperty>() ;            
    JcrInputProperty nodeInput = new JcrInputProperty() ;
    nodeInput.setJcrPath("/node") ;
    nodeInput.setValue(fileName) ;
    nodeInput.setMixintype("mix:i18n,mix:votable,mix:commentable") ;
    nodeInput.setType(JcrInputProperty.NODE) ;
    inputProperties.put("/node",nodeInput) ;

    JcrInputProperty jcrContent = new JcrInputProperty() ;
    jcrContent.setJcrPath("/node/jcr:content") ;
    jcrContent.setValue("") ;
    jcrContent.setMixintype("dc:elementSet") ;
    jcrContent.setNodetype("nt:resource") ;
    jcrContent.setType(JcrInputProperty.NODE) ;
    inputProperties.put("/node/jcr:content",jcrContent) ;

    JcrInputProperty jcrData = new JcrInputProperty() ;
    jcrData.setJcrPath("/node/jcr:content/jcr:data") ;            
    jcrData.setValue(value) ;          
    inputProperties.put("/node/jcr:content/jcr:data",jcrData) ; 

    JcrInputProperty jcrMimeType = new JcrInputProperty() ;
    jcrMimeType.setJcrPath("/node/jcr:content/jcr:mimeType") ;
    jcrMimeType.setValue(mimeType) ;          
    inputProperties.put("/node/jcr:content/jcr:mimeType",jcrMimeType) ;

    JcrInputProperty jcrLastModified = new JcrInputProperty() ;
    jcrLastModified.setJcrPath("/node/jcr:content/jcr:lastModified") ;
    jcrLastModified.setValue(lastModified) ;
    inputProperties.put("/node/jcr:content/jcr:lastModified",jcrLastModified) ;

    JcrInputProperty jcrEncoding = new JcrInputProperty() ;
    jcrEncoding.setJcrPath("/node/jcr:content/jcr:encoding") ;
    jcrEncoding.setValue("UTF-8") ;
    inputProperties.put("/node/jcr:content/jcr:encoding",jcrEncoding) ;         
    cmsService_.storeNode(NTFILE, newLanguageNode, inputProperties, true, repositoryName) ;
    return newLanguageNode.getNode(fileName) ;
  }
  
  public Node getFileLangNode(Node languageNode) throws Exception {
    if(languageNode.getNodes().getSize() > 0) {
      NodeIterator nodeIter = languageNode.getNodes() ;
      while(nodeIter.hasNext()) {
        Node ntFile = nodeIter.nextNode() ;
        if(ntFile.getPrimaryNodeType().getName().equals("nt:file")) {
          return ntFile ;
        }
      }
      return languageNode ;
    }
    return languageNode ;
  }

  public void addLanguage(Node node, Map inputs, String language, boolean isDefault) throws Exception {
    Node newLanguageNode = null ;
    Node languagesNode = null ;
    String defaultLanguage = getDefault(node) ;
    if(node.hasNode(LANGUAGES)) languagesNode = node.getNode(LANGUAGES) ;
    else languagesNode = node.addNode(LANGUAGES, NTUNSTRUCTURED) ;
    if(!defaultLanguage.equals(language)){
      if(isDefault) {
        if(languagesNode.hasNode(defaultLanguage)) {
          newLanguageNode = languagesNode.getNode(defaultLanguage) ;
        } else {
          newLanguageNode = languagesNode.addNode(defaultLanguage) ;
          NodeType[] mixins = node.getMixinNodeTypes() ;
          for(NodeType mixin:mixins) {
            if(!mixin.getName().equals("exo:actionable")) {
              if(newLanguageNode.canAddMixin(mixin.getName())) newLanguageNode.addMixin(mixin.getName()) ;
              for(PropertyDefinition def: mixin.getPropertyDefinitions()) {
                if(!def.isProtected()) {
                  String propName = def.getName() ;
                  if(def.isMandatory() && !def.isAutoCreated()) {
                    if(def.isMultiple()) {
                      newLanguageNode.setProperty(propName,node.getProperty(propName).getValues()) ;
                    } else {
                      newLanguageNode.setProperty(propName,node.getProperty(propName).getValue()) ; 
                    }
                  }        
                }
              }
            }
          }
        }
      } else {
        if(languagesNode.hasNode(language)) {
          newLanguageNode = languagesNode.getNode(language) ;
        } else {
          newLanguageNode = languagesNode.addNode(language) ;
          NodeType[] mixins = node.getMixinNodeTypes() ;
          for(NodeType mixin : mixins) {
            if(!mixin.getName().equals("exo:actionable")) {
              if(newLanguageNode.canAddMixin(mixin.getName())) newLanguageNode.addMixin(mixin.getName()) ;
              for(PropertyDefinition def: mixin.getPropertyDefinitions()) {
                if(!def.isProtected()) {
                  String propName = def.getName() ;
                  if(def.isMandatory() && !def.isAutoCreated()) {
                    if(def.isMultiple()) {
                      newLanguageNode.setProperty(propName,node.getProperty(propName).getValues()) ;
                    } else {
                      newLanguageNode.setProperty(propName,node.getProperty(propName).getValue()) ; 
                    }
                  }        
                }
              }
            }
          }
          newLanguageNode.setProperty(EXO_LANGUAGE, language) ;
        }
      }
    }   
    PropertyDefinition[] properties = node.getPrimaryNodeType().getPropertyDefinitions() ;
    for(PropertyDefinition pro : properties){
      if(!pro.isProtected()) {
        String propertyName = pro.getName() ;
        JcrInputProperty property = (JcrInputProperty)inputs.get(NODE + propertyName) ;
        if(defaultLanguage.equals(language) && property != null) {
          setPropertyValue(propertyName, node, pro.getRequiredType(), property.getValue(), pro.isMultiple()) ;
        } else {          
          if(isDefault) {            
            if(node.hasProperty(propertyName)) {
              Object value = null ;
              int requiredType = node.getProperty(propertyName).getDefinition().getRequiredType() ;
              boolean isMultiple = node.getProperty(propertyName).getDefinition().isMultiple() ;
              if(isMultiple) value = node.getProperty(propertyName).getValues() ;
              else value = node.getProperty(propertyName).getValue() ;
              setPropertyValue(propertyName, newLanguageNode, requiredType, value, isMultiple) ;
            }
            if(property != null) {
              setPropertyValue(propertyName, node, pro.getRequiredType(), property.getValue(), pro.isMultiple()) ;
            }
          } else {
            if(property != null) {
              setPropertyValue(propertyName, newLanguageNode, pro.getRequiredType(), property.getValue(), pro.isMultiple()) ;
            }
          }
        }               
      }
    }
    if(!defaultLanguage.equals(language) && isDefault){
      Node selectedLangNode = null ;
      if(languagesNode.hasNode(language)) selectedLangNode = languagesNode.getNode(language) ;
      setVoteProperty(newLanguageNode, node, selectedLangNode) ;
      setCommentNode(node, newLanguageNode, selectedLangNode) ;
    }
    if(isDefault) node.setProperty(EXO_LANGUAGE, language) ;
    if(isDefault && languagesNode.hasNode(language)) languagesNode.getNode(language).remove() ;
    node.save() ;
    node.getSession().save() ;    
  }
  
  public void addLanguage(Node node, Map inputs, String language, boolean isDefault, String nodeType) throws Exception {
    Node newLanguageNode = null ;
    Node languagesNode = null ;
    String defaultLanguage = getDefault(node) ;
    Workspace ws = node.getSession().getWorkspace() ;
    if(node.hasNode(LANGUAGES)) languagesNode = node.getNode(LANGUAGES) ;
    else languagesNode = node.addNode(LANGUAGES, NTUNSTRUCTURED) ;
    if(!defaultLanguage.equals(language)){
      if(isDefault) {
        if(languagesNode.hasNode(defaultLanguage)) {
          newLanguageNode = languagesNode.getNode(defaultLanguage) ;
        } else {
          newLanguageNode = languagesNode.addNode(defaultLanguage) ;
          NodeType[] mixins = node.getMixinNodeTypes() ;
          for(NodeType mixin:mixins) {
            if(!mixin.getName().equals("exo:actionable")) {
              if(newLanguageNode.canAddMixin(mixin.getName())) newLanguageNode.addMixin(mixin.getName()) ;            
              for(PropertyDefinition def: mixin.getPropertyDefinitions()) {
                if(!def.isProtected()) {
                  String propName = def.getName() ;
                  if(def.isMandatory() && !def.isAutoCreated()) {
                    if(def.isMultiple()) {
                      newLanguageNode.setProperty(propName,node.getProperty(propName).getValues()) ;
                    } else {
                      newLanguageNode.setProperty(propName,node.getProperty(propName).getValue()) ; 
                    }
                  }        
                }
              }
            }
          }
        }
      } else {
        if(languagesNode.hasNode(language)) {
          newLanguageNode = languagesNode.getNode(language) ;
        } else {
          newLanguageNode = languagesNode.addNode(language) ;
          NodeType[] mixins = node.getMixinNodeTypes() ;
          for(NodeType mixin : mixins) {
            if(!mixin.getName().equals("exo:actionable")) {
              if(newLanguageNode.canAddMixin(mixin.getName())) newLanguageNode.addMixin(mixin.getName()) ;
              for(PropertyDefinition def: mixin.getPropertyDefinitions()) {
                if(!def.isProtected()) {
                  String propName = def.getName() ;
                  if(def.isMandatory() && !def.isAutoCreated()) {
                    if(def.isMultiple()) {
                      newLanguageNode.setProperty(propName,node.getProperty(propName).getValues()) ;
                    } else {
                      newLanguageNode.setProperty(propName,node.getProperty(propName).getValue()) ; 
                    }
                  }        
                }
              }
            }
          }
          newLanguageNode.setProperty(EXO_LANGUAGE, language) ;
        }
      }
      Node jcrContent = node.getNode(nodeType) ;
      node.save() ;
      if(!newLanguageNode.hasNode(nodeType)) {
        ws.copy(jcrContent.getPath(), newLanguageNode.getPath() + "/" + jcrContent.getName()) ;
      }
      Node newContentNode = newLanguageNode.getNode(nodeType) ;
      PropertyIterator props = newContentNode.getProperties() ;
      while(props.hasNext()) {
        Property prop = props.nextProperty() ;
        if(inputs.containsKey(NODE + nodeType + "/" + prop.getName())) {
          JcrInputProperty inputVariable = (JcrInputProperty) inputs.get(NODE + nodeType + "/" + prop.getName()) ;
          boolean isMultiple = prop.getDefinition().isMultiple() ;
          setPropertyValue(prop.getName(), newContentNode, prop.getType(), inputVariable.getValue(), isMultiple) ;
        }
      }
      if(isDefault) {
        Node tempNode = node.addNode(TEMP_NODE, "nt:unstructured") ;
        node.getSession().move(node.getNode(nodeType).getPath(), tempNode.getPath() + "/" + nodeType) ;
        node.getSession().move(newLanguageNode.getNode(nodeType).getPath(), node.getPath() + "/" + nodeType) ;
        node.getSession().move(tempNode.getNode(nodeType).getPath(), languagesNode.getPath() + "/" + defaultLanguage + "/" + nodeType) ;
        tempNode.remove() ;
      }      
    } else {
      JcrInputProperty inputVariable = (JcrInputProperty) inputs.get(NODE + nodeType + "/" + JCRDATA) ;
      setPropertyValue(JCRDATA, node.getNode(nodeType), inputVariable.getType(), inputVariable.getValue(), false) ;
    }
    PropertyDefinition[] properties = node.getPrimaryNodeType().getPropertyDefinitions() ;
    for(PropertyDefinition pro : properties){
      if(!pro.isProtected()) {
        String propertyName = pro.getName() ;
        JcrInputProperty property = (JcrInputProperty)inputs.get(NODE + propertyName) ;
        if(defaultLanguage.equals(language) && property != null) {
          setPropertyValue(propertyName, node, pro.getRequiredType(), property.getValue(), pro.isMultiple()) ;
        } else {          
          if(isDefault) {            
            if(node.hasProperty(propertyName)) {
              Object value = null ;
              int requiredType = node.getProperty(propertyName).getDefinition().getRequiredType() ;
              boolean isMultiple = node.getProperty(propertyName).getDefinition().isMultiple() ;
              if(isMultiple) value = node.getProperty(propertyName).getValues() ;
              else value = node.getProperty(propertyName).getValue() ;
              setPropertyValue(propertyName, newLanguageNode, requiredType, value, isMultiple) ;
            }
            if(property != null) {
              setPropertyValue(propertyName, node, pro.getRequiredType(), property.getValue(), pro.isMultiple()) ;
            }
          } else {
            if(property != null) {
              setPropertyValue(propertyName, newLanguageNode, pro.getRequiredType(), property.getValue(), pro.isMultiple()) ;
            }
          }
        }               
      }
    }
    if(!defaultLanguage.equals(language) && isDefault){
      Node selectedLangNode = null ;
      if(languagesNode.hasNode(language)) selectedLangNode = languagesNode.getNode(language) ;
      setVoteProperty(newLanguageNode, node, selectedLangNode) ;
      setCommentNode(node, newLanguageNode, selectedLangNode) ;
    }
    if(isDefault) node.setProperty(EXO_LANGUAGE, language) ;
    if(isDefault && languagesNode.hasNode(language)) languagesNode.getNode(language).remove() ;
    node.save() ;
    node.getSession().save() ;    
  }
  
  public void addFileLanguage(Node node, String fileName, Value value, String mimeType, String language, String repositoryName, boolean isDefault) throws Exception {
    Node newLanguageNode = null ;
    Node languagesNode = null ;
    String defaultLanguage = getDefault(node) ;
    Node ntFileLangNode = null ;
    Node oldJcrContent = node.getNode(JCRCONTENT) ;
    String olfFileName = node.getName() ;
    Value oldValue = oldJcrContent.getProperty(JCRDATA).getValue() ;
    String oldMimeType = oldJcrContent.getProperty(JCR_MIMETYPE).getString() ;
    Object oldLastModified = oldJcrContent.getProperty(JCR_LASTMODIFIED).getDate().getTime() ;
    try {
      languagesNode = node.getNode(LANGUAGES) ;
    } catch(PathNotFoundException pe) {
      languagesNode = node.addNode(LANGUAGES, NTUNSTRUCTURED) ;
    }
    if(!defaultLanguage.equals(language)){
      if(isDefault) {
        try {
          newLanguageNode = languagesNode.getNode(defaultLanguage) ;
        } catch(PathNotFoundException pe) {
          newLanguageNode = languagesNode.addNode(defaultLanguage) ;
        }
        oldJcrContent.setProperty(JCR_MIMETYPE, mimeType) ;
        oldJcrContent.setProperty(JCRDATA, value) ;
        oldJcrContent.setProperty(JCR_LASTMODIFIED, new GregorianCalendar()) ;
      } else {
        try {
          newLanguageNode = languagesNode.getNode(language) ;
        } catch(PathNotFoundException pe) {
          newLanguageNode = languagesNode.addNode(language) ;
        }
      }
      node.save() ;
      try {
        ntFileLangNode = newLanguageNode.getNode(fileName) ;
      } catch(PathNotFoundException pe) {
        if(isDefault) {
          ntFileLangNode = addNewFileNode(olfFileName, newLanguageNode, oldValue, oldLastModified, oldMimeType, repositoryName) ;
        } else {
          ntFileLangNode = addNewFileNode(fileName, newLanguageNode, value, 
              new GregorianCalendar(), mimeType, repositoryName) ;
        }
      }
      Node newJcrContent = ntFileLangNode.getNode(JCRCONTENT) ;
      newJcrContent.setProperty(JCRDATA, value) ;
      newJcrContent.setProperty(JCR_LASTMODIFIED, new GregorianCalendar()) ;
      setMixin(node, ntFileLangNode) ;
    } else {
      node.getNode(JCRCONTENT).setProperty(JCRDATA, value) ;   
    }
    if(!defaultLanguage.equals(language) && isDefault){
      Node selectedFileLangeNode = null ;
      if(languagesNode.hasNode(language)) {
        Node selectedLangNode = languagesNode.getNode(language) ;
        selectedFileLangeNode = selectedLangNode.getNode(node.getName()) ;
      }
      setVoteProperty(ntFileLangNode, node, selectedFileLangeNode) ;
      setCommentNode(node, ntFileLangNode, selectedFileLangeNode) ;
    }
    if(isDefault) node.setProperty(EXO_LANGUAGE, language) ;
    node.getSession().save() ;    
  }
  
  public void addFileLanguage(Node node, String language, Map mappings, boolean isDefault) throws Exception {
    Node newLanguageNode = null ;
    Node languagesNode = null ;
    Workspace ws = node.getSession().getWorkspace() ;
    String defaultLanguage = getDefault(node) ;
    if(node.hasNode(LANGUAGES)) languagesNode = node.getNode(LANGUAGES) ;
    else languagesNode = node.addNode(LANGUAGES, NTUNSTRUCTURED) ;
    if(!defaultLanguage.equals(language)){
      if(isDefault) {
        if(languagesNode.hasNode(defaultLanguage)) newLanguageNode = languagesNode.getNode(defaultLanguage) ;
        else newLanguageNode = languagesNode.addNode(defaultLanguage) ;
      } else {
        if(languagesNode.hasNode(language)) newLanguageNode = languagesNode.getNode(language) ;
        else newLanguageNode = languagesNode.addNode(language) ;
      }
      Node jcrContent = node.getNode(JCRCONTENT) ;
      node.save() ;
      if(!newLanguageNode.hasNode(JCRCONTENT)) {
        ws.copy(jcrContent.getPath(), newLanguageNode.getPath() + "/" + jcrContent.getName()) ;
      }
      Node newContentNode = newLanguageNode.getNode(JCRCONTENT) ;
      PropertyIterator props = newContentNode.getProperties() ;
      while(props.hasNext()) {
        Property prop = props.nextProperty() ;
        if(mappings.containsKey(CONTENT_PATH + prop.getName())) {
          JcrInputProperty inputVariable = (JcrInputProperty) mappings.get(CONTENT_PATH + prop.getName()) ;
          boolean isMultiple = prop.getDefinition().isMultiple() ;
          setPropertyValue(prop.getName(), newContentNode, prop.getType(), inputVariable.getValue(), isMultiple) ;
        }
      }
      if(isDefault) {
        Node tempNode = node.addNode(TEMP_NODE, "nt:unstructured") ;
        node.getSession().move(node.getNode(JCRCONTENT).getPath(), tempNode.getPath() + "/" + JCRCONTENT) ;
        node.getSession().move(newLanguageNode.getNode(JCRCONTENT).getPath(), node.getPath() + "/" + JCRCONTENT) ;
        node.getSession().move(tempNode.getNode(JCRCONTENT).getPath(), languagesNode.getPath() + "/" + defaultLanguage + "/" + JCRCONTENT) ;
        tempNode.remove() ;
      }
      // add mixin type for node
      setMixin(node, newLanguageNode) ;
    } else {
      JcrInputProperty inputVariable = (JcrInputProperty) mappings.get(CONTENT_PATH + JCRDATA) ;
      setPropertyValue(JCRDATA, node.getNode(JCRCONTENT), inputVariable.getType(), inputVariable.getValue(), false) ;
    }
    PropertyDefinition[] properties = node.getPrimaryNodeType().getPropertyDefinitions() ;
    for(PropertyDefinition pro : properties){
      if(!pro.isProtected()) {
        String propertyName = pro.getName() ;
        JcrInputProperty property = (JcrInputProperty)mappings.get(NODE + propertyName) ;
        if(defaultLanguage.equals(language) && property != null) {
          setPropertyValue(propertyName, node, pro.getRequiredType(), property.getValue(), pro.isMultiple()) ;
        } else {          
          if(isDefault) {            
            if(node.hasProperty(propertyName)) {
              Object value = null ;
              int requiredType = node.getProperty(propertyName).getDefinition().getRequiredType() ;
              boolean isMultiple = node.getProperty(propertyName).getDefinition().isMultiple() ;
              if(isMultiple) value = node.getProperty(propertyName).getValues() ;
              else value = node.getProperty(propertyName).getValue() ;
              setPropertyValue(propertyName, newLanguageNode, requiredType, value, isMultiple) ;
            }
            if(property != null) {
              setPropertyValue(propertyName, node, pro.getRequiredType(), property.getValue(), pro.isMultiple()) ;
            }
          } else {
            if(property != null) {
              setPropertyValue(propertyName, newLanguageNode, pro.getRequiredType(), property.getValue(), pro.isMultiple()) ;
            }
          }
        }               
      }
    }    
    if(!defaultLanguage.equals(language) && isDefault) {
      Node selectedLangNode = null ;
      if(languagesNode.hasNode(language)) selectedLangNode = languagesNode.getNode(language) ;
      setVoteProperty(newLanguageNode, node, selectedLangNode) ;
      setCommentNode(node, newLanguageNode, selectedLangNode) ;
    }
    if(isDefault) node.setProperty(EXO_LANGUAGE, language) ;
    node.save() ;
    node.getSession().save() ;    
  }
  
  public String getDefault(Node node) throws Exception {
    if(node.hasProperty(EXO_LANGUAGE)) return node.getProperty(EXO_LANGUAGE).getString() ;
    return null ;
  }

  public List<String> getSupportedLanguages(Node node) throws Exception {
    List<String> languages = new ArrayList<String>();
    String defaultLang = getDefault(node) ;
    if(defaultLang != null) languages.add(defaultLang) ;
    if(node.hasNode(LANGUAGES)){
      Node languageNode = node.getNode(LANGUAGES) ;
      NodeIterator iter  = languageNode.getNodes() ;      
      while(iter.hasNext()) {
        languages.add(iter.nextNode().getName());
      }
    } 
    return languages;
  }

  private void setVoteProperty(Node newLang, Node node, Node selectedLangNode) throws Exception {
    if(hasMixin(newLang, "mix:votable")) {
      newLang.setProperty(VOTE_TOTAL_PROP, getVoteTotal(node)) ; 
      newLang.setProperty(VOTE_TOTAL_LANG_PROP, node.getProperty(VOTE_TOTAL_LANG_PROP).getLong()) ;
      newLang.setProperty(VOTING_RATE_PROP, node.getProperty(VOTING_RATE_PROP).getLong()) ;
      if(node.hasProperty(VOTER_PROP)) {
        newLang.setProperty(VOTER_PROP, node.getProperty(VOTER_PROP).getValues()) ;
      }
      if(selectedLangNode != null) {
        node.setProperty(VOTE_TOTAL_PROP, getVoteTotal(node)) ; 
        if(selectedLangNode.hasProperty(VOTE_TOTAL_LANG_PROP)) {
          node.setProperty(VOTE_TOTAL_LANG_PROP, selectedLangNode.getProperty(VOTE_TOTAL_LANG_PROP).getLong()) ;
        } else {
          node.setProperty(VOTE_TOTAL_LANG_PROP, 0) ;
        }
        if(selectedLangNode.hasProperty(VOTING_RATE_PROP)) {
          node.setProperty(VOTING_RATE_PROP, selectedLangNode.getProperty(VOTING_RATE_PROP).getLong()) ;
        } else {
          node.setProperty(VOTING_RATE_PROP, 0) ;
        }
        if(selectedLangNode.hasProperty(VOTER_PROP)) {
          node.setProperty(VOTER_PROP, selectedLangNode.getProperty(VOTER_PROP).getValues()) ;
        }
      } else {
        node.setProperty(VOTE_TOTAL_PROP, getVoteTotal(node)) ;
        node.setProperty(VOTE_TOTAL_LANG_PROP, 0) ;
        node.setProperty(VOTING_RATE_PROP, 0) ;
      }
    }
  }
  
  private void setCommentNode(Node node, Node newLang, Node selectedLangNode) throws Exception {
    if(node.hasNode(COMMENTS)) {
      node.getSession().move(node.getPath() + "/" + COMMENTS, newLang.getPath() + "/" + COMMENTS) ;
    }
    if(selectedLangNode != null && selectedLangNode.hasNode(COMMENTS)) {
      node.getSession().move(selectedLangNode.getPath() + "/" + COMMENTS, node.getPath() + "/" + COMMENTS) ;
    }
  }
  
  public long getVoteTotal(Node node) throws Exception {
    long voteTotal = 0;
    if(!node.hasNode(LANGUAGES) && node.hasProperty(VOTE_TOTAL_PROP)) {
      return node.getProperty(VOTE_TOTAL_LANG_PROP).getLong() ;
    }
    Node multiLanguages = node.getNode(LANGUAGES) ;
    voteTotal = node.getProperty(VOTE_TOTAL_LANG_PROP).getLong() ;
    NodeIterator nodeIter = multiLanguages.getNodes() ;
    String defaultLang = getDefault(node) ;
    while(nodeIter.hasNext()) {
      Node languageNode = nodeIter.nextNode() ;
      if(node.getPrimaryNodeType().getName().equals(NTFILE)) {
        languageNode = getFileLangNode(languageNode) ;
      }
      if(!languageNode.getName().equals(defaultLang) && languageNode.hasProperty(VOTE_TOTAL_LANG_PROP)) {
        voteTotal = voteTotal + languageNode.getProperty(VOTE_TOTAL_LANG_PROP).getLong() ;
      }
    }
    return voteTotal ;
  }
  
  private boolean hasMixin(Node node, String nodeTypeName) throws Exception {
    NodeType[] mixinTypes = node.getMixinNodeTypes() ; 
    for(NodeType nodeType : mixinTypes) {
      if(nodeType.getName().equals(nodeTypeName)) return true ;
    }
    return false ;
  }
  
  public void setDefault(Node node, String language, String repositoryName) throws Exception {
    String defaultLanguage = getDefault(node) ;
    if(!defaultLanguage.equals(language)){
      Node languagesNode = null ;
      try {
        languagesNode = node.getNode(LANGUAGES) ;
      } catch(PathNotFoundException pe) {
        languagesNode = node.addNode(LANGUAGES, NTUNSTRUCTURED) ;
      }
      Node selectedLangNode = languagesNode.getNode(language) ;
      Node newLang = languagesNode.addNode(defaultLanguage) ;
      if(node.getPrimaryNodeType().getName().equals(NTFILE)) {
        selectedLangNode = getFileLangNode(selectedLangNode) ;
        Node jcrContentNode = node.getNode(JCRCONTENT) ;
        newLang = addNewFileNode(node.getName(), newLang, jcrContentNode.getProperty(JCRDATA).getValue(), 
            new GregorianCalendar(), jcrContentNode.getProperty(JCR_MIMETYPE).getString(), repositoryName) ;
        Node newJcrContent = newLang.getNode(JCRCONTENT) ;
        newJcrContent.setProperty(JCRDATA, jcrContentNode.getProperty(JCRDATA).getValue()) ;
        newJcrContent.setProperty(JCR_MIMETYPE, jcrContentNode.getProperty(JCR_MIMETYPE).getString()) ;
      }
      PropertyDefinition[] properties = node.getPrimaryNodeType().getPropertyDefinitions() ;
      for(PropertyDefinition pro : properties){
        if(!pro.isProtected()){
          String propertyName = pro.getName() ;
          if(node.hasProperty(propertyName)) {
            if(node.getProperty(propertyName).getDefinition().isMultiple()) {
              Value[] values = node.getProperty(propertyName).getValues() ;
              newLang.setProperty(propertyName, values) ;
            } else {
              newLang.setProperty(propertyName, node.getProperty(propertyName).getValue()) ;
            }
          }
          if(selectedLangNode.hasProperty(propertyName)) {
            if(selectedLangNode.getProperty(propertyName).getDefinition().isMultiple()) {
              Value[] values = selectedLangNode.getProperty(propertyName).getValues() ;
              node.setProperty(propertyName, values) ;
            } else {
              node.setProperty(propertyName, selectedLangNode.getProperty(propertyName).getValue()) ;
            }
          }
        }
      }
      if(node.getPrimaryNodeType().getName().equals(NTFILE)) {
        Node tempNode = node.addNode(TEMP_NODE, "nt:unstructured") ;
        node.getSession().move(node.getNode(JCRCONTENT).getPath(), tempNode.getPath() + "/" + JCRCONTENT) ;
        node.getSession().move(selectedLangNode.getNode(JCRCONTENT).getPath(), node.getPath() + "/" + JCRCONTENT) ;
        tempNode.remove() ;
      } else if(hasNodeTypeNTResource(node)) {
        processWithDataChildNode(node, selectedLangNode, languagesNode, defaultLanguage, getChildNodeType(node)) ;
      }
      setMixin(node, newLang) ;
      setVoteProperty(newLang, node, selectedLangNode) ;
      node.setProperty(EXO_LANGUAGE, language) ;
      setCommentNode(node, newLang, selectedLangNode) ;
      if(node.getPrimaryNodeType().getName().equals(NTFILE)) {
        languagesNode.getNode(language).remove() ;
      } else {
        selectedLangNode.remove() ;
      }
      node.save() ;
      node.getSession().save() ;
    }
  }
  
  private void processWithDataChildNode(Node node, Node selectedLangNode, Node languagesNode, 
      String defaultLanguage, String nodeType) throws Exception {
    Node tempNode = node.addNode(TEMP_NODE, "nt:unstructured") ;
    node.getSession().move(node.getNode(nodeType).getPath(), tempNode.getPath() + "/" + nodeType) ;
    node.getSession().move(selectedLangNode.getNode(nodeType).getPath(), node.getPath() + "/" + nodeType) ;
    node.getSession().move(tempNode.getNode(nodeType).getPath(), languagesNode.getPath() + "/" + defaultLanguage + "/" + nodeType) ;
    tempNode.remove() ;
  }
  
  private boolean hasNodeTypeNTResource(Node node) throws Exception {
    if(node.hasNodes()) {
      NodeIterator nodeIter = node.getNodes() ;
      while(nodeIter.hasNext()) {
        Node childNode = nodeIter.nextNode() ;
        if(childNode.getPrimaryNodeType().getName().equals("nt:resource")) return true ;
      }
    }
    return false ;
  }
  
  private String getChildNodeType(Node node) throws Exception {
    if(node.hasNodes()) {
      NodeIterator nodeIter = node.getNodes() ;
      while(nodeIter.hasNext()) {
        Node childNode = nodeIter.nextNode() ;
        if(childNode.getPrimaryNodeType().getName().equals("nt:resource")) return childNode.getName() ;
      }
    }
    return null ;
  }

  public Node getLanguage(Node node, String language) throws Exception {
    if(node.hasNode(LANGUAGES + "/"+ language)) return node.getNode(LANGUAGES + "/"+ language) ;
    return null;
  }
}