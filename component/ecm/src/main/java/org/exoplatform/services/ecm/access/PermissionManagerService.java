/*
 * Copyright (C) 2003-2008 eXo Platform SAS.
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
package org.exoplatform.services.ecm.access;

import java.util.Collection;
import java.util.List;

import org.exoplatform.services.organization.Group;
import org.exoplatform.services.organization.Membership;
import org.exoplatform.services.organization.MembershipHandler;
import org.exoplatform.services.organization.auth.AuthenticationService;
import org.exoplatform.services.organization.auth.Identity;

/**
 * Created by The eXo Platform SAS
 * @author : Hoa.Pham
 *          hoa.pham@exoplatform.com
 * May 12, 2008  
 */
public class PermissionManagerService {
  /*
   * This service is use to check permission for current user is loged in ecm - base system. It's built on top of authentication service
   * It should support some permission pattern like that:
   * - user: root,marry..
   * - group: /platform/user, '/orgnization/*'
   * - membership type: "manager:/* " 
   * - membership: manager:/platform/user, *:/platform/administrator 
   * - public resource to everyone: Everyone 
   * It should implement base on sun security(subject, intents....)
   * This service should be implement in exo-core*/

  final public static String WILD_CARD = "*".intern() ;
  final public static String EVERYONE = "Everyone".intern();     

  private final AuthenticationService authService_;
  public PermissionManagerService(AuthenticationService authService) {    
    this.authService_ = authService ;
  }

  public final boolean hasPermission(String userId, List<String> permissions) throws Exception {
    // in case of permission list has Everyone permission or contains userID
    if(permissions.contains(EVERYONE) || permissions.contains(userId)) return true ;
    Identity identity = authService_.getCurrentIdentity();
    if(identity != null && identity.getUsername().equalsIgnoreCase(userId)) 
      return checkPermission(identity, permissions) ;    
    return checkPermission(userId, permissions) ;    
  }

  private boolean checkPermission(String userId,List<String> permissions) throws Exception {
    boolean hasPermission = false ;
    for(String permision:permissions) {
      if(permision.indexOf(":/*")> 0) {        
        hasPermission = isMembershipTypeMatch(userId, permision) ;
      }else if( permision.indexOf(":/")>0) {                          
        hasPermission = isMembershipMatch(userId,permision) ;        
      }else {        
        hasPermission = isGroupMatch(userId,permision) ;
      }
      if(hasPermission) return true ;
    }    
    return false ;    
  }

  private boolean checkPermission(Identity identity, List<String> permissions) throws Exception {
    boolean hasPermission = false ;
    for(String permision:permissions) {
      if(permision.indexOf(":/*")> 0) {        
        hasPermission = isMembershipTypeMatch(identity.getUsername(), permision) ;
      }else if( permision.indexOf(":/")>0) {                          
        hasPermission = isMembershipMatch(identity,permision) ;        
      }else {        
        hasPermission = isGroupMatch(identity,permision) ;
      }
      if(hasPermission) return true ;
    }    
    return false ;
  }

  private boolean isMembershipTypeMatch(String userId, String permission) throws Exception {
    String membershipType  = permission.split(":")[0] ;    
    if(WILD_CARD.equalsIgnoreCase(membershipType)) return true ;
    Collection<Membership> allMembership = authService_.getOrganizationService().getMembershipHandler().findMembershipsByUser(userId) ;
    for(Membership membership: allMembership) {
      if(membershipType.equalsIgnoreCase(membership.getMembershipType())) return true ;
    } 
    return false ;
  }

  private boolean isMembershipMatch(Identity identity, String memebership) {
    String membershipType = memebership.substring(0, memebership.indexOf(":"));
    String groupID = memebership.substring(memebership.indexOf(":") + 1);
    if(WILD_CARD.equalsIgnoreCase(membershipType)) {
      return identity.isInGroup(groupID) ;
    }    
    return identity.hasMembership(membershipType, groupID) ;    
  }

  private boolean isMembershipMatch(String userId, String memebership) throws Exception {
    String membershipType = memebership.substring(0, memebership.indexOf(":"));
    String groupID = memebership.substring(memebership.indexOf(":") + 1);
    MembershipHandler membershipHandler = authService_.getOrganizationService().getMembershipHandler() ;
    if(WILD_CARD.equalsIgnoreCase(membershipType)) {     
      Collection memeberships = membershipHandler.findMembershipsByUserAndGroup(userId, groupID) ;
      if(memeberships.size()>0) return true;
    }
    Membership membership = membershipHandler.findMembershipByUserGroupAndType(userId, groupID, membershipType) ;
    if(membership != null) return true;
    return false ;
  }

  private boolean isGroupMatch(Identity identity, String groupID) {
    if(groupID.indexOf(WILD_CARD)>0) {
      return false ;
      //we don't support wild card with group name
    }
    return identity.isInGroup(groupID); 
  }

  private boolean isGroupMatch(String userID, String groupName) throws Exception {
    if(groupName.indexOf(WILD_CARD)>0) {
      return false ;
      //we don't support wild card with group name
    } 
    Collection<Group> groups = authService_.getOrganizationService().getGroupHandler().findGroupsOfUser(userID) ;
    for(Group group:groups) {
      if(group.getGroupName().equals(groupName)) return true ;
    }
    return true ;
  }
}
