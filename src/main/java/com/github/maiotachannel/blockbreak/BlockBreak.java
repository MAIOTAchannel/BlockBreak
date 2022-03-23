package com.github.maiotachannel.blockbreak;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.enchantments.EnchantmentTarget;
import org.bukkit.inventory.*;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

@SuppressWarnings("ALL")
public final class BlockBreak extends JavaPlugin{

    @Override
    public void onEnable() {
        addRecipes();
        registerGlow();
    }

    void addRecipes(){
        /* 鉄インゴット -> 鋼鉄インゴット */addFurnaceRecipe("Steel_Ingot", addItem(Material.IRON_INGOT, "鋼鉄インゴット",null, null), new ItemStack(Material.IRON_INGOT), new Random().nextInt(5)+1, 10);
        /* 鋼鉄インゴット9個 -> 鋼鉄ブロック */addCraftRecipe(addItem(Material.IRON_BLOCK, "鋼鉄ブロック",null ,null), "***","***","***", new HashMap<Character,ItemStack>(){{put('*', addItem(Material.IRON_INGOT, "鋼鉄インゴット",null, null));}});
        /* ダイヤモンド8個 + 鋼鉄インゴット -> 圧縮ダイヤモンド */addCraftRecipe(addItem(Material.DIAMOND,"圧縮ダイヤモンド",null,null),"***","*+*","***", new HashMap<Character,ItemStack>(){{put('*', new ItemStack(Material.DIAMOND));put('+', addItem(Material.IRON_INGOT, "鋼鉄インゴット",null, null));}});
        /* ダイヤブロック8個 + 鋼鉄ブロック -> 圧縮ダイヤモンドブロック */addCraftRecipe(addItem(Material.DIAMOND_BLOCK,"圧縮ダイヤモンドブロック",null,null),"***","*+*","***", new HashMap<Character,ItemStack>(){{put('*', new ItemStack(Material.DIAMOND_BLOCK));put('+', addItem(Material.IRON_BLOCK, "鋼鉄ブロック",null ,null));}});
        /* 圧縮ダイヤモンド9個 -> 圧縮ダイヤモンドブロック */addCraftRecipe(addItem(Material.DIAMOND_BLOCK,"圧縮ダイヤモンドブロック",null,null),"***","***","***", new HashMap<Character,ItemStack>(){{put('*', addItem(Material.DIAMOND,"圧縮ダイヤモンド",null,null));}});
        /* 圧縮ダイヤモンドブロック8個 + ネザースター　->　「まじで？」ブロック */addCraftRecipe(addItem(Material.BEDROCK,"「まじで？」ブロック", null, null), "***", "*+*", "***", new HashMap<Character,ItemStack>(){{put('*', addItem(Material.DIAMOND_BLOCK,"圧縮ダイヤモンドブロック",null,null));put('+', new ItemStack(Material.NETHER_STAR));}});
        /* 鋼鉄インゴット2個 + 棒1個 ->　鋼鉄剣 */addCraftRecipe(addItem(Material.IRON_SWORD, "鋼鉄剣", null, null)," * ", " * ", " + ", new HashMap<Character,ItemStack>(){{put('*', addItem(Material.IRON_INGOT, "鋼鉄インゴット",null, null));put('+', new ItemStack(Material.STICK));}});
        /* 鋼鉄インゴット3個 + 棒2個 -> 鋼鉄ピッケル */addCraftRecipe(addItem(Material.IRON_PICKAXE, "鋼鉄ピッケル",new HashMap<Enchantment,Integer>(){{put(Enchantment.DURABILITY,1);}}, null),"***", " + ", " + ",new HashMap<Character,ItemStack>(){{put('*', addItem(Material.IRON_INGOT, "鋼鉄インゴット",null, null));put('+', new ItemStack(Material.STICK));}});
        /* 鋼鉄インゴット1個 + 棒2個 ->　鋼鉄シャベル */
        /* 鋼鉄剣 + ダイヤモンド　->　ダイヤ加工済み鋼鉄剣 */
        /* 鋼鉄ピッケル + ダイヤモンド　->　ダイヤ加工済み鋼鉄ピッケル */
        /* 鋼鉄シャベル + ダイヤモンド　->　ダイヤ加工済み鋼鉄シャベル */
        /* 圧縮ダイヤモンド2個 + 棒1個　->　強化ダイヤモンド剣 */
        /* 圧縮ダイヤモンド3個 + 棒2個 -> 強化ダイヤモンドピッケル　*/
        /* 圧縮ダイヤモンド1個 + 棒2個　->　強化ダイヤモンドシャベル */
        /* 圧縮ダイヤモンドブロック2個 + 棒1個　->　圧縮ダイヤモンド剣 */
        /* 圧縮ダイヤモンドブロック3個 + 棒2個　-> 圧縮ダイヤモンドピッケル　*/
        /* 圧縮ダイヤモンドブロック1個 + 棒2個　->　圧縮ダイヤモンドシャベル */
        /* 「まじで？」ブロック2個 + 棒1個　->　「もうやばいね！」剣 */
        /* 「まじで？」ブロック3個 + 棒2個　->　「もうやばいね！」ピッケル */
        /* 「まじで？」ブロック1個 + 棒2個　->　「もうやばいね！」シャベル */
        /* 圧縮ダイヤモンドブロック5個　->　圧縮ダイヤモンドヘルメット */
        /* 圧縮ダイヤモンドブロック8個　->　圧縮ダイヤモンドチェストプレート */
        /* 圧縮ダイヤモンドブロック7個　->　圧縮ダイヤモンドレギンス */
        /* 圧縮ダイヤモンドブロック4個　->　圧縮ダイヤモンドブーツ */
        /* 圧縮ダイヤモンドヘルメット + エンダードラゴンの頭　->　暗視付き圧縮ダイヤモンドヘルメット */
        /* 　->　 */
    }

