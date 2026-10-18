package dimaag;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.gui.screen.TitleScreen;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import org.lwjgl.glfw.GLFW;

public class DimaagLo implements ClientModInitializer {
    // Menu ka naam. Agar Hindi font sahi na dikhe to "Dimaag Lo" kar dena.
    public static final String NAME = "दिमाग लो";
    public static final String KEY = "123";
    public static boolean unlocked = false;
    private static KeyBinding menuKey;

    @Override
    public void onInitializeClient() {
        menuKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.dimaag.menu", InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_RIGHT_SHIFT, "category.dimaag"));

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (!unlocked) {
                if (client.currentScreen instanceof TitleScreen) client.setScreen(new KeyScreen());
                while (menuKey.wasPressed()) { }
                return;
            }
            while (menuKey.wasPressed()) {
                if (client.currentScreen == null) client.setScreen(new MenuScreen());
            }
            if (client.player == null || client.world == null || client.interactionManager == null) return;
            Modules.tick(client);
        });
    }
}
