package com.herovilleger.livesb;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;

public class SbaGuiScreen extends Screen {
    private String currentCategory = "General";

    public SbaGuiScreen() {
        super(Text.literal("§bLiveSB Premium Menu"));
    }

    @Override
    protected void init() {
        this.clearChildren();

        int guiWidth = 400;
        int guiHeight = 250;
        int startX = (this.width - guiWidth) / 2;
        int startY = (this.height - guiHeight) / 2;

        int sidebarWidth = 110;
        int contentX = startX + sidebarWidth + 15;
        int contentY = startY + 35;

        // === SIDEBAR TABS ===
        int catY = startY + 35;
        this.addDrawableChild(ButtonWidget.builder(Text.literal(currentCategory.equals("General") ? "§d▶ General" : "General"), button -> {
            currentCategory = "General";
            this.init();
        }).dimensions(startX + 10, catY, sidebarWidth - 20, 20).build());

        this.addDrawableChild(ButtonWidget.builder(Text.literal(currentCategory.equals("Party") ? "§d▶ Party" : "Party"), button -> {
            currentCategory = "Party";
            this.init();
        }).dimensions(startX + 10, catY + 25, sidebarWidth - 20, 20).build());

        this.addDrawableChild(ButtonWidget.builder(Text.literal(currentCategory.equals("Dungeons") ? "§d▶ Dungeons" : "Dungeons"), button -> {
            currentCategory = "Dungeons";
            this.init();
        }).dimensions(startX + 10, catY + 50, sidebarWidth - 20, 20).build());

        // === MAIN CONTENT SETTINGS ===
        int btnWidth = 120;
        int btnHeight = 20;

        if (currentCategory.equals("General")) {
            this.addDrawableChild(ButtonWidget.builder(Text.literal("AFK: " + (LiveModClient.isAfk ? "§aON" : "§cOFF")), button -> {
                LiveModClient.isAfk = !LiveModClient.isAfk;
                LiveModClient.saveConfig();
                button.setMessage(Text.literal("AFK: " + (LiveModClient.isAfk ? "§aON" : "§cOFF")));
            }).dimensions(contentX, contentY, btnWidth, btnHeight).build());

            this.addDrawableChild(ButtonWidget.builder(Text.literal("Math Bot: " + (LiveModClient.mathBot ? "§aON" : "§cOFF")), button -> {
                LiveModClient.mathBot = !LiveModClient.mathBot;
                LiveModClient.saveConfig();
                button.setMessage(Text.literal("Math Bot: " + (LiveModClient.mathBot ? "§aON" : "§cOFF")));
            }).dimensions(contentX, contentY + 30, btnWidth, btnHeight).build());
        }
        else if (currentCategory.equals("Party")) {
            this.addDrawableChild(ButtonWidget.builder(Text.literal("Auto-Accept: " + (LiveModClient.autoAccept ? "§aON" : "§cOFF")), button -> {
                LiveModClient.autoAccept = !LiveModClient.autoAccept;
                LiveModClient.saveConfig();
                button.setMessage(Text.literal("Auto-Accept: " + (LiveModClient.autoAccept ? "§aON" : "§cOFF")));
            }).dimensions(contentX, contentY, btnWidth, btnHeight).build());

            this.addDrawableChild(ButtonWidget.builder(Text.literal("Auto Welcome: " + (LiveModClient.autoWelcome ? "§aON" : "§cOFF")), button -> {
                LiveModClient.autoWelcome = !LiveModClient.autoWelcome;
                LiveModClient.saveConfig();
                button.setMessage(Text.literal("Auto Welcome: " + (LiveModClient.autoWelcome ? "§aON" : "§cOFF")));
            }).dimensions(contentX + btnWidth + 10, contentY, btnWidth, btnHeight).build());

            this.addDrawableChild(ButtonWidget.builder(Text.literal("Guild !p: " + (LiveModClient.guildP ? "§aON" : "§cOFF")), button -> {
                LiveModClient.guildP = !LiveModClient.guildP;
                LiveModClient.saveConfig();
                button.setMessage(Text.literal("Guild !p: " + (LiveModClient.guildP ? "§aON" : "§cOFF")));
            }).dimensions(contentX, contentY + 40, btnWidth, btnHeight).build());

            this.addDrawableChild(ButtonWidget.builder(Text.literal("Private !p: " + (LiveModClient.privateP ? "§aON" : "§cOFF")), button -> {
                LiveModClient.privateP = !LiveModClient.privateP;
                LiveModClient.saveConfig();
                button.setMessage(Text.literal("Private !p: " + (LiveModClient.privateP ? "§aON" : "§cOFF")));
            }).dimensions(contentX + btnWidth + 10, contentY + 40, btnWidth, btnHeight).build());

            this.addDrawableChild(ButtonWidget.builder(Text.literal("Public !p: " + (LiveModClient.publicP ? "§aON" : "§cOFF")), button -> {
                LiveModClient.publicP = !LiveModClient.publicP;
                LiveModClient.saveConfig();
                button.setMessage(Text.literal("Public !p: " + (LiveModClient.publicP ? "§aON" : "§cOFF")));
            }).dimensions(contentX, contentY + 80, btnWidth, btnHeight).build());
        }
        else if (currentCategory.equals("Dungeons")) {
            this.addDrawableChild(ButtonWidget.builder(Text.literal("Auto BOOM: " + (LiveModClient.deathBot ? "§aON" : "§cOFF")), button -> {
                LiveModClient.deathBot = !LiveModClient.deathBot;
                LiveModClient.saveConfig();
                button.setMessage(Text.literal("Auto BOOM: " + (LiveModClient.deathBot ? "§aON" : "§cOFF")));
            }).dimensions(contentX, contentY, btnWidth, btnHeight).build());
        }
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        context.fill(0, 0, this.width, this.height, 0x88000000); // Background dim

        int guiWidth = 400;
        int guiHeight = 250;
        int startX = (this.width - guiWidth) / 2;
        int startY = (this.height - guiHeight) / 2;
        int sidebarWidth = 110;

        // Backgrounds
        context.fill(startX + sidebarWidth, startY, startX + guiWidth, startY + guiHeight, 0xFF1E1E24);
        context.fill(startX, startY, startX + sidebarWidth, startY + guiHeight, 0xFF15151A);

        // Manual Purple Border Fix
        int borderColor = 0xFFAA00AA;
        context.fill(startX - 1, startY - 1, startX + guiWidth + 1, startY, borderColor);
        context.fill(startX - 1, startY + guiHeight, startX + guiWidth + 1, startY + guiHeight + 1, borderColor);
        context.fill(startX - 1, startY, startX, startY + guiHeight, borderColor);
        context.fill(startX + guiWidth, startY, startX + guiWidth + 1, startY + guiHeight, borderColor);

        // Divider
        context.fill(startX + sidebarWidth, startY + 24, startX + guiWidth, startY + 25, 0xFF303038);

        // Text
        context.drawTextWithShadow(this.textRenderer, "Categories", startX + 25, startY + 10, 0xAA00AA);
        context.drawTextWithShadow(this.textRenderer, "Settings: " + currentCategory, startX + sidebarWidth + 15, startY + 10, 0xFFFFFF);

        super.render(context, mouseX, mouseY, delta);
    }
}