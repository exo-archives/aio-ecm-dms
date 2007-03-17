package org.exoplatform.services.workflow;

import java.util.Locale;

/**
 * Created y the eXo platform team
 * User: Benjamin Mestrallet
 * Date: 17 mai 2004
 */
public interface WorkflowFormsService {

  public Form getForm(String processDefinitionId, String stateName, Locale locale);
  public void removeForms(String processDefinitionId);
}
