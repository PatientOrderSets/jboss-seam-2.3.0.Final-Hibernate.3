package org.jboss.seam.security.management.action;

import static org.jboss.seam.ScopeType.CONVERSATION;
import static org.jboss.seam.annotations.Install.BUILT_IN;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.jboss.seam.annotations.Begin;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Install;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.core.Conversation;
import org.jboss.seam.security.management.IdentityManager;

@Name("org.jboss.seam.security.management.roleAction")
@Scope(CONVERSATION)
@Install(precedence = BUILT_IN)
public class RoleAction implements Serializable
{
   private String originalRole;
   private String role;
   private List<String> groups;
   
   @In IdentityManager identityManager;
   
   @Begin
   public void createRole()
   {
      groups = new ArrayList<String>();
   }
   
   @Begin
   public void editRole(String role)
   {
      this.originalRole = role;
      this.role = role;
      groups = identityManager.getRoleGroups(role);
   }
      
   public String save()
   {
      if (role != null && originalRole != null && !role.equals(originalRole))
      {
         identityManager.deleteRole(originalRole);
      }
      
      if (identityManager.roleExists(role))
      {
         return saveExistingRole();
      }
      else
      {
         return saveNewRole();
      }
   }
   
   private String saveNewRole()
   {      
      boolean success = identityManager.createRole(role);
      
      if (success)
      {
         for (String r : groups)
         {
            identityManager.addRoleToGroup(role, r);
         }
         
         Conversation.instance().end();
      }
      
      return "success";      
   }
   
   private String saveExistingRole()
   {
      List<String> grantedRoles = identityManager.getRoleGroups(role);
      
      if (grantedRoles != null)
      {
         for (String r : grantedRoles)
         {
            if (!groups.contains(r)) identityManager.removeRoleFromGroup(role, r);
         }
      }
      
      for (String r : groups)
      {
         if (grantedRoles == null || !grantedRoles.contains(r)) identityManager.addRoleToGroup(role, r);
      }
               
      Conversation.instance().end();
      return "success";
   }
   
   public String getRole()
   {
      return role;
   }
   
   public List<String> getAssignableRoles()
   {
      List<String> roles = identityManager.listGrantableRoles();
      roles.remove(role);
      return roles;
   }
   
   public void setRole(String role)
   {
      this.role = role;
   }

   public List<String> getGroups()
   {
      return groups;
   }
   
   public void setGroups(List<String> groups)
   {
      this.groups = groups;
   }
}