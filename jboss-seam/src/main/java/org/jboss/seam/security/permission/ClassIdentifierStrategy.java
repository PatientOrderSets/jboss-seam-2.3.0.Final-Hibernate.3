package org.jboss.seam.security.permission;

import java.io.Serializable;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.jboss.seam.Seam;
import org.jboss.seam.annotations.security.permission.Identifier;

/**
 * An Identifier strategy for class-based permission checks
 * 
 * @author Shane Bryzak
 */
public class ClassIdentifierStrategy implements IdentifierStrategy, Serializable
{
   private static final long serialVersionUID = 3338883246522630571L;
   
   private Map<Class<?>,String> identifierNames = new ConcurrentHashMap<Class<?>,String>();   
   
   public boolean canIdentify(Class<?> targetClass)
   {
      return Class.class.equals(targetClass);
   }

   public String getIdentifier(Object target)
   {
      if (!(target instanceof Class))
      {
         throw new IllegalArgumentException("Target [" + target + "] must be instance of Class");
      }
      
      return getIdentifierName((Class<?>) target);
   }
   
   private String getIdentifierName(Class<?> cls)
   {
      if (!identifierNames.containsKey(cls))
      {   
         String name = null;
         
         if (cls.isAnnotationPresent(Identifier.class))
         {
            Identifier identifier = (Identifier) cls.getAnnotation(Identifier.class);
            if (identifier.name() != null && !"".equals(identifier.name().trim()))
            {
               name = identifier.name();
            }
         }
         
         if (name == null)
         {
            name = Seam.getComponentName(cls);
         }
         
         if (name == null)
         {
            name = cls.getName().substring(cls.getName().lastIndexOf('.') + 1);
         }
         
         identifierNames.put(cls, name);
         return name;
      }
      
      return identifierNames.get(cls);
   }
}
