package org.jboss.seam.security.permission;

import static org.jboss.seam.ScopeType.APPLICATION;
import static org.jboss.seam.annotations.Install.BUILT_IN;

import java.io.Serializable;
import java.security.Principal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.persistence.EntityManager;
import javax.persistence.Query;

import org.jboss.seam.Component;
import org.jboss.seam.annotations.Create;
import org.jboss.seam.annotations.Install;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.intercept.BypassInterceptors;
import org.jboss.seam.annotations.security.permission.PermissionAction;
import org.jboss.seam.annotations.security.permission.PermissionDiscriminator;
import org.jboss.seam.annotations.security.permission.PermissionRole;
import org.jboss.seam.annotations.security.permission.PermissionTarget;
import org.jboss.seam.annotations.security.permission.PermissionUser;
import org.jboss.seam.core.Expressions;
import org.jboss.seam.core.Expressions.ValueExpression;
import org.jboss.seam.log.LogProvider;
import org.jboss.seam.log.Logging;
import org.jboss.seam.security.Role;
import org.jboss.seam.security.SimplePrincipal;
import org.jboss.seam.security.management.IdentityManager;
import org.jboss.seam.security.management.IdentityStore;
import org.jboss.seam.security.management.JpaIdentityStore;
import org.jboss.seam.security.permission.PermissionMetadata.ActionSet;
import org.jboss.seam.util.AnnotatedBeanProperty;

/**
 * A permission store implementation that uses JPA as its persistence mechanism.
 * 
 * @author Shane Bryzak
 */
@Name("org.jboss.seam.security.jpaPermissionStore")
@Install(precedence = BUILT_IN, value=false) 
@Scope(APPLICATION)
@BypassInterceptors
public class JpaPermissionStore implements PermissionStore, Serializable
{
   private static final LogProvider log = Logging.getLogProvider(JpaPermissionStore.class);
   
   private enum Discrimination { user, role, either }
   
   private ValueExpression<EntityManager> entityManager;
   
   private Class userPermissionClass;
   private Class rolePermissionClass;
      
   private AnnotatedBeanProperty<PermissionUser> userProperty;
   private AnnotatedBeanProperty<PermissionRole> roleProperty;
   
   private AnnotatedBeanProperty<PermissionTarget> targetProperty;
   private AnnotatedBeanProperty<PermissionAction> actionProperty;   
   private AnnotatedBeanProperty<PermissionDiscriminator> discriminatorProperty;
   
   private AnnotatedBeanProperty<PermissionTarget> roleTargetProperty;
   private AnnotatedBeanProperty<PermissionAction> roleActionProperty;
   
   private Map<Integer,String> queryCache = new HashMap<Integer,String>();
   
   private IdentifierPolicy identifierPolicy;
   
   private PermissionMetadata metadata;

   @Create
   public void init()
   {
      metadata = new PermissionMetadata();
      
      // TODO see if we can scan for this automatically      
      if (userPermissionClass == null)
      {
         log.debug("No permissionClass set, JpaPermissionStore will be unavailable.");
         return;
      }   
      
      if (entityManager == null)
      {
         entityManager = Expressions.instance().createValueExpression("#{entityManager}", 
               EntityManager.class);
      }       
      
      initProperties();
      
      identifierPolicy = (IdentifierPolicy) Component.getInstance(IdentifierPolicy.class, true);
   }   
   
