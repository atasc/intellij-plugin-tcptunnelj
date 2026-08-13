## Build

    ./gradlew clean buildPlugin

Artifact: `build/distributions/intellij-plugin-tcptunnelj-<pluginVersion>.zip`

The version comes from `pluginVersion` in `gradle.properties`. Bump it before building, otherwise the IDE installs a package with the same version number as the one already there and there is no way to tell them apart in Settings > Plugins.

### Sandbox (does not touch the installed IDE)

    ./gradlew runIde

Starts a throwaway IDE (2023.3.8, see `platformVersion`) with the plugin loaded. Preferred while developing:
nothing is written to the real IDE installation.

### Install into the real IDE

1. `./gradlew clean buildPlugin`
2. In IntelliJ: Settings > Plugins > ⚙ > Install Plugin from Disk...
3. Pick `build/distributions/intellij-plugin-tcptunnelj-<pluginVersion>.zip`
4. Restart the IDE when prompted

The IDE unpacks it to the per-IDE plugins directory, replacing any previous copy:

    ~/.local/share/JetBrains/IntelliJIdea<year>.<n>/intellij-plugin-tcptunnelj/     # Linux
    ~/Library/Application Support/JetBrains/IntelliJIdea<year>.<n>/                 # macOS

Each IDE version has its own plugins directory, so installing in 2026.2 leaves 2026.1 on the old build.

### Manual install / uninstall

With the IDE closed, the zip can be unpacked straight into the plugins directory (same layout as above), and a broken install can be removed by deleting the `intellij-plugin-tcptunnelj` folder there.

### Compatibility

`pluginSinceBuild = 233` and `pluginUntilBuild` is commented out in `gradle.properties`, so the built plugin declares `since-build="233"` with no upper bound and loads on any IDE from 2023.3 onwards. If an upper bound is ever restored, a newer IDE will silently refuse to install the zip.

## Marketplace deploy

https://plugins.jetbrains.com/author/me

    ./gradlew publishPlugin     # needs PUBLISH_TOKEN; runs patchChangelog first
