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
 - [`mojangapi` shows how a fork could patch arbitrary non-git directories (such as `Paper-MojangAPI`)](https://github.com/PaperMC/paperweight-examples/tree/mojangapi)
 - [`submodules-mojang` shows the same as `mojangapi`, but on the git submodules setup from `submodules`](https://github.com/PaperMC/paperweight-examples/tree/submodules-mojangapi)

## Setup
Note that when importing this project into your IDE of choice for the first time, you may experience an error such as `Could not create task ':generateDevelopmentBundle'.`. This is expected behavior - you should follow the instructions below to setup your workspace.

1. Clone this repo (e.g. `git clone https://github.com/PaperMC/paperweight-examples`)
2. Run the `applyPatches` gradle task and let it complete.
3. Open paperweight-examples in your IDE. Your IDE's import process should now succeed. 
