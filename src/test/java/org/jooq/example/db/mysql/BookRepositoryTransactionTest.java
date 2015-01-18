package org.jooq.example.db.mysql;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.jboss.shrinkwrap.resolver.api.maven.Maven;
import org.junit.Test;
import org.junit.runner.RunWith;

import javax.json.Json;
import javax.json.JsonArray;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Response;
import java.io.File;
import java.io.StringReader;
import java.net.URISyntaxException;
import java.net.URL;

import static org.hamcrest.core.Is.*;
import static org.junit.Assert.*;

@RunWith(Arquillian.class)
//this is a simple showcase and it's actually running about the "production database"
//which is obviously not very bright idea
//no custom datasource is set for this test (but it should be - as the note below reflects)
//TODO: use with Arquillian Persistence Extenstion
public class BookRepositoryTransactionTest {

    @Deployment
    public static WebArchive deploy() {
        File[] libs = Maven.resolver().loadPomFromFile("pom.xml")
                .resolve("org.jooq:jooq").withoutTransitivity()
                .asFile();

        return ShrinkWrap.create(WebArchive.class, "test.war")
                //add jOOQ library - as this is specific dependency
                .addAsLibraries(libs)
                //include all classed (together with the generated jooq classes)
                .addPackages(true, BooksRepository.class.getPackage());
    }

    @Test
    @RunAsClient
    public void should_get_four_books(@ArquillianResource URL baseUrl) throws URISyntaxException {
        WebTarget target = ClientBuilder.newClient().target(baseUrl.toURI() + "library/books");
        Response response = target.request().get();
        JsonArray json = Json.createReader(new StringReader(response.readEntity(String.class))).readArray();
        assertThat(response.getStatus(), is(200));
        assertThat(json.size(), is(4));
    }

    @Test
    @RunAsClient
    public void should_not_alter_database_on_duplicates(@ArquillianResource URL baseUrl) throws URISyntaxException {
        WebTarget target = ClientBuilder.newClient().target(baseUrl.toURI() + "library/books");
        Response response = target.request().post(Entity.json(Json.createObjectBuilder()
                .add("author_firstname", "George")
                .add("author_lastname", "Orwell2")
                .add("title","1984")
                .add("published_in", 1948)
                .build().toString()));
        assertThat(response.getStatus(), is(500));
    }

}
