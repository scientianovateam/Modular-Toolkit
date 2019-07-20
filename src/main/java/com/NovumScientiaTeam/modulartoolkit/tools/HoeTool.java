package com.NovumScientiaTeam.modulartoolkit.tools;

import com.EmosewaPixel.pixellib.miscutils.ItemUtils;
import com.NovumScientiaTeam.modulartoolkit.tools.util.ToolUtils;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.CropsBlock;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUseContext;
import net.minecraft.item.Items;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.ServerWorld;
import net.minecraft.world.World;

import java.util.List;

public class HoeTool extends ModularTool {
    public HoeTool() {
        super("modulartoolkit:hoe_tool");
        addToolTags(ToolUtils.IS_HOE);
    }

    @Override
    public double getAttackDamage(ItemStack stack) {
        return 0;
    }

    @Override
    public double getAttackSpeed(ItemStack stack) {
        if (ToolUtils.isNull(stack))
            return -3;
        return ToolUtils.getToolMaterial(stack, 0).getItemTier().getHarvestLevel() + -3;
    }

    @Override
    public ActionResultType onItemUse(ItemUseContext context) {
        ItemStack stack = context.getPlayer().getHeldItem(context.getHand());
        if (ToolUtils.isBroken(stack) || ToolUtils.isNull(stack))
            return ActionResultType.PASS;
        ActionResultType result = Items.DIAMOND_HOE.onItemUse(context);
        if (result == ActionResultType.SUCCESS) {
            ToolUtils.addXP(stack, context.getPlayer());
            return ActionResultType.SUCCESS;
        }
        BlockState state = context.getWorld().getBlockState(context.getPos());
        if (state.getBlock() instanceof CropsBlock) {
            CropsBlock crops = (CropsBlock) state.getBlock();
            World world = context.getWorld();
            BlockPos pos = context.getPos();
            if (world.getBlockState(pos.down()).canSustainPlant(world, pos.down(), Direction.UP, crops) && crops.isMaxAge(state)) {
                Item seedItem = crops.getItem(world, pos, state).getItem();
                if (world instanceof ServerWorld) {
                    List<ItemStack> drops = Block.getDrops(state, (ServerWorld) world, pos, null);
                    drops.stream().filter(s -> s.getItem() == seedItem).findFirst().ifPresent(s -> s.shrink(1));

                    drops.forEach(s -> ItemUtils.spawnItemInWorld(world, pos, s));
                }
                context.getPlayer().swingArm(context.getHand());
                world.setBlockState(pos, crops.withAge(0));
                stack.damageItem(1, context.getPlayer(), e -> {
                });
                return ActionResultType.SUCCESS;
            }
        }

        return result;
    }
}