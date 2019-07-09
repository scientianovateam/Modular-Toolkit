package com.NovumScientiaTeam.modulartoolkit.tools.util;

import com.EmosewaPixel.pixellib.materialSystem.lists.Materials;
import com.EmosewaPixel.pixellib.materialSystem.materials.Material;
import com.NovumScientiaTeam.modulartoolkit.partTypes.Head;
import com.NovumScientiaTeam.modulartoolkit.partTypes.PartType;
import com.NovumScientiaTeam.modulartoolkit.tools.ModularTool;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.ImmutableList;
import javafx.util.Pair;
import net.minecraft.item.ItemStack;
import net.minecraftforge.common.ToolType;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class ToolUtils {
    //Tool Tags
    public static final String IS_TOOL = "is_tool";
    public static final String IS_MELEE_WEAPON = "is_melee_weapon";
    public static final String IS_RANGED_WEAPON = "is_ranged_weapon";
    public static final String IS_PROJECTILE = "is_projectile";
    public static final String IS_ARMOR = "is_armor";
    public static final String IS_HOE = "is_hoe";


    //NBT Methods
    public static boolean isNull(ItemStack stack) {
        return !(stack.getOrCreateTag().contains("Materials") && stack.getOrCreateTag().contains("Modifiers") && stack.getOrCreateTag().contains("XP") && stack.getOrCreateTag().contains("Boosts"));
    }

    public static boolean isBroken(ItemStack stack) {
        if (stack.getOrCreateTag().contains("isBroken"))
            return stack.getTag().getBoolean("isBroken");
        return false;
    }

    public static void makeBroken(ItemStack stack) {
        stack.getOrCreateTag().putBoolean("isBroken", true);
    }

    public static void unbreak(ItemStack stack) {
        stack.getOrCreateTag().putBoolean("isBroken", false);
    }

    public static Material getToolMaterial(ItemStack stack, int index) {
        if (isNull(stack))
            return null;
        return Materials.get(stack.getTag().getCompound("Materials").getString("material" + index));
    }

    public static List<Material> getAllToolMaterials(ItemStack stack) {
        List<Material> list = new ArrayList<>();
        if (!isNull(stack))
            for (int i = 0; stack.getTag().getCompound("Materials").contains("material" + i); i++)
                list.add(getToolMaterial(stack, i));

        return list;
    }

    public static int getUsedModifierSlotCount(ItemStack stack) {
        if (isNull(stack))
            return 0;
        return stack.getTag().getInt("ModifierSlotsUsed");
    }

    public static void setUsedModifierSlotCount(ItemStack stack, int amount) {
        if (isNull(stack))
            return;
        stack.getTag().putInt("ModifierSlots", amount);
    }

    public static void freeModifierSlots(ItemStack stack, int amount) {
        setUsedModifierSlotCount(stack, getUsedModifierSlotCount(stack) - amount);
    }

    public static void freeModifierSlot(ItemStack stack) {
        freeModifierSlots(stack, 1);
    }

    public static void useModifierSlots(ItemStack stack, int amount) {
        setUsedModifierSlotCount(stack, getUsedModifierSlotCount(stack) + amount);
    }

    public static void useModifierSlot(ItemStack stack) {
        useModifierSlots(stack, 1);
    }

    public static int getFreeModifierSlotCount(ItemStack stack) {
        return getLevel(stack) - getUsedModifierSlotCount(stack);
    }

    public static long getXP(ItemStack stack) {
        if (isNull(stack))
            return 0;
        return stack.getOrCreateTag().getLong("XP");
    }

    public static void setXP(ItemStack stack, long amount) {
        stack.getOrCreateTag().putLong("XP", amount);
    }

    public static void addXP(ItemStack stack, long amount) {
        if (getLevel(stack) != getLevelCap(stack))
            setXP(stack, getXP(stack) + amount);

        if (getXP(stack) >= getXPForLevelUp(stack) && getLevel(stack) + 1 <= getLevelCap(stack)) {
            levelUp(stack);
        }
    }

    public static void addXP(ItemStack stack) {
        addXP(stack, 1);
    }

    public static int getLevel(ItemStack stack) {
        if (isNull(stack))
            return 0;
        return stack.getOrCreateTag().getInt("Level");
    }

    public static void setLevel(ItemStack stack, int amount) {
        stack.getOrCreateTag().putInt("Level", amount);
    }

    public static void levelUp(ItemStack stack, int times) {
        setLevel(stack, getLevel(stack) + times);
    }

    public static void levelUp(ItemStack stack) {
        levelUp(stack, 1);
    }

    public static long getXPForLevel(ItemStack stack, int level) {
        if (level < 1)
            return 0;
        return 25 * (long) Math.pow(3, level - 1);
    }

    public static long getXPForCurentLevel(ItemStack stack) {
        return getXPForLevel(stack, getLevel(stack));
    }

    public static long getXPForLevelUp(ItemStack stack) {
        return getXPForLevel(stack, getLevel(stack) + 1);
    }

    public static int getLevelCap(ItemStack stack) {
        ImmutableList<PartType> partList = ((ModularTool) stack.getItem()).getPartList();
        return (int) IntStream.range(0, partList.size())
                .collect(HashMultimap::create, (map, i) -> map.put(partList.get(i).getName(), partList.get(i).getLevelCapMultiplier(getToolMaterial(stack, i))), (m1, m2) -> m1.putAll(m2))
                .asMap().values().stream()
                .mapToDouble(l -> l.stream().mapToDouble(d -> (double) d).average().getAsDouble()).reduce(1, (d1, d2) -> d1 * d2);
    }

    public static List<Material> getHeadMaterials(ItemStack stack) {
        ImmutableList<PartType> partList = ((ModularTool) stack.getItem()).getPartList();
        return IntStream.range(0, partList.size()).filter(i -> partList.get(i) instanceof Head).mapToObj(i -> getToolMaterial(stack, i)).collect(Collectors.toList());
    }

    public static Map<ToolType, Integer> getHarvestMap(ItemStack stack) {
        ImmutableList<PartType> partList = ((ModularTool) stack.getItem()).getPartList();
        return IntStream.range(0, partList.size()).filter(i -> partList.get(i) instanceof Head).filter(i -> ((Head) partList.get(i)).getToolType().isPresent()).mapToObj(i -> new Pair<>(((Head) partList.get(i)).getToolType().get(), getToolMaterial(stack, i).getItemTier().getHarvestLevel())).collect(Collectors.toMap(Pair::getKey, Pair::getValue, (i1, i2) -> i1 > i2 ? i1 : i2));
    }

    public static float getDestroySpeedForToolType(ItemStack stack, ToolType type) {
        ImmutableList<PartType> partList = ((ModularTool) stack.getItem()).getPartList();
        return (float) IntStream.range(0, partList.size()).filter(i -> partList.get(i) instanceof Head).filter(i -> ((Head) partList.get(i)).getToolType().get() == type).mapToDouble(i -> getToolMaterial(stack, i).getItemTier().getEfficiency()).max().orElse(1);
    }
}