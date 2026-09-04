package de.tosox.revanced.patches.duolingo

import app.revanced.patcher.*
import app.revanced.patcher.extensions.addInstructions
import app.revanced.patcher.patch.BytecodePatchContext
import app.revanced.patcher.patch.PatchException
import app.revanced.patcher.patch.bytecodePatch
import de.tosox.revanced.util.findEnumStaticField

private const val APP_ICON_TYPE = "Lcom/duolingo/feature/appicon/AppIconType;"
private const val APP_DISPLAY_NAME = "Lcom/duolingo/feature/appicon/AppDisplayName;"
private const val APP_ICON_ORIGIN = "Lcom/duolingo/feature/appicon/AppIconHelper\$Origin;"

private const val CLASSIC_APP_ICON_TYPE = "STREAK_SOCIETY"
private const val CLASSIC_LAUNCHER_ALIAS = "com.duolingo.app.StreakSocietyLauncher"

internal val BytecodePatchContext.setAppIconFingerprint by composingFirstMethod {
    returnType("Ljava/lang/Object;")
    parameterTypes("L", APP_ICON_ORIGIN, "L")
    instructions(
        reference("Landroid/content/pm/PackageManager;->setComponentEnabledSetting")
    )
}

@Suppress("unused")
val keepClassicAppIconPatch = bytecodePatch(
    name = "Keep Classic App Icon",
    description = "Stops the app from swapping its icon and name and restores the classic green owl",
) {
    // Tested with 6.94.5
    compatibleWith("com.duolingo")

    apply {
        val setAppIcon = setAppIconFingerprint.method
        val appIconStateType = setAppIcon.parameterTypes.first().toString()

        val classicIcon = findEnumStaticField(CLASSIC_APP_ICON_TYPE, APP_ICON_TYPE)
            ?: throw PatchException("Could not find the app icon type: $CLASSIC_APP_ICON_TYPE")
        val defaultName = findEnumStaticField("DEFAULT", APP_DISPLAY_NAME)
            ?: throw PatchException("Could not find the default app display name")

        firstMethodComposite {
            definingClass(APP_ICON_TYPE)
            name("<clinit>")
            strings(CLASSIC_LAUNCHER_ALIAS)
        }.immutableMethodOrNull
            ?: throw PatchException("Could not find the launcher alias: $CLASSIC_LAUNCHER_ALIAS")

        val appIconStateConstructor = "$appIconStateType-><init>($APP_ICON_TYPE$APP_DISPLAY_NAME)V"
        firstClassDef(appIconStateType).methods.firstOrNull {
            it.name == "<init>" &&
                    it.parameterTypes.map(CharSequence::toString) == listOf(APP_ICON_TYPE, APP_DISPLAY_NAME)
        } ?: throw PatchException("Could not find the app icon state constructor")

        val applyAppIconState = firstMethodComposite {
            returnType("Ljava/lang/Object;")
            parameterTypes(appIconStateType, APP_ICON_ORIGIN, "L")
            instructions(
                method { definingClass == setAppIcon.definingClass && name == setAppIcon.name }
            )
        }.method

        listOf(setAppIcon, applyAppIconState).forEach {
            it.addInstructions(
                0,
                """
                    new-instance v0, $appIconStateType
                    sget-object v1, $classicIcon
                    sget-object v2, $defaultName
                    invoke-direct { v0, v1, v2 }, $appIconStateConstructor
                    move-object/from16 p1, v0
                """
            )
        }
    }
}
