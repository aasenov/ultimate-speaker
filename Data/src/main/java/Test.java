import org.apache.log4j.BasicConfigurator;

import com.aasenov.database.manager.DatabaseProvider;
import com.aasenov.database.objects.FileItem;
import com.aasenov.database.objects.FileTable;

public class Test {
    public static void main(String[] args) {
        BasicConfigurator.configure();

        try {
            FileTable table = new FileTable("FileTable", false);
            System.out.println("Table size is: " + table.size());
            table.add(new FileItem("file1", "hash1", "location1", "speechLocation1"));
            table.add(new FileItem("file1", "hash2", "location1", "speechLocation1"));
            table.add(new FileItem("file1", "hash3", "location1", "speechLocation1"));
            System.out.println("Table size is: " + table.size());
            table.commit(true);
            System.out.println("Table size is: " + table.size());

            // statement.executeUpdate("drop table if exists person");
            // statement.executeUpdate("create table person (id integer, name string)");
            // statement.executeUpdate("insert into person values(1, 'leo')");
            // statement.executeUpdate("insert into person values(2, 'yui')");
            // ResultSet rs = statement.executeQuery("select * from person");
            // while (rs.next()) {
            // // read the result set
            // System.out.print("name = " + rs.getString("name"));
            // System.out.println(" id = " + rs.getInt("id"));
            // }
        } finally {
            DatabaseProvider.getDefaultManager().close();
        }
    }
}
