package main.pt.ipleiria.estg.dei.labcif.bhh.modules;

import main.pt.ipleiria.estg.dei.labcif.bhh.exceptions.OperatingSystemNotSupportedException;
import org.sleuthkit.autopsy.ingest.IngestJobContext;

import static main.pt.ipleiria.estg.dei.labcif.bhh.utils.OperatingSystemUtils.*;
import static main.pt.ipleiria.estg.dei.labcif.bhh.utils.OperatingSystemUtils.getRoot;

public class VivaldiModule extends ChromeModule {
	public VivaldiModule(IngestJobContext context, String databaseDirectory) {
		super(context, databaseDirectory);
	}

	public VivaldiModule(String databaseDirectory) {
		super(databaseDirectory);
	}

	public String getModuleName() {
		return "VIVALDI";
	}

	@Override
	public String getPathToBrowserInstallationInWindows10() {
		return "AppData/Local/VivaldiModule/User Data";
	}

	@Override
	public String getPathToBrowserInstallationInLinux() {
		return ".config/vivaldi";
	}

	@Override
	public String getFullPathToBrowserInstallationInCurrentMachine() {
		if (isWindows()) {
			return getRoot() + "AppData\\Local\\VivaldiModule\\User Data";
		} else if (isUnix()) {
			return  getRoot() + "/.config/vivaldi";
		} else {
			throw new OperatingSystemNotSupportedException();
		}
	}
}
