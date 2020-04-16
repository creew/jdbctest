package org.example.dao;

import org.example.entity.User;
import org.example.exception.CrudException;
import org.example.exception.CrudExceptionNotFound;
import org.jetbrains.annotations.NotNull;
import org.jooq.DSLContext;
import org.jooq.Record;

import static org.example.jooq.db.tables.Users.USERS;

public class CrudRepositoryUserJooq implements CrudRepository<Long, User> {

    private DSLContext dslContext;

    public CrudRepositoryUserJooq(DSLContext dslContext) {
        this.dslContext = dslContext;
    }

    @Override
    public Long create(@NotNull User value) throws CrudException {
        Record record = dslContext.insertInto(USERS, USERS.LOGIN, USERS.PASSWORD, USERS.EMAIL, USERS.FIRST_NAME, USERS.LAST_NAME)
                .values(value.getLogin(), value.getPassword(), value.getEmail(), value.getFirstName(), value.getLastName())
                .returningResult(USERS.USER_ID)
                .fetchOne();
        Integer id = record.getValue(USERS.USER_ID);
        return (id.longValue());
    }

    @Override
    public User read(@NotNull Long key) throws CrudException {
        User user = dslContext.selectFrom(USERS)
                .where(USERS.USER_ID.eq(key.intValue()))
                .fetchAny(record -> {
                    User inUser = new User(record.getLogin(), record.getPassword(), record.getEmail());
                    inUser.setFirstName(record.getFirstName());
                    inUser.setLastName(record.getLastName());
                    inUser.setId(record.getUserId());
                    return inUser;
                });
        if (user == null)
            throw new CrudExceptionNotFound();
        return user;
    }

    @Override
    public void update(@NotNull Long key, @NotNull User value) throws CrudException {
        if (dslContext.update(USERS)
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
        if (dslContext.deleteFrom(USERS)
                .where(USERS.USER_ID.eq(key.intValue()))
                .execute() == 0)
            throw new CrudExceptionNotFound();
    }
}
