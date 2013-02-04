//$Id: DroolsNumberGuessTest.java 6415 2007-10-07 22:27:57Z pmuir $
package org.jboss.seam.example.numberguess.test;

import java.io.File;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.OverProtocol;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.seam.core.Manager;
import org.jboss.seam.example.numberguess.Game;
import org.jboss.seam.example.numberguess.Guess;
import org.jboss.seam.example.numberguess.RandomNumber;
import org.jboss.seam.mock.JUnitSeamTest;
import org.jboss.seam.pageflow.Pageflow;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.importer.ZipImporter;
import org.jboss.shrinkwrap.api.spec.EnterpriseArchive;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(Arquillian.class)
public class DroolsNumberGuessTest extends JUnitSeamTest
{
   
   private int guessedValue;
   
   
   @Deployment(name="RenderTest")
   @OverProtocol("Servlet 3.0")
   public static Archive<?> createDeployment()
   {
       EnterpriseArchive er = ShrinkWrap.create(ZipImporter.class, "seam-drools.ear").importFrom(new File("../drools-ear/target/seam-drools.ear"))
           .as(EnterpriseArchive.class);
       WebArchive web = er.getAsType(WebArchive.class, "drools-web.war");
       web.addClasses(DroolsNumberGuessTest.class);
       
       // Install org.jboss.seam.mock.MockSeamListener
       web.delete("/WEB-INF/web.xml");
       web.addAsWebInfResource("web.xml");

       return er;
   }
   
   @Test
   public void testNumberGuessWin() throws Exception
   {
      String id = new NonFacesRequest("/numberGuess.jsp")
      {

         @Override
         protected void renderResponse() throws Exception {
            Game game = (Game) getInstance(Game.class);
            Guess guess = (Guess) getInstance(Guess.class);
            assert game.getBiggest()==100;
            assert game.getSmallest()==1;
            assert guess.getValue()==null;
            assert game.getGuessCount()==0;
            assert Manager.instance().isLongRunningConversation();
            assert Pageflow.instance().getProcessInstance().getRootToken()
            .getNode().getName().equals("displayGuess");
         }
         
      }.run();

      String id2 = new FacesRequest("/numberGuess.jsp", id)
      {

         @Override
         protected void applyRequestValues() throws Exception {
            Guess guess = (Guess) getInstance(Guess.class); 
            guessedValue = getRandomNumber() > 50 ? 25 : 75;
            guess.setValue(guessedValue);
         }

         @Override
         protected void invokeApplication() throws Exception {
            setOutcome("guess");
            //ng.guess();
         }
         
         @Override
         protected void afterRequest() {
            assert !isRenderResponseBegun();
            assert getViewId().equals("/numberGuess.jsp");
         }
         
      }.run();
      
      assert id2.equals(id);
      
      new NonFacesRequest("/numberGuess.jsp", id)
      {
         
         @Override
         protected void renderResponse() throws Exception {
            Game game = (Game) getInstance(Game.class);
            Guess guess = (Guess) getInstance(Guess.class);
            assert ( guessedValue > getRandomNumber() && game.getBiggest()==guessedValue-1 ) 
                  || ( guessedValue < getRandomNumber() && game.getSmallest()==guessedValue+1 );
            assert guess.getValue().equals(guessedValue);
            assert game.getGuessCount()==1;
            assert Manager.instance().isLongRunningConversation();
            assert Pageflow.instance().getProcessInstance().getRootToken()
                  .getNode().getName().equals("displayGuess");
         }
         
      }.run();

      id2 = new FacesRequest("/numberGuess.jsp", id)
      {

         @Override
         protected void applyRequestValues() throws Exception {
            Guess guess = (Guess) getInstance(Guess.class);
            guessedValue = getRandomNumber();
            guess.setValue(guessedValue);
         }

         @Override
         protected void invokeApplication() throws Exception {
             Guess guess = (Guess) getInstance(Guess.class);
            setOutcome("guess");
            assert guess.getValue().equals(getRandomNumber());
            assert Pageflow.instance().getProcessInstance().getRootToken()
            .getNode().getName().equals("displayGuess");
            //ng.guess();
         }
         
         @Override
         protected void afterRequest()
         {
            assert !isRenderResponseBegun();
            assert getViewId().equals("/win.jsp");
         }
         
      }.run();
      
      assert id2.equals(id);
      
      new NonFacesRequest("/win.jsp", id)
      {
         @Override
         protected void renderResponse() throws Exception {
            Game game = (Game) getInstance(Game.class);
            Guess guess = (Guess) getInstance(Guess.class);
            assert guess.getValue().equals(getRandomNumber());
            assert game.getGuessCount()==2;
            assert !Manager.instance().isLongRunningConversation();
            assert Pageflow.instance().getProcessInstance().getRootToken()
                  .getNode().getName().equals("win");
         }
         
      }.run();

   }
   
