/******************************************************************************
 * Copyright 2001-2006 The eXo Platform SARL. All rights reserved.            *
 * Please look at license.txt in info directory for more license detail.      *
 ******************************************************************************/
package org.exoplatform.services.workflow.impl.bonita;

import hero.interfaces.BnNodeLocal;
import hero.interfaces.BnProjectLocal;
import hero.interfaces.BnProjectLocalHome;
import hero.interfaces.BnProjectPK;
import hero.interfaces.BnProjectUtil;

import java.util.Locale;

import org.exoplatform.services.workflow.FileDefinition;
import org.exoplatform.services.workflow.Form;
import org.exoplatform.services.workflow.WorkflowFileDefinitionService;
import org.exoplatform.services.workflow.WorkflowFormsService;

/**
 * This service retrieves and caches Forms
 * 
 * Created by Bull R&D
 * @author Brice Revenant
 * Dec 28, 2005
 */
public class WorkflowFormsServiceImpl implements WorkflowFormsService {

  /** Caches the Forms that have been created so far */
  private FormCache cache = new FormCache();
  
  /** Reference to a File Definition Service implementation */
  private WorkflowFileDefinitionService fileDefinitionService = null;
  
  /**
   * Retrieves a Form based on a process model, a state and a Locale. As the
   * process instance identifier is not specified as parameter, the attributes
   * shown are only those defined in the process model, which means propagable
   * attributes are ignored.
   * @param processId identifies the process
   * @param stateName identifies the activity
   * @param locale    specifies the Locale
   */
  public Form getForm(String processId,
                      String stateName,
                      Locale locale) {

    // Determine if the Form is cached yet
    Form form = cache.getForm(processId, stateName, locale);

    if(form == null) {
      // The Form is not found. Retrieve it from the persistent storage
      FileDefinition fileDefinition =
        this.fileDefinitionService.retrieve(processId);
      
      if(fileDefinition != null && fileDefinition.isFormDefined(stateName)) {
        // The Form is found in the storage and defined
        form = new SpecifiedFormImpl(fileDefinition, stateName, locale);
      }
      else {
        // The Form is not found in the storage and not defined
        form = new AutomaticFormImpl(processId, stateName, locale);
      }
      
      // Cache the Form to speed up subsequent accesses
      cache.setForm(processId, stateName, locale, form);
    }

    return form;
  }
  
  /**
   * Remove all Forms corresponding to a Process Model
   * 
   * @param processDefinitionId identifies the Process Model
   */
  public void removeForms(String processDefinitionId) {
    
    // Remove the specified Forms from the cache
    this.cache.removeForms(processDefinitionId);
  }
  
  /**
   * Creates a new instance of the service
   * 
   * @param fileDefinitionService this injected reference to a File Definition
   *                              service is used to store and retrieved
   *                              definitions of processes, which include
   *                              among others Forms.
   */
  public WorkflowFormsServiceImpl(
      WorkflowFileDefinitionService fileDefinitionService) {
    // Store references to dependent services
    this.fileDefinitionService = fileDefinitionService;
  }
}