   protected void initProperties()
   {
      userProperty = new AnnotatedBeanProperty<PermissionUser>(userPermissionClass, PermissionUser.class);
      targetProperty = new AnnotatedBeanProperty<PermissionTarget>(userPermissionClass, PermissionTarget.class);
      actionProperty = new AnnotatedBeanProperty<PermissionAction>(userPermissionClass, PermissionAction.class);
      
      if (rolePermissionClass != null)
      {
         roleProperty = new AnnotatedBeanProperty<PermissionRole>(rolePermissionClass, PermissionRole.class);
         if (roleProperty.isSet())
         {
            roleTargetProperty = new AnnotatedBeanProperty<PermissionTarget>(rolePermissionClass, 
                  PermissionTarget.class);
            roleActionProperty = new AnnotatedBeanProperty<PermissionAction>(rolePermissionClass, 
                  PermissionAction.class);
         }
      }
      else
      {
         roleProperty = new AnnotatedBeanProperty<PermissionRole>(userPermissionClass, PermissionRole.class);
         if (roleProperty.isSet())
         {
            discriminatorProperty = new AnnotatedBeanProperty<PermissionDiscriminator>(userPermissionClass, 
                  PermissionDiscriminator.class);
         }
      }
      
      if (!userProperty.isSet()) 
      {
         throw new RuntimeException("Invalid userPermissionClass " + userPermissionClass.getName() + 
               " - required annotation @PermissionUser not found on any Field or Method.");
      }

      if (rolePermissionClass != null)
      {
         if (!roleProperty.isSet())
         {
            throw new RuntimeException("Invalid rolePermissionClass " + rolePermissionClass.getName() +
                  " - required annotation @PermissionRole not found on any Field or Method.");
         }
         
         if (!roleTargetProperty.isSet())
         {
            throw new RuntimeException("Invalid rolePermissionClass " + rolePermissionClass.getName() +
                  " - required annotation @PermissionTarget not found on any Field or Method.");
         }
         
         if (!roleActionProperty.isSet())
         {
            throw new RuntimeException("Invalid rolePermissionClass " + rolePermissionClass.getName() +
                  " - required annotation @PermissionAction not found on any Field or Method.");
         }
      }
      else if (!discriminatorProperty.isSet())
      {
         throw new RuntimeException("Invalid userPermissionClass " + userPermissionClass.getName() +
               " - no rolePermissionClass set and @PermissionDiscriminator annotation not found on " +
               "any Field or Method");
      }
   }   
   
   /**
    * Creates a Query that returns a list of permission records for the specified parameters. 
    * 
    * @param target The target of the permission, may be null
    * @param targets A set of permission targets, may be null
    * @param recipient The permission recipient, may be null
    * @param discrimination A discrimination (either user, role or both), required
    * @return Query The query generated for the provided parameters
    */
   protected Query createPermissionQuery(Object target, Set targets, Principal recipient, Discrimination discrimination)
   {
      if (target != null && targets != null)
      {
         throw new IllegalArgumentException("Cannot specify both target and targets");
      }
      
      int queryKey = (target != null) ? 1 : 0;
      queryKey |= (targets != null) ? 2 : 0;
      queryKey |= (recipient != null) ? 4 : 0;
      queryKey |= (discrimination.equals(Discrimination.user) ? 8 : 0);
      queryKey |= (discrimination.equals(Discrimination.role) ? 16 : 0);
      queryKey |= (discrimination.equals(Discrimination.either) ? 32 : 0);
      
      boolean isRole = discrimination.equals(Discrimination.role);
      boolean useRoleTable = isRole && rolePermissionClass != null; 
      
      if (!queryCache.containsKey(queryKey))
      {  
         boolean conditionsAdded = false;
         
         StringBuilder q = new StringBuilder();
         q.append("select p from ");
         q.append(useRoleTable ? rolePermissionClass.getName() : userPermissionClass.getName());
         q.append(" p");
         
         if (target != null)
         {
            q.append(" where p.");
            q.append(useRoleTable ? roleTargetProperty.getName() : targetProperty.getName());
            q.append(" = :target");
            conditionsAdded = true;
         }
         
         if (targets != null)
         {
            q.append(" where p.");
            q.append(useRoleTable ? roleTargetProperty.getName() : targetProperty.getName());
            q.append(" in (:targets)");
            conditionsAdded = true;
         }
         
         if (recipient != null)
         {
            q.append(conditionsAdded ? " and p." : " where p.");
            q.append(isRole ? roleProperty.getName() : userProperty.getName());
            q.append(" = :recipient");
            conditionsAdded = true;
         }
         
         // If there is no discrimination, then don't add such a condition to the query
         if (!discrimination.equals(Discrimination.either) && discriminatorProperty != null)
         {
            q.append(conditionsAdded ? " and p." : " where p.");
            q.append(discriminatorProperty.getName());
            q.append(" = :discriminator");
            conditionsAdded = true;
         }
         
         queryCache.put(queryKey, q.toString());
      }
      
      Query query = lookupEntityManager().createQuery(queryCache.get(queryKey));
      
      if (target != null) query.setParameter("target", identifierPolicy.getIdentifier(target));
      
      if (targets != null)
      {
         Set<String> identifiers = new HashSet<String>();
         for (Object t : targets)
         {
            identifiers.add(identifierPolicy.getIdentifier(t));
         }
         query.setParameter("targets", identifiers);
      }
      
      
      if (recipient != null) query.setParameter("recipient", resolvePrincipalEntity(recipient));
      
      if (!discrimination.equals(Discrimination.either) && discriminatorProperty != null) 
      {
         query.setParameter("discriminator", getDiscriminatorValue(
               discrimination.equals(Discrimination.role)));
      }
      
      return query;
   }
   
