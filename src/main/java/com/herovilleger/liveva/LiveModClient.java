package com.herovilleger.liveva;

import com.mojang.brigadier.arguments.StringArgumentType;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.message.v1.ClientReceiveMessageEvents;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.ingame.GenericContainerScreen;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.text.ClickEvent;
import net.minecraft.text.HoverEvent;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import org.lwjgl.glfw.GLFW;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class LiveModClient implements ClientModInitializer {
    public static final Logger LOGGER = LoggerFactory.getLogger("LiveVA");

    // --- Master Config Variables ---
    public static boolean isAfk = false;
    public static boolean autoAccept = true;
    public static boolean mathBot = true;
    public static boolean guildP = true;
    public static boolean privateP = true;
    public static boolean publicP = true;
    public static boolean autoWelcome = true;
    public static boolean deathBot = true;

    public static boolean rngGuildMsg = false;
    public static boolean rngPartyMsg = true;

    public static String welcomeTemplate = "Welcome {player} ( ﾟ◡ﾟ)/";
    public static String boomTemplate = "BOOM {player}";

    private static KeyBinding openMenuKey;
    private static boolean openGuiNextTick = false;

    private static final Map<String, Long> lastAfkMsg = new HashMap<>();
    private static final Map<String, Long> lastWelcomed = new HashMap<>();
    private static final long AFK_COOLDOWN_MS = 60000;
    private static final long WELCOME_COOLDOWN_MS = 20000;

    private static int tickDelay = -1;
    private static GenericContainerScreen currentChestScreen = null;
    private static boolean alreadyScanned = false;

    private static final List<String> RNG_DROPS = Arrays.asList(
            "Dark Claymore", "Necron's Handle", "Implosion", "Wither Shield", "Shadow Warp",
            "Fifth Master Star", "Necron Dye", "Master Skull - Tier 5", "Fourth Master Star",
            "Giant's Sword", "Third Master Star", "Shadow Fury", "Second Master Star",
            "Spirit Wing", "Spirit Bone", "First Master Star", "Recombobulator 3000"
    );

    private static final List<String> CHEST_NAMES = Arrays.asList(
            "Wood", "Gold", "Diamond", "Emerald", "Obsidian", "Bedrock"
    );

    public static final Set<String> whitelist = new HashSet<>();
    public static final Map<String, String> customSounds = new HashMap<>();

    private static final Path WHITELIST_FILE = FabricLoader.getInstance().getConfigDir().resolve("liveva_whitelist.txt");
    private static final Path CONFIG_FILE = FabricLoader.getInstance().getConfigDir().resolve("liveva_config.txt");
    private static final Path SOUNDS_FILE = FabricLoader.getInstance().getConfigDir().resolve("liveva_sounds.txt");
    private static final File SOUND_FOLDER = new File(FabricLoader.getInstance().getConfigDir().toFile(), "liveva_sounds");

    private static final Pattern P_PATTERN = Pattern.compile("^(Guild >|Party >|From )?\\s*(?:\\[.*?\\]\\s*)?([a-zA-Z0-9_]+)[^:]*:\\s*!p\\b", Pattern.CASE_INSENSITIVE);
    private static final Pattern INVITE_PATTERN = Pattern.compile("(?:\\[.*?\\]\\s*)?([a-zA-Z0-9_]+)\\s+has invited you to join their party!", Pattern.CASE_INSENSITIVE);
    private static final Pattern JOIN_PATTERN = Pattern.compile("^(?:Party Finder >\\s*)?\\(?(?:\\[.*?\\]\\s*)?([a-zA-Z0-9_]+)\\)?\\s+joined the (?:party|dungeon group)", Pattern.CASE_INSENSITIVE);
    private static final Pattern MATH_PATTERN = Pattern.compile("^(Guild >|Party >|From )\\s*(?:\\[.*?\\]\\s*)?([a-zA-Z0-9_]+)[^:]*:\\s*!math\\s+(.+)$", Pattern.CASE_INSENSITIVE);
    private static final Pattern DEATH_PATTERN = Pattern.compile("^(?:☠\\s*)?([a-zA-Z0-9_]+)\\s+.*and became a ghost", Pattern.CASE_INSENSITIVE);

    private static final Pattern RARE_PATTERN = Pattern.compile("RARE DROP! (.+) \\(\\+([\\d.,]+)% ✯ Magic Find\\)", Pattern.CASE_INSENSITIVE);
    private static final Pattern CRAZY_PATTERN = Pattern.compile("CRAZY RARE DROP! \\((.+)\\) \\(\\+([\\d.,]+)% ✯ Magic Find\\)", Pattern.CASE_INSENSITIVE);
    private static final Pattern INSANE_PATTERN = Pattern.compile("INSANE DROP! \\((.+)\\) \\(\\+([\\d.,]+)% ✯ Magic Find\\)", Pattern.CASE_INSENSITIVE);

    @Override
    public void onInitializeClient() {
        LOGGER.info("=======================================");
        LOGGER.info("LIVE BHAI KA PREMIUM MOD READY HAI!");
        LOGGER.info("=======================================");

        if (!SOUND_FOLDER.exists()) {
            SOUND_FOLDER.mkdirs();
        }

        loadConfig();
        loadWhitelist();
        loadCustomSounds();

        // ==========================================
        // CHAT MESSAGE SCANNER
        // ==========================================
        ClientReceiveMessageEvents.GAME.register((message, overlay) -> {
            if (overlay) return;
            MinecraftClient client = MinecraftClient.getInstance();
            if (client.player == null) return;

            String text = message.getString().replaceAll("§.", "").trim();
            String lowerText = text.toLowerCase();

            for (Map.Entry<String, String> entry : customSounds.entrySet()) {
                if (lowerText.contains(entry.getKey().toLowerCase())) {
                    playSound(entry.getValue());
                }
            }

            // --- Auto Flex Drops ---
            Matcher rareMatcher = RARE_PATTERN.matcher(text);
            if (rareMatcher.find()) {
                sendClickableMessage("RARE DROP", rareMatcher.group(1), rareMatcher.group(2), Formatting.YELLOW);
                autoFlexDrop(client, rareMatcher.group(1), "RARE DROP", rareMatcher.group(2));
            }

            Matcher crazyMatcher = CRAZY_PATTERN.matcher(text);
            if (crazyMatcher.find()) {
                sendClickableMessage("CRAZY RARE DROP", crazyMatcher.group(1), crazyMatcher.group(2), Formatting.LIGHT_PURPLE);
                autoFlexDrop(client, crazyMatcher.group(1), "CRAZY RARE DROP", crazyMatcher.group(2));
            }

            Matcher insaneMatcher = INSANE_PATTERN.matcher(text);
            if (insaneMatcher.find()) {
                sendClickableMessage("INSANE DROP", insaneMatcher.group(1), insaneMatcher.group(2), Formatting.RED);
                autoFlexDrop(client, insaneMatcher.group(1), "INSANE DROP", insaneMatcher.group(2));
            }

            // --- Auto Accept ---
            if (autoAccept) {
                Matcher invMatcher = INVITE_PATTERN.matcher(text);
                if (invMatcher.find()) {
                    String inviter = invMatcher.group(1).toLowerCase();
                    if (whitelist.contains(inviter)) {
                        client.execute(() -> client.player.sendMessage(Text.literal("§a[LiveVA] Auto-Accepting invite from " + inviter), false));
                        new Timer().schedule(new TimerTask() {
                            @Override
                            public void run() { client.execute(() -> client.getNetworkHandler().sendChatCommand("party accept " + inviter)); }
                        }, 800);
                    }
                }
            }

            // --- Auto Welcome ---
            if (autoWelcome) {
                Matcher joinMatcher = JOIN_PATTERN.matcher(text);
                if (joinMatcher.find()) {
                    String newPlayer = joinMatcher.group(1);
                    long now = System.currentTimeMillis();

                    if (now - lastWelcomed.getOrDefault(newPlayer, 0L) > WELCOME_COOLDOWN_MS) {
                        lastWelcomed.put(newPlayer, now);
                        new Timer().schedule(new TimerTask() {
                            @Override
                            public void run() {
                                String msgToSend = welcomeTemplate.replace("{player}", newPlayer);
                                client.execute(() -> client.getNetworkHandler().sendChatCommand("pc " + msgToSend));
                            }
                        }, 1500);
                    }
                }
            }

            // --- Smart !p ---
            Matcher pMatcher = P_PATTERN.matcher(text);
            if (pMatcher.find()) {
                String channel = pMatcher.group(1);
                String requester = pMatcher.group(2);
                boolean allowInvite = false;

                if (channel == null || channel.trim().isEmpty()) {
                    if (publicP) allowInvite = true;
                } else {
                    String ch = channel.trim().toLowerCase();
                    if (ch.equals("guild >") && guildP) allowInvite = true;
                    else if (ch.equals("from ") && privateP) allowInvite = true;
                }

                if (allowInvite) {
                    if (isAfk) {
                        long now = System.currentTimeMillis();
                        if (now - lastAfkMsg.getOrDefault(requester, 0L) > AFK_COOLDOWN_MS) {
                            lastAfkMsg.put(requester, now);
                            new Timer().schedule(new TimerTask() {
                                @Override
                                public void run() { client.execute(() -> client.getNetworkHandler().sendChatCommand("msg " + requester + " afk hu")); }
                            }, 500);
                        }
                    } else {
                        client.getNetworkHandler().sendChatCommand("p " + requester);
                    }
                }
            }

            // --- Math Bot ---
            if (mathBot) {
                Matcher mathMatcher = MATH_PATTERN.matcher(text);
                if (mathMatcher.find()) {
                    String channel = mathMatcher.group(1);
                    String user = mathMatcher.group(2);
                    String expr = mathMatcher.group(3);
                    String ans = safeCalcJava(expr);
                    if (ans != null) {
                        new Timer().schedule(new TimerTask() {
                            @Override
                            public void run() {
                                client.execute(() -> {
                                    String prefix = channel.toLowerCase();
                                    if (prefix.contains("guild")) client.getNetworkHandler().sendChatCommand("gc " + user + ", " + expr + " = " + ans);
                                    else if (prefix.contains("party")) client.getNetworkHandler().sendChatCommand("pc " + user + ", " + expr + " = " + ans);
                                    else if (prefix.contains("from")) client.getNetworkHandler().sendChatCommand("msg " + user + " " + expr + " = " + ans);
                                });
                            }
                        }, 1000);
                    }
                }
            }

            // --- Auto BOOM ---
            if (deathBot) {
                Matcher deathMatcher = DEATH_PATTERN.matcher(text);
                if (deathMatcher.find()) {
                    if (!text.toLowerCase().contains("disconnected")) {
                        String deadPlayer = deathMatcher.group(1);
                        String lowerName = deadPlayer.toLowerCase();

                        boolean isIgnoredName = lowerName.equals("you") || lowerName.equals("party") ||
                                lowerName.equals("guild") || lowerName.equals("from") ||
                                lowerName.equals("to") || lowerName.equals(client.getSession().getUsername().toLowerCase());

                        if (!isIgnoredName) {
                            new Timer().schedule(new TimerTask() {
                                @Override
                                public void run() {
                                    String boomMsgToSend = boomTemplate.replace("{player}", deadPlayer);
                                    client.execute(() -> client.getNetworkHandler().sendChatCommand("pc " + boomMsgToSend));
                                }
                            }, 1000);
                        }
                    }
                }
            }
        });

        // ==========================================
        // COMMAND REGISTRY
        // ==========================================
        ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) -> {
            dispatcher.register(ClientCommandManager.literal("va")
                    .executes(context -> {
                        openGuiNextTick = true;
                        return 1;
                    })
                    .then(ClientCommandManager.literal("add")
                            .then(ClientCommandManager.argument("player", StringArgumentType.word())
                                    .executes(context -> {
                                        String player = StringArgumentType.getString(context, "player").toLowerCase();
                                        whitelist.add(player);
                                        saveWhitelist();
                                        context.getSource().sendFeedback(Text.literal("§a[LiveVA] " + player + " added to whitelist."));
                                        return 1;
                                    })
                            )
                    )
                    .then(ClientCommandManager.literal("remove")
                            .then(ClientCommandManager.argument("player", StringArgumentType.word())
                                    .executes(context -> {
                                        String player = StringArgumentType.getString(context, "player").toLowerCase();
                                        if (whitelist.remove(player)) {
                                            saveWhitelist();
                                            context.getSource().sendFeedback(Text.literal("§c[LiveVA] " + player + " removed."));
                                        }
                                        return 1;
                                    })
                            )
                    )
                    .then(ClientCommandManager.literal("list")
                            .executes(context -> {
                                context.getSource().sendFeedback(Text.literal("§b=== [LiveVA] Whitelist ==="));
                                if (whitelist.isEmpty()) {
                                    context.getSource().sendFeedback(Text.literal("§7(Empty)"));
                                } else {
                                    for (String player : whitelist) {
                                        MutableText entry = Text.literal("§f- " + player + " ")
                                                .append(Text.literal("§c[✖]")
                                                        .styled(style -> style
                                                                .withClickEvent(new ClickEvent.RunCommand("/va remove " + player))
                                                                .withHoverEvent(new HoverEvent.ShowText(Text.literal("§cClick to remove " + player)))
                                                        ));
                                        context.getSource().sendFeedback(entry);
                                    }
                                }
                                return 1;
                            })
                    )
                    .then(ClientCommandManager.literal("addsound")
                            .then(ClientCommandManager.argument("text", StringArgumentType.string())
                                    .then(ClientCommandManager.argument("filename", StringArgumentType.word())
                                            .executes(context -> {
                                                String textToMatch = StringArgumentType.getString(context, "text");
                                                String filename = StringArgumentType.getString(context, "filename");
                                                customSounds.put(textToMatch, filename);
                                                saveCustomSounds();
                                                context.getSource().sendFeedback(Text.literal("§a[LiveVA] Sound added! When '" + textToMatch + "' appears, '" + filename + "' will play."));
                                                return 1;
                                            })
                                    )
                            )
                    )
                    .then(ClientCommandManager.literal("removesound")
                            .then(ClientCommandManager.argument("text", StringArgumentType.string())
                                    .executes(context -> {
                                        String textToMatch = StringArgumentType.getString(context, "text");
                                        if (customSounds.remove(textToMatch) != null) {
                                            saveCustomSounds();
                                            context.getSource().sendFeedback(Text.literal("§c[LiveVA] Sound trigger for '" + textToMatch + "' removed."));
                                        } else {
                                            context.getSource().sendFeedback(Text.literal("§c[LiveVA] Trigger not found!"));
                                        }
                                        return 1;
                                    })
                            )
                    )
                    .then(ClientCommandManager.literal("listsounds")
                            .executes(context -> {
                                context.getSource().sendFeedback(Text.literal("§b=== [LiveVA] Active Sounds ==="));
                                if (customSounds.isEmpty()) {
                                    context.getSource().sendFeedback(Text.literal("§7(No sounds active)"));
                                } else {
                                    for (Map.Entry<String, String> entry : customSounds.entrySet()) {
                                        String trigger = entry.getKey();
                                        MutableText line = Text.literal("§eTrigger: §f" + trigger + " §e-> File: §f" + entry.getValue() + " ")
                                                .append(Text.literal("§c[✖]")
                                                        .styled(style -> style
                                                                .withClickEvent(new ClickEvent.RunCommand("/va removesound \"" + trigger + "\""))
                                                                .withHoverEvent(new HoverEvent.ShowText(Text.literal("§cClick to remove trigger: " + trigger)))
                                                        ));
                                        context.getSource().sendFeedback(line);
                                    }
                                }
                                return 1;
                            })
                    )
                    .then(ClientCommandManager.literal("setwelcome")
                            .then(ClientCommandManager.argument("message", StringArgumentType.greedyString())
                                    .executes(context -> {
                                        welcomeTemplate = StringArgumentType.getString(context, "message");
                                        saveConfig();
                                        context.getSource().sendFeedback(Text.literal("§a[LiveVA] Welcome message set to: §f" + welcomeTemplate));
                                        return 1;
                                    })
                            )
                    )
                    .then(ClientCommandManager.literal("setboom")
                            .then(ClientCommandManager.argument("message", StringArgumentType.greedyString())
                                    .executes(context -> {
                                        boomTemplate = StringArgumentType.getString(context, "message");
                                        saveConfig();
                                        context.getSource().sendFeedback(Text.literal("§a[LiveVA] BOOM message set to: §f" + boomTemplate));
                                        return 1;
                                    })
                            )
                    )
                    .then(ClientCommandManager.literal("info")
                            .executes(context -> {
                                context.getSource().sendFeedback(Text.literal("§b=== [LiveVA] Current Messages ==="));
                                context.getSource().sendFeedback(Text.literal("§eWelcome: §f" + welcomeTemplate));
                                context.getSource().sendFeedback(Text.literal("§cBOOM: §f" + boomTemplate));
                                context.getSource().sendFeedback(Text.literal("§b================================="));
                                return 1;
                            })
                    )
                    .then(ClientCommandManager.literal("help")
                            .executes(context -> {
                                context.getSource().sendFeedback(Text.literal("§b§l=== 🚀 LiveVA Premium Commands ===§r"));
                                context.getSource().sendFeedback(Text.literal("§d/va §f- Open the GUI Settings Menu"));
                                context.getSource().sendFeedback(Text.literal(" "));
                                context.getSource().sendFeedback(Text.literal("§a» Party Whitelist:"));
                                context.getSource().sendFeedback(Text.literal("  §e/va add <player> §f- Add to auto-accept"));
                                context.getSource().sendFeedback(Text.literal("  §e/va list §f- View & manage whitelist"));
                                context.getSource().sendFeedback(Text.literal(" "));
                                context.getSource().sendFeedback(Text.literal("§b» Custom Chat Sounds:"));
                                context.getSource().sendFeedback(Text.literal("  §e/va addsound \"<text>\" <file.wav> §f- Bind a sound"));
                                context.getSource().sendFeedback(Text.literal("  §e/va listsounds §f- View & manage active sounds"));
                                context.getSource().sendFeedback(Text.literal(" "));
                                context.getSource().sendFeedback(Text.literal("§c» Auto Chat Messages:"));
                                context.getSource().sendFeedback(Text.literal("  §e/va setwelcome <msg> §f- Set party join message"));
                                context.getSource().sendFeedback(Text.literal("  §e/va setboom <msg> §f- Set dungeon death roast"));
                                context.getSource().sendFeedback(Text.literal("  §e/va info §f- View currently set messages"));
                                context.getSource().sendFeedback(Text.literal("§b§l==================================§r"));
                                return 1;
                            })
                    )
            );
        });

        openMenuKey = KeyBindingHelper.registerKeyBinding(new KeyBinding("Open VA Menu", InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_RIGHT_SHIFT, KeyBinding.Category.create(Identifier.of("liveva", "category"))));

        // ==========================================
        // GUI TICK & CHEST SCANNER
        // ==========================================
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            while (openMenuKey.wasPressed()) client.setScreen(new SbaGuiScreen());
            if (openGuiNextTick) {
                client.setScreen(new SbaGuiScreen());
                openGuiNextTick = false;
            }

            if (tickDelay > 0) {
                tickDelay--;
            } else if (tickDelay == 0) {
                tickDelay = -1;
                if (!alreadyScanned) {
                    scanChestForLoot(client);
                    alreadyScanned = true;
                }
            }

            if (client.currentScreen instanceof GenericContainerScreen containerScreen) {
                if (currentChestScreen != containerScreen) {
                    currentChestScreen = containerScreen;
                    alreadyScanned = false;
                    String title = containerScreen.getTitle().getString().trim();
                    if (isDungeonChest(title)) {
                        tickDelay = 3;
                    }
                }
            } else {
                currentChestScreen = null;
            }
        });
    }

    // ==========================================
    // AUTO FLEX LOGIC
    // ==========================================
    private void autoFlexDrop(MinecraftClient client, String itemName, String dropType, String magicFind) {
        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                client.execute(() -> {
                    if (client.getNetworkHandler() != null) {
                        String flexMsg = dropType + " (" + itemName + ") (+" + magicFind + "% Magic Find)";
                        if (rngGuildMsg) client.getNetworkHandler().sendChatCommand("gc " + flexMsg);
                        if (rngPartyMsg) client.getNetworkHandler().sendChatCommand("pc " + flexMsg);

                        client.inGameHud.setSubtitle(Text.literal("§6Found " + itemName + "!"));
                        client.inGameHud.setTitle(Text.literal("§d§l" + dropType));
                    }
                });
            }
        }, 500);
    }

    // ==========================================
    // HELPER METHODS
    // ==========================================
    private boolean isDungeonChest(String title) {
        for (String chestName : CHEST_NAMES) {
            if (title.contains(chestName)) return true;
        }
        return false;
    }

    private void scanChestForLoot(MinecraftClient client) {
        if (currentChestScreen == null || client.player == null) return;
        ScreenHandler handler = currentChestScreen.getScreenHandler();
        int slotsToScan = Math.min(54, handler.slots.size());

        for (int i = 0; i < slotsToScan; i++) {
            ItemStack stack = handler.getSlot(i).getStack();
            if (!stack.isEmpty()) {
                String itemName = stack.getName().getString().trim();
                if (RNG_DROPS.contains(itemName)) {
                    if (client.getNetworkHandler() != null) {
                        if (rngGuildMsg) client.getNetworkHandler().sendChatCommand("gc Found " + itemName + "!");
                        if (rngPartyMsg) client.getNetworkHandler().sendChatCommand("pc Found " + itemName + "!");

                        client.inGameHud.setSubtitle(Text.literal("§6Found " + itemName + "!"));
                        client.inGameHud.setTitle(Text.literal("§d§lRNG DROP"));
                    }
                }
            }
        }
    }

    private void sendClickableMessage(String dropType, String itemName, String magicFind, Formatting itemColor) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null) return;

        MutableText baseMsg = Text.literal("§bLiveVA§f§8 »§7 Do you Wanna Send your ")
                .append(Text.literal(itemName).formatted(itemColor))
                .append(Text.literal(" §7with §b" + magicFind + "% Magic Find §7to "));

        MutableText allButton = Text.literal("§7[ALL]")
                .styled(style -> style
                        .withClickEvent(new ClickEvent.RunCommand("/ac " + dropType + " (" + itemName + ") (+" + magicFind + "% ✯ Magic Find)"))
                        .withHoverEvent(new HoverEvent.ShowText(Text.literal("§bShare to All Chat")))
                );

        MutableText partyButton = Text.literal("§9[PARTY]")
                .styled(style -> style
                        .withClickEvent(new ClickEvent.RunCommand("/pc " + dropType + " (" + itemName + ") (+" + magicFind + "% ✯ Magic Find)"))
                        .withHoverEvent(new HoverEvent.ShowText(Text.literal("§bShare to Party Chat")))
                );

        MutableText guildButton = Text.literal("§a[GUILD]")
                .styled(style -> style
                        .withClickEvent(new ClickEvent.RunCommand("/gc " + dropType + " (" + itemName + ") (+" + magicFind + "% ✯ Magic Find)"))
                        .withHoverEvent(new HoverEvent.ShowText(Text.literal("§bShare to Guild Chat")))
                );

        MutableText finalMessage = baseMsg
                .append(allButton).append(Text.literal(" "))
                .append(partyButton).append(Text.literal(" "))
                .append(guildButton);

        client.player.sendMessage(finalMessage, false);
    }

    // ==========================================
    // SYSTEM & CONFIG METHODS
    // ==========================================
    private static void playSound(String fileName) {
        new Thread(() -> {
            try {
                File soundFile = new File(SOUND_FOLDER, fileName);
                if (soundFile.exists()) {
                    AudioInputStream audioIn = AudioSystem.getAudioInputStream(soundFile);
                    Clip clip = AudioSystem.getClip();
                    clip.open(audioIn);
                    clip.start();
                } else {
                    LOGGER.warn("Sound file not found: " + soundFile.getAbsolutePath());
                }
            } catch (Exception e) {
                LOGGER.error("Error playing sound: " + fileName, e);
            }
        }).start();
    }

    private static void loadCustomSounds() {
        try {
            if (Files.exists(SOUNDS_FILE)) {
                List<String> lines = Files.readAllLines(SOUNDS_FILE);
                for (String line : lines) {
                    if (line.contains("=")) {
                        String[] parts = line.split("=", 2);
                        customSounds.put(parts[0].trim(), parts[1].trim());
                    }
                }
            }
        } catch (Exception ignored) {}
    }

    private static void saveCustomSounds() {
        try {
            List<String> lines = new java.util.ArrayList<>();
            for (Map.Entry<String, String> entry : customSounds.entrySet()) {
                lines.add(entry.getKey() + "=" + entry.getValue());
            }
            Files.write(SOUNDS_FILE, lines);
        } catch (Exception ignored) {}
    }

    public static void loadConfig() {
        try {
            if (Files.exists(CONFIG_FILE)) {
                List<String> lines = Files.readAllLines(CONFIG_FILE);
                for (String line : lines) {
                    if (line.contains("=")) {
                        String[] parts = line.split("=", 2);
                        String key = parts[0].trim();
                        String value = parts[1].trim();
                        switch (key) {
                            case "isAfk": isAfk = Boolean.parseBoolean(value); break;
                            case "autoAccept": autoAccept = Boolean.parseBoolean(value); break;
                            case "mathBot": mathBot = Boolean.parseBoolean(value); break;
                            case "guildP": guildP = Boolean.parseBoolean(value); break;
                            case "privateP": privateP = Boolean.parseBoolean(value); break;
                            case "publicP": publicP = Boolean.parseBoolean(value); break;
                            case "autoWelcome": autoWelcome = Boolean.parseBoolean(value); break;
                            case "deathBot": deathBot = Boolean.parseBoolean(value); break;
                            case "rngGuildMsg": rngGuildMsg = Boolean.parseBoolean(value); break;
                            case "rngPartyMsg": rngPartyMsg = Boolean.parseBoolean(value); break;
                            case "welcomeTemplate": welcomeTemplate = value; break;
                            case "boomTemplate": boomTemplate = value; break;
                        }
                    }
                }
            }
        } catch (Exception e) {
            LOGGER.error("Failed to load config", e);
        }
    }

    public static void saveConfig() {
        try {
            List<String> lines = java.util.Arrays.asList(
                    "isAfk=" + isAfk,
                    "autoAccept=" + autoAccept,
                    "mathBot=" + mathBot,
                    "guildP=" + guildP,
                    "privateP=" + privateP,
                    "publicP=" + publicP,
                    "autoWelcome=" + autoWelcome,
                    "deathBot=" + deathBot,
                    "rngGuildMsg=" + rngGuildMsg,
                    "rngPartyMsg=" + rngPartyMsg,
                    "welcomeTemplate=" + welcomeTemplate,
                    "boomTemplate=" + boomTemplate
            );
            Files.write(CONFIG_FILE, lines);
        } catch (Exception e) {
            LOGGER.error("Failed to save config", e);
        }
    }

    private static void loadWhitelist() {
        try {
            if (Files.exists(WHITELIST_FILE)) {
                List<String> lines = Files.readAllLines(WHITELIST_FILE);
                for (String line : lines) { if (!line.trim().isEmpty()) whitelist.add(line.trim().toLowerCase()); }
            }
        } catch (Exception ignored) {}
    }

    private static void saveWhitelist() {
        try { Files.write(WHITELIST_FILE, whitelist); } catch (Exception ignored) {}
    }

    private String safeCalcJava(String expr) {
        try {
            expr = expr.replace("**", "^").trim();
            if (!expr.matches("^[0-9+\\-*/%^().\\s]+$")) return null;
            double result = evaluateMath(expr);
            if (Double.isInfinite(result) || Double.isNaN(result)) return null;
            return result == (long) result ? String.valueOf((long) result) : String.valueOf(Math.round(result * 10000.0) / 10000.0);
        } catch (Exception e) { return null; }
    }

    private double evaluateMath(final String str) {
        return new Object() {
            int pos = -1, ch;
            void nextChar() { ch = (++pos < str.length()) ? str.charAt(pos) : -1; }
            boolean eat(int charToEat) {
                while (ch == ' ') nextChar();
                if (ch == charToEat) { nextChar(); return true; }
                return false;
            }
            double parse() {
                nextChar();
                double x = parseExpression();
                if (pos < str.length()) throw new RuntimeException("Unexpected: " + (char)ch);
                return x;
            }
            double parseExpression() {
                double x = parseTerm();
                for (;;) {
                    if      (eat('+')) x += parseTerm();
                    else if (eat('-')) x -= parseTerm();
                    else return x;
                }
            }
            double parseTerm() {
                double x = parseFactor();
                for (;;) {
                    if      (eat('*')) x *= parseFactor();
                    else if (eat('/')) x /= parseFactor();
                    else if (eat('%')) x %= parseFactor();
                    else return x;
                }
            }
            double parseFactor() {
                if (eat('+')) return parseFactor();
                if (eat('-')) return -parseFactor();
                double x;
                int startPos = this.pos;
                if (eat('(')) { x = parseExpression(); eat(')'); }
                else if ((ch >= '0' && ch <= '9') || ch == '.') {
                    while ((ch >= '0' && ch <= '9') || ch == '.') nextChar();
                    x = Double.parseDouble(str.substring(startPos, this.pos));
                } else { throw new RuntimeException("Unexpected: " + (char)ch); }
                if (eat('^')) x = Math.pow(x, parseFactor());
                return x;
            }
        }.parse();
    }
}