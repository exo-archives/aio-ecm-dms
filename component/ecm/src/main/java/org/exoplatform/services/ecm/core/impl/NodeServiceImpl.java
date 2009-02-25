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
package org.exoplatform.services.ecm.core.impl;

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
import org.exoplatform.services.ecm.core.JcrItemInput;
import org.exoplatform.services.ecm.core.NodeService;
import org.exoplatform.services.idgenerator.IDGeneratorService;
import org.exoplatform.services.jcr.RepositoryService;
import org.exoplatform.services.jcr.core.ManageableRepository;
import org.exoplatform.services.jcr.core.nodetype.ExtendedItemDefinition;
import org.exoplatform.services.jcr.ext.common.SessionProvider;

/**
 * Created by The eXo Platform SAS Author :
 *  Hao Le Minh hao.le@exoplatform.com
 * Apr 28, 2008
 */
public class NodeServiceImpl implements NodeService {

  final static private String EXO_DATETIME_      = "exo:datetime".intern();
  final static private String EXO_DATE_MODIFIED_ = "exo:dateModified".intern();

  private RepositoryService   repositoryService;
  private IDGeneratorService  idGeneratorService;

  public NodeServiceImpl(RepositoryService repoService, IDGeneratorService idGenerateService)
  throws Exception {
    repositoryService = repoService;
    idGeneratorService = idGenerateService;
  }

  @SuppressWarnings("unused")
  public Node addNode(Node parent, String nodetype, Map<String, JcrItemInput> maps, boolean isNew)
  throws Exception {    
    Set<String> keys = maps.keySet();
    String nodePath = extractNodeName(keys);
    JcrItemInput jcrInputPro = (JcrItemInput) maps.get(nodePath);
    String nodeName = (String) jcrInputPro.getValue();
    if (nodeName == null || nodeName.length() == 0) {
      nodeName = idGeneratorService.generateStringID(nodetype);
    }
    String primaryType = jcrInputPro.getPrimaryNodeType();
    if (primaryType == null || primaryType.length() == 0) primaryType = nodetype;
    Session session = parent.getSession();
    NodeTypeManager nodeTypeManager = session.getWorkspace().getNodeTypeManager();
    NodeType nodeType = nodeTypeManager.getNodeType(primaryType);
    Node currentNode = null;
    String[] mixinNodeTypes = jcrInputPro.getMixinNodeTypes();
    if (isNew) {
      currentNode = parent.addNode(nodeName, primaryType);
      if (mixinNodeTypes != null) {
        for (String typeNode : mixinNodeTypes) {
          if (!currentNode.isNodeType(typeNode)) {
            currentNode.addMixin(typeNode);
          }
          NodeType mixinType = nodeTypeManager.getNodeType(typeNode);
          createNodeRecusively(NODE, currentNode, nodeType, maps);
        }
      }
      createNodeRecusively(NODE, currentNode, nodeType, maps);
    } else {
      currentNode = parent.getNode(nodeName);
      updateNodeRecusively(NODE, currentNode, nodeType, maps);
      if (currentNode.isNodeType(EXO_DATETIME_)) {
        currentNode.setProperty(EXO_DATE_MODIFIED_, new GregorianCalendar());
      }
    }
    return currentNode;
  }

  public Node addNode(String repository, String workspace, String parentPath, String nodetype,
      Map<String, JcrItemInput> jcrProperties, boolean isNew, SessionProvider sessionProvider)
  throws Exception {
    ManageableRepository manageableRepository = repositoryService.getRepository(repository);
    Session session = sessionProvider.getSession(workspace, manageableRepository);
    Node nodeHome = (Node) session.getItem(parentPath);
    return addNode(nodeHome, nodetype, jcrProperties, isNew);
  }

