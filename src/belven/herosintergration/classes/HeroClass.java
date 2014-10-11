package belven.herosintergration.classes;

import java.util.HashMap;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import resources.ClassDrop;

public class HeroClass {

	public HeroClass(String name) {
		className = name;
	}

	public String className = "";
	public HashMap<ClassDrop, Material> classDrops = new HashMap<ClassDrop, Material>();

	public ClassDrop getDrop(ItemStack is) {
		if (containsMaterial(is.getType())) {
			for (ClassDrop cd : classDrops.keySet()) {
				if (cd.is.getType().equals(is.getType())) {
					return cd;
				}
			}
		}
		return null;
	}

	public ClassDrop getDrop(ClassDrop cd) {
		return getDrop(cd.is);
	}

	public boolean containsMaterial(Material m) {
		return classDrops.containsValue(m);
	}

	// TODO make checks for potion types and types of materials etc
	public void setClassDrop(ClassDrop cd) {
		ClassDrop tempCD = getDrop(cd);
		if (tempCD != null) {
			tempCD = cd;
		} else {
			classDrops.put(cd, cd.is.getType());
		}
	}

}
