/*
 * JBoss, Home of Professional Open Source
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */ 
package com.jboss.dvd.seam;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;

import org.hibernate.search.annotations.Analyzer;
import org.hibernate.search.annotations.DocumentId;
import org.hibernate.search.annotations.Field;
import org.hibernate.search.annotations.Fields;
import org.hibernate.search.annotations.Index;
import org.hibernate.search.annotations.Indexed;

@Entity
@Table(name="ACTORS")
@Indexed
public class Actor
    implements Serializable
{
    private static final long serialVersionUID = 8176964737283403683L;

    long id;
    String name;

    @Id @GeneratedValue
    @Column(name="ID")
    @DocumentId
    public long getId() {
        return id;
    }                    
    public void setId(long id) {
        this.id = id;
    }     

    @Column(name="NAME", length=50)
    @Fields({
       @Field(index=Index.YES),
       @Field(index=Index.YES, name="name:ngrams", analyzer=@Analyzer(definition="ngrams"))})
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
