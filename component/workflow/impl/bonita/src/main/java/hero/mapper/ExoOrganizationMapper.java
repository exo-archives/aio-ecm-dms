/******************************************************************************
 * Copyright 2001-2006 The eXo Platform SARL. All rights reserved.            *
 * Please look at license.txt in info directory for more license detail.      *
 ******************************************************************************/
package hero.mapper;

import hero.interfaces.BnRoleLocal;
import hero.util.HeroException;

import java.util.ArrayList;
import java.util.Collection;

import org.exoplatform.commons.utils.PageList;
import org.exoplatform.container.PortalContainer;
import org.exoplatform.services.organization.OrganizationService;
import org.exoplatform.services.organization.User;
import org.exoplatform.services.organization.UserHandler;
import org.exoplatform.services.workflow.WorkflowServiceContainer;

/**
 * This Role Mapper associates the execution of Activities to a list of users
 * who match a Membership and a Group in eXo. That way you can directly
 * specifiy an eXo group (eg: "member:/company/direction") as Bonita role name.
 * 
 * Created by Bull R&D
 * @author Brice Revenant
 * Mar 07, 2006
 */
public class ExoOrganizationMapper implements RoleMapperI {
  
  /**
   * Gets the list of eXo users who belong to the specified Membership and Group
   * 
   * @param membershipAndGroup specifies the Membership and Group
   */
  public static Collection<String> GetUsersFromMembershipAndGroup(
    String membershipAndGroup) {
    
    // The returned list
    Collection<String> users = new ArrayList<String>();
    
    try {
      // Lookup the eXo Organization service
      PortalContainer container = PortalContainer.getInstance();
      OrganizationService organization = (OrganizationService)
        container.getComponentInstanceOfType(OrganizationService.class);
      
      // Determine the Membership and Group
      String[] tokens   = membershipAndGroup.split(
        WorkflowServiceContainer.ACTOR_ID_KEY_SEPARATOR);
      String membership = null;
      String group      = null;
      
      if(tokens.length == 2) {
        // There is a single colon character
        membership = tokens[0];
        group      = tokens[1];
      }
      else {
        // There is not is single colon character
        membership = "*";
        group      = membershipAndGroup;
        
        // TODO Use the logging API instead
        System.err.println("Warning : The specified Bonita role does not "
                           + "conform to the syntax membership:group.");
      }
      
      // Retrieve all the users contained by the specified group
      UserHandler userHandler       = organization.getUserHandler();
      PageList pageList             = userHandler.findUsersByGroup(group);
      Collection<User> usersInGroup = pageList.getAll();
      
      // Process each user in the group
      for(User user : usersInGroup) {
        // Retrieve the name of the current user
        String userName = user.getUserName();
        
        if("*".equals(membership)
           || organization.getMembershipHandler().
              findMembershipByUserGroupAndType(userName,
                                               group,
                                               membership) != null) {
          // The user has the specified membership
          users.add(userName);
        }
      }
    }
    catch(Exception e) {
      // TODO Use logging API instead
      e.printStackTrace();
    }

    return users;
  }
  
  /* (non-Javadoc)
   * @see hero.mapper.RoleMapperI#searchMembers(java.lang.Object, hero.interfaces.BnRoleLocal, java.lang.String)
   */
  public Collection searchMembers(Object      bean,
                                  BnRoleLocal role,
                                  String      userName)
    throws HeroException {
    
    // Delegate the call
    return ExoOrganizationMapper.GetUsersFromMembershipAndGroup(role.getName());
  }
}
