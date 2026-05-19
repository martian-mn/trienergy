package com.trienergy.network.bench;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import dev.architectury.event.events.common.CommandRegistrationEvent;
import dev.architectury.event.events.common.TickEvent;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Registers the {@code /trienergy benchmark <conduits> <machines>} command.
 *
 * <p>When invoked, the command places the benchmark layout starting one block
 * above the command source position, then starts a 100-tick measurement window.
 * On completion, results are written to {@code run/benchmarks/}.</p>
 *
 * <p>Call {@link #register()} once from {@code TriEnergy.init()}.</p>
 */
public final class BenchmarkCommand {

    /** Active measurement windows; driven by the SERVER_POST tick listener. */
    private static final List<BenchmarkRunner.MeasurementWindow> ACTIVE = new ArrayList<>();

    private static final int TICKS_TO_RUN = 100; // 5 seconds at 20 TPS

    private BenchmarkCommand() {}

    /** Registers the command and the server-tick listener. */
    public static void register() {
        CommandRegistrationEvent.EVENT.register(
                (dispatcher, registry, selection) -> registerWith(dispatcher));
        TickEvent.SERVER_POST.register(server -> tickActive());
    }

    private static void registerWith(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(
            Commands.literal("trienergy")
                .then(Commands.literal("benchmark")
                    .then(Commands.argument("conduits", IntegerArgumentType.integer(1, 5000))
                        .then(Commands.argument("machines", IntegerArgumentType.integer(0, 1000))
                            .executes(BenchmarkCommand::run))))
        );
    }

    private static int run(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        int conduits = IntegerArgumentType.getInteger(ctx, "conduits");
        int machines = IntegerArgumentType.getInteger(ctx, "machines");

        CommandSourceStack src = ctx.getSource();
        ServerLevel level = src.getLevel();
        // Place layout one block above the command source position
        BlockPos anchor = BlockPos.containing(src.getPosition()).offset(0, 1, 0);

        BenchmarkWorldGen.placeBenchmark(level, anchor, conduits, machines);

        src.sendSuccess(() -> Component.literal(
                "[TriEnergy] Benchmark placed: "
                + conduits + " conduits, " + machines + " machines at " + anchor),
                false);

        var window = BenchmarkRunner.start(
                conduits, machines, TICKS_TO_RUN,
                msg -> src.sendSuccess(() -> Component.literal("[TriEnergy] " + msg), false));
        ACTIVE.add(window);

        src.sendSuccess(() -> Component.literal(
                "[TriEnergy] Measuring " + TICKS_TO_RUN
                + " ticks. Results → run/benchmarks/"),
                false);

        return 1;
    }

    /** Drive all active windows; remove completed ones. */
    private static void tickActive() {
        Iterator<BenchmarkRunner.MeasurementWindow> it = ACTIVE.iterator();
        while (it.hasNext()) {
            if (it.next().tick()) {
                it.remove();
            }
        }
    }
}
