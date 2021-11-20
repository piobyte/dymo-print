package de.piobyte.dymoprint.service.print;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class Printer {
    String name;
    String serialNumber;
    String path;
}
