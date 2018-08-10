package org.ctp.enchantmentsolution.enchantments.level50.vanilla;

import org.apache.commons.lang3.StringUtils;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.ctp.enchantmentsolution.enchantments.CustomEnchantment;
import org.ctp.enchantmentsolution.utils.ItemUtils;

public class CurseOfBinding extends CustomEnchantment{
	
	public CurseOfBinding() {
		setTreasure(true);
	}
	
	@Override
	public Enchantment getRelativeEnchantment() {
		return Enchantment.BINDING_CURSE;
	}

	@Override
	public boolean canEnchantItem(Material item) {
		return false;
	}

	@Override
	public boolean canAnvilItem(Material item) {
		if(ItemUtils.getItemTypes().get("armor").contains(item)){
			return true;
		}
		if(ItemUtils.getItemTypes().get("elytra").contains(item)){
			return true;
		}
		if(item.equals(Material.BOOK)){
			return true;
		}
		return false;
	}

	@Override
	public boolean conflictsWith(CustomEnchantment ench) {
		if(ench.getName().equals(getName())) {
			return true;
		}
		return false;
	}

	@Override
	public int getMaxLevel() {
		return 1;
	}

	@Override
	public String getName() {
		// TODO Auto-generated method stub
		return "binding_curse";
	}

	@Override
	public String getDisplayName() {
		return "Curse of Binding";
	}

	@Override
	public int getStartLevel() {
		return 1;
	}

	@Override
	public int getWeight() {
		return 1;
	}

	@Override
	public int[] enchantability(int level) {
		int[] levels = new int[2];
		levels[0] = 25;
		levels[1] = 75;
		return levels;
	}
	
	@Override
	public String[] getPage() {
		String pageOne = "Name: " + getDisplayName() + StringUtils.LF + StringUtils.LF;
		pageOne += "Description: Prevents removal of the cursed item." + 
				StringUtils.LF + 
				"The cursed item cannot be removed from any armor slot (outside of Creative mode) unless the player dies or the item breaks." + StringUtils.LF;
		String pageTwo = "Max Level: " + getMaxLevel() + "."+ StringUtils.LF;
		pageTwo += "Weight: " + getWeight() + "."+ StringUtils.LF;
		pageTwo += "Start Level: " + getStartLevel() + "."+ StringUtils.LF;
		pageTwo += "Enchantable Items: None." + StringUtils.LF;
		pageTwo += "Anvilable Items: Armor, Elytra, Books." + StringUtils.LF;
		pageTwo += "Treasure Enchantment: " + isTreasure() + ". " + StringUtils.LF;
		return new String[] {pageOne, pageTwo};
	}

}
