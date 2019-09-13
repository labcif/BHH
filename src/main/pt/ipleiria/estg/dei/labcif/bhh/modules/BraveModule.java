package main.pt.ipleiria.estg.dei.labcif.bhh.modules;

import main.pt.ipleiria.estg.dei.labcif.bhh.exceptions.OperatingSystemNotSupportedException;
import org.sleuthkit.autopsy.ingest.IngestJobContext;

import static main.pt.ipleiria.estg.dei.labcif.bhh.utils.OperatingSystemUtils.*;
import static main.pt.ipleiria.estg.dei.labcif.bhh.utils.OperatingSystemUtils.getRoot;

public class BraveModule extends  ChromeModule {
	public BraveModule(IngestJobContext context, String databaseDirectory) {
		super(context, databaseDirectory);
	}

	public BraveModule(String databaseDirectory) {
		super(databaseDirectory);
	}

	public String getModuleName() {
		return "BRAVE";
	}

	@Override
	public String getPathToBrowserInstallationInWindows10() {
		return "AppData/Local/BraveSoftware/Brave-Browser/User Data";
	}

	@Override
	public String getPathToBrowserInstallationInLinux() {
		return ".config/brave-browser";
	}

	@Override
	public String getFullPathToBrowserInstallationInCurrentMachine() {
		if (isWindows()) {
			return getRoot() + "\\AppData\\Local\\BraveSoftware\\Brave-Browser\\User Data";
		} else if (isUnix()) {
			return  getRoot() + "/.config/brave-browser";
		} else {
			throw new OperatingSystemNotSupportedException();
		}
	}
}
