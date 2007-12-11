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
