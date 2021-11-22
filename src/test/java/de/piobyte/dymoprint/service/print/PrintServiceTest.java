/*
 * Copyright (C) 2021 piobyte GmbH
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package de.piobyte.dymoprint.service.print;

import static org.junit.jupiter.api.Assertions.fail;

import de.piobyte.dymoprint.printer.PrinterConfiguration;
import de.piobyte.dymoprint.printer.Tape;
import de.piobyte.dymoprint.printer.impl.LabelManagerPnPConfiguration;

import lombok.extern.slf4j.Slf4j;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
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
    public void listAvailablePrinters() {
        var result = service.listAvailablePrinters();
        log.info("Found: {}", result);
    }

    @Test
    void printLabel() throws IOException {
        printLabel("/test.png", Tape.D1_12_MM);
    }

    @Test
    @Disabled
    void printLongLabel() throws IOException {
        printLabel("/test-long.png", Tape.D1_12_MM);
    }

    @Test
    @Disabled
    void print9Label() throws IOException {
        printLabel("/test-9mm.png", Tape.D1_9_MM);
    }

    @Test
    @Disabled
    void print6Label() throws IOException {
        printLabel("/test-6mm.png", Tape.D1_6_MM);
    }

    private void printLabel(String resourcePath, Tape tape) throws IOException {
        BufferedImage image = ImageIO.read(getClass().getResourceAsStream(resourcePath));
        service.listAvailablePrinters()
                .forEach(printer -> {
                    try {
                        service.printLabel(printer.getSerialNumber(), tape, image);
                    } catch (IOException | InvalidParameterException | PrinterNotFoundException e) {
                        e.printStackTrace();
                        fail();
                    }
                });
    }
}
