package de.tosox.revanced.patches.kicker.pur

import app.revanced.patcher.fingerprint

internal val getAboStateFingerprint = fingerprint {
    custom { method, classDef ->
        classDef.endsWith("/KUserImpl;") && method.name == "getAboState"
    }
}
