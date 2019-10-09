package org.ctp.enchantmentsolution;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.logging.Level;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.ctp.enchantmentsolution.advancements.ESAdvancement;
import org.ctp.enchantmentsolution.advancements.ESAdvancementProgress;
import org.ctp.enchantmentsolution.commands.ConfigEdit;
import org.ctp.enchantmentsolution.commands.Debug;
import org.ctp.enchantmentsolution.commands.Enchant;
import org.ctp.enchantmentsolution.commands.EnchantInfo;
import org.ctp.enchantmentsolution.commands.Reload;
import org.ctp.enchantmentsolution.commands.RemoveEnchant;
import org.ctp.enchantmentsolution.commands.Reset;
import org.ctp.enchantmentsolution.commands.UnsafeEnchant;
import org.ctp.enchantmentsolution.database.SQLite;
import org.ctp.enchantmentsolution.enchantments.DefaultEnchantments;
import org.ctp.enchantmentsolution.inventory.InventoryData;
import org.ctp.enchantmentsolution.listeners.ChatMessage;
import org.ctp.enchantmentsolution.listeners.InventoryClick;
import org.ctp.enchantmentsolution.listeners.InventoryClose;
import org.ctp.enchantmentsolution.listeners.PlayerChatTabComplete;
import org.ctp.enchantmentsolution.listeners.PlayerInteract;
import org.ctp.enchantmentsolution.listeners.VanishListener;
import org.ctp.enchantmentsolution.listeners.VersionCheck;
import org.ctp.enchantmentsolution.listeners.abilities.AbilityPlayerRunnable;
import org.ctp.enchantmentsolution.listeners.abilities.BlockListener;
import org.ctp.enchantmentsolution.listeners.abilities.DamageListener;
import org.ctp.enchantmentsolution.listeners.abilities.DeathListener;
import org.ctp.enchantmentsolution.listeners.abilities.FishingListener;
import org.ctp.enchantmentsolution.listeners.abilities.MiscListener;
import org.ctp.enchantmentsolution.listeners.abilities.MiscRunnable;
import org.ctp.enchantmentsolution.listeners.abilities.PlayerListener;
import org.ctp.enchantmentsolution.listeners.abilities.ProjectileListener;
import org.ctp.enchantmentsolution.listeners.abilities.WalkerListener;
import org.ctp.enchantmentsolution.listeners.advancements.AdvancementEntityDeath;
import org.ctp.enchantmentsolution.listeners.advancements.AdvancementPlayerEvent;
import org.ctp.enchantmentsolution.listeners.advancements.AdvancementThread;
import org.ctp.enchantmentsolution.listeners.chestloot.ChestLootListener;
import org.ctp.enchantmentsolution.listeners.legacy.UpdateEnchantments;
import org.ctp.enchantmentsolution.listeners.mobs.MobSpawning;
import org.ctp.enchantmentsolution.listeners.mobs.Villagers;
import org.ctp.enchantmentsolution.listeners.vanilla.AnvilListener;
import org.ctp.enchantmentsolution.listeners.vanilla.EnchantmentListener;
import org.ctp.enchantmentsolution.listeners.vanilla.GrindstoneListener;
import org.ctp.enchantmentsolution.nms.animalmob.AnimalMob;
import org.ctp.enchantmentsolution.utils.AdvancementUtils;
import org.ctp.enchantmentsolution.utils.ChatUtils;
import org.ctp.enchantmentsolution.utils.ConfigUtils;
import org.ctp.enchantmentsolution.utils.Metrics;
import org.ctp.enchantmentsolution.utils.save.ConfigFiles;
import org.ctp.enchantmentsolution.utils.save.SaveUtils;
import org.ctp.enchantmentsolution.version.BukkitVersion;
import org.ctp.enchantmentsolution.version.PluginVersion;

public class EnchantmentSolution extends JavaPlugin {

	private static EnchantmentSolution PLUGIN;
	private List<InventoryData> inventories = new ArrayList<InventoryData>();
	private static List<ESAdvancementProgress> PROGRESS = new ArrayList<ESAdvancementProgress>();
	private static List<AnimalMob> ANIMALS = new ArrayList<AnimalMob>();
	private boolean disable = false, initialization = true;
	private SQLite db;
	private BukkitVersion bukkitVersion;
	private PluginVersion pluginVersion;
	private ConfigFiles files;
	private VersionCheck check;

