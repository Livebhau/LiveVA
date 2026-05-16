# 🚀 LiveVA Premium | The Flex Update 

**LiveVA** is a highly optimized, client-side Fabric mod designed to dominate your multiplayer experience on Hypixel Skyblock. It takes the annoyance out of repetitive tasks like party management, chat interactions, and RNG drop announcements, letting you focus 100% on grinding and flexing your loot.

*Note: This mod solely automates chat commands and provides visual/audio utilities. It strictly adheres to server rules and does NOT automate player movement, combat, or inventory management.*

---

## 🌟 The Ultimate Feature List

### 🎲 RNG Auto-Flex Engine (NEW)
Never miss a chance to flex your insane luck! LiveVA intelligently tracks your high-value drops without any extra effort on your part.
* **Smart Loot Scanner:** The mod instantly scans Obsidian and Bedrock chests. If it spots an RNG drop (like Necron's Handle, Shadow Warp, or Master Stars), it triggers a massive on-screen Title Alert and auto-announces the drop to your Guild or Party.
* **Slayer & Boss Auto-Flex:** Don't have a chest to open? LiveVA instantly detects `RARE DROP`, `CRAZY RARE DROP`, and `INSANE DROP` messages in your chat and auto-flexes them for you!
* **VIP Chat Actions ("Click-to-Flex"):** Whenever you get a rare drop, LiveVA generates a private message with interactive **`[ALL]`**, **`[PARTY]`**, and **`[GUILD]`** buttons. Just click one to instantly share your drop and Magic Find stats!

### 🖱️ 1-Click Ghost UI (NEW)
We've brought the GUI directly into your chat! Managing your lists is now faster than ever.
* Type `/va list` or `/va listsounds` to view your active friends or sound triggers.
* Every entry features a red, clickable **`[✖]`** button next to it. Just click it to instantly remove that player or sound trigger—no typing required!

### 🎛️ Premium In-Game GUI
Manage all your settings without ever touching a config file!
* Press **`Right Shift`** (or type `/va`) to open a sleek, dark-themed interactive menu.
* Contains categorized tabs (General, Party, Dungeons) to easily toggle features ON/OFF.
* **Instant Auto-Save:** Every click is instantly saved to your local config.

### 🤝 Smart Party & Guild Utilities
* **Whitelist Auto-Accept:** Add trusted friends to your whitelist. The mod will automatically accept their party invites within a second.
* **Smart `!p` Routing:** Automatically invite players who type `!p` in chat. Independent toggles available for Public Lobbies, Guild Chat, and Private Messages.
* **AFK Auto-Reply:** Going AFK? Turn on AFK mode. If someone sends a `!p` request, the mod auto-replies to them (features a 60-second anti-spam cooldown).

### 🤖 Dynamic Chat Bots
* **Auto-Welcome:** Greets new players joining your party/dungeon automatically. Use `{player}` to dynamically mention their name.
* **Dynamic Roast Bot (Auto-BOOM):** Roasts party members in chat when they die in a dungeon. (Safely ignores disconnects, ghosts, and your own deaths to prevent self-roasting).
* **In-Game Math Engine:** Turn your chat into a calculator! Any player can type `!math <equation>` (e.g., `!math 250 * 4 + 10`) in Guild, Party, or Private messages, and your client will automatically calculate and reply with the correct answer.

### 🎵 Custom Audio Alerts
Never miss an important message or rare drop, even when tabbed out!
* Link any `.wav` audio file to a specific text trigger in chat. 
* Place your `.wav` files inside the `.minecraft/config/liveva_sounds/` folder.

---

## ⚙️ Commands Guide

Forget how to use a feature? Just type **`/va help`** in-game for a beautifully formatted list of all commands!

**Whitelist Commands:**
* `/va add <player_name>` - Add a player to your auto-accept whitelist.
* `/va list` - Display everyone currently on your whitelist (Click `[✖]` to remove).

**Custom Sound Commands:**
* `/va addsound "<text_trigger>" <filename.wav>` - Bind a sound to chat text. *(Example: `/va addsound "has joined the guild" welcome.wav`)*
* `/va listsounds` - View all active sound triggers (Click `[✖]` to remove).

**Custom Message Commands:**
* `/va setwelcome <custom_message>` - Set your party join message (Use `{player}` for username).
* `/va setboom <custom_message>` - Set your dungeon death roast message (Use `{player}` for username).
* `/va info` - View your currently active custom messages.

---

## 📥 Installation & Requirements

1. **Minecraft Version:** 1.20.X
2. **Mod Loader:** [Fabric Loader](https://fabricmc.net/)
3. **Dependencies:** [Fabric API](https://modrinth.com/mod/fabric-api) is required for the mod to function correctly.
4. Download the latest `liveva-x.x.x.jar`.
5. Drop the `.jar` file into your `.minecraft/mods` folder and launch the game!

---
*Developed by LiveBhai. Built for the Hypixel Skyblock Community.*
