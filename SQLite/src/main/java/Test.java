import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Logger;

import com.aasenov.sqlite.manager.SQLiteManager;

public class Test {
    /**
     * Logger instance.
     */
    private static Logger sLog = Logger.getLogger(SQLiteManager.class);

    public static void main(String[] args) {
        BasicConfigurator.configure();

        SQLiteManager manager = null;
        try {
            manager = new SQLiteManager("simple.db");

            Connection connection = manager.getConnection();

            Statement statement = connection.createStatement();
            statement.setQueryTimeout(30); // set timeout to 30 sec.

            statement.executeUpdate("drop table if exists person");
            statement.executeUpdate("create table person (id integer, name string)");
            statement.executeUpdate("insert into person values(1, 'leo')");
            statement.executeUpdate("insert into person values(2, 'yui')");
            ResultSet rs = statement.executeQuery("select * from person");
            while (rs.next()) {
                // read the result set
                System.out.print("name = " + rs.getString("name"));
                System.out.println(" id = " + rs.getInt("id"));
            }

        } catch (SQLException e) {
            sLog.error(e.getMessage(), e);
        } finally {
            if (manager != null) {
                manager.close();
            }
        }
    }
}
