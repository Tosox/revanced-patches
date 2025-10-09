package de.tosox.revanced.patches.flatastic.premium

import app.revanced.patcher.patch.rawResourcePatch

@Suppress("unused")
val unlockPremiumPatch = rawResourcePatch(
    name = "Unlock Premium",
    description = "Unlocks the Premium plan",
) {
    // Tested with 3.11.0
    compatibleWith("com.flatastic.app")

    execute {
        get("assets/www/redesign.bundle.js").apply {
            val js = readText()
                .replace(
                    Regex("""setUserProperty\("is_premium",(\w+)\.isPremium\)"""),
                    """setUserProperty("is_premium",true)"""
                )
                .replace(
                    Regex("""setCustomUserProperty\("is_premium",(\w+)\.isPremium.toString\(\)\)"""),
                    """setCustomUserProperty("is_premium","true")"""
                )
                .replace(
                    Regex("""isPremium=function\(\)\{return!!(\w+)\.isLoggedIn&&(\w+)\.properties\.isPremium\}"""),
                    """isPremium=function(){return true}"""
                )
            writeText(js)
        }
    }
}
