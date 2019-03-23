
package pt.ipleiria.estg.dei.model;

import main.pt.ipleiria.estg.dei.model.BrowserEnum;
import main.pt.ipleiria.estg.dei.model.OperatingSystem;
import org.junit.*;
import org.junit.contrib.java.lang.system.EnvironmentVariables;
import main.pt.ipleiria.estg.dei.exceptions.NotSupportedException;

import java.util.HashMap;

import static main.pt.ipleiria.estg.dei.model.BrowserEnum.*;


public class LinuxOperatingSystemTest {
    @Rule
    public final EnvironmentVariables environmentVariables = new EnvironmentVariables();

    private static final HashMap<BrowserEnum, String> linuxLocations = new HashMap<>();
    
    @BeforeClass
    public static void setUpClass() {
        linuxLocations.put(CHROME,  "TODO");//TODO: yet to complete
        linuxLocations.put(BRAVE,  "TODO");//TODO: yet to complete
        linuxLocations.put(VIVALDI,  "TODO");//TODO: yet to complete
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
        environmentVariables.set("os.name", "linux");
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

    @Test(expected = NotSupportedException.class)//TODO: yet to complete
    public void test_linux_google_chrome_location() {
        String expectedLocation = linuxLocations.get(CHROME);
        String location = OperatingSystem.getLocation(CHROME);
        //assertEquals(expectedLocation, location);
    }

    @Test(expected = NotSupportedException.class)//TODO: yet to complete
    public void test_linux_brave_location() {
        String expectedLocation = linuxLocations.get(BRAVE);
        String location = OperatingSystem.getLocation(BRAVE);
        //assertEquals(expectedLocation, location);
    }

    @Test(expected = NotSupportedException.class)//TODO: yet to complete
    public void test_linux_vivaldi_location() {
        String expectedLocation = linuxLocations.get(VIVALDI);
        String location = OperatingSystem.getLocation(VIVALDI);
        //assertEquals(expectedLocation, location);
    }

    @Test(expected = NotSupportedException.class)//TODO: yet to complete
    public void test_linux_firefox_location() {
        OperatingSystem.getLocation(FIREFOX);
    }


}
