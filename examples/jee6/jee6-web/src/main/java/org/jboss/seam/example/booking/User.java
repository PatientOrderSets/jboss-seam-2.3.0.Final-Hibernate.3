//$Id: User.java 5579 2007-06-27 00:06:49Z gavin $
package org.jboss.seam.example.booking;

import static org.jboss.seam.ScopeType.SESSION;

import java.io.Serializable;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import javax.validation.constraints.*;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;

@Entity
@Name("user")
@Scope(SESSION)
@Table(name="Customer")
public class User implements Serializable
{
   private static final long serialVersionUID = 4818188553954060410L;
   
   private String username;
   private String password;
   private String name;
   
   public User(String name, String password, String username)
   {
      this.name = name;
      this.password = password;
      this.username = username;
   }
   
   public User() {}

   @NotNull
   @Size(max=100)
   public String getName()
   {
      return name;
   }
   public void setName(String name)
   {
      this.name = name;
   }
   
   @NotNull
   @Size(min=5, max=15)
   public String getPassword()
   {
      return password;
   }
   public void setPassword(String password)
   {
      this.password = password;
   }
   
   @Id
   @Size(min=4, max=15)
   @Pattern(regexp="^\\w*$", message="not a valid username")
   public String getUsername()
   {
      return username;
   }
   public void setUsername(String username)
   {
      this.username = username;
   }
   
   @Override
   public String toString() 
   {
      return "User(" + username + ")";
   }
}
