package slippybanana333.me.alchemist_origin;

import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroups;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;

/**
 * Registers the three craftable upgrade items.
 *
 * UPGRADE_1  — "Effect Upgrade I"   — used in /alc GUI to unlock a new slot (Level I).
 * UPGRADE_2  — "Effect Upgrade II"  — used in /alc GUI to upgrade a slot from Level I → II.
 * UPGRADE_3  — "Effect Upgrade III" — used in /alc GUI to upgrade a slot from Level II → III.
 */
public class AlchemistItems {

    public static final Item UPGRADE_1 = new Item(new Item.Settings().maxCount(16));
    public static final Item UPGRADE_2 = new Item(new Item.Settings().maxCount(16));
    public static final Item UPGRADE_3 = new Item(new Item.Settings().maxCount(16));

    public static void register() {
        Registry.register(Registries.ITEM, new Identifier("alchemist_origin", "effect_upgrade_1"), UPGRADE_1);
        Registry.register(Registries.ITEM, new Identifier("alchemist_origin", "effect_upgrade_2"), UPGRADE_2);
        Registry.register(Registries.ITEM, new Identifier("alchemist_origin", "effect_upgrade_3"), UPGRADE_3);

        ItemGroupEvents.modifyEntriesEvent(ItemGroups.TOOLS).register(entries -> {
            entries.add(UPGRADE_1);
            entries.add(UPGRADE_2);
            entries.add(UPGRADE_3);
        });
    }
}
