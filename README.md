@ -1,2 +1,217 @@
# economy
Economy system for the BrokenDogs MC server
# BrokenDogs Economy
A lightweight, configurable DogCoin currency system for NeoForge 1.21.1

## Overview

BrokenDogs Economy is a lightweight, server-side NeoForge mod providing a persistent DogCoin currency system for Minecraft 1.21.1.
It is intentionally simple, stable, and fully moddable; designed to act as the economy backbone for other mods (e.g., markets, NPC traders, auction houses).

Key features:

- Persistent per-player balances
- `/bal` and `/pay` commands
- DogCoin rewards scaled by mob difficulty (based on mob max health)
- Daily login reward system
- Fully configurable via a TOML file
- Colourful welcome messages
- Clean, stable API for other mods to hook into

This mod is "core infrastructure" for the BrokenDogs server project, but will work on any NeoForge server.

## DogCoin Currency

Name: DogCoin  
Plural: DogCoins  
Symbol: Ƀ  

Example: Ƀ42 DogCoins

## Features

-------------------------------------------------------------
1. Persistent Balances
-------------------------------------------------------------

Balances are stored server-side using Minecraft's SavedData.

Balances persist across:

- Server restarts
- Player relogs
- World reloads

Storage file:

`world/data/brokendogseconomy_balances.dat`

-------------------------------------------------------------
2. Commands
-------------------------------------------------------------

`/bal` 
Shows the player's DogCoin balance.

`/pay` <player> <amount>  
Transfers DogCoins between players.

- Cannot send negative values
- Fails if sender doesn't have enough DogCoins
- Both players receive notifications

-------------------------------------------------------------
3. Difficulty-Scaled Mob Rewards
-------------------------------------------------------------

Players earn DogCoins proportional to mob difficulty.

Formula:

reward = `round((mob_max_health / 2) * mobKillReward)`

Where mobKillReward = DogCoins per heart (configurable).

Examples (mobKillReward = 2):

- Zombie (10 hearts): 20 DogCoins
- Enderman (20 hearts): 40 DogCoins
- Iron Golem (50 hearts): 100 DogCoins

PvP kills give no money.

-------------------------------------------------------------
4. Daily Login Rewards
-------------------------------------------------------------

Players are awarded DogCoins once per real-world day.

- Checks UTC date
- Default amount: 25
- Cannot be abused by relogging
- Saved in SavedData

Example:

Daily reward: Ƀ25 DogCoins (now Ƀ155 DogCoins).

-------------------------------------------------------------
5. Welcome Messages
-------------------------------------------------------------

Players receive a stylised welcome message explaining:

- What DogCoins are
- Their current balance
- How to use /bal and /pay

Can be turned off in config.

## Configuration

Configuration file automatically generated:

config/brokendogseconomy-common.toml

Default contents:

[economy]  
    startingBalance = 0  
    mobKillReward = 2  
    dailyLoginReward = 25  
    showJoinMessage = true  

Restart the server after editing.

## Developer API

The mod exposes a stable interface for integration.

```java
Import:

import com.brokendogs.economy.EconomyApi;

Available methods:

long getBalance(ServerPlayer player);  
void setBalance(ServerPlayer player, long amount);  
void deposit(ServerPlayer player, long amount);  
boolean withdraw(ServerPlayer player, long amount);  
String formatAmount(long amount);  

Examples:

long bal = EconomyApi.getBalance(player);

if (!EconomyApi.withdraw(player, cost)) {  
    // not enough currency  
}

EconomyApi.deposit(player, 50);

String s = EconomyApi.formatAmount(42); // "Ƀ42 DogCoins"

```

## Integration for Other Mods (e.g., Market Mod)

In your market mod's mods.toml:

```toml
[[dependencies.yourmarketmodid]]  
    modId="brokendogseconomy"  
    mandatory=true  
    versionRange="[1.0.0,)"  
    side="BOTH"
```

In build.gradle:

```java 
dependencies {  
    implementation files("libs/brokendogseconomy-1.0.0.jar")  
}
```

## Technical Architecture

- SavedData stores:
  - Player balances
  - Last daily reward day

- EconomyEvents:
  - Mob kill rewards
  - Login rewards
  - Welcome messages

- EconomyApi:
  - Public-facing API for all economy operations

- BrokenDogsConfig:
  - Backing config system for tunable settings

## Future Plans

Possible expansions:

- Admin commands (/eco set, /eco give, /eco take)
- Marketplace mod integration
- Auction houses
- Player-run shops
- NPC merchant system
- Banking (interest, loans)
- Scoreboard/tab-list DogCoin display
- Dimension-based reward multipliers

## Credits

Author: Joshua J  
Team: BrokenDogs Development Team  
Minecraft: 1.21.1  
Loader: NeoForge

