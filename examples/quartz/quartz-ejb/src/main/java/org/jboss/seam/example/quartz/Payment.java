package org.jboss.seam.example.quartz;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.persistence.ManyToOne;

import javax.validation.constraints.Size;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Digits;
import org.jboss.seam.async.QuartzTriggerHandle;

@Entity
public class Payment
    implements Serializable
{
    @Id @GeneratedValue 
    private Long id;

    @NotNull
    @Digits(integer=8,fraction=2)
    private BigDecimal amount;

    @NotNull @Size(min=1)
    private String payee;

    @NotNull @ManyToOne
    private Account account;
   
    @NotNull
    private Date paymentDate;
    @NotNull
    private Date createdDate;
    private Date lastPaid;

    private boolean active = true;

    private String paymentCron;
    private Date paymentEndDate;

    private Frequency paymentFrequency = Frequency.DAILY;

    @Lob
    private QuartzTriggerHandle quartzTriggerHandle;
    
    public Long getId() {
        return id;
    }
    
    public BigDecimal getAmount() {
        return amount;
    }
    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public String getPayee()
    {
        return payee;
    }

    public void setPayee(String payee) {
        this.payee = payee;
    }

    public Account getAccount() {
        return account;
    }
    
    public void setAccount(Account account) {
        this.account = account;
        account.getPayments().add(this);
    }

    public Date getPaymentDate() {
        return paymentDate;
    }
    public void setPaymentDate(Date paymentDate) {
        this.paymentDate = paymentDate;
    }  

    public Date getPaymentEndDate() {
        return paymentEndDate;
    }
    public void setPaymentEndDate(Date paymentEndDate) {
        this.paymentEndDate = paymentEndDate;
    }  

    public Date getCreatedDate() {
        return createdDate;
    }    
    public void setCreatedDate(Date createdDate) {
        this.createdDate = createdDate;
    }  


    public Date getLastPaid() {
        return lastPaid;
    }    
    public void setLastPaid(Date lastPaid) {
        this.lastPaid = lastPaid;
    }  
        
    public boolean getActive() {
        return active;
    }
    public void setActive(boolean active) {
        this.active = active;
    }
   
    public Frequency getPaymentFrequency() { 
        return paymentFrequency; 
    }
    public void setPaymentFrequency(Frequency paymentFrequency) {
        this.paymentFrequency = paymentFrequency;
    }

    public String getPaymentCron() { 
        return paymentCron; 
    }
    public void setPaymentCron(String paymentCron) {
        this.paymentCron = paymentCron;
    }

    public QuartzTriggerHandle getQuartzTriggerHandle() {
        return quartzTriggerHandle;
    }
    public void setQuartzTriggerHandle(QuartzTriggerHandle quartzTriggerHandle) {
        this.quartzTriggerHandle = quartzTriggerHandle;
    }

    public enum Frequency {
        ONCE(null),
        EVERY_SECOND(1000l), // Just for testing
        EVERY_MINUTE(60*1000l),
        HOURLY(60*60*1000l), 
        DAILY(24*60*60*1000l), 
        WEEKLY(7*24*60*60*1000l);

        Long interval; 

        Frequency(Long interval) {
            this.interval = interval;
        }
        
        public Long getInterval() {
            return interval;
        }
    }
}
