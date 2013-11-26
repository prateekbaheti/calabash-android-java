package calabash.java.android;

import org.apache.commons.io.FileUtils;
import org.junit.*;
import org.junit.rules.ExpectedException;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;

import static calabash.java.android.TestUtils.*;
import static calabash.java.android.Utils.runCommand;
import static org.junit.Assert.*;

//Just run all the test. Can't help with emulator state dependency.
public class AndroidRunnerIT {

    @Rule
    public ExpectedException expectedException = ExpectedException.none();
    private File tempDir;
    private File tempAndroidPath;
    private String packageName;

    @Before
    public void setUp() throws IOException {
        packageName = "com.example.AndroidTestApplication";
        tempDir = createTempDir("TestAndroidApps");
        tempAndroidPath = createTempDirWithProj("AndroidTestApplication.apk", tempDir);
    }

    private File createTempDirWithProj(String androidApp, File dir) throws IOException {
        File androidAppPath = new File("tests/resources/" + androidApp);
        File tempAndroidPath = new File(dir, androidApp);
        FileUtils.copyFile(androidAppPath, tempAndroidPath);
        return tempAndroidPath;
    }

    @After
    public void tearDown() throws IOException {
        FileUtils.deleteDirectory(tempDir);
    }

    @Test
    public void shouldCreateTestServerApk() throws CalabashException, IOException {
        AndroidRunner androidRunner = new AndroidRunner(tempAndroidPath.getAbsolutePath());
        androidRunner.setup();
        File testServersDir = new File(tempDir, "test_servers");

        assertTrue(testServersDir.exists());
        File[] testServerApk = testServersDir.listFiles(new FileFilter() {
            public boolean accept(File pathname) {
                return pathname.getName().endsWith(".apk");
            }
        });

        assertEquals(1, testServerApk.length);
    }

    @Test
    public void shouldThrowExceptionIfSerialIsGivenWhenNotStarted() throws CalabashException {
        String serial = "emulator-x";
        expectedException.expect(CalabashException.class);
        expectedException.expectMessage(String.format("%s is not running. Cannot install app", serial));

        AndroidConfiguration configuration = new AndroidConfiguration();
        configuration.setSerial(serial);
        AndroidRunner androidRunner = new AndroidRunner(tempAndroidPath.getAbsolutePath(), configuration);
        androidRunner.setup();
        androidRunner.start();

    }

    @Test
    public void shouldInstallAppOnDeviceWithName() throws CalabashException {
        AndroidConfiguration configuration = new AndroidConfiguration();
        configuration.setDeviceName("device");
        configuration.setShouldReinstallApp(true);
        AndroidRunner androidRunner = new AndroidRunner(tempAndroidPath.getAbsolutePath(), configuration);

        androidRunner.setup();
        AndroidApplication application = androidRunner.start();

        assertTrue(isAppInstalled(packageName, application.getInstalledOn()));
        assertTrue(isMainActivity(application, "MyActivity"));
    }

    @Test
    public void shouldInstallApplicationIfSerialIsProvided() throws CalabashException {
        //note: emulator should be launched with serial 'emulator-5554
        String serial = "emulator-5554";
        uninstall(packageName);
        AndroidConfiguration configuration = new AndroidConfiguration();
        configuration.setSerial(serial);
        AndroidRunner androidRunner = new AndroidRunner(tempAndroidPath.getAbsolutePath(), configuration);

        androidRunner.setup();
        AndroidApplication application = androidRunner.start();

        assertTrue(isAppInstalled(packageName, serial));
        assertTrue(isMainActivity(application, "MyActivity"));
    }

    @Test
    public void shouldInstallApplicationAlreadyRunningDevice() throws CalabashException {
        //note: emulator with name 'device' should be launched with serial 'emulator-5554'

        String device = "device";
        String serial = "emulator-5554";
        uninstall(packageName);
        AndroidConfiguration configuration = new AndroidConfiguration();
        configuration.setDeviceName(device);
        AndroidRunner androidRunner = new AndroidRunner(tempAndroidPath.getAbsolutePath(), configuration);

        androidRunner.setup();
        AndroidApplication application = androidRunner.start();

        assertTrue(isAppInstalled(packageName, serial));
        assertTrue(isMainActivity(application, "MyActivity"));
    }

    public static AndroidApplication installAppOnEmulator(String serial, String packageName, File androidPath) throws CalabashException {
        return TestUtils.installAppOnEmulator(serial, packageName, androidPath);
    }
}
