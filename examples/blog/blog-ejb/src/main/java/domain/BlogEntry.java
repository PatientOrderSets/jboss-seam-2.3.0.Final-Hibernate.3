//$Id: BlogEntry.java 13984 2011-01-11 12:45:45Z sannegrinovero $
package domain;

import org.apache.solr.analysis.LowerCaseFilterFactory;
import org.apache.solr.analysis.NGramFilterFactory;
import org.apache.solr.analysis.SnowballPorterFilterFactory;
import org.apache.solr.analysis.StandardTokenizerFactory;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.search.annotations.*;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.persistence.ManyToOne;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.Date;

/**
 * Represents a blog entry.
 *
 * @author Simon Brown
 * @author Sanne Grinovero
 */
@Entity
@Cache(usage = CacheConcurrencyStrategy.TRANSACTIONAL)
@Indexed
@AnalyzerDefs({
        @AnalyzerDef(name = "en",
                tokenizer = @TokenizerDef(factory = StandardTokenizerFactory.class),
                filters = {
                        @TokenFilterDef(factory = LowerCaseFilterFactory.class),
                        @TokenFilterDef(factory = SnowballPorterFilterFactory.class, params = {
                                @Parameter(name = "language", value = "English")
                        })
                }),
        @AnalyzerDef(name = "ngrams", tokenizer = @TokenizerDef(factory = StandardTokenizerFactory.class),
                filters = {
                        @TokenFilterDef(factory = LowerCaseFilterFactory.class),
                        @TokenFilterDef(factory = NGramFilterFactory.class,
                                params = {@Parameter(name = "minGramSize", value = "3"), @Parameter(name = "maxGramSize", value = "3")})
                })}
)
public class BlogEntry {

   @Id
   @Size(min = 1, max = 20)
   @DocumentId
   private String id;

   @NotNull
   @Size(max = 70)
   @Fields({
           @Field(name = "title:en", analyzer = @Analyzer(definition = "en")),
           @Field(name = "title:ngrams", analyzer = @Analyzer(definition = "ngrams"))})
   private String title;

   @Size(max=1024)
   private String excerpt;

   @NotNull
   @Lob
   @Fields({
           @Field(name = "body:en", analyzer = @Analyzer(definition = "en")),
           @Field(name = "body:ngrams", analyzer = @Analyzer(definition = "ngrams"))})
   private String body;

   @NotNull
   private Date date = new Date();

   @ManyToOne
   @NotNull
   private Blog blog;

   public BlogEntry(Blog blog) {
      this.blog = blog;
   }

   BlogEntry() {
   }

   public String getId() {
      return id;
   }

   public String getTitle() {
      return title;
   }

   public String getExcerpt() {
      return excerpt;
   }

   public String getBody() {
      return body;
   }

   public Date getDate() {
      return date;
   }

   public void setBody(String body) {
      this.body = body;
   }

   public void setDate(Date date) {
      this.date = date;
   }

   public void setExcerpt(String excerpt) {
      if ("".equals(excerpt)) excerpt = null;
      this.excerpt = excerpt;
   }

   public void setId(String id) {
      this.id = id;
   }

   public void setTitle(String title) {
      this.title = title;
   }

}
