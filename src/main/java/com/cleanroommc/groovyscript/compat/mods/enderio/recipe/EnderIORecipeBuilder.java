package com.cleanroommc.groovyscript.compat.mods.enderio.recipe;

import com.cleanroommc.groovyscript.documentation.annotations.Property;
import com.cleanroommc.groovyscript.documentation.annotations.RecipeBuilderMethodDescription;
import com.cleanroommc.groovyscript.helper.recipe.AbstractRecipeBuilder;
import crazypants.enderio.base.recipe.RecipeLevel;

public abstract class EnderIORecipeBuilder<T> extends AbstractRecipeBuilder<T> {

    @Property(value = "groovyscript.wiki.enderio.level.value", needsOverride = true,defaultValue = "RecipeLevel.IGNORE", priority = 2000)
    protected RecipeLevel level = RecipeLevel.IGNORE;
    @Property(value = "groovyscript.wiki.enderio.energy.value", needsOverride = true, priority = 2000)
    protected int energy;

    @RecipeBuilderMethodDescription(field = "level")
    public EnderIORecipeBuilder<T> tierSimple() {
        this.level = RecipeLevel.SIMPLE;
        return this;
    }

    @RecipeBuilderMethodDescription(field = "level")
    public EnderIORecipeBuilder<T> tierNormal() {
        this.level = RecipeLevel.NORMAL;
        return this;
    }

    @RecipeBuilderMethodDescription(field = "level")
    public EnderIORecipeBuilder<T> tierEnhanced() {
        this.level = RecipeLevel.ADVANCED;
        return this;
    }

    @RecipeBuilderMethodDescription(field = "level")
    public EnderIORecipeBuilder<T> tierAny() {
        this.level = RecipeLevel.IGNORE;
        return this;
    }

    @RecipeBuilderMethodDescription
    public EnderIORecipeBuilder<T> energy(int energy) {
        this.energy = energy;
        return this;
    }
}
