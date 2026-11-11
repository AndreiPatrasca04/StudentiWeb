package ro.student.studenti;

public class Student {
    private final int id;
    private final String nume;
    private final int varsta;
    private final double media;

    public Student(int id, String nume, int varsta, double media) {
        this.id = id;
        this.nume = nume;
        this.varsta = varsta;
        this.media = media;
    }

    public int getId() { return id; }
    public String getNume() { return nume; }
    public int getVarsta() { return varsta; }
    public double getMedia() { return media; }
}