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
