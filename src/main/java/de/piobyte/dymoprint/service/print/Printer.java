package de.piobyte.dymoprint.service.print;

import de.piobyte.dymoprint.printer.Tape;

import lombok.Builder;
import lombok.Value;

import java.util.Map;

@Value
@Builder
public class Printer {
    String name;
    String serialNumber;
    String path;
    Map<Tape, Integer> labelHeight;
}
