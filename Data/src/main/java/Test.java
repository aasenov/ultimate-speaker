import java.util.List;

import javax.swing.SortOrder;

import org.apache.log4j.BasicConfigurator;

import com.aasenov.database.manager.DatabaseProvider;
import com.aasenov.database.objects.DatabaseTable;
import com.aasenov.database.objects.FileItem;

public class Test {
    public static void main(String[] args) {
        BasicConfigurator.configure();
        // Logger.getRootLogger().setLevel(Level.INFO);

        try {
            DatabaseProvider.getDefaultManager().deleteAllTables();
            DatabaseTable<FileItem> table = new DatabaseTable<FileItem>("FileTable", new FileItem());
            System.out.println("Table size is: " + table.size());
            table.add(new FileItem("file1", "hash1", "location1", "speechLocation1"));
            table.add(new FileItem("file1", "hash2", "location1", "speechLocation1"));
            table.add(new FileItem("file1", "hash3", "location1", "speechLocation1"));
            System.out.println("Table size is: " + table.size());
            System.out.println("Table size is: " + table.size());

            System.out.println(table.get("hash1"));
            System.out.println(table.get("hash5"));

            System.out.println("Table content:");
            int count = 1;
            int start = 0;
            do {
                List<FileItem> page = table.getPage(start, count, SortOrder.ASCENDING, new String[] { "ID" });
                for (FileItem file : page) {
                    System.out.println(file);
                }

                if (page.size() < count) {
                    break;
                }

                start += count;
            } while (true);

            table.remove("hash5");
            table.remove("hash1");

            System.out.println("Table content:");
            count = 1;
            start = 0;
            do {
                List<FileItem> page = table.getPage(start, count, SortOrder.DESCENDING, new String[] { "ID" });
                for (FileItem file : page) {
                    System.out.println(file);
                }

                if (page.size() < count) {
                    break;
                }

                start += count;
            } while (true);

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
