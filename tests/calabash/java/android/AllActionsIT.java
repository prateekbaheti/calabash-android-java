package calabash.java.android;


import org.junit.*;

import java.io.File;
import java.util.Map;

import static calabash.java.android.TestUtils.*;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class AllActionsIT {


    private static String packageName;
    private static File tempDir;
    private static File apkPath;
    private static AndroidApplication application;

    @BeforeClass
    public static void installApp() throws Exception {
        packageName = "com.example.AndroidTestApplication";
        tempDir = createTempDir("TestAndroidApps");
        apkPath = createTempDirWithProj("AndroidTestApplication.apk", tempDir);
        application = TestUtils.installAppOnEmulator("emulator-5554", packageName, apkPath);
    }

    @AfterClass
    public static void tearDown() {
        tempDir.delete();
    }

    @After
    public void goToMainActivity() throws CalabashException {
        application.goBack();
        assertEquals("MyActivity", application.getCurrentActivity());
    }

    @Test
    public void shouldQueryForElements() throws CalabashException {
        goToActivity(application, "Simple Elements");
        application.waitForActivity("SimpleElementsActivity", 5000);

        UIElements elements = application.query("textview marked:'Hello world!'");

        assertEquals(1, elements.size());
        assertEquals("Hello world!", elements.first().getText());
    }

    @Test
    @Ignore("Need to speed up inspect..taking too long")
    public void shouldInspectApplicationElements() throws CalabashException {
        goToActivity(application, "Nested Views");
        application.waitForActivity("NestedViewsActivity", 5000);
        String expectedElementCollection = "";
        final StringBuilder actualElementCollection = new StringBuilder();

        application.inspect(new InspectCallback() {
            public void onEachElement(UIElement element, int nestingLevel) {
                actualElementCollection.append(String.format("Element : %s , Nesting : %d\n", element.getElementClass(), nestingLevel));
            }
        });

        assertEquals(expectedElementCollection, actualElementCollection.toString());
    }

    @Test
    public void shouldTouchElements() throws CalabashException {
        goToActivity(application, "Simple Elements");
        application.waitForActivity("SimpleViewActivity ", 5000);
        UIElement button = application.query("button").first();
        UIElement radioButton = application.query("radioButton").first();
        UIElement imageButton = application.query("imageButton").first();

        button.touch();
        UIElement textView = application.query("textView index:1").first();
        assertEquals("normal button was clicked", textView.getText());

        radioButton.touch();
        textView = application.query("textView index:1").first();
        assertEquals("radio button was clicked", textView.getText());

        imageButton.touch();
        textView = application.query("textView index:1").first();
        assertEquals("image button was clicked", textView.getText());
    }

    @Test
    public void shouldSetText() throws CalabashException {
        goToActivity(application, "Simple Elements");
        application.waitForActivity("SimpleElementsActivity", 5000);
        UIElement editText = application.query("editText").first();

        editText.setText("foo bar");

        UIElement textView = application.query("textView index:1").first();
        assertEquals("foo bar was entered", textView.getText());
    }

    @Test
    public void shouldPerformCheckboxActions() throws CalabashException {
        goToActivity(application, "Nested Views");
        application.waitForActivity("NestedViewsActivity", 5000);

        UIElement checkBox = application.query("checkBox").first();
        boolean isChecked = checkBox.isChecked();
        assertEquals(isChecked, false);

        checkBox.setChecked(true);
        isChecked = checkBox.isChecked();
        assertEquals(isChecked, true);

        checkBox.setChecked(false);
        isChecked = checkBox.isChecked();
        assertEquals(isChecked, false);
    }

    @Test
    public void shouldPerformScrollActions() throws CalabashException {
        goToActivity(application, "Scroll List");
        application.waitForActivity("ScrollListActivity", 5000);

        String queryForSecondPageElement = "textView marked:'The House of Mirth'";
        assertEquals(0, application.query(queryForSecondPageElement).size());

        application.scrollDown();
        assertEquals(1, application.query(queryForSecondPageElement).size());

        String queryForFirstPageElement = "textView marked:'A Time to Kill'";
        assertEquals(0, application.query(queryForFirstPageElement).size());

        application.scrollUp();
        assertEquals(1, application.query(queryForFirstPageElement).size());

    }

    @Test
    public void shouldTakeScreenshot() throws CalabashException {
        File screenshotsDir = new File(tempDir, "screenshots");
        screenshotsDir.mkdirs();

        application.takeScreenshot(screenshotsDir, "first");
        File screenshot = new File(screenshotsDir, "first_0.png");

        assertTrue(screenshot.exists());
    }

    @Test
    public void shouldSelectMenuOptions() throws CalabashException {
        goToActivity(application, "Simple Elements");

        application.selectMenuItem("Third");
        UIElement textView = application.query("textView index:1").first();
        assertEquals("Third menu item was selected", textView.getText());


        application.selectMenuItem("Fourth");
        textView = application.query("textView index:1").first();
        assertEquals("Fourth menu item was selected", textView.getText());

    }

    @Test
    public void shouldGetSharedPreferences() throws CalabashException {
        Map<String, String> preferences = application.getSharedPreferences("my_preferences");

        assertEquals("true", preferences.get("a boolean"));
        assertEquals("my string", preferences.get("a string"));
        assertEquals("1.5", preferences.get("a float"));
        assertEquals("123", preferences.get("an int"));

    }
}
