package dimaag;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

public class KeyScreen extends Screen {
    private TextFieldWidget field;
    private Text error = Text.empty();

    public KeyScreen() {
        super(Text.literal(DimaagLo.NAME));
    }

    @Override
    protected void init() {
        field = new TextFieldWidget(textRenderer, width / 2 - 100, height / 2 + 20, 200, 20, Text.literal("Key"));
        field.setMaxLength(32);
        addDrawableChild(field);
        setInitialFocus(field);
        addDrawableChild(ButtonWidget.builder(Text.literal("Unlock"), b -> tryUnlock())
                .dimensions(width / 2 - 100, height / 2 + 50, 200, 20).build());
    }

    private void tryUnlock() {
        if (DimaagLo.KEY.equals(field.getText().trim())) {
            DimaagLo.unlocked = true;
            close();
        } else {
            error = Text.literal("Galat key!").formatted(Formatting.RED);
        }
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (keyCode == 257 || keyCode == 335) { tryUnlock(); return true; }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean shouldCloseOnEsc() { return false; }

    @Override
    public void render(DrawContext c, int mx, int my, float delta) {
        super.render(c, mx, my, delta);
        c.drawCenteredTextWithShadow(textRenderer, Text.literal("WARNING").formatted(Formatting.RED, Formatting.BOLD), width / 2, height / 2 - 60, 0xFFFFFF);
        c.drawCenteredTextWithShadow(textRenderer, Text.literal("Ye hack client sirf ek video ke liye hai."), width / 2, height / 2 - 40, 0xFFFFFF);
        c.drawCenteredTextWithShadow(textRenderer, Text.literal("Entertainment only. Use at your own risk, ban ki zimmedari aapki."), width / 2, height / 2 - 28, 0xFFFFFF);
        c.drawCenteredTextWithShadow(textRenderer, Text.literal("Agar try karna hai to neeche apni key daalo:"), width / 2, height / 2 - 8, 0xAAAAAA);
        c.drawCenteredTextWithShadow(textRenderer, error, width / 2, height / 2 + 80, 0xFFFFFF);
    }
}
