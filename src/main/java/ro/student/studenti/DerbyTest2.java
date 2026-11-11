package ro.student.studenti;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.logging.Level;
import java.util.logging.Logger;

public class DerbyTest2 {
    public static void main(String[] args) {
        try (Connection con = Db.getConnection();
             Statement smt = con.createStatement();
             ResultSet rs = smt.executeQuery("select * from studenti")) {
            while (rs.next())
                System.out.println(rs.getString("nume") + ", " + rs.getString("varsta")
                        + ", " + rs.getFloat("media"));
        } catch (SQLException ex) {
            Logger.getLogger(DerbyTest2.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}