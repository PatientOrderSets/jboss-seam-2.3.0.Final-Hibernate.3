package org.jboss.seam.example.restbay;

import org.jboss.seam.Component;
import static org.jboss.seam.ScopeType.APPLICATION;
import org.jboss.seam.annotations.Create;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.Startup;
import org.jboss.seam.annotations.intercept.BypassInterceptors;

import javax.naming.InitialContext;
import javax.persistence.EntityManager;
import javax.transaction.SystemException;
import javax.transaction.UserTransaction;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Random;

/**
 * Nasty nasty hack because hsqldb doesn't support date arithmetic and we actually
 * want to deploy with usable test data.
 * 
 * @author shane
 *
 */
@Startup
@Name("aargh")
@Scope(APPLICATION)
@BypassInterceptors
public class Aargh
{   

   @Create
   public void create()
   {
      UserTransaction t = null;
      try
      {
         InitialContext ctx = new InitialContext();
         
         t = (UserTransaction) ctx.lookup("java:/jboss/UserTransaction");
         t.begin();
      
         EntityManager em = (EntityManager) Component.getInstance("entityManager", true);
         
         List<Auction> auctions = em.createQuery("select a from Auction a").getResultList();
         
         Calendar cal = new GregorianCalendar();
         
         Random r = new Random(System.currentTimeMillis());
         
         for (Auction auction : auctions)
         {
            cal.setTime(auction.getEndDate());
            cal.add(Calendar.DATE, r.nextInt(7));
            cal.add(Calendar.MINUTE, 30 + r.nextInt(1410));
            auction.setEndDate(cal.getTime());
            auction.setStatus(Auction.STATUS_COMPLETED);
            em.merge(auction);

            AuctionEndAction auctionEnd = (AuctionEndAction) Component.getInstance(AuctionEndAction.class, true);
            auctionEnd.endAuction(auction.getAuctionId(), auction.getEndDate());
         }
         
         t.commit();
      } 
      catch (Exception e)
      {
         try
         {
            if (t != null)
               t.rollback();
         } 
         catch (SystemException e1) {}
         
         throw new RuntimeException("Error starting transaction", e);
      }      
   }
}
