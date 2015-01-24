package com.njackson.test.activities;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.ListPreference;
import android.preference.Preference;
import android.test.ActivityUnitTestCase;
import android.test.suitebuilder.annotation.SmallTest;

import com.njackson.R;
import com.njackson.activities.SettingsActivity;
import com.njackson.application.modules.AndroidModule;
import com.njackson.test.application.TestApplication;
import com.njackson.utils.watchface.IInstallWatchFace;
import com.njackson.utils.messages.IMessageMaker;

import javax.inject.Inject;
import javax.inject.Singleton;

import dagger.Module;
import dagger.ObjectGraph;
import dagger.Provides;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Created by server on 28/06/2014.
 */
public class SettingsActivityTest extends ActivityUnitTestCase<SettingsActivity> {
    private Context _targetContext;
    private SettingsActivity _activity;
    private Preference _installPreference;
    private Preference _unitsOfMeasure;
    private Preference _refreshPref;
    private Preference _loginPref;
    private Preference _loginMntPref;
    private ListPreference _oruxMaps;
    private ListPreference _canvas;

    public SettingsActivityTest() {
        super(SettingsActivity.class);
    }

    @Inject IInstallWatchFace _watchFaceMock;
    @Inject SharedPreferences _preferences;

    @Module(
            includes = AndroidModule.class,
            injects = SettingsActivityTest.class,
            overrides = true,
            complete = false
    )
    static class TestModule {
        @Provides @Singleton
        IInstallWatchFace providesWatchFaceInstall() {
            return mock(IInstallWatchFace.class);
        }

        @Provides
        @Singleton
        SharedPreferences provideSharedPreferences() {
            return mock(SharedPreferences.class);
        }
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();

        TestApplication app = new TestApplication();
        app.setObjectGraph(ObjectGraph.create(TestModule.class));
        app.inject(this);
        setApplication(app);

        setupMocks();

        //setActivityInitialTouchMode(false);
        _targetContext = getInstrumentation().getTargetContext();
        Intent intent = new Intent(_targetContext, SettingsActivity.class);
        startActivity(intent, null, null);

        _activity = getActivity();
        assertNotNull(_activity);

        _installPreference = _activity.findPreference("INSTALL_WATCHFACE");
        assertNotNull(_installPreference);

        _unitsOfMeasure = _activity.findPreference("UNITS_OF_MEASURE");
        assertNotNull(_unitsOfMeasure);

        _refreshPref = _activity.findPreference("REFRESH_INTERVAL");
        assertNotNull(_refreshPref);

        _loginPref = _activity.findPreference("LIVE_TRACKING_LOGIN");
        assertNotNull(_loginPref);

        _loginMntPref = _activity.findPreference("LIVE_TRACKING_MMT_LOGIN");
        assertNotNull(_loginMntPref);

        _oruxMaps = (ListPreference) _activity.findPreference("ORUXMAPS_AUTO");
        assertNotNull(_oruxMaps);

        _canvas = (ListPreference) _activity.findPreference("CANVAS_MODE");
        assertNotNull(_canvas);
    }

    private void setupMocks() {
        when(_preferences.getString("UNITS_OF_MEASURE","0")).thenReturn("0");
        when(_preferences.getString("REFRESH_INTERVAL","500")).thenReturn("20");
        when(_preferences.getString("LIVE_TRACKING_LOGIN","")).thenReturn("abcdefg");
        when(_preferences.getString("LIVE_TRACKING_MMT_LOGIN","")).thenReturn("abcdefghi");
        when(_preferences.getString("ORUXMAPS_AUTO","")).thenReturn("continue");
        when(_preferences.getString("CANVAS_MODE","")).thenReturn("canvas_and_pbw");
        when(_preferences.getString("hrm_name", "")).thenReturn("test");
    }

    @SmallTest
    public void testInstallListener() {
        Preference.OnPreferenceClickListener onPreferenceClickListener = _installPreference.getOnPreferenceClickListener();
        assertNotNull(onPreferenceClickListener);
    }

    @SmallTest
    public void testOnPauseRemovesListener() {
        getInstrumentation().callActivityOnPause(_activity);

        verify(_preferences,times(1)).unregisterOnSharedPreferenceChangeListener(any(SharedPreferences.OnSharedPreferenceChangeListener.class));
    }

    @SmallTest
    public void testOnResumeSetsListener() {
        getInstrumentation().callActivityOnResume(_activity);

        verify(_preferences,times(1)).registerOnSharedPreferenceChangeListener(any(SettingsActivity.class));
    }

    @SmallTest
    public void testOnResumeSetsUnits() {
        getInstrumentation().callActivityOnResume(_activity);

        assertEquals(_unitsOfMeasure.getSummary(),_activity.getString(R.string.PREF_UNITS_UNIT_IMPERIAL));
    }

    @SmallTest
    public void testOnResumeSetsRefresh() {
        getInstrumentation().callActivityOnResume(_activity);

        assertEquals("20 ms", _refreshPref.getSummary());
    }

