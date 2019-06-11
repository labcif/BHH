package main.pt.ipleiria.estg.dei.labcif.bhh.exceptions;

import static main.pt.ipleiria.estg.dei.labcif.bhh.utils.OperatingSystemUtils.OS;

public class OperatingSystemNotSupportedException extends BrowserHistoryIngestModuleExpection {

    public OperatingSystemNotSupportedException() {
        super("Operating system not supported. Yours:" + OS);
    }
}
