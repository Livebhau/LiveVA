# 🚀 LiveVA Premium - The Ultimate Hypixel Skyblock Assistant

**LiveVA** is a highly optimized, client-side Fabric mod designed to enhance your multiplayer experience on Hypixel Skyblock. It takes the annoyance out of repetitive tasks like party management, chat interactions, and RNG drop announcements, letting you focus 100% on grinding and having fun.

*Note: This mod solely automates chat commands and provides visual/audio utilities. It strictly adheres to server rules and does NOT automate player movement, combat, or inventory management.*

---

## 🌟 Core Features

### 🎛️ Premium In-Game GUI
Manage all your settings without ever touching a config file!
* Press **`Right Shift`** (or type `/va`) to open a sleek, dark-themed interactive menu.
* Contains categorized tabs (General, Party, Dungeons) to easily toggle features ON/OFF.
* **Instant Auto-Save:** Every click is instantly saved to your local config.

### 🎲 Auto-Flex & RNG Drop Manager
Never miss a chance to flex your luck! LiveVA intelligently scans both your chests and chat for RNG drops.
* **Dungeon Chest Scanner:** Automatically scans Obsidian and Bedrock chests. If an RNG drop (like Necron's Handle or Shadow Warp) is found, it sends an on-screen Title Alert and automatically flexes it to your Guild or Party (Toggleable in GUI).
* **Slayer/Boss Drop Auto-Flex:** Instantly detects `RARE DROP`, `CRAZY RARE DROP`, and `INSANE DROP` messages in chat and auto-announces them to your Party/Guild based on your GUI settings.
* **Interactive Chat Buttons:** Whenever you get a drop, the mod generates a private VIP message in your chat with clickable buttons: `[ALL]`, `[PARTY]`, and `[GUILD]`. Click a button to instantly share your drop with its Magic Find stats!

### 🤝 Smart Party & Guild Management
* **Whitelist Auto-Accept:** Add trusted friends to your whitelist. The mod will automatically accept their party invites within a second.
* **Interactive Whitelist Management:** Type `/va list` to see your whitelist. Easily remove players by clicking the interactive `[✖]` button right in the chat!
* **Smart `!p` Routing:** Automatically invite players who type `!p` in chat. Independent toggles available for Public Lobbies, Guild Chat, and Private Messages.
* **AFK Auto-Reply:** Going AFK? Turn on AFK mode. If someone sends a `!p` request, the mod auto-replies to them (features a 60-second anti-spam cooldown).

### 🤖 Dynamic Chat Bots
* **Auto-Welcome:** Greets new players joining your party/dungeon automatically. Use `{player}` to dynamically mention their name.
* **Auto-BOOM (Death Detector):** Roasts party members in chat when they die in a dungeon. (Safely ignores disconnects, ghosts, and your own deaths to prevent self-roasting).
* **In-Game Math Engine:** Turn your chat into a calculator! Any player can type `!math <equation>` (e.g., `!math 250 * 4 + 10`) in Guild, Party, or Private messages, and your client will automatically calculate and reply with the correct answer.

### 🎵 Custom Audio Alerts
Never miss an important message or rare drop, even when tabbed out!
* Link any `.wav` audio file to a specific text trigger in chat.
* Place your `.wav` files inside the `.minecraft/config/liveva_sounds/` folder.
* **Interactive Sound Manager:** Type `/va listsounds` to view active triggers. Remove them instantly by clicking the `[✖]` button in chat.

---

## ⚙️ Commands Guide

Forget how to use a feature? Just type **`/va help`** in-game for a beautifully formatted list of all commands!

**Whitelist Commands:**
* `/va add <player_name>` - Add a player to your auto-accept whitelist.
* `/va remove <player_name>` - Remove a player from the whitelist.
* `/va list` - Display everyone currently on your whitelist (Interactive).

**Custom Sound Commands:**
* `/va addsound "<text_trigger>" <filename.wav>` - Bind a sound to chat text. *(Example: `/va addsound "has joined the guild" welcome.wav`)*
* `/va removesound "<text_trigger>"` - Remove a sound trigger.
* `/va listsounds` - View all active sound triggers (Interactive).

**Custom Message Commands:**
* `/va setwelcome <custom_message>` - Set your welcome message. Use `{player}` for the username.
* `/va setboom <custom_message>` - Set your dungeon death roast message. Use `{player}` for the username.
* `/va info` - View your currently active custom messages.

---

## 📥 Installation & Requirements

1. **Minecraft Version:** 1.20.X
2. **Mod Loader:** [Fabric Loader](https://fabricmc.net/)
3. **Dependencies:** [Fabric API](https://modrinth.com/mod/fabric-api) is required for the mod to function correctly.
4. Download the latest `liveva-x.x.x.jar`.
5. Drop the `.jar` file into your `.minecraft/mods` folder and launch the game!

---
*Developed by LiveBhai. For support, suggestions, or bug reports, feel free to open an issue!*