   public boolean grantPermission(Permission permission)
   {
      return updatePermissionActions(permission.getTarget(), permission.getRecipient(), 
            new String[] {permission.getAction()}, true);
   }
   
   public boolean revokePermission(Permission permission)
   {
      return updatePermissionActions(permission.getTarget(), permission.getRecipient(),
            new String[] { permission.getAction() }, false);
   }   
      
   /**
    * This is where the bulk of the actual work happens. 
    * 
    * @param target The target object to update permissions for
    * @param recipient The recipient to update permissions for
    * @param actions The actions that will be updated
    * @param set true if the specified actions are to be granted, false if they are to be revoked
    * @return true if the operation is successful
    */
   protected boolean updatePermissionActions(Object target, Principal recipient, String[] actions,
         boolean set)
   {
      boolean recipientIsRole = recipient instanceof Role;
      
      try
      {
         if (recipientIsRole)
         {
            if (rolePermissionClass != null)
            {
               List permissions = createPermissionQuery(target, null, recipient, Discrimination.role).getResultList();

               if (permissions.isEmpty())
               {
                  if (!set) return true;
                  
                  ActionSet actionSet = metadata.createActionSet(target.getClass(), null);
                  for (String action : actions)
                  {
                     actionSet.add(action);
                  }
                  
                  Object instance = rolePermissionClass.newInstance();
                  roleTargetProperty.setValue(instance, identifierPolicy.getIdentifier(target));
                  roleActionProperty.setValue(instance, actionSet.toString());
                  roleProperty.setValue(instance, resolvePrincipalEntity(recipient));
                  lookupEntityManager().persist(instance);
                  return true;
               }
                              
               Object instance = permissions.get(0);
               
               ActionSet actionSet = metadata.createActionSet(target.getClass(), 
                     roleActionProperty.getValue(instance).toString());
               
               for (String action : actions)
               {
                  if (set)
                  {
                     actionSet.add(action);
                  }
                  else
                  {
                     actionSet.remove(action);
                  }
               }
               
               if (permissions.size() > 1)
               {
                  // This is where it gets a little messy.. if there is more than one permission
                  // record, then we need to consolidate them all into just the first one
                  for (Object p : permissions)
                  {
                     actionSet.addMembers(roleActionProperty.getValue(p).toString());
                     if (!p.equals(instance))
                     {
                        lookupEntityManager().remove(p);
                     }
                  }                  
               }
                  
               if (!actionSet.isEmpty())
               {
                  roleActionProperty.setValue(instance, actionSet.toString());
                  lookupEntityManager().merge(instance);
               }
               else
               {
                  // No actions remaining in set, so just remove the record
                  lookupEntityManager().remove(instance);
               }
               
               return true;
            }
            
            if (!discriminatorProperty.isSet())
            {
               throw new RuntimeException("Could not grant permission, rolePermissionClass not set");   
            }
         }
         
         if (userPermissionClass == null)
         {
            throw new RuntimeException("Could not grant permission, userPermissionClass not set");
         }
                         
         List permissions = createPermissionQuery(target, null, recipient, recipientIsRole ? 
               Discrimination.role : Discrimination.user).getResultList();

         if (permissions.isEmpty())
         {
            if (!set) return true;
            
            ActionSet actionSet = metadata.createActionSet(target.getClass(), null);
            for (String action : actions)
            {
               actionSet.add(action);
            }
            
            Object instance = userPermissionClass.newInstance();
            targetProperty.setValue(instance, identifierPolicy.getIdentifier(target));
            actionProperty.setValue(instance, actionSet.toString());
            
            if (recipientIsRole)
            {
               roleProperty.setValue(instance, resolvePrincipalEntity(recipient));
            }
            else
            {
               userProperty.setValue(instance, resolvePrincipalEntity(recipient));
            }
                       
            if (discriminatorProperty.isSet())
            {
               PermissionDiscriminator discriminator = discriminatorProperty.getAnnotation();
               discriminatorProperty.setValue(instance, recipientIsRole ? discriminator.roleValue() :
                  discriminator.userValue());
            }
            
            lookupEntityManager().persist(instance);
            return true;
         }
                        
         Object instance = permissions.get(0);
         
         ActionSet actionSet = metadata.createActionSet(target.getClass(), 
               actionProperty.getValue(instance).toString());
         
         for (String action : actions)
         {
            if (set)
            {
               actionSet.add(action);
            }
            else
            {
               actionSet.remove(action);
            }
         }
         
         if (permissions.size() > 1)
         {
            // Same as with roles, consolidate the records if there is more than one
            for (Object p : permissions)
            {
               actionSet.addMembers(actionProperty.getValue(p).toString());
               if (!p.equals(instance))
               {
                  lookupEntityManager().remove(p);
               }
            }                  
         }
            
         if (!actionSet.isEmpty())
         {
            actionProperty.setValue(instance, actionSet.toString());
            lookupEntityManager().merge(instance);
         }
         else
         {
            // No actions remaining in set, so just remove the record
            lookupEntityManager().remove(instance);
         }
         
         return true;                  
      }
      catch (Exception ex)
      {
         throw new RuntimeException("Could not grant permission", ex);
      }    
   }
   
