
package pt.ipleiria.estg.dei.model;

import main.pt.ipleiria.estg.dei.model.BrowserEnum;
import main.pt.ipleiria.estg.dei.model.OperatingSystem;
import org.junit.*;
import org.junit.contrib.java.lang.system.EnvironmentVariables;
import main.pt.ipleiria.estg.dei.exceptions.NotSupportedException;

import java.util.HashMap;

import static main.pt.ipleiria.estg.dei.model.BrowserEnum.*;
import static org.junit.Assert.assertEquals;


public class WindowOperatingSystemTest {
    @Rule
    public final EnvironmentVariables environmentVariables = new EnvironmentVariables();

    private static final HashMap<BrowserEnum, String> windowsLocations = new HashMap<>();
    
    @BeforeClass
    public static void setUpClass() {
        windowsLocations.put(CHROME,  "C:\\Users\\john\\AppData\\Local\\Google\\Chrome\\User Data\\Default\\History");
        windowsLocations.put(BRAVE,  "C:\\Users\\john\\AppData\\Local\\raveSoftware\\Brave-Browser\\User Data\\Default\\History");
        windowsLocations.put(VIVALDI,  "C:\\Users\\john\\AppData\\Local\\Vivaldi\\User Data\\Default\\History");
        windowsLocations.put(FIREFOX,  "TODO");//TODO: yet to complete
    }
    
    @AfterClass
    public static void tearDownClass() {
    }
    
    @Before
    public void setUp() {
        setValidEnvVariables();
    }

    private void setValidEnvVariables() {
        environmentVariables.set("user.name", "john");
        environmentVariables.set("os.name", "Windows 10");
    }

    @After
    public void tearDown() {
    }

    @Test(expected = NotSupportedException.class)
    public void test_system_not_supported() {
        environmentVariables.set("user.name", "john");
        environmentVariables.set("os.name", "Not exists...");
        OperatingSystem.getLocation(CHROME);
    }

    @Test
    public void test_windows_google_chrome_location() {
        String expectedLocation = windowsLocations.get(CHROME);
        String location = OperatingSystem.getLocation(CHROME);
        assertEquals(expectedLocation, location);
    }

    @Test
    public void test_windows_brave_location() {
        String expectedLocation = windowsLocations.get(BRAVE);
        String location = OperatingSystem.getLocation(BRAVE);
        assertEquals(expectedLocation, location);
    }

    @Test
    public void test_windows_vivaldi_location() {
        String expectedLocation = windowsLocations.get(VIVALDI);
        String location = OperatingSystem.getLocation(VIVALDI);
        assertEquals(expectedLocation, location);
    }

    @Test(expected = NotSupportedException.class)//TODO: yet to complete
    public void test_windows_firefox_location() {
        String expectedLocation = windowsLocations.get(FIREFOX);
        String location = OperatingSystem.getLocation(FIREFOX);
        assertEquals(expectedLocation, location);
    }


}
