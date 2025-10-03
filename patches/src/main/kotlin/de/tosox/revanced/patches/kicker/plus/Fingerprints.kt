package de.tosox.revanced.patches.kicker.plus

import app.revanced.patcher.fingerprint

internal val getAboStatePlusFingerprint = fingerprint {
    custom { method, classDef ->
        classDef.endsWith("/KUserImpl;") && method.name == "getAboStatePlus"
    }
}
