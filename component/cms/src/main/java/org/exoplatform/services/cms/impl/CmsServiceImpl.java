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
package org.exoplatform.services.cms.impl;

import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.GregorianCalendar;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

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
import javax.jcr.nodetype.NodeDefinition;
import javax.jcr.nodetype.NodeType;
import javax.jcr.nodetype.NodeTypeManager;
import javax.jcr.nodetype.PropertyDefinition;

import org.apache.commons.lang.StringUtils;
import org.exoplatform.commons.utils.ISO8601;
import org.exoplatform.services.cms.CmsService;
import org.exoplatform.services.cms.JcrInputProperty;
import org.exoplatform.services.idgenerator.IDGeneratorService;
import org.exoplatform.services.jcr.RepositoryService;
import org.exoplatform.services.jcr.core.ManageableRepository;
import org.exoplatform.services.jcr.core.nodetype.ExtendedItemDefinition;


/**
 * @author benjaminmestrallet
 */
public class CmsServiceImpl implements CmsService {

  private RepositoryService jcrService;  
  private IDGeneratorService idGeneratorService;  
  final static private String MIX_REFERENCEABLE = "mix:referenceable" ;

  public CmsServiceImpl(RepositoryService jcrService, IDGeneratorService idGeneratorService) {
    this.idGeneratorService = idGeneratorService;
    this.jcrService = jcrService;      
  }

  public String storeNode(String workspace, String nodeTypeName,
      String storePath, Map mappings,String repository) throws Exception {    
    Session session = jcrService.getRepository(repository).login(workspace);
    Node storeHomeNode = (Node) session.getItem(storePath);
    String path = storeNode(nodeTypeName, storeHomeNode, mappings, true,repository);
    storeHomeNode.save();
    session.save();
    session.logout();
    return path;
  }

  @SuppressWarnings("unused")
  public String storeNode(String nodeTypeName, Node storeHomeNode, Map mappings, 
      boolean isAddNew, String repository) throws Exception {    
    Set keys = mappings.keySet();
    String nodePath = extractNodeName(keys);
    JcrInputProperty relRootProp = (JcrInputProperty) mappings.get(nodePath); 
    String nodeName = (String)relRootProp.getValue();    
    if (nodeName == null || nodeName.length() == 0) {      
      nodeName = idGeneratorService.generateStringID(nodeTypeName);
    }
    String primaryType = relRootProp.getNodetype() ;
    if(primaryType == null || primaryType.length() == 0) {
      primaryType = nodeTypeName ;
    }
    Session session = storeHomeNode.getSession();
    NodeTypeManager nodetypeManager = session.getWorkspace().getNodeTypeManager();
    NodeType nodeType = nodetypeManager.getNodeType(primaryType);    
    Node currentNode = null;
    String[] mixinTypes = null ;
    String mixintypeName = relRootProp.getMixintype();
    if(mixintypeName != null && mixintypeName.trim().length() > 0) {
      if(mixintypeName.indexOf(",") > -1){
        mixinTypes = mixintypeName.split(",") ;
      }else {
        mixinTypes = new String[] {mixintypeName} ;
      }
    }
    if (isAddNew) {
      currentNode = storeHomeNode.addNode(nodeName, primaryType);            
      if(mixinTypes != null){
        for(String type : mixinTypes){
          if(!currentNode.isNodeType(type)) {
            currentNode.addMixin(type);
          }
          NodeType mixinType = nodetypeManager.getNodeType(type);          
          createNodeRecursively(NODE, currentNode, mixinType, mappings);
        }
      }        
      createNodeRecursively(NODE, currentNode, nodeType, mappings);                       
    } else {
      currentNode = storeHomeNode.getNode(nodeName);      
      updateNodeRecursively(NODE, currentNode, nodeType, mappings);
      if(currentNode.isNodeType("exo:datetime")) {
        currentNode.setProperty("exo:dateModified",new GregorianCalendar()) ;
      }
    }
    return currentNode.getPath();
  }

