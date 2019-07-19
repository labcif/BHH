package main.pt.ipleiria.estg.dei.labcif.bhh.utils;

import main.pt.ipleiria.estg.dei.labcif.bhh.exceptions.NotSupportedException;
import main.pt.ipleiria.estg.dei.labcif.bhh.models.OperatingSystem;

import java.io.File;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.file.Path;

public class OperatingSystemUtils {
    public static String OS = System.getProperty("os.name").toLowerCase();
    public static String USER = System.getProperty("user.name");

    private OperatingSystemUtils() {
    }

    public static boolean isWindows() {
        return (OS.contains("win"));
    }

    public static boolean isUnix() {
        return (OS.contains("nix") || OS.contains("nux") || OS.indexOf("aix") > 0 );
    }

    public static boolean isMacOs() {
        return OS.contains("mac");
    }


    /**
     * return root location of current disk. Example: C:\Users\nameExample or D:\
     * */
    public static String getRoot() {
        return System.getProperty("user.home");
    }

    public static String getComputerName() {
        try {
            return InetAddress.getLocalHost().getHostName();
        } catch (UnknownHostException e) {
            return "NO_NAME";
        }
    }

    public static OperatingSystem getOS() {
        if (isWindows()) {
            return OperatingSystem.WINDOWS_10;//TODO: change it to support windows xp and 8
        } else if (isUnix()) {
            return OperatingSystem.LINUX;
        } else if (isMacOs()) {
            return OperatingSystem.MAC_OS;
        } else {
            throw new NotSupportedException("Operating system not supported");
        }
    }
}
