package de.piobyte.dymoprint.service.hid;

import de.piobyte.dymoprint.printer.PrinterConfiguration;
import de.piobyte.dymoprint.printer.Tape;

import java.io.IOException;

public interface HidDevice {
    /**
     * Get serial number of printer.
     *
     * @return serial number
     */
    String getSerialNumber();

    /**
     * Get printer configuration.
     *
     * @return configuration model
     */
    PrinterConfiguration getPrinterConfiguration();

    /**
     * Get system path of HID.
     *
     * @return path
     */
    String getPath();

    /**
     * Open a connection.
     *
     * @throws IOException communication problem
     */
    void open() throws IOException;

    /**
     * Close connection.
     */
    void close();

    /**
     * Write label data matrix.
     *
     * @param tape installed tape
     * @param bytes bit-matrix of label
     * @throws IOException communication problem
     */
    void write(Tape tape, byte[] bytes) throws IOException;
}
