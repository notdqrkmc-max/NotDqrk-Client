package notdqrkclient.client.visual;

import java.util.Random;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;
import java.util.function.DoubleConsumer;
import java.util.function.IntConsumer;
import java.util.function.IntSupplier;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.SliderWidget;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

/**
 * Tabbed menu. Each tab is made of "module rows": [ON/OFF] [Style] [Colour], plus sliders.
 * Adding a module = adding one moduleRow(...) call. The Themes tab recolours everything at once.
 */
public class VisualsScreen extends Screen {
	private enum Tab {
		SKY("Sky"), UI("UI"), EFFECTS("Effects"), COSMETICS("Cosmetics"), THEMES("Themes");

		final String label;

		Tab(String label) {
			this.label = label;
		}
	}

	private static final int PANEL_W = 380;
	private static final int PANEL_H = 262;
	private static final int CONTENT_W = 340;
	private static final int GAP = 4;
	private static final int TOGGLE_W = 100;
	private static final int TAB_W = 68;
	private static final int TAB_GAP = 6;
	private static final int OPEN_MS = 400;

	private static final int DOTS = 48;
	private static final double[] DOT_X = new double[DOTS];
	private static final double[] DOT_Y = new double[DOTS];
	private static final double[] DOT_SPEED = new double[DOTS];
	private static final double[] DOT_PHASE = new double[DOTS];

	static {
		Random r = new Random(99);
		for (int i = 0; i < DOTS; i++) {
			DOT_X[i] = r.nextDouble();
			DOT_Y[i] = r.nextDouble();
			DOT_SPEED[i] = 0.4 + r.nextDouble();
			DOT_PHASE[i] = r.nextDouble() * Math.PI * 2;
		}
	}

	private static Tab currentTab = Tab.SKY; // remembered between openings

	private final long openedAt = System.currentTimeMillis();
	private String themeMessage = "";
	private long themeMessageUntil = 0;

	private int left;
	private int top;

	public VisualsScreen() {
		super(Text.literal("NOTDQRK CLIENT"));
	}

