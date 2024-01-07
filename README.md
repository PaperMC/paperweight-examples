# ForkTest - A Paper fork, using paperweight

This is an example project, showcasing how to setup a fork of Paper (or any other fork using paperweight), using paperweight.

The files of most interest are
- build.gradle.kts
- settings.gradle.kts
- gradle.properties

When updating upstream, be sure to keep the dependencies noted in `build.gradle.kts` in sync with upstream.
It's also a good idea to use the same version of the Gradle wrapper as upstream.

## Tasks

```
Paperweight tasks
-----------------
applyApiPatches
applyPatches
applyServerPatches
cleanCache - Delete the project setup cache and task outputs.
createMojmapBundlerJar - Build a runnable bundler jar
createMojmapPaperclipJar - Build a runnable paperclip jar
createReobfBundlerJar - Build a runnable bundler jar
createReobfPaperclipJar - Build a runnable paperclip jar
generateDevelopmentBundle
rebuildApiPatches
rebuildPatches
rebuildServerPatches
reobfJar - Re-obfuscate the built jar to obf mappings
runDev - Spin up a non-relocated Mojang-mapped test server
runReobf - Spin up a test server from the reobfJar output jar
runShadow - Spin up a test server from the shadowJar archiveFile
```

## Branches

Each branch of this project represents an example:

 - [`main` is the standard example](https://github.com/PaperMC/paperweight-examples/tree/main)
 - [`submodules` shows how paperweight can be applied on a fork using the more traditional git submodule system](https://github.com/PaperMC/paperweight-examples/tree/submodules)
