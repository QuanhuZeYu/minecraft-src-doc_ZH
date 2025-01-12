package net.minecraft.item.crafting;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

public class CraftingManager
{
    /** 该类的静态实例 */
    private static final CraftingManager instance = new CraftingManager();
    /** 添加的所有食谱的列表 */
    private List<IRecipe> recipes = new ArrayList<>();
    private static final String __OBFID = "CL_00000090";

    /**
     * 返回该类的静态实例
     */
    public static final CraftingManager getInstance()
    {
        return instance;
    }

    private CraftingManager()
    {
        (new RecipesTools()).addRecipes(this);
        (new RecipesWeapons()).addRecipes(this);
        (new RecipesIngots()).addRecipes(this);
        (new RecipesFood()).addRecipes(this);
        (new RecipesCrafting()).addRecipes(this);
        (new RecipesArmor()).addRecipes(this);
        (new RecipesDyes()).addRecipes(this);
        this.recipes.add(new RecipesArmorDyes());
        this.recipes.add(new RecipeBookCloning());
        this.recipes.add(new RecipesMapCloning());
        this.recipes.add(new RecipesMapExtending());
        this.recipes.add(new RecipeFireworks());
        this.addRecipe(new ItemStack(Items.paper, 3), new Object[] {"###", '#', Items.reeds});
        this.addShapelessRecipe(new ItemStack(Items.book, 1), new Object[] {Items.paper, Items.paper, Items.paper, Items.leather});
        this.addShapelessRecipe(new ItemStack(Items.writable_book, 1), new Object[] {Items.book, new ItemStack(Items.dye, 1, 0), Items.feather});
        this.addRecipe(new ItemStack(Blocks.fence, 2), new Object[] {"###", "###", '#', Items.stick});
        this.addRecipe(new ItemStack(Blocks.cobblestone_wall, 6, 0), new Object[] {"###", "###", '#', Blocks.cobblestone});
        this.addRecipe(new ItemStack(Blocks.cobblestone_wall, 6, 1), new Object[] {"###", "###", '#', Blocks.mossy_cobblestone});
        this.addRecipe(new ItemStack(Blocks.nether_brick_fence, 6), new Object[] {"###", "###", '#', Blocks.nether_brick});
        this.addRecipe(new ItemStack(Blocks.fence_gate, 1), new Object[] {"#W#", "#W#", '#', Items.stick, 'W', Blocks.planks});
        this.addRecipe(new ItemStack(Blocks.jukebox, 1), new Object[] {"###", "#X#", "###", '#', Blocks.planks, 'X', Items.diamond});
        this.addRecipe(new ItemStack(Items.lead, 2), new Object[] {"~~ ", "~O ", "  ~", '~', Items.string, 'O', Items.slime_ball});
        this.addRecipe(new ItemStack(Blocks.noteblock, 1), new Object[] {"###", "#X#", "###", '#', Blocks.planks, 'X', Items.redstone});
        this.addRecipe(new ItemStack(Blocks.bookshelf, 1), new Object[] {"###", "XXX", "###", '#', Blocks.planks, 'X', Items.book});
        this.addRecipe(new ItemStack(Blocks.snow, 1), new Object[] {"##", "##", '#', Items.snowball});
        this.addRecipe(new ItemStack(Blocks.snow_layer, 6), new Object[] {"###", '#', Blocks.snow});
        this.addRecipe(new ItemStack(Blocks.clay, 1), new Object[] {"##", "##", '#', Items.clay_ball});
        this.addRecipe(new ItemStack(Blocks.brick_block, 1), new Object[] {"##", "##", '#', Items.brick});
        this.addRecipe(new ItemStack(Blocks.glowstone, 1), new Object[] {"##", "##", '#', Items.glowstone_dust});
        this.addRecipe(new ItemStack(Blocks.quartz_block, 1), new Object[] {"##", "##", '#', Items.quartz});
        this.addRecipe(new ItemStack(Blocks.wool, 1), new Object[] {"##", "##", '#', Items.string});
        this.addRecipe(new ItemStack(Blocks.tnt, 1), new Object[] {"X#X", "#X#", "X#X", 'X', Items.gunpowder, '#', Blocks.sand});
        this.addRecipe(new ItemStack(Blocks.stone_slab, 6, 3), new Object[] {"###", '#', Blocks.cobblestone});
        this.addRecipe(new ItemStack(Blocks.stone_slab, 6, 0), new Object[] {"###", '#', Blocks.stone});
        this.addRecipe(new ItemStack(Blocks.stone_slab, 6, 1), new Object[] {"###", '#', Blocks.sandstone});
        this.addRecipe(new ItemStack(Blocks.stone_slab, 6, 4), new Object[] {"###", '#', Blocks.brick_block});
        this.addRecipe(new ItemStack(Blocks.stone_slab, 6, 5), new Object[] {"###", '#', Blocks.stonebrick});
        this.addRecipe(new ItemStack(Blocks.stone_slab, 6, 6), new Object[] {"###", '#', Blocks.nether_brick});
        this.addRecipe(new ItemStack(Blocks.stone_slab, 6, 7), new Object[] {"###", '#', Blocks.quartz_block});
        this.addRecipe(new ItemStack(Blocks.wooden_slab, 6, 0), new Object[] {"###", '#', new ItemStack(Blocks.planks, 1, 0)});
        this.addRecipe(new ItemStack(Blocks.wooden_slab, 6, 2), new Object[] {"###", '#', new ItemStack(Blocks.planks, 1, 2)});
        this.addRecipe(new ItemStack(Blocks.wooden_slab, 6, 1), new Object[] {"###", '#', new ItemStack(Blocks.planks, 1, 1)});
        this.addRecipe(new ItemStack(Blocks.wooden_slab, 6, 3), new Object[] {"###", '#', new ItemStack(Blocks.planks, 1, 3)});
        this.addRecipe(new ItemStack(Blocks.wooden_slab, 6, 4), new Object[] {"###", '#', new ItemStack(Blocks.planks, 1, 4)});
        this.addRecipe(new ItemStack(Blocks.wooden_slab, 6, 5), new Object[] {"###", '#', new ItemStack(Blocks.planks, 1, 5)});
        this.addRecipe(new ItemStack(Blocks.ladder, 3), new Object[] {"# #", "###", "# #", '#', Items.stick});
        this.addRecipe(new ItemStack(Items.wooden_door, 1), new Object[] {"##", "##", "##", '#', Blocks.planks});
        this.addRecipe(new ItemStack(Blocks.trapdoor, 2), new Object[] {"###", "###", '#', Blocks.planks});
        this.addRecipe(new ItemStack(Items.iron_door, 1), new Object[] {"##", "##", "##", '#', Items.iron_ingot});
        this.addRecipe(new ItemStack(Items.sign, 3), new Object[] {"###", "###", " X ", '#', Blocks.planks, 'X', Items.stick});
        this.addRecipe(new ItemStack(Items.cake, 1), new Object[] {"AAA", "BEB", "CCC", 'A', Items.milk_bucket, 'B', Items.sugar, 'C', Items.wheat, 'E', Items.egg});
        this.addRecipe(new ItemStack(Items.sugar, 1), new Object[] {"#", '#', Items.reeds});
        this.addRecipe(new ItemStack(Blocks.planks, 4, 0), new Object[] {"#", '#', new ItemStack(Blocks.log, 1, 0)});
        this.addRecipe(new ItemStack(Blocks.planks, 4, 1), new Object[] {"#", '#', new ItemStack(Blocks.log, 1, 1)});
        this.addRecipe(new ItemStack(Blocks.planks, 4, 2), new Object[] {"#", '#', new ItemStack(Blocks.log, 1, 2)});
        this.addRecipe(new ItemStack(Blocks.planks, 4, 3), new Object[] {"#", '#', new ItemStack(Blocks.log, 1, 3)});
        this.addRecipe(new ItemStack(Blocks.planks, 4, 4), new Object[] {"#", '#', new ItemStack(Blocks.log2, 1, 0)});
        this.addRecipe(new ItemStack(Blocks.planks, 4, 5), new Object[] {"#", '#', new ItemStack(Blocks.log2, 1, 1)});
        this.addRecipe(new ItemStack(Items.stick, 4), new Object[] {"#", "#", '#', Blocks.planks});
        this.addRecipe(new ItemStack(Blocks.torch, 4), new Object[] {"X", "#", 'X', Items.coal, '#', Items.stick});
        this.addRecipe(new ItemStack(Blocks.torch, 4), new Object[] {"X", "#", 'X', new ItemStack(Items.coal, 1, 1), '#', Items.stick});
        this.addRecipe(new ItemStack(Items.bowl, 4), new Object[] {"# #", " # ", '#', Blocks.planks});
        this.addRecipe(new ItemStack(Items.glass_bottle, 3), new Object[] {"# #", " # ", '#', Blocks.glass});
        this.addRecipe(new ItemStack(Blocks.rail, 16), new Object[] {"X X", "X#X", "X X", 'X', Items.iron_ingot, '#', Items.stick});
        this.addRecipe(new ItemStack(Blocks.golden_rail, 6), new Object[] {"X X", "X#X", "XRX", 'X', Items.gold_ingot, 'R', Items.redstone, '#', Items.stick});
        this.addRecipe(new ItemStack(Blocks.activator_rail, 6), new Object[] {"XSX", "X#X", "XSX", 'X', Items.iron_ingot, '#', Blocks.redstone_torch, 'S', Items.stick});
        this.addRecipe(new ItemStack(Blocks.detector_rail, 6), new Object[] {"X X", "X#X", "XRX", 'X', Items.iron_ingot, 'R', Items.redstone, '#', Blocks.stone_pressure_plate});
        this.addRecipe(new ItemStack(Items.minecart, 1), new Object[] {"# #", "###", '#', Items.iron_ingot});
        this.addRecipe(new ItemStack(Items.cauldron, 1), new Object[] {"# #", "# #", "###", '#', Items.iron_ingot});
        this.addRecipe(new ItemStack(Items.brewing_stand, 1), new Object[] {" B ", "###", '#', Blocks.cobblestone, 'B', Items.blaze_rod});
        this.addRecipe(new ItemStack(Blocks.lit_pumpkin, 1), new Object[] {"A", "B", 'A', Blocks.pumpkin, 'B', Blocks.torch});
        this.addRecipe(new ItemStack(Items.chest_minecart, 1), new Object[] {"A", "B", 'A', Blocks.chest, 'B', Items.minecart});
        this.addRecipe(new ItemStack(Items.furnace_minecart, 1), new Object[] {"A", "B", 'A', Blocks.furnace, 'B', Items.minecart});
        this.addRecipe(new ItemStack(Items.tnt_minecart, 1), new Object[] {"A", "B", 'A', Blocks.tnt, 'B', Items.minecart});
        this.addRecipe(new ItemStack(Items.hopper_minecart, 1), new Object[] {"A", "B", 'A', Blocks.hopper, 'B', Items.minecart});
        this.addRecipe(new ItemStack(Items.boat, 1), new Object[] {"# #", "###", '#', Blocks.planks});
        this.addRecipe(new ItemStack(Items.bucket, 1), new Object[] {"# #", " # ", '#', Items.iron_ingot});
        this.addRecipe(new ItemStack(Items.flower_pot, 1), new Object[] {"# #", " # ", '#', Items.brick});
        this.addShapelessRecipe(new ItemStack(Items.flint_and_steel, 1), new Object[] {new ItemStack(Items.iron_ingot, 1), new ItemStack(Items.flint, 1)});
        this.addRecipe(new ItemStack(Items.bread, 1), new Object[] {"###", '#', Items.wheat});
        this.addRecipe(new ItemStack(Blocks.oak_stairs, 4), new Object[] {"#  ", "## ", "###", '#', new ItemStack(Blocks.planks, 1, 0)});
        this.addRecipe(new ItemStack(Blocks.birch_stairs, 4), new Object[] {"#  ", "## ", "###", '#', new ItemStack(Blocks.planks, 1, 2)});
        this.addRecipe(new ItemStack(Blocks.spruce_stairs, 4), new Object[] {"#  ", "## ", "###", '#', new ItemStack(Blocks.planks, 1, 1)});
        this.addRecipe(new ItemStack(Blocks.jungle_stairs, 4), new Object[] {"#  ", "## ", "###", '#', new ItemStack(Blocks.planks, 1, 3)});
        this.addRecipe(new ItemStack(Blocks.acacia_stairs, 4), new Object[] {"#  ", "## ", "###", '#', new ItemStack(Blocks.planks, 1, 4)});
        this.addRecipe(new ItemStack(Blocks.dark_oak_stairs, 4), new Object[] {"#  ", "## ", "###", '#', new ItemStack(Blocks.planks, 1, 5)});
        this.addRecipe(new ItemStack(Items.fishing_rod, 1), new Object[] {"  #", " #X", "# X", '#', Items.stick, 'X', Items.string});
        this.addRecipe(new ItemStack(Items.carrot_on_a_stick, 1), new Object[] {"# ", " X", '#', Items.fishing_rod, 'X', Items.carrot}).func_92100_c();
        this.addRecipe(new ItemStack(Blocks.stone_stairs, 4), new Object[] {"#  ", "## ", "###", '#', Blocks.cobblestone});
        this.addRecipe(new ItemStack(Blocks.brick_stairs, 4), new Object[] {"#  ", "## ", "###", '#', Blocks.brick_block});
        this.addRecipe(new ItemStack(Blocks.stone_brick_stairs, 4), new Object[] {"#  ", "## ", "###", '#', Blocks.stonebrick});
        this.addRecipe(new ItemStack(Blocks.nether_brick_stairs, 4), new Object[] {"#  ", "## ", "###", '#', Blocks.nether_brick});
        this.addRecipe(new ItemStack(Blocks.sandstone_stairs, 4), new Object[] {"#  ", "## ", "###", '#', Blocks.sandstone});
        this.addRecipe(new ItemStack(Blocks.quartz_stairs, 4), new Object[] {"#  ", "## ", "###", '#', Blocks.quartz_block});
        this.addRecipe(new ItemStack(Items.painting, 1), new Object[] {"###", "#X#", "###", '#', Items.stick, 'X', Blocks.wool});
        this.addRecipe(new ItemStack(Items.item_frame, 1), new Object[] {"###", "#X#", "###", '#', Items.stick, 'X', Items.leather});
        this.addRecipe(new ItemStack(Items.golden_apple, 1, 0), new Object[] {"###", "#X#", "###", '#', Items.gold_ingot, 'X', Items.apple});
        this.addRecipe(new ItemStack(Items.golden_apple, 1, 1), new Object[] {"###", "#X#", "###", '#', Blocks.gold_block, 'X', Items.apple});
        this.addRecipe(new ItemStack(Items.golden_carrot, 1, 0), new Object[] {"###", "#X#", "###", '#', Items.gold_nugget, 'X', Items.carrot});
        this.addRecipe(new ItemStack(Items.speckled_melon, 1), new Object[] {"###", "#X#", "###", '#', Items.gold_nugget, 'X', Items.melon});
        this.addRecipe(new ItemStack(Blocks.lever, 1), new Object[] {"X", "#", '#', Blocks.cobblestone, 'X', Items.stick});
        this.addRecipe(new ItemStack(Blocks.tripwire_hook, 2), new Object[] {"I", "S", "#", '#', Blocks.planks, 'S', Items.stick, 'I', Items.iron_ingot});
        this.addRecipe(new ItemStack(Blocks.redstone_torch, 1), new Object[] {"X", "#", '#', Items.stick, 'X', Items.redstone});
        this.addRecipe(new ItemStack(Items.repeater, 1), new Object[] {"#X#", "III", '#', Blocks.redstone_torch, 'X', Items.redstone, 'I', Blocks.stone});
        this.addRecipe(new ItemStack(Items.comparator, 1), new Object[] {" # ", "#X#", "III", '#', Blocks.redstone_torch, 'X', Items.quartz, 'I', Blocks.stone});
        this.addRecipe(new ItemStack(Items.clock, 1), new Object[] {" # ", "#X#", " # ", '#', Items.gold_ingot, 'X', Items.redstone});
        this.addRecipe(new ItemStack(Items.compass, 1), new Object[] {" # ", "#X#", " # ", '#', Items.iron_ingot, 'X', Items.redstone});
        this.addRecipe(new ItemStack(Items.map, 1), new Object[] {"###", "#X#", "###", '#', Items.paper, 'X', Items.compass});
        this.addRecipe(new ItemStack(Blocks.stone_button, 1), new Object[] {"#", '#', Blocks.stone});
        this.addRecipe(new ItemStack(Blocks.wooden_button, 1), new Object[] {"#", '#', Blocks.planks});
        this.addRecipe(new ItemStack(Blocks.stone_pressure_plate, 1), new Object[] {"##", '#', Blocks.stone});
        this.addRecipe(new ItemStack(Blocks.wooden_pressure_plate, 1), new Object[] {"##", '#', Blocks.planks});
        this.addRecipe(new ItemStack(Blocks.heavy_weighted_pressure_plate, 1), new Object[] {"##", '#', Items.iron_ingot});
        this.addRecipe(new ItemStack(Blocks.light_weighted_pressure_plate, 1), new Object[] {"##", '#', Items.gold_ingot});
        this.addRecipe(new ItemStack(Blocks.dispenser, 1), new Object[] {"###", "#X#", "#R#", '#', Blocks.cobblestone, 'X', Items.bow, 'R', Items.redstone});
        this.addRecipe(new ItemStack(Blocks.dropper, 1), new Object[] {"###", "# #", "#R#", '#', Blocks.cobblestone, 'R', Items.redstone});
        this.addRecipe(new ItemStack(Blocks.piston, 1), new Object[] {"TTT", "#X#", "#R#", '#', Blocks.cobblestone, 'X', Items.iron_ingot, 'R', Items.redstone, 'T', Blocks.planks});
        this.addRecipe(new ItemStack(Blocks.sticky_piston, 1), new Object[] {"S", "P", 'S', Items.slime_ball, 'P', Blocks.piston});
        this.addRecipe(new ItemStack(Items.bed, 1), new Object[] {"###", "XXX", '#', Blocks.wool, 'X', Blocks.planks});
        this.addRecipe(new ItemStack(Blocks.enchanting_table, 1), new Object[] {" B ", "D#D", "###", '#', Blocks.obsidian, 'B', Items.book, 'D', Items.diamond});
        this.addRecipe(new ItemStack(Blocks.anvil, 1), new Object[] {"III", " i ", "iii", 'I', Blocks.iron_block, 'i', Items.iron_ingot});
        this.addShapelessRecipe(new ItemStack(Items.ender_eye, 1), new Object[] {Items.ender_pearl, Items.blaze_powder});
        this.addShapelessRecipe(new ItemStack(Items.fire_charge, 3), new Object[] {Items.gunpowder, Items.blaze_powder, Items.coal});
        this.addShapelessRecipe(new ItemStack(Items.fire_charge, 3), new Object[] {Items.gunpowder, Items.blaze_powder, new ItemStack(Items.coal, 1, 1)});
        this.addRecipe(new ItemStack(Blocks.daylight_detector), new Object[] {"GGG", "QQQ", "WWW", 'G', Blocks.glass, 'Q', Items.quartz, 'W', Blocks.wooden_slab});
        this.addRecipe(new ItemStack(Blocks.hopper), new Object[] {"I I", "ICI", " I ", 'I', Items.iron_ingot, 'C', Blocks.chest});
        Collections.sort(this.recipes, new Comparator()
        {
            private static final String __OBFID = "CL_00000091";
            public int compare(IRecipe p_compare_1_, IRecipe p_compare_2_)
            {
                return p_compare_1_ instanceof ShapelessRecipes && p_compare_2_ instanceof ShapedRecipes ? 1 : (p_compare_2_ instanceof ShapelessRecipes && p_compare_1_ instanceof ShapedRecipes ? -1 : (p_compare_2_.getRecipeSize() < p_compare_1_.getRecipeSize() ? -1 : (p_compare_2_.getRecipeSize() > p_compare_1_.getRecipeSize() ? 1 : 0)));
            }
            public int compare(Object p_compare_1_, Object p_compare_2_)
            {
                return this.compare((IRecipe)p_compare_1_, (IRecipe)p_compare_2_);
            }
        });
    }

