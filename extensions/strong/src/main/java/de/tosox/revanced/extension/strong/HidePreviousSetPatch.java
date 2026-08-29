package de.tosox.revanced.extension.strong;

import android.text.TextUtils;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@SuppressWarnings("unused")
public final class HidePreviousSetPatch {
	// Stable cell-type ids (gx.b), used to tell a single-column previous value's parts apart without
	// guessing a column's meaning from the formatted text.
	private static final int ID_REPS = 6;
	private static final int ID_DISTANCE = 8;

	// A previous-set value is assembled column by column. A weighted set renders as "weight × reps"
	// where the reps column is always prefixed by the REPS separator " × " (gx.REPS.g()) and weight
	// cells never use it. Single-column sets join their cells with ", "; durations always contain a
	// colon and reps are a plain integer.
	private static final String REPS_SEPARATOR = " × ";
	private static final String TOKEN_SEPARATOR = ", ";

	// Shown in place of a previous value whose every part is hidden, so the cell is not left blank.
	private static final CharSequence FULLY_HIDDEN_PLACEHOLDER = "•••";

	// Stable name of TextFieldView#getCellType, which returns the (obfuscated) cell-type holder.
	private static final String GET_CELL_TYPE = "getCellType";

	// Reflection handles resolved from the obfuscated names the patch passes in, cached by
	// "<class>#<member>". Binding happens on the UI thread, but a concurrent map keeps this safe at
	// negligible cost.
	private static final Map<String, Field> FIELD_CACHE = new ConcurrentHashMap<>();
	private static final Map<String, Method> METHOD_CACHE = new ConcurrentHashMap<>();

	/**
	 * Masks the configured parts (weight / reps / time / distance) of a previous-set value until the
	 * current set has been completed.
	 *
	 * <p>All of the holder access is done reflectively because the members it reads are obfuscated;
	 * the patch resolves their names at patch time and hands them over so this code stays free of
	 * version-specific names.
	 *
	 * @param holder      The {@code SetViewHolder2} instance being bound.
	 * @param text        The formatted previous-set value (may be a styled {@link CharSequence}).
	 * @param memberNames Comma-separated obfuscated member names resolved by the patch, in order:
	 *                    item field, completed getter, input-fields field, cell-type id field.
	 * @return The text with the hidden parts removed, or the original text when completed, when
	 * nothing needs to be hidden, or when the holder could not be read. For the weighted layout the
	 * kept part keeps its styling spans.
	 */
	public static CharSequence maskPreviousValue(Object holder, CharSequence text, String memberNames) {
		if (text == null) {
			return text;
		}

		try {
			String[] names = memberNames.split(",");
			String itemFieldName = names[0];
			String completedMethodName = names[1];
			String inputFieldsFieldName = names[2];
			String cellTypeIdFieldName = names[3];

			Object item = field(holder.getClass(), itemFieldName).get(holder);
			boolean completed = (Boolean) method(item.getClass(), completedMethodName).invoke(item);
			if (completed) {
				return text;
			}

			boolean hideWeight = StrongSettings.isEnabled(StrongSettings.Setting.HIDE_PREVIOUS_WEIGHT);
			boolean hideReps = StrongSettings.isEnabled(StrongSettings.Setting.HIDE_PREVIOUS_REPS);
			boolean hideTime = StrongSettings.isEnabled(StrongSettings.Setting.HIDE_PREVIOUS_TIME);
			boolean hideDistance = StrongSettings.isEnabled(StrongSettings.Setting.HIDE_PREVIOUS_DISTANCE);
			if (!hideWeight && !hideReps && !hideTime && !hideDistance) {
				return text;
			}

			int separator = TextUtils.indexOf(text, REPS_SEPARATOR);
			if (separator >= 0) {
				// Weighted layout: weight on the left, reps (and any RPE) on the right.
				boolean keepWeight = !hideWeight;
				boolean keepReps = !hideReps;
				if (keepWeight && keepReps) {
					return text;
				}
				if (keepWeight) {
					return text.subSequence(0, separator);
				}
				if (keepReps) {
					return text.subSequence(separator + REPS_SEPARATOR.length(), text.length());
				}
				return FULLY_HIDDEN_PLACEHOLDER;
			}

			// Single group: any combination of duration, distance and/or a bodyweight rep count.
			int[] typeIds = cellTypeIds(holder, inputFieldsFieldName, cellTypeIdFieldName);
			boolean hasReps = hasType(typeIds, ID_REPS);
			boolean hasDistance = hasType(typeIds, ID_DISTANCE);
			return filterTokens(text, hasReps, hasDistance, hideReps, hideTime, hideDistance);
		} catch (Throwable ignored) {
			// Never let a reflection failure break the previous-value rendering.
			return text;
		}
	}