   public boolean grantPermissions(List<Permission> permissions)
   {
      // Target/Recipient/Action map
      Map<Object,Map<Principal,List<Permission>>> groupedPermissions = groupPermissions(permissions);
      
      for (Object target : groupedPermissions.keySet())
      {
         Map<Principal,List<Permission>> recipientPermissions = groupedPermissions.get(target);
                  
         for (Principal recipient : recipientPermissions.keySet())
         {
            List<Permission> ps = recipientPermissions.get(recipient);
            String[] actions = new String[ps.size()];
            for (int i = 0; i < ps.size(); i++) actions[i] = ps.get(i).getAction();
            updatePermissionActions(target, recipient, actions, true);
         }
      }
      
      return true;
   }
   
   public boolean revokePermissions(List<Permission> permissions)
   {
      // Target/Recipient/Action map
      Map<Object,Map<Principal,List<Permission>>> groupedPermissions = groupPermissions(permissions);
      
      for (Object target : groupedPermissions.keySet())
      {
         Map<Principal,List<Permission>> recipientPermissions = groupedPermissions.get(target);
                  
         for (Principal recipient : recipientPermissions.keySet())
         {
            List<Permission> ps = recipientPermissions.get(recipient);
            String[] actions = new String[ps.size()];
            for (int i = 0; i < ps.size(); i++) actions[i] = ps.get(i).getAction();
            updatePermissionActions(target, recipient, actions, false);
         }
      }
      
      return true;
   }  
   
   /**
    * Groups a list of arbitrary permissions into a more easily-consumed structure
    * 
    * @param permissions The list of permissions to group
    * @return 
    */
   private Map<Object,Map<Principal,List<Permission>>> groupPermissions(List<Permission> permissions)
   {
      // Target/Recipient/Action map
      Map<Object,Map<Principal,List<Permission>>> groupedPermissions = new HashMap<Object,Map<Principal,List<Permission>>>();
      
      for (Permission permission : permissions)
      {
         if (!groupedPermissions.containsKey(permission.getTarget()))
         {
            groupedPermissions.put(permission.getTarget(), new HashMap<Principal,List<Permission>>());
         }
         
         Map<Principal,List<Permission>> recipientPermissions = groupedPermissions.get(permission.getTarget());
         if (!recipientPermissions.containsKey(permission.getRecipient()))
         {
            List<Permission> perms = new ArrayList<Permission>();
            perms.add(permission);
            recipientPermissions.put(permission.getRecipient(), perms);
         }
         else
         {
            recipientPermissions.get(permission.getRecipient()).add(permission);
         }
      }

      return groupedPermissions;
   }
   
