package de.tosox.revanced.patches.earphonealarm.premium

import app.revanced.patcher.fingerprint

internal val getPlanStatusFingerprint = fingerprint {
    custom { method, classDef ->
        classDef.endsWith("/SharedPrefs;") && method.name == "getPlanStatus"
    }
}
