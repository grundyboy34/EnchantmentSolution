package org.ctp.enchantmentsolution.nms;

import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.ctp.enchantmentsolution.nms.chest.ChestPopulate_v1_13_1_R1;
import org.ctp.enchantmentsolution.nms.chest.ChestPopulate_v1_13_R1;

public class ChestPopulateNMS {
	public static void populateChest(Block block) {
		switch(Version.VERSION_NUMBER) {
		case 1:
			ChestPopulate_v1_13_R1.populateChest(block);
			break;
		case 2:
			ChestPopulate_v1_13_1_R1.populateChest(block);
			break;
		}
	}
	
	public static boolean isLootChest(Block block) {
		switch(Version.VERSION_NUMBER) {
		case 1:
			return ChestPopulate_v1_13_R1.isLootChest(block);
		case 2:
			return ChestPopulate_v1_13_1_R1.isLootChest(block);
		}
		return false;
	}
	
	public static void populateCart(Entity e) {
		switch(Version.VERSION_NUMBER) {
		case 1:
			ChestPopulate_v1_13_R1.populateCart(e);
			break;
		case 2:
			ChestPopulate_v1_13_1_R1.populateCart(e);
			break;
		}
	}
	
	public static boolean isLootCart(Entity e) {
		switch(Version.VERSION_NUMBER) {
		case 1:
			return ChestPopulate_v1_13_R1.isLootCart(e);
		case 2:
			return ChestPopulate_v1_13_1_R1.isLootCart(e);
		}
		return false;
	}
}