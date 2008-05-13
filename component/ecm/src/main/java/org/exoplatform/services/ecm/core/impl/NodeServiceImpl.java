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

import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import javax.jcr.Node;
import javax.jcr.PathNotFoundException;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.nodetype.NodeType;
import javax.jcr.nodetype.NodeTypeManager;
import javax.jcr.nodetype.PropertyDefinition;

import org.apache.commons.lang.StringUtils;
import org.exoplatform.services.ecm.core.JcrItemInput;
import org.exoplatform.services.ecm.core.NodeService;
import org.exoplatform.services.idgenerator.IDGeneratorService;
import org.exoplatform.services.jcr.RepositoryService;
import org.exoplatform.services.jcr.core.ManageableRepository;
import org.exoplatform.services.jcr.ext.common.SessionProvider;

/**
 * Created by The eXo Platform SAS
 * Author : Hoa.Pham
 *          hoa.pham@exoplatform.com
 * Apr 28, 2008  
 */
public class NodeServiceImpl implements NodeService {
  
  private static final String EXO_DATETIME_ = "exo:datetime";
  private static final String EXO_DATE_MODIFIED_ = "exo:dateModified";  
  private static final String FOLDER_NT_UNSTRUCTURED_ = "nt:unstructured";
  private RepositoryService repositoryService_ ;
  private IDGeneratorService idGeneratorService_ ;
  
  public NodeServiceImpl(RepositoryService repoService, IDGeneratorService idGenerateService) throws Exception{
     repositoryService_ = repoService;
     idGeneratorService_ = idGenerateService;     
  }
  
