package org.jooq.example.db.mysql;

import org.jooq.DSLContext;
import org.jooq.Record4;
import org.jooq.Result;
import org.jooq.example.db.mysql.tables.records.AuthorRecord;
import org.jooq.example.db.mysql.tables.records.BookRecord;

import javax.annotation.Resource;
import javax.inject.Inject;
import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonBuilderFactory;
import javax.json.JsonObject;
import javax.json.JsonReader;
import javax.transaction.SystemException;
import javax.transaction.Transactional;
import javax.transaction.UserTransaction;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import java.io.StringReader;
import java.util.Collections;
import java.util.logging.Logger;

import static org.jooq.example.db.mysql.Tables.*;

@Path("/books")
@Produces("application/json")
public class BooksRepository {

    final static Logger log = Logger.getLogger(BooksRepository.class.getName());
    @Resource
    UserTransaction tx;
    @Inject
    private DSLContext ctx;

    @GET
    @Path("/")
    public JsonArray getAllBooks() {
        Result<Record4<String, String, String, Integer>> books = ctx.select(AUTHOR.FIRST_NAME, AUTHOR.LAST_NAME, BOOK.TITLE, BOOK.PUBLISHED_IN)
                .from(BOOK)
                .join(AUTHOR).on(BOOK.AUTHOR_ID.equal(AUTHOR.ID))
                .fetch();

        JsonBuilderFactory builderFactory = Json.createBuilderFactory(Collections.emptyMap());
        return books.stream().map((book) -> Json.createObjectBuilder()
                .add("author_firstname", book.getValue(AUTHOR.FIRST_NAME))
                .add("author_lastname", book.getValue(AUTHOR.LAST_NAME))
                .add("title", book.getValue(BOOK.TITLE))
                .add("published_in", book.getValue(BOOK.PUBLISHED_IN))
        ).collect(builderFactory::createArrayBuilder,
                (a, s) -> a.add(s),
                (b1, b2) -> b1.add(b2)).build();
    }

    @POST
    @Path("/")
    @Transactional
    public JsonObject saveBook(String messageBody) throws SystemException {
//        {
//            "author_firstname": "",
//            "author_lastname": "",
//            "title": "",
//            "published_in": ""
//        }

        JsonReader reader = Json.createReader(new StringReader(messageBody));
        JsonObject object = reader.readObject();
        reader.close();

        AuthorRecord authorRecord = ctx.insertInto(AUTHOR, AUTHOR.FIRST_NAME, AUTHOR.LAST_NAME)
                .values(object.getString("author_firstname"), object.getString("author_lastname"))
                .returning()
                .fetchOne();

        BookRecord bookRecord = ctx.insertInto(BOOK, BOOK.AUTHOR_ID, BOOK.TITLE, BOOK.PUBLISHED_IN)
                .values(authorRecord.getId(), object.getString("title"), object.getInt("published_in"))
                .returning().fetchOne();

        return Json.createObjectBuilder()
                .add("author_id", authorRecord.getId())
                .add("author_firstname", authorRecord.getFirstName())
                .add("author_lastname", authorRecord.getFirstName())
                .add("book_id", bookRecord.getId())
                .add("book_title", bookRecord.getTitle())
                .add("book_published_in", bookRecord.getPublishedIn()).build();

    }

}
