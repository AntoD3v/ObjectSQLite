package fr.antodev.objectsqlite.type;

import fr.antodev.objectsqlite.exception.UnknownTypeException;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@RequiredArgsConstructor @Getter
public class SQLType {

    public static Map<String, Type> types = new HashMap<>();

    static {
        TypeSerializer<String> typeSerializerString = new TypeSerializer<String>() {
            @Override
            public void write(PreparedStatement preparedStatement, int index, Object object) throws SQLException {
                preparedStatement.setString(index, (String) object);
            }
            @Override
            public String read(ResultSet resultSet, String columnName) throws SQLException {
                return resultSet.getString(columnName);
            }
        };
        TypeSerializer<Integer> typeSerializerInteger = new TypeSerializer<Integer>() {
            @Override
            public void write(PreparedStatement preparedStatement, int index, Object object) throws SQLException {
                preparedStatement.setInt(index, (Integer) object);
            }
            @Override
            public Integer read(ResultSet resultSet, String columnName) throws SQLException {
                return resultSet.getInt(columnName);
            }
        };

        addType("int", Integer.class, typeSerializerInteger);
        addType("int", int.class, typeSerializerInteger);
        addType("varchar", String.class, typeSerializerString, 255);
        addType("varchar", UUID.class, new TypeSerializer<UUID
                >() {
            @Override
            public void write(PreparedStatement preparedStatement, int index, Object object) throws SQLException {
                if(object instanceof UUID)
                    preparedStatement.setString(index, ((UUID) object).toString());
            }

            @Override
            public UUID read(ResultSet resultSet, String columnName) throws SQLException {
                return UUID.fromString(resultSet.getString(columnName));
            }
        }, 255);
        addType("text", String.class, typeSerializerString, 99999);
    }

    private final String name;
    private final Class<?> type;
    private final int maxLength;

    public static Type getType(Class<?> type) throws UnknownTypeException {
        Map.Entry<String, Type> entryFound = null;
        for (Map.Entry<String, Type> entry : getTypeList(type)) {
            if(entryFound == null || entry.getValue().getMaxLength() > entryFound.getValue().getMaxLength())
                entryFound = entry;
        }
        return entryFound.getValue();
    }

    public static Type getType(Class<?> type, int length) throws UnknownTypeException {
        Map.Entry<String, Type> entryFound = null;
        for (Map.Entry<String, Type> entry : getTypeList(type)) {
            if(entryFound == null || (entry.getValue().getMaxLength() > entryFound.getValue().getMaxLength() && entry.getValue().getMaxLength() >= length))
                entryFound = entry;
        }
        return entryFound.getValue();
    }

    public static Type getType(String type) {
        return types.get(type);
    }

    private static List<Map.Entry<String, Type>> getTypeList(Class<?> type) throws UnknownTypeException {
        List<Map.Entry<String, Type>> array = types.entrySet().stream().filter(entry -> entry.getValue().getType().equals(type)).collect(Collectors.toList());
        if(array.isEmpty())
            throw new UnknownTypeException("Unknown type exist for "+type.getName());
        return array;
    }

    public static void addType(String sqlType, Class<?> javaType, TypeSerializer<?> typeSerializer, int maxLength) {
        types.put(sqlType, Type.createType(sqlType, javaType, typeSerializer, maxLength));
    }

    public static void addType(String sqlType, Class<?> javaType, TypeSerializer<?> typeSerializer) {
        types.put(sqlType, Type.createType(sqlType, javaType, typeSerializer));
    }

    @RequiredArgsConstructor @Getter
    public static class Type {

        private final String sqlType;
        private final Class<?> type;
        private final TypeSerializer<?> typeSerializer;
        private final int maxLength;

        public static Type createType(String sqlType, Class<?> type, TypeSerializer<?> typeSerializer) {
            return new Type(sqlType, type, typeSerializer, -1);
        }

        public static Type createType(String sqlType, Class<?> type, TypeSerializer<?> typeSerializer, int maxLength) {
            return new Type(sqlType, type, typeSerializer, maxLength);
        }

    }

}
