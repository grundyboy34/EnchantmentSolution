package org.ctp.enchantmentsolution.enchantments;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.EnchantmentStorageMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.ctp.enchantmentsolution.enchantments.helper.CombineEnchantments;
import org.ctp.enchantmentsolution.enchantments.helper.EnchantmentLevel;
import org.ctp.enchantmentsolution.utils.ChatUtils;
import org.ctp.enchantmentsolution.utils.ConfigUtils;
import org.ctp.enchantmentsolution.utils.StringUtils;
import org.ctp.enchantmentsolution.utils.items.nms.ItemType;

public class Enchantments {

	private static List<CustomEnchantment> ENCHANTMENTS = new ArrayList<CustomEnchantment>();

	public static List<CustomEnchantment> getEnchantments() {
		return ENCHANTMENTS;
	}
	
	public static List<CustomEnchantment> getEnchantmentsAlphabetical(){
		List<CustomEnchantment> alphabetical = new ArrayList<CustomEnchantment>();
		for(CustomEnchantment enchantment : ENCHANTMENTS) {
			alphabetical.add(enchantment);
		}
		Collections.sort(alphabetical, new Comparator<CustomEnchantment>(){
			@Override
			public int compare(CustomEnchantment o1, CustomEnchantment o2) {
				return o1.getDisplayName().compareTo(o2.getDisplayName());
			}
		});
		return alphabetical;
	}

	public static boolean addEnchantment(CustomEnchantment enchantment) {
		if(ENCHANTMENTS.contains(enchantment)) {
			return true;
		}
		ENCHANTMENTS.add(enchantment);
		boolean custom = enchantment.getRelativeEnchantment() instanceof CustomEnchantmentWrapper;
		String error_message = "Trouble adding the " + enchantment.getName() + (custom ? " custom" : "") + " enchantment: ";
		String success_message = "Added the " + enchantment.getName() + (custom ? " custom" : "") + " enchantment.";
		if(!custom) {
			ChatUtils.sendToConsole(Level.INFO, success_message);
			return true;
		}
		try {
		    Field f = Enchantment.class.getDeclaredField("acceptingNew");
		    f.setAccessible(true);
		    f.set(null, true);
		    Enchantment.registerEnchantment(enchantment.getRelativeEnchantment());
		    ChatUtils.sendToConsole(Level.INFO, success_message);
			return true;
		} catch (Exception e) {
			ENCHANTMENTS.remove(enchantment);

			ChatUtils.sendToConsole(Level.WARNING, error_message);
		    e.printStackTrace();
		    return false;
		}
	}
	
	public static ItemStack convertToEnchantedBook(ItemStack item) {
		ItemStack newItem = new ItemStack(Material.ENCHANTED_BOOK, item.getAmount());
		EnchantmentStorageMeta enchantmentStorage = (EnchantmentStorageMeta) newItem.getItemMeta();
		
		ItemMeta meta = item.getItemMeta();
		
		if(meta != null && meta.getEnchants().size() > 0) {
			List<String> lore = new ArrayList<String>();
			for (Iterator<java.util.Map.Entry<Enchantment, Integer>> it = meta.getEnchants().entrySet().iterator(); it.hasNext();) {
				java.util.Map.Entry<Enchantment, Integer> e = it.next();
				Enchantment enchant = e.getKey();
				int level = e.getValue();
				enchantmentStorage.addStoredEnchant(enchant, level, true);
				if(enchant instanceof CustomEnchantmentWrapper) {
					String enchName = StringUtils.returnEnchantmentName(DefaultEnchantments.getCustomEnchantment(enchant), level);
					lore.add(ChatUtils.hideText("solution") + "" + ChatColor.GRAY + enchName);
				}
				meta.removeEnchant(enchant);
			}
			meta = enchantmentStorage;
			meta = Enchantments.setLore(meta, lore);
			newItem.setItemMeta(meta);
		}
		return newItem;
	}
	
