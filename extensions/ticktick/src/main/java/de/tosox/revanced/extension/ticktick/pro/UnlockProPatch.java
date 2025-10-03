package de.tosox.revanced.extension.ticktick.pro;

import com.ticktick.task.data.User;

@SuppressWarnings("unused")
public final class UnlockProPatch {
	public static boolean shouldBePro(User user) {
		return user.getUsername() != null;
	}
}
