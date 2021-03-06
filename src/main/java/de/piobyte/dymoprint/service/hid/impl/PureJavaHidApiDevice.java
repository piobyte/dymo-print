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
package de.piobyte.dymoprint.service.hid.impl;

import de.piobyte.dymoprint.printer.PrinterConfiguration;
import de.piobyte.dymoprint.printer.Tape;
import de.piobyte.dymoprint.service.hid.HidDevice;
import io.github.jna4usb.purejavahidapi.HidDeviceInfo;
import io.github.jna4usb.purejavahidapi.PureJavaHidApi;
import lombok.RequiredArgsConstructor;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.IntStream;

@Slf4j
@ToString
@RequiredArgsConstructor
public class PureJavaHidApiDevice implements HidDevice {

    private final static byte CMD_ESC = 0x1B;
    private final static byte CMD_SYN = 0x16;
    private final static byte CMD_A = 0x41;
    private final static byte CMD_B = 0x42;
    private final static byte CMD_C = 0x43;
    private final static byte CMD_D = 0x44;
    private final static byte CMD_E = 0x45;

    private final HidDeviceInfo hidDeviceInfo;
    private final PrinterConfiguration printer;
    private boolean open;

    private io.github.jna4usb.purejavahidapi.HidDevice pureJavaHidDevice;

    @Override
    public String getSerialNumber() {
        return hidDeviceInfo.getSerialNumberString();
    }

    @Override
    public PrinterConfiguration getPrinterConfiguration() {
        return printer;
    }

    @Override
    public String getPath() {
        return hidDeviceInfo.getPath();
    }

    @Override
    public synchronized void write(Tape tape, byte[] labelData) throws IOException {
        if (pureJavaHidDevice != null) {
            byte bytesPerLine = printer.getSupportedTapes().get(tape).byteValue();
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();

            // tape color
            byteArrayOutputStream.write(new byte[]{CMD_ESC, CMD_C, (byte) printer.getColor()});

            // height
            byte height;
            if (tape == Tape.D1_6_MM) {
                height = 2;
            } else if (tape == Tape.D1_9_MM) {
                height = 1;
            } else {
                height = 0;
            }
            byteArrayOutputStream.write(new byte[]{CMD_ESC, CMD_B, height});

            // 2 empty lines
            printEmptyLines(2, byteArrayOutputStream);

            // label
            byteArrayOutputStream.write(new byte[]{CMD_ESC, CMD_D, bytesPerLine});
            for (int i = 0; i < labelData.length; i++) {
                if (i % bytesPerLine == 0) {
                    byteArrayOutputStream.write(CMD_SYN);
                }
                byteArrayOutputStream.write(labelData[i]);
            }

            // left margin
            printEmptyLines(printer.getLeftMargin(), byteArrayOutputStream);

            // cut tape
            if (printer.supportsCutting()) {
                cutTape(byteArrayOutputStream);
            }

            byte[] rawData = byteArrayOutputStream.toByteArray();
            divideArray(rawData, 64).forEach(bytes -> {
                pureJavaHidDevice.setOutputReport((byte) 0, bytes, bytes.length);
            });
        }
    }

    @Override
    public synchronized void open() throws IOException {
        close();
        pureJavaHidDevice = PureJavaHidApi.openDevice(hidDeviceInfo);
        open = true;
        pureJavaHidDevice.setInputReportListener((source, reportID, reportData, reportLength) -> {
            log.info("source={}, reportID={}, reportData={}, reportLength={}", source, reportID, reportData, reportLength);
        });
        pureJavaHidDevice.setDeviceRemovalListener(source -> {
            log.info("removed: {}", source);
            open = false;
        });
    }

    @Override
    public synchronized void close() {
        if (pureJavaHidDevice != null) {
            pureJavaHidDevice.setInputReportListener(null);
            pureJavaHidDevice.close();
            open = false;
        }
    }

    private void printEmptyLines(int count, ByteArrayOutputStream byteArrayOutputStream) throws IOException {
        byteArrayOutputStream.write(new byte[]{CMD_ESC, CMD_D, 0});
        IntStream.range(0, count).forEach(value -> byteArrayOutputStream.write(CMD_SYN));
    }

    private void cutTape(ByteArrayOutputStream byteArrayOutputStream) throws IOException {
        byteArrayOutputStream.write(new byte[]{CMD_ESC, CMD_E});
    }

    private void sentStatusCommand() {
        if (pureJavaHidDevice != null) {
            var command = new byte[]{CMD_ESC, CMD_A};
            pureJavaHidDevice.setOutputReport((byte) 0, command, command.length);
        }
    }

    private List<byte[]> divideArray(byte[] source, int chunkSize) {

        List<byte[]> result = new ArrayList<byte[]>();
        int start = 0;
        while (start < source.length) {
            int end = Math.min(source.length, start + chunkSize);
            result.add(Arrays.copyOfRange(source, start, end));
            start += chunkSize;
        }

        return result;
    }
}
