/******************************************************************************
 * Copyright 2001-2006 The eXo Platform SARL. All rights reserved.            *
 * Please look at license.txt in info directory for more license detail.      *
 ******************************************************************************/
package org.exoplatform.services.workflow.impl.bonita;

import java.util.Hashtable;
import java.util.Locale;

import org.exoplatform.services.workflow.Form;

/**
 * Contains Forms that were previously created so
 * that they do not need to be created every time.
 * 
 * Created by Bull R&D
 * @author Brice Revenant
 * Feb 21, 2006
 */
public class FormCache {
  
  /**
   * Contains the Forms currently stored. The indexation is the following one :
   * Process Id -> State Name -> Locale -> Form
   */
  Hashtable<String, Hashtable<String, Hashtable<Locale, Form>>> forms =
    new Hashtable<String, Hashtable<String, Hashtable<Locale, Form>>>();
  
  /**
   * Retrieves a Form from the cache based on specified information
   * 
   * @param processId identifies the process
   * @param stateName identifies the state
   * @param locale    locale in which the Form should be retrieved
   * @return the requested Form or null if missing
   */
  public Form getForm(String processId,
                      String stateName,
                      Locale locale) {
    // Retrieve the states hashtable based on the Process identifier
    Hashtable<String, Hashtable<Locale, Form>> states = forms.get(processId);
    if(states == null) {
      return null;
    }
    
    // Retrieve the locales hashtable based on the state name
    Hashtable<Locale, Form> locales = states.get(stateName);
    if(locales == null) {
      return null;
    }
    
    // Retrieve the form based on the Locale
    return locales.get(locale);
  }
  
  /**
   * Remove all Forms corresponding to a Process Model
   * 
   * @param processId identifies the process
   */
  public void removeForms(String processId) {
    // Remove the entry from the Hashtable
    forms.remove(processId);
  }
  
  /**
   * Puts a Form in the cache based on specified information
   * 
   * @param processId identifies the process
   * @param stateName identifies the state
   * @param locale    locale of the form
   * @param form      the Form to be cached
   */
  public void setForm(String processId,
                      String stateName,
                      Locale locale,
                      Form   form) {
    // Retrieve or create the states hashtable
    Hashtable<String, Hashtable<Locale, Form>> states = forms.get(processId);
    if(states == null) {
      states = new Hashtable<String, Hashtable<Locale, Form>>();
      forms.put(processId, states);
    }
    
    // Retrieve or create the locales hashtable
    Hashtable<Locale, Form> locales = states.get(stateName);
    if(locales == null) {
      locales = new Hashtable<Locale, Form>();
      states.put(stateName, locales);
    }
    
    // Put the form in the locales hashtable
    locales.put(locale, form);
  }
  
  /**
   * Creates a String representation of the cache for debugging purpose
   * @return a String representation of the cache
   */
  public String toString() {
    StringBuffer ret = new StringBuffer();
    
    for(String processId : forms.keySet()) {
      ret.append("Process Id = " + processId + "\n");
      Hashtable<String, Hashtable<Locale, Form>> states = forms.get(processId);
      
      for(String stateName : states.keySet()) {
        ret.append("  State Name = " + stateName + "\n");
        Hashtable<Locale, Form> locales = states.get(stateName);
        
        for(Locale locale : locales.keySet()) {
          ret.append("    Locale = " + locale.toString() + "\n");
        }
      }
    }
    
    return ret.toString();
  }
}
