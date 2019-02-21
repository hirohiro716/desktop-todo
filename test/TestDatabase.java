import java.sql.SQLException;

import com.hirohiro716.desktop.todo.Database;
import com.hirohiro716.desktop.todo.ToDo;

@SuppressWarnings("all")
public class TestDatabase {

    public static void main(String[] args) throws SQLException {
        
        Database database = new Database();
        database.connect();
        
        ToDo todo = new ToDo(database);
        
    }
    
}
