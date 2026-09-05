package dev.creationcore.item;

import dev.creationcore.registry.ModTags;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.Tiers;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.EmptyBlockGetter;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.common.ItemAbilities;
import net.neoforged.neoforge.common.ItemAbility;

/**
 * Creation Core's end-game universal tool.
 *
 * <p>The item deliberately has no durability component. Mining speed is the best speed of the
 * vanilla Netherite pickaxe/axe/shovel/hoe or shears for the target state. Special unbreakable
 * block progress is handled by {@code BlockStateBaseMixin}. Right-click tool actions are limited
 * to the axe and shovel suites.</p>
 */
public final class MineCraftItem extends Item {
    private static final float NETHERITE_SPEED = Tiers.NETHERITE.getSpeed();

    private static final ItemStack NETHERITE_PICKAXE = Items.NETHERITE_PICKAXE.getDefaultInstance();
    private static final ItemStack NETHERITE_AXE = Items.NETHERITE_AXE.getDefaultInstance();
    private static final ItemStack NETHERITE_SHOVEL = Items.NETHERITE_SHOVEL.getDefaultInstance();
    private static final ItemStack NETHERITE_HOE = Items.NETHERITE_HOE.getDefaultInstance();
    private static final ItemStack SHEARS = Items.SHEARS.getDefaultInstance();

    public MineCraftItem(Properties properties) {
        super(properties);
    }

    @Override
    public float getDestroySpeed(ItemStack stack, BlockState state) {
        float speed = 1.0F;
        speed = Math.max(speed, NETHERITE_PICKAXE.getDestroySpeed(state));
        speed = Math.max(speed, NETHERITE_AXE.getDestroySpeed(state));
        speed = Math.max(speed, NETHERITE_SHOVEL.getDestroySpeed(state));
        speed = Math.max(speed, NETHERITE_HOE.getDestroySpeed(state));
        speed = Math.max(speed, SHEARS.getDestroySpeed(state));

        // Explicitly make glass-family lighting blocks behave like pickaxe-efficient blocks.
        if (state.is(ModTags.MINE_CRAFT_PICKAXE_BONUS)) {
            speed = Math.max(speed, NETHERITE_SPEED);
        }

        // getDestroySpeed has no level/pos parameters. For vanilla indestructible states the
        // destroy-speed field is position-independent, so EmptyBlockGetter is sufficient to
        // ensure Player#getDestroySpeed starts with Netherite speed before Efficiency is added.
        if (state.getDestroySpeed(EmptyBlockGetter.INSTANCE, BlockPos.ZERO) < 0.0F) {
            speed = Math.max(speed, NETHERITE_SPEED);
        }
        return speed;
    }

    @Override
    public boolean isCorrectToolForDrops(ItemStack stack, BlockState state) {
        return true;
    }

    @Override
    public boolean canPerformAction(ItemStack stack, ItemAbility ability) {
        // Dig abilities preserve the original universal mining identity. Only the axe/shovel
        // modification abilities below are exposed for right-click block interaction.
        return ability == ItemAbilities.PICKAXE_DIG
                || ability == ItemAbilities.AXE_DIG
                || ability == ItemAbilities.SHOVEL_DIG
                || ability == ItemAbilities.HOE_DIG
                || ability == ItemAbilities.SHEARS_DIG
                || ability == ItemAbilities.AXE_STRIP
                || ability == ItemAbilities.AXE_SCRAPE
                || ability == ItemAbilities.AXE_WAX_OFF
                || ability == ItemAbilities.SHOVEL_FLATTEN
                || ability == ItemAbilities.SWORD_SWEEP;
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        // Delegate only axe and shovel block modifications to vanilla tools so sounds, particles,
        // game events and NeoForge getToolModifiedState hooks remain intact.
        InteractionResult result = Items.NETHERITE_AXE.useOn(context);
        if (result.consumesAction()) return result;

        result = Items.NETHERITE_SHOVEL.useOn(context);
        if (result.consumesAction()) return result;

        return InteractionResult.PASS;
    }

    @Override
    public int getEnchantmentValue() {
        return Tiers.NETHERITE.getEnchantmentValue();
    }

    @Override
    public int getEnchantmentValue(ItemStack stack) {
        return Tiers.NETHERITE.getEnchantmentValue();
    }

    @Override
    public boolean isEnchantable(ItemStack stack) {
        // Vanilla normally ties enchantability to damageability. Mine Craft intentionally has no
        // durability, but still needs to work in the enchanting table.
        return true;
    }

    @Override
    public boolean supportsEnchantment(ItemStack stack, Holder<Enchantment> enchantment) {
        if (isBlockedEnchantment(enchantment)) return false;
        return super.supportsEnchantment(stack, enchantment);
    }

    @Override
    public boolean isPrimaryItemFor(ItemStack stack, Holder<Enchantment> enchantment) {
        if (isBlockedEnchantment(enchantment)) return false;
        return super.isPrimaryItemFor(stack, enchantment);
    }

    @Override
    public int getEnchantmentLevel(ItemStack stack, Holder<Enchantment> enchantment) {
        // Commands/data-component editors and other mods can still force normally incompatible
        // enchantments onto the stack. Make all four intentionally unsupported enchantments
        // gameplay-inert even in that case.
        if (isBlockedEnchantment(enchantment)) return 0;
        return super.getEnchantmentLevel(stack, enchantment);
    }

    private static boolean isBlockedEnchantment(Holder<Enchantment> enchantment) {
        return enchantment.is(Enchantments.UNBREAKING)
                || enchantment.is(Enchantments.MENDING)
                || enchantment.is(Enchantments.SILK_TOUCH)
                || enchantment.is(Enchantments.FORTUNE);
    }
}
