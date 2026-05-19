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

Pre-release. Milestones 0 (scaffolding) and 1 (energy & logistics backbone) complete.

## Smoke tests

- NeoForge: ✅ Launches, `/give @p trienergy:test_block` works and block is placeable. (M0)
- Fabric: ✅ Launches, `/give @p trienergy:test_block` works and block is placeable. (M0)
- M1 smoke (both loaders): ✅ Generator → conduit → consumer chain delivers energy; `/trienergy benchmark` populates `run/benchmarks/<timestamp>.csv`.

## License

MIT — see [LICENSE](LICENSE).
