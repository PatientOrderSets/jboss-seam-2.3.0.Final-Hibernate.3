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
@Table(name="CATEGORIES")
//@Cache(usage=CacheConcurrencyStrategy.READ_ONLY)
@Indexed
public class Category
    implements Serializable
{
    private static final long serialVersionUID = 5544598397835376242L;
    int    id;
    String name;

    @Id @GeneratedValue
    @Column(name="CATEGORY")
    @DocumentId
    public int getCategoryId() {
        return id;
    }
    public void setCategoryId(int id) {
        this.id = id;
    }

    @Column(name="NAME",nullable=false,unique=true,length=50)
    @Fields({
       @Field(index=Index.YES),
       @Field(index=Index.YES, name="name:ngrams", analyzer=@Analyzer(definition="ngrams"))})
    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }

    @Override
    public boolean equals(Object other) {
        if (other == null || !(other instanceof Category)) {
            return false;
        }

        Category otherCategory = (Category) other;
        return (getCategoryId() == otherCategory.getCategoryId());
    }

    @Override
    public int hashCode() {
        return 37*getCategoryId() + 97;
    }

}
