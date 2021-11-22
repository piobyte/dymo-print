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
package de.piobyte.dymoprint.printer.impl;

import de.piobyte.dymoprint.printer.PrinterConfiguration;
import de.piobyte.dymoprint.printer.Tape;

import lombok.Value;

import java.util.Map;

@Value
public class LabelManagerPnPConfiguration implements PrinterConfiguration {
    String name = "DYMO LabelManager PnP";
    short vendorId = 0x0922;
    short productId = 0x1002;
    int color = 0;
    int leftMargin = 112;
    Map<Tape, Integer> supportedTapes = Map.of(
            Tape.D1_6_MM, 4,
            Tape.D1_9_MM, 6,
            Tape.D1_12_MM, 8);

    @Override
    public boolean supportsCutting() {
        return false;
    }
}
