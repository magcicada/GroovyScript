package com.cleanroommc.groovyscript.compat.mods.extendedcrafting;

import com.blakebr0.extendedcrafting.config.ModConfig;
import com.blakebr0.extendedcrafting.crafting.endercrafter.EnderCrafterRecipeManager;
import com.cleanroommc.groovyscript.api.IIngredient;
import com.cleanroommc.groovyscript.api.documentation.annotations.Example;
import com.cleanroommc.groovyscript.api.documentation.annotations.MethodDescription;
import com.cleanroommc.groovyscript.api.documentation.annotations.RecipeBuilderDescription;
import com.cleanroommc.groovyscript.api.documentation.annotations.RegistryDescription;
import com.cleanroommc.groovyscript.helper.SimpleObjectStream;
import com.cleanroommc.groovyscript.registry.VirtualizedRegistry;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;

import java.util.List;

@RegistryDescription
public class EnderCrafting extends VirtualizedRegistry<IRecipe> {

    public EnderCrafting() {
        super();
    }

    @RecipeBuilderDescription(example = {
            @Example(".output(item('minecraft:stone')).matrix('BXX', 'X B').key('B', item('minecraft:stone')).key('X', item('minecraft:gold_ingot')).time(1).mirrored()"),
            @Example(".output(item('minecraft:diamond') * 32).matrix([[item('minecraft:gold_ingot'), item('minecraft:gold_ingot'), item('minecraft:gold_ingot')],[item('minecraft:gold_ingot'), item('minecraft:gold_ingot'), item('minecraft:gold_ingot')],[item('minecraft:gold_ingot'), item('minecraft:gold_ingot'), item('minecraft:gold_ingot')]]).time(1)")
    })
    public EnderRecipeBuilder.Shaped shapedBuilder() {
        return new EnderRecipeBuilder.Shaped();
    }

    @RecipeBuilderDescription(example = {
            @Example(".output(item('minecraft:clay') * 8).input(item('minecraft:stone'), item('minecraft:stone'), item('minecraft:stone'))"),
            @Example(".output(item('minecraft:clay') * 32).input(item('minecraft:diamond'), item('minecraft:diamond'), item('minecraft:diamond'), item('minecraft:diamond'), item('minecraft:diamond'), item('minecraft:diamond'), item('minecraft:diamond'), item('minecraft:diamond')).time(1)")
    })
    public EnderRecipeBuilder.Shapeless shapelessBuilder() {
        return new EnderRecipeBuilder.Shapeless();
    }

    @Override
    public void onReload() {
        removeScripted().forEach(recipe -> EnderCrafterRecipeManager.getInstance().getRecipes().removeIf(r -> r == recipe));
        EnderCrafterRecipeManager.getInstance().getRecipes().addAll(restoreFromBackup());
    }

    @MethodDescription(description = "groovyscript.wiki.extendedcrafting.endercrafting.addShaped0", type = MethodDescription.Type.ADDITION)
    public IRecipe addShaped(ItemStack output, List<List<IIngredient>> input) {
        return addShaped(ModConfig.confEnderTimeRequired, output, input);
    }

    @MethodDescription(description = "groovyscript.wiki.extendedcrafting.endercrafting.addShaped1", type = MethodDescription.Type.ADDITION)
    public IRecipe addShaped(int time, ItemStack output, List<List<IIngredient>> input) {
        return (IRecipe) shapedBuilder()
                .matrix(input)
                .time(time)
                .output(output)
                .register();
    }

    @MethodDescription(description = "groovyscript.wiki.extendedcrafting.endercrafting.addShapeless0", type = MethodDescription.Type.ADDITION)
    public IRecipe addShapeless(ItemStack output, List<List<IIngredient>> input) {
        return addShaped(ModConfig.confEnderTimeRequired, output, input);
    }

    @MethodDescription(description = "groovyscript.wiki.extendedcrafting.endercrafting.addShapeless1", type = MethodDescription.Type.ADDITION)
    public IRecipe addShapeless(int time, ItemStack output, List<IIngredient> input) {
        return (IRecipe) shapelessBuilder()
                .input(input)
                .time(time)
                .output(output)
                .register();
    }

    public IRecipe add(IRecipe recipe) {
        if (recipe != null) {
            addScripted(recipe);
            EnderCrafterRecipeManager.getInstance().getRecipes().add(recipe);
        }
        return recipe;
    }

    @MethodDescription(description = "groovyscript.wiki.removeByOutput", example = @Example("item('extendedcrafting:material:40')"))
    public boolean removeByOutput(ItemStack stack) {
        return EnderCrafterRecipeManager.getInstance().getRecipes().removeIf(recipe -> {
            if (recipe != null && recipe.getRecipeOutput().isItemEqual(stack)) {
                addBackup(recipe);
                return true;
            }
            return false;
        });
    }

    public boolean remove(IRecipe recipe) {
        if (EnderCrafterRecipeManager.getInstance().getRecipes().removeIf(r -> r == recipe)) {
            addBackup(recipe);
            return true;
        }
        return false;
    }

    @MethodDescription(description = "groovyscript.wiki.streamRecipes", type = MethodDescription.Type.QUERY)
    public SimpleObjectStream<IRecipe> streamRecipes() {
        return new SimpleObjectStream<>(EnderCrafterRecipeManager.getInstance().getRecipes()).setRemover(this::remove);
    }

    @MethodDescription(description = "groovyscript.wiki.removeAll", priority = 2000, example = @Example(commented = true))
    public void removeAll() {
        EnderCrafterRecipeManager.getInstance().getRecipes().forEach(this::addBackup);
        EnderCrafterRecipeManager.getInstance().getRecipes().clear();
    }
}
