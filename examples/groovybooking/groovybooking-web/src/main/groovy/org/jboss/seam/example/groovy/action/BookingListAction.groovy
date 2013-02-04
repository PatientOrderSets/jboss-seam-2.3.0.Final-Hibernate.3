//$Id: BookingListAction.groovy 8748 2008-08-20 12:08:30Z pete.muir@jboss.org $
package org.jboss.seam.example.groovy.action

import javax.persistence.EntityManager

import org.jboss.seam.ScopeType
import org.jboss.seam.annotations.Factory
import org.jboss.seam.annotations.In
import org.jboss.seam.annotations.Logger
import org.jboss.seam.annotations.Name
import org.jboss.seam.annotations.Scope
import org.jboss.seam.annotations.Observer;
import org.jboss.seam.annotations.datamodel.DataModel
import org.jboss.seam.annotations.datamodel.DataModelSelection
import org.jboss.seam.faces.FacesMessages
import org.jboss.seam.log.Log
import org.jboss.seam.example.groovy.model.Booking
import org.jboss.seam.example.groovy.model.User
import org.jboss.seam.annotations.security.Restrict

@Scope(ScopeType.SESSION)
@Restrict("#{identity.loggedIn}")
@Name("bookingList")
class BookingListAction implements Serializable
{

   @In
   EntityManager em

   @In
   User user

   @DataModel
   private List<Booking> bookings

   @DataModelSelection
   Booking booking

   @Logger
   Log log

   @Factory
   public void getBookings()
   {
      bookings = em.createQuery('''
         select b from Booking b
         where b.user.username = :username
         order by b.checkinDate''')
            .setParameter("username", user.username)
            .getResultList()
   }

   public void cancel()
   {
      log.info("Cancel booking: #{bookingList.booking.id} for #{user.username}")
      Booking cancelled = em.find(Booking.class, booking.id)
      if (cancelled != null) em.remove( cancelled )
      getBookings()
      FacesMessages.instance().add("Booking cancelled for confirmation number #0", booking.getId())
   }

    public Booking getBooking()
    {
        return booking
    }

}
