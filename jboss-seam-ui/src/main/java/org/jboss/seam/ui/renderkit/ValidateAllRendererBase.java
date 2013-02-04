package org.jboss.seam.ui.renderkit;

import java.io.IOException;
import java.util.List;

import javax.faces.component.EditableValueHolder;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.context.ResponseWriter;

import org.jboss.seam.ui.component.UIValidateAll;
import org.jboss.seam.ui.util.cdk.RendererBase;
import org.jboss.seam.ui.validator.ModelValidator;
import org.richfaces.cdk.annotations.JsfRenderer;

@JsfRenderer(type="org.jboss.seam.ui.ValidateAllRenderer", family="org.jboss.seam.ui.ValidateAllRenderer")
public class ValidateAllRendererBase extends RendererBase
{

   @Override
   protected Class getComponentClass()
   {
      return UIValidateAll.class;
   }
   
   @Override
   protected void doEncodeChildren(ResponseWriter writer, FacesContext context, UIComponent component) throws IOException
   {
      UIValidateAll validateAll = (UIValidateAll) component;
      if (!validateAll.isValidatorsAdded())
      {
         addValidators(validateAll.getChildren());
         validateAll.setValidatorsAdded(true);
      }
      renderChildren(context, component);
   }
   
   private void addValidators(List children)
   {
      for (Object child: children)
      {
         if (child instanceof EditableValueHolder)
         {
            EditableValueHolder evh =  (EditableValueHolder) child;
            if ( evh.getValidators().length==0 )
            {
               evh.addValidator( new ModelValidator() );
            }
         }
         addValidators( ( (UIComponent) child ).getChildren() );
      }
   }
   
   @Override
   public boolean getRendersChildren()
   {
      return true;
   }


}
