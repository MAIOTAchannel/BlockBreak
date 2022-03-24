package com.github.maiotachannel.blockbreak;

import net.coreprotect.CoreProtect;
import net.coreprotect.CoreProtectAPI;
import org.bukkit.*;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.block.Block;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.command.*;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.enchantments.EnchantmentTarget;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.*;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.StringUtil;

import java.lang.reflect.Field;
import java.util.*;

@SuppressWarnings("ALL")
public final class BlockBreak extends JavaPlugin implements Listener, CommandExecutor, TabCompleter {

    int time = 0;
    int BlockCount = 0;
    int maxCount = 0;
    boolean Game = false;
    int BLs = 0;
    BossBar bossBar;
    BukkitTask task;
    List<Material> materials = Arrays.asList(new Material[]{Material.STONE, Material.GRANITE, Material.DIORITE,Material.ANDESITE, Material.GRASS_BLOCK, Material.DIRT, Material.COARSE_DIRT, Material.PODZOL, Material.GRAVEL, Material.BLACKSTONE});

    @Override
    public void onEnable() {
        getServer().getPluginManager().registerEvents(this,this);
        Objects.requireNonNull(getCommand("game")).setExecutor(this);
        Objects.requireNonNull(getCommand("game")).setTabCompleter(this);
        registerGlow();
        addRecipes();
    }

    @Override
    public void onDisable(){
        bossBar.removeAll();
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String s, String[] args){

        Player p = (Player) sender;

        if(sender instanceof Player){
            p = (Player) sender;
        }else{
            sender.sendMessage("プレイヤー以外は実行できません");
            return true;
        }

        if(!p.hasPermission("game.command")){
            p.sendMessage("権限がありません");
            return true;
        }

        if(args.length >= 1) {
            String subcommand = args[0];
            if (subcommand.contains("start")) {
                if (!Game) {
                    if (maxCount > 0) {
                        Game = true;
                        time = 0;
                        bossBar = Bukkit.createBossBar("採掘量: " + BlockCount + " / " + maxCount + "  経過時間:00:00:00" + " 採掘速度: " + BLs + "BL/s", BarColor.PURPLE, BarStyle.SOLID);
                        bossBar.setProgress(0);
                        for (Player player : getServer().getOnlinePlayers()) {
                            bossBar.addPlayer(player);
                        }
                        task = new BukkitRunnable() {

                            int breaked;

                            @Override
                            public void run() {
                                int s = time % 60;
                                int m = time / 60;
                                int h = m / 60;
                                m = m % 60;
                                BLs = BlockCount - breaked;
                                breaked = BlockCount;
                                bossBar.setTitle("採掘量: " + BlockCount + " / " + maxCount + "  経過時間: " + h + ":" + m + ":" + s + " 採掘速度: " + BLs + "BL/s");
                                for (Player player : getServer().getOnlinePlayers()) {
                                    if (player.getInventory().getHelmet().getItemMeta().getDisplayName().contains(ChatColor.DARK_PURPLE + "暗視付き圧縮ダイヤモンドヘルメット")) {
                                        player.addPotionEffect(new PotionEffect(PotionEffectType.NIGHT_VISION, 300, 0));
                                    }
                                }
                                time++;
                            }
                        }.runTaskTimer(this, 0, 20);
                    } else {
                        sender.sendMessage("目標値を0以上にしてください");
                    }
                }else{
                    sender.sendMessage("ゲームを実行中です");
                }
            }
            if (subcommand.contains("maxcount")) {
                String subcommand1 = "0";
                if (args[1] != null) {
                    subcommand1 = args[1];
                }
                maxCount = Integer.parseInt(subcommand1);
            }
            if (subcommand.contains("end")){
                Game = false;
                bossBar.removeAll();
                task.cancel();
                BlockCount = 0;
            }
        }

        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        final List<String> completions = new ArrayList<>();
        //copy matches of first argument from list (ex: if first arg is 'm' will return just 'minecraft')

        String[] COMMANDS = {"start", "maxcount", "end"};

        if(args.length == 1) {
            StringUtil.copyPartialMatches(args[0], Arrays.asList(COMMANDS), completions);
        }
        //sort the list
        Collections.sort(completions);
        return completions;
    }

