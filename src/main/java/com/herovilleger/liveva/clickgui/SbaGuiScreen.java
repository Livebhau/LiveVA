package com.herovilleger.liveva.clickgui;

import io.wispforest.owo.ui.base.BaseOwoScreen;
import io.wispforest.owo.ui.container.FlowLayout;
import io.wispforest.owo.ui.container.Containers;
import io.wispforest.owo.ui.core.*;
import com.herovilleger.liveva.LiveModClient;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class SbaGuiScreen extends BaseOwoScreen<FlowLayout> {

    @Override
    protected @NotNull OwoUIAdapter<FlowLayout> createAdapter() {
        return OwoUIAdapter.create(this, Containers::horizontalFlow);
    }

    @Override
    protected void build(FlowLayout root) {
        root.surface(Surface.VANILLA_TRANSLUCENT);
        root.alignment(HorizontalAlignment.LEFT, VerticalAlignment.TOP);
        root.padding(Insets.of(0));

        root.child(new Category("General", List.of(
                new Module("AFK Mode",    LiveModClient.F_AFK,          "Sends AFK message when someone !p's you"),
                new Module("Math Bot",    LiveModClient.F_MATHBOT,      "Calculates !math expressions in chat")
        )));

        root.child(new Category("Party", List.of(
                new Module("Auto Accept",  LiveModClient.F_AUTOACCEPT,  "Auto accepts party invites from whitelist"),
                new Module("Auto Welcome", LiveModClient.F_AUTOWELCOME, "Sends welcome message when someone joins"),
                new Module("Guild !p",     LiveModClient.F_GUILDP,      "Invites players who use !p in guild chat"),
                new Module("Private !p",   LiveModClient.F_PRIVATEP,    "Invites players who DM you with !p"),
                new Module("Public !p",    LiveModClient.F_PUBLICP,     "Invites players who use !p in public chat")
        )));

        root.child(new Category("Dungeons", List.of(
                new Module("Auto BOOM",   LiveModClient.F_DEATHBOT,    "Sends BOOM message when a player dies")
        )));
    }

    @Override
    public boolean shouldPause() {
        return false;
    }
}