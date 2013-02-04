package org.jboss.seam.test.unit;

import java.util.Date;

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.IntegerConverter;
import javax.faces.validator.Validator;
import javax.faces.validator.ValidatorException;

import javax.validation.constraints.Size;
import org.jboss.seam.contexts.Contexts;
import org.jboss.seam.core.Expressions;
import org.jboss.seam.core.Validators;
import org.jboss.seam.faces.DateConverter;
import org.jboss.seam.mock.EnhancedMockHttpServletRequest;
import org.jboss.seam.navigation.Pages;
import org.jboss.seam.navigation.Param;
import org.testng.annotations.Test;

/**
 * Verifies that page parameters are setup properly and report the correct information
 * about validators and converters.
 * 
 * @author Dan Allen
 */
public class PageParamTest extends AbstractPageTest
{
   @Test
   public void testGetConverterById()
   {
      String converterId = "javax.faces.Integer";
      String converterClass = "javax.faces.convert.IntegerConverter";
      Param param = new Param("param");
      param.setConverterId(converterId);
      assert param.getConverter() instanceof IntegerConverter : "expecting: " + converterClass + "; got: " + param.getConverter();
   }
   
   /**
    * Verify EL expression disability in actionOutcome parameter
    */
   @Test(expectedExceptions = IllegalArgumentException.class )
   public void testGetCallAction()
   {
      EnhancedMockHttpServletRequest request = new EnhancedMockHttpServletRequest();
      request.addParameter("actionOutcome", "#{variable}");
      FacesContext.getCurrentInstance().getExternalContext().setRequest(request);
      Pages.instance().preRender(FacesContext.getCurrentInstance());      
   }
   
   @Test(expectedExceptions = IllegalArgumentException.class )
   public void testGetCallActionEscaped()
   {
      EnhancedMockHttpServletRequest request = new EnhancedMockHttpServletRequest();
      request.addParameter("actionOutcome", "%3d%23%7dvariable%7b");
      FacesContext.getCurrentInstance().getExternalContext().setRequest(request);
      Pages.instance().preRender(FacesContext.getCurrentInstance());      
   }
   
   /**
    * Verify that converter is null when the parameter value is a value expression and
    * we are operating outside of a FacesContext.
    * @jira JBSEAM-3674
    */
   @Test
   public void testConverterIsNullForNonFacesValueExpression()
   {
      Param param = new Param("param");
      param.setValueExpression(Expressions.instance().createValueExpression("#{variable}"));
      assert param.getConverter() == null;
   }
   
   @Test
   public void testDateConverter()
   {
      Param param = setupParam(false);
      
      assert DateConverter.class.equals(param.getConverter().getClass());
      
   }
   
   @Test
   public void testGetValidatorById() throws ClassNotFoundException
   {
      String validatorId = "TestValidator";
      String validatorClass= "org.jboss.seam.test.unit.PageParamTest$TestValidator";
      FacesContext.getCurrentInstance().getApplication().addValidator(validatorId, validatorClass);
      Param param = new Param("param");
      param.setValidatorId(validatorId);
      assert param.getValidator() instanceof TestValidator : "expecting: " + validatorClass + "; got: " + param.getValidator();
   }
   
   @Test(expectedExceptions = ValidatorException.class)
   public void testValidateModelWithInvalidValue()
   {
      setupParamToValidate(false).validateConvertedValue(FacesContext.getCurrentInstance(), "a");
   }
   
   @Test
   public void testValidateModelWithValidValue()
   {
      setupParamToValidate(false).validateConvertedValue(FacesContext.getCurrentInstance(), "aaa");
   }
   
   @Test
   public void testValidateModelDisabled()
   {
      setupParamToValidate(true).validateConvertedValue(FacesContext.getCurrentInstance(), "a");
   }
   
   protected Param setupParamToValidate(boolean disableModelValidator)
   {
      installComponent(Contexts.getApplicationContext(), Validators.class);
      Param param = new Param("value");
      param.setValueExpression(Expressions.instance().createValueExpression("#{bean.value}"));
      Contexts.getEventContext().set("bean", new Bean());
      if (disableModelValidator) {
         param.setValidateModel(false);
      }
      return param;
   }

   protected Param setupParam(boolean disableModelValidator)
   {
      installComponent(Contexts.getApplicationContext(), DateConverter.class);
      Param param = new Param("birthDate");
      param.setValueExpression(Expressions.instance().createValueExpression("#{bean.birthDate}"));
      Bean bean = new Bean();
      bean.setBirthDate(new Date());
      Contexts.getEventContext().set("bean", bean);
      if (disableModelValidator) {
         param.setValidateModel(false);
      }
      return param;
   }
   
   public static class TestValidator implements Validator
   {
      public void validate(FacesContext context, UIComponent component, Object value) throws ValidatorException
      {
      }
   }
   
   class Bean {
      private String value;
      private Date birthDate;
      public Date getBirthDate()
      {
         return birthDate;
      }
      public void setBirthDate(Date birth)
      {
         this.birthDate = birth;
      }
      @Size(min = 3, max = 10)
      public String getValue() {
         return value;
      }
      public void setValue(String value) {
         this.value = value;
      }
   }
}
