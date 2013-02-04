//$Id: BijectionInterceptor.java 9561 2008-11-14 03:26:13Z dan.j.allen $
package org.jboss.seam.core;

import java.util.concurrent.locks.ReentrantLock;

import org.jboss.seam.Component;
import org.jboss.seam.CyclicDependencyException;
import org.jboss.seam.annotations.intercept.AroundInvoke;
import org.jboss.seam.annotations.intercept.Interceptor;
import org.jboss.seam.intercept.AbstractInterceptor;
import org.jboss.seam.intercept.InvocationContext;
import org.jboss.seam.util.Exceptions;

/**
 * Before invoking the component, inject all dependencies. After
 * invoking, outject dependencies back into their context.
 * 
 * @author Gavin King
 * @author Shane Bryzak
 */
@Interceptor
public class BijectionInterceptor extends AbstractInterceptor
{
   private static final long serialVersionUID = 4686458105931528659L;
   
   private boolean injected;
   
   private boolean injecting;
   
   private int clients = 0;
   
   private ReentrantLock lock = new ReentrantLock();
   
   @Override
   public void setComponent(Component component)
   {
      super.setComponent(component);
   }
      
   @AroundInvoke
   public Object aroundInvoke(InvocationContext invocation) throws Exception
   {
      Component component = getComponent();
      boolean enforceRequired = !component.isLifecycleMethod( invocation.getMethod() );      
      
      try
      {    
         lock.lock();
         try
         {
            if (!injected)
            {              
               if (injecting)
               {
                  throw new CyclicDependencyException();
               }

               injecting = true;
               try
               {
                  component.inject(invocation.getTarget(), enforceRequired);
               }
               finally
               {
                  injecting = false;
               }
               injected = true;
            }
            
            clients++;
         }
         finally
         {
            lock.unlock();
         }
                           
         Object result = invocation.proceed();
            
         lock.lock();
         try
         {
            if (clients == 1)
            {
               try
               {                     
                  component.outject( invocation.getTarget(), enforceRequired );
               }
               finally
               {
                  // Avoid an extra lock by disinjecting here instead of the finally block
                  if (injected)
                  {
                     injected = false;
                     clients--;
                     component.disinject( invocation.getTarget() );
                  }
               }   
            }
         }
         finally
         {
            lock.unlock();
         }
         
         return result;
      }
      catch (Exception e)
      {
         Exception root = e;
         while (Exceptions.getCause(root) != null)
         {
            root = Exceptions.getCause(root);
         }
         if (root instanceof CyclicDependencyException)
         {
            CyclicDependencyException cyclicDependencyException = (CyclicDependencyException) root;
            cyclicDependencyException.addInvocation(getComponent().getName(), invocation.getMethod());
         }
         throw e;
      }
      finally
      {            
         if (injected)
         {
            lock.lock();
            try
            {
               clients--;
               
               if (clients == 0)
               {
                  injected = false;
                  component.disinject( invocation.getTarget() );     
               }
            }
            finally
            {
               lock.unlock();
            }
         }
      }
   }

   public boolean isInterceptorEnabled()
   {
      return getComponent().needsInjection() || getComponent().needsOutjection();
   }
   
}
