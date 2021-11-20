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
