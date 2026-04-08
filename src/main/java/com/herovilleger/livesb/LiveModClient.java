package com.herovilleger.livesb;

import com.mojang.brigadier.arguments.StringArgumentType;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.message.v1.ClientReceiveMessageEvents;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.lwjgl.glfw.GLFW;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Files;
import java.nio.file.Path;
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
    public static final Logger LOGGER = LoggerFactory.getLogger("LiveSB");

    // --- Master Config Variables ---
    public static boolean isAfk = false;
    public static boolean autoAccept = true;
    public static boolean mathBot = true;
    public static boolean guildP = true;
    public static boolean privateP = true;
    public static boolean publicP = true;
    public static boolean autoWelcome = true;
    public static boolean deathBot = true;

    public static String welcomeTemplate = "Welcome {player} ( ﾟ◡ﾟ)/";
    public static String boomTemplate = "BOOM {player}";

    private static KeyBinding openMenuKey;
    private static boolean openGuiNextTick = false;

    // --- Cooldowns & Trackers ---
    private static final Map<String, Long> lastAfkMsg = new HashMap<>();
    private static final Map<String, Long> lastWelcomed = new HashMap<>();
    private static final long AFK_COOLDOWN_MS = 60000;
    private static final long WELCOME_COOLDOWN_MS = 20000;

    // --- File Paths ---
    public static final Set<String> whitelist = new HashSet<>();
    private static final Path WHITELIST_FILE = FabricLoader.getInstance().getConfigDir().resolve("livesb_whitelist.txt");
    private static final Path CONFIG_FILE = FabricLoader.getInstance().getConfigDir().resolve("livesb_config.txt");

    // --- Strict Regex Patterns ---
    private static final Pattern P_PATTERN = Pattern.compile("^(Guild >|Party >|From )?\\s*(?:\\[.*?\\]\\s*)?([a-zA-Z0-9_]+)[^:]*:\\s*!p\\b", Pattern.CASE_INSENSITIVE);
    private static final Pattern INVITE_PATTERN = Pattern.compile("(?:\\[.*?\\]\\s*)?([a-zA-Z0-9_]+)\\s+has invited you to join their party!", Pattern.CASE_INSENSITIVE);
    private static final Pattern JOIN_PATTERN = Pattern.compile("^(?:Party Finder >\\s*)?\\(?(?:\\[.*?\\]\\s*)?([a-zA-Z0-9_]+)\\)?\\s+joined the (?:party|dungeon group)", Pattern.CASE_INSENSITIVE);
    private static final Pattern MATH_PATTERN = Pattern.compile("^(Guild >|Party >|From )\\s*(?:\\[.*?\\]\\s*)?([a-zA-Z0-9_]+)[^:]*:\\s*!math\\s+(.+)$", Pattern.CASE_INSENSITIVE);
    private static final Pattern DEATH_PATTERN = Pattern.compile("^(?:☠\\s*)?([a-zA-Z0-9_]+)\\s+.*and became a ghost", Pattern.CASE_INSENSITIVE);

    @Override
    public void onInitializeClient() {
        LOGGER.info("=======================================");
        LOGGER.info("LIVE BHAI KA PREMIUM MOD READY HAI!");
        LOGGER.info("=======================================");

        loadConfig();
        loadWhitelist();

        ClientReceiveMessageEvents.GAME.register((message, overlay) -> {
            if (overlay) return;
            MinecraftClient client = MinecraftClient.getInstance();
            if (client.player == null) return;

            String text = message.getString().replaceAll("§.", "").trim();

            // 1. Auto Accept (Whitelist Based)
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

            // 2. Auto Welcome (Dynamic Template)
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

            // 3. Smart !p Invites (Public, Guild, Private)
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

            // 4. Math Bot
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

            // 5. Smart Auto BOOM
            if (deathBot) {
                Matcher deathMatcher = DEATH_PATTERN.matcher(text);
                if (deathMatcher.find()) {
                    if (!text.toLowerCase().contains("disconnected")) {
                        String deadPlayer = deathMatcher.group(1);
                        String lowerName = deadPlayer.toLowerCase();

                        boolean isIgnoredName = lowerName.equals("you") ||
                                lowerName.equals("party") ||
                                lowerName.equals("guild") ||
                                lowerName.equals("from") ||
                                lowerName.equals("to") ||
                                lowerName.equals(client.getSession().getUsername().toLowerCase());

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

        // --- Commands Registration ---
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
                                context.getSource().sendFeedback(Text.literal("§b[LiveVA] Whitelist: §f" + String.join(", ", whitelist)));
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
            );
        });

        // Keybind (Right Shift)
        openMenuKey = KeyBindingHelper.registerKeyBinding(new KeyBinding("Open VA Menu", InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_RIGHT_SHIFT, KeyBinding.Category.create(Identifier.of("livesb", "category"))));

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            while (openMenuKey.wasPressed()) client.setScreen(new SbaGuiScreen());
            if (openGuiNextTick) {
                client.setScreen(new SbaGuiScreen());
                openGuiNextTick = false;
            }
        });
    }

    // --- File System Methods ---
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
                            case "partyP":
                            case "privateP": privateP = Boolean.parseBoolean(value); break;
                            case "publicP": publicP = Boolean.parseBoolean(value); break;
                            case "autoWelcome": autoWelcome = Boolean.parseBoolean(value); break;
                            case "deathBot": deathBot = Boolean.parseBoolean(value); break;
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

    // --- Math Engine ---
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