    /**
     * 添加一个成形配方到配方管理器中。
     * <p>
     * 该方法根据提供的配方模式和物品构建一个新的成形配方，并将其添加到配方列表中。
     * 配方模式由一系列字符串组成，每个字符串代表配方的一个水平行，字符串中的字符对应不同的物品。
     * 物品由一系列字符和对应的物品对象（Item, Block, ItemStack）组成。
     * </p>
     *
     * @param resultItem 配方的结果物品，即通过配方可以得到的物品。
     * @param recipePatternAndIngredients 配方模式和物品的可变参数数组。
     *                                    前面部分是配方模式，每个元素是一个字符串，代表配方的一个水平行。
     *                                    后面部分是物品字符和对应的物品对象的交替排列。
     *                                    物品对象可以是 Item 类型、Block 类型或者 ItemStack 类型。
     * @return 返回新创建的 ShapedRecipes 对象。
     * @throws IllegalArgumentException 如果 recipePatternAndIngredients 参数格式不正确。
     */
    public ShapedRecipes addRecipe(ItemStack resultItem, Object... recipePatternAndIngredients) {
        String recipePattern = "";
        int patternHeight = 0;
        int patternWidth = 0;
        int patternIndex = 0;

        // 解析配方模式
        if (recipePatternAndIngredients[patternIndex] instanceof String[]) {
            String[] patternLines = (String[]) recipePatternAndIngredients[patternIndex++];
            for (String patternLine : patternLines) {
                ++patternHeight;
                patternWidth = patternLine.length();
                recipePattern += patternLine;
            }
        } else {
            while (recipePatternAndIngredients[patternIndex] instanceof String) {
                String patternLine = (String) recipePatternAndIngredients[patternIndex++];
                ++patternHeight;
                patternWidth = patternLine.length();
                recipePattern += patternLine;
            }
        }

        // 解析配方中的物品
        HashMap<Character, ItemStack> ingredientMap = new HashMap<>();
        for (; patternIndex < recipePatternAndIngredients.length; patternIndex += 2) {
            Character ingredientChar = (Character) recipePatternAndIngredients[patternIndex];
            ItemStack ingredientStack = null;

            if (recipePatternAndIngredients[patternIndex + 1] instanceof Item) {
                ingredientStack = new ItemStack((Item) recipePatternAndIngredients[patternIndex + 1]);
            } else if (recipePatternAndIngredients[patternIndex + 1] instanceof Block) {
                ingredientStack = new ItemStack((Block) recipePatternAndIngredients[patternIndex + 1], 1, 32767);
            } else if (recipePatternAndIngredients[patternIndex + 1] instanceof ItemStack) {
                ingredientStack = (ItemStack) recipePatternAndIngredients[patternIndex + 1];
            }

            ingredientMap.put(ingredientChar, ingredientStack);
        }

        // 根据配方模式和物品映射创建物品栈数组
        ItemStack[] recipeIngredients = new ItemStack[patternWidth * patternHeight];
        for (int i = 0; i < patternWidth * patternHeight; ++i) {
            char patternChar = recipePattern.charAt(i);
            if (ingredientMap.containsKey(patternChar)) {
                recipeIngredients[i] = ingredientMap.get(patternChar).copy();
            } else {
                recipeIngredients[i] = null;
            }
        }

        // 创建新的 ShapedRecipes 对象并添加到配方列表中
        ShapedRecipes newRecipe = new ShapedRecipes(patternWidth, patternHeight, recipeIngredients, resultItem);
        this.recipes.add(newRecipe);
        return newRecipe;
    }

