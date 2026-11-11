package ro.student.studenti;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;
import java.util.List;
import java.util.Locale;

@WebServlet(name = "StudentiServlet", urlPatterns = {"/studenti"})
public class StudentiServlet extends HttpServlet {

    private final StudentDao dao = new StudentDao();

    // ---------- utilitare ----------
    private static String esc(String s) {   // evita ca textul introdus sa fie interpretat ca HTML
        if (s == null) return "";
        return s.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;")
                .replace("\"", "&quot;").replace("'", "&#39;");
    }

    private static String enc(String s) {
        return URLEncoder.encode(s == null ? "" : s, StandardCharsets.UTF_8);
    }

    private static String fmt(double media) {
        return String.format(Locale.US, "%.2f", media);
    }

    // ---------- GET: afisarea paginii ----------
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        req.setCharacterEncoding("UTF-8");
        resp.setContentType("text/html;charset=UTF-8");

        String q = req.getParameter("q") == null ? "" : req.getParameter("q");
        String sort = req.getParameter("sort") == null ? "" : req.getParameter("sort");
        String edit = req.getParameter("edit");
        String mesaj = req.getParameter("mesaj");
        String eroare = req.getParameter("eroare");
        String ctx = req.getContextPath() + "/studenti";

        try (PrintWriter out = resp.getWriter()) {
            out.println("""
                <!DOCTYPE html>
                <html lang="ro"><head><meta charset="UTF-8">
                <title>Evidența studenților</title>
                <style>
                  body { font-family: Arial, sans-serif; margin: 30px auto; max-width: 850px; background: #f4f6f8; }
                  h1 { color: #1f4e79; }
                  table { border-collapse: collapse; width: 100%; background: white; }
                  th, td { border: 1px solid #ccc; padding: 8px; text-align: left; }
                  th { background: #1f4e79; color: white; }
                  th a { color: white; text-decoration: none; }
                  tr:nth-child(even) { background: #f0f4f8; }
                  .bun { color: #0a7a2f; font-weight: bold; }
                  .ok { background: #d9f2e0; padding: 8px; border-radius: 4px; }
                  .eroare { background: #f8d7da; padding: 8px; border-radius: 4px; }
                  .stat { background: white; padding: 10px; border-left: 5px solid #1f4e79; margin: 15px 0; }
                  form.inline { display: inline; }
                  input { padding: 5px; margin-right: 8px; }
                  button { padding: 5px 10px; cursor: pointer; }
                </style></head><body>
                <h1>Evidența studenților</h1>
                """);

            if (mesaj != null)
                out.println("<p class=\"ok\">" + esc(mesaj) + "</p>");
            if (eroare != null)
                out.println("<p class=\"eroare\">" + esc(eroare) + "</p>");

            try {
                // --- statistici (calculate de baza de date) ---
                double[] sumar = dao.sumar();
                Student best = dao.celMaiBun();
                out.println("<div class=\"stat\">Studenți: <b>" + (int) sumar[0]
                        + "</b> &nbsp;|&nbsp; Media generală: <b>" + fmt(sumar[1]) + "</b>");
                if (best != null)
                    out.println(" &nbsp;|&nbsp; Cel mai bun: <b>" + esc(best.getNume())
                            + "</b> (" + fmt(best.getMedia()) + ")");
                out.println("</div>");

                // --- cautare ---
                out.println("<form method=\"get\" action=\"" + ctx + "\">"
                        + "Caută după nume: <input name=\"q\" value=\"" + esc(q) + "\">"
                        + "<input type=\"hidden\" name=\"sort\" value=\"" + esc(sort) + "\">"
                        + "<button type=\"submit\">Caută</button> "
                        + "<a href=\"" + ctx + "\">Resetează</a></form><br>");

                // --- tabelul ---
                String baza = ctx + "?q=" + enc(q) + "&sort=";
                out.println("<table><tr><th><a href=\"" + baza + "\">ID</a></th>"
                        + "<th><a href=\"" + baza + "nume\">Nume ▲</a></th>"
                        + "<th><a href=\"" + baza + "varsta\">Vârsta ▲</a></th>"
                        + "<th><a href=\"" + baza + "media\">Media ▼</a></th>"
                        + "<th>Acțiuni</th></tr>");

                List<Student> lista = dao.gaseste(q, sort);
                for (Student s : lista) {
                    out.println("<tr><td>" + s.getId() + "</td><td>" + esc(s.getNume())
                            + "</td><td>" + s.getVarsta() + "</td><td"
                            + (s.getMedia() >= 9 ? " class=\"bun\"" : "") + ">" + fmt(s.getMedia()) + "</td><td>"
                            + "<a href=\"" + ctx + "?edit=" + s.getId() + "\">Editează</a> "
                            + "<form class=\"inline\" method=\"post\" action=\"" + ctx + "\" "
                            + "onsubmit=\"return confirm('Ștergi studentul?');\">"
                            + "<input type=\"hidden\" name=\"actiune\" value=\"sterge\">"
                            + "<input type=\"hidden\" name=\"id\" value=\"" + s.getId() + "\">"
                            + "<button type=\"submit\">Șterge</button></form></td></tr>");
                }
                if (lista.isEmpty())
                    out.println("<tr><td colspan=\"5\">Niciun student găsit.</td></tr>");
                out.println("</table>");

                // --- formular adaugare / modificare ---
                Student deEditat = null;
                if (edit != null && edit.matches("\\d+"))
                    deEditat = dao.gasesteDupaId(Integer.parseInt(edit));
                boolean editare = deEditat != null;

                out.println("<h2>" + (editare ? "Modifică student" : "Adaugă student") + "</h2>");
                out.println("<form method=\"post\" action=\"" + ctx + "\">"
                        + "<input type=\"hidden\" name=\"actiune\" value=\"" + (editare ? "modifica" : "adauga") + "\">");
                if (editare)
                    out.println("<input type=\"hidden\" name=\"id\" value=\"" + deEditat.getId() + "\">");
                out.println("Nume: <input name=\"nume\" maxlength=\"50\" required value=\""
                        + esc(editare ? deEditat.getNume() : "") + "\">"
                        + "Vârsta: <input name=\"varsta\" type=\"number\" min=\"16\" max=\"99\" required style=\"width:70px\" value=\""
                        + (editare ? deEditat.getVarsta() : "") + "\">"
                        + "Media: <input name=\"media\" type=\"number\" step=\"0.01\" min=\"1\" max=\"10\" required style=\"width:80px\" value=\""
                        + (editare ? fmt(deEditat.getMedia()) : "") + "\">"
                        + "<button type=\"submit\">" + (editare ? "Salvează modificarea" : "Adaugă") + "</button>");
                if (editare)
                    out.println(" <a href=\"" + ctx + "\">Anulează</a>");
                out.println("</form>");

            } catch (SQLException ex) {
                out.println("<p class=\"eroare\">Eroare la baza de date: " + esc(ex.getMessage())
                        + "<br>Verifică dacă serverul Derby este pornit.</p>");
            }
            out.println("</body></html>");
        }
    }

    // ---------- POST: adaugare / modificare / stergere ----------
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        req.setCharacterEncoding("UTF-8");
        String actiune = req.getParameter("actiune");
        String ctx = req.getContextPath() + "/studenti";

        try {
            if ("sterge".equals(actiune)) {
                dao.sterge(Integer.parseInt(req.getParameter("id")));
                resp.sendRedirect(ctx + "?mesaj=" + enc("Student șters."));
                return;
            }

            if ("adauga".equals(actiune) || "modifica".equals(actiune)) {
                String nume = req.getParameter("nume") == null ? "" : req.getParameter("nume").trim();
                int varsta = Integer.parseInt(req.getParameter("varsta").trim());
                double media = Double.parseDouble(req.getParameter("media").trim().replace(',', '.'));

                // validari
                if (nume.isEmpty() || nume.length() > 50)
                    throw new IllegalArgumentException("Numele trebuie să aibă între 1 și 50 de caractere.");
                if (varsta < 16 || varsta > 99)
                    throw new IllegalArgumentException("Vârsta trebuie să fie între 16 și 99.");
                if (media < 1 || media > 10)
                    throw new IllegalArgumentException("Media trebuie să fie între 1 și 10.");

                if ("adauga".equals(actiune)) {
                    dao.adauga(nume, varsta, media);
                    resp.sendRedirect(ctx + "?mesaj=" + enc("Student adăugat."));
                } else {
                    dao.modifica(Integer.parseInt(req.getParameter("id")), nume, varsta, media);
                    resp.sendRedirect(ctx + "?mesaj=" + enc("Student modificat."));
                }
                return;
            }
            resp.sendRedirect(ctx);

        } catch (NumberFormatException ex) {      // trebuie prins inaintea lui IllegalArgumentException
            resp.sendRedirect(ctx + "?eroare=" + enc("Vârsta și media trebuie să fie numere."));
        } catch (IllegalArgumentException ex) {
            resp.sendRedirect(ctx + "?eroare=" + enc(ex.getMessage()));
        } catch (SQLException ex) {
            resp.sendRedirect(ctx + "?eroare=" + enc("Eroare la baza de date: " + ex.getMessage()));
        }
    }
}