	public static ItemStack convertToRegularBook(ItemStack item) {
		ItemStack newItem = new ItemStack(Material.BOOK, item.getAmount());
		EnchantmentStorageMeta enchantmentStorage = (EnchantmentStorageMeta) item.getItemMeta();
		
		ItemMeta meta = newItem.getItemMeta();
		
		if(enchantmentStorage != null && enchantmentStorage.getStoredEnchants().size() > 0) {
			List<String> lore = new ArrayList<String>();
			for (Iterator<java.util.Map.Entry<Enchantment, Integer>> it = enchantmentStorage.getStoredEnchants().entrySet().iterator(); it.hasNext();) {
				java.util.Map.Entry<Enchantment, Integer> e = it.next();
				Enchantment enchant = e.getKey();
				int level = e.getValue();
				meta.addEnchant(enchant, level, true);
				if(enchant instanceof CustomEnchantmentWrapper) {
					String enchName = StringUtils.returnEnchantmentName(DefaultEnchantments.getCustomEnchantment(enchant), level);
					lore.add(ChatUtils.hideText("solution") + "" + ChatColor.GRAY + enchName);
				}
				enchantmentStorage.removeStoredEnchant(enchant);
			}
			meta = Enchantments.setLore(meta, lore);
			newItem.setItemMeta(meta);
		}
		return newItem;
	}
	
	public static List<EnchantmentLevel> getEnchantmentLevels(ItemStack item){
		List<EnchantmentLevel> levels = new ArrayList<EnchantmentLevel>();
		if(item.getItemMeta() != null) {
			ItemMeta meta = item.getItemMeta();
			Map<Enchantment, Integer> enchantments = meta.getEnchants();
			if(item.getType() == Material.ENCHANTED_BOOK) {
				enchantments = ((EnchantmentStorageMeta) meta).getStoredEnchants();
			}
			for (Iterator<java.util.Map.Entry<Enchantment, Integer>> it = enchantments.entrySet().iterator(); it.hasNext();) {
				java.util.Map.Entry<Enchantment, Integer> e = it.next();
				levels.add(new EnchantmentLevel(DefaultEnchantments.getCustomEnchantment(e.getKey()), e.getValue()));
			}
		}
		return levels;
	}

	public static boolean isEnchantable(ItemStack item) {
		if (item == null) {
			return false;
		}
		ItemMeta meta = item.getItemMeta();
		if(item.getType() == Material.ENCHANTED_BOOK) {
			for(CustomEnchantment enchant : ENCHANTMENTS){
				if(((EnchantmentStorageMeta) meta).hasStoredEnchant(enchant.getRelativeEnchantment())){
					return false;
				}
			}
		} else {
			for(CustomEnchantment enchant : ENCHANTMENTS){
				if(meta.hasEnchant(enchant.getRelativeEnchantment())){
					return false;
				}
			}
		}
		if (ItemType.ALL.getItemTypes().contains(item.getType())) {
			return true;
		}
		if(item.getType().equals(Material.BOOK)){
			return true;
		}
		return false;
	}

