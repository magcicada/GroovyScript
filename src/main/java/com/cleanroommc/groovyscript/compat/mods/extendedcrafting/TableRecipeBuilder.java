package com.cleanroommc.groovyscript.compat.mods.extendedcrafting;

import com.blakebr0.extendedcrafting.crafting.table.TableRecipeShaped;
import com.cleanroommc.groovyscript.api.GroovyLog;
import com.cleanroommc.groovyscript.api.IIngredient;
import com.cleanroommc.groovyscript.compat.mods.ModSupport;
import com.cleanroommc.groovyscript.compat.vanilla.CraftingRecipeBuilder;
import com.cleanroommc.groovyscript.documentation.annotations.Comp;
import com.cleanroommc.groovyscript.documentation.annotations.Property;
import com.cleanroommc.groovyscript.documentation.annotations.RecipeBuilderMethodDescription;
import com.cleanroommc.groovyscript.documentation.annotations.RecipeBuilderRegistrationMethod;
import com.cleanroommc.groovyscript.helper.ingredient.IngredientHelper;
import it.unimi.dsi.fastutil.chars.Char2ObjectOpenHashMap;
import net.minecraft.item.crafting.IRecipe;
import org.apache.commons.lang3.ArrayUtils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

public abstract class TableRecipeBuilder extends CraftingRecipeBuilder {

    // 0 = any table it fits in, 1-4 specifically that tier of table
    @Property(valid = {@Comp(value = "0", type = Comp.Type.GTE), @Comp(value = "4", type = Comp.Type.LTE)})
    protected int tier = 0;

    public TableRecipeBuilder() {
        super(9, 9);
    }

    @RecipeBuilderMethodDescription
    public TableRecipeBuilder tier(int tier) {
        this.tier = tier;
        int size = this.tier == 0 ? 9 : this.tier * 2 + 1;
        this.width = size;
        this.height = size;
        return this;
    }

    @RecipeBuilderMethodDescription(field = "tier")
    public TableRecipeBuilder tierAny() {
        return tier(0);
    }

    @RecipeBuilderMethodDescription(field = "tier")
    public TableRecipeBuilder tierBasic() {
        return tier(1);
    }

    @RecipeBuilderMethodDescription(field = "tier")
    public TableRecipeBuilder tierAdvanced() {
        return tier(2);
    }

    @RecipeBuilderMethodDescription(field = "tier")
    public TableRecipeBuilder tierElite() {
        return tier(3);
    }

    @RecipeBuilderMethodDescription(field = "tier")
    public TableRecipeBuilder tierUltimate() {
        return tier(4);
    }

    public static class Shaped extends TableRecipeBuilder {

        @Property(value = "groovyscript.wiki.craftingrecipe.keyMap.value", defaultValue = "\\` \\` = IIngredient.EMPTY", priority = 200)
        private final Char2ObjectOpenHashMap<IIngredient> keyMap = new Char2ObjectOpenHashMap<>();
        private final List<String> errors = new ArrayList<>();
        @Property("groovyscript.wiki.craftingrecipe.mirrored.value")
        protected boolean mirrored = false;
        @Property(value = "groovyscript.wiki.craftingrecipe.keyBasedMatrix.value", requirement = "groovyscript.wiki.craftingrecipe.matrix.required", priority = 210)
        private String[] keyBasedMatrix;
        @Property(value = "groovyscript.wiki.craftingrecipe.ingredientMatrix.value", requirement = "groovyscript.wiki.craftingrecipe.matrix.required", valid = {@Comp(value = "1", type = Comp.Type.GTE), @Comp(value = "81", type = Comp.Type.LTE)}, priority = 250)
        private List<List<IIngredient>> ingredientMatrix;

        public Shaped() {
            keyMap.put(' ', IIngredient.EMPTY);
        }

        @RecipeBuilderMethodDescription
        public TableRecipeBuilder.Shaped mirrored(boolean mirrored) {
            this.mirrored = mirrored;
            return this;
        }

        @RecipeBuilderMethodDescription
        public TableRecipeBuilder.Shaped mirrored() {
            return mirrored(true);
        }

        @RecipeBuilderMethodDescription(field = "keyBasedMatrix")
        public TableRecipeBuilder.Shaped matrix(String... matrix) {
            this.keyBasedMatrix = matrix;
            return this;
        }

        @RecipeBuilderMethodDescription(field = "keyBasedMatrix")
        public TableRecipeBuilder.Shaped shape(String... matrix) {
            this.keyBasedMatrix = matrix;
            return this;
        }

        @RecipeBuilderMethodDescription(field = "keyBasedMatrix")
        public TableRecipeBuilder.Shaped row(String row) {
            if (this.keyBasedMatrix == null) {
                this.keyBasedMatrix = new String[]{row};
            } else {
                this.keyBasedMatrix = ArrayUtils.add(this.keyBasedMatrix, row);
            }
            return this;
        }