  public Node copyNode(String repository, String srcWorkspace, String srcPath, String desWorkspace,
      String destPath, SessionProvider sessionProvider) throws Exception {
    ManageableRepository manageableRepository = repositoryService.getRepository(repository);
    Session srcSession = sessionProvider.getSession(srcWorkspace, manageableRepository);
    String newNodePath = destPath + "/" + srcPath.substring(srcPath.lastIndexOf("/") + 1);
    if (srcWorkspace.equals(desWorkspace)) {
      srcSession = sessionProvider.getSession(srcWorkspace, manageableRepository);
      srcSession.getWorkspace().copy(srcPath, newNodePath);
      srcSession.save();
      return (Node) srcSession.getItem(newNodePath);
    }
    srcSession = sessionProvider.getSession(srcWorkspace, manageableRepository);
    Session destSession = sessionProvider.getSession(desWorkspace, manageableRepository);
    Workspace workspace = destSession.getWorkspace();
    destSession.getItem(destPath);
    workspace.clone(srcWorkspace, srcPath, destPath, true);
    srcSession.save();
    return (Node) destSession.getItem(destPath);
  }

  public Node moveNode(String repository, String srcWorkspace, String srcPath, String desWorkspace,
      String destPath, SessionProvider sessionProvider) throws Exception {
    String nodeReturnPath = null;
    ManageableRepository manageableRepository = repositoryService.getRepository(repository);
    Session srcSession = sessionProvider.getSession(srcWorkspace, manageableRepository);
    Session desSession = sessionProvider.getSession(desWorkspace, manageableRepository);
    nodeReturnPath = destPath + "/" + srcPath.substring(srcPath.lastIndexOf("/") + 1);
    srcSession.getWorkspace().move(srcPath, nodeReturnPath);
    srcSession.save();
    return (Node) desSession.getItem(nodeReturnPath);
  }

  public void setProperty(Node node, String propertyName, Object value, int requiredtype,
      boolean isMultiple) throws Exception {
    switch (requiredtype) {
    case PropertyType.BINARY:
      if (value == null) {
        node.setProperty(propertyName, "");
      } else if (value instanceof byte[]) {
        node.setProperty(propertyName, new ByteArrayInputStream((byte[]) value));
      } else if (value instanceof String) {
        node.setProperty(propertyName, new ByteArrayInputStream(((String) value).getBytes()));
      } else if (value instanceof String[]) {
        node.setProperty(propertyName, new ByteArrayInputStream((((String[]) value)).toString()
            .getBytes()));
      }
      break;
    case PropertyType.DOUBLE:
      if (value == null || "".equals(value)) {
        node.setProperty(propertyName, 0);
      } else if (value instanceof String) {
        node.setProperty(propertyName, new Double((String) value).doubleValue());
      } else if (value instanceof String[]) {
        node.setProperty(propertyName, (String[]) value);
      }
      break;
    case PropertyType.LONG:
      if (value == null || "".equals(value)) {
        node.setProperty(propertyName, 0);
      } else if (value instanceof String) {
        node.setProperty(propertyName, new Long((String) value).longValue());
      } else if (value instanceof String[]) {
        node.setProperty(propertyName, (String[]) value);
      }
      break;
    case PropertyType.STRING:
      if (value == null) {
        node.setProperty(propertyName, "");
      } else {
        if (isMultiple) {
          if (value instanceof String) {
            node.setProperty(propertyName, new String[] { (String) value });
          } else if (value instanceof String[]) {
            node.setProperty(propertyName, (String[]) value);
          } else {
            node.setProperty(propertyName, (String) value);
          }
        } else {
          node.setProperty(propertyName, value.toString()) ;
        }
      }
      break;
    case PropertyType.REFERENCE:
      if (value == null)
        throw new RepositoryException("null value for a reference " + requiredtype);
      if (value instanceof Value[]) {
        node.setProperty(propertyName, (Value[]) value);
      } else if (value instanceof String) {
        String referenceWorksapce = null;
        String referenceNodeName = null;
        Session session = node.getSession();
        String repositoty = ((ManageableRepository) session.getRepository()).getConfiguration()
        .getName();
        if (((String) value).indexOf(":/") > -1) {
          referenceWorksapce = ((String) value).split(":/")[0];
          referenceNodeName = ((String) value).split(":/")[1];
          Session session2 = repositoryService.getRepository(repositoty).getSystemSession(
              referenceWorksapce);
          if (session2.getRootNode().hasNode(referenceNodeName)) {
            Node referenceNode = session2.getRootNode().getNode(referenceNodeName);
            Value value2add = session2.getValueFactory().createValue(referenceNode);
            node.setProperty(propertyName, new Value[] { value2add });
          } else {
            node.setProperty(propertyName, session2.getValueFactory().createValue((String) value));
          }
        } else {
          if (session.getRootNode().hasNode((String) value)) {
            Node referenceNode = session.getRootNode().getNode(referenceNodeName);
            Value value2add = session.getValueFactory().createValue(referenceNode);
            node.setProperty(propertyName, new Value[] { value2add });
          } else {
            node.setProperty(propertyName, session.getValueFactory().createValue((String) value));
          }
        }
      } else if (value instanceof String[]) {
        String[] values = (String[]) value;
        String referenceWorksapce = null;
        String referenceNodeName = null;
        Session session = node.getSession();
        String repositoty = ((ManageableRepository) session.getRepository()).getConfiguration()
        .getName();
        List<Value> list = new ArrayList<Value>();
        for (String v : values) {
          Value valueObj = null;
          if (v.indexOf(":/") > 0) {
            referenceWorksapce = v.split(":/")[0];
            referenceNodeName = v.split(":/")[1];
            Session session2 = repositoryService.getRepository(repositoty).getSystemSession(
                referenceWorksapce);
            if (session2.getRootNode().hasNode(referenceNodeName)) {
              Node referenceNode = session2.getRootNode().getNode(referenceNodeName);
              valueObj = session2.getValueFactory().createValue(referenceNode);
            } else {
              valueObj = session2.getValueFactory().createValue(v);
            }
          } else {
            if (session.getRootNode().hasNode(v)) {
              Node referenceNode = session.getRootNode().getNode(v);
              valueObj = session.getValueFactory().createValue(referenceNode);
            } else {
              valueObj = session.getValueFactory().createValue(v);
            }
          }
          list.add(valueObj);
        }
        node.setProperty(propertyName, list.toArray(new Value[list.size()]));
      }
      break;
    case PropertyType.DATE:
      if (value == null) {
        node.setProperty(propertyName, new GregorianCalendar());
      } else {
        if (isMultiple) {
          Session session = node.getSession();
          if (value instanceof String) {
            Value value2add = session.getValueFactory().createValue(ISO8601.parse((String) value));
            node.setProperty(propertyName, new Value[] { value2add });
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
            node.setProperty(propertyName, ISO8601.parse((String) value));
          } else if (value instanceof GregorianCalendar)
            node.setProperty(propertyName, (GregorianCalendar) value);
        }
      }
      break;
    case PropertyType.BOOLEAN:
      if (value == null) {
        node.setProperty(propertyName, false);
      } else if (value instanceof String) {
        node.setProperty(propertyName, new Boolean((String) value).booleanValue());
      } else if (value instanceof String[]) {
        node.setProperty(propertyName, (String[]) value);
      }
      break;
    default:
      throw new RepositoryException("unknown type " + requiredtype);
    }

  }

