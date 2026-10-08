# Create: Workbench

A NeoForge addon for **Create** that adds the **Workbench**: a placeable block
that stores up to eight Toolboxes and serves their contents remotely, as if each
Toolbox were placed.

- Insert whole Toolboxes in a management screen (right-click).
- Open a stored Toolbox's native contents with Ctrl+left-click.
- Press the radial keybind (**Grave**) to bind a hotbar slot to a stored
  Toolbox's Compartment; auto-restock then works with Create's Toolbox parity.
- **Lock** a Workbench with a **Workbench Key** (sneak-right-click); access
  follows key possession.
- Punch to pick the whole Workbench up with its contents, Shulker-style.
- Works on Create contraptions.

## Requirements

- Minecraft 1.21.1 (NeoForge 21.1.213)
- [Create](https://modrinth.com/mod/create) `6.0.6+` (declared `[6.0.6,6.1.0)`)
- Java 21

## Building & running

Use the Gradle wrapper from the repository root:

```bash
./gradlew build                 # compile + jar
./gradlew test                  # unit tests
./gradlew runGameTestServer     # headless server + gametests
./gradlew runClient             # dev client
./gradlew runServer             # dev dedicated server
./gradlew runData               # data generators
./gradlew mutationTest          # PIT mutation testing (90% gate)
```

The build directory is kept outside the project (see `AGENTS.md`), and all
dev-launch JVMs are pinned to Java 21. `org.gradle.configuration-cache` is
disabled because the project directory is shared across operating systems.

## License

MIT — see [LICENSE](LICENSE).
