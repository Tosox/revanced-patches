package de.tosox.revanced.extension.strong;

import android.app.Activity;
import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.Typeface;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Switch;
import android.widget.TextView;

/**
 * Builds a "ReVanced" section on Strong's (static) settings screen, styled to match the vanilla
 * rows (section header + a switch row with a secondary-coloured hint). Everything is created
 * programmatically and themed from the app's own theme attributes so no layout resources have to be
 * added or patched.
 */
@SuppressWarnings({"unused", "deprecation"})
public final class StrongSettingsScreen {
	private static final String SECTION_TITLE = "ReVanced";

	// The toggle rows shown under the section header, in display order. Each maps a stored setting to
	// the title and hint shown for it.
	private enum Row {
		WEIGHT(StrongSettings.Setting.HIDE_PREVIOUS_WEIGHT, "Hide previous weight",
			"Hides the weight of the previous set until you complete the current set"),
		REPS(StrongSettings.Setting.HIDE_PREVIOUS_REPS, "Hide previous reps",
			"Hides the reps of the previous set until you complete the current set"),
		TIME(StrongSettings.Setting.HIDE_PREVIOUS_TIME, "Hide previous time",
			"Hides the duration of the previous set until you complete the current set"),
		DISTANCE(StrongSettings.Setting.HIDE_PREVIOUS_DISTANCE, "Hide previous distance",
			"Hides the distance of the previous set until you complete the current set");

		private final StrongSettings.Setting setting;
		private final String title;
		private final String hint;

		Row(StrongSettings.Setting setting, String title, String hint) {
			this.setting = setting;
			this.title = title;
			this.hint = hint;
		}
	}

	// The "Other services" section header directly follows the "Advanced" section, so inserting
	// in front of it places our section right after "Advanced".
	private static final String ANCHOR_VIEW_NAME = "settings_other_services_title";

	public static void addRevancedSettings(Activity activity) {
		try {
			LinearLayout container = findSettingsContainer(activity);
			if (container == null) {
				return;
			}

			StrongSettings.init(activity);

			Context context = container.getContext();

			LinearLayout section = new LinearLayout(context);
			section.setOrientation(LinearLayout.VERTICAL);
			section.setLayoutParams(new LinearLayout.LayoutParams(
				ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));

			section.addView(createSectionHeader(context));
			for (Row row : Row.values()) {
				section.addView(createSwitchRow(
					context,
					row.title,
					row.hint,
					StrongSettings.isEnabled(row.setting),
					enabled -> StrongSettings.setEnabled(row.setting, enabled)));
			}
			section.addView(createDivider(context));

			container.addView(section, insertionIndex(activity, container));
		} catch (Exception ignored) {
			// Never let an extension failure crash the settings screen.
		}
	}

	private interface OnToggle {
		void onToggle(boolean enabled);
	}

	// region View builders

	private static TextView createSectionHeader(Context context) {
		TextView title = new TextView(context);
		applyTextAppearance(title, appAttr(context, "textAppearanceBody1"));
		title.setText(SECTION_TITLE);
		title.setTypeface(title.getTypeface(), Typeface.BOLD);
		int horizontal = horizontalMargin(context);
		// Matches the vanilla headers: horizontal padding only, vertical spacing comes from the
		// preceding divider.
		title.setPadding(horizontal, 0, horizontal, 0);
		return title;
	}

