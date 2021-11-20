package de.piobyte.dymoprint.printer;

public enum Tape {
    D1_6_MM("Dymo D1 Labeling Tape (6 mm | ¼ in)"),
    D1_9_MM("Dymo D1 Labeling Tape (9 mm | ⅜ in)"),
    D1_12_MM("Dymo D1 Labeling Tape (12 mm | ½ in)");

    private String description;

    Tape(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
