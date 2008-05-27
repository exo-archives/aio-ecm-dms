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
package org.exoplatform.services.cms.publication.impl;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.ResourceBundle;

import javax.jcr.Node;
import javax.jcr.Property;
import javax.jcr.PropertyIterator;
import javax.jcr.Session;
import javax.jcr.Value;

import org.apache.commons.logging.Log;
import org.exoplatform.container.ExoContainer;
import org.exoplatform.container.ExoContainerContext;
import org.exoplatform.services.cms.publication.IncorrectStateUpdateLifecycleException;
import org.exoplatform.services.cms.publication.PublicationPlugin;
import org.exoplatform.services.cms.publication.PublicationService;
import org.exoplatform.services.jcr.access.PermissionType;
import org.exoplatform.services.jcr.access.SystemIdentity;
import org.exoplatform.services.jcr.core.ExtendedNode;
import org.exoplatform.services.jcr.core.ManageableRepository;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.resources.ResourceBundleService;
import org.exoplatform.webui.form.UIForm;

/**
 * Created by The eXo Platform SAS
 * Author : Romain Dénarié
 *          romain.denarie@exoplatform.com
 * 16 mai 08  
 */
public class StaticAndDirectPublicationPlugin extends PublicationPlugin {
  
  public static final String         ENROLLED              = "enrolled".intern();
  public static final String         NON_PUBLISHED         = "non published".intern();
  public static final String         PUBLISHED             = "published".intern();
  
  private static final String         PUBLICATION           = "exo:publication".intern();
  private static final String         LIFECYCLE_NAME        = "exo:lifecycleName".intern();
  private static final String         CURRENT_STATE         = "exo:currentState".intern();
  private static final String         HISTORY               = "exo:history".intern();
  
  private static final String         VISIBILITY         = "exo:visibility".intern();
  private static final String         VERSIONS_PUBLICATION_STATES            = "exo:versionsPublicationStates".intern();
  
  private static final String         PUBLIC         = "public".intern();
  private static final String         PRIVATE            = "private".intern();
  
  
  protected static Log log; 
  
  private final String localeFile = "locale.ecm.cms.publication.StaticAndDirectPublicationPlugin";
  
  public StaticAndDirectPublicationPlugin() {
    log = ExoLogger.getLogger("portal:StaticAndDirectPublicationPlugin");
  }
  
