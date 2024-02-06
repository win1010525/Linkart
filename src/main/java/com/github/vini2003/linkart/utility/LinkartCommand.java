package com.github.vini2003.linkart.utility;

import com.github.vini2003.linkart.Linkart;
import com.mojang.brigadier.CommandDispatcher;
import me.melontini.dark_matter.api.base.util.Exceptions;
import me.melontini.dark_matter.api.base.util.Mapper;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.util.function.Supplier;

public class LinkartCommand {
    private static final Logger LOGGER = LogManager.getLogger();

    private static final Supplier<Text> RELOADED = () -> TextUtil.literal("reloaded linkart config");
    private static final MethodHandle cachedHandle = Exceptions.supply(() -> {
        try {
            String mthd = Mapper.mapMethod(ServerCommandSource.class, "method_9226", MethodType.methodType(void.class, Supplier.class, boolean.class));
            return MethodHandles.insertArguments(MethodHandles.lookup().findVirtual(ServerCommandSource.class, mthd, MethodType.methodType(void.class, Supplier.class, boolean.class)),
                    1, RELOADED, true);
        } catch (Throwable e) {
            String mthd = Mapper.mapMethod(ServerCommandSource.class, "method_9226", MethodType.methodType(void.class, Text.class, boolean.class));
            return MethodHandles.insertArguments(MethodHandles.lookup().findVirtual(ServerCommandSource.class, mthd, MethodType.methodType(void.class, Text.class, boolean.class)),
                    1, RELOADED.get(), true);
        }
    });

    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(CommandManager.literal("linkart")
                .then(CommandManager.literal("config")
                        .then(CommandManager.literal("reload")
                                .executes(context -> {
                                    Linkart.loadConfig();
                                    invoke(context.getSource());
                                    return 1;
                                }))));
    }

    private static void invoke(Object... args) {
        try {
            LinkartCommand.cachedHandle.invokeWithArguments(args);
        } catch (Throwable e) {
            LOGGER.error(e);
            throw new RuntimeException(e);
        }
    }
}
