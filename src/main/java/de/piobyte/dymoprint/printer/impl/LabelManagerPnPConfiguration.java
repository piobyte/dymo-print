package de.piobyte.dymoprint.printer.impl;

import de.piobyte.dymoprint.printer.PrinterConfiguration;
import de.piobyte.dymoprint.printer.Tape;

import lombok.Value;

import java.util.Map;

@Value
public class LabelManagerPnPConfiguration implements PrinterConfiguration {
    String name = "DYMO LabelManager PnP";
    short vendorId = 0x0922;
    short productId = 0x1002;
    int color = 0;
    int leftMargin = 112;
    Map<Tape, Integer> supportedTapes = Map.of(
            Tape.D1_6_MM, 4,
            Tape.D1_9_MM, 6,
            Tape.D1_12_MM, 8);

    @Override
    public boolean supportsCutting() {
        return false;
    }
}