  @Override
  public void changeState(Node node, String newState, HashMap<String, String> context)
      throws IncorrectStateUpdateLifecycleException, Exception {
    log.info("Change node state to "+newState);
    
    Session session = node.getSession() ;
    ManageableRepository repository = (ManageableRepository)session.getRepository() ;
    Session systemSession = repository.getSystemSession(session.getWorkspace().getName()) ;
    
    if (newState.equals(ENROLLED)) {
      log.info("Set node to "+NON_PUBLISHED);
      node.setProperty(CURRENT_STATE, NON_PUBLISHED);
    } else if (newState.equals(PUBLISHED)) {
      String currentState = node.getProperty(CURRENT_STATE).getString();
      if (currentState.equals(NON_PUBLISHED)) {
        log.info("Node is non published");
        String nodeVersionUUID = context.get("nodeVersionUUID");
        String visibility = context.get("visibility");
        if (nodeVersionUUID == null || visibility == null) {
          log.error("nodeVersionUUID or visibility is null");
          throw new IncorrectStateUpdateLifecycleException ("StaticAndDirectPublicationPlugin.changeState : nodeVersionUUID or visibility is not present in context.");
        } else {
          log.info("nodeVersionUUID and visibility is not null");
          Value[] values = node.getProperty(VERSIONS_PUBLICATION_STATES).getValues();
          int i=0;
          while (i<values.length && !(values[i].getString().split(","))[0].equals(nodeVersionUUID)) {
            i++;
          }
          if (i==values.length || (values[i].getString().split(","))[1].equals(NON_PUBLISHED)) {
            log.info("Specified version not already published");
            //specified version to publish is not present in the tab exo:versionsPublicationStates
            //or is in NON_PUBLISHED state
            
            log.info("Set this version published");
            
            String newStringValue= nodeVersionUUID+","+PUBLISHED;
            Value value2add=systemSession.getValueFactory().createValue(newStringValue);
            values[i] = value2add;
            node.setProperty(VERSIONS_PUBLICATION_STATES,values) ;
            
            //set visibility
            log.info("Set the visibility");
            Value newValueVisibility=systemSession.getValueFactory().createValue(visibility);
            node.setProperty(VISIBILITY,newValueVisibility) ;
            
            //set permissions
            log.info("Set permissions in function of visibility");
            setVisibility(node, visibility);
            
            //set currentState to published
            node.setProperty(CURRENT_STATE,PUBLISHED);
            
            //add log
            log.info("Add log");
            ExoContainer container = ExoContainerContext.getCurrentContainer();   
            PublicationService publicationService = (PublicationService) container.getComponentInstanceOfType(PublicationService.class);
            String date =  new SimpleDateFormat("yyyyMMdd.HHmmss.SSS").format(new Date());
            String version = session.getNodeByUUID(nodeVersionUUID).getName();
            String log[] = {date,newState,session.getUserID(),"PublicationService.StaticAndDirectPublicationPlugin.nodePublished",version,visibility};
            publicationService.addLog(node, log);
            
          } else {
            //should not appear because if currentState of the node is NON_PUBLISHED
            //no version is PUBLISHED
            throw new IncorrectStateUpdateLifecycleException("StaticAndDirectPublicationPlugin.changeState : Node Version "+nodeVersionUUID+" is already published");
          }
        }
      } else {
        //currentState = PUBLISHED
        // means that one version is published
        log.info("Node is already published, user want to published another version.");
        String nodeVersionUUID = context.get("nodeVersionUUID");
        String visibility = context.get("visibility");
        if (nodeVersionUUID == null || visibility == null) {
          log.error("nodeVersionUUID or visibility is null");
          throw new IncorrectStateUpdateLifecycleException ("StaticAndDirectPublicationPlugin.changeState : nodeVersionUUID or visibility is not present in context.");
        } else {
          log.info("nodeVersionUUID and visibility is not null");
          Value[] values = node.getProperty(VERSIONS_PUBLICATION_STATES).getValues();
          
          //find which version is published
          int i=0;
          while (i<values.length && !(values[i].getString().split(","))[1].equals(PUBLISHED)) {
            i++;
          }
          if (i!=values.length) {
            //and unpublished it
            String publishedVersionUUID=values[i].getString().split(",")[0];
            log.info("Unpublished current published version");
            String newStringValue= publishedVersionUUID+","+NON_PUBLISHED;
            Value value2add=systemSession.getValueFactory().createValue(newStringValue);
            values[i] = value2add;
            node.setProperty(VERSIONS_PUBLICATION_STATES,values) ;
          } else {
            //error : currentSate = PUBLISHED but no version PUBLISHED
            //should not appear
            throw new IncorrectStateUpdateLifecycleException("StaticAndDirectPublicationPlugin.changeState : currentState is published but no version is published");
          }
          
          //publish the new version 
          i=0;
          while (i<values.length && !(values[i].getString().split(","))[0].equals(nodeVersionUUID)) {
            i++;
          }
          if (i==values.length || (values[i].getString().split(","))[1].equals(NON_PUBLISHED)) {
            log.info("Specified version not already published");
            //specified version to publish is not present in the tab exo:versionsPublicationStates
            //or is in NON_PUBLISHED state
            
            log.info("Set this version published");
            
            String newStringValue= nodeVersionUUID+","+PUBLISHED;
            Value value2add=systemSession.getValueFactory().createValue(newStringValue);
            values[i] = value2add;
            node.setProperty(VERSIONS_PUBLICATION_STATES,values) ;
            
            //set visibility
            log.info("Set the visibility");
            Value newValueVisibility=systemSession.getValueFactory().createValue(visibility);
            node.setProperty(VISIBILITY,newValueVisibility) ;
            
            //set permissions
            log.info("Set permissions in function of visibility");
            setVisibility(node, visibility);
            
            //set currentState to published
            node.setProperty(CURRENT_STATE,PUBLISHED);
            
            //add log
            log.info("Add log");
            ExoContainer container = ExoContainerContext.getCurrentContainer();   
            PublicationService publicationService = (PublicationService) container.getComponentInstanceOfType(PublicationService.class);
            String date =  new SimpleDateFormat("yyyyMMdd.HHmmss.SSS").format(new Date());
            String version = session.getNodeByUUID(nodeVersionUUID).getName();
            String log[] = {date,newState,session.getUserID(),"PublicationService.StaticAndDirectPublicationPlugin.nodePublished",version,visibility};
            publicationService.addLog(node, log);
            
          } else {
            //should not appear because if currentState of the node is NON_PUBLISHED
            //no version is PUBLISHED
            throw new IncorrectStateUpdateLifecycleException("StaticAndDirectPublicationPlugin.changeState : Node Version "+nodeVersionUUID+" is already published");
          }
        }
        
      }
    } else if (newState.equals(NON_PUBLISHED)) {
      String currentState = node.getProperty(CURRENT_STATE).getString();
      if (currentState.equals(NON_PUBLISHED)) {
        log.info("node already unpublished");
      } else if (currentState.equals(PUBLISHED)) {
        log.info("Node published, unpublish it");
        Value[] values = node.getProperty(VERSIONS_PUBLICATION_STATES).getValues();
        
        //find which version is published
        int i=0;
        while (i<values.length && !(values[i].getString().split(","))[1].equals(PUBLISHED)) {
          i++;
        }
        if (i!=values.length) {
          //and unpublished it
          String publishedVersionUUID=values[i].getString().split(",")[0];
          log.info("Unpublished current published version");
          String newStringValue= publishedVersionUUID+","+NON_PUBLISHED;
          Value value2add=systemSession.getValueFactory().createValue(newStringValue);
          values[i] = value2add;
          node.setProperty(VERSIONS_PUBLICATION_STATES,values) ;
          
          //set currentState to non published
          node.setProperty(CURRENT_STATE,NON_PUBLISHED);
          
          
        //add log
          log.info("Add log");
          ExoContainer container = ExoContainerContext.getCurrentContainer();   
          PublicationService publicationService = (PublicationService) container.getComponentInstanceOfType(PublicationService.class);
          String date =  new SimpleDateFormat("yyyyMMdd.HHmmss.SSS").format(new Date());
          String log[] = {date,newState,session.getUserID(),"PublicationService.StaticAndDirectPublicationPlugin.nodeUnpublished"};
          publicationService.addLog(node, log);
        } else {
          //error : currentSate = PUBLISHED but no version PUBLISHED
          //should not appear
          throw new IncorrectStateUpdateLifecycleException("StaticAndDirectPublicationPlugin.changeState : currentState is published but no version is published");
        }
        
      }
    } else {
      throw new IncorrectStateUpdateLifecycleException("Incorrect current State");
    }
  }

