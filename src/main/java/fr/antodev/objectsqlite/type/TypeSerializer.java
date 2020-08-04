package fr.antodev.objectsqlite.type;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public interface TypeSerializer<T> {

    void write(PreparedStatement preparedStatement, int index, Object object) throws SQLException;
    T read(ResultSet resultSet, String columnName) throws SQLException;

}
