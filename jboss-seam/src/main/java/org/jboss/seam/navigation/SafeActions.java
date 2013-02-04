package org.jboss.seam.navigation;

import static org.jboss.seam.annotations.Install.BUILT_IN;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import javax.faces.context.FacesContext;

import org.jboss.seam.Component;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Install;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.intercept.BypassInterceptors;
import org.jboss.seam.contexts.Contexts;

/**
 * Maintains a set of "safe" actions that may be performed 
 * by &lt;s:link/&gt;, as determined by actually parsing
 * the view.
 * 
 * @author Gavin King
 *
 */
@Scope(ScopeType.APPLICATION)
@BypassInterceptors
@Name("org.jboss.seam.navigation.safeActions")
@Install(precedence=BUILT_IN, classDependencies="javax.faces.context.FacesContext")
public class SafeActions
{
   
   private Set<String> safeActions = Collections.synchronizedSet( new HashSet<String>() );
   
   public static String toActionId(String viewId, String expression)
   {
      return viewId.substring(1) + ':' + expression.substring( 2, expression.length()-1 );
   }
   
   public static String toAction(String id)
   {
      int loc = id.indexOf(':');
      if (loc<0) throw new IllegalArgumentException();
      return "#{" + id.substring(loc+1) + "}";
   }
   
   public void addSafeAction(String id)
   {
      safeActions.add(id);
   }
   
   public boolean isActionSafe(String id)
   {
      if ( safeActions.contains(id) ) return true;
      
      int loc = id.indexOf(':');
      if (loc<0) throw new IllegalArgumentException("Invalid action method " + id);
      String viewId = id.substring(0, loc);
      String action = "\"#{" + id.substring(loc+1) + "}\"";
      
      // adding slash as it otherwise won't find a page viewId by getResource*
      InputStream is = FacesContext.getCurrentInstance().getExternalContext().getResourceAsStream("/" +viewId);
      if (is==null) throw new IllegalStateException("Unable to read view " + "/" + viewId + " to execute action " + action);
      BufferedReader reader = new BufferedReader( new InputStreamReader(is) );
      try
      {
         while ( reader.ready() ) 
         {
            if ( reader.readLine().contains(action) ) 
            {
               addSafeAction(id);
               return true;
            }
         }
         return false;
      }
      catch (IOException ioe)
      {
         throw new RuntimeException("Error parsing view " + "/" + viewId + " to execute action " + action, ioe);
      }
      finally
      {
         try
         {
            reader.close();
         }
         catch (IOException ioe) {
            throw new RuntimeException(ioe);
         }
      }
   }
   
   public static SafeActions instance()
   {
      if ( !Contexts.isApplicationContextActive() )
      {
         throw new IllegalStateException("No active application context");
      }
      return (SafeActions) Component.getInstance(SafeActions.class, ScopeType.APPLICATION);
   }
   
}