  private void createNodeRecusively(String itemPath, Node curentNode, NodeType nodeType,
      Map<String, JcrItemInput> jcrInputPro) throws Exception {
    processNodeRecusively(true, itemPath, curentNode, nodeType, jcrInputPro);
  }

  private String extractNodeName(final Set<String> keys) {
    for (String key : keys){
      if (key.endsWith(NODE)) return key;
    }
    return null;
  }

  private void processEditNodeProperty(boolean create, String path, Node curentNode,
      final NodeType curentNodeTye, final Map<String, JcrItemInput> jcrInputPro) throws Exception {
    if (create) {      
      for (PropertyDefinition proDef : curentNodeTye.getPropertyDefinitions()) {
        if (!proDef.isAutoCreated() && !proDef.isProtected()) {
          String proName = proDef.getName();          
          String jcrPath = path + "/" + proName;
          JcrItemInput propertyInput = (JcrItemInput) jcrInputPro.get(jcrPath);
          if(propertyInput != null && propertyInput.getValue() != null) {
            setProperty(curentNode, proName, propertyInput.getValue(), proDef.getRequiredType(), proDef.isMultiple());
            //Remove propery input
            jcrInputPro.remove(jcrPath) ;
          }          
        }
      }
    }
  }

  private void processNodeRecusively(boolean isCreate, String itemPath, Node currentNode,
      NodeType currentNodeType, final Map<String, JcrItemInput> jcrItemInputs) throws Exception {    
    if (isCreate) {
      processEditNodeProperty(true, itemPath, currentNode, currentNodeType, jcrItemInputs);
    } else {
      for (PropertyIterator iterator = currentNode.getProperties(); iterator.hasNext();) {
        Property property = iterator.nextProperty();        
        String propertyPath = itemPath + "/" + property.getName();
        JcrItemInput propertyInput = (JcrItemInput) jcrItemInputs.get(propertyPath);        
        PropertyDefinition proDef = property.getDefinition();
        if(!proDef.isProtected() && propertyInput != null && propertyInput.getValue() != null) {
          setProperty(currentNode, proDef.getName(), propertyInput.getValue(), proDef.getRequiredType(), proDef.isMultiple());
          jcrItemInputs.remove(propertyPath) ;
        }                                        
      }
      processEditNodeProperty(false, itemPath, currentNode, currentNodeType, jcrItemInputs);
    }
    int itemLevel = StringUtils.countMatches(itemPath, "/") ;            
    List<JcrItemInput>childNodeInputs = extractNodeInputs(jcrItemInputs, itemLevel + 1) ;    
    NodeTypeManager nodeTypeManger = currentNode.getSession().getWorkspace().getNodeTypeManager();
    List<Object> childs = new ArrayList<Object>();
    if (isCreate) {            
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
        for(JcrItemInput input:childNodeInputs) {
          String primaryNodeType = input.getPrimaryNodeType() ;
          NodeType nodeType = nodeTypeManger.getNodeType(primaryNodeType) ;
          if(!canAddNode(nodeDef, nodeType)) continue ;
          String[] mixinTypes = input.getMixinNodeTypes() ;          
          Node childNode = doAddNode(currentNode, (String)input.getValue(), nodeType.getName(), mixinTypes) ;
          String childItemPath = itemPath + "/" + childNode.getName();
          processNodeRecusively(isCreate, childItemPath, childNode, childNode.getPrimaryNodeType(), jcrItemInputs);          
        }
      }else {               
        String childNodeName = null;
        if (obj instanceof Node) {          
          childNodeName = ((Node) obj).getName();
        } else {
          childNodeName = ((NodeDefinition) obj).getName();
        }
        String newItemPath = itemPath + "/" + childNodeName;
        JcrItemInput jcrInputVariable = (JcrItemInput) jcrItemInputs.get(newItemPath);
        if(jcrInputVariable == null) continue ;
        String nodeTypeName = jcrInputVariable.getPrimaryNodeType();
        String[] mixinTypes = jcrInputVariable.getMixinNodeTypes();          
        NodeType nodeType = null;
        if(obj instanceof Node) {
          nodeType = ((Node) obj).getPrimaryNodeType();
        } else if (nodeTypeName == null || nodeTypeName.length() == 0) {
          nodeType = nodeDef.getRequiredPrimaryTypes()[0];
        } else {
          nodeType = nodeTypeManger.getNodeType(nodeTypeName);
        }
        Node childNode = doAddNode(currentNode, childNodeName, nodeType.getName(), mixinTypes) ;        
        processNodeRecusively(isCreate, newItemPath, childNode, childNode.getPrimaryNodeType(), jcrItemInputs);        
      }      
    }
  }

  private void updateNodeRecusively(String path, Node curentNode, NodeType curentNodeType,
      Map<String, JcrItemInput> jcrInputPro) throws Exception {
    processNodeRecusively(false, path, curentNode, curentNodeType, jcrInputPro);
  }

  private List<JcrItemInput> extractNodeInputs(final Map<String, JcrItemInput> map,int itemLevel) {    
    List<JcrItemInput> list = new ArrayList<JcrItemInput>() ;
    for(Iterator<String> iterator = map.keySet().iterator();iterator.hasNext();) {
      String jcrPath = iterator.next();
      if(itemLevel == StringUtils.countMatches(jcrPath, "/")) {
        JcrItemInput input = map.get(jcrPath) ;
        if(input.isNodeInput()) {
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
    if (mixinTypes.length > 0) {
      for (String mixinName : mixinTypes) {
        if (childNode.isNodeType(mixinName))
          childNode.addMixin(mixinName);
      }
    }          
    return childNode ;     
  }
}
