/*
 * Copyright 2001-2003 The eXo platform SARL All rights reserved.
 * Please look at license.txt in info directory for more license detail. 
 */

package org.exoplatform.services.workflow.impl.jbpm;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import org.exoplatform.services.workflow.Form;
import org.exoplatform.services.workflow.WorkflowFormsService;
import org.exoplatform.services.workflow.WorkflowServiceContainer;
import org.jbpm.db.JbpmSession;
import org.jbpm.file.def.FileDefinition;
import org.jbpm.graph.def.ProcessDefinition;

/**
 * Created y the eXo platform team User: Benjamin Mestrallet Date: 17 mai 2004
 */
public class WorkflowFormsServiceImpl implements WorkflowFormsService {

  /**
   * double nested map : definitionId's --> stateName's --> Form's
   */
  private static Map allForms = new HashMap();

  private WorkflowServiceContainerImpl container;

  public WorkflowFormsServiceImpl(
      WorkflowServiceContainer workflowServiceContainer) {
    this.container = (WorkflowServiceContainerImpl) workflowServiceContainer;
  }

  public Form getForm(String definitionStringId, String stateName, Locale locale) {
    long definitionId = Long.parseLong(definitionStringId);
    Form formConfiguration = null;
    if (stateName == null)
      throw new NullPointerException("stateName is null in Form.getForm");

    Map stateNameToForms = (Map) allForms.get(new Long(definitionId));
    if (stateNameToForms == null) {
      addForms(definitionId, locale);
      stateNameToForms = (Map) allForms.get(new Long(definitionId));
    }

    formConfiguration = (Form) stateNameToForms.get(stateName);
    if (stateNameToForms == null)
      throw new IllegalArgumentException("no form was specified for state '"
          + stateName + "' in definition '" + definitionId + "'");

    return formConfiguration;
  }

  private void addForms(long definitionId, Locale locale) {
    if (!allForms.containsKey(new Long(definitionId))) {
      Map stateNameToForms = new HashMap();
      JbpmSession session = null;
      try {        
        session = container.openSession();
        ProcessDefinition pD = session.getGraphSession().loadProcessDefinition(
            definitionId);
        FileDefinition fD = pD.getFileDefinition();
        InputStream iS = fD.getInputStream("forms.xml");
        SAXReader reader = new SAXReader();
        Document document = reader.read(iS);   
        Element rootElement = document.getRootElement();
        List list = rootElement.elements("form");
        for (Iterator iter = list.iterator(); iter.hasNext();) {
          Element element = (Element) iter.next();
          Form formConfiguration = new FormImpl(fD, element, locale);
          stateNameToForms.put(formConfiguration.getStateName(),
              formConfiguration);
        }
        allForms.put(new Long(definitionId), stateNameToForms);
      } catch (DocumentException e) {
        e.printStackTrace();
      }
    }
  }

  public void removeForms(String processDefinitionId) {
    allForms.remove(new Long(processDefinitionId));
  }
}