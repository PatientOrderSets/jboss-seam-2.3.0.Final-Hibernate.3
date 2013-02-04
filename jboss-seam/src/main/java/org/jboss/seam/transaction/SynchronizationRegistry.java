package org.jboss.seam.transaction;

import java.util.ArrayList;
import java.util.List;

import javax.transaction.Status;
import javax.transaction.Synchronization;

import org.jboss.seam.core.Events;
import org.jboss.seam.log.LogProvider;
import org.jboss.seam.log.Logging;

/**
 * A list of Synchronizations to be invoked before and after transaction
 * completion. This class is used when we can't register a synchronization
 * directly with JTA.
 * 
 * @author Gavin King
 *
 */
class SynchronizationRegistry
{
   private static final LogProvider log = Logging.getLogProvider(SynchronizationRegistry.class);

   private List<Synchronization> synchronizations = new ArrayList<Synchronization>();

   void registerSynchronization(Synchronization sync)
   {
      synchronizations.add(sync);
   }
   
   void afterTransactionCompletion(boolean success)
   {
      if ( Events.exists() ) 
      {
         Events.instance().raiseEvent("org.jboss.seam.afterTransactionCompletion", success);
      }
      for (Synchronization sync: synchronizations)
      {
         try
         {
            sync.afterCompletion(success ? Status.STATUS_COMMITTED : Status.STATUS_ROLLEDBACK);
         }
         catch (Exception e)
         {
            log.error("Exception processing transaction Synchronization after completion", e);
         }
      }
      synchronizations.clear();
   }

   void beforeTransactionCompletion()
   {
      if ( Events.exists() )
      {
         Events.instance().raiseEvent("org.jboss.seam.beforeTransactionCompletion");
      }
      for (Synchronization sync: synchronizations)
      {
         try
         {
            sync.beforeCompletion();
         }
         catch (Exception e)
         {
            log.error("Exception processing transaction Synchronization before completion", e);
         }
      }
   }
   
}
