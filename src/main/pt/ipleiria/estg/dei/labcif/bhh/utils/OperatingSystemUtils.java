package main.pt.ipleiria.estg.dei.labcif.bhh.utils;

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
     * return root location of current disk. Example: C:\ or D:\
     * */
    public static String getRoot() {
        File currDir = new File(System.getProperty("user.dir", "."));
        Path root = currDir.toPath().getRoot();
        return root.toString();
    }

    public static String getComputerName() {
        try {
            return InetAddress.getLocalHost().getHostName();
        } catch (UnknownHostException e) {
            return "NO_NAME";
        }
    }
}
