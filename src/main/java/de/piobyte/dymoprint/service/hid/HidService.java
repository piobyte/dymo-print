package de.piobyte.dymoprint.service.hid;

import java.util.List;

public interface HidService {

    List<HidDevice> listPrinterDevices();
}
