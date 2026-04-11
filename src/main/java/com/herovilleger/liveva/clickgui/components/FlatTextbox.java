package com.herovilleger.liveva.clickgui.components;

import io.wispforest.owo.ui.component.TextBoxComponent;
import io.wispforest.owo.ui.core.Insets;
import io.wispforest.owo.ui.core.Sizing;
import net.minecraft.client.gui.DrawContext;

public class FlatTextbox extends TextBoxComponent {
    public int borderColor = 0xff5ca0bf;

    public FlatTextbox(Sizing horizontalSizing) {
        super(horizontalSizing);
        this.verticalSizing(Sizing.fixed(18));
        this.margins(Insets.of(0, 0, 0, 8));
        this.setMaxLength(256);
    }

    @Override
    public void renderWidget(DrawContext context, int mouseX, int mouseY, float deltaTicks) {
        context.fill(this.getX(), this.getY(), this.getX() + this.width() + 4, this.getY() + this.height(), 0xff101010);
        context.fill(this.getX(), this.getY(), this.getX() + this.width() + 4, this.getY() + 1, this.borderColor);
        context.fill(this.getX(), this.getY() + this.height() - 1, this.getX() + this.width() + 4, this.getY() + this.height(), this.borderColor);
        context.fill(this.getX(), this.getY(), this.getX() + 1, this.getY() + this.height(), this.borderColor);
        context.fill(this.getX() + this.width() + 3, this.getY(), this.getX() + this.width() + 4, this.getY() + this.height(), this.borderColor);
        super.renderWidget(context, mouseX, mouseY, deltaTicks);
    }

    @Override
    public boolean drawsBackground() { return false; }

    @Override
    public int getInnerWidth() { return this.width - 8; }
}