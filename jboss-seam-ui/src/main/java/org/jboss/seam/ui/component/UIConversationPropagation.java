/**
 * License Agreement.
 *
 * Ajax4jsf 1.1 - Natural Ajax for Java Server Faces (JSF)
 *
 * Copyright (C) 2007 Exadel, Inc.
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License version 2.1 as published by the Free Software Foundation.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301  USA
 */

package org.jboss.seam.ui.component;

import javax.faces.component.UIParameter;
import javax.faces.context.FacesContext;

import org.richfaces.cdk.annotations.Attribute;
import org.richfaces.cdk.annotations.Description;
import org.richfaces.cdk.annotations.JsfComponent;
import org.richfaces.cdk.annotations.JsfRenderer;
import org.richfaces.cdk.annotations.Tag;

/**
 * JSF component class
 *
 */
@JsfComponent(description=@Description(displayName="org.jboss.seam.ui.ConversationPropagation",value="Customize the conversation propagation for a command link or button (or similar JSF control)."),
family="org.jboss.seam.ui.ConversationPropagation", type="org.jboss.seam.ui.ConversationPropagation",generate="org.jboss.seam.ui.component.html.HtmlConversationPropagation", 
tag = @Tag(baseClass="org.jboss.seam.ui.util.cdk.UIComponentTagBase", name="conversationPropagation"), 
renderer = @JsfRenderer(type="org.jboss.seam.ui.ConversationPropagationRenderer", family="org.jboss.seam.ui.ConversationPropagationRenderer"),
attributes = {"conversationPropagation.xml" })
public abstract class UIConversationPropagation extends UIParameter {
	
	private static final String COMPONENT_TYPE = "org.jboss.seam.ui.ConversationPropagation";
   
   @Override
   public String getName()
   {
      return "conversationPropagation";
   }

   @Override
   public Object getValue()
   {
      return getPageflow()==null ? getType() : getType() + "." + getPageflow();
   }

   @Attribute
   public abstract String getPageflow();

   public abstract void setPageflow(String pageflow);

   @Attribute
   public abstract String getType();

   public abstract void setType(String type);
   
   public static UIConversationPropagation newInstance() {
      return (UIConversationPropagation) FacesContext.getCurrentInstance().getApplication().createComponent(COMPONENT_TYPE);
   }
   
}
