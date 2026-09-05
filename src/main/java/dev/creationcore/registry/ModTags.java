package dev.creationcore.registry;

import dev.creationcore.CreativeCoreMod;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;

/**
 * Public compatibility tags used by Creation Core.
 *
 * Other mods/data packs may add their own compatible container items to
 * #creationcore:creative_core_containers without a hard dependency on this mod's code.
 */
public final class ModTags {
    public static final TagKey<Item> CREATIVE_CORE_CONTAINERS = TagKey.create(
            Registries.ITEM,
            ResourceLocation.fromNamespaceAndPath(CreativeCoreMod.MODID, "creative_core_containers")
    );

    /** Blocks which Mine Craft deliberately treats as pickaxe-efficient. */
    public static final TagKey<Block> MINE_CRAFT_PICKAXE_BONUS = TagKey.create(
            Registries.BLOCK,
            ResourceLocation.fromNamespaceAndPath(CreativeCoreMod.MODID, "mine_craft_pickaxe_bonus")
    );

    private ModTags() {}
}
