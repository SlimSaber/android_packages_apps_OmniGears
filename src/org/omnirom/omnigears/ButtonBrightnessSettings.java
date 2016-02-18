/*
 *  Copyright (C) 2014 The OmniROM Project
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 */

package org.omnirom.omnigears.brightness;

import com.android.settings.SettingsPreferenceFragment;

import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Handler;
import android.os.PowerManager;
import android.os.IPowerManager;
import android.os.ServiceManager;
import android.preference.Preference;
import android.preference.PreferenceGroup;
import android.preference.PreferenceScreen;
import android.preference.ListPreference;
import android.preference.CheckBoxPreference;
import android.preference.PreferenceCategory;
import android.provider.Settings;
import android.provider.SearchIndexableResource;
import android.view.View;
import android.util.Log;
import android.app.AlertDialog;
import android.text.TextWatcher;
import android.text.Editable;
import android.widget.Button;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.TextView;

import org.omnirom.omnigears.R;

import com.android.internal.logging.MetricsLogger;
import com.android.settings.preference.SeekBarPreference;
import com.android.settings.search.BaseSearchIndexProvider;
import com.android.settings.search.Indexable;

import java.util.List;
import java.util.ArrayList;
import java.util.Iterator;

public class ButtonBrightnessSettings extends SettingsPreferenceFragment implements
        Preference.OnPreferenceChangeListener, Indexable {
    private static final String TAG = "ButtonBrightnessSettings";

    private static final String KEY_BUTTON_NO_BRIGHTNESS = "button_no_brightness";
    private static final String KEY_BUTTON_LINK_BRIGHTNESS = "button_link_brightness";
    private static final String KEY_BUTTON_MANUAL_BRIGHTNESS_NEW = "button_manual_brightness_new";
    private static final String KEY_BUTTON_TIMEOUT = "button_timeout";

    private CheckBoxPreference mNoButtonBrightness;
    private CheckBoxPreference mLinkButtonBrightness;
    private IPowerManager mPowerService;
    private SeekBarPreference mButtonTimoutBar;
    private SeekBarPreference mManualButtonBrightnessNew;

    @Override
    protected int getMetricsCategory() {
        return MetricsLogger.OMNI_SETTINGS;
    }

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.button_brightness_settings);

        PreferenceScreen prefSet = getPreferenceScreen();
        ContentResolver resolver = getContentResolver();

        mNoButtonBrightness = (CheckBoxPreference) findPreference(KEY_BUTTON_NO_BRIGHTNESS);
        mNoButtonBrightness.setChecked(Settings.System.getInt(resolver,
                Settings.System.CUSTOM_BUTTON_DISABLE_BRIGHTNESS, 0) != 0);

        mLinkButtonBrightness = (CheckBoxPreference) findPreference(KEY_BUTTON_LINK_BRIGHTNESS);
        mLinkButtonBrightness.setChecked(Settings.System.getInt(resolver,
                Settings.System.CUSTOM_BUTTON_USE_SCREEN_BRIGHTNESS, 0) != 0);

        mManualButtonBrightnessNew = (SeekBarPreference) findPreference(KEY_BUTTON_MANUAL_BRIGHTNESS_NEW);
        final int customButtonBrightness = getResources().getInteger(
                com.android.internal.R.integer.config_button_brightness_default);
        final int currentBrightness = Settings.System.getInt(resolver,
                Settings.System.CUSTOM_BUTTON_BRIGHTNESS, customButtonBrightness);
        mManualButtonBrightnessNew.setValue(currentBrightness);
        mManualButtonBrightnessNew.setOnPreferenceChangeListener(this);

        mButtonTimoutBar = (SeekBarPreference) findPreference(KEY_BUTTON_TIMEOUT);
        int currentTimeout = Settings.System.getInt(resolver,
                        Settings.System.BUTTON_BACKLIGHT_TIMEOUT, 0);
        mButtonTimoutBar.setValue(currentTimeout);
        mButtonTimoutBar.setOnPreferenceChangeListener(this);

        mPowerService = IPowerManager.Stub.asInterface(ServiceManager.getService("power"));

        updateEnablement();
    }

    private void updateEnablement() {
        if (mNoButtonBrightness.isChecked()){
            mLinkButtonBrightness.setEnabled(false);
            mButtonTimoutBar.setEnabled(false);
            mManualButtonBrightnessNew.setEnabled(false);
        } else if (mLinkButtonBrightness.isChecked()){
            mNoButtonBrightness.setEnabled(false);
            mManualButtonBrightnessNew.setEnabled(false);
        } else {
            mNoButtonBrightness.setEnabled(true);
            mLinkButtonBrightness.setEnabled(true);
            mButtonTimoutBar.setEnabled(true);
            mManualButtonBrightnessNew.setEnabled(true);
        }
    }

    @Override
    public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen, Preference preference) {
        if (preference == mNoButtonBrightness) {
            boolean checked = ((CheckBoxPreference)preference).isChecked();
            Settings.System.putInt(getContentResolver(),
                    Settings.System.CUSTOM_BUTTON_DISABLE_BRIGHTNESS, checked ? 1:0);
            updateEnablement();
            return true;
        } else if (preference == mLinkButtonBrightness) {
            boolean checked = ((CheckBoxPreference)preference).isChecked();
            Settings.System.putInt(getContentResolver(),
                    Settings.System.CUSTOM_BUTTON_USE_SCREEN_BRIGHTNESS, checked ? 1:0);
            updateEnablement();
            return true;
        }
        return super.onPreferenceTreeClick(preferenceScreen, preference);
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object objValue) {
        final String key = preference.getKey();

        if (preference == mButtonTimoutBar) {
            int buttonTimeout = (Integer) objValue;
            Settings.System.putInt(getContentResolver(),
                    Settings.System.BUTTON_BACKLIGHT_TIMEOUT, buttonTimeout);
        } else if (preference == mManualButtonBrightnessNew) {
            int buttonBrightness = (Integer) objValue;
            Settings.System.putInt(getContentResolver(),
                    Settings.System.CUSTOM_BUTTON_BRIGHTNESS, buttonBrightness);
        } else {
            return false;
        }
        return true;
    }
}

