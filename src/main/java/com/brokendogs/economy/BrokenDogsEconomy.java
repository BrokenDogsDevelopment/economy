package com.brokendogs.economy;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.RegisterCommandsEvent;

@Mod(BrokenDogsEconomy.MOD_ID)
public class BrokenDogsEconomy {

    // MUST match mod_id in gradle.properties
    public static final String MOD_ID = "brokendogseconomy";

    public BrokenDogsEconomy(IEventBus modEventBus) {
        // register for events
        NeoForge.EVENT_BUS.register(this);
        NeoForge.EVENT_BUS.register(new EconomyEvents());
    }

    @SubscribeEvent
    public void onRegisterCommands(RegisterCommandsEvent event) {
        CommandDispatcher<CommandSourceStack> dispatcher = event.getDispatcher();

        // /bal -> show your balance
        dispatcher.register(
                Commands.literal("bal")
                        .executes(ctx -> {
                            ServerPlayer player = ctx.getSource().getPlayerOrException();
                            long balance = EconomyApi.getBalance(player);

                            ctx.getSource().sendSuccess(
                                    () -> Component.literal("Balance: " + EconomyApi.formatAmount(balance)),
                                    false
                            );
                            return 1;
                        })
        );

        // /pay <player> <amount> -> send DogCoins to another player
        dispatcher.register(
                Commands.literal("pay")
                        .then(Commands.argument("target", EntityArgument.player())
                                .then(Commands.argument("amount", IntegerArgumentType.integer(1))
                                        .executes(ctx -> {
                                            CommandSourceStack source = ctx.getSource();
                                            ServerPlayer sender = source.getPlayerOrException();
                                            ServerPlayer target = EntityArgument.getPlayer(ctx, "target");
                                            int amount = IntegerArgumentType.getInteger(ctx, "amount");

                                            if (amount <= 0) {
                                                source.sendFailure(Component.literal("Amount must be positive."));
                                                return 0;
                                            }

                                            if (!EconomyApi.withdraw(sender, amount)) {
                                                source.sendFailure(Component.literal("You don't have enough " + EconomyApi.CURRENCY_NAME_PLURAL + "."));
                                                return 0;
                                            }

                                            EconomyApi.deposit(target, amount);

                                            source.sendSuccess(
                                                    () -> Component.literal("Paid " + EconomyApi.formatAmount(amount) + " to " + target.getGameProfile().getName()),
                                                    false
                                            );
                                            target.sendSystemMessage(
                                                    Component.literal("You received " + EconomyApi.formatAmount(amount) + " from " + sender.getGameProfile().getName())
                                            );
                                            return 1;
                                        })
                                )
                        )
        );
    }
}
