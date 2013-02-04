//$Id: Expressions.java 14085 2011-04-22 09:36:42Z manaRH $
package org.jboss.seam.core;

import static org.jboss.seam.annotations.Install.BUILT_IN;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import javax.el.ELContext;
import javax.el.ExpressionFactory;

import org.jboss.seam.Component;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Install;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.intercept.BypassInterceptors;
import org.jboss.seam.contexts.Contexts;
import org.jboss.seam.el.EL;
import org.jboss.seam.el.SeamExpressionFactory;
import org.jboss.seam.log.LogProvider;
import org.jboss.seam.log.Logging;

/**
 * Factory for EL method and value expressions.
 * 
 * This default implementation uses JBoss EL.
 * 
 * @author Gavin King
 */
@Scope(ScopeType.APPLICATION)
@BypassInterceptors
@Install(precedence=BUILT_IN)
@Name("org.jboss.seam.core.expressions")
public class Expressions implements Serializable
{
   private static final LogProvider log = Logging.getLogProvider(Expressions.class);
   private static List<String> blacklist = new ArrayList<String>();
   
   // loading blacklisted patterns of non-valid EL expressions
   static 
   {
      BufferedReader reader = null;
      try
      {
         InputStream blacklistIS = ResourceLoader.instance().getResourceAsStream("blacklist.properties");
         reader = new BufferedReader(new InputStreamReader(blacklistIS));
         String line; 
         while ((line = reader.readLine()) != null)
         {
            blacklist.add(line);
         }
      }
      catch (IOException e)
      {
         log.warn("Black list of non-valid EL expressions was not found!");
      }
      finally
      {
         if (reader != null)
         {
            try
            {
               reader.close();
            }
            catch (IOException e) { }
         }
      }
      
   }
   
   /**
    * Get the JBoss EL ExpressionFactory
    */
   public ExpressionFactory getExpressionFactory()
   {
      return SeamExpressionFactory.INSTANCE;
   }
   
   /**
    * Get an appropriate ELContext. If there is an active JSF request,
    * use JSF's ELContext. Otherwise, use one that we create.
    */
   public ELContext getELContext() {
       return EL.createELContext();
   }

   /**
    * Create a value expression.
    * 
    * @param expression a JBoss EL value expression
    */
   public ValueExpression<Object> createValueExpression(String expression)
   {
      return createValueExpression(expression, Object.class);
   }
   
   /**
    * Create a method expression.
    * 
    * @param expression a JBoss EL method expression
    */
   public MethodExpression<Object> createMethodExpression(String expression)
   {
      return createMethodExpression(expression, Object.class);
   }
   
   /**
    * Create a value expression.
    * 
    * @param expression a JBoss EL value expression
    * @param type the type of the value 
    */
   public <T> ValueExpression<T> createValueExpression(final String expression, final Class<T> type)
   {

      checkELExpression(expression);
      
      return new ValueExpression<T>()
      {
         private javax.el.ValueExpression facesValueExpression;
         private javax.el.ValueExpression seamValueExpression;
         
         public javax.el.ValueExpression toUnifiedValueExpression()
         {
            if ( isFacesContextActive() )
            {
               if (facesValueExpression==null)
               {
                  facesValueExpression = createExpression();
               }
               return facesValueExpression;
            }
            else
            {
               if (seamValueExpression==null)
               {
                  seamValueExpression = createExpression();
               }
               return seamValueExpression;
            }
         }
         
         private javax.el.ValueExpression createExpression()
         {
            return getExpressionFactory().createValueExpression( getELContext(), expression, type );
         }
         
         public T getValue()
         {
            return (T) toUnifiedValueExpression().getValue( getELContext() );
         }        

        public void setValue(T value)
         {
            toUnifiedValueExpression().setValue( getELContext(), value );
         }
         
         public String getExpressionString()
         {
            return expression;
         }
         
         public Class<T> getType()
         {
            // QUESTION shouldn't we use the type provided in the constructor?
            return (Class<T>) toUnifiedValueExpression().getType( getELContext() );
         }
         
      };
   }
   
