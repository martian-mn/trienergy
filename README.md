# TriEnergy

A Minecraft tech mod for NeoForge and Fabric. Signature mechanic: three energy forms (mechanical, electrical, thermal) with different physics; converting between them is the central engineering puzzle. See `../mc-mod-plan.md` and `../mc-mod-d5-design.md` for the project plan and signature-mechanic design.

## Building

```sh
./gradlew build
```

Produces:

- `neoforge/build/libs/trienergy-neoforge-<version>.jar`
- `fabric/build/libs/trienergy-fabric-<version>.jar`

## Running

```sh
./gradlew :neoforge:runClient    # launches Minecraft + NeoForge + TriEnergy
./gradlew :fabric:runClient      # launches Minecraft + Fabric + TriEnergy
```

## Status

Pre-release. Milestone 0 (scaffolding) is the current target.

## Smoke tests

- NeoForge: ✅ Launches, `/give @p trienergy:test_block` works and block is placeable. (M0)

## License

MIT — see [LICENSE](LICENSE).
