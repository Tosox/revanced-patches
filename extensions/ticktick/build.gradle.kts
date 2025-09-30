extension {
    name = "extensions/ticktick.rve"
}

android {
    namespace = "de.tosox.revanced.extension"
}

dependencies {
    compileOnly(project(":extensions:ticktick:stub"))
}
