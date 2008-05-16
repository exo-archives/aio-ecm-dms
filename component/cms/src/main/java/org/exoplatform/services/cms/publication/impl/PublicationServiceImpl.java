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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;

import javax.jcr.Node;
import javax.jcr.Session;
import javax.jcr.Value;
import javax.jcr.nodetype.NoSuchNodeTypeException;

import org.exoplatform.container.ExoContainer;
import org.exoplatform.container.ExoContainerContext;
import org.exoplatform.services.cms.publication.AlreadyInPublicationLifecycleException;
import org.exoplatform.services.cms.publication.IncorrectStateUpdateLifecycleException;
import org.exoplatform.services.cms.publication.NotInPublicationLifecycleException;
import org.exoplatform.services.cms.publication.PublicationPlugin;
import org.exoplatform.services.cms.publication.PublicationPresentationService;
import org.exoplatform.services.cms.publication.PublicationService;
import org.exoplatform.services.resources.ResourceBundleService;
import org.exoplatform.services.resources.*;

import org.apache.commons.logging.Log;
import org.exoplatform.services.jcr.core.ManageableRepository;
import org.exoplatform.services.log.ExoLogger;

/**
 * Created by The eXo Platform SAS
 * Author : Romain Dénarié
 *          romain.denarie@exoplatform.com
 * 7 mai 08  
 */ 
public class PublicationServiceImpl implements PublicationService {

  private static final String         PUBLICATION           = "exo:publication".intern();
  private static final String         LIFECYCLE_NAME        = "exo:lyfecycleName".intern();
  private static final String         CURRENT_STATE         = "exo:currentState".intern();
  private static final String         HISTORY               = "exo:history".intern();
  
  protected static Log log;  
  private PublicationPresentationService publicationPresentationService;
  
  Map<String, PublicationPlugin> publicationPlugins_;
  
  public PublicationServiceImpl () {
    log = ExoLogger.getLogger("portal:PublicationServiceImpl");

    log.info("#####################################");
    log.info("# PublicationService initialization #");
    log.info("#####################################\n");
    
    ExoContainer container = ExoContainerContext.getCurrentContainer();    
    publicationPresentationService = (PublicationPresentationService) container.getComponentInstanceOfType(PublicationPresentationService.class);
    
    publicationPlugins_ = new HashMap<String, PublicationPlugin>();
    
    log.info("#####################################");    
    log.info("#  PublicationService initialized   #");
    log.info("#####################################\n");
  }
  
  /* (non-Javadoc)
   * @see org.exoplatform.services.cms.publication.PublicationService#addLog(javax.jcr.Node, java.lang.String[])
   */
  public void addLog(Node node, String[] args) throws NotInPublicationLifecycleException, Exception {
    log.info("############");
    log.info("#  addLog  #");
    log.info("############\n");
    
    Session session = node.getSession() ;
    ManageableRepository repository = (ManageableRepository)session.getRepository() ;
    Session systemSession = repository.getSystemSession(session.getWorkspace().getName()) ;
    
    if (!isNodeEnrolledInLifecycle(node)) {
      throw new NotInPublicationLifecycleException();
    } else {
      List<Value> newValues = new ArrayList<Value>();
      Value[] values = node.getProperty(HISTORY).getValues();
      newValues.addAll(Arrays.<Value>asList(values)) ;
      String string2add = "";
      for (int i=0; i<args.length;i++) {
        if (i==0) {
          string2add += args[i];
        } else {
          string2add += ","+args[i];
        }
      }
      Value value2add=systemSession.getValueFactory().createValue(string2add);
      newValues.add(value2add);
      node.setProperty(HISTORY,newValues.toArray(new Value[newValues.size()])) ; 
    }
  }

  /* (non-Javadoc)
   * @see org.exoplatform.services.cms.publication.PublicationService#addPublicationPlugin(org.exoplatform.services.cms.publication.PublicationPlugin)
   */
  public void addPublicationPlugin(PublicationPlugin p) {
    log.info("##########################");
    log.info("#  addPublicationPlugin  #");
    log.info("##########################\n");
    
    this.publicationPlugins_.put(p.getLifecycleName(),p);
    publicationPresentationService.addPublicationPlugin(p);
  }

  /* (non-Javadoc)
   * @see org.exoplatform.services.cms.publication.PublicationService#changeState(javax.jcr.Node, java.lang.String, java.util.HashMap)
   */
  public void changeState(Node node, String newState, HashMap<String, String> context)
      throws NotInPublicationLifecycleException, IncorrectStateUpdateLifecycleException, Exception {
    log.info("#################");
    log.info("#  changeState  #");
    log.info("#################\n");
    
    if (!isNodeEnrolledInLifecycle(node)) {
      throw new NotInPublicationLifecycleException();
    } else {
      String lifecycleName=getNodeLifecycleName(node);
      PublicationPlugin nodePlugin = this.publicationPlugins_.get(lifecycleName);
      nodePlugin.changeState(node, newState, context);
    }
    
    
  }

  /* (non-Javadoc)
   * @see org.exoplatform.services.cms.publication.PublicationService#enrollNodeInLifecycle(javax.jcr.Node, java.lang.String)
   */
  public void enrollNodeInLifecycle(Node node, String lifecycle)
      throws AlreadyInPublicationLifecycleException, Exception {
    log.info("###########################");
    log.info("#  enrollNodeInLifecycle  #");
    log.info("###########################\n");
    
    if (isNodeEnrolledInLifecycle(node)) {
      throw new AlreadyInPublicationLifecycleException();
    } else {
      //create mixin publication,
      //with lifecycleName = lifecycle
      //current state = default state = enrolled
      //history : empty
      if(node.canAddMixin(PUBLICATION)) node.addMixin(PUBLICATION) ;
      else throw new NoSuchNodeTypeException() ;
      node.setProperty(LIFECYCLE_NAME, lifecycle);
      node.setProperty(CURRENT_STATE, "enrolled"); 
      List<Value> history = new ArrayList<Value>();
      node.setProperty(HISTORY, history.toArray(new Value[history.size()]));
    }
  }

