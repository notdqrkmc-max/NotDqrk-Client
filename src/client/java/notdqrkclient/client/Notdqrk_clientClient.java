package notdqrkclient.client;

import org.lwjgl.glfw.GLFW;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.hud.VanillaHudElements;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import notdqrkclient.Notdqrk_client;
import notdqrkclient.client.visual.Cosmetics;
import notdqrkclient.client.visual.InfoOverlay;
import notdqrkclient.client.visual.ParticleEffects;
import notdqrkclient.client.visual.SkyOverlay;
import notdqrkclient.client.visual.VisualConfig;
import notdqrkclient.client.visual.VisualsScreen;

public class Notdqrk_clientClient implements ClientModInitializer {
	private static KeyBinding openMenuKey;

	@Override
	public void onInitializeClient() {
		VisualConfig.load();

		openMenuKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
				"key.notdqrk-client.open_menu",
				InputUtil.Type.KEYSYM,
				GLFW.GLFW_KEY_RIGHT_SHIFT,
				KeyBinding.Category.create(Notdqrk_client.id("main"))));

		// Sky / tint / filter layer, then the info pills, both just before the chat layer.
		HudElementRegistry.attachElementBefore(
				VanillaHudElements.CHAT,
				Notdqrk_client.id("sky_overlay"),
				SkyOverlay::render);

		HudElementRegistry.attachElementBefore(
				VanillaHudElements.CHAT,
				Notdqrk_client.id("info_overlay"),
				InfoOverlay::render);

		ClientTickEvents.END_CLIENT_TICK.register(client -> {
			while (openMenuKey.wasPressed()) {
				client.setScreen(new VisualsScreen());
			}
			ParticleEffects.tick(client);
			Cosmetics.tick(client);
		});
	}
}
