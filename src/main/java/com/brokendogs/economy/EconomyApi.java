package com.brokendogs.economy;

import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;

public class EconomyApi {

    // Currency definition
    public static final String CURRENCY_NAME_SINGULAR = "DogCoin";
    public static final String CURRENCY_NAME_PLURAL   = "DogCoins";
    public static final String CURRENCY_SYMBOL        = "Ƀ";

    // ----- Internal helpers -----

    private static EconomyData getData(ServerPlayer player) {
        MinecraftServer server = player.getServer();
        if (server == null) {
            return null; // shouldn't happen for online players
        }
        ServerLevel overworld = server.overworld();
        return EconomyData.get(overworld);
    }

    private static long getConfiguredStartBalance() {
        try {
            return BrokenDogsConfig.COMMON.startingBalance.get();
        } catch (IllegalStateException e) {
            // Config not loaded yet – fall back to 0
            return 0L;
        }
    }

    // ----- Public API -----

    public static long getBalance(ServerPlayer player) {
        EconomyData data = getData(player);
        if (data == null) return getConfiguredStartBalance();

        long bal = data.getBalance(player.getUUID());

        // If player is new (no entry) and starting balance > 0, seed them
        if (bal == 0L) {
            long start = getConfiguredStartBalance();
            if (start > 0L) {
                data.setBalance(player.getUUID(), start);
                return start;
            }
        }
        return bal;
    }

    public static void setBalance(ServerPlayer player, long amount) {
        EconomyData data = getData(player);
        if (data == null) return;
        data.setBalance(player.getUUID(), amount);
    }

    public static void deposit(ServerPlayer player, long amount) {
        if (amount <= 0) return;
        EconomyData data = getData(player);
        if (data == null) return;
        data.addBalance(player.getUUID(), amount);
    }

    public static boolean withdraw(ServerPlayer player, long amount) {
        if (amount <= 0) return true;
        EconomyData data = getData(player);
        if (data == null) return false;

        long current = data.getBalance(player.getUUID());
        if (current < amount) {
            return false;
        }
        data.setBalance(player.getUUID(), current - amount);
        return true;
    }

    /**
     * Formats an amount as:
     * Ƀ1 DogCoin
     * Ƀ5 DogCoins
     */
    public static String formatAmount(long amount) {
        String name = (amount == 1) ? CURRENCY_NAME_SINGULAR : CURRENCY_NAME_PLURAL;
        return CURRENCY_SYMBOL + amount + " " + name;
    }
}
