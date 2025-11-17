package com.brokendogs.economy;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;

import java.time.LocalDate;
import java.time.ZoneOffset;

public class EconomyEvents {

    private static long safeMobKillPerHeart() {
        try {
            return BrokenDogsConfig.COMMON.mobKillReward.get();
        } catch (IllegalStateException e) {
            // Config not ready – use default 2 DogCoins per heart
            return 2L;
        }
    }

    private static long safeDailyReward() {
        try {
            return BrokenDogsConfig.COMMON.dailyLoginReward.get();
        } catch (IllegalStateException e) {
            // Config not ready – disable daily reward until it is
            return 0L;
        }
    }

    private static boolean safeShowJoinMessage() {
        try {
            return BrokenDogsConfig.COMMON.showJoinMessage.get();
        } catch (IllegalStateException e) {
            // Config not ready – default to true so players still see info
            return true;
        }
    }

    /**
     * How many DogCoins to pay for this mob, based on its max health.
     * 1 heart = 2 health = 1 unit; reward = hearts * perHeartReward.
     */
    private static long calculateMobKillReward(LivingEntity victim) {
        long perHeart = safeMobKillPerHeart();
        if (perHeart <= 0) {
            return 0L; // disabled
        }

        double maxHealth = victim.getMaxHealth(); // e.g. 20.0 for 10 hearts
        double hearts = maxHealth / 2.0D;

        long reward = Math.round(hearts * perHeart);
        return Math.max(0L, reward);
    }

    @SubscribeEvent
    public void onMobKilled(LivingDeathEvent event) {
        if (!(event.getSource().getEntity() instanceof ServerPlayer player)) {
            return;
        }

        if (!(event.getEntity() instanceof LivingEntity victim)) {
            return;
        }

        if (victim instanceof ServerPlayer) {
            return; // no PvP reward
        }

        long reward = calculateMobKillReward(victim);
        if (reward <= 0) {
            return;
        }

        EconomyApi.deposit(player, reward);

        long newBalance = EconomyApi.getBalance(player);
        player.sendSystemMessage(
                Component.literal("You earned " + EconomyApi.formatAmount(reward) +
                                " (now " + EconomyApi.formatAmount(newBalance) + ").")
                        .withStyle(ChatFormatting.GREEN)
        );
    }

    @SubscribeEvent
    public void onPlayerLoggedIn(PlayerEvent.PlayerLoggedInEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) {
            return;
        }

        ServerLevel level = player.serverLevel();
        EconomyData data = EconomyData.get(level);

        // Daily login reward
        long dailyReward = safeDailyReward();
        if (dailyReward > 0) {
            long todayEpochDay = LocalDate.now(ZoneOffset.UTC).toEpochDay();
            long lastDay = data.getLastDailyRewardDay(player.getUUID());

            if (todayEpochDay > lastDay) {
                EconomyApi.deposit(player, dailyReward);
                data.setLastDailyRewardDay(player.getUUID(), todayEpochDay);

                long newBalance = EconomyApi.getBalance(player);
                player.sendSystemMessage(
                        Component.literal("Daily reward: " + EconomyApi.formatAmount(dailyReward) +
                                        " (now " + EconomyApi.formatAmount(newBalance) + ").")
                                .withStyle(ChatFormatting.AQUA)
                );
            }
        }

        // Welcome message
        if (!safeShowJoinMessage()) {
            return;
        }

        long balance = EconomyApi.getBalance(player);

        Component line1 = Component.literal("[")
                .withStyle(ChatFormatting.DARK_AQUA)
                .append(Component.literal("BrokenDogs").withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD))
                .append(Component.literal("] ").withStyle(ChatFormatting.DARK_AQUA))
                .append(Component.literal("Welcome, ")
                        .withStyle(ChatFormatting.GREEN))
                .append(Component.literal(player.getGameProfile().getName())
                        .withStyle(ChatFormatting.YELLOW))
                .append(Component.literal("!")
                        .withStyle(ChatFormatting.GREEN));

        Component line2 = Component.literal("→ ")
                .withStyle(ChatFormatting.DARK_AQUA)
                .append(Component.literal("This server uses ")
                        .withStyle(ChatFormatting.AQUA))
                .append(Component.literal(EconomyApi.CURRENCY_NAME_PLURAL)
                        .withStyle(ChatFormatting.GOLD))
                .append(Component.literal(" (")
                        .withStyle(ChatFormatting.AQUA))
                .append(Component.literal(EconomyApi.CURRENCY_SYMBOL)
                        .withStyle(ChatFormatting.LIGHT_PURPLE))
                .append(Component.literal(") as currency.")
                        .withStyle(ChatFormatting.AQUA));

        Component line3 = Component.literal("→ ")
                .withStyle(ChatFormatting.DARK_AQUA)
                .append(Component.literal("Use ")
                        .withStyle(ChatFormatting.AQUA))
                .append(Component.literal("/bal")
                        .withStyle(ChatFormatting.YELLOW))
                .append(Component.literal(" to check your balance (currently ")
                        .withStyle(ChatFormatting.AQUA))
                .append(Component.literal(EconomyApi.formatAmount(balance))
                        .withStyle(ChatFormatting.GOLD))
                .append(Component.literal(") and ")
                        .withStyle(ChatFormatting.AQUA))
                .append(Component.literal("/pay <player> <amount>")
                        .withStyle(ChatFormatting.YELLOW))
                .append(Component.literal(" to send DogCoins.")
                        .withStyle(ChatFormatting.AQUA));

        player.sendSystemMessage(line1);
        player.sendSystemMessage(line2);
        player.sendSystemMessage(line3);
    }
}
