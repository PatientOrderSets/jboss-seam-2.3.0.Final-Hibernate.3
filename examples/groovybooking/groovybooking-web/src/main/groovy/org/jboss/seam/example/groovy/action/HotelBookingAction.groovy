//$Id: HotelBookingAction.groovy 5563 2007-06-26 22:20:03Z gavin $
package org.jboss.seam.example.groovy.action

import javax.persistence.EntityManager

import org.jboss.seam.ScopeType
import org.jboss.seam.annotations.Begin
import org.jboss.seam.annotations.End
import org.jboss.seam.annotations.In
import org.jboss.seam.annotations.Logger
import org.jboss.seam.annotations.Name
import org.jboss.seam.annotations.Out
import org.jboss.seam.core.Events
import org.jboss.seam.faces.FacesMessages
import org.jboss.seam.log.Log
import org.jboss.seam.example.groovy.model.Booking
import org.jboss.seam.example.groovy.model.Hotel
import org.jboss.seam.example.groovy.model.User
import org.jboss.seam.annotations.security.Restrict

@Name("hotelBooking")
@Restrict("#{identity.loggedIn}")
class HotelBookingAction
{

   @In
   EntityManager em

   @In
   User user

   @In(required=false) @Out
   Hotel hotel

   @In(required=false)
   @Out(required=false)
   Booking booking

   @In
   FacesMessages facesMessages

   @In
   Events events

   @Logger
   Log log

   boolean bookingValid

   @Begin
   void selectHotel(Hotel selectedHotel)
   {
      hotel = em.merge(selectedHotel)
   }

   void bookHotel()
   {
      booking = new Booking(hotel, user)	
      Calendar calendar = Calendar.getInstance()
      booking.checkinDate = calendar.time
      calendar.add Calendar.DAY_OF_MONTH, 1
      booking.checkoutDate = calendar.time
   }

   void setBookingDetails()
   {
      Calendar calendar = Calendar.getInstance()
      calendar.add Calendar.DAY_OF_MONTH, -1
      if ( booking.checkinDate < calendar.time )
      {
         facesMessages.addToControl "checkinDate", "Check in date must be a future date"
         bookingValid=false
      }
      else if ( booking.checkinDate >= booking.checkoutDate )
      {
         facesMessages.addToControl "checkoutDate", "Check out date must be later than check in date"
         bookingValid=false
      }
      else
      {
         bookingValid=true
      }
   }

   @Out (required=false, scope=ScopeType.SESSION)
   List <Booking> bookings

   @End
   void confirm()
   {
      em.persist(booking)
      facesMessages.add "Thank you, #{user.name}, your confimation number for #{hotel.name} is #{booking.id}", new Object[0]
      log.info("New booking: #{booking.id} for #{user.username}")
      //events.raiseTransactionSuccessEvent("bookingConfirmed")

      // force refresh
      bookings = null
   }

   @End
   void cancel() {}

}
