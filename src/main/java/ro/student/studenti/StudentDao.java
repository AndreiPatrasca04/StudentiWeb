package ro.student.studenti;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class StudentDao {

    private Student dinRand(ResultSet rs) throws SQLException {
        return new Student(rs.getInt("id"), rs.getString("nume"),
                rs.getInt("varsta"), rs.getDouble("media"));
    }

    // lista studentilor, cu cautare dupa nume si sortare
    public List<Student> gaseste(String cautare, String sortare) throws SQLException {
        // coloana de sortare e aleasa din lista fixa (nu se lipeste text de la utilizator in SQL)
        String ordine;
        if ("nume".equals(sortare)) ordine = "nume";
        else if ("varsta".equals(sortare)) ordine = "varsta";
        else if ("media".equals(sortare)) ordine = "media DESC";
        else ordine = "id";

        String sql = "SELECT id, nume, varsta, media FROM studenti "
                + "WHERE LOWER(nume) LIKE ? ORDER BY " + ordine;
        List<Student> lista = new ArrayList<>();
        try (Connection con = Db.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            String c = (cautare == null) ? "" : cautare.trim().toLowerCase();
            ps.setString(1, "%" + c + "%");
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next())
                    lista.add(dinRand(rs));
            }
        }
        return lista;
    }

    public Student gasesteDupaId(int id) throws SQLException {
        try (Connection con = Db.getConnection();
             PreparedStatement ps = con.prepareStatement(
                     "SELECT id, nume, varsta, media FROM studenti WHERE id = ?")) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? dinRand(rs) : null;
            }
        }
    }

    public void adauga(String nume, int varsta, double media) throws SQLException {
        try (Connection con = Db.getConnection();
             PreparedStatement ps = con.prepareStatement(
                     "INSERT INTO studenti (nume, varsta, media) VALUES (?, ?, ?)")) {
            ps.setString(1, nume);
            ps.setInt(2, varsta);
            ps.setDouble(3, media);
            ps.executeUpdate();
        }
    }

    public void modifica(int id, String nume, int varsta, double media) throws SQLException {
        try (Connection con = Db.getConnection();
             PreparedStatement ps = con.prepareStatement(
                     "UPDATE studenti SET nume = ?, varsta = ?, media = ? WHERE id = ?")) {
            ps.setString(1, nume);
            ps.setInt(2, varsta);
            ps.setDouble(3, media);
            ps.setInt(4, id);
            ps.executeUpdate();
        }
    }

    public void sterge(int id) throws SQLException {
        try (Connection con = Db.getConnection();
             PreparedStatement ps = con.prepareStatement("DELETE FROM studenti WHERE id = ?")) {
            ps.setInt(1, id);
            ps.executeUpdate();
        }
    }

    // [0] = numarul de studenti, [1] = media generala
    public double[] sumar() throws SQLException {
        try (Connection con = Db.getConnection();
             Statement st = con.createStatement();
             ResultSet rs = st.executeQuery("SELECT COUNT(*), AVG(media) FROM studenti")) {
            rs.next();
            return new double[]{rs.getInt(1), rs.getDouble(2)};
        }
    }

    public Student celMaiBun() throws SQLException {
        try (Connection con = Db.getConnection();
             Statement st = con.createStatement();
             ResultSet rs = st.executeQuery(
                     "SELECT id, nume, varsta, media FROM studenti "
                     + "ORDER BY media DESC FETCH FIRST 1 ROWS ONLY")) {
            return rs.next() ? dinRand(rs) : null;
        }
    }
}