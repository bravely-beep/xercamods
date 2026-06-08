package xerca.xercapaint.compat.jei;

import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.constants.RecipeTypes;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.registration.IRecipeRegistration;
import mezz.jei.api.runtime.IIngredientManager;
import mezz.jei.api.runtime.IJeiRuntime;
import net.minecraft.core.NonNullList;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.DyeItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.ShapedRecipe;
import net.minecraft.world.item.crafting.ShapedRecipePattern;
import net.minecraft.world.item.crafting.ShapelessRecipe;
import net.minecraft.world.level.ItemLike;
import xerca.xercapaint.Mod;
import xerca.xercapaint.item.Items;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * JEI integration for Joy of Painting.
 *
 * <p>The palette is created and topped up by two {@link net.minecraft.world.item.crafting.CustomRecipe}s,
 * {@link xerca.xercapaint.item.crafting.RecipeCraftPalette} and
 * {@link xerca.xercapaint.item.crafting.RecipeFillPalette}. Because those are dynamic recipes with no
 * fixed ingredient list, neither the vanilla recipe book nor recipe viewers can show them on their own.
 *
 * <p>This plugin teaches JEI how the palette is made by adding two representative recipes to the vanilla
 * crafting category (so the palette shows up when looking up how to make it, and planks/dyes show it under
 * their uses).
 */
@JeiPlugin
public class XercaPaintJeiPlugin implements IModPlugin {
    private static final ResourceLocation PLUGIN_UID = Mod.id("jei");
    private static final int BASIC_COLOR_COUNT = 16;
    /** The example craft shows three dyes; the recipe itself accepts anywhere from 1 to 6. */
    private static final int CRAFT_EXAMPLE_COLORS = 3;
    /** The example fill tops a palette up by three colors. */
    private static final int FILL_EXAMPLE_COLORS = 3;

    @Override
    public ResourceLocation getPluginUid() {
        return PLUGIN_UID;
    }

    @Override
    public void registerRecipes(IRecipeRegistration registration) {
        registration.addRecipes(RecipeTypes.CRAFTING, List.of(craftPaletteExample(), fillPaletteExample()));
    }

    /**
     * Three planks in a row with dyes above them, mirroring {@link xerca.xercapaint.item.crafting.RecipeCraftPalette}.
     */
    private static RecipeHolder<CraftingRecipe> craftPaletteExample() {
        ShapedRecipePattern pattern = ShapedRecipePattern.of(
                Map.of('P', planks(), 'D', anyDye()),
                List.of("DDD", "PPP"));
        ShapedRecipe recipe = new ShapedRecipe(
                "", CraftingBookCategory.MISC, pattern, paletteWithColors(CRAFT_EXAMPLE_COLORS), true);
        return new RecipeHolder<>(Mod.id("jei/craft_palette"), recipe);
    }

    /**
     * An existing palette plus more dyes, mirroring {@link xerca.xercapaint.item.crafting.RecipeFillPalette}.
     */
    private static RecipeHolder<CraftingRecipe> fillPaletteExample() {
        NonNullList<Ingredient> ingredients = NonNullList.create();
        ingredients.add(Ingredient.of(Items.ITEM_PALETTE));
        for (int i = 0; i < FILL_EXAMPLE_COLORS; i++) {
            ingredients.add(anyDye());
        }
        ShapelessRecipe recipe = new ShapelessRecipe(
                "", CraftingBookCategory.MISC, paletteWithColors(FILL_EXAMPLE_COLORS), ingredients);
        return new RecipeHolder<>(Mod.id("jei/fill_palette"), recipe);
    }

    private static Ingredient planks() {
        return Ingredient.of(ItemTags.PLANKS);
    }

    /** Every vanilla dye, so the slot cycles through all of them in JEI. */
    private static Ingredient anyDye() {
        ItemLike[] dyes = Arrays.stream(DyeColor.values())
                .map(DyeItem::byColor)
                .toArray(ItemLike[]::new);
        return Ingredient.of(dyes);
    }

    /** A palette stack with its first {@code count} basic colors marked as filled, for a representative result icon. */
    private static ItemStack paletteWithColors(int count) {
        ItemStack stack = new ItemStack(Items.ITEM_PALETTE);
        byte[] basicColors = new byte[BASIC_COLOR_COUNT];
        for (int i = 0; i < count && i < basicColors.length; i++) {
            basicColors[i] = 1;
        }
        stack.set(Items.PALETTE_BASIC_COLORS, basicColors);
        return stack;
    }
}
