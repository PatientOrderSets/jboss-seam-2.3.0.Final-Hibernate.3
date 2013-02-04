package org.jboss.seam.ui.graphicImage;

import java.io.IOException;

import javax.faces.component.UIComponentBase;

import org.richfaces.cdk.annotations.Attribute;
import org.richfaces.cdk.annotations.Description;
import org.richfaces.cdk.annotations.JsfComponent;
import org.richfaces.cdk.annotations.Tag;

@JsfComponent(description=@Description(displayName="org.jboss.seam.ui.graphicImage.TransformImageBlur",value="Nested in a s:graphicImage. Transform an image by applying a blur."),
family="org.jboss.seam.ui.graphicImage.TransformImageBlur", type="org.jboss.seam.ui.graphicImage.TransformImageBlur",generate="org.jboss.seam.ui.component.html.HtmlTransformImageBlur", 
tag = @Tag(baseClass="org.jboss.seam.ui.util.cdk.UIComponentTagBase", name="transformImageBlur"), 
attributes = {"transformImageBlur.xml" })
public abstract class UITransformImageBlur extends UIComponentBase implements ImageTransform
{
   
   public void applyTransform(Image image) throws IOException
   {
      if (!isRendered())
      {
         return;
      }
      image.blur(new Integer(getRadius()));
   }
   
   @Attribute
   public abstract String getRadius();
   
   public abstract void setRadius(String width);
   
  
   
}
