Seam Hibernate Example
======================

This is the Hotel Booking example implemented in Seam and Hibernate POJOs.

To deploy the example, follow these steps:

* In the example root directory run:

    mvn clean install

* Set JBOSS_HOME environment property.

* In the hibernate-web directory run:

    mvn jboss-as:deploy

* Open this URL in a web browser: http://localhost:8080/jboss-seam-hibernate
