package com.herovilleger.liveva;

import com.herovilleger.liveva.clickgui.SbaGuiScreen;
import com.herovilleger.liveva.config.Feature;
import com.mojang.brigadier.arguments.StringArgumentType;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.message.v1.ClientReceiveMessageEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
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

    public static final Feature F_AFK         = new Feature("isAfk",       false);
    public static final Feature F_AUTOACCEPT  = new Feature("autoAccept",  false);
    public static final Feature F_MATHBOT     = new Feature("mathBot",      false);
    public static final Feature F_GUILDP      = new Feature("guildP",       false);
    public static final Feature F_PRIVATEP    = new Feature("privateP",     false);
    public static final Feature F_PUBLICP     = new Feature("publicP",      false);
    public static final Feature F_AUTOWELCOME = new Feature("autoWelcome",  false);
    public static final Feature F_DEATHBOT    = new Feature("deathBot",     false);

    public static String welcomeTemplate = "Welcome {player} ( ﾟ◡ﾟ)/";
    public static String boomTemplate    = "BOOM {player}";

    private static KeyBinding openMenuKey;
    private static boolean openGuiNextTick = false;

    private static final Map<String, Long> lastAfkMsg   = new HashMap<>();
    private static final Map<String, Long> lastWelcomed = new HashMap<>();
    private static final long AFK_COOLDOWN_MS     = 60000;
    private static final long WELCOME_COOLDOWN_MS = 20000;

    public static final Set<String> whitelist       = new HashSet<>();
    private static final Path LIVEVA_DIR     = FabricLoader.getInstance().getConfigDir().resolve("LiveVA");
    private static final Path WHITELIST_FILE = LIVEVA_DIR.resolve("whitelist.txt");
    private static final Path CONFIG_FILE    = LIVEVA_DIR.resolve("config.txt");

    private static final Pattern P_PATTERN     = Pattern.compile("^(Guild >|Party >|From )?\\s*(?:\\[.*?\\]\\s*)?([a-zA-Z0-9_]+)[^:]*:\\s*!p\\b", Pattern.CASE_INSENSITIVE);
    private static final Pattern INVITE_PATTERN = Pattern.compile("(?:\\[.*?\\]\\s*)?([a-zA-Z0-9_]+)\\s+has invited you to join their party!", Pattern.CASE_INSENSITIVE);
    private static final Pattern JOIN_PATTERN  = Pattern.compile("^(?:Party Finder >\\s*)?\\(?(?:\\[.*?\\]\\s*)?([a-zA-Z0-9_]+)\\)?\\s+joined the (?:party|dungeon group)", Pattern.CASE_INSENSITIVE);
    private static final Pattern MATH_PATTERN  = Pattern.compile("^(Guild >|Party >|From )\\s*(?:\\[.*?\\]\\s*)?([a-zA-Z0-9_]+)[^:]*:\\s*!math\\s+(.+)$", Pattern.CASE_INSENSITIVE);
    private static final Pattern DEATH_PATTERN = Pattern.compile("^(?:☠\\s*)?([a-zA-Z0-9_]+)\\s+.*and became a ghost", Pattern.CASE_INSENSITIVE);

    @Override
    public void onInitializeClient() {
        LOGGER.info("=======================================");
        LOGGER.info("LIVE BHAI KA PREMIUM MOD READY HAI!");
        LOGGER.info("=======================================");

        loadConfig();
        loadWhitelist();

        ClientPlayConnectionEvents.JOIN.register((handler, sender, client) -> {
            if (!Files.exists(CONFIG_FILE)) {
                client.execute(() -> {
                    if (client.player != null) {
                        client.player.sendMessage(Text.literal("§b[LiveVA] §fMod install ho gaya! Features use karne ke liye §e/va §ftype karo."), false);
                    }
                });
            }
        });

        ClientReceiveMessageEvents.GAME.register((message, overlay) -> {
            if (overlay) return;
            MinecraftClient client = MinecraftClient.getInstance();
            if (client.player == null) return;

            String text = message.getString().replaceAll("§.", "").trim();

            if (F_AUTOACCEPT.isActive()) {
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

            if (F_AUTOWELCOME.isActive()) {
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

            Matcher pMatcher = P_PATTERN.matcher(text);
            if (pMatcher.find()) {
                String channel     = pMatcher.group(1);
                String requester   = pMatcher.group(2);
                boolean allowInvite = false;

                if (channel == null || channel.trim().isEmpty()) {
                    if (F_PUBLICP.isActive()) allowInvite = true;
                } else {
                    String ch = channel.trim().toLowerCase();
                    if (ch.equals("guild >") && F_GUILDP.isActive())   allowInvite = true;
                    else if (ch.equals("from ") && F_PRIVATEP.isActive()) allowInvite = true;
                }

                if (allowInvite) {
                    if (F_AFK.isActive()) {
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

            if (F_MATHBOT.isActive()) {
                Matcher mathMatcher = MATH_PATTERN.matcher(text);
                if (mathMatcher.find()) {
                    String channel = mathMatcher.group(1);
                    String user    = mathMatcher.group(2);
                    String expr    = mathMatcher.group(3);
                    String ans     = safeCalcJava(expr);
                    if (ans != null) {
                        new Timer().schedule(new TimerTask() {
                            @Override
                            public void run() {
                                client.execute(() -> {
                                    String prefix = channel.toLowerCase();
                                    if (prefix.contains("guild"))      client.getNetworkHandler().sendChatCommand("gc " + user + ", " + expr + " = " + ans);
                                    else if (prefix.contains("party")) client.getNetworkHandler().sendChatCommand("pc " + user + ", " + expr + " = " + ans);
                                    else if (prefix.contains("from"))  client.getNetworkHandler().sendChatCommand("msg " + user + " " + expr + " = " + ans);
                                });
                            }
                        }, 1000);
                    }
                }
            }

            if (F_DEATHBOT.isActive()) {
                Matcher deathMatcher = DEATH_PATTERN.matcher(text);
                if (deathMatcher.find()) {
                    if (!text.toLowerCase().contains("disconnected")) {
                        String deadPlayer = deathMatcher.group(1);
                        String lowerName  = deadPlayer.toLowerCase();
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

        openMenuKey = KeyBindingHelper.registerKeyBinding(new KeyBinding("Open VA Menu", InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_RIGHT_SHIFT, KeyBinding.Category.create(Identifier.of("livesb", "category"))));

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            while (openMenuKey.wasPressed()) client.setScreen(new com.herovilleger.liveva.clickgui.SbaGuiScreen());
            if (openGuiNextTick) {
                client.setScreen(new com.herovilleger.liveva.clickgui.SbaGuiScreen());
                openGuiNextTick = false;
            }
        });
    }

    public static void loadConfig() {
        try {
            Files.createDirectories(LIVEVA_DIR);
            if (Files.exists(CONFIG_FILE)) {
                List<String> lines = Files.readAllLines(CONFIG_FILE);
                for (String line : lines) {
                    if (line.contains("=")) {
                        String[] parts = line.split("=", 2);
                        String key   = parts[0].trim();
                        String value = parts[1].trim();
                        switch (key) {
                            case "isAfk":        F_AFK.setActive(Boolean.parseBoolean(value));         break;
                            case "autoAccept":   F_AUTOACCEPT.setActive(Boolean.parseBoolean(value));  break;
                            case "mathBot":      F_MATHBOT.setActive(Boolean.parseBoolean(value));     break;
                            case "guildP":       F_GUILDP.setActive(Boolean.parseBoolean(value));      break;
                            case "partyP":
                            case "privateP":     F_PRIVATEP.setActive(Boolean.parseBoolean(value));    break;
                            case "publicP":      F_PUBLICP.setActive(Boolean.parseBoolean(value));     break;
                            case "autoWelcome":  F_AUTOWELCOME.setActive(Boolean.parseBoolean(value)); break;
                            case "deathBot":     F_DEATHBOT.setActive(Boolean.parseBoolean(value));    break;
                            case "welcomeTemplate": welcomeTemplate = value;                           break;
                            case "boomTemplate":    boomTemplate    = value;                           break;
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
            Files.createDirectories(LIVEVA_DIR);
            List<String> lines = java.util.Arrays.asList(
                    "isAfk="           + F_AFK.isActive(),
                    "autoAccept="      + F_AUTOACCEPT.isActive(),
                    "mathBot="         + F_MATHBOT.isActive(),
                    "guildP="          + F_GUILDP.isActive(),
                    "privateP="        + F_PRIVATEP.isActive(),
                    "publicP="         + F_PUBLICP.isActive(),
                    "autoWelcome="     + F_AUTOWELCOME.isActive(),
                    "deathBot="        + F_DEATHBOT.isActive(),
                    "welcomeTemplate=" + welcomeTemplate,
                    "boomTemplate="    + boomTemplate
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