  @SuppressWarnings("unused")
  public String storeNodeByUUID(String nodeTypeName, Node storeHomeNode, Map mappings, 
      boolean isAddNew, String repository) throws Exception {    
    Set keys = mappings.keySet();
    String nodePath = extractNodeName(keys);
    JcrInputProperty relRootProp = (JcrInputProperty) mappings.get(nodePath); 
    String nodeName = (String)relRootProp.getValue();    
    if (nodeName == null || nodeName.length() == 0) {      
      nodeName = idGeneratorService.generateStringID(nodeTypeName);
    }
    String primaryType = relRootProp.getNodetype() ;
    if(primaryType == null || primaryType.length() == 0) {
      primaryType = nodeTypeName ;
    }
    Session session = storeHomeNode.getSession();
    NodeTypeManager nodetypeManager = session.getWorkspace().getNodeTypeManager();
    NodeType nodeType = nodetypeManager.getNodeType(primaryType);    
    Node currentNode = null;
    String[] mixinTypes = null ;
    String mixintypeName = relRootProp.getMixintype();
    if(mixintypeName != null && mixintypeName.trim().length() > 0) {
      if(mixintypeName.indexOf(",") > -1){
        mixinTypes = mixintypeName.split(",") ;
      }else {
        mixinTypes = new String[] {mixintypeName} ;
      }
    }
    if (isAddNew) {
      currentNode = storeHomeNode.addNode(nodeName, primaryType);            
      if(mixinTypes != null){
        for(String type : mixinTypes){
          if(!currentNode.isNodeType(type)) {
            currentNode.addMixin(type);
          }
          NodeType mixinType = nodetypeManager.getNodeType(type);          
          createNodeRecursively(NODE, currentNode, mixinType, mappings);
        }
      }        
      createNodeRecursively(NODE, currentNode, nodeType, mappings);                       
    } else {
      currentNode = storeHomeNode.getNode(nodeName);      
      updateNodeRecursively(NODE, currentNode, nodeType, mappings);
      if(currentNode.isNodeType("exo:datetime")) {
        currentNode.setProperty("exo:dateModified",new GregorianCalendar()) ;
      }
    }
    if(!currentNode.isNodeType(MIX_REFERENCEABLE)) currentNode.addMixin(MIX_REFERENCEABLE) ;
    return currentNode.getUUID();
  }

  private void updateNodeRecursively(String path, Node currentNode,
      NodeType currentNodeType, Map jcrVariables) throws Exception {
    processNodeRecursively(false, path, currentNode, currentNodeType,
        jcrVariables);
  }

  private void createNodeRecursively(String path, Node currentNode,
      NodeType currentNodeType, Map jcrVariables) throws Exception {
    processNodeRecursively(true, path, currentNode, currentNodeType,
        jcrVariables);
  }

  private void processAddEditProperty(boolean create, Node currentNode, String path, NodeType currentNodeType, Map jcrVariables) throws Exception {
    if(create) {
      PropertyDefinition[] propertyDefs = currentNodeType.getPropertyDefinitions();
      for (int i = 0; i < propertyDefs.length; i++) {      
        PropertyDefinition propertyDef = propertyDefs[i];         
        if (!propertyDef.isAutoCreated() && !propertyDef.isProtected()) {        
          String propertyName = propertyDef.getName();     
          int requiredtype = propertyDef.getRequiredType();
          String currentPath = path + "/" + propertyName;
          JcrInputProperty inputVariable = (JcrInputProperty) jcrVariables.get(currentPath) ;
          Object value = null;
          if(inputVariable != null) value = inputVariable.getValue();
          if(value != null || propertyDef.isMandatory()) {
            processProperty(propertyName, currentNode, requiredtype, value, propertyDef.isMultiple()) ;
          }
        }
      }
    }
  }