    @EventHandler
    void onBlockBreak(BlockBreakEvent event){
        Player p = event.getPlayer();
        Block block = event.getBlock();
        CoreProtectAPI api = getCoreProtect();

        if(Game) {
            if (materials.contains(block.getType())) {
                if (api.blockLookup(block, time).isEmpty()) {
                    BlockCount += 1;
                    int s = time % 60;
                    int m = time / 60;
                    int h = m / 60;
                    m = m % 60;
                    bossBar.setTitle("採掘量: " + BlockCount + " / " + maxCount + "  経過時間: "+h+":"+m+":"+s + " 採掘速度: "+BLs+"BL/s");
                    bossBar.setProgress((double) BlockCount/(double) maxCount);
                    if (BlockCount == maxCount){
                        Game = false;
                        bossBar.removeAll();
                        task.cancel();
                        BlockCount = 0;
                        for (Player player :getServer().getOnlinePlayers()){
                            player.sendTitle(ChatColor.RED + "ゲーム終了！","あなたたちは"+BlockCount+"Blockを"+h+":"+m+":"+s+"でクリアした！");
                            player.playSound(player.getLocation(),Sound.ENTITY_ENDER_DRAGON_AMBIENT,0.5f,1.0f);
                        }
                    }
                }
            }
            if (block.getType() == Material.DIAMOND_ORE) {
                if (new Random().nextInt(100) == 77){
                    block.getWorld().dropItem(block.getLocation(),new ItemStack(Material.NETHER_STAR,1));
                }
            }
        }
    }

