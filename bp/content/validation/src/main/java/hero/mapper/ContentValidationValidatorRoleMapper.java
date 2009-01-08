/*
 * Copyright (C) 2003-2007 eXo Platform SAS.
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
