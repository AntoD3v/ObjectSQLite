package fr.antodev.objectsqlite.api;

import lombok.SneakyThrows;

import java.sql.Connection;
import java.sql.DriverManager;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;

public abstract class AbstractSQLiteData<K, V> {

    protected final Map<K, V> data = new HashMap<>();
    protected final ExecutorService service = Executors.newSingleThreadExecutor();
    private final String dir;
    private Connection connection;

    @SneakyThrows
    public AbstractSQLiteData(String dir) {
        this.dir = dir;
        getConnection().createStatement().executeQuery(getCreateTableSQL());
    }

    public V get(K key) {
        return data.get(key);
    }

    public abstract void getFuture(K key, Consumer<V> future);
    public abstract void add(K key, V value);
    public abstract void remove(K key);

    protected abstract String getCreateTableSQL();

    @SneakyThrows
    public Connection getConnection() {
        if(connection != null && !connection.isClosed())
            return connection;
        return (connection = DriverManager.getConnection("jdbc:sqlite:"+dir));
    }

}
