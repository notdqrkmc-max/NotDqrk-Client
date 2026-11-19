package notdqrkclient.client.visual;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.RenderTickCounter;

/** Watermark, FPS and coordinates, drawn in one of three styles. */
public class InfoOverlay {
	public static void render(DrawContext context, RenderTickCounter tickCounter) {
		MinecraftClient client = MinecraftClient.getInstance();
		if (client.player == null) {
			return;
		}
		int accent = 0xFF000000 | VisualConfig.accent();
		int y = 4;

		if (VisualConfig.watermark || VisualConfig.fpsCounter) {
			String text = VisualConfig.watermark ? "notdqrk client" : "";
			if (VisualConfig.fpsCounter) {
				text += (text.isEmpty() ? "" : "  |  ") + client.getCurrentFps() + " FPS";
			}
			line(context, client, text, 4, y, accent);
			y += 16;
		}

		if (VisualConfig.coords) {
			String text = "XYZ  " + client.player.getBlockX() + "  " + client.player.getBlockY() + "  " + client.player.getBlockZ();
			line(context, client, text, 4, y, accent);
		}
	}

	private static void line(DrawContext context, MinecraftClient client, String text, int x, int y, int accent) {
		switch (VisualConfig.hudStyle) {
			case 1 -> context.drawTextWithShadow(client.textRenderer, text, x + 2, y + 3, accent);
			case 2 -> {
				context.drawTextWithShadow(client.textRenderer, "[", x, y + 3, accent);
				context.drawTextWithShadow(client.textRenderer, text, x + 8, y + 3, 0xFFFFFFFF);
				int end = x + 8 + client.textRenderer.getWidth(text) + 3;
				context.drawTextWithShadow(client.textRenderer, "]", end, y + 3, accent);
			}
			default -> {
				int w = client.textRenderer.getWidth(text) + 12;
				context.fill(x, y, x + w, y + 13, 0xB0120A1E);
				context.fill(x, y, x + 2, y + 13, accent);
				context.drawTextWithShadow(client.textRenderer, text, x + 7, y + 3, 0xFFFFFFFF);
			}
		}
	}
}