        @RecipeBuilderMethodDescription(field = "keyMap")
        public TableRecipeBuilder.Shaped key(char c, IIngredient ingredient) {
            this.keyMap.put(c, ingredient);
            return this;
        }

        @RecipeBuilderMethodDescription(field = "keyMap")
        public TableRecipeBuilder.Shaped key(String c, IIngredient ingredient) {
            if (c == null || c.length() != 1) {
                errors.add("key must be a single char, but found '" + c + "'");
                return this;
            }
            this.keyMap.put(c.charAt(0), ingredient);
            return this;
        }

        @RecipeBuilderMethodDescription(field = "keyMap")
        public TableRecipeBuilder.Shaped key(Map<String, IIngredient> map) {
            for (Map.Entry<String, IIngredient> x : map.entrySet()) {
                key(x.getKey(), x.getValue());
            }
            return this;
        }

        @RecipeBuilderMethodDescription(field = "ingredientMatrix")
        public TableRecipeBuilder.Shaped matrix(List<List<IIngredient>> matrix) {
            this.ingredientMatrix = matrix;
            return this;
        }

        @RecipeBuilderMethodDescription(field = "ingredientMatrix")
        public TableRecipeBuilder.Shaped shape(List<List<IIngredient>> matrix) {
            this.ingredientMatrix = matrix;
            return this;
        }

        @Override
        @RecipeBuilderRegistrationMethod
        public IRecipe register() {
            GroovyLog.Msg msg = GroovyLog.msg("Error adding shaped Extended Crafting Table recipe").error()
                    .add((keyBasedMatrix == null || keyBasedMatrix.length == 0) && (ingredientMatrix == null || ingredientMatrix.isEmpty()), () -> "No matrix was defined")
                    .add(keyBasedMatrix != null && ingredientMatrix != null, () -> "A key based matrix AND a ingredient based matrix was defined. This is not allowed!");
            if (msg.postIfNotEmpty()) return null;
            msg.add(IngredientHelper.isEmpty(this.output), () -> "Output must not be empty");
            TableRecipeShaped recipe = null;
            if (keyBasedMatrix != null) {
                recipe = validateShape(msg, errors, keyBasedMatrix, keyMap, ((width1, height1, ingredients) -> ShapedTableRecipe.make(tier, output, ingredients, width1, height1, mirrored, recipeFunction, recipeAction)));
            } else if (ingredientMatrix != null) {
                recipe = validateShape(msg, ingredientMatrix, ((width1, height1, ingredients) -> ShapedTableRecipe.make(tier, output.copy(), ingredients, width1, height1, mirrored, recipeFunction, recipeAction)));
            }
            if (msg.postIfNotEmpty()) return null;
            if (recipe != null) {
                ModSupport.EXTENDED_CRAFTING.get().tableCrafting.add(recipe);
            }
            return recipe;
        }
    }

    public static class Shapeless extends TableRecipeBuilder {

        @Property(value = "groovyscript.wiki.craftingrecipe.ingredients.value", valid = {@Comp(value = "1", type = Comp.Type.GTE), @Comp(value = "81", type = Comp.Type.LTE)}, priority = 250)
        private final List<IIngredient> ingredients = new ArrayList<>();

        @RecipeBuilderMethodDescription(field = "ingredients")
        public TableRecipeBuilder.Shapeless input(IIngredient ingredient) {
            ingredients.add(ingredient);
            return this;
        }

        @RecipeBuilderMethodDescription(field = "ingredients")
        public TableRecipeBuilder.Shapeless input(IIngredient... ingredients) {
            if (ingredients != null) {
                for (IIngredient ingredient : ingredients) {
                    input(ingredient);
                }
            }
            return this;
        }

        @RecipeBuilderMethodDescription(field = "ingredients")
        public TableRecipeBuilder.Shapeless input(Collection<IIngredient> ingredients) {
            if (ingredients != null && !ingredients.isEmpty()) {
                for (IIngredient ingredient : ingredients) {
                    input(ingredient);
                }
            }
            return this;
        }

        public boolean validate() {
            GroovyLog.Msg msg = GroovyLog.msg("Error adding shapeless Extended Crafting Table recipe").error();
            msg.add(tier < 0 || tier > 4, () -> "tier must be between 0 and 4, was instead " + tier);
            msg.add(IngredientHelper.isEmpty(this.output), () -> "Output must not be empty");
            msg.add(ingredients.isEmpty(), () -> "inputs must not be empty");
            msg.add(ingredients.size() > width * height, () -> "maximum inputs are " + (width * height) + " but found " + ingredients.size());
            return !msg.postIfNotEmpty();
        }

        @Override
        @RecipeBuilderRegistrationMethod
        public IRecipe register() {
            if (!validate()) return null;
            ShapelessTableRecipe recipe = ShapelessTableRecipe.make(tier, output.copy(), ingredients, recipeFunction, recipeAction);
            ModSupport.EXTENDED_CRAFTING.get().tableCrafting.add(recipe);
            return recipe;
        }
    }
}
