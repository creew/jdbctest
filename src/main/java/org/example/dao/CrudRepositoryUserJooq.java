package org.example.dao;

import org.example.entity.User;
import org.example.exception.CrudException;
import org.example.exception.CrudExceptionNotFound;
import org.example.jooq.db.tables.records.UsersRecord;
import org.jetbrains.annotations.NotNull;
import org.jooq.DSLContext;
import org.jooq.Record;
import org.jooq.SQLDialect;
import org.jooq.impl.DSL;

import java.sql.Connection;

import static org.example.jooq.db.tables.Users.USERS;

public class CrudRepositoryUserJooq implements CrudRepository<Long, User> {

    private Connection connection;

    public CrudRepositoryUserJooq(Connection connection) {
        this.connection = connection;
    }

    @Override
    public Long create(@NotNull User value) throws CrudException {
        DSLContext create = DSL.using(
                connection, SQLDialect.POSTGRES);
        Record record = create.insertInto(USERS, USERS.LOGIN, USERS.PASSWORD, USERS.EMAIL, USERS.FIRST_NAME, USERS.LAST_NAME)
                .values(value.getLogin(), value.getPassword(), value.getEmail(), value.getFirstName(), value.getLastName())
                .returningResult(USERS.USER_ID)
                .fetchOne();
        Integer id = record.getValue(USERS.USER_ID);
        return (id.longValue());
    }

    @Override
    public User read(@NotNull Long key) throws CrudException {
        DSLContext create = DSL.using(
                connection, SQLDialect.POSTGRES);
        UsersRecord usersRecord = create.selectFrom(USERS)
                .where(USERS.USER_ID.eq(key.intValue()))
                .fetchAny();
        if (usersRecord == null)
            throw new CrudExceptionNotFound();
        User user = new User(usersRecord.getLogin(), usersRecord.getPassword(), usersRecord.getEmail());
        user.setFirstName(usersRecord.getFirstName());
        user.setLastName(usersRecord.getLastName());
        user.setId(usersRecord.getUserId());
        return user;
    }

    @Override
    public void update(@NotNull Long key, @NotNull User value) throws CrudException {
        DSLContext create = DSL.using(
                connection, SQLDialect.POSTGRES);
        if (create.update(USERS)
                .set(USERS.LOGIN, value.getLogin())
                .set(USERS.PASSWORD, value.getPassword())
                .set(USERS.EMAIL, value.getEmail())
                .set(USERS.FIRST_NAME, value.getFirstName())
                .set(USERS.LAST_NAME, value.getLastName())
                .where(USERS.USER_ID.eq(key.intValue()))
                .execute() == 0) {
            throw new CrudExceptionNotFound();
        }
    }

    @Override
    public void delete(@NotNull Long key) throws CrudException {
        DSLContext create = DSL.using(
                connection, SQLDialect.POSTGRES);
        if (create.deleteFrom(USERS)
                .where(USERS.USER_ID.eq(key.intValue()))
                .execute() == 0)
            throw new CrudExceptionNotFound();
    }
}