  @Override
  public String[] getPossibleStates() {
    String [] result = new String [3];
    result [0] = ENROLLED;
    result [1] = NON_PUBLISHED;
    result [2] = PUBLISHED;
    return result;
  }

  @Override
  public byte[] getStateImage(Node node) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public UIForm getStateUI(Node node) {
    // TODO Auto-generated method stub
    return null;
  }
 
  @Override
  public String getUserInfo(Node node, Locale locale) throws Exception {
    ExoContainer container = ExoContainerContext.getCurrentContainer();
    PublicationService publicationService = (PublicationService) container.getComponentInstanceOfType(PublicationService.class);
    ResourceBundleService resourceBundleService = (ResourceBundleService) container.getComponentInstanceOfType(ResourceBundleService.class);
    ResourceBundle resourceBundle= resourceBundleService.getResourceBundle(localeFile,locale);
    
    Session session = node.getSession() ;
    
    if (node.getProperty(CURRENT_STATE).getString().equals(ENROLLED) || node.getProperty(CURRENT_STATE).getString().equals(NON_PUBLISHED)) {
      return resourceBundle.getString("PublicationService.StaticAndDirectPublicationPlugin.nodeNotPublished");
    } else if (node.getProperty(CURRENT_STATE).getString().equals(PUBLISHED)) {
      Value[] values = node.getProperty(VERSIONS_PUBLICATION_STATES).getValues();
      int i=0;
      while (i<values.length && !(values[i].getString().split(","))[1].equals(PUBLISHED)) {
        i++;
      }
      if (i==values.length) {
        //should not append :
        //if current state = PUBLISHED , the tab VERSIONS_PUBLICATION_STATES must contain a string like this : "UUID,published"
        throw new Exception("StaticAndDirectPublicationPlugin.getUserInfo : currentState=published, but state published not present in history");
      } else {
        //find uuid of version node published
        String currentHistory[]=values[i].getString().split(",");
        String uuid=currentHistory[0];
        
        //get name and label of this version
        Node versionNode = session.getNodeByUUID(uuid);
        String name = versionNode.getName();
        
        Node labelNode = ((Node)versionNode.getParent()).getNode("jcr:versionLabels");
        //if this instruction do not find the jcr:versionLabels node
        //possibility to use ((Node)versionNode.getParent()).getNodes("jcr:versionLabels");
        //which return an iterator of child of this node which name matching pattern
        
        PropertyIterator propertyIterator = labelNode.getProperties();
        String label="";
        while (propertyIterator.hasNext()) {
          Property property = propertyIterator.nextProperty();
          if (property.getValue().getString().equals(uuid)) {
            label = property.getName();
          }
        }
        
        //get visibility from the current node
        String visibility = node.getProperty(VISIBILITY).getValue().getString() ;
        
        //create the correct string
        String result = "";
        if (label.equals("")) {
          String [] valuesLocale = {name};
          result += publicationService.getLocalizedAndSubstituteLog(locale, "PublicationService.StaticAndDirectPublicationPlugin.versionPublishedWithoutLabel", valuesLocale);
        } else {
          String [] valuesLocale = {name,label};
          result += publicationService.getLocalizedAndSubstituteLog(locale, "PublicationService.StaticAndDirectPublicationPlugin.versionPublishedWithLabel", valuesLocale);
        }
        
        if (visibility.equals("PUBLIC")) {
          result += resourceBundle.getString("PublicationService.StaticAndDirectPublicationPlugin.visibilityPublic");
        } else {
          result += resourceBundle.getString("PublicationService.StaticAndDirectPublicationPlugin.visibilityPrivate");
        }
        
        return result;
      }
      
      
    } else {
      //should not append : unknown state
      throw new Exception("StaticAndDirectPublicationPlugin.getUserInfo : Unknown state : "+node.getProperty(CURRENT_STATE).getString());
      
    }
      
  }
  
  public void setVisibility (Node node, String visibility) throws Exception {
    ExtendedNode extNode = (ExtendedNode)node;
    if (extNode.canAddMixin("exo:privilegeable")) extNode.addMixin("exo:privilegeable");
    
    if (visibility.equals(PUBLIC)) {
      //add any
      String[] arrayPersmission = {PermissionType.READ} ;
      extNode.setPermission(SystemIdentity.ANY, arrayPersmission);
    } else {
      extNode.removePermission(SystemIdentity.ANY);
    }
  }
 

}
