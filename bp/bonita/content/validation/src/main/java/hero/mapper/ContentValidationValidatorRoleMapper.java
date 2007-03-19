/******************************************************************************
 * Copyright 2001-2006 The eXo Platform SARL. All rights reserved.            *
 * Please look at license.txt in info directory for more license detail.      *
 ******************************************************************************/
package hero.mapper;

import hero.interfaces.BnRoleLocal;

import java.util.Collection;
import java.util.Map;

import org.exoplatform.services.workflow.impl.bonita.WorkflowServiceContainerImpl;

/**
 * This Role Mapper retrieves the name of the validator Group in eXo from
 * Instance Properties. As a Role Mapper is invoked prior Instance Properties
 * can be set, this Role Mapper finds them in a Thread Local set by the method
 * that starts the Instance in the Bonita service.
 * 
 * Created by Bull R&D
 * @author Brice Revenant
 * Mar 09, 2006
 */
public class ContentValidationValidatorRoleMapper implements RoleMapperI {
  
  /** Name of the Property that contains the eXo Membership and Group */
  public static final String PROPERTY_NAME = "exo:validator";
  
  /* (non-Javadoc)
   * @see hero.mapper.RoleMapperI#searchMembers(java.lang.Object, hero.interfaces.BnRoleLocal, java.lang.String)
   */
  public Collection searchMembers(Object      bean,
                                  BnRoleLocal role,
                                  String      userName) {
    
    /*
     * The eXo role name to be mapped with the Bonita validator role is
     * contained by an initial Instance Property. As Instance Properties have
     * not been set yet, a Thread Local is used to store them temporarily. 
     */
    Map<String,Object> variables =
      WorkflowServiceContainerImpl.InitialVariables.get();
    String roleName = (String) variables.get(PROPERTY_NAME);
    
    // Delegate the call
    return ExoOrganizationMapper.GetUsersFromMembershipAndGroup(roleName);
  }
}