    public void addShapelessRecipe(ItemStack p_77596_1_, Object ... p_77596_2_)
    {
        ArrayList arraylist = new ArrayList();
        Object[] aobject = p_77596_2_;
        int i = p_77596_2_.length;

        for (int j = 0; j < i; ++j)
        {
            Object object1 = aobject[j];

            if (object1 instanceof ItemStack)
            {
                arraylist.add(((ItemStack)object1).copy());
            }
            else if (object1 instanceof Item)
            {
                arraylist.add(new ItemStack((Item)object1));
            }
            else
            {
                if (!(object1 instanceof Block))
                {
                    throw new RuntimeException("Invalid shapeless recipy!");
                }

                arraylist.add(new ItemStack((Block)object1));
            }
        }

        this.recipes.add(new ShapelessRecipes(p_77596_1_, arraylist));
    }

    public ItemStack findMatchingRecipe(InventoryCrafting craftingInventory, World world) {
        // 记录非空物品栈的数量
        int nonEmptyStackCount = 0;
        // 第一个非空物品栈
        ItemStack firstItemStack = null;
        // 第二个非空物品栈
        ItemStack secondItemStack = null;

        // 遍历合成网格中的所有槽位
        for (int slotIndex = 0; slotIndex < craftingInventory.getSizeInventory(); ++slotIndex) {
            ItemStack currentStack = craftingInventory.getStackInSlot(slotIndex);

            if (currentStack != null) {
                // 如果这是第一个非空物品栈，则记录它
                if (nonEmptyStackCount == 0) {
                    firstItemStack = currentStack;
                }
                // 如果这是第二个非空物品栈，则记录它
                else if (nonEmptyStackCount == 1) {
                    secondItemStack = currentStack;
                }

                // 增加非空物品栈计数
                ++nonEmptyStackCount;
            }
        }

        // 检查是否仅有两个物品栈且它们可以修复
        if (nonEmptyStackCount == 2 &&
            firstItemStack.getItem() == secondItemStack.getItem() &&
            firstItemStack.stackSize == 1 &&
            secondItemStack.stackSize == 1 &&
            firstItemStack.getItem().isRepairable())
        {
            Item item = firstItemStack.getItem();
            // 计算第一个物品栈的剩余耐久度
            int firstStackRemainingDamage = item.getMaxDamage() - firstItemStack.getItemDamageForDisplay();
            // 计算第二个物品栈的剩余耐久度
            int secondStackRemainingDamage = item.getMaxDamage() - secondItemStack.getItemDamageForDisplay();
            // 计算修复后的新耐久度
            int newRemainingDamage = firstStackRemainingDamage + secondStackRemainingDamage + item.getMaxDamage() * 5 / 100;
            int newDamageValue = item.getMaxDamage() - newRemainingDamage;

            // 确保耐久度值不低于0
            if (newDamageValue < 0)
            {
                newDamageValue = 0;
            }

            // 返回修复后的物品
            return new ItemStack(item, 1, newDamageValue);
        }
        else {
            // 遍历所有注册的合成配方
            for (int recipeIndex = 0; recipeIndex < this.recipes.size(); ++recipeIndex) {
                IRecipe currentRecipe = (IRecipe)this.recipes.get(recipeIndex);

                // 如果当前配方匹配，则返回合成结果
                if (currentRecipe.matches(craftingInventory, world)) {
                    return currentRecipe.getCraftingResult(craftingInventory);
                }
            }

            // 如果没有匹配的配方，则返回null
            return null;
        }
    }


    /**
     * returns the List<> of all recipes
     */
    public List<net.minecraft.item.crafting.IRecipe> getRecipeList()
    {
        return this.recipes;
    }
}