	private static View createSwitchRow(
		Context context,
		String title,
		String hint,
		boolean checked,
		OnToggle onToggle
	) {
		int horizontal = horizontalMargin(context);
		int vertical = dp(context, 8);

		RelativeLayout row = new RelativeLayout(context);
		LinearLayout.LayoutParams rowParams = new LinearLayout.LayoutParams(
			ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
		rowParams.topMargin = vertical;
		row.setLayoutParams(rowParams);
		row.setMinimumHeight(dp(context, 56));
		row.setPadding(horizontal, vertical, horizontal, vertical);
		row.setBackgroundResource(frameworkAttr(context, android.R.attr.selectableItemBackground));

		final Switch toggle = new Switch(context);
		toggle.setId(View.generateViewId());
		toggle.setChecked(checked);
		toggle.setOnCheckedChangeListener((button, isChecked) -> onToggle.onToggle(isChecked));
		RelativeLayout.LayoutParams toggleParams = new RelativeLayout.LayoutParams(
			ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
		toggleParams.addRule(RelativeLayout.ALIGN_PARENT_END);
		toggleParams.addRule(RelativeLayout.CENTER_VERTICAL);
		row.addView(toggle, toggleParams);

		LinearLayout textBlock = new LinearLayout(context);
		textBlock.setOrientation(LinearLayout.VERTICAL);
		RelativeLayout.LayoutParams textParams = new RelativeLayout.LayoutParams(
			ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
		textParams.addRule(RelativeLayout.ALIGN_PARENT_START);
		textParams.addRule(RelativeLayout.CENTER_VERTICAL);
		textParams.addRule(RelativeLayout.START_OF, toggle.getId());
		textParams.setMarginEnd(horizontal);
		row.addView(textBlock, textParams);

		TextView titleView = new TextView(context);
		applyTextAppearance(titleView, appAttr(context, "textAppearanceBody1"));
		titleView.setText(title);
		textBlock.addView(titleView);

		TextView hintView = new TextView(context);
		applyTextAppearance(hintView, appAttr(context, "textAppearanceBody2"));
		hintView.setText(hint);
		ColorStateList secondary = themeColor(context, android.R.attr.textColorSecondary);
		if (secondary != null) {
			hintView.setTextColor(secondary);
		}
		textBlock.addView(hintView);

		// Tapping anywhere on the row flips the switch, like the vanilla switch rows.
		row.setOnClickListener(v -> toggle.toggle());
		return row;
	}

	private static View createDivider(Context context) {
		View divider = new View(context);
		LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
			ViewGroup.LayoutParams.MATCH_PARENT, dp(context, 1));
		params.topMargin = dp(context, 8);
		params.bottomMargin = dp(context, 16);
		divider.setLayoutParams(params);
		applyDividerBackground(context, divider);
		return divider;
	}

	// endregion

	// region Container / placement

	private static LinearLayout findSettingsContainer(Activity activity) {
		int id = activity.getResources()
			.getIdentifier("settings_container", "id", activity.getPackageName());
		if (id == 0) {
			return null;
		}
		View view = activity.findViewById(id);
		return (view instanceof LinearLayout) ? (LinearLayout) view : null;
	}

	private static int insertionIndex(Activity activity, LinearLayout container) {
		int anchorId = activity.getResources()
			.getIdentifier(ANCHOR_VIEW_NAME, "id", activity.getPackageName());
		if (anchorId != 0) {
			int index = container.indexOfChild(activity.findViewById(anchorId));
			if (index >= 0) {
				return index;
			}
		}
		return container.getChildCount();
	}

	// endregion

	// region Theming helpers

	private static int horizontalMargin(Context context) {
		int id = context.getResources()
			.getIdentifier("activity_horizontal_margin", "dimen", context.getPackageName());
		if (id != 0) {
			return context.getResources().getDimensionPixelSize(id);
		}
		return dp(context, 16);
	}

	/** Resolves an application theme attribute (e.g. textAppearanceBody1) to a resource id. */
	private static int appAttr(Context context, String attrName) {
		int attrId = context.getResources()
			.getIdentifier(attrName, "attr", context.getPackageName());
		return (attrId == 0) ? 0 : frameworkAttr(context, attrId);
	}

	/** Resolves any theme attribute id to the resource id it points at. */
	private static int frameworkAttr(Context context, int attrId) {
		TypedValue value = new TypedValue();
		if (context.getTheme().resolveAttribute(attrId, value, true)) {
			return value.resourceId;
		}
		return 0;
	}

	private static void applyTextAppearance(TextView view, int styleResId) {
		if (styleResId != 0) {
			view.setTextAppearance(view.getContext(), styleResId);
		}
	}

	private static ColorStateList themeColor(Context context, int attrId) {
		TypedArray array = context.obtainStyledAttributes(new int[]{attrId});
		try {
			return array.getColorStateList(0);
		} finally {
			array.recycle();
		}
	}

	private static void applyDividerBackground(Context context, View view) {
		int attrId = context.getResources()
			.getIdentifier("divider", "attr", context.getPackageName());
		if (attrId != 0) {
			TypedValue value = new TypedValue();
			if (context.getTheme().resolveAttribute(attrId, value, true)) {
				if (value.type >= TypedValue.TYPE_FIRST_COLOR_INT
					&& value.type <= TypedValue.TYPE_LAST_COLOR_INT) {
					view.setBackgroundColor(value.data);
					return;
				}
				if (value.resourceId != 0) {
					view.setBackgroundResource(value.resourceId);
					return;
				}
			}
		}
		view.setBackgroundColor(Color.argb(0x33, 0x88, 0x88, 0x88));
	}

	private static int dp(Context context, int value) {
		return (int) TypedValue.applyDimension(
			TypedValue.COMPLEX_UNIT_DIP, value, context.getResources().getDisplayMetrics());
	}

	// endregion
}
