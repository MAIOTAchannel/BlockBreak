package com.github.maiotachannel.blockbreak;

import jdk.internal.org.jline.utils.DiffHelper;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.FurnaceRecipe;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.RecipeChoice;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashMap;
import java.util.Map;

@SuppressWarnings("ALL")
public final class BlockBreak extends JavaPlugin{

    @Override
    public void onEnable() { addRecipes(); }

    void addRecipes(){
        /* ダイヤブロック4個 -> 圧縮ダイヤモンドブロック1個 */addCraftRecipe(addItem(Material.DIAMOND_BLOCK,"圧縮ダイヤモンドブロック",null,null),"   ","** ","** ", new HashMap<Character,ItemStack>(){{put('*', new ItemStack(Material.DIAMOND_BLOCK));}});
        /* 圧縮ダイヤモンド9個 -> 圧縮ダイヤモンドブロック1個 */addCraftRecipe(addItem(Material.DIAMOND_BLOCK,"圧縮ダイヤモンドブロック",null,null),"***","***","***", new HashMap<Character,ItemStack>(){{put('*', addItem(Material.DIAMOND,"圧縮ダイヤモンド",null,null));}});
        /* ダイヤモンド4個 -> 圧縮ダイヤモンド1個 */addCraftRecipe(addItem(Material.DIAMOND,"圧縮ダイヤモンド",null,null),"   ","** ","** ", new HashMap<Character,ItemStack>(){{put('*', new ItemStack(Material.DIAMOND));}});
        /*  */
    }

    ItemStack addItem(Material material, String name, Map<Enchantment,Integer> enchantments, Map<Attribute,Integer> attributes){
        ItemStack itemStack = new ItemStack(material);
        ItemMeta itemMeta = itemStack.getItemMeta();
        itemMeta.setDisplayName(ChatColor.DARK_PURPLE + name);
        if (enchantments != null){
            for (Map.Entry<Enchantment,Integer> entry : enchantments.entrySet()){
                itemMeta.addEnchant(entry.getKey(),entry.getValue(), true);
            }
        }
        if (enchantments != null){
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

    void addFurnaceRecipe(String key, ItemStack result, ItemStack material, float giveXP, int CookingTime){
        RecipeChoice recipeChoice = new RecipeChoice.ExactChoice(material);
        FurnaceRecipe furnaceRecipe = new FurnaceRecipe(new NamespacedKey(this, key), result, recipeChoice,giveXP,CookingTime);
        getServer().addRecipe(furnaceRecipe);
    }

}
