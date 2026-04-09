package com.herovilleger.liveva.clickgui;

import io.wispforest.owo.ui.base.BaseOwoScreen;
import io.wispforest.owo.ui.base.BaseComponent;
import io.wispforest.owo.ui.component.ButtonComponent;
import io.wispforest.owo.ui.component.Components;
import io.wispforest.owo.ui.container.FlowLayout;
import io.wispforest.owo.ui.container.ScrollContainer;
import io.wispforest.owo.ui.container.Containers;
import io.wispforest.owo.ui.core.*;
import net.minecraft.client.gui.Click;
import net.minecraft.client.input.KeyInput;
import net.minecraft.text.Text;
import com.herovilleger.liveva.clickgui.components.*;
import org.jetbrains.annotations.NotNull;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class Settings extends BaseOwoScreen<FlowLayout> {
    public static final ButtonComponent.Renderer buttonRenderer = (context, button, delta) -> {
        context.fill(button.getX(), button.getY(), button.getX() + button.getWidth(), button.getY() + button.getHeight(), 0xff101010);
        context.drawRectOutline(button.getX(), button.getY(), button.getWidth(), button.getHeight(), 0xff5ca0bf);
    };
    public static final ButtonComponent.Renderer buttonRendererWhite = (context, button, delta) -> {
        context.fill(button.getX(), button.getY(), button.getX() + button.getWidth(), button.getY() + button.getHeight(), 0xff101010);
        context.drawRectOutline(button.getX(), button.getY(), button.getWidth(), button.getHeight(), 0xffffffff);
    };

    public List<FlowLayout> settings;
    public Text title = Text.empty();
    public ScrollContainer<FlowLayout> scroll;

    public Settings(List<FlowLayout> settings) {
        this.settings = settings;
    }

    public Settings(FlowLayout... settings) {
        this(List.of(settings));
    }

    private static ButtonComponent buildResetButton(Consumer<ButtonComponent> onPress) {
        ButtonComponent button = Components.button(Text.literal("Reset").withColor(0xffffff), onPress);
        button.positioning(Positioning.relative(100, 0));
        button.renderer(buttonRendererWhite);
        return button;
    }

    private static boolean isBinding(List<FlowLayout> settings, int button) {
        for (FlowLayout setting : settings) {
            for (Component child : setting.children()) {
                if (findKeybindButton(child, button)) return true;
            }
        }
        return false;
    }

    private static boolean findKeybindButton(Component child, int button) {
        if (child instanceof KeybindButton keybind) {
            if (keybind.isBinding) { keybind.bind(button); return true; }
        } else if (child instanceof FlowLayout layout) {
            for (Component layoutChild : layout.children()) {
                if (findKeybindButton(layoutChild, button)) return true;
            }
        }
        return false;
    }

    @Override
    public boolean keyPressed(KeyInput input) {
        if (isBinding(this.settings, input.key())) return true;
        if (input.key() == GLFW.GLFW_KEY_PAGE_UP || input.key() == GLFW.GLFW_KEY_PAGE_DOWN) {
            this.scroll.onMouseScroll(0, 0, input.key() == GLFW.GLFW_KEY_PAGE_UP ? 4 : -4);
            return true;
        }
        return super.keyPressed(input);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        this.scroll.onMouseScroll(0, 0, verticalAmount * 2);
        return true;
    }

    @Override
    public boolean mouseClicked(Click click, boolean doubled) {
        if (isBinding(this.settings, click.button())) return true;
        return super.mouseClicked(click, doubled);
    }

    @Override
    protected @NotNull OwoUIAdapter<FlowLayout> createAdapter() {
        return OwoUIAdapter.create(this, Containers::verticalFlow);
    }

    @Override
    protected void build(FlowLayout root) {
        root.surface(Surface.VANILLA_TRANSLUCENT);
        root.alignment(HorizontalAlignment.CENTER, VerticalAlignment.CENTER);
        FlowLayout parent = Containers.verticalFlow(Sizing.content(), Sizing.content());
        parent.padding(Insets.of(5));
        Color textColor = Color.ofArgb(0xffffffff);
        FlowLayout settingsLayout = Containers.verticalFlow(Sizing.content(), Sizing.content());
        settingsLayout.surface(Surface.flat(0xaa000000)).alignment(HorizontalAlignment.LEFT, VerticalAlignment.CENTER);
        int width = 300;
        List<FlowLayout> optionsMutable = new ArrayList<>(this.settings);
        for (FlowLayout option : optionsMutable) {
            option.horizontalSizing(Sizing.fixed(width));
            settingsLayout.child(option);
        }
        int settingsH = (int) Math.clamp(settingsLayout.children().size() * 30, 30,
                net.minecraft.client.MinecraftClient.getInstance().getWindow().getScaledHeight() * 0.8);
        this.scroll = Containers.verticalScroll(Sizing.content(), Sizing.fixed(settingsH), settingsLayout)
                .scrollbarThiccness(2)
                .scrollbar(ScrollContainer.Scrollbar.flat(Color.ofArgb(0xffffffff)));
        BaseComponent label = new PlainLabel(this.title)
                .color(textColor)
                .horizontalTextAlignment(HorizontalAlignment.CENTER)
                .verticalTextAlignment(VerticalAlignment.CENTER);
        ParentComponent header = Containers.verticalFlow(Sizing.fixed(width), Sizing.content())
                .child(label)
                .alignment(HorizontalAlignment.CENTER, VerticalAlignment.CENTER)
                .padding(Insets.of(3))
                .surface(Surface.flat(0xff5ca0bf));
        parent.child(header);
        parent.child(this.scroll);
        root.child(parent);
    }

    @Override
    public void close() {
        net.minecraft.client.MinecraftClient.getInstance().setScreen(new SbaGuiScreen());
    }

    public Settings setTitle(Text title) {
        this.title = title;
        return this;
    }

    public static class Toggle extends FlowLayout {
        public com.herovilleger.liveva.config.SettingBool setting;
        public ToggleButton toggle;

        public Toggle(String name, com.herovilleger.liveva.config.SettingBool setting, String tooltip) {
            super(Sizing.content(), Sizing.content(), Algorithm.HORIZONTAL);
            this.padding(Insets.of(5));
            this.horizontalAlignment(HorizontalAlignment.LEFT);
            this.setting = setting;
            PlainLabel label = new PlainLabel(Text.literal(name).withColor(0xffffff));
            label.tooltip(Text.literal(tooltip));
            this.toggle = new ToggleButton(this.setting.value());
            this.toggle.onToggled().subscribe(value -> this.setting.set(value));
            label.verticalTextAlignment(VerticalAlignment.CENTER).margins(Insets.of(0, 0, 0, 5)).verticalSizing(Sizing.fixed(20));
            this.child(label);
            this.child(this.toggle);
            this.child(buildResetButton(btn -> {
                this.setting.reset();
                this.toggle.setToggle(this.setting.value());
            }));
        }
    }
}