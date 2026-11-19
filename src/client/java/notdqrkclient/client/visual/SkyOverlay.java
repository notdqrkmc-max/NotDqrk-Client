package notdqrkclient.client.visual;

import java.util.Random;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.RenderTickCounter;

/** Draws the selected sky style, the colour tint and the grayscale filter on the HUD layer. */
public class SkyOverlay {
	private static final int STAR_COUNT = 90;
	private static final double[] STAR_X = new double[STAR_COUNT];
	private static final double[] STAR_Y = new double[STAR_COUNT];
	private static final double[] STAR_PHASE = new double[STAR_COUNT];

	static {
		Random r = new Random(1234);
		for (int i = 0; i < STAR_COUNT; i++) {
			STAR_X[i] = r.nextDouble();
			STAR_Y[i] = r.nextDouble() * 0.7;
			STAR_PHASE[i] = r.nextDouble() * Math.PI * 2;
		}
	}

	public static void render(DrawContext context, RenderTickCounter tickCounter) {
		int w = context.getScaledWindowWidth();
		int h = context.getScaledWindowHeight();

		// Desaturation: blending toward mid-grey drains colour from everything underneath.
		if (VisualConfig.grayscale) {
			int a = (int) (VisualConfig.grayAmount * 0.75 * 255);
			context.fill(0, 0, w, h, (a << 24) | 0x808080);
		}

		if (VisualConfig.tintOn) {
			int rgb = VisualConfig.color(VisualConfig.tintColor);
			int a = (int) (VisualConfig.tintStrength * 150);
			context.fillGradient(0, 0, w, h, (a << 24) | rgb, (Math.min(255, a + 25) << 24) | mix(rgb, 0x000000, 0.5));
		}

		if (VisualConfig.skyOn) {
			double intensity = VisualConfig.skyIntensity;
			MinecraftClient client = MinecraftClient.getInstance();
			if (VisualConfig.lookUpGlow && client.player != null) {
				double up = Math.max(0, Math.min(1, -client.player.getPitch() / 60.0));
				intensity *= 0.3 + 0.7 * up;
			}
			draw(context, 0, 0, w, h, VisualConfig.skyStyle, intensity, VisualConfig.skySpeed,
					VisualConfig.color(VisualConfig.skyColor));
		}
	}

	/** Draws one sky style inside a rectangle (used by both the HUD and the menu header). */
	public static void draw(DrawContext ctx, int left, int top, int w, int h, int style,
			double intensity, double speed, int rgb) {
		double t = System.currentTimeMillis() / 1000.0 * speed;
		switch (style) {
			case 1 -> nebula(ctx, left, top, w, h, t, intensity, rgb);
			case 2 -> sunset(ctx, left, top, w, h, t, intensity, rgb);
			case 3 -> storm(ctx, left, top, w, h, t, intensity, rgb);
			case 4 -> starfield(ctx, left, top, w, h, t, intensity, rgb);
			default -> aurora(ctx, left, top, w, h, t, intensity, rgb);
		}
	}

	private static void aurora(DrawContext ctx, int left, int top, int w, int h, double t, double intensity, int rgb) {
		int strips = 64;
		double stripW = (double) w / strips;
		for (int i = 0; i < strips; i++) {
			double band = h * (0.22 + 0.14 * Math.sin(t * 0.6 + i * 0.30) + 0.06 * Math.sin(t * 1.3 + i * 0.7));
			double glow = (0.30 + 0.22 * Math.sin(t * 0.9 + i * 0.22)) * (0.4 + intensity);
			int x1 = left + (int) Math.floor(i * stripW);
			int x2 = left + (int) Math.ceil((i + 1) * stripW);
			ctx.fillGradient(x1, top, x2, top + (int) band, argb(glow, rgb), rgb);
		}
	}

	private static void nebula(DrawContext ctx, int left, int top, int w, int h, double t, double intensity, int rgb) {
		int cols = 24;
		double cw = (double) w / cols;
		for (int j = 0; j < 4; j++) {
			double cy = top + h * (0.14 + 0.10 * j) + Math.sin(t * 0.3 + j * 1.3) * h * 0.04;
			double half = h * 0.13;
			int layer = mix(rgb, 0xFFFFFF, j * 0.12);
			for (int i = 0; i < cols; i++) {
				double a = (0.18 + 0.14 * Math.sin(t * 0.5 + i * 0.4 + j * 1.7)) * (0.4 + intensity);
				int x1 = left + (int) (i * cw);
				int x2 = left + (int) Math.ceil((i + 1) * cw);
				ctx.fillGradient(x1, (int) (cy - half), x2, (int) cy, layer, argb(a, layer));
				ctx.fillGradient(x1, (int) cy, x2, (int) (cy + half), argb(a, layer), layer);
			}
		}
	}

	private static void sunset(DrawContext ctx, int left, int top, int w, int h, double t, double intensity, int rgb) {
		double pulse = 0.85 + 0.15 * Math.sin(t * 0.5);
		ctx.fillGradient(left, top, left + w, top + (int) (h * 0.5), argb(0.55 * (0.4 + intensity) * pulse, rgb), rgb);
		int warm = mix(rgb, 0xFF8A3D, 0.6);
		ctx.fillGradient(left, top + (int) (h * 0.5), left + w, top + h, warm, argb(0.45 * (0.4 + intensity) * pulse, warm));
	}

	private static void storm(DrawContext ctx, int left, int top, int w, int h, double t, double intensity, int rgb) {
		ctx.fillGradient(left, top, left + w, top + (int) (h * 0.6), argb(0.6 * (0.4 + intensity), 0x050010), 0x050010);
		double rate = t * 1.2;
		long k = (long) Math.floor(rate);
		boolean flashing = Math.floorMod(k * 31 + 7, 6) == 0;
		if (flashing) {
			double f = Math.exp(-(rate - k) * 5.0);
			ctx.fill(left, top, left + w, top + h, argb(f * 0.35 * (0.4 + intensity), rgb));
		}
	}

	private static void starfield(DrawContext ctx, int left, int top, int w, int h, double t, double intensity, int rgb) {
		ctx.fillGradient(left, top, left + w, top + (int) (h * 0.5), argb(0.25 * (0.4 + intensity), rgb), rgb);
		int starColor = mix(rgb, 0xFFFFFF, 0.6);
		for (int i = 0; i < STAR_COUNT; i++) {
			double twinkle = 0.35 + 0.65 * (0.5 + 0.5 * Math.sin(t * 2.0 + STAR_PHASE[i]));
			int x = left + (int) (STAR_X[i] * w);
			int y = top + (int) (STAR_Y[i] * h);
			int size = i % 7 == 0 ? 2 : 1;
			ctx.fill(x, y, x + size, y + size, argb(twinkle * (0.5 + intensity * 0.6), starColor));
		}
	}

	/** Packs an alpha (0..1) with an RGB colour into ARGB. */
	private static int argb(double alpha, int rgb) {
		int a = (int) Math.max(0, Math.min(255, alpha * 255));
		return (a << 24) | (rgb & 0xFFFFFF);
	}

	private static int mix(int a, int b, double f) {
		int r = (int) (((a >> 16) & 255) * (1 - f) + ((b >> 16) & 255) * f);
		int g = (int) (((a >> 8) & 255) * (1 - f) + ((b >> 8) & 255) * f);
		int bl = (int) ((a & 255) * (1 - f) + (b & 255) * f);
		return (r << 16) | (g << 8) | bl;
	}
}