	public static List<EnchantmentLevel> generateEnchantments(Player player,
			Material material, int level, int lapis, boolean treasure) {
		List<EnchantmentLevel> enchants = new ArrayList<EnchantmentLevel>();
		int enchantability = getEnchantability(material, level, lapis);
		double multiEnchantDivisor = ConfigUtils.getMultiEnchantDivisor();
		int totalWeight = 0;
		List<CustomEnchantment> customEnchants = new ArrayList<CustomEnchantment>();
		for(CustomEnchantment enchantment : ENCHANTMENTS){
			if(enchantment.isEnabled()) {
				if (treasure) {
					if(enchantment.canEnchant(player, enchantability, level) && enchantment.canEnchantItem(material)){
						totalWeight += enchantment.getWeight();
						customEnchants.add(enchantment);
					}
				} else {
					if(enchantment.canEnchant(player, enchantability, level) && enchantment.canEnchantItem(material) && !enchantment.isTreasure()){
						totalWeight += enchantment.getWeight();
						customEnchants.add(enchantment);
					}
				}
			}
		}
		int getWeight = (int)(Math.random() * totalWeight);
		for(CustomEnchantment customEnchant : customEnchants){
			getWeight -= customEnchant.getWeight();
			if(getWeight <= 0){
				enchants.add(new EnchantmentLevel(customEnchant, customEnchant.getEnchantLevel(player, enchantability)));
				break;
			}
		}
		int finalEnchantability = enchantability / 2;
		while(((finalEnchantability + 1) / multiEnchantDivisor) > Math.random()){
			totalWeight = 0;
			customEnchants = new ArrayList<CustomEnchantment>();
			for(CustomEnchantment enchantment : ENCHANTMENTS){
				boolean conflicts = false;
				for(EnchantmentLevel enchant : enchants) {
					if(CustomEnchantment.conflictsWith(enchant.getEnchant(), enchantment)) {
						conflicts = true;
					}
				}
				if(enchantment.isEnabled() && !conflicts) {
					if (treasure) {
						if(enchantment.canEnchant(player, enchantability, level) && enchantment.canEnchantItem(material)){
							totalWeight += enchantment.getWeight();
							customEnchants.add(enchantment);
						}
					} else {
						if(enchantment.canEnchant(player, enchantability, level) && enchantment.canEnchantItem(material) && !enchantment.isTreasure()){
							totalWeight += enchantment.getWeight();
							customEnchants.add(enchantment);
						}
					}
				}
			}
			if(totalWeight == 0){
				break;
			}
			getWeight = (int)(Math.random() * totalWeight);
			for(CustomEnchantment customEnchant : customEnchants){
				getWeight -= customEnchant.getWeight();
				if(getWeight <= 0){
					if(ConfigUtils.getEnchantabilityDecay()) {
						enchants.add(new EnchantmentLevel(customEnchant, customEnchant.getEnchantLevel(player, finalEnchantability)));
					} else {
						enchants.add(new EnchantmentLevel(customEnchant, customEnchant.getEnchantLevel(player, enchantability)));
					}
					break;
				}
			}
			finalEnchantability = finalEnchantability / 2;
		}
		int maxEnchants = ConfigUtils.getMaxEnchantments();
		if(maxEnchants > 0) {
			for(int i = enchants.size() - 1; i > maxEnchants; i--) {
				enchants.remove(i);
			}
		}
		return enchants;
	}

	public static int getBookshelves(Location loc) {
		int bookshelves = 0;
		for (int x = loc.getBlockX() - 2; x < loc.getBlockX() + 3; x++) {
			for (int y = loc.getBlockY(); y < loc.getBlockY() + 2; y++) {
				for (int z = loc.getBlockZ() - 2; z < loc.getBlockZ() + 3; z++) {
					if ((x == loc.getBlockX() - 2 || x == loc.getBlockX() + 2)
							|| (z == loc.getBlockZ() - 2 || z == loc
									.getBlockZ() + 2)) {
						Location bookshelf = new Location(loc.getWorld(), x, y,
								z);
						if (bookshelf.getBlock().getType()
								.equals(Material.BOOKSHELF)) {
							bookshelves++;
						}
					}
				}
			}
		}
		if (ConfigUtils.isLevel50()) {
			if (bookshelves > 23)
				bookshelves = 23;
		} else {
			if (bookshelves > 15)
				bookshelves = 15;
		}
		return bookshelves;
	}