  @SuppressWarnings("unused")
  public Node addNode(Node parent, String nodetype, Map<String, JcrItemInput> maps,
      boolean isNew) throws Exception {
    Set<String> keys = maps.keySet();
    String nodePath = extractNodeName(keys);
    JcrItemInput jcrInputPro = (JcrItemInput) maps.get(nodePath);
    String nodeName = (String) jcrInputPro.getValue();
    if (nodeName == null || nodeName.length() == 0){
      nodeName = idGeneratorService_.generateStringID(nodetype);
    }  
    String primaryType = jcrInputPro.getPrimaryNodeType();
    if (primaryType == null || primaryType.length() == 0){
      primaryType = nodetype;
    }  
    Session session = parent.getSession();
    NodeTypeManager nodeTypeManager = session.getWorkspace().getNodeTypeManager();
    NodeType nodeType = nodeTypeManager.getNodeType(primaryType);
    Node currentNode = null;
    String[] mixinNodeTypes = jcrInputPro.getMixinNodeTypes();
    if (isNew) {
      currentNode = parent.addNode(nodeName, primaryType);
      if (mixinNodeTypes != null) {
        for (String typeNode : mixinNodeTypes) {
          if (currentNode.isNodeType(typeNode)){
            currentNode.addMixin(typeNode);
          } 
          NodeType mixinType = nodeTypeManager.getNodeType(typeNode);
        }
      }
    }else{
      //TO DO update
    }
    return currentNode;
  }
  public Node addNode(String repository, String workspace, String parentPath, String nodetype,
      Map<String, JcrItemInput> jcrProperties, boolean isNew, SessionProvider sessionProvider)
      throws Exception {
    ManageableRepository manageableRepository = repositoryService_.getRepository(repository);
    Session session = sessionProvider.getSession(workspace, manageableRepository);
    Node nodeHome = (Node)session.getItem(parentPath);
    Node currentNode = addNode(nodeHome, nodetype, jcrProperties, isNew);
    session.save();    
    return currentNode;
  }
  //Copy node task
  public Node copyNode(String repository, String srcWorkspace, String srcPath,
      String destWorkspace, String destPath, SessionProvider sessionProvider) throws Exception {
    /*
    Session srcSession = null;
    Session desSession = null;
    if(srcWorkspace.equals(destWorkspace)){
      try{
        srcSession = repositoryService_.getRepository(repository).getSystemSession(srcWorkspace);
        desSession = repositoryService_.getRepository(repository).getSystemSession(destWorkspace);
        Workspace workspace = desSession.getWorkspace();
        Node srcNode = (Node)srcSession.getItem(srcPath);
        try{
          desSession.getItem(destPath);
        }catch(PathNotFoundException pathEx){
          createNode(desSession, srcPath);
        }
        workspace.copy(srcWorkspace, srcPath, destPath);
        
        System.out.println("\n\n\n-------Copying...........\n\n\n\n");
        
        srcSession.save();
        srcSession.logout();
        desSession.save();
        desSession.logout();
      }catch(Exception ex){
        if(srcSession != null) srcSession.logout();
        if(desSession != null) desSession.logout();
      }
    }else{
      Session session = null;
      try{
        session = repositoryService_.getRepository(srcPath).getSystemSession(srcWorkspace);
        Workspace workspace = session.getWorkspace();
        try{
          session.getItem(destPath);
        }catch(PathNotFoundException pathEx){
          createNode(session, destPath);
          session.refresh(false);
        }
        workspace.copy(srcPath, destPath);
        
        System.out.println("\n\n\n----ELSE---Copying...........\n\n\n\n");
        
        session.save();
        session.logout();
      }catch(Exception ex){
        if(session != null) session.logout(); 
      }
    }
    System.out.println("\n\n\n----This is copyNode(....) method ----: Building....\n\n\n");
    */
    return null;
  }  
  //Move node task
  public Node moveNode(String repository, String srcWorkspace, String srcPath,
      String destWorkspace, String destPath, SessionProvider sessionProvider) throws Exception {
    // TODO Auto-generated method stub
   /*
    Session srcSession = null;
    Session desSession = null;
    if(srcWorkspace.equals(destWorkspace)){
      try{
        srcSession = repositoryService_.getRepository(repository).getSystemSession(srcWorkspace);
        desSession = repositoryService_.getRepository(repository).getSystemSession(destWorkspace);
        Workspace workspace = desSession.getWorkspace();
        Node srcNode = (Node)srcSession.getItem(srcPath);
        try{
          desSession.getItem(destPath);
        }catch(PathNotFoundException pathEx){
          createNode(desSession, srcPath);
        }
        workspace.clone(srcWorkspace, srcPath, destPath, true);
        srcNode.remove();
        
        System.out.println("\n\n\n-----Removing...........\n\n\n\n");
        
        srcSession.save();
        srcSession.logout();
        desSession.save();
        desSession.logout();
      }catch(Exception ex){
        if(srcSession != null) srcSession.logout();
        if(desSession != null) desSession.logout();
      }
    }else{
      Session session = null;
      try{
        session = repositoryService_.getRepository(srcPath).getSystemSession(srcWorkspace);
        Workspace workspace = session.getWorkspace();
        try{
          session.getItem(destPath);
        }catch(PathNotFoundException pathEx){
          createNode(session, destPath);
          session.refresh(false);
        }
        workspace.move(srcPath, destPath);
        
        System.out.println("\n\n\n----ELSE---Copying...........\n\n\n\n");
        
        session.save();
        session.logout();
      }catch(Exception ex){
        if(session != null) session.logout(); 
      }
    }
    System.out.println("\n\n\n----This is moveNode(....) method ----: Building....\n\n\n");
    */
    return null;
  }
  //Set properties task
  public void setProperty(Node node, String propertyName, Object value, int requiredtype,
      boolean isMultiple) throws Exception{
   /* switch(requiredtype){
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
      case PropertyType.DOUBLE:
        if (value == null || "".equals(value))
          node.setProperty(propertyName, 0);
        else if (value instanceof String)
          node.setProperty(propertyName, new Double((String) value).doubleValue());
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
            Session session2 = repositoryService_.getRepository(repositoty).getSystemSession(referenceWorksapce) ;
            if(session2.getRootNode().hasNode(referenceNodeName)) {
              Node referenceNode = session2.getRootNode().getNode(referenceNodeName);
              Value value2add = session2.getValueFactory().createValue(referenceNode);
              node.setProperty(propertyName, new Value[] {value2add});          
            }else
              node.setProperty(propertyName, session2.getValueFactory().createValue((String)value));
          } else {
            if(session.getRootNode().hasNode((String) value)) {
              Node referenceNode = session.getRootNode().getNode(referenceNodeName);
              Value value2add = session.getValueFactory().createValue(referenceNode);
              node.setProperty(propertyName, new Value[] {value2add});
            } else
              node.setProperty(propertyName, session.getValueFactory().createValue((String)value));
          }
        } else if(value instanceof String[]) {
          String[] values = (String[]) value ;        
          String referenceWorksapce = null ;
          String referenceNodeName = null ;
          Session session = node.getSession();
          String repositoty = ((ManageableRepository)session.getRepository()).getConfiguration().getName();
          List<Value> list = new ArrayList<Value>() ;        
          for(String v: values) {          
            Value valueObj = null ;
            if(v.indexOf(":/")>0) {
              referenceWorksapce = v.split(":/")[0];
              referenceNodeName = v.split(":/")[1] ;
              Session session2 = repositoryService_.getRepository(repositoty).getSystemSession(referenceWorksapce) ;            
              if(session2.getRootNode().hasNode(referenceNodeName)) {
                Node referenceNode = session2.getRootNode().getNode(referenceNodeName) ;
                valueObj = session2.getValueFactory().createValue(referenceNode) ;              
              }else              
                valueObj = session2.getValueFactory().createValue(v) ;
           
            }else {            
              if(session.getRootNode().hasNode(v)) {
                Node referenceNode = session.getRootNode().getNode(v) ;
                valueObj = session.getValueFactory().createValue(referenceNode) ;
              }else
                valueObj = session.getValueFactory().createValue(v) ;
            }
            list.add(valueObj) ;          
          }
          node.setProperty(propertyName,list.toArray(new Value[list.size()])) ;
        }    
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
            } else if (value instanceof GregorianCalendar)
              node.setProperty(propertyName, (GregorianCalendar) value);
          }
        }      
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
        default:
          throw new RepositoryException("unknown type " + requiredtype);
    }
    */
  } 
  //Create node
  private void createNode(Session session, String uri) throws RepositoryException{
    String [] splitName = StringUtils.split(uri, "/");
    Node rootNode = session.getRootNode();
    for(int splIndex = 0; splIndex < splitName.length; splIndex++){
      try{
        rootNode.getNodes(splitName[splIndex]);
      }catch(PathNotFoundException pathEx){
        rootNode.addNode(splitName[splIndex], FOLDER_NT_UNSTRUCTURED_);
        rootNode.save();
      }
      rootNode = rootNode.addNode(splitName[splIndex]);
    }
    session.save();
  }
  //Create node task
  private void createNodeRecusively(String path, Node curentNode, 
      NodeType curentNodeType, Map<String, JcrItemInput> jcrInputPro) throws Exception{
      processNodeRecusively(true, path, curentNode, curentNodeType, jcrInputPro);
  }
  //Extract node task
  private String extractNodeName(Set keys){
    String key = null ;
    for(Iterator iter = keys.iterator(); iter.hasNext();){
      key = (String)iter.next() ;
      if(key.endsWith(NODE)) return key;
    }
    return null;
  }
  //Process edit properties
  private void processEditNodeProperty(boolean create, String path, 
      Node curentNode, NodeType curentNodeTye, Map<String,JcrItemInput> jcrInputPro) throws Exception{
     if(create || path.equals(NODE)){
       PropertyDefinition [] proDefin = curentNodeTye.getPropertyDefinitions();
       for(int proIndex = 0; proIndex < proDefin.length; proIndex++){
         PropertyDefinition proDefiDetail = proDefin[proIndex];
         if(!proDefiDetail.isAutoCreated() && !proDefiDetail.isProtected()){
           String proName = proDefiDetail.getName();
           int proReqType = proDefiDetail.getRequiredType();
           String curentPath = path + "/" + proName;
           JcrItemInput varJcrInput = (JcrItemInput)jcrInputPro.get(curentPath);
           Object value = null;
           if(varJcrInput == null) value = varJcrInput.getValue();
           if(varJcrInput != null && !proDefiDetail.isMandatory())
             setProperty(curentNode,proName, value,proReqType, proDefiDetail.isMultiple());
         }
       }
     }
  }
  //Create node process
  private void processNodeRecusively(boolean create, String path, 
      Node curentNode, NodeType curentNodeType, Map jcrInputPro) throws Exception{
   /*
    if(create)
      processEditNodeProperty(true, path, curentNode, curentNodeType, jcrInputPro);
    else{
      for(PropertyIterator proIterate = curentNode.getProperties(); proIterate.hasNext();){
        Property property = proIterate.nextProperty();
        PropertyDefinition proDefiDetail = property.getDefinition();
        String proName = property.getName();
        int proReqType = property.getType();
        String curentPath = path + "/" + proName;
        JcrItemInput varJcrInputPro = (JcrItemInput)jcrInputPro.get(curentPath);
        Object value = null;
        if(varJcrInputPro != null) value = varJcrInputPro.getValue();
        if(value != null && !proDefiDetail.isProtected())
          setProperty(curentNode, proName, value, proReqType, proDefiDetail.isMultiple());
      }
    } 
    processEditNodeProperty(false, path, curentNode, curentNodeType, jcrInputPro);
    
    List<Object> childs = new ArrayList<Object>();
    if(create){
      PropertyDefinition [] childNodeDefins = curentNodeType.getPropertyDefinitions();
      for(int childIndex = 0; childIndex < childNodeDefins.length; childIndex++)
        childs.add(childNodeDefins[childIndex]);
    }else{
      NodeIterator nodeIterate = curentNode.getNodes();
      while(nodeIterate.hasNext())
        childs.add(nodeIterate.next());
    }
    Iterator iterate = childs.iterator();
    while(iterate.hasNext()){
      Object obj = iterate.next();
      NodeDefinition nodeDefin;
      String nodeName = null;
      if(obj instanceof Node){
        nodeDefin = ((Node)obj).getDefinition();
        nodeName = ((Node)obj).getName();
      }else{
        nodeDefin = (NodeDefinition)obj;
        nodeName = ((NodeDefinition)obj).getName();
      }
      if(!nodeDefin.isAutoCreated() && !nodeDefin.isProtected()
          && ("*".equals(nodeDefin.getName()) && (obj instanceof NodeDefinition))){
        String curentPath = path + "/" + nodeName;
        JcrItemInput jcrInputVariable = (JcrItemInput)jcrInputPro.get(curentPath);
        String nodeTypeName = null;
        String mixinTypeName [] = null;
        if(jcrInputVariable != null){
          nodeTypeName = jcrInputVariable.getPrimaryNodeType();
          mixinTypeName = jcrInputVariable.getMixinNodeTypes();
          NodeTypeManager nodeTypeManger = curentNode.getSession().getWorkspace().getNodeTypeManager();
          NodeType nodeType = null;
          if(obj instanceof Node)
            nodeType = ((Node)obj).getPrimaryNodeType();            
          else if(nodeTypeName != null || "".equals(nodeTypeName))
            nodeType = nodeDefin.getRequiredPrimaryTypes()[0];
          else
            nodeType = nodeTypeManger.getNodeType(nodeTypeName);
          
          Node childNode = null;
          if(create){
            childNode = curentNode.addNode(nodeName, nodeType.getName());
            if(mixinTypeName.length > 0){
              for(String typeNode : mixinTypeName){
                if(childNode.isNodeType(typeNode))
                  childNode.addMixin(typeNode);
                  NodeType mixinType = nodeTypeManger.getNodeType(typeNode);
              }
            }
            String nodePath = path + "/" + nodeName;
            processNodeRecusively(create, nodePath, curentNode, curentNodeType, jcrInputPro);
          }else{
            try{
              childNode = curentNode.getNode(nodeName);
            }catch(PathNotFoundException pathEx){
              childNode = curentNode.addNode(nodeName, nodeType.getName());
              if(mixinTypeName.length > 0){
                for(String typeNode : mixinTypeName){
                  if(childNode.isNodeType(typeNode))
                    childNode.addMixin(typeNode);
                    NodeType mixinType = nodeTypeManger.getNodeType(typeNode);
                }
              }
              String nodePath = path + "/" + nodeName;
              processNodeRecusively(create, nodePath, curentNode, curentNodeType, jcrInputPro);
            }
          }
          String nodePath = path + "/" + nodeName;
          processNodeRecusively(create, nodePath, curentNode, curentNodeType, jcrInputPro);
        }
      }
    }
    */
  }
  //Edit node task
  private void updateNodeRecusively(String path, Node curentNode, 
      NodeType curentNodeType, Map jcrInputPro) throws Exception{
    processNodeRecusively(false, path, curentNode, curentNodeType, jcrInputPro);
  }
}