	public void onEnable() {
		PLUGIN = this;
		bukkitVersion = new BukkitVersion();
		pluginVersion = new PluginVersion(this, getDescription().getVersion());
		if(!bukkitVersion.isVersionAllowed()) {
			Bukkit.getLogger().log(Level.WARNING, "Bukkit Version " + bukkitVersion.getVersion() + " is not compatible with this plugin. Please use a version that is compatible.");
			Bukkit.getPluginManager().disablePlugin(PLUGIN);
			return;
		}
		
		if (!getDataFolder().exists()) {
			getDataFolder().mkdirs();
		}
		
		db = new SQLite(this);
		db.load();
		DefaultEnchantments.addDefaultEnchantments();
		
		files = new ConfigFiles(this);
		files.createConfigFiles();
		
		if(disable) {
			Bukkit.getPluginManager().disablePlugin(PLUGIN);
			return;
		}

		SaveUtils.getData();
		
		registerEvent(new BlockListener());
		registerEvent(new DamageListener());
		registerEvent(new DeathListener());
		registerEvent(new FishingListener());
		registerEvent(new MiscListener());
		registerEvent(new PlayerListener());
		registerEvent(new ProjectileListener());
		registerEvent(new WalkerListener());
		registerEvent(new AdvancementEntityDeath());
		registerEvent(new AdvancementPlayerEvent());
		registerEvent(new org.ctp.enchantmentsolution.listeners.BlockListener());
		registerEvent(new ChatMessage());
		registerEvent(new ChestLootListener());
		registerEvent(new InventoryClick());
		registerEvent(new InventoryClose());
		registerEvent(new MobSpawning());
		registerEvent(new PlayerInteract());
		registerEvent(new UpdateEnchantments());
		registerEvent(new VanishListener());
		registerEvent(new Villagers());
		registerEvent(new AnvilListener());
		registerEvent(new EnchantmentListener());
		if(bukkitVersion.getVersionNumber() > 3) {
			registerEvent(new GrindstoneListener());
		}

		Bukkit.getScheduler().scheduleSyncRepeatingTask(PLUGIN,
				new AbilityPlayerRunnable(), 1l, 1l);
		Bukkit.getScheduler().scheduleSyncRepeatingTask(PLUGIN,
				new AdvancementThread(), 1l, 1l);
		Bukkit.getScheduler().scheduleSyncRepeatingTask(PLUGIN,
						new MiscRunnable(), 1l, 1l);
		Bukkit.getScheduler().scheduleSyncRepeatingTask(PLUGIN,
				new WalkerListener(), 1l, 1l);

		getCommand("Enchant").setExecutor(new Enchant());
		getCommand("Info").setExecutor(new EnchantInfo());
		getCommand("RemoveEnchant").setExecutor(new RemoveEnchant());
		getCommand("EnchantUnsafe").setExecutor(new UnsafeEnchant());
		getCommand("ESReload").setExecutor(new Reload());
		getCommand("ESConfig").setExecutor(new ConfigEdit());
		getCommand("ESReset").setExecutor(new Reset());
		getCommand("ESDebug").setExecutor(new Debug());
		getCommand("Enchant").setTabCompleter(new PlayerChatTabComplete());
		getCommand("Info").setTabCompleter(new PlayerChatTabComplete());
		getCommand("RemoveEnchant").setTabCompleter(new PlayerChatTabComplete());
		getCommand("EnchantUnsafe").setTabCompleter(new PlayerChatTabComplete());
				
		check = new VersionCheck(pluginVersion, "https://raw.githubusercontent.com/crashtheparty/EnchantmentSolution/master/VersionHistory", 
				"https://www.spigotmc.org/resources/enchantment-solution.59556/", "https://github.com/crashtheparty/EnchantmentSolution", 
				getConfigFiles().getDefaultConfig().getBoolean("version.get_latest"), getConfigFiles().getDefaultConfig().getBoolean("version.get_experimental"));
		registerEvent(check);
		checkVersion();
		initialization = false;
		
		AdvancementUtils.createAdvancements();
		
		Metrics metrics = new Metrics(this);
		
		metrics.addCustomChart(new Metrics.SingleLineChart("level_fifty", new Callable<Integer>() {
	        @Override
	        public Integer call() throws Exception {
	            if(ConfigUtils.isLevel50()) {
	            	return 1;
	            }
	            return 0;
	        }
	    }));
		
		metrics.addCustomChart(new Metrics.SingleLineChart("level_thirty", new Callable<Integer>() {
	        @Override
	        public Integer call() throws Exception {
	            if(!ConfigUtils.isLevel50()) {
	            	return 1;
	            }
	            return 0;
	        }
	    }));
		
		metrics.addCustomChart(new Metrics.SingleLineChart("advanced_file", new Callable<Integer>() {
	        @Override
	        public Integer call() throws Exception {
	            if(ConfigUtils.useAdvancedFile()) {
	            	return 1;
	            }
	            return 0;
	        }
	    }));
		
		metrics.addCustomChart(new Metrics.SingleLineChart("basic_file", new Callable<Integer>() {
	        @Override
	        public Integer call() throws Exception {
	            if(!ConfigUtils.useAdvancedFile()) {
	            	return 1;
	            }
	            return 0;
	        }
	    }));
		
		metrics.addCustomChart(new Metrics.SingleLineChart("enhanced_gui", new Callable<Integer>() {
	        @Override
	        public Integer call() throws Exception {
	            if(ConfigUtils.useESGUI()) {
	            	return 1;
	            }
	            return 0;
	        }
	    }));
		
		metrics.addCustomChart(new Metrics.SingleLineChart("vanilla_gui", new Callable<Integer>() {
	        @Override
	        public Integer call() throws Exception {
	            if(!ConfigUtils.useESGUI()) {
	            	return 1;
	            }
	            return 0;
	        }
	    }));
	}
	