	public static int getEnchantability(Material material, int level, int lapis) {
		int enchantability = 1;
		if (ItemType.WOODEN_TOOLS.getItemTypes().contains(material)) {
			enchantability = 15;
		} else if (ItemType.STONE_TOOLS.getItemTypes()
				.contains(material)) {
			enchantability = 5;
		} else if (ItemType.GOLDEN_TOOLS.getItemTypes()
				.contains(material)) {
			enchantability = 22;
		} else if (ItemType.IRON_TOOLS.getItemTypes()
				.contains(material)) {
			enchantability = 14;
		} else if (ItemType.DIAMOND_TOOLS.getItemTypes().contains(material)) {
			enchantability = 10;
		} else if (ItemType.LEATHER_ARMOR.getItemTypes().contains(material)) {
			enchantability = 15;
		} else if (ItemType.GOLDEN_ARMOR.getItemTypes().contains(material)) {
			enchantability = 25;
		} else if (ItemType.CHAINMAIL_ARMOR.getItemTypes().contains(material)) {
			enchantability = 12;
		} else if (ItemType.IRON_ARMOR.getItemTypes().contains(material)) {
			enchantability = 9;
		} else if (ItemType.DIAMOND_ARMOR.getItemTypes().contains(material)) {
			enchantability = 10;
		}

		int enchantability_2 = enchantability / 2;
		int rand_enchantability = 1 + randomInt(enchantability_2 / 2 + 1)
				+ randomInt(enchantability_2 / 2 + 1);

		if(ConfigUtils.useLapisLevels()) {
			rand_enchantability += (lapis - ConfigUtils.getLapisConstant()) * ConfigUtils.getLapisModifier();
		}
		
		int k = level + rand_enchantability;
		float rand_bonus_percent = (float) (1 + (randomFloat() + randomFloat() - 1) * .15);
		return (int) (k * rand_bonus_percent + 0.5);
	}

	private static int randomInt(int num) {
		double random = Math.random();

		return (int) (random * num);
	}

	private static float randomFloat() {
		return (float) Math.random();
	}
	
	public static ItemStack addEnchantmentsToItem(ItemStack item, List<EnchantmentLevel> levels){
		ItemMeta meta = item.getItemMeta();
		List<String> lore = new ArrayList<String>();
		List<String> previousLore = meta.getLore();
		if(levels == null) {
			if(previousLore != null) {
				for(String l : previousLore) {
					if(!StringUtils.isEnchantment(l)) {
						lore.add(l);
					}
				}
			}
			meta = Enchantments.setLore(meta, lore);
			item.setItemMeta(meta);
			return item;
		}
		if(item.getType() == Material.ENCHANTED_BOOK) {
			EnchantmentStorageMeta enchantmentStorage = (EnchantmentStorageMeta) meta;
			for(EnchantmentLevel level : levels){
				enchantmentStorage.addStoredEnchant(level.getEnchant().getRelativeEnchantment(), level.getLevel(), true);
				if(level.getEnchant().getRelativeEnchantment() instanceof CustomEnchantmentWrapper) {
					String enchName = StringUtils.returnEnchantmentName(level.getEnchant(), level.getLevel());
					lore.add(ChatUtils.hideText("solution") + "" + ChatColor.GRAY + enchName);
				}
			}
			meta = enchantmentStorage;
		} else {
			for(EnchantmentLevel level : levels){
				meta.addEnchant(level.getEnchant().getRelativeEnchantment(), level.getLevel(), true);
				if(level.getEnchant().getRelativeEnchantment() instanceof CustomEnchantmentWrapper) {
					String enchName = StringUtils.returnEnchantmentName(level.getEnchant(), level.getLevel());
					lore.add(ChatUtils.hideText("solution") + "" + ChatColor.GRAY + enchName);
				}
			}
		}
		if(previousLore != null) {
			for(String l : previousLore) {
				if(!StringUtils.isEnchantment(l)) {
					lore.add(l);
				}
			}
		}
		meta = Enchantments.setLore(meta, lore);
		item.setItemMeta(meta);
		return item;
	}

