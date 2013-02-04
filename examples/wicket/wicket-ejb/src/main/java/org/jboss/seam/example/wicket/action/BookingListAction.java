//$Id: BookingListAction.java,v 1.23 2007/06/27 00:06:49 gavin Exp $
package org.jboss.seam.example.wicket.action;

import static javax.ejb.TransactionAttributeType.REQUIRES_NEW;
import static org.jboss.seam.ScopeType.SESSION;

import java.io.Serializable;
import java.util.List;

import javax.ejb.Remove;
import javax.ejb.Stateful;
import javax.ejb.TransactionAttribute;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.jboss.seam.annotations.Factory;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Logger;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Observer;
import org.jboss.seam.annotations.Out;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.security.Restrict;
import org.jboss.seam.faces.FacesMessages;
import org.jboss.seam.international.StatusMessages;
import org.jboss.seam.log.Log;

@Stateful
@Scope(SESSION)
@Name("bookingList")
@Restrict("#{identity.loggedIn}")
@TransactionAttribute(REQUIRES_NEW)
public class BookingListAction implements BookingList, Serializable
{
   private static final long serialVersionUID = 1L;
   
   @PersistenceContext
   private EntityManager em;
   
   @In
   private User user;
   
   @SuppressWarnings("unused")
   @Out
   private List<Booking> bookings;
   
   private Booking booking;
   
   @Logger 
   private Log log;
   
   @Factory
   @Observer("bookingConfirmed")
   public void getBookings()
   {
      bookings = em.createQuery("select b from Booking b where b.user.username = :username order by b.checkinDate")
            .setParameter("username", user.getUsername())
            .getResultList();
   }
   
   public void cancel()
   {
      log.info("Cancel booking: #{bookingList.booking.id} for #{user.username}");
      Booking cancelled = em.find(Booking.class, booking.getId());
      if (cancelled!=null) em.remove( cancelled );
      getBookings();
      StatusMessages.instance().addToControl("body", "Booking cancelled for confirmation number #0", booking.getId());
   }
   
   // TODO replace this with @Out in Wicket
   public void cancel(Booking booking)
   {
      this.booking = booking;
      cancel();
   }
   
   public Booking getBooking()
   {
      return booking;
   }
   
   @Remove
   public void destroy() {}
}
