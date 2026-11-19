package notdqrkclient.client.visual;

import java.util.concurrent.ThreadLocalRandom;

import net.minecraft.client.MinecraftClient;
import net.minecraft.particle.DustParticleEffect;
import net.minecraft.particle.ParticleEffect;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.util.math.Vec3d;

/** Particle-based wings (4 styles), halo and capes (5 styles) for the local player. Best seen in third person (F5). */
public class Cosmetics {
	// Each wing is an outline of {sideways, height, backwards} points, measured from the player's feet.
	private static final double[][][] SHAPES = {
			// Angel: soft rounded feathers
			{{0.25, 1.35, 0.20}, {0.55, 1.65, 0.28}, {1.00, 1.90, 0.34}, {1.50, 1.95, 0.40}, {1.85, 1.75, 0.45},
					{1.75, 1.45, 0.45}, {1.45, 1.20, 0.42}, {1.10, 1.00, 0.38}, {0.70, 0.90, 0.32}, {0.30, 1.00, 0.22}},
			// Demon: sharp bat wing with scalloped edge
			{{0.20, 1.30, 0.20}, {0.80, 1.95, 0.30}, {1.60, 2.35, 0.40}, {2.10, 1.70, 0.45}, {1.60, 1.55, 0.43},
					{1.50, 1.00, 0.40}, {1.15, 1.35, 0.36}, {0.90, 0.85, 0.32}, {0.60, 1.20, 0.28}, {0.30, 0.90, 0.22}},
			// Butterfly: big upper lobe, smaller lower lobe
			{{0.20, 1.30, 0.20}, {0.70, 1.95, 0.30}, {1.30, 2.05, 0.38}, {1.65, 1.65, 0.42}, {1.20, 1.30, 0.36},
					{0.50, 1.25, 0.26}, {1.00, 1.00, 0.34}, {1.25, 0.60, 0.38}, {0.80, 0.45, 0.30}, {0.30, 0.80, 0.22}},
			// Phoenix: long sweeping feathers
			{{0.20, 1.30, 0.20}, {0.90, 1.70, 0.30}, {1.70, 1.65, 0.40}, {2.50, 1.30, 0.50}, {3.10, 0.80, 0.60},
					{2.30, 0.90, 0.50}, {1.60, 0.70, 0.42}, {1.00, 0.50, 0.34}, {0.50, 0.70, 0.26}, {0.25, 1.00, 0.20}}
	};

	private static final ParticleEffect[] ACCENTS = {
			ParticleTypes.END_ROD, ParticleTypes.SOUL_FIRE_FLAME, ParticleTypes.ELECTRIC_SPARK, ParticleTypes.FLAME
	};

	public static void tick(MinecraftClient client) {
		if (client.player == null || client.world == null || client.isPaused()) {
			return;
		}
		if (!VisualConfig.wings && !VisualConfig.halo && !VisualConfig.capeOn) {
			return;
		}

		ThreadLocalRandom r = ThreadLocalRandom.current();
		double yaw = Math.toRadians(client.player.getYaw());
		double fx = -Math.sin(yaw); // forward
		double fz = Math.cos(yaw);
		double rx = -Math.cos(yaw); // right
		double rz = -Math.sin(yaw);
		double px = client.player.getX();
		double py = client.player.getY();
		double pz = client.player.getZ();

		if (VisualConfig.wings) {
			int style = Math.floorMod(VisualConfig.wingStyle, SHAPES.length);
			double[][] wing = SHAPES[style];
			ParticleEffect body = new DustParticleEffect(VisualConfig.color(VisualConfig.wingColor), 1.0f);
			ParticleEffect accent = ACCENTS[style];

			double t = System.currentTimeMillis() / 1000.0;
			double flap = 0.85 + 0.15 * Math.sin(t * 3.0 * VisualConfig.wingFlap);
			double size = VisualConfig.wingSize;

			for (int i = 0; i < 6; i++) {
				int seg = r.nextInt(wing.length - 1);
				double f = r.nextDouble();
				double l = lerp(wing[seg][0], wing[seg + 1][0], f) * size * flap;
				double h = 1.0 + (lerp(wing[seg][1], wing[seg + 1][1], f) - 1.0) * size;
				double b = lerp(wing[seg][2], wing[seg + 1][2], f);
				ParticleEffect effect = i < 4 ? body : accent;

				for (int side = -1; side <= 1; side += 2) {
					double x = px + rx * l * side - fx * b;
					double z = pz + rz * l * side - fz * b;
					client.world.addParticleClient(effect, x, py + h, z, 0, 0, 0);
				}
			}
		}

		if (VisualConfig.halo) {
			ParticleEffect effect = new DustParticleEffect(VisualConfig.color(VisualConfig.haloColor), 1.0f);
			for (int i = 0; i < 4; i++) {
				double a = r.nextDouble() * Math.PI * 2;
				client.world.addParticleClient(effect,
						px + Math.cos(a) * 0.35, py + 2.2, pz + Math.sin(a) * 0.35, 0, 0, 0);
			}
		}

		if (VisualConfig.capeOn) {
			cape(client, r, px, py, pz, fx, fz, rx, rz);
		}
	}

	/** A hanging sheet of dust particles behind the player that trails out when you move. */
	private static void cape(MinecraftClient client, ThreadLocalRandom r,
			double px, double py, double pz, double fx, double fz, double rx, double rz) {
		Vec3d vel = client.player.getVelocity();
		double speed = Math.sqrt(vel.x * vel.x + vel.z * vel.z);
		double t = System.currentTimeMillis() / 1000.0;

		int c1 = VisualConfig.color(VisualConfig.capeColor);
		int c2 = VisualConfig.color(VisualConfig.capeColor2);
		int style = VisualConfig.capeStyle;

		for (int i = 0; i < 10; i++) {
			double u = r.nextDouble() * 2 - 1; // across the cape: -1 (left edge) .. 1 (right edge)
			double v = r.nextDouble();         // down the cape: 0 (shoulders) .. 1 (hem)

			double width = 0.28 * (1 + 0.25 * v);
			double l = u * width;
			double sway = Math.min(0.7, speed * 3.0) * v + 0.04 * v * Math.sin(t * 3 + v * 4 + u * 2);
			double back = 0.17 + 0.10 * v + sway;
			double height = 1.45 - v * 0.95;

			double x = px + rx * l - fx * back;
			double z = pz + rz * l - fz * back;

			int colour;
			if (style == 1) { // Royal: contrasting trim along the edges and hem
				boolean trim = Math.abs(u) > 0.82 || v > 0.92;
				colour = trim ? c2 : c1;
			} else if (style == 2 || style == 3) { // Gradient / Flame: top colour fades to hem colour
				colour = VisualConfig.mix(c1, c2, v);
			} else if (style == 4) { // Galaxy: random blend of both colours
				colour = VisualConfig.mix(c1, c2, r.nextDouble());
			} else { // Classic
				colour = c1;
			}

			client.world.addParticleClient(new DustParticleEffect(colour, 0.9f), x, py + height, z, 0, 0, 0);

			if (style == 3 && v > 0.75 && r.nextDouble() < 0.3) {
				client.world.addParticleClient(ParticleTypes.FLAME, x, py + height, z, 0, 0.01, 0);
			}
			if (style == 4 && r.nextDouble() < 0.12) {
				client.world.addParticleClient(ParticleTypes.END_ROD, x, py + height, z, 0, 0, 0);
			}
		}
	}

	private static double lerp(double a, double b, double f) {
		return a + (b - a) * f;
	}
}
