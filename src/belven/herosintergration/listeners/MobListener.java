package belven.herosintergration.listeners;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.ProjectileLaunchEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.metadata.MetadataValue;

import resources.ClassDrop;
import resources.EntityFunctions;
import resources.Functions;
import belven.arena.MDM;
import belven.arena.arenas.BaseArena;
import belven.herosintergration.HerosIntergrationManager;
import belven.herosintergration.classes.HeroClass;

public class MobListener implements Listener {
	private final HerosIntergrationManager plugin;

	public List<ClassDrop> classDrops = new ArrayList<ClassDrop>();

	Random randomGenerator = new Random();

	public MobListener(HerosIntergrationManager instance) {
		plugin = instance;
	}

	@EventHandler
	public void onProjectileLaunchEvent(ProjectileLaunchEvent event) {
		if (event.getEntity().getType() == EntityType.FIREBALL) {
			event.setCancelled(true);
		}
	}

	@EventHandler
	public void onEntityDeathEvent(EntityDeathEvent event) {
		Entity currentEntity = event.getEntity();
		List<MetadataValue> currentMetaData = MDM.getMetaData(MDM.ArenaMob,
				currentEntity);

		if (plugin.arenas != null && currentMetaData != null) {
			BaseArena currentArena = (BaseArena) currentMetaData.get(0);

			if (currentArena != null) {
				for (Player p : currentArena.arenaPlayers) {
					GiveClassDrops(p, false);
				}
			}
		} else {
			EntityDamageEvent cause = event.getEntity().getLastDamageCause();

			if (cause instanceof EntityDamageByEntityEvent) {
				LivingEntity e = EntityFunctions
						.GetDamager((EntityDamageByEntityEvent) cause);

				if (e.getType() == EntityType.PLAYER) {
					Player p = (Player) e;

					for (ItemStack is : event.getDrops()) {
						p.getInventory().addItem(is);
					}

					event.getDrops().clear();
					p.giveExp(event.getDroppedExp());
					event.setDroppedExp(0);
					GiveClassDrops(p, true);
				}
			}
		}
	}

	private void GiveClassDrops(Player p, boolean isWilderness) {
		int ran = randomGenerator.nextInt(99);
		PlayerInventory pInv = p.getInventory();

		// TODO Get Heros class
		HeroClass playerClass = new HeroClass("");// plugin.GetClass(p);

		for (ClassDrop cd : playerClass.classDrops.keySet()) {
			if (cd.alwaysGive
					|| Functions
							.numberBetween(ran, cd.lowChance, cd.highChance)
					&& cd.isWilderness == isWilderness) {
				if (!cd.isArmor) {
					ItemStack is = cd.is.clone();
					is.setAmount(cd.isWilderness ? cd.wildernessAmount : cd.is
							.getAmount());
					Functions.AddToInventory(p, is, cd.maxAmount);
				} else {
					if (AddArmor(pInv, cd.is)) {
						break;
					}
				}
			}
		}

	}

	public boolean AddArmor(PlayerInventory pInv, ItemStack is) {
		if (is.getType().name().contains("CHEST")
				&& pInv.getChestplate() == null) {
			pInv.setChestplate(is);
			return true;
		} else if (is.getType().name().contains("LEGGINGS")
				&& pInv.getLeggings() == null) {
			pInv.setLeggings(is);
			return true;
		} else if (is.getType().name().contains("HELMET")
				&& pInv.getHelmet() == null) {
			pInv.setHelmet(is);
			return true;
		} else if (is.getType().name().contains("BOOTS")
				&& pInv.getBoots() == null) {
			pInv.setBoots(is);
			return true;
		}
		return false;
	}
}