package notdqrkclient.client.visual;

import java.util.concurrent.ThreadLocalRandom;

import net.minecraft.client.MinecraftClient;
import net.minecraft.particle.DustParticleEffect;
import net.minecraft.particle.ParticleEffect;
import net.minecraft.particle.ParticleTypes;

/** Sparkles, snowfall and shooting stars around the player (client-side only). */
public class ParticleEffects {
	public static void tick(MinecraftClient client) {
		if (client.player == null || client.world == null || client.isPaused()) {
			return;
		}

		ThreadLocalRandom r = ThreadLocalRandom.current();
		double px = client.player.getX();
		double py = client.player.getY();
		double pz = client.player.getZ();

		if (VisualConfig.sparkles) {
			ParticleEffect effect = sparkle();
			int count = (int) Math.round(VisualConfig.sparkleDensity);
			for (int i = 0; i < count; i++) {
				double x = px + (r.nextDouble() - 0.5) * 24;
				double y = py + r.nextDouble() * 10 - 1;
				double z = pz + (r.nextDouble() - 0.5) * 24;
				client.world.addParticleClient(effect, x, y, z,
						(r.nextDouble() - 0.5) * 0.01, -0.01, (r.nextDouble() - 0.5) * 0.01);
			}
		}

		if (VisualConfig.snowfall) {
			ParticleEffect effect = snow();
			int count = (int) Math.round(VisualConfig.snowDensity);
			for (int i = 0; i < count; i++) {
				double x = px + (r.nextDouble() - 0.5) * 30;
				double y = py + 4 + r.nextDouble() * 8;
				double z = pz + (r.nextDouble() - 0.5) * 30;
				client.world.addParticleClient(effect, x, y, z, 0.0, -0.05, 0.0);
			}
		}

		if (VisualConfig.shootingStars && r.nextDouble() < 0.04) {
			ParticleEffect effect = VisualConfig.starColor == VisualConfig.WHITE
					? ParticleTypes.END_ROD
					: new DustParticleEffect(VisualConfig.color(VisualConfig.starColor), 1.5f);
			double angle = r.nextDouble() * Math.PI * 2;
			double dist = 8 + r.nextDouble() * 14;
			double sx = px + Math.cos(angle) * dist;
			double sy = py + 16 + r.nextDouble() * 10;
			double sz = pz + Math.sin(angle) * dist;
			double dir = r.nextDouble() * Math.PI * 2;
			double vx = Math.cos(dir) * 0.45;
			double vz = Math.sin(dir) * 0.45;
			for (int i = 0; i < 8; i++) {
				client.world.addParticleClient(effect,
						sx - vx * i * 0.8, sy + 0.05 * i * 0.8, sz - vz * i * 0.8, vx, -0.06, vz);
			}
		}
	}

	private static ParticleEffect sparkle() {
		return switch (VisualConfig.sparkleStyle) {
			case 1 -> new DustParticleEffect(VisualConfig.color(VisualConfig.sparkleColor), 1.0f);
			case 2 -> ParticleTypes.ENCHANT;
			default -> ParticleTypes.END_ROD;
		};
	}

	private static ParticleEffect snow() {
		return switch (VisualConfig.snowStyle) {
			case 1 -> ParticleTypes.CHERRY_LEAVES;
			case 2 -> ParticleTypes.ASH;
			default -> ParticleTypes.SNOWFLAKE;
		};
	}
}
