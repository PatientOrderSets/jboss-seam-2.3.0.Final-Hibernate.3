package org.jboss.seam.test.integration;

import javax.ejb.ActivationConfigProperty;
import javax.ejb.MessageDriven;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.TextMessage;

import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.test.integration.MessagingTest.SimpleReference;

@MessageDriven(activationConfig =
{
      @ActivationConfigProperty(propertyName = "destinationType", propertyValue = "javax.jms.Queue"),
      @ActivationConfigProperty(propertyName = "destination", propertyValue = "queue/seamTest")
})
@Name("testQueueListener")
public class TestQueueListener implements MessageListener
{
   @In
   private SimpleReference<String> testMessage;

   public void onMessage(Message msg)
   {
      try
      {
         testMessage.setValue(((TextMessage) msg).getText());
      }
      catch (Exception e)
      {
         e.printStackTrace();
      }
   }
}