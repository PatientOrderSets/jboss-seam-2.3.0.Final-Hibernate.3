package com.jboss.dvd.seam.test;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import javax.el.ELException;
import javax.faces.model.DataModel;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.OverProtocol;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.seam.core.Conversation;
import org.jboss.seam.mock.JUnitSeamTest;
import org.jboss.seam.security.NotLoggedInException;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.importer.ZipImporter;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Test;
import org.junit.runner.RunWith;

import com.jboss.dvd.seam.Order;
import com.jboss.dvd.seam.Order.Status;
import com.jboss.dvd.seam.Accept;
import com.jboss.dvd.seam.Product;
import com.jboss.dvd.seam.ShoppingCart;
import com.jboss.dvd.seam.User;

@RunWith(Arquillian.class)
public class OrderTest
   extends JUnitSeamTest
{
   
   @Deployment(name = "OrderTest")
   @OverProtocol("Servlet 3.0")
   public static Archive<?> createDeployment()
   {
      WebArchive web = ShrinkWrap.create(ZipImporter.class, "seam-dvdstore.war").importFrom(new File("target/seam-dvdstore.war")).as(WebArchive.class);
      web.addPackages(true, Accept.class.getPackage());

      return web;
   }
    
    @Test
    public void selectDvd() 
        throws Exception 
    {
        new FacesRequest("/dvd.xhtml") {
            @Override
            protected void  beforeRequest() {
                setParameter("id", "41");
            }

            @Override
            protected void renderResponse() throws Exception {
                Product dvd = (Product) getValue("#{dvd}");
                assert dvd != null;
                assert dvd.getProductId() == 41;                               
            }
        }.run();
    }
        
    @Test
    public void addToCart() 
        throws Exception 
    {
        String id = new FacesRequest("/dvd.xhtml") {
            @Override
            protected void beforeRequest() {
                setParameter("id", "41");
            }
        }.run();
        new FacesRequest("/dvd.xhtml", id) {
            @Override
            protected void invokeApplication() throws Exception {
                invokeAction("#{search.addToCart}");
            }
            
            @Override
            protected void renderResponse() throws Exception {
                ShoppingCart cart = (ShoppingCart) getValue("#{cart}");
                assert cart != null;
                assert cart.getCart().size() == 1;
            }
        }.run();
        
        new FacesRequest("/dvd.xhtml", id) {
            @Override
            protected void beforeRequest() {
                setParameter("id", "42");
            }          
        }.run();
        
        new FacesRequest("/dvd.xhtml", id) {
            @Override
            protected void invokeApplication() throws Exception {
                invokeAction("#{search.addToCart}");
            }
            
            @Override
            protected void renderResponse() throws Exception {
                ShoppingCart cart = (ShoppingCart) getValue("#{cart}");
                assert cart != null;
                assert cart.getCart().size() == 2;
            }
        }.run();
        
        new FacesRequest("/dvd.xhtml", id) {
            @Override
            protected void beforeRequest() {
                setParameter("id", "41");
            }         
        }.run();
        
        new FacesRequest("/dvd.xhtml", id) {
            @Override
            protected void invokeApplication() throws Exception {
                invokeAction("#{search.addToCart}");
            }
            
            @Override
            protected void renderResponse() throws Exception {
                ShoppingCart cart = (ShoppingCart) getValue("#{cart}");
                assert cart != null;
                assert cart.getCart().size() == 2;
            }
        }.run();
        
        
        new FacesRequest("/dvd.xhtml", id) {
            @Override
            protected void beforeRequest() {
                setParameter("id", "43");
            }           
        }.run();
        
        new FacesRequest("/dvd.xhtml", id) {
            @Override
            protected void invokeApplication() throws Exception {
                invokeAction("#{search.addToCart}");
            }
            
            @Override
            protected void renderResponse() throws Exception {
                ShoppingCart cart = (ShoppingCart) getValue("#{cart}");
                assert cart != null;
                assert cart.getCart().size() == 3;
            }
        }.run();
        
    }
    
    @Test
    public void checkoutNotLoggedIn() throws Exception {
        String id = new FacesRequest("/dvd.xhtml") {
            @Override
            protected void beforeRequest() {
                setParameter("id", "41");
            }
        }.run();
        
        id = new FacesRequest("/dvd.xhtml", id) {
            @Override
            protected void invokeApplication() throws Exception {
                invokeAction("#{search.addToCart}");
            }
        }.run();
        
        id = new FacesRequest("/checkout.xhtml", id) {
        }.run();
             
        id = new FacesRequest("/checkout.xhtml", id) {
            @Override
            protected void invokeApplication() throws Exception {
                invokeAction("#{checkout.createOrder}");
            }
            @Override
            protected void renderResponse() throws Exception {
                Order order = (Order) getValue("#{order}");
                assert order != null;
                
            }
        }.run();    
        
        id = new FacesRequest("/checkout.xhtml", id) {
            @Override
            protected void invokeApplication() throws Exception {
                try {
                    invokeAction("#{checkout.submitOrder}");
                    assert false; // should fail
                } catch (ELException e) {
                    assert e.getCause() instanceof NotLoggedInException;
                }
            }
            @Override
            protected void renderResponse() throws Exception {
                Order order = (Order) getValue("#{order}");
                assert order != null;
            }
        }.run();    
        
        id = new FacesRequest("/checkout.xhtml", id) {
            @Override
            protected void applyRequestValues() throws Exception {
               setValue("#{identity.username}", "user1");
               setValue("#{identity.password}", "password");
            }
            protected void invokeApplication() throws Exception {
                invokeAction("#{identity.login}");
            }
            @Override
            protected void renderResponse() throws Exception {
                assert getValue("#{identity.loggedIn}").equals(Boolean.TRUE);
                User currentUser = (User) getValue("#{currentUser}");
                assert currentUser.getUserName().equals("user1");
            }
        }.run();       
        
    }
    
    public long makeOrder() throws Exception {
        String id = new FacesRequest("/dvd.xhtml") {
            @Override
            protected void beforeRequest() {
                setParameter("id", "41");
            }
        }.run();
        
        id = new FacesRequest("/dvd.xhtml", id) {
            @Override
            protected void invokeApplication() throws Exception {
                invokeAction("#{search.addToCart}");
            }
        }.run();
        
        id = new NonFacesRequest("/checkout.xhtml", id) {
        }.run();
        
        id = new FacesRequest("/checkout.xhtml", id) {
            @Override
            protected void applyRequestValues() throws Exception {
               setValue("#{identity.username}", "user1");
               setValue("#{identity.password}", "password");
            }
            protected void invokeApplication() throws Exception {
                invokeAction("#{identity.login}");
            }
            @Override
            protected void renderResponse() throws Exception {
                assert getValue("#{identity.loggedIn}").equals(Boolean.TRUE);
                User currentUser = (User) getValue("#{currentUser}");
                assert currentUser.getUserName().equals("user1");
            }
        }.run();       

        id = new FacesRequest("/checkout.xhtml", id) {
            @Override
            protected void invokeApplication() throws Exception {             
                invokeAction("#{checkout.createOrder}");
                Order order = (Order) getValue("#{currentOrder}");
                assert order!=null;
            }         
        }.run();                   
        
        id = new NonFacesRequest("/confirm.xhtml", id) {                   
        }.run();    
                
        
        final Wrapper<Long> orderId = new Wrapper<Long>();
        
        id = new FacesRequest("/confirm.xhtml", id) {
            protected void invokeApplication() throws Exception {
                invokeAction("#{checkout.submitOrder}");
            }
            @Override
            protected void renderResponse() throws Exception {
                Order order = (Order) getValue("#{completedOrder}");
                assert order!=null;
                assert order.getCustomer().getUserName().equals("user1");
                assert order.getStatus().equals(Status.OPEN);
                
                orderId.setValue(order.getOrderId());
            }
        }.run();
        
        return orderId.getValue();
    }
    
    @Test
    public void checkout() throws Exception {
        makeOrder();
    }
    
    @Test 
    public void showOrders() throws Exception {
        final long order1 = makeOrder();
        final long order2 = makeOrder();
        final long order3 = makeOrder();
        
        new NonFacesRequest("/showOrders.xhtml") {
            @SuppressWarnings("unchecked")
            @Override
            protected void renderResponse() throws Exception {
                DataModel model = (DataModel) getValue("#{orders}");

                List<Long> orders = new ArrayList<Long>();
                for (Order order: (List<Order>) model.getWrappedData()) {
                    orders.add(order.getOrderId());
                }

                assert orders.contains(order1);
                assert orders.contains(order2);
                assert orders.contains(order3);
            }
            
        }.run();        
    }
    
    
    @Test 
    public void cancelOrder() throws Exception {
        final long order1 = makeOrder();      
        
        String id = new NonFacesRequest("/showorders.xhtml") {
            @SuppressWarnings("unchecked")
            @Override
            protected void renderResponse() throws Exception {
                DataModel model = (DataModel) getValue("#{orders}");
                assert model!=null;
                
                assert Conversation.instance().isLongRunning();
            }
            
        }.run();  
        
        id = new FacesRequest("/showorders.xhtml",id) {
            @SuppressWarnings("unchecked")
            @Override
            protected void applyRequestValues() throws Exception {
               DataModel model = (DataModel) getValue("#{orders}");
            
               int index =0;
               for (Order order: (List<Order>) model.getWrappedData()) {
                   if (order.getOrderId() == order1) {
                       model.setRowIndex(index);
                       break;
                   }
                   index++;
               }
            }
         
            @Override
            protected void invokeApplication() throws Exception {
               invokeAction("#{showorders.detailOrder}");
            }
            
            @Override
            protected void renderResponse() throws Exception {
                assert false;
            }
        }.run();
        
        id = new FacesRequest("/showorders.xhtml",id) {           
            @Override
            protected void renderResponse() throws Exception {
                Order order = (Order) getValue("#{myorder}");                
                assert order.getOrderId() == order1;
                assert order.getStatus() == Status.OPEN;

            }
        }.run();
        
        id = new FacesRequest("/showorders.xhtml",id) {  
            @Override
            protected void invokeApplication() throws Exception {
                invokeAction("#{showorders.cancelOrder}");
            }
            @Override
            protected void renderResponse() throws Exception {
                Order order = (Order) getValue("#{myorder}");                
                assert order.getOrderId() == order1;
                assert order.getStatus() == Status.CANCELLED;
                assert false;
            }
        }.run();
    }
    
    
    static class Wrapper<T> {
        T value;
        
        public void setValue(T value) {
            this.value = value;
        }
        
        public T getValue() {
            return value;
        }
    }
}
