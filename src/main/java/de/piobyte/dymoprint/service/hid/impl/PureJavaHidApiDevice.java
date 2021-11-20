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
            byteArrayOutputStream.write(new byte[]{CMD_ESC, CMD_B, 0});

            // 2 empty lines
            printEmptyLines(2, byteArrayOutputStream);

            // label
            byteArrayOutputStream.write(new byte[]{CMD_ESC, CMD_D, bytesPerLine});
            for (int i = 0; i < labelData.length; i++) {
                if (i % 8 == 0) {
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
            pureJavaHidDevice.setOutputReport((byte) 0, rawData, rawData.length);
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
}