   private String getDiscriminatorValue(boolean isRole)
   {
      PermissionDiscriminator discriminator = discriminatorProperty.getAnnotation();
      return isRole ? discriminator.roleValue() : discriminator.userValue();      
   }

   /**
    * If the user or role properties in the entity class refer to other entities, then this method
    * uses the JpaIdentityStore (if available) to lookup that user or role entity.  Otherwise it
    * simply returns the name of the recipient. 
    * 
    * @param recipient
    * @return The entity or name representing the permission recipient
    */
   protected Object resolvePrincipalEntity(Principal recipient)
   {
      boolean recipientIsRole = recipient instanceof Role;
         
      JpaIdentityStore identityStore = (JpaIdentityStore) Component.getInstance(JpaIdentityStore.class, true);
      
      if (identityStore != null)
      {
         if (recipientIsRole && roleProperty.isSet() && roleProperty.getPropertyType().equals(identityStore.getRoleClass()))
         {
            return identityStore.lookupRole(recipient.getName());
         }
         else if (userProperty.getPropertyType().equals(identityStore.getUserClass()))
         {
            return identityStore.lookupUser(recipient.getName());
         }
      }      
      
      return recipient.getName();
   }
   
   protected Principal resolvePrincipal(Object principal, boolean isUser)
   {
      IdentityStore ids = IdentityManager.instance().getRoleIdentityStore();
      JpaIdentityStore identityStore = null;
      
      if (ids instanceof JpaIdentityStore) identityStore = (JpaIdentityStore) ids;
      
      if (principal instanceof String)
      {        
         return isUser ? new SimplePrincipal((String) principal) : new Role((String) principal, 
               identityStore == null ? false : identityStore.isRoleConditional((String) principal));
      }      
      
      if (identityStore != null)
      {
         if (isUser && identityStore.getUserClass().isAssignableFrom(principal.getClass()))
         {
            return new SimplePrincipal(identityStore.getUserName(principal));
         }
         
         if (!isUser && identityStore.getRoleClass().isAssignableFrom(principal.getClass()))
         {
            String name = identityStore.getRoleName(principal);
            return new Role(name, identityStore.isRoleConditional(name));
         }
      }
      
      throw new IllegalArgumentException("Cannot resolve principal name for principal " + principal); 
   }

   /**
    * Returns a list of all user and role permissions for the specified action for all specified target objects
    */
   public List<Permission> listPermissions(Set<Object> targets, String action)
   {
      // TODO limit the number of targets passed at a single time to 25
      return listPermissions(null, targets, action);
   }
   
   /**
    * Returns a list of all user and role permissions for a specific permission target and action.
    */
   public List<Permission> listPermissions(Object target, String action) 
   {
      return listPermissions(target, null, action);
   }
   