	private void registerEvent(Listener l) {
		getServer().getPluginManager().registerEvents(l, this);
	}

	public void onDisable() {
		if(bukkitVersion.isVersionAllowed() && !disable) {
			try {
				resetInventories();
			} catch (Exception ex) {
				ex.printStackTrace();
			}
			try {
				SaveUtils.setAbilityData();
			} catch (NoClassDefFoundError ex) {
				ChatUtils.sendInfo("Found a no class def found error");
				ex.printStackTrace();
			}
		}
	}
	
	public void resetInventories() {
		for(int i = inventories.size() - 1; i >= 0; i--) {
			InventoryData inv = inventories.get(i);
			inv.close(true);
		}
	}
	
	public InventoryData getInventory(Player player) {
		for(InventoryData inv : inventories) {
			if(inv.getPlayer().getUniqueId().equals(player.getUniqueId())) {
				return inv;
			}
		}
		
		return null;
	}
	
	public boolean hasInventory(InventoryData inv) {
		return inventories.contains(inv);
	}
	
	public void addInventory(InventoryData inv) {
		inventories.add(inv);
	}
	
	public void removeInventory(InventoryData inv) {
		inventories.remove(inv);
	}
	
	private void checkVersion(){
		Bukkit.getScheduler().runTaskTimerAsynchronously(PLUGIN, check, 20l, 20 * 60 * 60 * 4l);
    }

	public SQLite getDb() {
		return db;
	}

	public BukkitVersion getBukkitVersion() {
		return bukkitVersion;
	}

	public PluginVersion getPluginVersion() {
		return pluginVersion;
	}

	public ConfigFiles getConfigFiles() {
		return files;
	}
	
	public static EnchantmentSolution getPlugin() {
		return PLUGIN;
	}

	public boolean isInitializing() {
		return initialization;
	}

	public static List<ESAdvancementProgress> getProgress() {
		return PROGRESS;
	}
	
	public static void addProgress(ESAdvancementProgress progress) {
		PROGRESS.add(progress);
	}
	
	public static ESAdvancementProgress getAdvancementProgress(OfflinePlayer player, ESAdvancement advancement, String criteria) {
		for(ESAdvancementProgress progress : EnchantmentSolution.getProgress()) {
			if(progress.getPlayer().equals(player) && progress.getAdvancement() == advancement && progress.getCriteria().equals(criteria)) {
				return progress;
			}
		}
		ESAdvancementProgress progress = new ESAdvancementProgress(advancement, criteria, 0, player);
		EnchantmentSolution.addProgress(progress);
		return progress;
	}
	
	public static List<ESAdvancementProgress> getAdvancementProgress(){
		List<ESAdvancementProgress> progress = new ArrayList<ESAdvancementProgress>();
		for(ESAdvancementProgress pr : PROGRESS) {
			progress.add(pr);
		}
		return progress;
	}

	public static void completed(ESAdvancementProgress esProgress) {
		PROGRESS.remove(esProgress);
	}

	public static boolean exists(Player player, ESAdvancement advancement, String criteria) {
		for(ESAdvancementProgress progress : PROGRESS) {
			if(progress.getPlayer().equals(player) && progress.getAdvancement() == advancement && progress.getCriteria().equals(criteria)) {
				return true;
			}
		}
		return false;
	}

	public static List<AnimalMob> getAnimals() {
		return ANIMALS;
	}

	public static void setAnimals(List<AnimalMob> animals) {
		ANIMALS = animals;
	}

	public static void addAnimals(AnimalMob mob) {
		ANIMALS.add(mob);
	}

	public static void removeAnimals(AnimalMob remove) {
		ANIMALS.remove(remove);
	}
	
	public void setVersionCheck(boolean getRelease, boolean getExperimental) {
		check.setLatestVersion(getRelease);
		check.setExperimentalVersion(getExperimental);
		check.run();
	}

}
