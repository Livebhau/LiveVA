# 🚀 LiveSB Premium 

**LiveSB Premium** is a powerful, lightweight, and highly customizable Hypixel Skyblock Assistant mod for Minecraft Fabric. Designed for efficiency, it runs quietly in the background to automate tedious tasks, manage parties, and provide utility features right inside your game chat!

![Minecraft Version](https://img.shields.io/badge/Minecraft-1.20.X-success)
![Mod Loader](https://img.shields.io/badge/Fabric-Ready-blue)
![License](https://img.shields.io/badge/License-MIT-green)

---

## ✨ Features

### 🛡️ Smart Party & Guild Management
* **Auto-Accept Invites:** Automatically accepts party invites from your trusted friends (Whitelist system).
* **Smart `!p` System:** Allows players to type `!p` in Public, Guild, or Private messages to automatically get a party invite. (Fully toggleable per chat channel).
* **AFK Auto-Reply:** Automatically replies with an AFK message when someone requests an invite while you are away (1-minute anti-spam cooldown).

### 💬 Dynamic Chat Bots
* **Auto-Welcome:** Welcomes new players joining your party/dungeon automatically. 
* **Auto-BOOM (Death Detector):** Roasts your party members when they die and become a ghost in Dungeons! *(Smart logic: Strictly ignores your own deaths so you don't get roasted!)*
* **Custom Message Templates:** Completely customize your Welcome and BOOM messages using the `{player}` placeholder directly from the game.
* **Math Engine (`!math`):** A fully functional calculator built into the chat. Type `!math 25*4` in Guild, Party, or Private chat, and the bot will reply with the answer!

### 🎨 Premium GUI Menu
* **Tabbed Interface:** A sleek, dark-themed, and organized GUI menu to control all your settings on the fly.
* **Instant Save:** All your toggle preferences and custom messages are saved instantly to `livesb_config.txt`.

---

## ⚙️ Commands

### Main GUI
* `Right Shift` (or `/va`) - Opens the Premium LiveSB Settings Menu.

### Whitelist Management
* `/va add <player>` - Adds a player to your auto-accept whitelist.
* `/va remove <player>` - Removes a player from the whitelist.
* `/va list` - Displays everyone currently on your whitelist.

### Custom Message Setup
* `/va setwelcome <message>` - Sets your custom welcome text. (e.g., `/va setwelcome Welcome {player} to the party!`)
* `/va setboom <message>` - Sets your custom dungeon death text. (e.g., `/va setboom RIP {player} ☠`)
* `/va info` - Shows your currently active message templates.

---

## 📥 Installation

1. Make sure you have [Fabric Loader](https://fabricmc.net/use/installer/) installed for your Minecraft version.
2. Download the Fabric API and place it in your `mods` folder.
3. Download the latest `livesb-X.X.X.jar` from the **Releases** tab.
4. Drop the `.jar` file into your `.minecraft/mods` folder.
5. Launch the game and press `Right Shift` to open the menu!

---

## 📂 Configuration
All your settings are automatically managed by the GUI and saved locally in your `.minecraft/config/` folder:
* `livesb_config.txt` (Saves toggles and custom messages)
* `livesb_whitelist.txt` (Saves trusted players)

---

*Made with ❤️ by LiveBhai*
