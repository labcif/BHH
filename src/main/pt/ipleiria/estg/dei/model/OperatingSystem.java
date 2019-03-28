package main.pt.ipleiria.estg.dei.model;

import main.pt.ipleiria.estg.dei.exceptions.NotSupportedException;


public class OperatingSystem {

    private OperatingSystem(){
    }

    public static String getLocation(BrowserEnum browser){
        String operatingSystem = System.getenv("OS");
        if (isWindows(operatingSystem)) {
            return getWindowsLocation(browser);
        }
        if(isMac(operatingSystem)){
            return getLinuxLocation(browser);
        }
        if(isUnix(operatingSystem)){
            return getLinuxLocation(browser);
        }
        throw new NotSupportedException("Operating system not supported:" + operatingSystem);
    }

    //will change (eventually)
    public static String getLocationEmail(BrowserEnum browser){
        String operatingSystem = System.getenv("OS");
        if (isWindows(operatingSystem)) {
            return System.getenv("SystemDrive") + "\\Users\\" + System.getenv("USERNAME")  +"\\AppData\\Local\\Google\\Chrome\\User Data\\Default\\Login Data";
        }
        throw new NotSupportedException("Operating system not supported:" + operatingSystem);
    }

    private static String getWindowsLocation(BrowserEnum browser){
        StringBuilder sb = new StringBuilder();
        sb.append(System.getenv("SystemDrive"));
        switch (browser) {
            case CHROME:
                sb.append("\\Users\\")
                        .append(System.getenv("USERNAME"))
                        .append("\\AppData\\Local\\Google\\Chrome\\User Data\\Default\\History");
                return  sb.toString();
            case BRAVE:
                sb.append("\\Users\\")
                        .append(System.getenv("USERNAME"))
                        .append("\\AppData\\Local\\raveSoftware\\Brave-Browser\\User Data\\Default\\History");
                return  sb.toString();
            case VIVALDI:
                sb.append("\\Users\\")
                        .append(System.getenv("USERNAME"))
                        .append("\\AppData\\Local\\Vivaldi\\User Data\\Default\\History");
                return  sb.toString();
            case FIREFOX:
                sb.append("\\Users\\")
                        .append(System.getProperty("user.name"))
                        .append("\\AppData\\Roaming\\Mozilla\\Firefox\\Profiles\\%PROFILE%.default\\places.sqlite"); //Todo needs to select profile
                return  sb.toString();
            case SAFARI:
                sb.append("\\Users\\")
                        .append(System.getProperty("user.name"))
                        .append("\\AppData\\Roaming\\Apple Computer\\Safari");
                return  sb.toString();
            default:
                throw new NotSupportedException("Browser not supported: " + browser);

        }
    }

    private static String getLinuxLocation(BrowserEnum browser){
        StringBuilder sb = new StringBuilder();
        sb.append(System.getenv("/home/"));
        switch (browser) {
            case CHROME:
                sb.append(System.getProperty("user.name"))
                    .append("/.config/google-chrome/Default/Preferences");
            case BRAVE:
                sb.append("\\Users\\")
                        .append(System.getProperty("user.name"))
                        .append("/.config/brave/Default/Preferences");
                return  sb.toString();
            case VIVALDI:
                sb.append("\\Users\\")
                        .append(System.getProperty("user.name"))
                        .append("/.config/vivaldi/Default/Preferences");
                return  sb.toString();
            case FIREFOX:
                sb.append(System.getProperty("user.name"))
                        .append("/.mozilla/firefox/$PROFILE.default/places.sqlite"); //Todo needs to select profile
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
