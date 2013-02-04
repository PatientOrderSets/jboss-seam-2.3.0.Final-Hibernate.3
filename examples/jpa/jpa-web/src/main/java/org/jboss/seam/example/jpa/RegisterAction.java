//$Id: RegisterAction.java 5509 2007-06-25 16:19:40Z gavin $
package org.jboss.seam.example.jpa;

import static org.jboss.seam.ScopeType.EVENT;

import javax.persistence.EntityManager;
import javax.persistence.NoResultException;

import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.faces.FacesMessages;

@Scope(EVENT)
@Name("register")
public class RegisterAction
{

   @In
   private User user;
   
   @In
   private EntityManager em;
   
   @In
   private FacesMessages facesMessages;
   
   private String verify;
   
   private boolean registered;
   
   public void register()
   {
      if ( user.getPassword().equals(verify) )
      {
         // this is JPA 2.0 usage of new method getSingleResult() 
         try
         {
            em.createQuery("select u.username from User u where u.username=#{user.username}").getSingleResult();
            facesMessages.addToControl("username", "Username #{user.username} already exists");
         }
         catch (NoResultException e)
         {
            em.persist(user);
            facesMessages.add("Successfully registered as #{user.username}");
            registered = true;
         }

      }
      else 
      {
         facesMessages.add("verify", "Re-enter your password");
         verify=null;
      }
   }
   
   public void invalid()
   {
      facesMessages.add("Please try again");
   }
   
   public boolean isRegistered()
   {
      return registered;
   }

   public String getVerify()
   {
      return verify;
   }

   public void setVerify(String verify)
   {
      this.verify = verify;
   }
   
}
