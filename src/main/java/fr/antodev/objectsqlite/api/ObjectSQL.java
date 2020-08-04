package fr.antodev.objectsqlite.api;

import fr.antodev.objectsqlite.exception.UnknownTypeException;

import java.sql.SQLException;
import java.util.Map;

public interface ObjectSQL<K, O> {

    void createTable() throws UnknownTypeException, SQLException;
    O get(K key) throws SQLException, UnknownTypeException, IllegalAccessException, InstantiationException, NoSuchFieldException;
    Map<K, O> getAll() throws SQLException, IllegalAccessException, InstantiationException, NoSuchFieldException, UnknownTypeException;
    O getOnlyMap(K key);
    Map<K, O> getMap();

    boolean exist(K key);
    void remove(K key) throws SQLException;
    void purgeMap();
    void truncate() throws SQLException;

    void put(K key, O object) throws SQLException;
    void update(K key, O object) throws SQLException;

    void close() throws SQLException;

    String getTableName();

}
