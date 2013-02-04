package com.jboss.dvd.seam;

import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceException;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Out;
import org.jboss.seam.bpm.Actor;
import org.jboss.seam.security.Identity;

@Stateless
@Name("authenticator")
public class AuthenticatorAction implements Authenticator
{
    @In 
    private EntityManager entityManager;

    @In Actor actor;
    @In Identity identity;

    @Out(required=false, scope=ScopeType.SESSION) 
    User currentUser;

    public boolean authenticate()
    {
        try {
            currentUser = (User) 
                entityManager.createQuery("select u from User u where u.userName = #{identity.username} and u.password = #{identity.password}")       
                             .getSingleResult();
        } catch (PersistenceException e) {
            return false;
        }

        actor.setId(identity.getUsername());

        if (currentUser instanceof Admin) {
            actor.getGroupActorIds().add("shippers");
            actor.getGroupActorIds().add("reviewers");
            identity.addRole("admin");
        }
      
        return true;
    }
}
