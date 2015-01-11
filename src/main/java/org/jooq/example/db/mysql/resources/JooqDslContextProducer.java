package org.jooq.example.db.mysql.resources;


import org.jooq.DSLContext;
import org.jooq.SQLDialect;
import org.jooq.impl.DSL;

import javax.annotation.Resource;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Produces;
import javax.sql.DataSource;

/**
 * JOOQ DSL context producer
 * http://stackoverflow.com/questions/16534728/please-explain-the-produces-annotation-in-cdi
 */
@ApplicationScoped
public class JooqDslContextProducer {

    @Resource(lookup="java:jboss/datasources/JooqExampleDS")
    DataSource dataSource;

    @Produces
    public DSLContext jooq() {
        return DSL.using(dataSource, SQLDialect.MYSQL);
    }


}
