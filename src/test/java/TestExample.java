import org.h2.tools.DeleteDbFiles;
import org.h2.tools.Server;
import org.junit.Test;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class TestExample {

    private static final String URL = "jdbc:h2:~/test;AUTO_SERVER=TRUE;Mode=Oracle";
    private static final String USERNAME = "sa";
    private static final String PASSWORD = "";

    @Test
    public void test1() throws SQLException, InterruptedException {

        /**
         * Нужно реализовать сервис который будет выполнять crud операции нужно использовать jdbc
         * Create (создание);
         * Read (чтение);
         * Update (обновление);
         * Delete (удаление).
         * И реализовать для него тесты
         */
        DeleteDbFiles.execute("~", "test", true);
        Server server = Server.createWebServer();
        server.start();
        Connection conn = DriverManager.getConnection(URL);
        Statement statement = conn.createStatement();
        statement.execute("create table geolocations(id serial, time_zone varchar(255), zip varchar(255))");
        statement.execute("insert into geolocations(time_zone, zip) values ('12345','12345')");
        statement.execute("insert into geolocations(time_zone, zip) values ('12345','ddd')");
        statement.execute("insert into geolocations(time_zone, zip) values ('12345','123ff45')");
        statement.execute("insert into geolocations(time_zone, zip) values ('12345','fffff')");
        ResultSet resultSet = statement.executeQuery("select id, time_zone from geolocations ");
        while (resultSet.next()) {
            System.out.println(resultSet.getInt("id"));
            System.out.println(resultSet.getString("time_zone"));
        }
        conn.close();

    }
}
