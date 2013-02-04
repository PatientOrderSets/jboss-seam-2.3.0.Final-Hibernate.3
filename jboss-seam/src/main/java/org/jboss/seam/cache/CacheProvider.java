package org.jboss.seam.cache;

import java.io.InputStream;

import org.jboss.seam.Component;
import org.jboss.seam.ScopeType;
import org.jboss.seam.contexts.Contexts;
import org.jboss.seam.core.ResourceLoader;

/**
 * CacheProvider provides methods to control and manipulate the configured cache
 * 
 * @author Sebastian Hennebrueder
 * @author Pete Muir
 */

public abstract class CacheProvider<T>
{

   /**
    * the region name to be used if no region is specified
    */
   public static final String DEFAULT_REGION = "org.jboss.seam.cache.DefaultRegion";
   private String configuration;
   private String defaultRegion = DEFAULT_REGION;

   /**
    * @return - the cache the cache provider delegates to
    */
   public abstract T getDelegate();

   /**
    * Get the configuration file used by the cache
    * 
    */
   public String getConfiguration()
   {
      return this.configuration;
   }

   protected InputStream getConfigurationAsStream()
   {
      return ResourceLoader.instance().getResourceAsStream(getConfiguration());
   }

   /**
    * Set the configuration file to be used by the cache
    */
   public void setConfiguration(String cfgResourceName)
   {
      this.configuration = cfgResourceName;
   }

   /**
    * Fetches an object for the given key from the cache and returns it if
    * found. Only the default cache region will be searched.
    * 
    * @param key - a key to identify the object.
    * @return - the object if found or null if not
    */
   public Object get(String key)
   {
      return get(null, key);
   }
   
   /**
    * Fetches an object for the given key from the cache and returns it if
    * found. Only the default cache region will be searched.
    * 
    * @param key - a key to identify the object.
    * @param type - the type of the object to return
    * @return - the object if found or null if not
    */
   public <E> E get(String key, E type)
   {
      return (E) get(null, key);
   }

   /**
    * Fetches an object for the given key from the cache and returns it if
    * found. Only the specified cache region will be searched.
    * 
    * @param region - the name of a cache region
    * @param key - a key to identify the object.
    * @return - the object if found or null if not
    */
   public abstract Object get(String region, String key);
   
   /**
    * Fetches an object for the given key from the cache and returns it if
    * found. Only the specified cache region will be searched.
    * 
    * @param region - the name of a cache region
    * @param key - a key to identify the object.
    * @param type - the type of object to return
    * 
    * @return - the object if found or null if not
    */
   public <E> E get(String region, String key, E type)
   {
      return (E) get(region, key);
   }

   /**
    * Put an object into the cache. The object is placed in the default cache
    * region under the given key.
    * 
    * @param key - a key to identify the object
    * @param object - the object to be stored in the cache
    */
   public void put(String key, Object object)
   {
      put(null, key, object);
   }

   /**
    * Puts an object into the cache. The object is placed in the specified cache
    * region under the given key.
    * 
    * @param region - the name of a cache region
    * @param key - a key to identify the object
    * @param object - the object to be stored in the cache
    */
   public abstract void put(String region, String key, Object object);

   /**
    * Removes an object from the cache. The object is removed from the default
    * cache region under the given key.
    * 
    * @param key - a key to identify the object
    */
   public void remove(String key)
   {
      remove(null, key);
   }

   /**
    * Removes an object from the cache. The object is removed from the specified
    * cache region under the given key.
    * 
    * @param region - the name of a cache region
    * @param key - a key to identify the object
    */
   public abstract void remove(String region, String key);

   /**
    * Removes all objects from all cache regions
    */
   public abstract void clear();

   public String getDefaultRegion()
   {
      return defaultRegion;
   }

   public void setDefaultRegion(String defaultRegion)
   {
      this.defaultRegion = defaultRegion;
   }

   public static CacheProvider instance()
   {
      if (!Contexts.isApplicationContextActive())
      {
         throw new IllegalStateException("No active application scope");
      }
      return (CacheProvider) Component.getInstance("org.jboss.seam.cache.cacheProvider", ScopeType.APPLICATION);
   }

   public static <T> CacheProvider<T> instance(Class<? extends T> type)
   {
      return instance();
   }

}