	@Override
	protected void init() {
		left = (this.width - PANEL_W) / 2;
		top = (this.height - PANEL_H) / 2;

		// Tab buttons
		int x0 = left + (PANEL_W - (Tab.values().length * TAB_W + (Tab.values().length - 1) * TAB_GAP)) / 2;
		Tab[] tabs = Tab.values();
		for (int i = 0; i < tabs.length; i++) {
			Tab tab = tabs[i];
			boolean selected = tab == currentTab;
			Text label = Text.literal(tab.label).formatted(selected ? Formatting.BOLD : Formatting.GRAY,
					selected ? Formatting.LIGHT_PURPLE : Formatting.GRAY);
			addDrawableChild(ButtonWidget.builder(label, b -> {
				currentTab = tab;
				this.clearAndInit();
			}).dimensions(x0 + i * (TAB_W + TAB_GAP), top + 38, TAB_W, 20).build());
		}

		int x = left + (PANEL_W - CONTENT_W) / 2;
		int y = top + 68;
		int row = 24;
		int half = (CONTENT_W - GAP) / 2;

		switch (currentTab) {
			case SKY -> {
				moduleRow(x, y, "Sky", () -> VisualConfig.skyOn, v -> VisualConfig.skyOn = v,
						"Style", VisualConfig.SKY_STYLES, () -> VisualConfig.skyStyle, v -> VisualConfig.skyStyle = v,
						() -> VisualConfig.skyColor, v -> VisualConfig.skyColor = v);
				slider("Intensity", x, y + row, CONTENT_W, 0, 1, VisualConfig.skyIntensity, false, v -> VisualConfig.skyIntensity = v);
				slider("Speed", x, y + row * 2, CONTENT_W, 0.2, 3, VisualConfig.skySpeed, false, v -> VisualConfig.skySpeed = v);
				toggle("Look-Up Glow", x, y + row * 3, CONTENT_W, () -> VisualConfig.lookUpGlow, v -> VisualConfig.lookUpGlow = v);
				moduleRow(x, y + row * 4, "Tint", () -> VisualConfig.tintOn, v -> VisualConfig.tintOn = v,
						null, null, null, null,
						() -> VisualConfig.tintColor, v -> VisualConfig.tintColor = v);
				slider("Tint Strength", x, y + row * 5, CONTENT_W, 0, 1, VisualConfig.tintStrength, false, v -> VisualConfig.tintStrength = v);
			}
			case UI -> {
				int third = (CONTENT_W - GAP * 2) / 3;
				toggle("Watermark", x, y, third, () -> VisualConfig.watermark, v -> VisualConfig.watermark = v);
				toggle("FPS", x + third + GAP, y, third, () -> VisualConfig.fpsCounter, v -> VisualConfig.fpsCounter = v);
				toggle("Coords", x + (third + GAP) * 2, y, third, () -> VisualConfig.coords, v -> VisualConfig.coords = v);
				cycle("Style", x, y + row, half, VisualConfig.HUD_STYLES, () -> VisualConfig.hudStyle, v -> VisualConfig.hudStyle = v);
				colourButton(x + half + GAP, y + row, half, "", () -> VisualConfig.hudColor, v -> VisualConfig.hudColor = v);
			}
			case EFFECTS -> {
				moduleRow(x, y, "Sparkles", () -> VisualConfig.sparkles, v -> VisualConfig.sparkles = v,
						"Style", VisualConfig.SPARKLE_STYLES, () -> VisualConfig.sparkleStyle, v -> VisualConfig.sparkleStyle = v,
						() -> VisualConfig.sparkleColor, v -> VisualConfig.sparkleColor = v);
				slider("Sparkle Amount", x, y + row, CONTENT_W, 1, 8, VisualConfig.sparkleDensity, true, v -> VisualConfig.sparkleDensity = v);
				moduleRow(x, y + row * 2, "Snow", () -> VisualConfig.snowfall, v -> VisualConfig.snowfall = v,
						"Style", VisualConfig.SNOW_STYLES, () -> VisualConfig.snowStyle, v -> VisualConfig.snowStyle = v,
						null, null);
				slider("Snow Amount", x, y + row * 3, CONTENT_W, 1, 12, VisualConfig.snowDensity, true, v -> VisualConfig.snowDensity = v);
				moduleRow(x, y + row * 4, "Stars", () -> VisualConfig.shootingStars, v -> VisualConfig.shootingStars = v,
						null, null, null, null,
						() -> VisualConfig.starColor, v -> VisualConfig.starColor = v);
				toggle("Grayscale", x, y + row * 5, TOGGLE_W + 8, () -> VisualConfig.grayscale, v -> VisualConfig.grayscale = v);
				slider("Amount", x + TOGGLE_W + 8 + GAP, y + row * 5, CONTENT_W - TOGGLE_W - 8 - GAP, 0, 1, VisualConfig.grayAmount, false, v -> VisualConfig.grayAmount = v);
			}
			case COSMETICS -> {
				moduleRow(x, y, "Wings", () -> VisualConfig.wings, v -> VisualConfig.wings = v,
						"Style", VisualConfig.WING_STYLES, () -> VisualConfig.wingStyle, v -> VisualConfig.wingStyle = v,
						() -> VisualConfig.wingColor, v -> VisualConfig.wingColor = v);
				slider("Wing Size", x, y + row, half, 0.6, 1.6, VisualConfig.wingSize, false, v -> VisualConfig.wingSize = v);
				slider("Flap Speed", x + half + GAP, y + row, half, 0.3, 3, VisualConfig.wingFlap, false, v -> VisualConfig.wingFlap = v);
				moduleRow(x, y + row * 2, "Halo", () -> VisualConfig.halo, v -> VisualConfig.halo = v,
						null, null, null, null,
						() -> VisualConfig.haloColor, v -> VisualConfig.haloColor = v);
				moduleRow(x, y + row * 3, "Cape", () -> VisualConfig.capeOn, v -> VisualConfig.capeOn = v,
						"Style", VisualConfig.CAPE_STYLES, () -> VisualConfig.capeStyle, v -> VisualConfig.capeStyle = v,
						() -> VisualConfig.capeColor, v -> VisualConfig.capeColor = v);
				colourButton(x, y + row * 4, CONTENT_W, "Cape Trim / 2nd Colour: ", () -> VisualConfig.capeColor2, v -> VisualConfig.capeColor2 = v);
			}
			case THEMES -> {
				int cols = 5;
				int bw = (CONTENT_W - GAP * (cols - 1)) / cols;
				int[] themes = {0, 1, 2, 3, 4, 5, 6, 7, 8, VisualConfig.RAINBOW};
				for (int i = 0; i < themes.length; i++) {
					int pal = themes[i];
					int bx = x + (i % cols) * (bw + GAP);
					int by = y + 22 + (i / cols) * row;
					addDrawableChild(ButtonWidget.builder(colourLabel("", pal), b -> {
						VisualConfig.applyTheme(pal);
						themeMessage = "Applied theme: " + VisualConfig.COLOR_NAMES[pal];
						themeMessageUntil = System.currentTimeMillis() + 2500;
					}).dimensions(bx, by, bw, 20).build());
				}
			}
		}

		addDrawableChild(ButtonWidget.builder(Text.literal("Done"), b -> this.close())
				.dimensions(left + PANEL_W / 2 - 40, top + PANEL_H - 28, 80, 20).build());
	}