    void addRecipes(){
        /* 鉄インゴット -> 鋼鉄インゴット */addFurnaceRecipe("steel_ingot", addItem(Material.IRON_INGOT, "鋼鉄インゴット", 1,null, null), new ItemStack(Material.IRON_INGOT), new Random().nextInt(5)+1, 10);
        /* 鋼鉄インゴット9個 -> 鋼鉄ブロック */addCraftRecipe(addItem(Material.IRON_BLOCK, "鋼鉄ブロック", 1,null ,null), "steel_block", "***","***","***", new HashMap<Character,ItemStack>(){{put('*', addItem(Material.IRON_INGOT, "鋼鉄インゴット", 1,null, null));}});
        /* 鋼鉄ブロック -> 鋼鉄インゴット9個 */addNonCraftRecipe(addItem(Material.IRON_INGOT, "鋼鉄インゴット", 9,null, null),"steel_block_disassembly",new HashMap<Character,ItemStack>(){{put('*', addItem(Material.IRON_BLOCK, "鋼鉄ブロック", 1,null ,null));}});
        /* ダイヤモンド8個 + 鋼鉄インゴット -> 圧縮ダイヤモンド */addCraftRecipe(addItem(Material.DIAMOND,"圧縮ダイヤモンド", 1,null,null), "compressed_diamond","***","*+*","***", new HashMap<Character,ItemStack>(){{put('*', new ItemStack(Material.DIAMOND));put('+', addItem(Material.IRON_INGOT, "鋼鉄インゴット", 1,null, null));}});
        /* ダイヤブロック8個 + 鋼鉄ブロック -> 圧縮ダイヤモンドブロック */addCraftRecipe(addItem(Material.DIAMOND_BLOCK,"圧縮ダイヤモンドブロック", 1,null,null), "compressed_diamond_block1","***","*+*","***", new HashMap<Character,ItemStack>(){{put('*', new ItemStack(Material.DIAMOND_BLOCK));put('+', addItem(Material.IRON_BLOCK, "鋼鉄ブロック", 1,null ,null));}});
        /* 圧縮ダイヤモンド9個 -> 圧縮ダイヤモンドブロック */addCraftRecipe(addItem(Material.DIAMOND_BLOCK,"圧縮ダイヤモンドブロック", 1,null,null),"compressed_diamond_block2", "***", "***","***", new HashMap<Character,ItemStack>(){{put('*', addItem(Material.DIAMOND,"圧縮ダイヤモンド", 1,null,null));}});
        /* 圧縮ダイヤモンドブロック -> 圧縮ダイヤモンド9個 */addNonCraftRecipe(addItem(Material.DIAMOND,"圧縮ダイヤモンド", 9,null,null),"compressed_diamond_block_disassembly",new HashMap<Character,ItemStack>(){{put('*', addItem(Material.DIAMOND_BLOCK,"圧縮ダイヤモンドブロック", 1,null,null));}});
        /* 圧縮ダイヤモンドブロック8個 + ネザースター　->　「まじで？」ブロック */addCraftRecipe(addItem(Material.BEDROCK,"「まじで？」ブロック", 1, null, null), "majide_block", "***", "*+*", "***", new HashMap<Character,ItemStack>(){{put('*', addItem(Material.DIAMOND_BLOCK,"圧縮ダイヤモンドブロック", 1,null,null));put('+', new ItemStack(Material.NETHER_STAR));}});
        /* 鋼鉄インゴット2個 + 棒1個 ->　鋼鉄剣 */addCraftRecipe(addItem(Material.IRON_SWORD, "鋼鉄剣", 1, new HashMap<Enchantment,Integer>(){{put(Enchantment.DURABILITY,1);put(Enchantment.DAMAGE_ALL,1);}}, null), "steel_sword"," * ", " * ", " + ", new HashMap<Character,ItemStack>(){{put('*', addItem(Material.IRON_INGOT, "鋼鉄インゴット", 1,null, null));put('+', new ItemStack(Material.STICK));}});
        /* 鋼鉄インゴット3個 + 棒2個 -> 鋼鉄ピッケル */addCraftRecipe(addItem(Material.IRON_PICKAXE, "鋼鉄ピッケル", 1, new HashMap<Enchantment,Integer>(){{put(Enchantment.DURABILITY,1); put(Enchantment.DIG_SPEED,1);}}, null), "steel_pickaxe","***", " + ", " + ",new HashMap<Character,ItemStack>(){{put('*', addItem(Material.IRON_INGOT, "鋼鉄インゴット", 1,null, null));put('+', new ItemStack(Material.STICK));}});
        /* 鋼鉄インゴット1個 + 棒2個 ->　鋼鉄シャベル */addCraftRecipe(addItem(Material.IRON_SHOVEL,"鋼鉄シャベル", 1,new HashMap<Enchantment,Integer>(){{put(Enchantment.DURABILITY,1); put(Enchantment.DIG_SPEED,1);}},null), "steel_shovel"," * "," + "," + ",new HashMap<Character,ItemStack>(){{put('*', addItem(Material.IRON_INGOT, "鋼鉄インゴット", 1,null, null)); put('+', new ItemStack(Material.STICK));}});
        /* 鋼鉄剣 + ダイヤモンド　->　ダイヤ加工済み鋼鉄剣 */addNonCraftRecipe(addItem(Material.IRON_SWORD,"ダイヤ加工済み鋼鉄剣", 1,new HashMap<Enchantment,Integer>(){{put(Enchantment.DURABILITY,2);put(Enchantment.DAMAGE_ALL,2);put(Enchantment.LOOT_BONUS_MOBS,1);}},null), "diamond_processed_steel_sword",new HashMap<Character,ItemStack>(){{put('*', addItem(Material.IRON_SWORD, "鋼鉄剣", 1, new HashMap<Enchantment,Integer>(){{put(Enchantment.DURABILITY,1);put(Enchantment.DAMAGE_ALL,1);}}, null)); put('+', new ItemStack(Material.DIAMOND));}});
        /* 鋼鉄ピッケル + ダイヤモンド　->　ダイヤ加工済み鋼鉄ピッケル */addNonCraftRecipe(addItem(Material.IRON_PICKAXE,"ダイヤ加工済み鋼鉄ピッケル", 1,new HashMap<Enchantment,Integer>(){{put(Enchantment.DURABILITY,2); put(Enchantment.DIG_SPEED,2);put(Enchantment.LOOT_BONUS_BLOCKS,1);}},null), "diamond_processed_steel_pickaxe",new HashMap<Character,ItemStack>(){{put('*', addItem(Material.IRON_PICKAXE, "鋼鉄ピッケル", 1, new HashMap<Enchantment,Integer>(){{put(Enchantment.DURABILITY,1); put(Enchantment.DIG_SPEED,1);}}, null)); put('+', new ItemStack(Material.DIAMOND));}});
        /* 鋼鉄シャベル + ダイヤモンド　->　ダイヤ加工済み鋼鉄シャベル */addNonCraftRecipe(addItem(Material.IRON_SHOVEL,"ダイヤ加工済み鋼鉄シャベル", 1,new HashMap<Enchantment,Integer>(){{put(Enchantment.DURABILITY,2); put(Enchantment.DIG_SPEED,2);put(Enchantment.LOOT_BONUS_BLOCKS,1);}},null), "diamond_processed_steel_shovel",new HashMap<Character,ItemStack>(){{put('*', addItem(Material.IRON_SHOVEL,"鋼鉄シャベル", 1,new HashMap<Enchantment,Integer>(){{put(Enchantment.DURABILITY,1); put(Enchantment.DIG_SPEED,1);}},null)); put('+', new ItemStack(Material.DIAMOND));}});
        /* 圧縮ダイヤモンド2個 + 棒1個　->　強化ダイヤモンド剣 */addCraftRecipe(addItem(Material.DIAMOND_SWORD,"強化ダイヤモンド剣", 1,new HashMap<Enchantment,Integer>(){{put(Enchantment.DURABILITY,3);put(Enchantment.DAMAGE_ALL,3);}},null), "reinforced_diamond_sword"," * "," * "," + ",new HashMap<Character,ItemStack>(){{put('*', addItem(Material.DIAMOND,"圧縮ダイヤモンド", 1,null,null));put('+', new ItemStack(Material.STICK));}});
        /* 圧縮ダイヤモンド3個 + 棒2個 -> 強化ダイヤモンドピッケル　*/addCraftRecipe(addItem(Material.DIAMOND_PICKAXE,"強化ダイヤモンドピッケル", 1,new HashMap<Enchantment,Integer>(){{put(Enchantment.DURABILITY,3);put(Enchantment.DIG_SPEED,3);}},null), "reinforced_diamond_pickaxe","***"," + "," + ",new HashMap<Character,ItemStack>(){{put('*', addItem(Material.DIAMOND,"圧縮ダイヤモンド", 1,null,null));put('+', new ItemStack(Material.STICK));}});
        /* 圧縮ダイヤモンド1個 + 棒2個　->　強化ダイヤモンドシャベル */addCraftRecipe(addItem(Material.DIAMOND_SHOVEL,"強化ダイヤモンドシャベル", 1,new HashMap<Enchantment,Integer>(){{put(Enchantment.DURABILITY,3);put(Enchantment.DIG_SPEED,3);}},null), "reinforced_diamond_shovel"," * "," + "," + ",new HashMap<Character,ItemStack>(){{put('*', addItem(Material.DIAMOND,"圧縮ダイヤモンド", 1,null,null));put('+', new ItemStack(Material.STICK));}});
        /* 圧縮ダイヤモンドブロック2個 + 棒1個　->　圧縮ダイヤモンド剣 */addCraftRecipe(addItem(Material.DIAMOND_SWORD,"圧縮ダイヤモンド剣", 1,new HashMap<Enchantment,Integer>(){{put(Enchantment.DURABILITY,5);put(Enchantment.DAMAGE_ALL,5);put(Enchantment.LOOT_BONUS_MOBS,3);}},null), "compressed_diamond_sword"," * "," * "," + ",new HashMap<Character,ItemStack>(){{put('*', addItem(Material.DIAMOND_BLOCK,"圧縮ダイヤモンドブロック", 1,null,null));put('+', new ItemStack(Material.STICK));}});
        /* 圧縮ダイヤモンドブロック3個 + 棒2個　-> 圧縮ダイヤモンドピッケル　*/addCraftRecipe(addItem(Material.DIAMOND_PICKAXE,"圧縮ダイヤモンドピッケル", 1,new HashMap<Enchantment,Integer>(){{put(Enchantment.DURABILITY,5); put(Enchantment.DIG_SPEED,5);put(Enchantment.LOOT_BONUS_BLOCKS,3);}},null), "compressed_diamond_pickaxe","***"," + "," + ",new HashMap<Character,ItemStack>(){{put('*', addItem(Material.DIAMOND_BLOCK,"圧縮ダイヤモンドブロック", 1,null,null));put('+', new ItemStack(Material.STICK));}});
        /* 圧縮ダイヤモンドブロック1個 + 棒2個　->　圧縮ダイヤモンドシャベル */addCraftRecipe(addItem(Material.DIAMOND_SHOVEL,"圧縮ダイヤモンドシャベル", 1,new HashMap<Enchantment,Integer>(){{put(Enchantment.DURABILITY,5); put(Enchantment.DIG_SPEED,5);put(Enchantment.LOOT_BONUS_BLOCKS,3);}},null), "compressed_diamond_shovel"," * "," + "," + ",new HashMap<Character,ItemStack>(){{put('*', addItem(Material.DIAMOND_BLOCK,"圧縮ダイヤモンドブロック", 1,null,null));put('+', new ItemStack(Material.STICK));}});
        /* 「まじで？」ブロック2個 + 棒1個　->　「もうやばいね！」剣 */addCraftRecipe(addItem(Material.DIAMOND_SWORD,"「もうやばいね！」剣", 1,new HashMap<Enchantment,Integer>(){{put(Enchantment.DURABILITY,10);put(Enchantment.SWEEPING_EDGE,10);put(Enchantment.LOOT_BONUS_MOBS,5);}},new HashMap<Attribute,Integer>(){{put(Attribute.GENERIC_MOVEMENT_SPEED,3);put(Attribute.GENERIC_ATTACK_SPEED,6);}}), "mouyabaine_sword"," * "," * "," + ",new HashMap<Character,ItemStack>(){{put('*', addItem(Material.BEDROCK,"「まじで？」ブロック", 1, null, null));put('+', new ItemStack(Material.STICK));}});
        /* 「まじで？」ブロック3個 + 棒2個　->　「もうやばいね！」ピッケル */addCraftRecipe(addItem(Material.DIAMOND_PICKAXE,"「もうやばいね！」ピッケル", 1,new HashMap<Enchantment,Integer>(){{put(Enchantment.DURABILITY,10); put(Enchantment.DIG_SPEED,10);put(Enchantment.LOOT_BONUS_BLOCKS,5);}},new HashMap<Attribute,Integer>(){{put(Attribute.GENERIC_MOVEMENT_SPEED,3);put(Attribute.GENERIC_MAX_HEALTH,6);}}), "mouyabaine_pickaxe","***"," + "," + ",new HashMap<Character,ItemStack>(){{put('*', addItem(Material.BEDROCK,"「まじで？」ブロック", 1, null, null));put('+', new ItemStack(Material.STICK));}});
        /* 「まじで？」ブロック1個 + 棒2個　->　「もうやばいね！」シャベル */addCraftRecipe(addItem(Material.DIAMOND_SHOVEL,"「もうやばいね！」シャベル", 1,new HashMap<Enchantment,Integer>(){{put(Enchantment.DURABILITY,10); put(Enchantment.DIG_SPEED,10);put(Enchantment.LOOT_BONUS_BLOCKS,5);}},new HashMap<Attribute,Integer>(){{put(Attribute.GENERIC_MOVEMENT_SPEED,3);put(Attribute.GENERIC_MAX_HEALTH,6);}}), "mouyabaine_shovel"," * "," + "," + ",new HashMap<Character,ItemStack>(){{put('*', addItem(Material.BEDROCK,"「まじで？」ブロック", 1, null, null));put('+', new ItemStack(Material.STICK));}});
        /* 圧縮ダイヤモンドブロック5個　->　圧縮ダイヤモンドヘルメット */addCraftRecipe(addItem(Material.DIAMOND_HELMET,"圧縮ダイヤモンドヘルメット", 1,new HashMap<Enchantment,Integer>(){{put(Enchantment.DURABILITY,5);put(Enchantment.PROTECTION_FIRE,3);put(Enchantment.PROTECTION_ENVIRONMENTAL,5);}},null), "compressed_diamond_helmet","***","* *","   ",new HashMap<Character,ItemStack>(){{put('*',addItem(Material.DIAMOND_BLOCK,"圧縮ダイヤモンドブロック", 1,null,null));}});
        /* 圧縮ダイヤモンドブロック8個　->　圧縮ダイヤモンドチェストプレート */addCraftRecipe(addItem(Material.DIAMOND_CHESTPLATE,"圧縮ダイヤモンドチェストプレート", 1,new HashMap<Enchantment,Integer>(){{put(Enchantment.DURABILITY,5);put(Enchantment.PROTECTION_FIRE,3);put(Enchantment.PROTECTION_ENVIRONMENTAL,5);}},null), "compressed_diamond_chestplate","* *","***","***",new HashMap<Character,ItemStack>(){{put('*',addItem(Material.DIAMOND_BLOCK,"圧縮ダイヤモンドブロック", 1,null,null));}});
        /* 圧縮ダイヤモンドブロック7個　->　圧縮ダイヤモンドレギンス */addCraftRecipe(addItem(Material.DIAMOND_LEGGINGS,"圧縮ダイヤモンドレギンス", 1,new HashMap<Enchantment,Integer>(){{put(Enchantment.DURABILITY,5);put(Enchantment.PROTECTION_FIRE,3);put(Enchantment.PROTECTION_ENVIRONMENTAL,5);}},null), "compressed_diamond_leggings","***","* *","* *",new HashMap<Character,ItemStack>(){{put('*',addItem(Material.DIAMOND_BLOCK,"圧縮ダイヤモンドブロック", 1,null,null));}});
        /* 圧縮ダイヤモンドブロック4個　->　圧縮ダイヤモンドブーツ */addCraftRecipe(addItem(Material.DIAMOND_BOOTS,"圧縮ダイヤモンドブーツ", 1,new HashMap<Enchantment,Integer>(){{put(Enchantment.DURABILITY,5);put(Enchantment.PROTECTION_FIRE,3);put(Enchantment.PROTECTION_ENVIRONMENTAL,5);}},null), "compressed_diamond_boots","   ","* *","* *",new HashMap<Character,ItemStack>(){{put('*',addItem(Material.DIAMOND_BLOCK,"圧縮ダイヤモンドブロック", 1,null,null));}});
        /* 圧縮ダイヤモンドヘルメット + エンダードラゴンの頭　->　暗視付き圧縮ダイヤモンドヘルメット */addNonCraftRecipe(addItem(Material.DIAMOND_HELMET,"暗視付き圧縮ダイヤモンドヘルメット", 1,new HashMap<Enchantment,Integer>(){{put(Enchantment.DURABILITY,5);put(Enchantment.PROTECTION_FIRE,3);put(Enchantment.PROTECTION_ENVIRONMENTAL,5);}},null), "compressed_diamond_leggings_with_night_vision",new HashMap<Character,ItemStack>(){{put('*',addItem(Material.DIAMOND_HELMET,"圧縮ダイヤモンドヘルメット", 1,new HashMap<Enchantment,Integer>(){{put(Enchantment.DURABILITY,5);put(Enchantment.PROTECTION_FIRE,3);put(Enchantment.PROTECTION_ENVIRONMENTAL,5);}},null));put('+', new ItemStack(Material.DRAGON_HEAD));}});
        /* 　->　 addCraftRecipe(addItem(Material,"",null,null),"","","",new HashMap<Character,ItemStack>(){{put('',)}});*/
    }