   protected List<Permission> listPermissions(Object target, Set<Object> targets, String action)
   {
      if (target != null && targets != null)
      {
         throw new IllegalArgumentException("Cannot specify both target and targets");
      }
      
      List<Permission> permissions = new ArrayList<Permission>();
      
      if (targets != null && targets.isEmpty()) return permissions;
      
      // First query for user permissions
      Query permissionQuery = targets != null ?
            createPermissionQuery(null, targets, null, Discrimination.either) :
            createPermissionQuery(target, null, null, Discrimination.either);
            
      List userPermissions = permissionQuery.getResultList();
      
      Map<String,Principal> principalCache = new HashMap<String,Principal>();
      
      boolean useDiscriminator = rolePermissionClass == null && discriminatorProperty.isSet();
      
      Map<String,Object> identifierCache = null;
      
      if (targets != null)
      {
         identifierCache = new HashMap<String,Object>();
         
         for (Object t : targets)
         {
            identifierCache.put(identifierPolicy.getIdentifier(t), t);
         }
      }
      
      for (Object permission : userPermissions)
      {
         ActionSet actionSet = null;
         
         if (targets != null)
         {            
            target = identifierCache.get(targetProperty.getValue(permission));
            if (target != null)
            {
               actionSet = metadata.createActionSet(target.getClass(), 
                  actionProperty.getValue(permission).toString());
            }
         }
         else
         {
            actionSet = metadata.createActionSet(target.getClass(),
                  actionProperty.getValue(permission).toString()); 
         }
         
         if (target != null && (action == null || (actionSet != null && actionSet.contains(action))))
         {         
            boolean isUser = true;
            
            if (useDiscriminator && 
               discriminatorProperty.getAnnotation().roleValue().equals(
                     discriminatorProperty.getValue(permission)))
            {
               isUser = false;
            }

            Principal principal = lookupPrincipal(principalCache, permission, isUser);
            
            if (action != null)
            {
               permissions.add(new Permission(target, action, principal));
            }
            else
            {            
               for (String a : actionSet.members())
               {
                  permissions.add(new Permission(target, a, principal));
               }
            }
         }
      }
      
      // If we have a separate class for role permissions, then query them now
      if (rolePermissionClass != null)
      {
         permissionQuery = targets != null ?
               createPermissionQuery(null, targets, null, Discrimination.role) :
               createPermissionQuery(target, null, null, Discrimination.role);        
         List rolePermissions = permissionQuery.getResultList();
         
         for (Object permission : rolePermissions)
         {            
            ActionSet actionSet = null;
            
            if (targets != null)
            {            
               target = identifierCache.get(roleTargetProperty.getValue(permission));
               if (target != null)
               {
                  actionSet = metadata.createActionSet(target.getClass(), 
                     roleActionProperty.getValue(permission).toString());
               }
            }
            else
            {
               actionSet = metadata.createActionSet(target.getClass(),
                     roleActionProperty.getValue(permission).toString()); 
            }            
                       
            if (target != null && (action == null || (actionSet != null && actionSet.contains(action))))
            {            
               Principal principal = lookupPrincipal(principalCache, permission, false);
               
               if (action != null)
               {
                  permissions.add(new Permission(target, action, principal));
               }
               else
               {            
                  for (String a : actionSet.members())
                  {
                     permissions.add(new Permission(target, a, principal));
                  }
               }               
            }
         }
      }
      
      return permissions;
   }
   
   private Principal lookupPrincipal(Map<String,Principal> cache, Object permission, boolean isUser)
   {
      Principal principal = resolvePrincipal(isUser ? userProperty.getValue(permission) :
         roleProperty.getValue(permission), isUser);
      
      String key = (isUser ? "u:" : "r:") + principal.getName();
      
      if (!cache.containsKey(key))
      {
         cache.put(key, principal);
      }
      else
      {
         principal = cache.get(key);
      }      
      
      return principal;
   }

   public List<Permission> listPermissions(Object target) 
   {
      return listPermissions(target, null);
   }
   
   public List<String> listAvailableActions(Object target)
   {
      return metadata.listAllowableActions(target.getClass());
   }

   private EntityManager lookupEntityManager()
   {
      return entityManager.getValue();
   }
   
   public ValueExpression getEntityManager()
   {
      return entityManager;
   }
   
   public void setEntityManager(ValueExpression expression)
   {
      this.entityManager = expression;
   } 
   
   public Class getUserPermissionClass()
   {
      return userPermissionClass;
   }
   
   public void setUserPermissionClass(Class userPermissionClass)
   {
      this.userPermissionClass = userPermissionClass;
   }
   
   public Class getRolePermissionClass()
   {
      return rolePermissionClass;
   }
   
   public void setRolePermissionClass(Class rolePermissionClass)
   {
      this.rolePermissionClass = rolePermissionClass;
   }
   
   public void clearPermissions(Object target)
   {
      EntityManager em = lookupEntityManager();
      String identifier = identifierPolicy.getIdentifier(target);
      
      em.createQuery(
            "delete from " + userPermissionClass.getName() + " p where p." +
            targetProperty.getName() + " = :target")
            .setParameter("target", identifier)
            .executeUpdate();
      
      if (rolePermissionClass != null)
      {
         em.createQuery(
               "delete from " + rolePermissionClass.getName() + " p where p." +
               roleTargetProperty.getName() + " = :target")
               .setParameter("target", identifier)
               .executeUpdate();
      }
   }
}
