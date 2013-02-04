package org.jboss.seam.cache;

import static org.jboss.seam.ScopeType.APPLICATION;
import static org.jboss.seam.annotations.Install.BUILT_IN;

import org.jboss.cache.CacheException;
import org.jboss.cache.Node;
import org.jboss.cache.PropertyConfigurator;
import org.jboss.cache.TreeCache;
import org.jboss.seam.annotations.AutoCreate;
import org.jboss.seam.annotations.Create;
import org.jboss.seam.annotations.Destroy;
import org.jboss.seam.annotations.Install;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.intercept.BypassInterceptors;
import org.jboss.seam.log.LogProvider;
import org.jboss.seam.log.Logging;

/**
 * Implementation of CacheProvider backed by JBoss Cache 1.x
 * 
 * @author Sebastian Hennebrueder
 * @author Pete Muir
 */

@Name("org.jboss.seam.cache.cacheProvider")
@Scope(APPLICATION)
@BypassInterceptors
@Install(precedence = BUILT_IN, classDependencies={"org.jboss.cache.TreeCache", "org.jgroups.MembershipListener"})
@AutoCreate
public class JbossCacheProvider extends AbstractJBossCacheProvider<TreeCache>
{

   private TreeCache cache;

   private static final LogProvider log = Logging.getLogProvider(JbossCacheProvider.class);

   @Create
   public void create()
   {
      log.debug("Starting JBoss Treecache 1.x");

      try
      {
         cache = new TreeCache();
         new PropertyConfigurator().configure(cache, getConfigurationAsStream());
         cache.createService();
         cache.startService();

      }
      catch (Exception e)
      {
         throw new IllegalStateException("Error starting JBoss Treecache 1.x", e);
      }
   }

   @Destroy
   public void destroy()
   {
      log.debug("Stopping JBoss Treecache 1.x");

      try
      {
         cache.stopService();
         cache.destroyService();
      }
      catch (RuntimeException e)
      {
         throw new IllegalStateException("Error stopping JBoss Treecache 1.x", e);
      }
      cache = null;
   }

   @Override
   public Object get(String region, String key)
   {
      try
      {
         Node node = cache.get(getFqn(region));
         if (node != null)
         {
            return node.get(key);
         }
         else
         {
            return null;
         }
      }
      catch (CacheException e)
      {
         throw new IllegalStateException(String.format("Cache throw exception when trying to get %s from region %s.", key, region), e);
      }
   }

   @Override
   public void put(String region, String key, Object object)
   {
      try
      {
         cache.put(getFqn(region), key, object);
      }
      catch (CacheException e)
      {
         throw new IllegalStateException(String.format("JBoss Cache throw exception when adding object for key %s to region %s", key, region), e);
      }
   }

   @Override
   public void remove(String region, String key)
   {
      try
      {
         cache.remove(getFqn(region), key);
      }
      catch (CacheException e)
      {
         throw new IllegalStateException(String.format("JBoss Cache throw exception when removing object for key %s in region %s", key, region), e);
      }

   }

   @Override
   public TreeCache getDelegate()
   {
      return cache;
   }

   @Override
   public void clear()
   {
      try
      {
         cache.remove(getFqn(null));
      }
      catch (CacheException e)
      {
         throw new IllegalStateException(String.format("JBoss Cache throw exception when clearing default cache."), e);
      }
   }

}