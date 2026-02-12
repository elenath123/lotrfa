package lotrfa.common.enums;

public enum SilmarilsType {
    VARDA("varda"),
    EARENDIL("earendil"),
    MAEDHROS("maedhros");

    private final String name;

    SilmarilsType(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }
}
