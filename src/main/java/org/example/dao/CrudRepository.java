package org.example.dao;

import org.example.entity.Entity;
import org.example.exception.CrudException;
import org.jetbrains.annotations.NotNull;

public interface CrudRepository<K, V extends Entity> {

    K create(@NotNull V value) throws CrudException;

    V read(@NotNull K key) throws CrudException;

    void update(@NotNull K key, @NotNull V value) throws CrudException;

    void delete(@NotNull K key) throws CrudException;
}
