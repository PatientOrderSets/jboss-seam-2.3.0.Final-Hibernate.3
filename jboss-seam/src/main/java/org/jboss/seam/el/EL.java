package org.jboss.seam.el;

import java.util.Locale;

import javax.el.ArrayELResolver;
import javax.el.BeanELResolver;
import javax.el.CompositeELResolver;
import javax.el.ELContext;
import javax.el.ELResolver;
import javax.el.ExpressionFactory;
import javax.el.FunctionMapper;
import javax.el.ListELResolver;
import javax.el.MapELResolver;
import javax.el.ResourceBundleELResolver;
import javax.el.VariableMapper;

import org.jboss.el.ExpressionFactoryImpl;
import org.jboss.el.lang.FunctionMapperImpl;
import org.jboss.el.lang.VariableMapperImpl;

/**
 * An instance of JBoss EL.
 * 
 * @author Gavin King
 *
 */
public class EL
{
   public static final ELResolver EL_RESOLVER = createELResolver();
   //ELContext instances should not be shared between threads
   //public static final ELContext EL_CONTEXT = createELContext( EL_RESOLVER, new FunctionMapperImpl() );
   
   public static final ExpressionFactory EXPRESSION_FACTORY = new ExpressionFactoryImpl();
   
   private static ELResolver createELResolver()
   {
      CompositeELResolver resolver = new CompositeELResolver();
      resolver.add( new SeamELResolver() );
      resolver.add( new MapELResolver() );
      resolver.add( new ListELResolver() );
      resolver.add( new ArrayELResolver() );
      resolver.add( new ResourceBundleELResolver() );
      resolver.add( new BeanELResolver() );
      return resolver;
   }

   public static ELContext createELContext() {
       return createELContext( EL_RESOLVER, new FunctionMapperImpl() );
   }
   
   public static ELContext createELContext(final ELResolver resolver, final FunctionMapper functionMapper)
   {
      return new ELContext()
      {
         final VariableMapperImpl variableMapper = new VariableMapperImpl();

         @Override
         public ELResolver getELResolver()
         {
            return resolver;
         }

         @Override
         public FunctionMapper getFunctionMapper()
         {
            return functionMapper;
         }

         @Override
         public VariableMapper getVariableMapper()
         {
            return variableMapper;
         }
         
      };
   }
   
   public static ELContext createELContext(final ELContext context, final ELResolver resolver)
   {
      return new ELContext()
      {

         @Override
         public Locale getLocale()
         {
            return context.getLocale();
         }
         
         @Override
         public void setPropertyResolved(boolean value)
         {
            super.setPropertyResolved(value);
            context.setPropertyResolved(value);
         }
         
         /*@Override
         public boolean isPropertyResolved()
         {
            return super.isPropertyResolved();
         }*/
         
         @Override
         public void putContext(Class clazz, Object object)
         {
            super.putContext(clazz, object);
            context.putContext(clazz, object);
         }
         
         @Override
         public Object getContext(Class clazz)
         {
            return context.getContext(clazz);
         }
         
         @Override
         public void setLocale(Locale locale)
         {
            super.setLocale(locale);
            context.setLocale(locale);
         }
         
         @Override
         public ELResolver getELResolver()
         {
            return resolver;
         }

         @Override
         public FunctionMapper getFunctionMapper()
         {
            return context.getFunctionMapper();
         }

         @Override
         public VariableMapper getVariableMapper()
         {
            return context.getVariableMapper();
         }
         
      };
   }
   
}
