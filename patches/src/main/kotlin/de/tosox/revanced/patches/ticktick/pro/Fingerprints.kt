package de.tosox.revanced.patches.ticktick.pro

import app.revanced.patcher.fingerprint

internal val isProFingerprint = fingerprint {
    custom { method, classDef ->
        classDef.endsWith("/User;") && method.name == "isPro"
    }
}

internal val getProTypeForFakeFingerprint = fingerprint {
    custom { method, classDef ->
        classDef.endsWith("/User;") && method.name == "getProTypeForFake"
    }
}
