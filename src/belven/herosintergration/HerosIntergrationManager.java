package belven.herosintergration;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Set;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.Potion;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.potion.PotionType;

import resources.ClassDrop;
import belven.arena.ArenaManager;
import belven.herosintergration.classes.HeroClass;

public class HerosIntergrationManager extends JavaPlugin {

	public ArenaManager arenas = null;
	HashMap<String, HeroClass> currentHeroClasses = new HashMap<String, HeroClass>();

	@Override
	public void onEnable() {
		loadClassDropsFromConfig();
		arenas = (ArenaManager) Bukkit.getServer().getPluginManager()
				.getPlugin("BelvensArenas");
	}

	// TODO Load drop data some how
	public void loadClassDropsFromConfig() {
		reloadConfig();
		for (String s : getConfig().getKeys(false)) {
			HeroClass newHeroClass = new HeroClass(s);
			// for (ItemStack is : GetItemsAtPath(s)) {
			//
			// }

			currentHeroClasses.put(s, newHeroClass);
		}
	}

	public String getItemPath(String heroClassName) {
		return heroClassName + ".Items";
	}

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label,
			String[] args) {
		Player p = (Player) sender;

		switch (args[0].toLowerCase()) {
		case "saveclassdrops":
		case "scd":
			saveClassDrops(p, args[1]);
		case "reloadfromconfig":
		case "rfc":
			currentHeroClasses.clear();
			loadClassDropsFromConfig();
		case "setdrop":
		case "sd":
			setSingleClassDrop(p, args);
		}
		return false;
	}

	private void setSingleClassDrop(Player p, String[] args) {
		String heroClassName = args[0];
		if (doesHeroClassExist(heroClassName)) {
			setClassDrop(p, getHeroClass(heroClassName), args);
		} else {
			p.sendMessage("This class does not exist.");
		}
	}

	// Examples
	// /hi sd true 10 - will always drop and stacks to max of 10, doesn't drop
	// in the wilderness

	// /hi sd true 10 4 - will always drop and stacks to max of 10 and a max of
	// 4 in the wilderness

	// /hi sd 10 20 30 - will drop if the random number is between 10 and 20
	// with a max stack of 30

	// /hi sd 10 20 30 10 - will drop if the random number is between 10 and 20
	// with a max stack of 30 and max of 10 in the wilderness

	private void setClassDrop(Player p, HeroClass hc, String[] args) {
		int length = args.length;
		if (length >= 3) {
			String isBoolOrInt = args[1];
			String knownInt = args[2];

			if (isBoolOrInt != null) {
				if (isBoolean(isBoolOrInt)) {
					boolean always = Boolean.valueOf(isBoolOrInt);
					int max = isInteger(knownInt) ? getInteger(knownInt) : 1;

					if (length < 4) {
						hc.setClassDrop(new ClassDrop(p.getItemInHand(),
								always, max));
					} else {
						int wilderMax = isInteger(args[3]) ? getInteger(args[3])
								: 1;
						hc.setClassDrop(new ClassDrop(p.getItemInHand(),
								always, max, wilderMax));
					}
				} else if (isInteger(isBoolOrInt)) {
					int low = isInteger(isBoolOrInt) ? getInteger(isBoolOrInt)
							: 1;
					int high = isInteger(knownInt) ? getInteger(knownInt) : 1;
					int max = isInteger(args[3]) ? getInteger(args[3]) : 1;

					if (length < 4) {
						hc.setClassDrop(new ClassDrop(p.getItemInHand(), low,
								high, max));
					} else {
						int wilderMax = isInteger(args[4]) ? getInteger(args[4])
								: 1;
						hc.setClassDrop(new ClassDrop(p.getItemInHand(), low,
								high, max, wilderMax));
					}
				}
			}
		}
	}

	public boolean isBoolean(String arg) {
		return arg.equalsIgnoreCase("true") || arg.equalsIgnoreCase("false");
	}

	public boolean isInteger(String arg) {
		return Integer.valueOf(arg) != null;
	}

	public int getInteger(String arg) {
		return Integer.valueOf(arg);
	}

	private void saveClassDrops(Player p, String heroClassName) {
		if (!doesHeroClassExist(heroClassName)) {
			setHeroClassItems(new HeroClass(heroClassName), p);
		} else {
			setHeroClassItems(getHeroClass(heroClassName), p);
		}
	}

	public void setHeroClassItems(HeroClass hc, Player p) {
		ListIterator<ItemStack> iter = p.getInventory().iterator();
		while (iter.hasNext()) {
			ItemStack is = iter.next();
			hc.setClassDrop(new ClassDrop(is, true, 1));
		}
	}

	public HeroClass getHeroClass(String heroClassName) {
		return currentHeroClasses.get(heroClassName);
	}

	public boolean doesHeroClassExist(String heroClassName) {
		return currentHeroClasses.containsKey(heroClassName);
	}

	private ItemStack AddItemEnchantments(ItemStack is, String Path) {
		Set<String> enchants = getConfig().getConfigurationSection(
				Path + ".Enchantments").getKeys(false);

		for (String et : enchants) {
			String enchantPath = Path + ".Enchantments." + et;
			int level = getConfig().getInt(enchantPath);
			Enchantment e = Enchantment.getByName(et);
			is.addEnchantment(e, level);
		}
		return is;
	}

	private ItemStack GetItemFromPath(String item, String Path) {
		ItemStack currentItem;
		String itemPath = Path + item;
		Material mat = Material.getMaterial(item);

		if (mat != null) {
			int amount = getConfig().getInt(itemPath + ".Amount");
			int durability = getConfig().getInt(itemPath + ".Durability");
			currentItem = new ItemStack(mat, amount > 0 ? amount : 1);
			currentItem.setDurability((short) durability);

			ConfigurationSection enchantsConfig = getConfig()
					.getConfigurationSection(itemPath + ".Enchantments");

			if (enchantsConfig != null) {
				currentItem = AddItemEnchantments(currentItem, itemPath);
			}
			return currentItem;
		}
		return new ItemStack(Material.AIR);
	}

	@SuppressWarnings("deprecation")
	private ItemStack AddPotionFromConfig(String pe, String itemPath) {
		PotionEffectType pet = PotionEffectType.getByName(pe);

		if (pet == null) {
			return null;
		}

		PotionType pt = PotionType.getByEffect(pet);

		if (pt != null) {
			String potionPath = itemPath + ".Potions." + pe;
			int Amplifier = getConfig().getInt(potionPath + ".Amplifier");
			boolean Splash = getConfig().getBoolean(potionPath + ".Splash");
			Potion p = new Potion(pt, Amplifier == 0 ? 1 : Amplifier, Splash);
			return p.toItemStack(1);
		}
		return null;
	}

	public void ItemStackToPath(ItemStack is, String Path) {
		if (is == null) {
			return;
		}

		if (is.getType() == Material.POTION) {
			SaveItemPotionEffect(is, Path);
		} else {
			Path += "." + is.getType().toString();
			getConfig().set(Path + ".Amount", is.getAmount());
			getConfig().set(Path + ".Durability", is.getDurability());

			if (is.getEnchantments().size() > 0) {
				SaveItemEnchantments(is, Path);
			}
		}
	}

	private void SaveItemEnchantments(ItemStack is, String currentPath) {
		Map<Enchantment, Integer> enchants = is.getEnchantments();

		if (!enchants.isEmpty()) {
			currentPath += ".Enchantments.";

			for (Enchantment e : enchants.keySet()) {
				getConfig().set(currentPath + e.getName(), enchants.get(e));
			}
		}
	}

	private void SaveItemPotionEffect(ItemStack is, String currentPath) {
		Potion p = Potion.fromItemStack(is);
		currentPath += ".Potions.";
		Collection<PotionEffect> fx = p.getEffects();
		for (PotionEffect pe : fx) {
			String path = currentPath + pe.getType().getName();
			getConfig().set(path + ".Amplifier", pe.getAmplifier());
			getConfig().set(path + ".Splash", p.isSplash());
		}
	}

	@SuppressWarnings("unused")
	private List<ItemStack> GetItemsAtPath(String Path) {
		List<ItemStack> tempItems = new ArrayList<ItemStack>();

		Set<String> items = getConfig().getConfigurationSection(Path).getKeys(
				false);

		for (String item : items) {
			if (item.equals("Potions")) {
				ConfigurationSection potionsConfig = getConfig()
						.getConfigurationSection(Path + ".Potions");
				if (potionsConfig != null) {
					for (String pe : potionsConfig.getKeys(false)) {
						tempItems.add(AddPotionFromConfig(pe, Path));
					}
				}
			} else {
				tempItems.add(GetItemFromPath(item, Path));
			}
		}
		return tempItems;
	}
}