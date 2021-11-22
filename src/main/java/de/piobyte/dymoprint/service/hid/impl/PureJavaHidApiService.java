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
import de.piobyte.dymoprint.service.hid.HidDevice;
import de.piobyte.dymoprint.service.hid.HidService;

import io.github.jna4usb.purejavahidapi.HidDeviceInfo;
import io.github.jna4usb.purejavahidapi.PureJavaHidApi;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class PureJavaHidApiService implements HidService {

    private final List<PrinterConfiguration> printerConfigurations;

    public PureJavaHidApiService(List<PrinterConfiguration> printerConfigurations) {
        this.printerConfigurations = printerConfigurations;
    }

    @Override
    public List<HidDevice> listPrinterDevices() {
        return PureJavaHidApi.enumerateDevices().stream()
                .map(this::map)
                .filter(hidDevice -> hidDevice.getPrinterConfiguration() != null)
                .collect(Collectors.toList());
    }

    private Optional<PrinterConfiguration> findPrinterMatch(HidDeviceInfo hidDeviceInfo) {
        short vendorId = hidDeviceInfo.getVendorId();
        short productId = hidDeviceInfo.getProductId();

        return printerConfigurations.stream()
                .filter(printerConfiguration -> printerConfiguration.getVendorId() == vendorId &&
                        printerConfiguration.getProductId() == productId)
                .findFirst();
    }

    private HidDevice map(HidDeviceInfo hidDeviceInfo) {
        PrinterConfiguration printer = findPrinterMatch(hidDeviceInfo).orElse(null);
        return new PureJavaHidApiDevice(hidDeviceInfo, printer);
    }

}
