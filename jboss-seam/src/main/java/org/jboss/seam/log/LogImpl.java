package org.jboss.seam.log;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

import org.jboss.seam.core.Interpolator;

/**
 * Implementation of the Log interface using commons logging.
 * 
 * @author Gavin King
 */
class LogImpl implements Log, Externalizable
{
   private static final long serialVersionUID = -1664298172030714342L;
   
   private transient LogProvider log;
   private String category;
   
   public LogImpl() {} //for Externalizable
   
   LogImpl(String category)
   {
      this.category = category;
      this.log = Logging.getLogProvider(category, true);
   }
   
   public boolean isDebugEnabled()
   {
      return log.isDebugEnabled();
   }
   public boolean isErrorEnabled()
   {
      return log.isErrorEnabled();
   }
   public boolean isFatalEnabled()
   {
      return log.isFatalEnabled();
   }
   public boolean isInfoEnabled()
   {
      return log.isInfoEnabled();
   }
   public boolean isTraceEnabled()
   {
      return log.isTraceEnabled();
   }
   public boolean isWarnEnabled()
   {
      return log.isWarnEnabled();
   }
   public void trace(Object object, Object... params)
   {
      if ( isTraceEnabled() )
      {
         log.trace(  interpolate(object, params) );
      }
   }
   public void trace(Object object, Throwable t, Object... params)
   {
      if ( isTraceEnabled() )
      {
         log.trace(  interpolate(object, params), t );
      }
   }
   public void debug(Object object, Object... params)
   {
      if ( isDebugEnabled() )
      {
         log.debug(  interpolate(object, params) );
      }
   }
   public void debug(Object object, Throwable t, Object... params)
   {
      if ( isDebugEnabled() )
      {
         log.debug(  interpolate(object, params), t );
      }
   }
   public void info(Object object, Object... params)
   {
      if ( isInfoEnabled() )
      {
         log.info( interpolate(object, params) );
      }
   }
   public void info(Object object, Throwable t, Object... params)
   {
      if ( isInfoEnabled() )
      {
         log.info( interpolate(object, params), t );
      }
   }
   public void warn(Object object, Object... params)
   {
      if ( isWarnEnabled() )
      {
         log.warn( interpolate(object, params) );
      }
   }
   public void warn(Object object, Throwable t, Object... params)
   {
      if ( isWarnEnabled() )
      {
         log.warn( interpolate(object, params), t );
      }
   }
   public void error(Object object, Object... params)
   {
      if ( isErrorEnabled() )
      {
         log.error( interpolate(object, params) );
      }
   }
   public void error(Object object, Throwable t, Object... params)
   {
      if ( isErrorEnabled() )
      {
         log.error( interpolate(object, params), t );
      }
   }
   public void fatal(Object object, Object... params)
   {
      if ( isFatalEnabled() )
      {
         log.fatal( interpolate(object, params) );
      }
   }
   public void fatal(Object object, Throwable t, Object... params)
   {
      if ( isFatalEnabled() )
      {
         log.fatal( interpolate(object, params), t );
      }
   }
   
   @SuppressWarnings("finally")
   private Object interpolate(Object object, Object... params)
   {
      if (object instanceof String)
      {
         try {
            object = Interpolator.instance().interpolate( (String) object, params );
         } catch (Exception e) {
            log.error("exception interpolating string: " + object, e);
         } finally {
            return object;
         }
         
      }
      else
      {
         return object;
      }
   }
   
   public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException
   {
      category = (String) in.readObject();
      log = Logging.getLogProvider(category, true);
   }
   
   public void writeExternal(ObjectOutput out) throws IOException
   {
      out.writeObject(category);
   }
   
   /*void readObject(ObjectInputStream ois) 
         throws ClassNotFoundException, IOException
   {
      ois.defaultReadObject();
      log = LogFactory.getLog(category);
   }*/     
}
