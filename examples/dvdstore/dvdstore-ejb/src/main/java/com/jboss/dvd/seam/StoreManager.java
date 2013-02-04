/*
 * JBoss, Home of Professional Open Source
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */ 
package com.jboss.dvd.seam;

import java.math.BigDecimal;
import javax.ejb.Local;

@Local
public interface StoreManager
{  
    public long getNumberOrders();
    public long getUnitsSold();
    public long getTotalInventory();
    public BigDecimal getTotalSales();
}