	// ---- Module row: [ON/OFF] [Style] [Colour] (style and colour are optional) ----

	private void moduleRow(int x, int y, String name, BooleanSupplier on, Consumer<Boolean> setOn,
			String styleName, String[] styles, IntSupplier styleGet, IntConsumer styleSet,
			IntSupplier colourGet, IntConsumer colourSet) {
		int parts = 1 + (styles != null ? 1 : 0) + (colourGet != null ? 1 : 0);
		int toggleW = parts == 1 ? CONTENT_W : TOGGLE_W;
		toggle(name, x, y, toggleW, on, setOn);
		if (parts == 1) {
			return;
		}
		int otherW = (CONTENT_W - TOGGLE_W - GAP * (parts - 1)) / (parts - 1);
		int cx = x + TOGGLE_W + GAP;
		if (styles != null) {
			cycle(styleName, cx, y, otherW, styles, styleGet, styleSet);
			cx += otherW + GAP;
		}
		if (colourGet != null) {
			colourButton(cx, y, otherW, "", colourGet, colourSet);
		}
	}

	private void toggle(String name, int x, int y, int w, BooleanSupplier getter, Consumer<Boolean> setter) {
		addDrawableChild(ButtonWidget.builder(toggleLabel(name, getter.getAsBoolean()), button -> {
			boolean next = !getter.getAsBoolean();
			setter.accept(next);
			button.setMessage(toggleLabel(name, next));
		}).dimensions(x, y, w, 20).build());
	}

	private static Text toggleLabel(String name, boolean on) {
		return Text.literal(name + ": ").append(Text.literal(on ? "ON" : "OFF")
				.formatted(on ? Formatting.GREEN : Formatting.RED));
	}

	private void cycle(String name, int x, int y, int w, String[] options, IntSupplier getter, IntConsumer setter) {
		addDrawableChild(ButtonWidget.builder(cycleLabel(name, options, getter.getAsInt()), button -> {
			int next = (getter.getAsInt() + 1) % options.length;
			setter.accept(next);
			button.setMessage(cycleLabel(name, options, next));
		}).dimensions(x, y, w, 20).build());
	}

	private static Text cycleLabel(String name, String[] options, int index) {
		return Text.literal(name + ": " + options[Math.floorMod(index, options.length)]);
	}

	private void colourButton(int x, int y, int w, String prefix, IntSupplier getter, IntConsumer setter) {
		addDrawableChild(ButtonWidget.builder(colourLabel(prefix, getter.getAsInt()), button -> {
			int next = (getter.getAsInt() + 1) % VisualConfig.COLOR_NAMES.length;
			setter.accept(next);
			button.setMessage(colourLabel(prefix, next));
		}).dimensions(x, y, w, 20).build());
	}

	private static Text colourLabel(String prefix, int index) {
		int i = Math.floorMod(index, VisualConfig.COLOR_NAMES.length);
		int rgb = i == VisualConfig.RAINBOW ? 0xFFFFFF : VisualConfig.color(i);
		return Text.literal(prefix + VisualConfig.COLOR_NAMES[i]).styled(s -> s.withColor(rgb));
	}

	private void slider(String name, int x, int y, int w, double min, double max, double current,
			boolean integer, DoubleConsumer setter) {
		addDrawableChild(new ConfigSlider(x, y, w, 20, name, min, max, current, integer, setter));
	}

	// ---- Drawing ----

