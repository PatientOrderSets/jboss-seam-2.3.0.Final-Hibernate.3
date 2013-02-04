//$Id: Booking.groovy 4698 2007-04-18 06:40:06Z ebernard $
package org.jboss.seam.example.groovy.model

import java.text.DateFormat
import javax.persistence.Basic
import javax.persistence.Entity
import javax.persistence.GeneratedValue
import javax.persistence.Id
import javax.persistence.ManyToOne
import javax.persistence.Temporal
import javax.persistence.TemporalType
import javax.persistence.Transient

import org.jboss.seam.annotations.Name
import javax.validation.constraints.NotNull
import javax.validation.constraints.Pattern
import javax.validation.constraints.Size

@Entity
@Name("booking")
class Booking implements Serializable
{
   @Id @GeneratedValue
   Long id

   @ManyToOne @NotNull
   User user

   @ManyToOne @NotNull
   Hotel hotel

   @NotNull
   @Basic @Temporal(TemporalType.DATE)
   Date checkinDate

   @Basic @Temporal(TemporalType.DATE)
   @NotNull
   Date checkoutDate

   @NotNull(message="Credit card number is required")
   @Size(min=16, max=16, message="Credit card number must 16 digits long")
   @Pattern(regexp=/^\d*$/, message="Credit card number must be numeric")
   String creditCard

   @NotNull(message="Credit card name is required")
   @Size(min=3, max=70, message="Credit card name is required")
   String creditCardName

   int creditCardExpiryMonth

   int creditCardExpiryYear

   boolean smoking

   int beds

   Booking() {}

   Booking(Hotel hotel, User user)
   {
      this.hotel = hotel
      this.user = user
   }

   @Transient
   BigDecimal getTotal()
   {
      return hotel.price * getNights()
   }

   @Transient
   int getNights()
   {
      return (int) ( ( checkoutDate.time - checkinDate.time ) / 1000 / 60 / 60 / 24 )
   }

   @Transient
   String getDescription()
   {
      DateFormat df = DateFormat.getDateInstance(DateFormat.MEDIUM)
      return hotel ?
            "${hotel.name}, ${df.format( checkinDate )} to ${df.format(checkoutDate)}" :
            null
   }

   @Override
   String toString()
   {
      return "Booking(" + user + ","+ hotel + ")"
   }

}
