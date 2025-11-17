package com.brokendogs.economy;

import org.apache.commons.lang3.tuple.Pair;
import net.neoforged.neoforge.common.ModConfigSpec;

public class BrokenDogsConfig {

    public static final Common COMMON;
    public static final ModConfigSpec COMMON_SPEC;

    static {
        // IMPORTANT: Builder#configure returns org.apache.commons.lang3.tuple.Pair
        Pair<Common, ModConfigSpec> pair =
                new ModConfigSpec.Builder().configure(Common::new);
        COMMON = pair.getLeft();
        COMMON_SPEC = pair.getRight();
    }

    public static class Common {

        public final ModConfigSpec.LongValue startingBalance;
        public final ModConfigSpec.LongValue mobKillReward;
        public final ModConfigSpec.BooleanValue showJoinMessage;
        public final ModConfigSpec.LongValue dailyLoginReward;

        Common(ModConfigSpec.Builder builder) {
            builder.push("economy");

            startingBalance = builder
                    .comment("Starting DogCoins balance for new players")
                    .defineInRange("startingBalance", 0L, 0L, Long.MAX_VALUE);

            mobKillReward = builder
                    .comment("DogCoins earned PER HEART of the mob killed (1 heart = 2 HP)")
                    .defineInRange("mobKillReward", 2L, 0L, Long.MAX_VALUE);

            showJoinMessage = builder
                    .comment("Show the colourful DogCoins welcome message when players join")
                    .define("showJoinMessage", true);

            dailyLoginReward = builder
                    .comment("DogCoins awarded once per real-world day when a player logs in (0 = disabled)")
                    .defineInRange("dailyLoginReward", 25L, 0L, Long.MAX_VALUE);

            builder.pop();
        }
    }
}
