import fr.antodev.objectsqlite.SQLiteHelper;
import fr.antodev.objectsqlite.annotation.Vartype;
import fr.antodev.objectsqlite.api.ObjectSQL;
import fr.antodev.objectsqlite.exception.UnknownTypeException;
import fr.antodev.objectsqlite.type.SQLType;
import fr.antodev.objectsqlite.type.TypeSerializer;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.IOException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;

public class MyData {

    public static void main(String[] args) throws IOException, SQLException, UnknownTypeException, IllegalAccessException, NoSuchFieldException, InstantiationException {
        SQLiteHelper sqLiteHelper = new SQLiteHelper();
        ObjectSQL<UUID, Player> object = sqLiteHelper.from("test.db", Player.class);
        //object.createTable();
        UUID key;
        object.getAll().forEach((uuid, player) -> {
            System.out.println("UUID: "+uuid.toString()+" / "+player.name);
        });
        //object.put(key = UUID.randomUUID(), new Player(key, "Anto", 10, 1, 20));
    }

    @NoArgsConstructor
    @AllArgsConstructor
    @Setter
    @Getter
    static
    public class Player {

        // Faire en sorte que les finals soit des primary key ??

        @Vartype.PrimaryKey
        private UUID uuid;
        private String name;
        private int argent;
        @Vartype.Ignored
        private int genre;
        private int thune;

    }

}