    ItemStack addItem(Material material, String name, int ammo,Map<Enchantment,Integer> enchantments, Map<Attribute,Integer> attributes){
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
        itemStack.setAmount(ammo);
        return itemStack;
    }

    void addCraftRecipe(ItemStack result, String key, String Recipe1, String Recipe2, String Recipe3, Map<Character,ItemStack> RecipeItem){

        ShapedRecipe shapedRecipe = new ShapedRecipe(new NamespacedKey(this, key), result);
        shapedRecipe.shape(Recipe1,Recipe2,Recipe3);
        for (Map.Entry<Character,ItemStack> entry : RecipeItem.entrySet()){
            RecipeChoice recipeChoice = new RecipeChoice.ExactChoice(entry.getValue());
            shapedRecipe.setIngredient(entry.getKey(), recipeChoice);
        }

        getServer().addRecipe(shapedRecipe);
    }

    void addNonCraftRecipe(ItemStack result, String key, Map<Character,ItemStack> RecipeItem){
        ShapelessRecipe shapedRecipe = new ShapelessRecipe(new NamespacedKey(this,key),result);
        for (Map.Entry<Character,ItemStack> entry : RecipeItem.entrySet()){
            RecipeChoice recipeChoice = new RecipeChoice.ExactChoice(entry.getValue());
            shapedRecipe.addIngredient(recipeChoice);
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

    private CoreProtectAPI getCoreProtect() {
        Plugin plugin = getServer().getPluginManager().getPlugin("CoreProtect");

        // Check that CoreProtect is loaded
        if (plugin == null || !(plugin instanceof CoreProtect)) {
            return null;
        }

        // Check that the API is enabled
        CoreProtectAPI CoreProtect = ((CoreProtect) plugin).getAPI();
        if (CoreProtect.isEnabled() == false) {
            return null;
        }

        // Check that a compatible version of the API is loaded
        if (CoreProtect.APIVersion() < 9) {
            return null;
        }

        return CoreProtect;
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