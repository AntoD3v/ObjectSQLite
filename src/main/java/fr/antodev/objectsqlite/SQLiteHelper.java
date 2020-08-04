package fr.antodev.objectsqlite;

import fr.antodev.objectsqlite.api.ObjectSQL;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class SQLiteHelper {

    public ObjectSQL from(File file, Class<?> object) throws IOException, SQLException {
        if (file.createNewFile()) {

        }
        return new CraftObjectSQL(getConnection(file.getAbsolutePath()), object);
    }

    public ObjectSQL from(String path, Class<?> object) throws IOException, SQLException {
        return from(new File(path), object);
    }

    private Connection getConnection(String sqlFile) throws SQLException {
        return DriverManager.getConnection("jdbc:sqlite:" + sqlFile);
    }

}
