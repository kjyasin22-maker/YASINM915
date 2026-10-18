package dimaag;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import org.lwjgl.glfw.GLFW;

import java.util.function.BooleanSupplier;
import java.util.function.Consumer;

public class MenuScreen extends Screen {
    public MenuScreen() {
        super(Text.literal(DimaagLo.NAME));
    }

    @Override
    protected void init() {
        int y = height / 2 - 50;
        addToggle(y, "Auto Crystal", () -> Modules.autoCrystal, v -> Modules.autoCrystal = v);
        addToggle(y + 26, "Auto Anchor", () -> Modules.autoAnchor, v -> Modules.autoAnchor = v);
        addToggle(y + 52, "Night Vision", () -> Modules.nightVision, v -> Modules.nightVision = v);
        addToggle(y + 78, "Aim Assist", () -> Modules.aimAssist, v -> Modules.aimAssist = v);
    }

    private static Text label(String name, boolean on) {
        return Text.literal(name + ": " + (on ? "ON" : "OFF")).formatted(on ? Formatting.GREEN : Formatting.RED);
    }

    private void addToggle(int y, String name, BooleanSupplier get, Consumer<Boolean> set) {
        addDrawableChild(ButtonWidget.builder(label(name, get.getAsBoolean()), btn -> {
            set.accept(!get.getAsBoolean());
            btn.setMessage(label(name, get.getAsBoolean()));
        }).dimensions(width / 2 - 100, y, 200, 20).build());
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (keyCode == GLFW.GLFW_KEY_RIGHT_SHIFT) { close(); return true; }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean shouldPause() { return false; }

    @Override
    public void render(DrawContext c, int mx, int my, float delta) {
        super.render(c, mx, my, delta);
        c.drawCenteredTextWithShadow(textRenderer, Text.literal(DimaagLo.NAME).formatted(Formatting.BOLD), width / 2, height / 2 - 75, 0xFFFFFF);
    }
}
