package de.piobyte.dymoprint.service.print;

import de.piobyte.dymoprint.printer.PrinterConfiguration;
import de.piobyte.dymoprint.printer.Tape;
import de.piobyte.dymoprint.printer.impl.LabelManagerPnPConfiguration;
import de.piobyte.dymoprint.service.hid.HidDevice;
import de.piobyte.dymoprint.service.hid.HidService;
import de.piobyte.dymoprint.service.hid.impl.PureJavaHidApiService;

import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Slf4j
public class PrintService {

    private final static String PERMISSION_ERROR = " 13";

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

    // TODO payload, not found/available and javadoc
    public void printLabel(@NonNull String serialNumber, @NonNull Tape tape) throws IOException, InvalidParameterException {
        // TODO use other model
        int rows = 128;
        byte[] labelData = new byte[rows * 8];
        IntStream.range(0, rows * 8).forEach(value -> labelData[value] = 0xffffffff);

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

            // TODO check label height

            try {
                printerDevice.get().open();
                printerDevice.get().write(tape, labelData);
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
        }
    }

    private Printer map(HidDevice hidDevice) {
        return Printer.builder()
                .name(hidDevice.getPrinterConfiguration().getName())
                .serialNumber(hidDevice.getSerialNumber())
                .path(hidDevice.getPath())
                .build();
    }
}
