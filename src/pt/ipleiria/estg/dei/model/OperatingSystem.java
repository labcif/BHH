package pt.ipleiria.estg.dei.model;

import pt.ipleiria.estg.dei.exceptions.NotSupportedException;


public class OperatingSystem {

    private OperatingSystem(){
    }

    public static String getLocation(BrowserEnum browser){
        String operatingSystem = System.getenv("os.name");
        if (isWindows(operatingSystem)) {
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
                        .append(System.getenv("user.name"))
                        .append("\\AppData\\Local\\Google\\Chrome\\User Data\\Default\\History");
                return  sb.toString();
            case BRAVE:
                sb.append("\\Users\\")
                        .append(System.getenv("user.name"))
                        .append("\\AppData\\Local\\raveSoftware\\Brave-Browser\\User Data\\Default\\History");
                return  sb.toString();
            case VIVALDI:
                sb.append("\\Users\\")
                        .append(System.getenv("user.name"))
                        .append("\\AppData\\Local\\Vivaldi\\User Data\\Default\\History");
                return  sb.toString();
            case FIREFOX:
            default:
                throw new NotSupportedException("Browser not supported: " + browser);

        }
    }

    private static String getLinuxLocation(BrowserEnum browser){
        switch (browser) {
            case CHROME:
                return "/home/" + System.getenv("user.name") + "/.config/google-chrome/Default/Preferences";
            case FIREFOX:
            default:
                throw new NotSupportedException("Browser not supported: " + browser);

        }
    }

    private static boolean isWindows(String operatingSystem) {
        return (operatingSystem != null && operatingSystem.toLowerCase().contains("win"));
    }

    private static boolean isMac(String operatingSystem) {
        return (operatingSystem != null && operatingSystem.toLowerCase().contains("mac"));
    }

    private static boolean isUnix(String operatingSystem) {
        return operatingSystem != null && (operatingSystem.toLowerCase().contains("nix") ||
                operatingSystem.toLowerCase().contains("nux") ||
                operatingSystem.toLowerCase().indexOf("aix") > 0 );
    }
}
