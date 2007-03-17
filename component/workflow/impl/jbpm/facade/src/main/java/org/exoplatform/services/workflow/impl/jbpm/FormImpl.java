/*
 * Copyright 2001-2003 The eXo platform SARL All rights reserved.
 * Please look at license.txt in info directory for more license detail. 
 */

package org.exoplatform.services.workflow.impl.jbpm;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.PropertyResourceBundle;
import java.util.ResourceBundle;

import org.apache.commons.logging.Log;
import org.dom4j.Element;
import org.exoplatform.container.PortalContainer;
import org.exoplatform.services.download.DownloadService;
import org.exoplatform.services.download.InputStreamDownloadResource;
import org.exoplatform.services.log.LogService;
import org.exoplatform.services.workflow.Form;
import org.jbpm.file.def.FileDefinition;

/**
 * Created y the eXo platform team
 * User: Benjamin Mestrallet
 * Date: 17 mai 2004
 */
public class FormImpl implements Form{

  private String stateName;
  private List variables;
  private List submitButtons;
  private ResourceBundle resourceBundle;
  private Log log;
  private boolean customizedView;
  private boolean delegatedView;
  private String customizedViewString;
  private byte[] iconBytes;
  private byte[] stateImageBytes;

  public FormImpl(FileDefinition fileDefinition, Element element, Locale locale) {
    this.log = ((LogService)PortalContainer.getInstance().getComponentInstanceOfType(LogService.class)).
        getLog("org.exoplatform.services.workflow");
    
    Element childElement = element.element("resource-bundle");
    String formFileName = "";
    if(childElement != null)
      formFileName = childElement.getText();

    //manage properties
    String localisedFileName = getLocalisedString(formFileName, locale);
    log.debug("Try to find localised resource : " + localisedFileName);
    byte[] bytes = null;
    try {
      bytes = fileDefinition.getBytes(localisedFileName);
    } catch (Exception e) {
      log.debug("Try to find default resource : " + formFileName + ".properties");
      try {
        bytes = fileDefinition.getBytes(formFileName + ".properties");        
      } catch (Exception ex) {}  
    }
    if(bytes != null) {
     log.debug("resource bundle found true");
     try {
       resourceBundle = new PropertyResourceBundle(new ByteArrayInputStream(bytes));
     } catch (IOException e) {
       e.printStackTrace();
     }     
    } else {
      log.debug("resource bundle not found");
    }

    childElement = element.element("state-name");
    if(childElement != null)
      this.stateName = childElement.getText();
    
    initializeVariables(element);
    initializeSubmitButtons(element);
    
    childElement = element.element("customized-view");    
    if(childElement != null)
      this.customizedViewString = childElement.getText();
    if(customizedViewString != null && !"".equals(customizedViewString)){
      customizedView = true;
    }
    
    childElement = element.element("delegated-view");
    String delegatedViewString = "";
    if(childElement != null)
      delegatedViewString = childElement.getText();
    if("true".equals(delegatedViewString)){
      delegatedView = true;
    }    
    
    
    //manages bound images
    this.iconBytes = getBytes(fileDefinition, stateName + "-icon.gif");
    this.stateImageBytes = getBytes(fileDefinition, stateName + "-state.gif");
  }

  private String getLocalisedString(String fileName, Locale locale) {
    return fileName + "_" + locale.getLanguage() + ".properties";
  }

  private void initializeVariables(Element element) {
    this.variables = new ArrayList();
    Map attributes = null;
    Iterator iter = element.elements("variable").iterator();    
    while (iter.hasNext()) {
      Element variableElement = (Element) iter.next();
      attributes = new HashMap();
      String variableName = variableElement.attributeValue("name");
      attributes.put("name", variableName);
      String componentName = variableElement.attributeValue("component");
      attributes.put("component", componentName);
      String editable = variableElement.attributeValue("editable");
      attributes.put("editable", editable);
      String mandatory = variableElement.attributeValue("mandatory");
      attributes.put("mandatory", mandatory);      
      this.variables.add(attributes);      
    }
  }

  private void initializeSubmitButtons(Element element) {
    this.submitButtons = new ArrayList();
    Map attributes = null;
    Iterator iter = element.elements("submitbutton").iterator();
    while (iter.hasNext()) {
      Element submitButtonElement = (Element) iter.next();
      attributes = new HashMap();
      String value = submitButtonElement.attributeValue("name");
      attributes.put("name", value);
      String transitionName = submitButtonElement.attributeValue("transition-name");
      attributes.put("transition", transitionName);
      this.submitButtons.add(attributes);
    }
  }

  public List getVariables() {
    return variables;
  }

  public List getSubmitButtons() {
    return submitButtons;
  }

  public String getStateName() {
    return stateName;
  }

  public ResourceBundle getResourceBundle() {
    return resourceBundle;
  }

  public boolean isCustomizedView() {
    return customizedView;
  }
  
  public String getCustomizedView() {
    return customizedViewString;
  }

  public boolean isDelegatedView() {
    return delegatedView;
  }

  public String getIconURL() {
    return getURL(iconBytes);
  }
  
  public String getStateImageURL() {
    return getURL(stateImageBytes);
  }

  public byte[] getBytes(FileDefinition fileDefinition, String file) {
    try {
      return fileDefinition.getBytes(file);
    } catch (Throwable t) {
      return null;
    }    
  }
  
  public String getURL(byte[] bytes) {
    DownloadService dS = (DownloadService) PortalContainer.getInstance().getComponentInstanceOfType(
        DownloadService.class);
    InputStream iS = new ByteArrayInputStream(bytes);    
    String id = dS.addDownloadResource(new InputStreamDownloadResource(iS, "image/gif"));
    return dS.getDownloadLink(id);
  }

}