  private void processNodeRecursively(boolean create, String itemPath, 
      Node currentNode, NodeType currentNodeType, Map jcrVariables)
  throws Exception {
    if(create) {
      processAddEditProperty(true, currentNode, itemPath, currentNodeType, jcrVariables) ;
    } else {
      for(PropertyIterator pi = currentNode.getProperties(); pi.hasNext();) {
        Property property = pi.nextProperty();
        PropertyDefinition propertyDef = property.getDefinition();
        String propertyName = property.getName();
        int requiredtype = property.getType();
        String currentPath = itemPath + "/" + propertyName;
        JcrInputProperty inputVariable = (JcrInputProperty) jcrVariables.get(currentPath) ;
        Object value = null;
        if(inputVariable != null) value = inputVariable.getValue();
        if(value != null &&  !propertyDef.isProtected()) {
          processProperty(propertyName, currentNode, requiredtype, value, propertyDef.isMultiple());
        }
      }
      processAddEditProperty(false, currentNode, itemPath, currentNodeType, jcrVariables) ;
    }
    int itemLevel = StringUtils.countMatches(itemPath, "/") ;            
    List<JcrInputProperty>childNodeInputs = extractNodeInputs(jcrVariables, itemLevel + 1) ;    
    NodeTypeManager nodeTypeManger = currentNode.getSession().getWorkspace().getNodeTypeManager();
    List<Object> childs = new ArrayList<Object>();
    if (create) {            
      for (NodeDefinition childNodeDef : currentNodeType.getChildNodeDefinitions()) {        
        childs.add(childNodeDef);             
      }
    } else {
      for(NodeIterator iterator = currentNode.getNodes(); iterator.hasNext();) {
        childs.add(iterator.nextNode());
      }      
    }        
    for(Object obj : childs){  
      NodeDefinition nodeDef;      
      if (obj instanceof Node) {
        nodeDef = ((Node) obj).getDefinition();        
      } else {
        nodeDef = (NodeDefinition) obj;
      } 
      if(nodeDef.isAutoCreated() || nodeDef.isProtected() || !(obj instanceof NodeDefinition)) {
        continue ;
      }            
      if(((ExtendedItemDefinition)nodeDef).isResidualSet()) {
        for(JcrInputProperty input:childNodeInputs) {
          String primaryNodeType = input.getNodetype();
          NodeType nodeType = nodeTypeManger.getNodeType(primaryNodeType) ;
          if(!canAddNode(nodeDef, nodeType)) continue ;
          String[] mixinTypes = null ; 
          if(input.getMixintype()!= null) {
            mixinTypes = input.getMixintype().split(",") ; 
          }                   
          Node childNode = doAddNode(currentNode, (String)input.getValue(), nodeType.getName(), mixinTypes) ;
          String childItemPath = itemPath + "/" + childNode.getName();
          processNodeRecursively(create, childItemPath, childNode, childNode.getPrimaryNodeType(), jcrVariables);          
        }
      }else {               
        String childNodeName = null;
        if (obj instanceof Node) {          
          childNodeName = ((Node) obj).getName();
        } else {
          childNodeName = ((NodeDefinition) obj).getName();
        }
        String newItemPath = itemPath + "/" + childNodeName;
        JcrInputProperty jcrInputVariable = (JcrInputProperty) jcrVariables.get(newItemPath);
        if(jcrInputVariable == null) continue ;
        String nodeTypeName = jcrInputVariable.getNodetype();
        String[] mixinTypes = null ; 
        if(jcrInputVariable.getMixintype()!= null) {
          mixinTypes = jcrInputVariable.getMixintype().split(",") ; 
        }           
        NodeType nodeType = null;
        if(obj instanceof Node) {
          nodeType = ((Node) obj).getPrimaryNodeType();
        } else if (nodeTypeName == null || nodeTypeName.length() == 0) {
          nodeType = nodeDef.getRequiredPrimaryTypes()[0];
        } else {
          nodeType = nodeTypeManger.getNodeType(nodeTypeName);
        }
        Node childNode = doAddNode(currentNode, childNodeName, nodeType.getName(), mixinTypes) ;        
        processNodeRecursively(create, newItemPath, childNode, childNode.getPrimaryNodeType(), jcrVariables);                  
      }
    }    
  }

