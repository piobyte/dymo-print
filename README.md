# dymo-print
Java library that supports Dymo label printers and enables labels to be created by Java applications.

Thanks to the Python-based project dymoprint (https://github.com/computerlyrik/dymoprint), this library could be implemented. 
I would like to thank the developers sincerely.

The <a href="https://github.com/nyholku/purejavahidapi" target ="purejavahidapi">Pure Java HID-API</a> library 
is used to communicate with the printer.

## Supported printers
- DYMO LabelManager PnP

## Supported Platforms
- Linux
- Mac OS
- Windows

## Code samples
### List attached (USB) printers
```java
PrintService service = new PrintService(configuration);
service.listAvailablePrinters();
```

### Print label
```java
PrintService service = new PrintService(configuration);
    BufferedImage image = ImageIO.read(getClass().getResourceAsStream("/test.png"));
    service.listAvailablePrinters()
        .forEach(printer -> {
            try {
                service.printLabel(printer.getSerialNumber(), Tape.D1_12_MM, image);
            } catch (IOException | InvalidParameterException | PrinterNotFoundException e) {
                e.printStackTrace();
            }
        });
```

## Setup

### Linux
#### Ubuntu or Debian based System
TODO
#### Arch Linux
TODO

