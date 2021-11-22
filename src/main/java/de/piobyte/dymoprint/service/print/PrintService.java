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

import de.piobyte.dymoprint.printer.PrinterConfiguration;
import de.piobyte.dymoprint.printer.Tape;
import de.piobyte.dymoprint.printer.impl.LabelManagerPnPConfiguration;
import de.piobyte.dymoprint.service.hid.HidDevice;
import de.piobyte.dymoprint.service.hid.HidService;
import de.piobyte.dymoprint.service.hid.impl.PureJavaHidApiService;

import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.BitSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
public class PrintService {

    private final static String PERMISSION_ERROR = " 13";
    private final static int BITS_IN_BYTE = 8;

    private final HidService hidService;

    public PrintService() {
        this(new LabelManagerPnPConfiguration());
    }

    public PrintService(PrinterConfiguration configuration) {
        this(List.of(configuration));
    }

    public PrintService(List<PrinterConfiguration> configurations) {
        hidService = new PureJavaHidApiService(configurations);
    }

    /**
     * List available (connected, powered on) label printers.
     *
     * @return list of printers
     */
    public List<Printer> listAvailablePrinters() {
        return hidService.listPrinterDevices()
                .stream()
                .map(this::map)
                .collect(Collectors.toList());
    }

    /**
     * Print label.
     *
     * The passed image height needs to match the following value:
     * <pre>
     * +------------------+--------------+
     * |  Label cassette  | Image height |
     * +------------------+--------------+
     * | D1 6mm  | 1/4 in | 32 pixel     |
     * | D1 9mm  | 3/8 in | 48 pixel     |
     * | D1 12mm | 1/2 in | 64 pixel     |
     * +------------------+--------------+
     * </pre>
     *
     * @param serialNumber serial number of label printer
     * @param tape installed tape type
     * @param label image of label
     * @throws IOException communication exception
     * @throws InvalidParameterException invalid parameter passed (e.g. unsupported tape)
     * @throws PrinterNotFoundException printer not found
     */
    public void printLabel(@NonNull String serialNumber, @NonNull Tape tape,
                           @NonNull BufferedImage label) throws IOException, InvalidParameterException, PrinterNotFoundException {

        var printerDevice = hidService.listPrinterDevices().stream()
                .filter(hidDevice -> serialNumber.equalsIgnoreCase(hidDevice.getSerialNumber()))
                .findFirst();

        if (printerDevice.isPresent()) {
            var configuration = printerDevice.get().getPrinterConfiguration();
            if (!configuration.getSupportedTapes().containsKey(tape)) {
                log.error("Tape is not supported by printer! tape={}, supportedTapes={}", tape,
                        configuration.getSupportedTapes());
                throw new InvalidParameterException("Tape is not supported by printer!");
            }

            var tapeHeight = printerDevice.get().getPrinterConfiguration().getSupportedTapes().get(tape) * BITS_IN_BYTE;
            if (label.getHeight() != tapeHeight) {
                log.error("Wrong image height! imageHeight={} targetHeight={}", label.getHeight(), tapeHeight);
                throw new InvalidParameterException("Wrong image height!");
            }

            try {
                printerDevice.get().open();
                printerDevice.get().write(tape, convertLabel(label));
                printerDevice.get().close();
            } catch (IOException e) {
                var errorMessage = e.getMessage();
                if (errorMessage != null && errorMessage.contains(PERMISSION_ERROR)) {
                    log.error("Permission denied! You are not allowed to access '{}'. Please grant access to HID.",
                            printerDevice.get().getPath());
                } else {
                    log.error("Could not print label! error={}", e.getMessage());
                }
                throw e;
            }
        } else {
            throw new PrinterNotFoundException("Printer not found! serialNumber=" + serialNumber);
        }
    }

    private byte[] convertLabel(BufferedImage originalLabel) {
        BufferedImage label = convertToBinaryImage(originalLabel);

        var targetByteCount = (label.getHeight() * label.getWidth()) / BITS_IN_BYTE;
        byte[] labelData = new byte[targetByteCount];

        int bytePosition = 0;
        BitSet bitSet = new BitSet(8);
        for (int x = label.getWidth() - 1; x >= 0; x--) {
            for (int y = 0; y < label.getHeight(); y++) {
                int bitPosition = (BITS_IN_BYTE - 1) - (y % BITS_IN_BYTE);
                bitSet.set(bitPosition, label.getRGB(x, y) != Color.WHITE.getRGB());

                if (log.isDebugEnabled()) {
                    log.debug("{},{}: {} (bitPosition:{})", x, y, label.getRGB(x, y) != Color.WHITE.getRGB(), bitPosition);
                }

                if (bitPosition == 0) {
                    byte[] byteArray = bitSet.toByteArray();
                    labelData[bytePosition++] = byteArray.length == 0 ? 0 : byteArray[0];
                    bitSet.clear();
                }
            }
        }
        return labelData;
    }

    private BufferedImage convertToBinaryImage(BufferedImage originalLabel) {
        BufferedImage binaryLabel = new BufferedImage(originalLabel.getWidth(), originalLabel.getHeight(),
                BufferedImage.TYPE_BYTE_BINARY);
        Graphics2D graphic = binaryLabel.createGraphics();
        graphic.drawImage(originalLabel, 0, 0, Color.WHITE, null);
        graphic.dispose();

        return binaryLabel;
    }

    private Printer map(HidDevice hidDevice) {
        return Printer.builder()
                .name(hidDevice.getPrinterConfiguration().getName())
                .serialNumber(hidDevice.getSerialNumber())
                .path(hidDevice.getPath())
                .labelHeight(mapToLabelHeightInPixel(hidDevice.getPrinterConfiguration().getSupportedTapes()))
                .build();
    }

    private Map<Tape, Integer> mapToLabelHeightInPixel(Map<Tape, Integer> supportedTapes) {
        HashMap<Tape, Integer> heightMap = new HashMap<>();
        supportedTapes.keySet().forEach(tape -> heightMap.put(tape, supportedTapes.get(tape) * BITS_IN_BYTE));
        return heightMap;
    }
}
