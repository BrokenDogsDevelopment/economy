package com.brokendogs.economy;

import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.storage.DimensionDataStorage;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class EconomyData extends SavedData {

    private static final String DATA_NAME = "brokendogseconomy_balances";

    private final Map<UUID, Long> balances = new HashMap<>();
    private final Map<UUID, Long> lastDailyRewardDay = new HashMap<>();

    public EconomyData() {
    }

    // ----- Factory methods -----

    public static EconomyData create() {
        return new EconomyData();
    }

    public static EconomyData load(CompoundTag tag, HolderLookup.Provider lookupProvider) {
        EconomyData data = create();

        // Balances
        ListTag balanceList = tag.getList("balances", Tag.TAG_COMPOUND);
        for (int i = 0; i < balanceList.size(); i++) {
            CompoundTag entry = balanceList.getCompound(i);
            UUID id = entry.getUUID("id");
            long bal = entry.getLong("bal");
            data.balances.put(id, bal);
        }

        // Daily reward days
        ListTag dailyList = tag.getList("daily", Tag.TAG_COMPOUND);
        for (int i = 0; i < dailyList.size(); i++) {
            CompoundTag entry = dailyList.getCompound(i);
            UUID id = entry.getUUID("id");
            long day = entry.getLong("day");
            data.lastDailyRewardDay.put(id, day);
        }

        return data;
    }

    public static EconomyData get(ServerLevel level) {
        DimensionDataStorage storage = level.getDataStorage();
        return storage.computeIfAbsent(
                new SavedData.Factory<>(EconomyData::create, EconomyData::load),
                DATA_NAME
        );
    }

    // ----- Balance logic -----

    public long getBalance(UUID id) {
        return balances.getOrDefault(id, 0L);
    }

    public void setBalance(UUID id, long amount) {
        if (amount < 0) amount = 0;
        balances.put(id, amount);
        setDirty();
    }

    public void addBalance(UUID id, long delta) {
        long current = getBalance(id);
        setBalance(id, current + delta);
    }

    // ----- Daily reward tracking -----

    /**
     * Last epoch day a daily reward was given to this player.
     * Uses LocalDate.toEpochDay(). Returns Long.MIN_VALUE if never.
     */
    public long getLastDailyRewardDay(UUID id) {
        return lastDailyRewardDay.getOrDefault(id, Long.MIN_VALUE);
    }

    public void setLastDailyRewardDay(UUID id, long epochDay) {
        lastDailyRewardDay.put(id, epochDay);
        setDirty();
    }

    // ----- Saving to disk -----

    @Override
    public CompoundTag save(CompoundTag tag, HolderLookup.Provider registries) {
        // Balances
        ListTag balanceList = new ListTag();
        for (Map.Entry<UUID, Long> entry : balances.entrySet()) {
            CompoundTag entryTag = new CompoundTag();
            entryTag.putUUID("id", entry.getKey());
            entryTag.putLong("bal", entry.getValue());
            balanceList.add(entryTag);
        }
        tag.put("balances", balanceList);

        // Daily reward days
        ListTag dailyList = new ListTag();
        for (Map.Entry<UUID, Long> entry : lastDailyRewardDay.entrySet()) {
            CompoundTag entryTag = new CompoundTag();
            entryTag.putUUID("id", entry.getKey());
            entryTag.putLong("day", entry.getValue());
            dailyList.add(entryTag);
        }
        tag.put("daily", dailyList);

        return tag;
    }
}
