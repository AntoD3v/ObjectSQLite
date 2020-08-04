package fr.antodev.objectsqlite;

import fr.antodev.objectsqlite.annotation.Vartype;
import fr.antodev.objectsqlite.api.ObjectSQL;
import fr.antodev.objectsqlite.type.SQLType;
import fr.antodev.objectsqlite.exception.UnknownTypeException;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.lang.reflect.Field;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;
import java.util.stream.Collectors;

@RequiredArgsConstructor
public class CraftObjectSQL<K, O> implements ObjectSQL<K, O> {

    private final Map<K, O> maps = new HashMap<>();

    private final Class<?> clazz;
    private final String tableName;
    @Getter private final List<Field> fields = new ArrayList<>();
    @Getter private final Connection connection;
    private final String primaryKey;

    public CraftObjectSQL(Connection connection, Class<?> clazz) {
        this.connection = connection;
        this.tableName = clazz.getSimpleName();
        this.clazz = clazz;
        this.fields.addAll(Arrays.stream(clazz.getDeclaredFields()).filter(field -> !field.isAnnotationPresent(Vartype.Ignored.class)).collect(Collectors.toList()));
        System.out.println(fields.size());
        this.primaryKey = fields.stream().filter(field -> field.isAnnotationPresent(Vartype.PrimaryKey.class)).findFirst().get().getName();

    }

    public void createTable() throws UnknownTypeException, SQLException {
        StringBuilder request = new StringBuilder("CREATE TABLE IF NOT EXISTS `" + tableName + "` (");

        String length, notNull, primaryKey = null;
        SQLType.Type type;
        for (Field field : fields) {
            length = field.isAnnotationPresent(Vartype.Length.class) ? "("  + field.getAnnotation(Vartype.Length.class).length() + ")" : "";
            type = length.equals("") ? SQLType.getType(field.getType()) : SQLType.getType(field.getType(), field.getAnnotation(Vartype.Length.class).length());
            notNull = field.isAnnotationPresent(Vartype.NotNull.class) ? " NOT NULL" : "";

            if(field.isAnnotationPresent(Vartype.PrimaryKey.class))
                primaryKey = "PRIMARY KEY (`" + field.getName() + "`)";

            request.append(" `").append(field.getName()).append("` ").append(type.getSqlType()).append(length).append(notNull).append(", \n");

        }
        request.append(primaryKey).append(");");
        getConnection().createStatement().executeUpdate(request.toString());
    }

    @Override
    public O get(K key) throws SQLException, UnknownTypeException, IllegalAccessException, InstantiationException, NoSuchFieldException {
        O object;
        if((object = getOnlyMap(key)) != null)
            return object;
        PreparedStatement statement = getConnection().prepareStatement("SELECT * from " + tableName + " WHERE " + primaryKey + " = ?");
        SQLType.Type sqlType = SQLType.getType(key.getClass());
        sqlType.getTypeSerializer().write(statement, 1, key);
        ResultSet result = statement.executeQuery();
        if(!result.next())
            return null;
        object = (O) clazz.newInstance();
        for (Field field : getFields()) {
            Field fieldObject = object.getClass().getDeclaredField(field.getName());
            fieldObject.setAccessible(true);
            fieldObject.set(object, SQLType.getType(field.getType()).getTypeSerializer().read(result, field.getName()));
        }
        maps.put(key, object);
        return object;
    }

    @Override
    public Map<K, O> getAll() throws SQLException, IllegalAccessException, InstantiationException, NoSuchFieldException, UnknownTypeException {
        ResultSet resultSet = getConnection().createStatement().executeQuery("SELECT * from " + tableName);
        K key = null; O object;
        while(resultSet.next()) {
            object = (O) clazz.newInstance();
            for (Field field : getFields()) {
                Field fieldObject = object.getClass().getDeclaredField(field.getName());
                fieldObject.setAccessible(true);
                Object fieldO = SQLType.getType(field.getType()).getTypeSerializer().read(resultSet, field.getName());
                fieldObject.set(object, fieldO);
                if(field.getName().equals(primaryKey))
                    key = (K) fieldO;
            }
            maps.put(key, object);
        }
        return maps;
    }

    @Override
    public O getOnlyMap(K key) {
        return maps.get(key);
    }

    @Override
    public Map<K, O> getMap() {
        return maps;
    }

    @Override
    public boolean exist(K key) {
        return false;
    }

    @Override
    public void remove(K key) throws SQLException {
        getConnection().createStatement().executeUpdate("DELETE from " + tableName + " WHERE " + primaryKey + "= ?");

    }

    @Override
    public void purgeMap() {
        maps.clear();
    }

    @Override
    public void truncate() throws SQLException {
        getConnection().createStatement().executeUpdate("TRUNCATE " + tableName);
    }

    @Override
    public void put(K key, O object) throws SQLException {
        List<Field> fields = getFields();
        StringBuilder values = new StringBuilder(), columnsName = new StringBuilder();
        fields.forEach(field -> {
            columnsName.append(field.getName()).append(",");
            values.append("?,");
        });
        values.delete(values.length()-1, values.length());
        columnsName.delete(columnsName.length()-1, columnsName.length());
        PreparedStatement statement = getConnection().prepareStatement("REPLACE INTO " + tableName + "(" + columnsName.toString() + ") VALUES(" + values.toString() + ")");
        for (int i = 0; i < fields.size(); i++) {
            Field field = fields.get(i);
            try {
                field.setAccessible(true);
                SQLType.Type type;
                if (field.getType().equals(String.class))
                    type = SQLType.getType  (field.getType(), ((String) field.get(object)).length());
                else if (field.getType().equals(Integer.class))
                    type = SQLType.getType(field.getType(), field.getInt(object));
                else
                    type = SQLType.getType(field.getType());
                type.getTypeSerializer().write(statement, i + 1, field.get(object));
            } catch (UnknownTypeException | IllegalAccessException e) {
                e.printStackTrace();
            }
        }
        statement.executeUpdate();
    }

    @Override
    public void update(K key, O object) throws SQLException {
        put(key, object);
    }

    @Override
    public void close() throws SQLException {
        getConnection().close();
    }

    @Override
    public String getTableName() {
        return tableName;
    }


}
