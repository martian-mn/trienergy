# Baseline — Milestone 0 (empty mod loaded)

Captured: 2026-05-19

## Reference scenario

- **Mod state:** TriEnergy loaded; `TestBlock` registered; no instances placed in the world.
- **Server config:** dev server launched via `./gradlew :neoforge:runServer` with all defaults (single dedicated server, no players, freshly generated default world).
- **Loader:** NeoForge 21.4.155 on Minecraft 1.21.4.
- **JVM:** `org.gradle.java.home`-pinned JDK 21 (OpenJDK 21.0.11+10), Gradle 8.14.

## Hardware

| | |
|---|---|
| CPU | AMD Ryzen 7 7800X3D (8 cores / 16 threads, boost 5.05 GHz) |
| RAM | 30 GB |
| OS | Arch Linux (CachyOS kernel 7.0.9-1) |
| Architecture | x86_64 |

## NeoForge `forge tps` output

```
[14:12:24] [Server thread/INFO] [minecraft/MinecraftServer]: Overworld: 20.000 TPS (0.029 ms/tick)
[14:12:24] [Server thread/INFO] [minecraft/MinecraftServer]: The Nether:  20.000 TPS (0.005 ms/tick)
[14:12:24] [Server thread/INFO] [minecraft/MinecraftServer]: The End:     20.000 TPS (0.003 ms/tick)
[14:12:24] [Server thread/INFO] [minecraft/MinecraftServer]: Overall:     20.000 TPS (0.044 ms/tick)
```

| Dimension | TPS | ms/tick |
|---|---|---|
| Overworld | 20.000 | 0.029 |
| The Nether | 20.000 | 0.005 |
| The End | 20.000 | 0.003 |
| **Overall** | **20.000** | **0.044** |

## Fabric

**Deferred to Milestone 1.** Vanilla Minecraft's dedicated server has no built-in `tps` command, and installing `spark` (the de-facto Fabric profiling mod) is more work than M0 warrants for an empty-mod baseline. M1 introduces the permanent benchmark harness (master plan §M1) which will measure both loaders under a controlled scenario.

For sanity: empty-mod Fabric tick time is essentially the same as NeoForge — both are running vanilla MC logic with one extra registered (but unplaced) block. The NeoForge number above stands in as the empty-mod baseline for both.

## Notes

- **Headroom budget:** 0.044 ms/tick is ~0.1% of the 50 ms/tick budget at 20 TPS. The remaining 49.956 ms/tick is the engineering budget for everything M1 onward will add. The master plan's headline commitment — ≥ 19 TPS at 200 machines + 4 simulated players — translates to staying under ~52.6 ms/tick under that scenario.
- **Reference scenario completeness:** this baseline does **not** yet conform to the "reference scenario spec" the plan critique flagged as a missing M0 deliverable (full mod list, server JVM config, base composition). That spec is still open. Future milestone baselines should reuse this hardware + JVM block as the host context column but extend the scenario column.
- **Re-running:** to reproduce, `cd /home/martian/trienergy && ./gradlew :neoforge:runServer`, wait for "Done", run `forge tps`, then `stop`.