	public static ItemStack addEnchantmentToItem(ItemStack item, CustomEnchantment enchantment, int level){
		ItemMeta meta = item.getItemMeta();
		List<String> lore = meta.getLore();
		if(lore == null){
			lore = new ArrayList<String>();
		}
		if(item.getType() == Material.ENCHANTED_BOOK) {
			EnchantmentStorageMeta enchantmentStorage = (EnchantmentStorageMeta) meta;
			if(Enchantments.hasEnchantment(item, enchantment.getRelativeEnchantment())){
				lore = StringUtils.removeEnchantment(enchantment, meta.getEnchantLevel(enchantment.getRelativeEnchantment()), lore);
				enchantmentStorage.removeStoredEnchant(enchantment.getRelativeEnchantment());
			}
			enchantmentStorage.addStoredEnchant(enchantment.getRelativeEnchantment(), level, true);
			if(enchantment.getRelativeEnchantment() instanceof CustomEnchantmentWrapper) {
				String enchName = StringUtils.returnEnchantmentName(enchantment, level);
				lore.add(ChatUtils.hideText("solution") + "" + ChatColor.GRAY + enchName);
			}
			meta = enchantmentStorage;
		} else {
			if(Enchantments.hasEnchantment(item, enchantment.getRelativeEnchantment())){
				lore = StringUtils.removeEnchantment(enchantment, meta.getEnchantLevel(enchantment.getRelativeEnchantment()), lore);
				meta.removeEnchant(enchantment.getRelativeEnchantment());
			}
			meta.addEnchant(enchantment.getRelativeEnchantment(), level, true);
			if(enchantment.getRelativeEnchantment() instanceof CustomEnchantmentWrapper) {
				String enchName = StringUtils.returnEnchantmentName(enchantment, level);
				lore.add(ChatUtils.hideText("solution") + "" + ChatColor.GRAY + enchName);
			}
		}

		meta = Enchantments.setLore(meta, lore);
		item.setItemMeta(meta);
		return item;
	}
	
	public static ItemStack removeEnchantmentFromItem(ItemStack item, CustomEnchantment enchantment){
		ItemMeta meta = item.getItemMeta();
		List<String> lore = meta.getLore();
		if(lore == null){
			lore = new ArrayList<String>();
		}
		if(Enchantments.hasEnchantment(item, enchantment.getRelativeEnchantment())){
			if(enchantment.getRelativeEnchantment() instanceof CustomEnchantmentWrapper) {
				lore = StringUtils.removeEnchantment(enchantment, meta.getEnchantLevel(enchantment.getRelativeEnchantment()), lore);
			}
			if(meta instanceof EnchantmentStorageMeta) {
				((EnchantmentStorageMeta) meta).removeStoredEnchant(enchantment.getRelativeEnchantment());
			} else {
				meta.removeEnchant(enchantment.getRelativeEnchantment());
			}
		}
		meta = Enchantments.setLore(meta, lore);
		item.setItemMeta(meta);
		return item;
	}
	
	public static ItemStack removeAllEnchantments(ItemStack item) {
		ItemMeta meta = item.getItemMeta();
		List<String> lore = meta.getLore();
		if(lore == null){
			lore = new ArrayList<String>();
		}
		for(CustomEnchantment enchantment : DefaultEnchantments.getEnchantments()) {
			if(Enchantments.hasEnchantment(item, enchantment.getRelativeEnchantment())){
				if(enchantment.getRelativeEnchantment() instanceof CustomEnchantmentWrapper) {
					lore = StringUtils.removeEnchantment(enchantment, meta.getEnchantLevel(enchantment.getRelativeEnchantment()), lore);
				}
				if(meta instanceof EnchantmentStorageMeta) {
					((EnchantmentStorageMeta) meta).removeStoredEnchant(enchantment.getRelativeEnchantment());
				} else {
					meta.removeEnchant(enchantment.getRelativeEnchantment());
				}
			}
		}
		meta = Enchantments.setLore(meta, lore);
		item.setItemMeta(meta);
		return item;
	}
	
