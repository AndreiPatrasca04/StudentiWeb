package ro.student.studenti;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class Db {
    private static final String URL = "jdbc:derby://localhost:1527/lab2";
    private static final String USER = "Andrei";
    private static final String PASS = "test";

    public static Connection getConnection() throws SQLException {
        // incarcam explicit driverul (in aplicatii web e mai sigur decat sa ne bazam pe incarcarea automata)
        for (String clasa : new String[]{
                "org.apache.derby.client.ClientAutoloadedDriver",
                "org.apache.derby.jdbc.ClientDriver"}) {
            try {
                Class.forName(clasa);
                break;
            } catch (ClassNotFoundException e) {
                // incercam urmatorul nume
            }
        }
        return DriverManager.getConnection(URL, USER, PASS);
    }
}