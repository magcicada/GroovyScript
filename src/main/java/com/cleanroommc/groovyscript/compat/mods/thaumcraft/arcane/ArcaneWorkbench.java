package com.cleanroommc.groovyscript.compat.mods.thaumcraft.arcane;

import com.cleanroommc.groovyscript.api.GroovyBlacklist;
import com.cleanroommc.groovyscript.api.IIngredient;
import com.cleanroommc.groovyscript.compat.vanilla.VanillaModule;
import com.cleanroommc.groovyscript.documentation.annotations.Example;
import com.cleanroommc.groovyscript.documentation.annotations.MethodDescription;
import com.cleanroommc.groovyscript.documentation.annotations.RecipeBuilderDescription;
import com.cleanroommc.groovyscript.documentation.annotations.RegistryDescription;
import com.cleanroommc.groovyscript.registry.ReloadableRegistryManager;
import com.cleanroommc.groovyscript.registry.VirtualizedRegistry;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.registry.ForgeRegistries;

@RegistryDescription
public class ArcaneWorkbench extends VirtualizedRegistry<Void> {

    public static final ResourceLocation DEFAULT = new ResourceLocation("");

    @Override
    @GroovyBlacklist
    public void onReload() {
        // do nothing
    }

    public void add(String name, IRecipe recipe) {
        ReloadableRegistryManager.addRegistryEntry(ForgeRegistries.RECIPES, name, recipe);
    }

    @MethodDescription
    public void remove(String name) {
        ReloadableRegistryManager.removeRegistryEntry(ForgeRegistries.RECIPES, name);
    }

    @MethodDescription(description = "groovyscript.wiki.removeByOutput", example = @Example("item('thaumcraft:mechanism_simple')"))
    public void removeByOutput(IIngredient output) {
        VanillaModule.crafting.removeByOutput(output, true);
    }

    @RecipeBuilderDescription(example = {
            @Example(".researchKey('UNLOCKALCHEMY@3').output(item('minecraft:pumpkin')).row('SS ').row('   ').row('   ').key('S', item('minecraft:pumpkin_seeds')).aspect('terra').vis(5)"),
            @Example(".researchKey('UNLOCKALCHEMY@3').output(item('minecraft:clay')).matrix('SS ','   ','   ').key('S', item('minecraft:pumpkin')).aspect(aspect('terra')).vis(5)")
    })
    public ArcaneRecipeBuilder.Shaped shapedBuilder() {
        return new ArcaneRecipeBuilder.Shaped();
    }

    @RecipeBuilderDescription(example = @Example(".researchKey('UNLOCKALCHEMY@3').input(item('minecraft:pumpkin')).input(item('minecraft:stick')).input(item('minecraft:stick')).output(item('thaumcraft:void_hoe')).vis(0)"))
    public ArcaneRecipeBuilder.Shapeless shapelessBuilder() {
        return new ArcaneRecipeBuilder.Shapeless();
    }

}