    ItemStack addItem(Material material, String name, Map<Enchantment,Integer> enchantments, Map<Attribute,Integer> attributes){
        ItemStack itemStack = new ItemStack(material);
        ItemMeta itemMeta = itemStack.getItemMeta();
        itemMeta.setDisplayName(ChatColor.DARK_PURPLE + name);
        if (enchantments != null) {
            for (Map.Entry<Enchantment, Integer> entry : enchantments.entrySet()) {
                itemMeta.addEnchant(entry.getKey(), entry.getValue(), true);
            }
        } else {
            NamespacedKey key = new NamespacedKey(this, getDescription().getName());
            Glow glow = new Glow(key);
            itemMeta.addEnchant(glow, 1, true);
        }
        if (attributes != null){
            for (Map.Entry<Attribute,Integer> entry : attributes.entrySet()){
                itemMeta.addAttributeModifier(entry.getKey(), new AttributeModifier(entry.getKey().name(), entry.getValue(), AttributeModifier.Operation.ADD_NUMBER));
            }
        }
        itemStack.setItemMeta(itemMeta);
        return itemStack;
    }

    void addCraftRecipe(ItemStack result, String Recipe1, String Recipe2, String Recipe3, Map<Character,ItemStack> RecipeItem){

        ShapedRecipe shapedRecipe = new ShapedRecipe(result);
        shapedRecipe.shape(Recipe1,Recipe2,Recipe3);
        for (Map.Entry<Character,ItemStack> entry : RecipeItem.entrySet()){
            RecipeChoice recipeChoice = new RecipeChoice.ExactChoice(entry.getValue());
            shapedRecipe.setIngredient(entry.getKey(), recipeChoice);
        }

        getServer().addRecipe(shapedRecipe);
    }

    void addFurnaceRecipe(String key, ItemStack result, ItemStack material, float giveXP, float CookingTime){
        RecipeChoice recipeChoice = new RecipeChoice.ExactChoice(material);
        FurnaceRecipe furnaceRecipe = new FurnaceRecipe(new NamespacedKey(this, key), result, recipeChoice,giveXP, (int) (CookingTime*20));
        getServer().addRecipe(furnaceRecipe);
    }

    public void registerGlow() {
        try {
            Field f = Enchantment.class.getDeclaredField("acceptingNew");
            f.setAccessible(true);
            f.set(null, true);
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        try {
            NamespacedKey key = new NamespacedKey(this, getDescription().getName());

            Glow glow = new Glow(key);
            Enchantment.registerEnchantment(glow);
        }
        catch (IllegalArgumentException e){
        }
        catch(Exception e){
            e.printStackTrace();
        }
    }

}

@SuppressWarnings("ConstantConditions")
class Glow extends Enchantment {

    public Glow(NamespacedKey i) {
        super(i);
    }

    @Override
    public boolean canEnchantItem(ItemStack arg0) {
        return false;
    }

    @Override
    public boolean conflictsWith(Enchantment arg0) {
        return false;
    }

    @Override
    public EnchantmentTarget getItemTarget() {
        return null;
    }

    @Override
    public int getMaxLevel() {
        return 0;
    }

    @Override
    public String getName() {
        return null;
    }

    @Override
    public int getStartLevel() {
        return 0;
    }

    @Override
    public boolean isCursed() {
        return false;
    }

    @Override
    public boolean isTreasure() {
        return false;
    }

}