	@Override
	public void renderBackground(DrawContext context, int mouseX, int mouseY, float delta) {
		super.renderBackground(context, mouseX, mouseY, delta);

		double t = System.currentTimeMillis() / 1000.0;
		int rgb = VisualConfig.accent();
		int accent = 0xFF000000 | rgb;

		// Full-screen backdrop: your chosen sky, plus floating motes
		SkyOverlay.draw(context, 0, 0, this.width, this.height, VisualConfig.skyStyle, 0.45, 1.0,
				VisualConfig.color(VisualConfig.skyColor));
		for (int i = 0; i < DOTS; i++) {
			double y = (((DOT_Y[i] - t * 0.03 * DOT_SPEED[i]) % 1.0) + 1.0) % 1.0;
			int dx = (int) (DOT_X[i] * this.width);
			int dy = (int) (y * this.height);
			int a = (int) ((0.25 + 0.35 * Math.sin(t * 1.5 + DOT_PHASE[i])) * 255);
			context.fill(dx, dy, dx + 2, dy + 2, (Math.max(0, a) << 24) | rgb);
		}

		// Pulsing glow around the panel
		double pulse = 0.5 + 0.5 * Math.sin(t * 2.0);
		for (int i = 3; i >= 1; i--) {
			int a = (int) ((0.10 + 0.12 * pulse) * 255 / i);
			context.fill(left - 1 - i * 2, top - 1 - i * 2, left + PANEL_W + 1 + i * 2, top + PANEL_H + 1 + i * 2, (a << 24) | rgb);
		}

		context.fill(left - 1, top - 1, left + PANEL_W + 1, top + PANEL_H + 1, accent);
		context.fill(left, top, left + PANEL_W, top + PANEL_H, 0xF0100818);

		// Live preview of the selected sky in the header
		SkyOverlay.draw(context, left, top, PANEL_W, 72, VisualConfig.skyStyle, 0.8, 1.0,
				VisualConfig.color(VisualConfig.skyColor));

		int tabsWidth = Tab.values().length * TAB_W + (Tab.values().length - 1) * TAB_GAP;
		int ux = left + (PANEL_W - tabsWidth) / 2 + currentTab.ordinal() * (TAB_W + TAB_GAP);
		context.fill(ux, top + 59, ux + TAB_W, top + 61, accent);
	}

	@Override
	public void render(DrawContext context, int mouseX, int mouseY, float delta) {
		super.render(context, mouseX, mouseY, delta);

		double t = System.currentTimeMillis() / 1000.0;
		int rgb = VisualConfig.accent();

		// Title with a shimmer that travels along the letters
		String title = this.title.getString();
		int totalW = this.textRenderer.getWidth(title);
		int tx = this.width / 2 - totalW / 2;
		for (int i = 0; i < title.length(); i++) {
			String ch = String.valueOf(title.charAt(i));
			double shimmer = 0.5 + 0.5 * Math.sin(t * 3.0 - i * 0.5);
			int col = 0xFF000000 | VisualConfig.mix(rgb, 0xFFFFFF, shimmer * 0.8);
			context.drawTextWithShadow(this.textRenderer, ch, tx, top + 10, col);
			tx += this.textRenderer.getWidth(ch);
		}
		context.drawCenteredTextWithShadow(this.textRenderer, Text.literal("visuals for 1.21.11"),
				this.width / 2, top + 23, 0xFFAAAAAA);

		if (currentTab == Tab.THEMES) {
			int ty = top + 68 + 4;
			context.drawCenteredTextWithShadow(this.textRenderer,
					Text.literal("Pick a theme to recolour every module at once"), this.width / 2, ty, 0xFFDDDDDD);
			context.drawCenteredTextWithShadow(this.textRenderer,
					Text.literal("You can still tweak each module's colour in its own tab"),
					this.width / 2, top + 68 + 22 + 24 * 2 + 8, 0xFF999999);
			if (System.currentTimeMillis() < themeMessageUntil) {
				context.drawCenteredTextWithShadow(this.textRenderer, Text.literal(themeMessage),
						this.width / 2, top + 68 + 22 + 24 * 2 + 28, 0xFF000000 | rgb);
			}
		}

		// Opening animation: the whole panel fades in from black
		long age = System.currentTimeMillis() - openedAt;
		if (age < OPEN_MS) {
			double p = age / (double) OPEN_MS;
			p = 1 - (1 - p) * (1 - p);
			int a = (int) ((1 - p) * 255);
			context.fill(left - 8, top - 8, left + PANEL_W + 8, top + PANEL_H + 8, (a << 24) | 0x0A0510);
		}
	}

	@Override
	public void removed() {
		VisualConfig.save();
	}

	@Override
	public boolean shouldPause() {
		return false;
	}

	/** A slider that writes its value back through a setter. */
	private static class ConfigSlider extends SliderWidget {
		private final String name;
		private final double min;
		private final double max;
		private final boolean integer;
		private final DoubleConsumer setter;

		ConfigSlider(int x, int y, int w, int h, String name, double min, double max, double current,
				boolean integer, DoubleConsumer setter) {
			super(x, y, w, h, Text.empty(), Math.max(0, Math.min(1, (current - min) / (max - min))));
			this.name = name;
			this.min = min;
			this.max = max;
			this.integer = integer;
			this.setter = setter;
			updateMessage();
		}

		private double actual() {
			double v = min + (max - min) * this.value;
			return integer ? Math.round(v) : v;
		}

		@Override
		protected void updateMessage() {
			double v = actual();
			setMessage(Text.literal(name + ": " + (integer ? String.valueOf((int) v) : String.format("%.2f", v))));
		}

		@Override
		protected void applyValue() {
			setter.accept(actual());
		}
	}
}