	public static boolean hasEnchantment(ItemStack item, Enchantment enchant){
		if(item.getItemMeta() != null) {
			Map<Enchantment, Integer> enchantments = item.getItemMeta().getEnchants();
			if(item.getType() == Material.ENCHANTED_BOOK) {
				enchantments = ((EnchantmentStorageMeta) item.getItemMeta()).getStoredEnchants();
			}
			for (Iterator<java.util.Map.Entry<Enchantment, Integer>> it = enchantments.entrySet().iterator(); it.hasNext();) {
				java.util.Map.Entry<Enchantment, Integer> e = it.next();
				if(e.getKey().equals(enchant)){
					return true;
				}
			}
		}
		return false;
	}
	
	public static int getTotalEnchantments(ItemStack item) {
		if(item.getItemMeta() != null) {
			ItemMeta meta = item.getItemMeta();
			Map<Enchantment, Integer> enchantments = meta.getEnchants();
			if(item.getType() == Material.ENCHANTED_BOOK) {
				enchantments = ((EnchantmentStorageMeta) meta).getStoredEnchants();
			}
			if(enchantments == null) return 0;
			return enchantments.size();
		}
		return 0;
	}
	
	public static int getLevel(ItemStack item, Enchantment enchant){
		if(item.getItemMeta() != null) {
			ItemMeta meta = item.getItemMeta();
			Map<Enchantment, Integer> enchantments = meta.getEnchants();
			if(item.getType() == Material.ENCHANTED_BOOK) {
				enchantments = ((EnchantmentStorageMeta) meta).getStoredEnchants();
			}
			for (Iterator<java.util.Map.Entry<Enchantment, Integer>> it = enchantments.entrySet().iterator(); it.hasNext();) {
				java.util.Map.Entry<Enchantment, Integer> e = it.next();
				if(e.getKey().equals(enchant)){
					return e.getValue();
				}
			}
		}
		return 0;
	}
	
	public static boolean canAddEnchantment(CustomEnchantment customEnchant, ItemStack item) {
		ItemMeta meta = item.clone().getItemMeta();
		Map<Enchantment, Integer> enchants = meta.getEnchants();
		if(customEnchant.getDisabledItems().contains(item.getType())) {
			return false;
		}
		if(item.getType().equals(Material.ENCHANTED_BOOK)) {
			enchants = ((EnchantmentStorageMeta) meta).getStoredEnchants();
		}else if(!customEnchant.canAnvilItem(item.getType())) {
			return false;
		}
		for (Iterator<java.util.Map.Entry<Enchantment, Integer>> it = enchants.entrySet().iterator(); it.hasNext();) {
			java.util.Map.Entry<Enchantment, Integer> e = it.next();
			Enchantment enchant = e.getKey();
			for(CustomEnchantment custom : Enchantments.ENCHANTMENTS) {
				if(custom.getRelativeEnchantment().equals(enchant)) {
					if(CustomEnchantment.conflictsWith(customEnchant, custom) && !customEnchant.getName().equals(custom.getName())) {
						return false;
					}
				}
			}
		}
		return true;
	}
	
