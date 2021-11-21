package de.piobyte.dymoprint.service.print;

import static org.junit.jupiter.api.Assertions.assertEquals;

import de.piobyte.dymoprint.printer.PrinterConfiguration;
import de.piobyte.dymoprint.printer.Tape;
import de.piobyte.dymoprint.printer.impl.LabelManagerPnPConfiguration;

import lombok.extern.slf4j.Slf4j;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.awt.image.BufferedImage;
import java.io.IOException;

import javax.imageio.ImageIO;

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
    void printLabel() throws IOException, InvalidParameterException, PrinterNotFoundException {
        var printerDevice = service.listAvailablePrinters().stream().findFirst().get();
        BufferedImage image = ImageIO.read(getClass().getResourceAsStream("/test.png"));

        service.printLabel(printerDevice.getSerialNumber(), Tape.D1_12_MM, image);
    }
}
