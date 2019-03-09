package pt.ipleiria.estg.dei.model;

import pt.ipleiria.estg.dei.exceptions.NotSupportedException;


public class OperatingSystem {
    private static  String operatingSystem= System.getProperty("os.name");

    private OperatingSystem(){
    }

    public static String getLocation(BrowserEnum browser){
        if (isWindows()) {
            return getWindowsLocation(browser);
        }
        throw new NotSupportedException("Operating system not supported:" + operatingSystem);
    }

    private static String getWindowsLocation(BrowserEnum browser){
        StringBuilder sb = new StringBuilder();
        sb.append(System.getenv("SystemDrive"));
        switch (browser) {
            case CHROME:
                sb.append("\\Users\\")
                        .append(System.getProperty("user.name"))
                        .append("\\AppData\\Local\\Google\\Chrome\\User Data\\Default\\History");
                return  sb.toString();
            case FIREFOX:
            default:
                throw new NotSupportedException("Browser not supported: " + browser);

        }
    }

    private static String getLinuxLocation(BrowserEnum browser){
        switch (browser) {
            case CHROME:
                return "/home/" + System.getProperty("user.name") + "/.config/google-chrome/Default/Preferences";
            case FIREFOX:
            default:
                throw new NotSupportedException("Browser not supported: " + browser);

        }
    }

    private static boolean isWindows() {
        return (operatingSystem != null && operatingSystem.toLowerCase().contains("win"));
    }

    private static boolean isMac() {
        return (operatingSystem != null && operatingSystem.toLowerCase().contains("mac"));
    }

    private static boolean isUnix() {
        return operatingSystem != null && (operatingSystem.toLowerCase().contains("nix") ||
                operatingSystem.toLowerCase().contains("nux") ||
                operatingSystem.toLowerCase().indexOf("aix") > 0 );
    }
}