	public static CombineEnchantments combineEnchantments(Player player, ItemStack first, ItemStack second){
		int cost = 0;
		ItemMeta firstMeta = first.clone().getItemMeta();
		Map<Enchantment, Integer> firstEnchants = firstMeta.getEnchants();
		if(first.getType().equals(Material.ENCHANTED_BOOK)) {
			EnchantmentStorageMeta meta = (EnchantmentStorageMeta) firstMeta;
			firstEnchants = meta.getStoredEnchants();
		}
		ItemMeta secondMeta = second.clone().getItemMeta();
		Map<Enchantment, Integer> secondEnchants = secondMeta.getEnchants();
		if(second.getType().equals(Material.ENCHANTED_BOOK)) {
			EnchantmentStorageMeta meta = (EnchantmentStorageMeta) secondMeta;
			secondEnchants = meta.getStoredEnchants();
		}
		List<EnchantmentLevel> secondLevels = new ArrayList<EnchantmentLevel>();
		List<EnchantmentLevel> firstLevels = new ArrayList<EnchantmentLevel>();
		
		List<EnchantmentLevel> enchantments = new ArrayList<EnchantmentLevel>();
		for (Iterator<java.util.Map.Entry<Enchantment, Integer>> it = secondEnchants.entrySet().iterator(); it.hasNext();) {
			java.util.Map.Entry<Enchantment, Integer> e = it.next();
			Enchantment enchant = e.getKey();
			int level = e.getValue();
			for(CustomEnchantment customEnchant : Enchantments.ENCHANTMENTS) {
				if(ConfigUtils.isRepairable(customEnchant) && customEnchant.getRelativeEnchantment().equals(enchant)) {
					secondLevels.add(new EnchantmentLevel(customEnchant, level));
				}
			}
		}
		
		for (Iterator<java.util.Map.Entry<Enchantment, Integer>> it = firstEnchants.entrySet().iterator(); it.hasNext();) {
			java.util.Map.Entry<Enchantment, Integer> e = it.next();
			Enchantment enchant = e.getKey();
			int level = e.getValue();
			for(CustomEnchantment customEnchant : Enchantments.ENCHANTMENTS) {
				if(ConfigUtils.isRepairable(customEnchant) && customEnchant.getRelativeEnchantment().equals(enchant)) {
					firstLevels.add(new EnchantmentLevel(customEnchant, level));
				}
			}
		}
		
		boolean godAnvil = player.hasPermission("enchantmentsolution.god-anvil");
		boolean demiGodAnvil = player.hasPermission("enchantmentsolution.demigod-anvil");
		if(godAnvil) {
			demiGodAnvil = true;
		}
		boolean demiGodBooks = demiGodAnvil && player.hasPermission("enchantmentsolution.demigod-books");
		
		for(EnchantmentLevel enchantTwo : secondLevels) {
			boolean conflict = false;
			boolean same = false;
			boolean canAdd = true;
			int levelCost = enchantTwo.getLevel();
			int originalLevel = -1;
			for(EnchantmentLevel enchantOne : firstLevels) {
				if(enchantTwo.getEnchant().getRelativeEnchantment().equals(enchantOne.getEnchant().getRelativeEnchantment())) {
					same = true;
					originalLevel = enchantOne.getLevel();
					if(!godAnvil) {
						if(enchantOne.getLevel() > enchantOne.getEnchant().getMaxLevel()) {
							enchantOne.setLevel(enchantOne.getEnchant().getMaxLevel());
						}
						if(enchantTwo.getLevel() > enchantTwo.getEnchant().getMaxLevel()) {
							enchantTwo.setLevel(enchantTwo.getEnchant().getMaxLevel());
						}
					}
					if(enchantTwo.getLevel() == enchantOne.getLevel()) {
						if (enchantTwo.getLevel() >= enchantTwo.getEnchant().getMaxLevel()) {
							levelCost = enchantTwo.getLevel();
						} else {
							levelCost = enchantTwo.getLevel() + 1;
						}
					} else if (enchantTwo.getLevel() > enchantOne.getLevel()) {
						levelCost = enchantTwo.getLevel();
					} else {
						levelCost = enchantOne.getLevel();
					}
				}else if(CustomEnchantment.conflictsWith(enchantOne.getEnchant(), enchantTwo.getEnchant())) {
					conflict = true;
				}
			}
			if(demiGodAnvil) {
				if(demiGodBooks && (second.getType() == Material.BOOK || second.getType() == Material.ENCHANTED_BOOK)) {
					// nothing needs to change
				} else if(!enchantTwo.getEnchant().canAnvil(player, levelCost) && originalLevel >= levelCost) {
					levelCost = originalLevel;
					if(!godAnvil && levelCost > enchantTwo.getEnchant().getMaxLevel()) {
						levelCost = enchantTwo.getEnchant().getMaxLevel();
					}
				} else if (!enchantTwo.getEnchant().canAnvil(player, levelCost)) {
					int level = enchantTwo.getEnchant().getAnvilLevel(player, levelCost);
					if(level <= 0) {
						canAdd = false;
					}
					levelCost = level;
				}
			} else {
				if(!enchantTwo.getEnchant().canAnvil(player, levelCost)) {
					int level = enchantTwo.getEnchant().getAnvilLevel(player, levelCost);
					if(level <= 0) {
						canAdd = false;
					}
					levelCost = level;
				}
			}
			
			if (canAdd && (same || !conflict)) {
				if (enchantTwo.getEnchant().canAnvilItem(first.getType()) || godAnvil) {
					enchantments.add(new EnchantmentLevel(enchantTwo.getEnchant(), levelCost));
					cost += levelCost * enchantTwo.getEnchant().multiplier(second.getType());
				} else {
					cost += 1;
				}
			} else if (godAnvil) {
				enchantments.add(new EnchantmentLevel(enchantTwo.getEnchant(), levelCost));
				cost += levelCost * enchantTwo.getEnchant().multiplier(second.getType());
			} else {
				cost += 1;
			}
		}
		
		for(EnchantmentLevel enchantOne : firstLevels) {
			boolean added = false;
			for(EnchantmentLevel enchantment : enchantments) {
				if(enchantOne.getEnchant().getRelativeEnchantment().equals(enchantment.getEnchant().getRelativeEnchantment())) {
					added = true; 
					break;
				}
			}
			if(!added && (enchantOne.getEnchant().canAnvilItem(first.getType()) || godAnvil)) {
				if(demiGodAnvil) {
					if(!godAnvil) {
						if(enchantOne.getLevel() > enchantOne.getEnchant().getMaxLevel()) {
							enchantOne.setLevel(enchantOne.getEnchant().getMaxLevel());
						}
					}
				} else {
					if(!enchantOne.getEnchant().canAnvil(player, enchantOne.getLevel())) {
						int level = enchantOne.getEnchant().getAnvilLevel(player, enchantOne.getLevel());
						if(level <= 0) {
							continue;
						}
					}
				}
				enchantments.add(enchantOne);
			}
		}
		int maxEnchants = ConfigUtils.getMaxEnchantments();
		if(maxEnchants > 0) {
			for(int i = enchantments.size() - 1; i > maxEnchants; i--) {
				enchantments.remove(i);
			}
		}
		
		return new CombineEnchantments(cost, enchantments);
	}
	