  private void processProperty(String propertyName, Node node, int requiredtype,
      Object value, boolean isMultiple) throws Exception {

    switch (requiredtype) {
    case PropertyType.STRING:
      if (value == null)
        node.setProperty(propertyName, "");
      else {
        if(isMultiple) {
          if (value instanceof String)
            node.setProperty(propertyName, new String[] { (String)value});
          else if (value instanceof String[])
            node.setProperty(propertyName, (String[]) value);          
        } else
          node.setProperty(propertyName, (String) value);
      }
      break;
    case PropertyType.BINARY:
      if (value == null)
        node.setProperty(propertyName, "");
      else if (value instanceof byte[])
        node.setProperty(propertyName, 
            new ByteArrayInputStream((byte[]) value));
      else if (value instanceof String)
        node.setProperty(propertyName, 
            new ByteArrayInputStream(((String)value).getBytes()));
      else if (value instanceof String[])        
        node.setProperty(propertyName, 
            new ByteArrayInputStream((((String[]) value)).toString().getBytes()));      
      break;
    case PropertyType.BOOLEAN:
      if (value == null)
        node.setProperty(propertyName, false);
      else if (value instanceof String)
        node.setProperty(propertyName, 
            new Boolean((String) value).booleanValue());
      else if (value instanceof String[])
        node.setProperty(propertyName, (String[]) value);         
      break;
    case PropertyType.LONG:
      if (value == null || "".equals(value))
        node.setProperty(propertyName, 0);
      else if (value instanceof String)
        node.setProperty(propertyName, new Long((String) value).longValue());
      else if (value instanceof String[])
        node.setProperty(propertyName, (String[]) value);  
      break;
    case PropertyType.DOUBLE:
      if (value == null || "".equals(value))
        node.setProperty(propertyName, 0);
      else if (value instanceof String)
        node.setProperty(propertyName, new Double((String) value).doubleValue());
      else if (value instanceof String[])
        node.setProperty(propertyName, (String[]) value);        
      break;
    case PropertyType.DATE:      
      if (value == null){        
        node.setProperty(propertyName, new GregorianCalendar());
      } else {
        if(isMultiple) {
          Session session = node.getSession();
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
          }
        } else {
          if (value instanceof String) {
            node.setProperty(propertyName, ISO8601.parse((String)value));
          } else if (value instanceof GregorianCalendar) {
            node.setProperty(propertyName, (GregorianCalendar) value);
          }

        }
      }      
      break;
    case PropertyType.REFERENCE:      
      if (value == null) throw new RepositoryException("null value for a reference " + requiredtype);
      if (value instanceof Value[]) {
        node.setProperty(propertyName, (Value[]) value);
      } else if (value instanceof String) {
        String referenceWorksapce = null;
        String referenceNodeName = null ;
        Session session = node.getSession();
        String repositoty = ((ManageableRepository)session.getRepository()).getConfiguration().getName();
        if(((String)value).indexOf(":/") > -1) {
          referenceWorksapce = ((String)value).split(":/")[0];
          referenceNodeName = ((String)value).split(":/")[1] ;
          Session session2 = jcrService.getRepository(repositoty).getSystemSession(referenceWorksapce) ;
          if(session2.getRootNode().hasNode(referenceNodeName)) {
            Node referenceNode = session2.getRootNode().getNode(referenceNodeName);
            Value value2add = session2.getValueFactory().createValue(referenceNode);
            node.setProperty(propertyName, new Value[] {value2add});          
          }else {
            node.setProperty(propertyName, session2.getValueFactory().createValue((String)value));
          }
        } else {
          if(session.getRootNode().hasNode((String) value)) {
            Node referenceNode = session.getRootNode().getNode(referenceNodeName);
            Value value2add = session.getValueFactory().createValue(referenceNode);
            node.setProperty(propertyName, new Value[] {value2add});
          } else {
            node.setProperty(propertyName, session.getValueFactory().createValue((String)value));
          }
        }
      } else if(value instanceof String[]) {
        String[] values = (String[]) value;        
        String referenceWorksapce = null;
        String referenceNodeName = null ;
        Session session = node.getSession();
        String repositoty = ((ManageableRepository)session.getRepository()).getConfiguration().getName();
        List<Value> list = new ArrayList<Value>() ;        
        for(String v: values) {          
          Value valueObj = null ;
          if(v.indexOf(":/")>0) {
            referenceWorksapce = v.split(":/")[0];
            referenceNodeName = v.split(":/")[1] ;
            Session session2 = jcrService.getRepository(repositoty).getSystemSession(referenceWorksapce) ;            
            if(session2.getRootNode().hasNode(referenceNodeName)) {
              Node referenceNode = session2.getRootNode().getNode(referenceNodeName) ;
              valueObj = session2.getValueFactory().createValue(referenceNode) ;              
            }else {             
              valueObj = session2.getValueFactory().createValue(v) ;
            }            
          }else {            
            if(session.getRootNode().hasNode(v)) {
              Node referenceNode = session.getRootNode().getNode(v) ;
              valueObj = session.getValueFactory().createValue(referenceNode) ;
            }else {
              valueObj = session.getValueFactory().createValue(v) ;
            }
          }
          list.add(valueObj) ;          
        }
        node.setProperty(propertyName,list.toArray(new Value[list.size()])) ;
      }       
      break ;
    default:
      throw new RepositoryException("unknown type " + requiredtype);
    }
  }

  private String extractNodeName(Set keys) {
    for (Iterator iter = keys.iterator(); iter.hasNext();) {
      String key = (String) iter.next();
      if (key.endsWith(NODE)) return key;
    }
    return null;
  }

  public void moveNode(String nodePath, String srcWorkspace, String destWorkspace,
      String destPath, String repository) {
    Session srcSession = null ;
    Session destSession = null ;
    if(!srcWorkspace.equals(destWorkspace)){      
      try {        
        srcSession = jcrService.getRepository(repository).getSystemSession(srcWorkspace);
        destSession = jcrService.getRepository(repository).getSystemSession(destWorkspace);
        Workspace workspace = destSession.getWorkspace();
        Node srcNode = (Node) srcSession.getItem(nodePath);
        try {
          destSession.getItem(destPath);        
        } catch (PathNotFoundException e) {
          createNode(destSession, destPath);                   
        }
        workspace.clone(srcWorkspace, nodePath, destPath, true);
        //Remove src node
        srcNode.remove();
        srcSession.save();
        destSession.save() ;
        srcSession.logout();
        destSession.logout();
      } catch (Exception e) {
        if(srcSession != null) srcSession.logout();
        if(destSession !=null) destSession.logout();
        //e.printStackTrace();
      }
    }else {
      Session session = null ;
      try{
        session = jcrService.getRepository(repository).getSystemSession(srcWorkspace);
        Workspace workspace = session.getWorkspace();
        try {
          session.getItem(destPath);        
        } catch (PathNotFoundException e) {
          createNode(session, destPath);
          session.refresh(false) ;
        }        
        workspace.move(nodePath, destPath);
        session.logout();
      }catch(Exception e){
        if(session !=null && session.isLive()) session.logout(); 
        //e.printStackTrace() ;
      }
    }
  }

  private void createNode(Session session, String uri) throws RepositoryException {
    String[] splittedName = StringUtils.split(uri, "/"); 
    Node rootNode = session.getRootNode();
    for (int i = 0; i < splittedName.length - 1; i++) {
      try {
        rootNode.getNode(splittedName[i]);
      } catch (PathNotFoundException exc) {        
        rootNode.addNode(splittedName[i], "nt:unstructured"); 
        rootNode.save() ;
      }
      rootNode = rootNode.getNode(splittedName[i]) ;
    }
    session.save() ;    
  }

  private List<JcrInputProperty> extractNodeInputs(final Map<String, JcrInputProperty> map,int itemLevel) {    
    List<JcrInputProperty> list = new ArrayList<JcrInputProperty>() ;
    for(Iterator<String> iterator = map.keySet().iterator();iterator.hasNext();) {
      String jcrPath = iterator.next();
      if(itemLevel == StringUtils.countMatches(jcrPath, "/")) {
        JcrInputProperty input = map.get(jcrPath) ;
        if(input.getType() == JcrInputProperty.NODE) {
          list.add(input) ;
        }
      }
    }
    return list ;
  }

  private boolean canAddNode(final NodeDefinition nodeDef,final NodeType nodeType) {
    for(NodeType type: nodeDef.getRequiredPrimaryTypes()) {
      if(nodeType.isNodeType(type.getName())) return true ;
    }
    return false ;
  }

  private Node doAddNode(final Node currentNode,String nodeName,String nodeType, String[] mixinTypes) throws Exception {
    Node childNode = null;    
    try {
      childNode = currentNode.getNode(nodeName) ;
    } catch(PathNotFoundException pe) {
      childNode = currentNode.addNode(nodeName, nodeType);
    }
    if (mixinTypes != null && mixinTypes.length > 0) {
      for (String mixinName : mixinTypes) {
        if (childNode.isNodeType(mixinName))
          childNode.addMixin(mixinName);
      }
    }          
    return childNode ;     
  }
}