package de.piobyte.dymoprint.service.print;

import static org.junit.jupiter.api.Assertions.*;

import de.piobyte.dymoprint.printer.PrinterConfiguration;
import de.piobyte.dymoprint.printer.Tape;
import de.piobyte.dymoprint.printer.impl.LabelManagerPnPConfiguration;
import de.piobyte.dymoprint.printer.impl.MockedPrinterConfiguration;

import lombok.extern.slf4j.Slf4j;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;

@Slf4j
class PrintServiceTest {

    private PrintService service;
    private PrinterConfiguration configuration = new LabelManagerPnPConfiguration();

    @BeforeEach
    void setUp() {
        service = new PrintService(configuration);
    }

    @Test
    void listAvailablePrinters() {
        var result = service.listAvailablePrinters();
        log.info("Found: {}", result);

        assertEquals(1, result.size());
        assertEquals(configuration.getName(), result.get(0).getName());
    }

    @Test
    void printLabel() throws IOException, InvalidParameterException {
        service.printLabel("06260808092021", Tape.D1_12_MM);
    }
}
