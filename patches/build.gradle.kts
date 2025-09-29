group = "de.tosox.revanced"

patches {
    about {
        name = "Tosox's ReVanced Patches"
        description = "Patches for ReVanced"
        source = "git@github.com:Tosox/revanced-patches.git"
        author = "Tosox"
        contact = "tosoxdev@gmail.com"
        website = "https://github.com/Tosox"
        license = "GNU General Public License v3.0"
    }
}

kotlin {
    compilerOptions {
        freeCompilerArgs = listOf("-Xcontext-receivers")
    }
}
