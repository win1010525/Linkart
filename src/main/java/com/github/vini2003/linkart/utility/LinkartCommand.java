package com.github.vini2003.linkart.utility;

import com.github.vini2003.linkart.Linkart;
import com.mojang.brigadier.CommandDispatcher;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.MappingResolver;
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
    private static Logger LOGGER = LogManager.getLogger();

    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(CommandManager.literal("linkart")
                .then(CommandManager.literal("config")
                        .then(CommandManager.literal("reload")
                                .executes(context -> {
                                    Linkart.loadConfig();

                                    MappingResolver resolver = FabricLoader.getInstance().getMappingResolver();
                                    try {
                                        String mthd = resolver.mapMethodName("intermediary", "net.minecraft.class_2168", "method_9226", "(Ljava/util/function/Supplier;Z)V");
                                        MethodHandle sendFeedback = MethodHandles.lookup().findVirtual(context.getSource().getClass(), mthd, MethodType.methodType(void.class, Supplier.class, boolean.class));
                                        sendFeedback.invokeWithArguments(context.getSource(), ((Supplier<Text>)() -> TextUtil.literal("reloaded linkart config")), true);
                                    } catch (Throwable e) {
                                        try {
                                            String mthd = resolver.mapMethodName("intermediary", "net.minecraft.class_2168", "method_9226", "(Lnet/minecraft/class_2561;Z)V");
                                            MethodHandle sendFeedback = MethodHandles.lookup().findVirtual(context.getSource().getClass(), mthd, MethodType.methodType(void.class, Text.class, boolean.class));
                                            sendFeedback.invokeWithArguments(context.getSource(), TextUtil.literal("reloaded linkart config"), true);
                                        } catch (Throwable ex) {
                                            LOGGER.error(ex);
                                            throw new RuntimeException(ex);
                                        }
                                    }

                                    return 1;
                                }))));
    }
}
