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

import com.google.zxing.BarcodeFormat;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.oned.Code128Writer;

import de.piobyte.dymoprint.printer.PrinterConfiguration;
import de.piobyte.dymoprint.printer.Tape;
import de.piobyte.dymoprint.printer.impl.LabelManagerPnPConfiguration;

import lombok.extern.slf4j.Slf4j;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.awt.Color;
import java.awt.Font;
import java.awt.FontFormatException;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.font.FontRenderContext;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.text.DecimalFormat;
import java.text.NumberFormat;

import javax.imageio.ImageIO;

@Slf4j
class PrintServiceBarcodeTest {

    private PrintService service;
    private PrinterConfiguration configuration = new LabelManagerPnPConfiguration();

    @BeforeEach
    void setUp() {
        service = new PrintService(configuration);
    }


    @Test
    @Disabled
    void printBarcodeLabel() throws IOException, FontFormatException {
        // settings
        Tape tape = Tape.D1_12_MM;
        boolean preview = true;
        String nameLabel = "Apple iPhone X Pro";
        String font = "Arial";
        int size = 16;

        // calc values
        int height = getPrinterHeight(tape);
        int nameLabelWidth = getTextWidth(font, size, "N: " + nameLabel);
        String numberString = generateRandomNumberString();
        BufferedImage barcode = createBarcode(numberString, 200, height);
        BufferedImage logo = loadLogo();

        // Combine images
        int width = (int) (barcode.getWidth() + Math.max(logo.getWidth() + 10, (nameLabelWidth * 0.98)  - 3) + 5);
        BufferedImage label = new BufferedImage(width, height, BufferedImage.TYPE_BYTE_BINARY);
        Graphics2D g2 = label.createGraphics();
        g2.setPaint(Color.WHITE);
        g2.fillRect(0, 0, width, height);
        g2.drawImage(barcode, null, 0, 0);
        g2.drawImage(logo, null, barcode.getWidth() + 5, 0);
        g2.dispose();

        // draw text
        drawText(label, barcode.getWidth() + 5, label.getHeight() - 20, font, size, "N");
        drawText(label, barcode.getWidth() + 5, label.getHeight() - 3, font, size, "T");

        drawText(label, barcode.getWidth() + 5 + 12, label.getHeight() - 20, font, size, ": " + nameLabel);
        drawText(label, barcode.getWidth() + 5 + 12, label.getHeight() - 3, font, size, ": " + numberString);


        printLabel(label, tape, preview);
    }

    private int getPrinterHeight(Tape tape) {
        if (Tape.D1_12_MM.equals(tape)) {
            return 64;
        } else if (Tape.D1_9_MM.equals(tape)) {
            return 48;
        } else {
            return 32;
        }
    }

    private BufferedImage createBarcode(String text, int width, int height) {
        Code128Writer barcodeWriter = new Code128Writer();
        BitMatrix bitMatrix = barcodeWriter.encode(text, BarcodeFormat.CODE_128, width, height);
        return MatrixToImageWriter.toBufferedImage(bitMatrix);
    }

    private String generateRandomNumberString() {
        int n = (int)Math.floor( Math.random() * 100000 + 1 );
        NumberFormat formatter = new DecimalFormat("000000");
        return "p" + formatter.format(n);
    }

    private void drawText(BufferedImage image, int x, int y, String fontName, int size, String text) throws IOException, FontFormatException {
        Font font = new Font(fontName, Font.BOLD, size);

        Graphics g = image.getGraphics();
        g.setFont(font);
        g.setColor(Color.BLACK);
        g.drawString(text, x, y);
        g.dispose();
    }

    private int getTextWidth(String fontName, int size, String text) {
        AffineTransform affinetransform = new AffineTransform();
        FontRenderContext frc = new FontRenderContext(affinetransform,true,true);
        Font font = new Font(fontName, Font.BOLD, size);

        return (int)(font.getStringBounds(text, frc).getWidth());
    }

    private BufferedImage loadLogo() throws IOException {
        return ImageIO.read(getClass().getResourceAsStream("/logo-height-28.png"));
    }

    private void printLabel(BufferedImage label, Tape tape, boolean preview) {
        if (preview) {
            try {
                File outputFile = File.createTempFile("label_", ".png");
                ImageIO.write(label, "png", outputFile);
                log.info("Created image. {}", outputFile.getAbsolutePath());
            } catch (Exception e) {
                fail("Exception", e);
            }
        } else {
            service.listAvailablePrinters()
                    .forEach(printer -> {
                        try {
                            service.printLabel(printer.getSerialNumber(), tape, label);
                        } catch (IOException | InvalidParameterException | PrinterNotFoundException e) {
                            e.printStackTrace();
                            fail();
                        }
                    });
        }
    }
}