	// Gathers the stable cell-type id of each of the row's columns, mirroring the order of the
	// holder's input fields. A column whose cell type is missing is reported as -1.
	private static int[] cellTypeIds(Object holder, String inputFieldsFieldName, String cellTypeIdFieldName)
		throws ReflectiveOperationException {
		Object[] inputFields = (Object[]) field(holder.getClass(), inputFieldsFieldName).get(holder);
		int[] typeIds = new int[inputFields.length];
		for (int i = 0; i < inputFields.length; i++) {
			Object cellType = method(inputFields[i].getClass(), GET_CELL_TYPE).invoke(inputFields[i]);
			typeIds[i] = cellType == null
				? -1
				: field(cellType.getClass(), cellTypeIdFieldName).getInt(cellType);
		}
		return typeIds;
	}

	private static CharSequence filterTokens(
		CharSequence text,
		boolean hasReps,
		boolean hasDistance,
		boolean hideReps,
		boolean hideTime,
		boolean hideDistance
	) {
		StringBuilder result = new StringBuilder();
		for (String token : text.toString().split(TOKEN_SEPARATOR)) {
			boolean hidden;
			if (token.indexOf(':') >= 0) {
				hidden = hideTime;                          // duration, e.g. "0:30"
			} else if (hasReps && isInteger(token)) {
				hidden = hideReps;                          // bodyweight rep count, e.g. "12"
			} else if (hasDistance) {
				hidden = hideDistance;                      // distance / unit value, e.g. "5 km"
			} else {
				hidden = false;                             // unknown column: leave it visible
			}
			if (hidden) {
				continue;
			}
			if (result.length() > 0) {
				result.append(TOKEN_SEPARATOR);
			}
			result.append(token);
		}
		return result.length() == 0 ? FULLY_HIDDEN_PLACEHOLDER : result.toString();
	}

	private static boolean hasType(int[] typeIds, int id) {
		for (int typeId : typeIds) {
			if (typeId == id) {
				return true;
			}
		}
		return false;
	}

	private static boolean isInteger(String value) {
		String trimmed = value.trim();
		if (trimmed.isEmpty()) {
			return false;
		}
		for (int i = 0; i < trimmed.length(); i++) {
			if (!Character.isDigit(trimmed.charAt(i))) {
				return false;
			}
		}
		return true;
	}

	private static Field field(Class<?> owner, String name) throws NoSuchFieldException {
		String key = owner.getName() + '#' + name;
		Field field = FIELD_CACHE.get(key);
		if (field == null) {
			field = owner.getDeclaredField(name);
			field.setAccessible(true);
			FIELD_CACHE.put(key, field);
		}
		return field;
	}

	private static Method method(Class<?> owner, String name) throws NoSuchMethodException {
		String key = owner.getName() + '#' + name;
		Method method = METHOD_CACHE.get(key);
		if (method == null) {
			method = owner.getDeclaredMethod(name);
			method.setAccessible(true);
			METHOD_CACHE.put(key, method);
		}
		return method;
	}
}