	public static ItemMeta setLore(ItemMeta meta, List<String> lore) {
		if(lore == null) {
			meta.setLore(new ArrayList<String>());
			return meta;
		}
		List<String> enchantmentsFirst = new ArrayList<String>();
		for(String l : lore) {
			if(StringUtils.isEnchantment(l)) {
				enchantmentsFirst.add(l);
			}
		}
		for(String l : lore) {
			if(!StringUtils.isEnchantment(l)) {
				enchantmentsFirst.add(l);
			}
		}
		meta.setLore(enchantmentsFirst);
		return meta;
	}

	public static int[] getEnchantabilityCalc(int enchantability, int constant, int modifier) {
		int enchantability_2 = enchantability / 2;
		int rand_enchantability = 1 + enchantability_2;
		
		int level = 1;
	
		int max = ((ConfigUtils.isLevel50() ? 50 : 30) + rand_enchantability);
		if(ConfigUtils.useLapisLevels()) {
			max += ((ConfigUtils.isLevel50() ? 6 : 3) - ConfigUtils.getLapisConstant()) * ConfigUtils.getLapisModifier();
		}
		max = (int) (max * 1.15 + 0.5);
		
		if(modifier <= 0) return new int[] {1, max};
		
		int maxEnchantability = modifier * level + constant;
		while (max > maxEnchantability + modifier) {
			level ++;
			maxEnchantability = modifier * level + constant;
		}
		
		return new int[] {level--, max};
	}
}