   /**
    * Create a method expression.
    * 
    * @param expression a JBoss EL method expression
    * @param type the method return type
    * @param argTypes the method parameter types
    */
   public <T> MethodExpression<T> createMethodExpression(final String expression, final Class<T> type, final Class... argTypes)
   {
      checkELExpression(expression);
      
      return new MethodExpression<T>()
      {
         private javax.el.MethodExpression facesMethodExpression;
         private javax.el.MethodExpression seamMethodExpression;
         
         public javax.el.MethodExpression toUnifiedMethodExpression()
         {
            if ( isFacesContextActive() )
            {
               if (facesMethodExpression==null)
               {
                  facesMethodExpression = createExpression();
               }
               return facesMethodExpression;
            }
            else
            {
               if (seamMethodExpression==null)
               {
                  seamMethodExpression = createExpression();
               }
               return seamMethodExpression;
            }
         }
         
         private javax.el.MethodExpression createExpression()
         {
            return getExpressionFactory().createMethodExpression( getELContext(), expression, type, argTypes );
         }
         
         public T invoke(Object... args)
         {
            return (T) toUnifiedMethodExpression().invoke( getELContext(), args );
         }
         
         public String getExpressionString()
         {
            return expression;
         }
         
      };
   }
   
   /**
    * A value expression - an EL expression that evaluates to
    * an attribute getter or get/set pair. This interface
    * is just a genericized version of the Unified EL ValueExpression
    * interface.
    * 
    * @author Gavin King
    *
    * @param <T> the type of the value
    */
   public static interface ValueExpression<T> extends Serializable
   {
      public T getValue();
      public void setValue(T value);
      public String getExpressionString();
      public Class<T> getType();
      /**
       * @return the underlying Unified EL ValueExpression
       */
      public javax.el.ValueExpression toUnifiedValueExpression();
   }
   
   /**
    * A method expression - an EL expression that evaluates to
    * a method. This interface is just a genericized version of 
    * the Unified EL ValueExpression interface.
    * 
    * @author Gavin King
    *
    * @param <T> the method return type
    */
   public static interface MethodExpression<T> extends Serializable
   {
      public T invoke(Object... args);
      public String getExpressionString();
      /**
       * @return the underlying Unified EL MethodExpression
       */
      public javax.el.MethodExpression toUnifiedMethodExpression();
   }
   
   protected boolean isFacesContextActive()
   {
      return false;
   }

   /*
    * Gets the validator from the Component object (if this is a Seam
    * component, we need to use the validator for the bean class, not
    * the proxy class) or from a Model object (if it is not a Seam
    * component, there isn't any proxy).
    * 
    * @param instance the object to be validated
    * @param componentName the name of the context variable, which might be a component name
    * @return a ClassValidator object
    */
   /*private static ClassValidator getValidator(Object instance, String componentName)
   {
      if (instance==null || componentName==null )
      {
         throw new IllegalArgumentException();
      }
      Component component = Component.forName(componentName);
      return ( component==null ? Model.forClass( instance.getClass() ) : component ).getValidator();
   }*/
   
   public static Expressions instance()
   { 
       if (!Contexts.isApplicationContextActive()) {
           return new Expressions();
       } else {
           return (Expressions) Component.getInstance(Expressions.class, ScopeType.APPLICATION);
       }
   }
   
   // optimalization of REGEX
   final static String WHITESPACE_REGEX_STRING = "\\s";
   final static Pattern WHITESPACE_REGEX_PATTERN = Pattern.compile(WHITESPACE_REGEX_STRING);
   
   private static void checkELExpression(final String expression)
   {
      if (expression == null)
      {
         return;
      }
      
      final String expressionTrimmed = WHITESPACE_REGEX_PATTERN.matcher(expression).replaceAll("");
      
      for (int index = 0; blacklist.size() > index; index++)
      {
         if ( expressionTrimmed.contains(blacklist.get(index)) ) {
            throw new IllegalArgumentException("This EL expression is not allowed!");
         }
      }
      
      // for any case blacklist is not provided this is definitely not permitted
      if ( expressionTrimmed.contains(".getClass(") ||  expressionTrimmed.contains(".class.") )
      {
         throw new IllegalArgumentException("This EL expression is not allowed!");
      }
   }

}