   @Test
   public void testNumberGuessLose() throws Exception
   {
      String id = new NonFacesRequest("/numberGuess.jsp")
      {

         @Override
         protected void renderResponse() throws Exception {
            Game game = (Game) getInstance(Game.class);
            Guess guess = (Guess) getInstance(Guess.class);
            assert game.getBiggest()==100;
            assert game.getSmallest()==1;
            assert guess.getValue()==null;
            assert game.getGuessCount()==0;
            assert Manager.instance().isLongRunningConversation();
         }
         
      }.run();
      
      for (int i=1; i<=9; i++)
      {
         
         final int count = i;

         new FacesRequest("/numberGuess.jsp", id)
         {
   
            @Override
            protected void applyRequestValues() throws Exception {
               Guess guess = (Guess) getInstance(Guess.class);
               guessedValue = getRandomNumber() > 50 ? 25+count : 75-count;
               guess.setValue(guessedValue);
            }
   
            @Override
            protected void invokeApplication() throws Exception {
               setOutcome("guess");
               //ng.guess();
               //assert Pageflow.instance().getProcessInstance().getRootToken()
//                     .getNode().getName().equals("displayGuess");
            }
            
            @Override
            protected void afterRequest()
            {
               assert !isRenderResponseBegun();
               assert getViewId().equals("/numberGuess.jsp");
            }
            
         }.run();
         
         new NonFacesRequest("/numberGuess.jsp", id)
         {
   
            @Override
            protected void renderResponse() throws Exception {
               Game game = (Game) getInstance(Game.class);
               Guess guess = (Guess) getInstance(Guess.class);
               assert ( guessedValue > getRandomNumber() && game.getBiggest()==guessedValue-1 ) 
                     || ( guessedValue < getRandomNumber() && game.getSmallest()==guessedValue+1 );
               assert guess.getValue().equals(guessedValue);
               assert game.getGuessCount()==count;
               assert Manager.instance().isLongRunningConversation();
               assert Pageflow.instance().getProcessInstance().getRootToken()
                     .getNode().getName().equals("displayGuess");
            }
            
         }.run();
      
      }

      new FacesRequest("/numberGuess.jsp", id)
      {

         @Override
         protected void applyRequestValues() throws Exception {
            Guess guess = (Guess) getInstance(Guess.class);
            guessedValue = getRandomNumber() > 50 ? 49 : 51;
            guess.setValue(guessedValue);
         }

         @Override
         protected void invokeApplication() throws Exception {
            setOutcome("guess");
            //ng.guess();
            assert Pageflow.instance().getProcessInstance().getRootToken()
                  .getNode().getName().equals("displayGuess");
         }

         @Override
         protected void afterRequest()
         {
            assert !isRenderResponseBegun();
            assert getViewId().equals("/lose.jsp");
         }
         
      }.run();

      new NonFacesRequest("/lose.jsp", id)
      {

         @Override
         protected void renderResponse() throws Exception {
            Guess guess = (Guess) getInstance(Guess.class);
            Game game = (Game) getInstance(Game.class);
            assert ( guessedValue > getRandomNumber() && game.getBiggest()==guessedValue-1 ) 
                  || ( guessedValue < getRandomNumber() && game.getSmallest()==guessedValue+1 );
            assert guess.getValue().equals(guessedValue);
            assert game.getGuessCount()==10;
            assert !Manager.instance().isLongRunningConversation();
            assert Pageflow.instance().getProcessInstance().getRootToken()
                  .getNode().getName().equals("lose");
         }
         
      }.run();

   }
   
   private Integer getRandomNumber()
   {
       return (Integer) getInstance(RandomNumber.class);
   }
   
}
