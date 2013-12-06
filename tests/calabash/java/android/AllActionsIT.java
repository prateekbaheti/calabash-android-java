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
        application.waitForActivity("MyActivity", 5000);
    }

    @Test
    public void shouldQueryForElements() throws CalabashException {
        goToActivity(application, ACTIVITY_SIMPLE_ELEMENTS);

        UIElements elements = application.query("textview marked:'Hello world!'");

        assertEquals(1, elements.size());
        assertEquals("Hello world!", elements.first().getText());
    }

    @Test
    @Ignore("Need to speed up inspect..taking too long")
    public void shouldInspectApplicationElements() throws CalabashException {
        goToActivity(application, ACTIVITY_SIMPLE_ELEMENTS);
        String expectedElementCollection = "Element : com.android.internal.policy.impl.PhoneWindow$DecorView , Nesting : 0\n" +
                "Element : com.android.internal.widget.ActionBarOverlayLayout , Nesting : 1\n" +
                "Element : android.widget.FrameLayout , Nesting : 2\n" +
                "Element : android.widget.LinearLayout , Nesting : 3\n" +
                "Element : android.widget.TextView , Nesting : 4\n" +
                "Element : android.widget.EditText , Nesting : 4\n" +
                "Element : android.widget.Button , Nesting : 4\n" +
                "Element : android.widget.RadioButton , Nesting : 4\n" +
                "Element : android.widget.ImageButton , Nesting : 4\n" +
                "Element : android.widget.TextView , Nesting : 4\n" +
                "Element : android.widget.ImageView , Nesting : 4\n" +
                "Element : android.widget.LinearLayout , Nesting : 2\n" +
                "Element : com.android.internal.widget.ActionBarContainer , Nesting : 3\n" +
                "Element : com.android.internal.widget.ActionBarView , Nesting : 4\n" +
                "Element : android.widget.LinearLayout , Nesting : 5\n" +
                "Element : com.android.internal.widget.ActionBarView$HomeView , Nesting : 6\n" +
                "Element : android.widget.ImageView , Nesting : 7\n" +
                "Element : android.widget.LinearLayout , Nesting : 6\n" +
                "Element : android.widget.LinearLayout , Nesting : 7\n" +
                "Element : android.widget.TextView , Nesting : 8\n" +
                "Element : com.android.internal.view.menu.ActionMenuView , Nesting : 5\n" +
                "Element : com.android.internal.view.menu.ActionMenuItemView , Nesting : 6\n" +
                "Element : com.android.internal.view.menu.ActionMenuPresenter$OverflowMenuButton , Nesting : 6\n";
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
        goToActivity(application, ACTIVITY_SIMPLE_ELEMENTS);
        UIElement button = application.query("button marked:'Normal Button'").first();
        UIElement radioButton = application.query("radioButton").first();
        UIElement imageButton = application.query("imageButton").first();

        button.touch();
        UIElement textView = application.query("textView id:'textView'").first();
        assertEquals("normal button was clicked", textView.getText());

        radioButton.touch();
        textView = application.query("textView id:'textView'").first();
        assertEquals("radio button was clicked", textView.getText());

        imageButton.touch();
        textView = application.query("textView id:'textView'").first();
        assertEquals("image button was clicked", textView.getText());
    }

    @Test
    public void shouldSetText() throws CalabashException {
        goToActivity(application, ACTIVITY_SIMPLE_ELEMENTS);
        UIElement editText = application.query("editText").first();

        editText.setText("foo bar");

        UIElement textView = application.query("textView id:'textView'").first();
        assertEquals("foo bar was entered", textView.getText());
    }

    @Test
    public void shouldPerformCheckboxActions() throws CalabashException {
        goToActivity(application, ACTIVITY_NESTED_VIEWS);

        UIElement checkBox = application.query("checkBox").first();
        String isChecked = checkBox.getProperty("checked").toString();
        assertEquals(false, Boolean.parseBoolean(isChecked));

        checkBox.setChecked(true);
        isChecked = checkBox.getProperty("checked").toString();
        assertEquals(true, Boolean.parseBoolean(isChecked));

        checkBox.setChecked(false);
        isChecked = checkBox.getProperty("checked").toString();
        assertEquals(false, Boolean.parseBoolean(isChecked));
    }

    @Test
    public void shouldPerformScrollActions() throws CalabashException {
        goToActivity(application, ACTIVITY_SCROLL_LIST);

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
        goToActivity(application, ACTIVITY_NESTED_VIEWS);
        File screenshotsDir = new File(tempDir, "screenshots");
        screenshotsDir.mkdirs();

        application.takeScreenshot(screenshotsDir, "first");
        File screenshot = new File(screenshotsDir, "first_0.png");

        assertTrue(screenshot.exists());
    }

    @Test
    public void shouldSelectMenuOptions() throws CalabashException {
        goToActivity(application, ACTIVITY_SIMPLE_ELEMENTS);

        application.selectMenuItem("Third");
        UIElement textView = application.query("textView id:'textView'").first();
        assertEquals("Third menu item was selected", textView.getText());

        application.selectMenuItem("Fourth");
        textView = application.query("textView id:'textView'").first();
        assertEquals("Fourth menu item was selected", textView.getText());

    }

    @Test
    public void shouldPerformSwipeActions() throws CalabashException {
        goToActivity(application, ACTIVITY_SWIPE_PAGE);

        application.swipe(Direction.RIGHT);
        int index = Integer.parseInt((String) application.query("* id:'pager'").first().getProperty("currentItem"));
        assertEquals(1, index);

        application.swipe(Direction.LEFT);
        index = Integer.parseInt((String) application.query("* id:'pager'").first().getProperty("currentItem"));
        assertEquals(0, index);
    }

    @Test
    public void shouldPerformLongPress() throws CalabashException {
        goToActivity(application, ACTIVITY_SIMPLE_ELEMENTS);

        application.query("textView marked:'Long press text'").first().longPress();
        UIElement resultTextView = application.query("textView id:'textView'").first();
        assertEquals("long press text was long pressed", resultTextView.getText());

        application.query("imageView id:'longPressImage'").first().longPress();
        UIElement resultTextViewAfter = application.query("textView id:'textView'").first();
        assertEquals("long press image was long pressed", resultTextViewAfter.getText());
    }

    @Test
    public void shouldSetGPSCoordinates() throws CalabashException {
        goToActivity(application, ACTIVITY_CURRENT_LOCATION);

        application.setGPSCoordinates(12.928909, 77.628906);
        UIElement latitudeText = application.query("textView id:'latitude'").first();
        UIElement longitudeText = application.query("textView id:'longitude'").first();
        assertEquals(Double.parseDouble(latitudeText.getText()), 12.928909, .001);
        assertEquals(Double.parseDouble(longitudeText.getText()), 77.628906, .001);

        application.setGPSLocation("Thoughtworks inc, San Francisco");
        latitudeText = application.query("textView id:'latitude'").first();
        longitudeText = application.query("textView id:'longitude'").first();

        assertEquals(Double.parseDouble(latitudeText.getText()), 37.792626, .05);
        assertEquals(Double.parseDouble(longitudeText.getText()), -122.402698, .05);
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