    @SmallTest
    public void testOnResumeSetsJayPSLogin() {
        getInstrumentation().callActivityOnResume(_activity);

        assertEquals("abcdefg", _loginPref.getSummary());
    }

    @SmallTest
    public void testOnResumeSetsMMTLogin() {
        getInstrumentation().callActivityOnResume(_activity);

        assertEquals("abcdefghi", _loginMntPref.getSummary());
    }

    @SmallTest
    public void testOnResumeSetsOruxMaps() {
        getInstrumentation().callActivityOnResume(_activity);

        assertEquals("Continue record", _oruxMaps.getSummary());
    }

    @SmallTest
    public void testOnResumeSetsCanvas() {
        getInstrumentation().callActivityOnResume(_activity);

        assertEquals("Canvas only", _canvas.getSummary());
    }

    @SmallTest
    public void testOnPreferenceClickExecutesInstallWatchFace(){
        Preference.OnPreferenceClickListener onPreferenceClickListener = _installPreference.getOnPreferenceClickListener();
        boolean b = onPreferenceClickListener.onPreferenceClick(new Preference(_targetContext));

        verify(_watchFaceMock, times(1)).execute(any(Context.class), any(IMessageMaker.class));
    }

    @SmallTest
    public void testOnPreferenceChangedSetsUnitsOfMeasureToImperial(){
        when(_preferences.getString("UNITS_OF_MEASURE","0")).thenReturn("0");
        _activity.onSharedPreferenceChanged(_preferences, "UNITS_OF_MEASURE");

        assertEquals(_unitsOfMeasure.getSummary(),_activity.getString(R.string.PREF_UNITS_UNIT_IMPERIAL));
    }

    @SmallTest
    public void testOnPreferenceChangedSetsUnitsOfMeasureToMetric(){
        when(_preferences.getString("UNITS_OF_MEASURE","0")).thenReturn("1");
        _activity.onSharedPreferenceChanged(_preferences, "UNITS_OF_MEASURE");

        assertEquals(_unitsOfMeasure.getSummary(),_activity.getString(R.string.PREF_UNITS_UNIT_METRIC));
    }

    @SmallTest
    public void testOnPreferenceChangedWithInvalidIntervalSetsRefreshIntervalToDefault(){
        reset(_preferences);
        when(_preferences.getString("REFRESH_INTERVAL","0")).thenReturn("abcdse");
        _activity.onSharedPreferenceChanged(_preferences, "REFRESH_INTERVAL");

        assertEquals("500 ms", _refreshPref.getSummary());
    }

    @SmallTest
    public void testOnPreferenceChangedAndIntervalLessThan1000SetsRefreshIntervalToSeconds(){
        when(_preferences.getString("REFRESH_INTERVAL","500")).thenReturn("20");
        _activity.onSharedPreferenceChanged(_preferences, "REFRESH_INTERVAL");

        assertEquals("20 ms", _refreshPref.getSummary());
    }

    @SmallTest
    public void testOnPreferenceChangedAndIntervalGreaterThan1000SetsRefreshIntervalToMilliseconds(){
        when(_preferences.getString("REFRESH_INTERVAL","500")).thenReturn("2000");
        _activity.onSharedPreferenceChanged(_preferences, "REFRESH_INTERVAL");

        assertEquals("2 s", _refreshPref.getSummary());
    }

    @SmallTest
    public void testOnPreferenceChangedSetsJayPSLogin(){
        when(_preferences.getString("LIVE_TRACKING_LOGIN","")).thenReturn("abcdefg");
        _activity.onSharedPreferenceChanged(_preferences, "LIVE_TRACKING_LOGIN");

        assertEquals("abcdefg", _loginPref.getSummary());
    }

    @SmallTest
    public void testOnPreferenceChangedSetsMmtLogin(){
        when(_preferences.getString("LIVE_TRACKING_MMT_LOGIN","")).thenReturn("abcdefghi");
        _activity.onSharedPreferenceChanged(_preferences, "LIVE_TRACKING_MMT_LOGIN");

        assertEquals("abcdefghi", _loginMntPref.getSummary());
    }

    @SmallTest
    public void testOnPreferenceChangedSetsOruxMapsMode(){
        when(_preferences.getString("ORUXMAPS_AUTO","")).thenReturn("continue");
        _oruxMaps.setValue("continue");
        _activity.onSharedPreferenceChanged(_preferences, "ORUXMAPS_AUTO");

        assertEquals("Continue record", _oruxMaps.getSummary());
    }

    @SmallTest
    public void testOnPreferenceChangedSetsCanvasMode(){
        when(_preferences.getString("CANVAS_MODE","")).thenReturn("canvas_only");
        _canvas.setValue("canvas_only");
        _activity.onSharedPreferenceChanged(_preferences, "CANVAS_MODE");

        assertEquals("Canvas only", _canvas.getSummary());
    }
}
