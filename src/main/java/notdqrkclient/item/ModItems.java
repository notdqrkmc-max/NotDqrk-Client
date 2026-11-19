package notdqrkclient.item;

import java.util.function.Function;

import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroups;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import notdqrkclient.Notdqrk_client;

public class ModItems {
	public static final Item RUBY = register("ruby", Item::new, new Item.Settings());

	private static Item register(String name, Function<Item.Settings, Item> factory, Item.Settings settings) {
		// Since 1.21.2 every item's settings must carry its registry key.
		RegistryKey<Item> key = RegistryKey.of(RegistryKeys.ITEM, Notdqrk_client.id(name));
		return Registry.register(Registries.ITEM, key, factory.apply(settings.registryKey(key)));
	}

	public static void initialize() {
		ItemGroupEvents.modifyEntriesEvent(ItemGroups.INGREDIENTS).register(entries -> entries.add(RUBY));
	}
}
