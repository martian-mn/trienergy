package com.trienergy.network.bench;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.time.Instant;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;

/**
 * Accumulates per-tick timing across a fixed window and writes a CSV row
 * to {@code run/benchmarks/<timestamp>.csv} when the window closes.
 *
 * <p>Usage: call {@link #start} once after placing the benchmark layout.
 * Wire the returned {@link MeasurementWindow#tick()} to every server tick
 * until it returns {@code true}.</p>
 */
public final class BenchmarkRunner {
    private BenchmarkRunner() {}

    private static final String CSV_HEADER = "timestamp,conduits,machines,mean_tps,mean_ms_per_tick\n";

    /**
     * Begin a measurement window of {@code ticksToRun} server ticks.
     *
     * <p>The window starts from the moment this method is called.  Each call
     * to {@link MeasurementWindow#tick()} records one elapsed tick.  When the
     * count reaches {@code ticksToRun} the mean tick time and TPS are computed
     * from the wall-clock span, a CSV file is written, and the window reports
     * itself complete.</p>
     *
     * @param conduits   conduit count (metadata written to CSV)
     * @param machines   machine count (metadata written to CSV)
     * @param ticksToRun number of ticks to measure
     * @param reportTo   callback to deliver status messages to the player
     * @return the window; caller must invoke {@link MeasurementWindow#tick()} each tick
     */
    public static MeasurementWindow start(
            int conduits, int machines, int ticksToRun, Consumer<String> reportTo) {

        long wallStart = System.nanoTime();
        AtomicInteger ticksElapsed = new AtomicInteger(0);

        BooleanSupplier callback = () -> {
            int n = ticksElapsed.incrementAndGet();
            if (n < ticksToRun) {
                return false; // not done yet
            }

            // Window complete — compute stats
            long totalNanos = System.nanoTime() - wallStart;
            double meanMsPerTick = (totalNanos / 1_000_000.0) / ticksToRun;
            // TPS: 1000 ms / meanMsPerTick, capped at 20.0
            double tps = (meanMsPerTick > 0 && meanMsPerTick < 50.0) ? 20.0
                    : (meanMsPerTick >= 50.0 ? 1000.0 / meanMsPerTick : 20.0);

            String csvRow = String.format(
                    "%s,%d,%d,%.3f,%.3f%n",
                    Instant.now(), conduits, machines, tps, meanMsPerTick);

            writeCsv(csvRow, reportTo);
            return true; // done
        };

        return new MeasurementWindow(callback);
    }

    private static void writeCsv(String row, Consumer<String> reportTo) {
        try {
            Path benchDir = Paths.get("run", "benchmarks");
            Files.createDirectories(benchDir);
            String filename = "benchmark-"
                    + Instant.now().toString().replace(":", "-")
                    + ".csv";
            Path csv = benchDir.resolve(filename);
            Files.writeString(csv, CSV_HEADER + row,
                    StandardOpenOption.CREATE, StandardOpenOption.WRITE);
            reportTo.accept("Benchmark complete. CSV: " + csv.toAbsolutePath());
        } catch (IOException e) {
            reportTo.accept("Benchmark CSV write failed: " + e.getMessage());
        }
    }

    /**
     * Holds the per-tick callback for one measurement run.
     *
     * @param tickCallback returns {@code true} once the window is complete
     */
    public record MeasurementWindow(BooleanSupplier tickCallback) {
        /** Advance the window by one tick. Returns {@code true} when done. */
        public boolean tick() {
            return tickCallback.getAsBoolean();
        }
    }
}
