package de.piobyte.dymoprint.printer;

import java.util.Map;

public interface PrinterConfiguration {

    /**
     * Printer label.
     *
     * @return label
     */
    String getName();

    /**
     * USB vendor id.
     *
     * @return id
     */
    short getVendorId();

    /**
     * USB product id.
     *
     * @return id
     */
    short getProductId();

    /**
     * Printing color.
     *
     * @return color (RGB)
     */
    int getColor();

    /**
     * Left margin.
     *
     * @return margin in pixel
     */
    int getLeftMargin();

    /**
     * Get supported tapes with appropriate information about bytes per line.
     *
     * @return supported tapes
     */
    Map<Tape, Integer> getSupportedTapes();

    /**
     * Printer is able to cut tape.
     *
     * @return is supported or not
     */
    boolean supportsCutting();
}
