package de.tosox.revanced.extension.strong;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Persistent configuration for the Strong patches, backed by a private {@link SharedPreferences}
 * file. {@link #init(Context)} must be called once (from the application's onCreate) so the stored
 * values are available everywhere, even before the settings screen has been opened.
 */
@SuppressWarnings("unused")
public final class StrongSettings {
	private static final String PREFS_NAME = "revanced_strong";

	/** A single boolean toggle: its stored key and the value used before anything is saved. */
	public enum Setting {
		HIDE_PREVIOUS_WEIGHT("hide_previous_weight", false),
		HIDE_PREVIOUS_REPS("hide_previous_reps", true),
		HIDE_PREVIOUS_TIME("hide_previous_time", false),
		HIDE_PREVIOUS_DISTANCE("hide_previous_distance", false);

		private final String key;
		private final boolean defaultValue;

		Setting(String key, boolean defaultValue) {
			this.key = key;
			this.defaultValue = defaultValue;
		}
	}

	private static SharedPreferences preferences;

	public static void init(Context context) {
		if (context == null || preferences != null) {
			return;
		}
		preferences = context.getApplicationContext()
			.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
	}

	/** Falls back to the setting's default when preferences were never initialised. */
	public static boolean isEnabled(Setting setting) {
		return preferences == null
			? setting.defaultValue
			: preferences.getBoolean(setting.key, setting.defaultValue);
	}

	public static void setEnabled(Setting setting, boolean enabled) {
		if (preferences != null) {
			preferences.edit().putBoolean(setting.key, enabled).apply();
		}
	}
}
