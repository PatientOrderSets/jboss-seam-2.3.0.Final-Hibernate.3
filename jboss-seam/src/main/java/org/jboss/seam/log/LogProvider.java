package org.jboss.seam.log;

/**
 * 
 * Abstracts the underlying logging implementation
 * (log4j or JDK logging). This saves us from having
 * to use evil and absurdly overengineered stuff
 * like commons-logging.
 * 
 * @author Gavin King
 *
 */
public interface LogProvider
{
   public void trace(Object object);
   public void trace(Object object, Throwable t);
   public void debug(Object object);
   public void debug(Object object, Throwable t);
   public void info(Object object);
   public void info(Object object, Throwable t);
   public void warn(Object object);
   public void warn(Object object, Throwable t);
   public void error(Object object);
   public void error(Object object, Throwable t);
   public void fatal(Object object);
   public void fatal(Object object, Throwable t);
   public boolean isTraceEnabled();
   public boolean isDebugEnabled();
   public boolean isInfoEnabled();
   public boolean isWarnEnabled();
   public boolean isErrorEnabled();
   public boolean isFatalEnabled();
}
