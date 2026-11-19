package notdqrkclient.client.visual;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;

import net.fabricmc.loader.api.FabricLoader;

/**
 * Every setting of every module lives here as a plain static field.
 * Saving/loading uses reflection, so adding a new non-final static field automatically persists it.
 */
public class VisualConfig {
	// ---- Colour palette (shared by every module) ----
	public static final String[] COLOR_NAMES = {"Purple", "Pink", "Red", "Orange", "Gold", "Green", "Cyan", "Blue", "Grey", "White", "Rainbow"};
	private static final int[] COLOR_RGB = {0xB44BFF, 0xFF4BC8, 0xFF3B4B, 0xFF8A3D, 0xFFC83D, 0x4BFF9A, 0x3FE0FF, 0x4B6BFF, 0x9AA0AA, 0xFFFFFF};
	public static final int WHITE = 9;
	public static final int RAINBOW = 10;
	/** The partner colour each theme uses for trims and secondary accents (indexed by palette index). */
	private static final int[] THEME_SECONDARY = {1, 0, 4, 4, 3, 6, 7, 6, 9, 8, 10};

	// ---- Style names ----
	public static final String[] SKY_STYLES = {"Aurora", "Nebula", "Sunset", "Storm", "Starfield"};
	public static final String[] HUD_STYLES = {"Pill", "Minimal", "Bracket"};
	public static final String[] SPARKLE_STYLES = {"Stars", "Coloured", "Magic"};
	public static final String[] SNOW_STYLES = {"Snow", "Petals", "Ash"};
	public static final String[] WING_STYLES = {"Angel", "Demon", "Butterfly", "Phoenix"};
	public static final String[] CAPE_STYLES = {"Classic", "Royal", "Gradient", "Flame", "Galaxy"};

	// ---- Sky module ----
	public static boolean skyOn = true;
	public static int skyStyle = 0;
	public static int skyColor = 0;
	public static double skyIntensity = 0.6;
	public static double skySpeed = 1.0;
	public static boolean lookUpGlow = true;

	// ---- Tint module ----
	public static boolean tintOn = false;
	public static int tintColor = 0;
	public static double tintStrength = 0.25;

	// ---- HUD module ----
	public static boolean watermark = true;
	public static boolean fpsCounter = true;
	public static boolean coords = false;
	public static int hudStyle = 0;
	public static int hudColor = 0;

	// ---- Particle modules ----
	public static boolean sparkles = true;
	public static int sparkleStyle = 0;
	public static int sparkleColor = 0;
	public static double sparkleDensity = 2;

	public static boolean snowfall = true;
	public static int snowStyle = 0;
	public static double snowDensity = 4;

	public static boolean shootingStars = true;
	public static int starColor = 9;

	// ---- Filter module ----
	public static boolean grayscale = false;
	public static double grayAmount = 0.6;

	// ---- Cosmetics modules ----
	public static boolean wings = false;
	public static int wingStyle = 0;
	public static int wingColor = 9;
	public static double wingSize = 1.0;
	public static double wingFlap = 1.0;

	public static boolean halo = false;
	public static int haloColor = 4;

	public static boolean capeOn = false;
	public static int capeStyle = 0;
	public static int capeColor = 2;
	public static int capeColor2 = 4;

	/** Returns 0xRRGGBB for a palette index (Rainbow animates over time). */
	public static int color(int index) {
		int i = Math.floorMod(index, COLOR_NAMES.length);
		if (i == RAINBOW) {
			return hsv((System.currentTimeMillis() % 6000L) / 6000f);
		}
		return COLOR_RGB[i];
	}

	/** Recolours every module at once from one palette colour. */
	public static void applyTheme(int c) {
		int sec = THEME_SECONDARY[Math.floorMod(c, THEME_SECONDARY.length)];
		skyColor = c;
		tintColor = c;
		hudColor = c;
		sparkleColor = c;
		starColor = c;
		wingColor = c;
		haloColor = sec;
		capeColor = c;
		capeColor2 = sec;
	}

	/** Blends two 0xRRGGBB colours (f = 0 gives a, f = 1 gives b). */
	public static int mix(int a, int b, double f) {
		int r = (int) (((a >> 16) & 255) * (1 - f) + ((b >> 16) & 255) * f);
		int g = (int) (((a >> 8) & 255) * (1 - f) + ((b >> 8) & 255) * f);
		int bl = (int) ((a & 255) * (1 - f) + (b & 255) * f);
		return (r << 16) | (g << 8) | bl;
	}

	/** The colour shown in menus and HUD accents. */
	public static int accent() {
		return color(hudColor);
	}

	private static int hsv(float h) {
		float x = h * 6f;
		int i = (int) x;
		float f = x - i;
		float q = 1f - f;
		float r;
		float g;
		float b;
		switch (i % 6) {
			case 0 -> { r = 1; g = f; b = 0; }
			case 1 -> { r = q; g = 1; b = 0; }
			case 2 -> { r = 0; g = 1; b = f; }
			case 3 -> { r = 0; g = q; b = 1; }
			case 4 -> { r = f; g = 0; b = 1; }
			default -> { r = 1; g = 0; b = q; }
		}
		return ((int) (r * 255) << 16) | ((int) (g * 255) << 8) | (int) (b * 255);
	}

	private static Path file() {
		return FabricLoader.getInstance().getConfigDir().resolve("notdqrk-client.properties");
	}

	private static boolean isSetting(Field f) {
		int m = f.getModifiers();
		Class<?> t = f.getType();
		return Modifier.isStatic(m) && !Modifier.isFinal(m)
				&& (t == boolean.class || t == int.class || t == double.class);
	}

	public static void save() {
		Properties p = new Properties();
		for (Field f : VisualConfig.class.getDeclaredFields()) {
			if (!isSetting(f)) {
				continue;
			}
			try {
				p.setProperty(f.getName(), String.valueOf(f.get(null)));
			} catch (IllegalAccessException ignored) {
			}
		}
		try (OutputStream out = Files.newOutputStream(file())) {
			p.store(out, "notdqrk client visuals");
		} catch (IOException ignored) {
		}
	}

	public static void load() {
		Path path = file();
		if (!Files.exists(path)) {
			return;
		}
		Properties p = new Properties();
		try (InputStream in = Files.newInputStream(path)) {
			p.load(in);
		} catch (IOException e) {
			return;
		}
		for (Field f : VisualConfig.class.getDeclaredFields()) {
			if (!isSetting(f)) {
				continue;
			}
			String v = p.getProperty(f.getName());
			if (v == null) {
				continue;
			}
			try {
				if (f.getType() == boolean.class) {
					f.setBoolean(null, Boolean.parseBoolean(v));
				} else if (f.getType() == int.class) {
					f.setInt(null, Integer.parseInt(v));
				} else {
					f.setDouble(null, Double.parseDouble(v));
				}
			} catch (Exception ignored) {
			}
		}
	}
}
