package org.jboss.seam.example.seamdiscs.action;

import javax.ejb.Local;

import org.apache.myfaces.trinidad.model.TreeModel;
import org.jboss.seam.example.seamdiscs.model.Artist;


/**
 * @author Pete Muir
 *
 */
@Local
public interface ArtistHome
{

   // Methods from ArtistHomeImpl
   public Artist getArtist();

   public String getType();

   public void setType(String type);

   public void addBandMember();

   public void addDisc();

   public TreeModel getTree();
   
   public void ejbRemove();
   
   // Methods from EntityHome and Home
   
   public Object getId(); 
   
   public void setId(Object id);
   
   public String persist();
   
   public String update();
   
   public boolean isManaged();

   public void create();
   
   public Object getInstance();

}