  /* (non-Javadoc)
   * @see org.exoplatform.services.cms.publication.PublicationService#getCurrentState(javax.jcr.Node)
   */
  public String getCurrentState(Node node) throws NotInPublicationLifecycleException,Exception {
    log.info("#####################");
    log.info("#  getCurrentState  #");
    log.info("#####################\n");
    
    if (!isNodeEnrolledInLifecycle(node)) {
      throw new NotInPublicationLifecycleException();
    } else {
      return node.getProperty(CURRENT_STATE).getString();
    }
  }

  /* (non-Javadoc)
   * @see org.exoplatform.services.cms.publication.PublicationService#getLog(javax.jcr.Node)
   */
  public String[][] getLog(Node node) throws NotInPublicationLifecycleException, Exception {
    log.info("############");
    log.info("#  getLog  #");
    log.info("############\n");
    
    if (!isNodeEnrolledInLifecycle(node)) {
      throw new NotInPublicationLifecycleException();
    } else {
      Value[] values = node.getProperty(HISTORY).getValues();
      String [][] result=new String[values.length][];
      for (int i=0;i<values.length;i++) {
        Value currentValue=values[i];
        String currentString=currentValue.getString();
        String [] currentStrings=currentString.split(",");
        result[i]=currentStrings;
      }
      return result;
    }
    
  }

  /* (non-Javadoc)
   * @see org.exoplatform.services.cms.publication.PublicationService#getNodeLifecycleDesc(javax.jcr.Node)
   */
  public String getNodeLifecycleDesc(Node node) throws NotInPublicationLifecycleException, Exception {
    log.info("##########################");
    log.info("#  getNodeLifecycleDesc  #");
    log.info("##########################\n");
    
    if (!isNodeEnrolledInLifecycle(node)) {
      throw new NotInPublicationLifecycleException();
    } else {
      String lifecycleName=getNodeLifecycleName(node);
      PublicationPlugin nodePlugin = this.publicationPlugins_.get(lifecycleName);
      return nodePlugin.getNodeLifecycleDesc(node);
    }
    
  }

  /* (non-Javadoc)
   * @see org.exoplatform.services.cms.publication.PublicationService#getNodeLifecycleName(javax.jcr.Node)
   */
  public String getNodeLifecycleName(Node node) throws NotInPublicationLifecycleException, Exception {
    log.info("##########################");
    log.info("#  getNodeLifecycleName  #");
    log.info("##########################\n");
    
    if (!isNodeEnrolledInLifecycle(node)) {
      throw new NotInPublicationLifecycleException();
    } else {
      return node.getProperty(LIFECYCLE_NAME).getString();
    }
    
  }

  /* (non-Javadoc)
   * @see org.exoplatform.services.cms.publication.PublicationService#getPublicationPlugins()
   */
  public Map<String,PublicationPlugin> getPublicationPlugins() {
    log.info("##########################");
    log.info("#  getPublicationPlugins #");
    log.info("##########################\n");
    return this.publicationPlugins_;
  }

  /* (non-Javadoc)
   * @see org.exoplatform.services.cms.publication.PublicationService#getStateImage(javax.jcr.Node)
   */
  public byte[] getStateImage(Node node) throws NotInPublicationLifecycleException, Exception {
    log.info("###################");
    log.info("#  getStateImage  #");
    log.info("###################\n");
    if (!isNodeEnrolledInLifecycle(node)) {
      throw new NotInPublicationLifecycleException();
    } else {
      String lifecycleName=getNodeLifecycleName(node);
      PublicationPlugin nodePlugin = this.publicationPlugins_.get(lifecycleName);
      return nodePlugin.getStateImage(node);
    }
  }


  /* (non-Javadoc)
   * @see org.exoplatform.services.cms.publication.PublicationService#getUserInfo(javax.jcr.Node)
   */
  public String getUserInfo(Node node, Locale locale) throws NotInPublicationLifecycleException, Exception {
    log.info("#################");
    log.info("#  getUserInfo  #");
    log.info("#################\n");
    
    if (!isNodeEnrolledInLifecycle(node)) {
      throw new NotInPublicationLifecycleException();
    } else {
      String lifecycleName=getNodeLifecycleName(node);
      PublicationPlugin nodePlugin = this.publicationPlugins_.get(lifecycleName);
      String userInfo = nodePlugin.getUserInfo(node);
      
      ExoContainer container = ExoContainerContext.getCurrentContainer();    
      ResourceBundleService resourceBundleService = (ResourceBundleService) container.getComponentInstanceOfType(ResourceBundleService.class);
      ResourceBundleData resourceBundleData= resourceBundleService.getResourceBundleData(userInfo);
      
      resourceBundleData.setLanguage(locale.getDisplayLanguage());
      return resourceBundleData.getData();
       
    }
  }

  /* (non-Javadoc)
   * @see org.exoplatform.services.cms.publication.PublicationService#isNodeEnrolledInLifecycle(javax.jcr.Node)
   */
  public boolean isNodeEnrolledInLifecycle(Node node) throws Exception {
    log.info("###############################");
    log.info("#  isNodeEnrolledInLifecycle  #");
    log.info("###############################\n");
    
    return node.isNodeType(PUBLICATION);
  }

}
