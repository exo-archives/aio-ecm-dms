/******************************************************************************
 * Copyright 2001-2006 The eXo Platform SARL. All rights reserved.            *
 * Please look at license.txt in info directory for more license detail.      *
 ******************************************************************************/
package hero.mapper;

import hero.interfaces.BnRoleLocal;
import hero.util.HeroException;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;

import org.exoplatform.services.workflow.impl.bonita.WorkflowServiceContainerImpl;

/**
 * This Role Mapper retrieves the name of the initiator from Instance
 * Properties. It was decided to use Instance Properties and not Bonita
 * convenience methods to match closely what was previously done in eXo. 
 * As a Role Mapper is invoked prior Instance Properties can be set,
 * this Role Mapper finds them in a Thread Local set by the method that starts
 * the Instance in the Bonita service.
 * 
 * Created by Bull R&D
 * @author Brice Revenant
 * Mar 13, 2006
 */
public class ContentValidationInitiatorRoleMapper implements RoleMapperI {

  /** Name of the Property that contains the Initiator name */
  public static final String PROPERTY_NAME = "initiator";
  
  public Collection searchMembers(Object bean,
                                  BnRoleLocal role,
                                  String userName)
      throws HeroException {
    
    /*
     * The user name to be mapped with the Bonita initiator role is contained by
     * an initial Instance Property. As Instance Properties have not been set
     * yet, a Thread Local is used to store them temporarily.
     * However we have decided to retrieve the initiator role based on the
     * user who launched the Process Instance. This is more reliable. At this
     * time, the initiator name set by eXo is "__system", which is a bug...
     * 
     * Map<String,Object> variables =
     *   WorkflowServiceContainerImpl.InitialVariables.get();
     * String userName = (String) variables.get(PROPERTY_NAME);
     */

    // Return a Collection containing the single retrieved user
    Collection<String> ret = new ArrayList<String>();
    ret.add(userName);
    
    return ret;
  }
}
