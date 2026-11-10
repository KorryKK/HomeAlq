package korry.homealq.util.enums;

public enum Titulo {
    HomeAlqApp("HOMEALQ");

    private final String title;

    Titulo(String title){
        this.title = title;
    }

    public String getTitle() {
        return title;
    }
}
