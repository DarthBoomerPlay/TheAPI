package me.DevTec.Blocks;

import java.io.File;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.spigotmc.AsyncCatcher;

import com.google.common.collect.Lists;

import me.DevTec.ConfigAPI;
import me.DevTec.TheAPI;
import me.DevTec.Other.PercentageList;
import me.DevTec.Other.Position;
import me.DevTec.Other.Ref;
import me.DevTec.Other.StringUtils;
import me.DevTec.Other.TheMaterial;
import me.DevTec.Scheduler.Tasker;
import me.DevTec.Utils.Error;

public class BlocksAPI {
	private static interface Blocking {
		public void set(Position pos);
	}
	public static int amount = 500; //1000 blocks per 10 ticks, Can be changed: BlocksAPI.amount = <new value in int>

	private static void set(Shape form, Position where, int radius, Blocking task) {
		World w = where.getWorld();
		int Xx = where.getBlockX();
		int Yy = where.getBlockY();
		int Zz = where.getBlockZ();
		switch (form) {
		case Square:
			for (int x = Xx - radius; x <= Xx + radius; x++)
				for (int y = Yy - radius; y <= Yy + radius; y++)
					for (int z = Zz - radius; z <= Zz + radius; z++)
						task.set(new Position(w, x, y, z));
			break;
		case Sphere:
			for (int Y = -radius; Y < radius; Y++)
				for (int X = -radius; X < radius; X++)
					for (int Z = -radius; Z < radius; Z++)
						if (Math.sqrt((X * X) + (Y * Y) + (Z * Z)) <= radius)
							task.set(new Position(w, X + Xx, Y + Yy, Z + Zz));
		}
	}
	
	private static void set(Position from, Position to, Blocking task) {
		BlockGetter g = new BlockGetter(from,to);
		while(g.has())
			task.set(g.get());
	}
	
	public static enum Shape {
		Sphere, Square
	}
	
	public static Schemate getSchemate(String name) {
		return new Schemate(name);
	}

	public static String getLocationAsString(Location loc) {
		return StringUtils.getLocationAsString(loc);
	}

	public static Location getLocationFromString(String saved) {
		return StringUtils.getLocationFromString(saved);
	}

	public static List<Entity> getNearbyEntities(Location l, int radius) {
		return getNearbyEntities(new Position(l), radius);
	}

	public static List<Entity> getNearbyEntities(Position l, int radius) {
		if (radius > 256) {
			Error.err("getting nearby entities", "The radius cannot be greater than 256");
			return Lists.newArrayList();
		}
		int chunkRadius = radius < 16 ? 1 : (radius - (radius % 16)) / 16;
		List<Entity> radiusEntities = Lists.newArrayList();
		for (int chX = 0 - chunkRadius; chX <= chunkRadius; chX++)
			for (int chZ = 0 - chunkRadius; chZ <= chunkRadius; chZ++)
				for (Entity e : new Location(l.getWorld(), l.getX() + (chX * 16), l.getY(), l.getZ() + (chZ * 16)).getChunk().getEntities())
					if (l.distance(e.getLocation()) <= radius)
						radiusEntities.add(e);
		return radiusEntities;
	}

	public static List<Entity> getNearbyEntities(Entity ed, int radius) {
		return getNearbyEntities(new Position(ed.getLocation()), radius);
	}

	public static List<Entity> getNearbyEntities(World world, double x, double y, double z, int radius) {
		return getNearbyEntities(new Position(world, x, y, z), radius);
	}

	public static BlockSave getBlockSave(Position b) {
		return new BlockSave(b);
	}

	public static BlockGetter get(Location from, Location to) {
		return new BlockGetter(new Position(from), new Position(to));
	}

	public static BlockGetter get(Position from, Position to) {
		return new BlockGetter(from, to);
	}

	public static float count(Location from, Location to) {
		return count(new Position(from), new Position(to));
	}

	public static float count(Position from, Position to) {
		return new BigDecimal(""+(((from.getBlockX() < to.getBlockX() ? to.getBlockX() : from.getBlockX())-(from.getBlockX() > to.getBlockX() ? to.getBlockX() : from.getBlockX()))+1))
				.multiply(new BigDecimal(""+(((from.getBlockZ() < to.getBlockZ() ? to.getBlockZ() : from.getBlockZ())-(from.getBlockZ() > to.getBlockZ() ? to.getBlockZ() : from.getBlockZ()))+1)))
				.multiply(new BigDecimal(""+(((from.getBlockY() < to.getBlockY() ? to.getBlockY() : from.getBlockY())-(from.getBlockY() > to.getBlockY() ? to.getBlockY() : from.getBlockY()))+1))).floatValue();
	}

	public static List<Position> get(Position from, Position to, TheMaterial ignore) {
		return gt(from, to, Arrays.asList(ignore));
	}

	public static List<Position> get(Position from, Position to, List<TheMaterial> ignore) {
		return gt(from, to, ignore);
	}
	
	private static List<Position> gt(Position from, Position to, List<TheMaterial> ignore){
		List<Position> blocks = Lists.newArrayList();
		BlockGetter getter = get(from, to);
		while(getter.has()) {
			Position s = getter.get();
			if (ignore==null||!ignore.contains(s.getType()))
				blocks.add(s);
		}
		return blocks;
	}
	
	public static List<BlockSave> getBlockSaves(List<Position> a){
		List<BlockSave> b = Lists.newArrayList();
		for(Position s : a)b.add(getBlockSave(s));
		return b;
	}

	public static void set(Position loc, Material material) {
		set(loc,new TheMaterial(material));
	}

	public static void set(Block loc, Material material) {
		set(new Position(loc), new TheMaterial(material));
	}


	public static void set(Position loc, TheMaterial material) {
		if (!material.getType().isBlock())return;
		loc.setType(material);
	}

	public static void set(Block loc, TheMaterial material) {
		set(new Position(loc), material);
	}

	public static void set(Position loc, List<TheMaterial> material) {
		set(loc, (TheMaterial)TheAPI.getRandomFromList(material));
	}

	public static void set(Block loc, List<TheMaterial> material) {
		set(loc, (TheMaterial)TheAPI.getRandomFromList(material));
	}

	public static void set(Position loc, PercentageList<TheMaterial> material) {
		set(loc, material.getRandom());
	}

	public static void set(Block loc, PercentageList<TheMaterial> material) {
		set(new Position(loc), material);
	}

	public static void loadBlockSave(Position pos, BlockSave s) {
		s.load(pos,true);
	}

	public static void pasteBlockSave(Position pos, BlockSave s) {
		s.load(pos,true);
	}

	public static List<Position> get(Shape form, Position where, int radius) {
		return g(form, where, radius, null);
	}

	public static List<Position> get(Shape form, Position where, int radius, TheMaterial ignore) {
		return g(form, where, radius, Arrays.asList(ignore));
	}
	
	private static List<Position> g(Shape form, Position where, int radius, List<TheMaterial> ignore){
		List<Position> blocks = Lists.newArrayList();
		World w = where.getWorld();
		int Xx = where.getBlockX();
		int Yy = where.getBlockY();
		int Zz = where.getBlockZ();
		switch (form) {
		case Square:
			for (int x = Xx - radius; x <= Xx + radius; x++)
				for (int y = Yy - radius; y <= Yy + radius; y++)
					for (int z = Zz - radius; z <= Zz + radius; z++) {
						Position s = new Position(w, x, y, z);
						if (ignore==null||!ignore.contains(s.getType()))
							blocks.add(s);
					}
			break;
		case Sphere:
			for (int Y = -radius; Y < radius; Y++)
				for (int X = -radius; X < radius; X++)
					for (int Z = -radius; Z < radius; Z++)
						if (Math.sqrt((X * X) + (Y * Y) + (Z * Z)) <= radius) {
							Position s = new Position(w, X + Xx, Y + Yy, Z + Zz);
							if (ignore==null||!ignore.contains(s.getType()))
								blocks.add(s);
						}
		}
		return blocks;
	}

	public static List<Position> get(Shape form, Position where, int radius, List<TheMaterial> ignore) {
		return g(form, where, radius, ignore);
	}

	public static void replace(Position from, Position to, TheMaterial block, TheMaterial with) {
		set(from, to, new Blocking() {
			@Override
			public void set(Position pos) {
				if (pos.getType() == block)
					pos.setType(with);
			}
		});
	}

	public static void replace(Shape form, Position where, int radius, TheMaterial block, TheMaterial with) {
		set(form, where, radius, new Blocking() {
			@Override
			public void set(Position pos) {
				if (block == pos.getType())
					pos.setType(with);
			}
		});
	}

	public static void replace(Position from, Position to, TheMaterial block, List<TheMaterial> with) {
		set(from, to, new Blocking() {
			@Override
			public void set(Position pos) {
				if (block == pos.getType())
					pos.setType((TheMaterial) TheAPI.getRandomFromList(with));
			}
		});
	}

	public static void replace(Position from, Position to, TheMaterial block, PercentageList<TheMaterial> with) {
		set(from, to, new Blocking() {
			@Override
			public void set(Position pos) {
				if (block == pos.getType())
					pos.setType(with.getRandom());
			}
		});
	}

	public static void replace(Shape form, Position where, int radius, TheMaterial block, PercentageList<TheMaterial> with) {
		set(form, where, radius, new Blocking() {
			@Override
			public void set(Position pos) {
				if (block == pos.getType())
					pos.setType(with.getRandom());
			}
		});
	}

	public static void replace(Shape form, Position where, int radius, PercentageList<TheMaterial> block, TheMaterial with) {
		set(form, where, radius, new Blocking() {
			@Override
			public void set(Position pos) {
				if (block.contains(pos.getType()))
					if (TheAPI.generateChance(block.getChance(block.getRandom())))
					pos.setType(with);
			}
		});
	}

	public static void replace(Position from, Position to, PercentageList<TheMaterial> block, TheMaterial with) {
		set(from, to, new Blocking() {
			@Override
			public void set(Position pos) {
				if (block.contains(pos.getType()))
					if (TheAPI.generateChance(block.getChance(block.getRandom())))
					pos.setType(with);
			}
		});
	}

	public static void replace(Shape form, Position where, int radius, PercentageList<TheMaterial> block,
			PercentageList<TheMaterial> with) {
		set(form, where, radius, new Blocking() {
			@Override
			public void set(Position pos) {
				if (block.contains(pos.getType())) {
					TheMaterial random = block.getRandom();
					if (TheAPI.generateChance(block.getChance(random)))
					pos.setType(with.getRandom());
			}}
		});
	}

	public static void replace(Position from, Position to, PercentageList<TheMaterial> block,
			PercentageList<TheMaterial> with) {
		set(from, to, new Blocking() {
			@Override
			public void set(Position pos) {
				if (block.contains(pos.getType())) {
					TheMaterial random = block.getRandom();
					if (TheAPI.generateChance(block.getChance(random)))
					pos.setType(with.getRandom());
			}}
		});
	}

	public static void replace(Shape form, Position where, int radius, List<TheMaterial> block, TheMaterial with) {
		set(form, where, radius, new Blocking() {
			@Override
			public void set(Position pos) {
				if (block.contains(pos.getType())) 
					pos.setType(with);
			}
		});
	}

	public static void replace(Shape form, Position where, int radius, List<TheMaterial> block, List<TheMaterial> with) {
		set(form, where, radius, new Blocking() {
			@Override
			public void set(Position pos) {
				if (block.contains(pos.getType())) 
					pos.setType((TheMaterial)TheAPI.getRandomFromList(with));
			}
		});
	}

	public static void replace(Position from, Position to, List<TheMaterial> block, TheMaterial with) {
		set(from, to, new Blocking() {
			@Override
			public void set(Position pos) {
				if(block.contains(pos.getType()))
				pos.setType(with);
			}
		});
	}

	public static void replace(Position from, Position to, List<TheMaterial> block, List<TheMaterial> with) {
		set(from, to, new Blocking() {
			@Override
			public void set(Position pos) {
				if(block.contains(pos.getType()))
				pos.setType((TheMaterial) TheAPI.getRandomFromList(with));
			}
		});
	}

	public static void set(Shape form, Position where, int radius, TheMaterial block) {
		set(form, where, radius, new Blocking() {
			@Override
			public void set(Position pos) {
				pos.setType(block);
			}
		});
	}

	public static void set(Shape form, Position where, int radius, TheMaterial block, List<TheMaterial> ignore) {
		set(form, where, radius, new Blocking() {
			@Override
			public void set(Position pos) {
				if(!ignore.contains(pos.getType()))
				pos.setType(block);
			}
		});
	}

	public static void set(Shape form, Position where, int radius, TheMaterial block, TheMaterial ignore) {
		set(form, where, radius, new Blocking() {
			@Override
			public void set(Position pos) {
				if(ignore!=pos.getType())
				pos.setType(block);
			}
		});
	}

	public static void set(Shape form, Position where, int radius, List<TheMaterial> block) {
		set(form, where, radius, new Blocking() {
			@Override
			public void set(Position pos) {
				pos.setType((TheMaterial) TheAPI.getRandomFromList(block));
			}
		});
	}

	public static void set(Shape form, Position where, int radius, List<TheMaterial> block, List<TheMaterial> ignore) {
		set(form, where, radius, new Blocking() {
			@Override
			public void set(Position pos) {
				if(!ignore.contains(pos.getType()))
				pos.setType((TheMaterial) TheAPI.getRandomFromList(block));
			}
		});
	}

	public static void set(Shape form, Position where, int radius, List<TheMaterial> block, TheMaterial ignore) {
		set(form, where, radius, new Blocking() {
			@Override
			public void set(Position pos) {
				if(ignore!=pos.getType())
				pos.setType((TheMaterial) TheAPI.getRandomFromList(block));
			}
		});
	}

	public static void set(Shape form, Position where, int radius, PercentageList<TheMaterial> block) {
		set(form, where, radius, new Blocking() {
			@Override
			public void set(Position pos) {
				pos.setType(block.getRandom());
			}
		});
	}

	public static void set(Shape form, Position where, int radius, PercentageList<TheMaterial> block,
			List<TheMaterial> ignore) {
		set(form, where, radius, new Blocking() {
			@Override
			public void set(Position pos) {
				if (!ignore.contains(pos.getType()))
					pos.setType(block.getRandom());
			}
		});
	}

	public static void set(Shape form, Position where, int radius, PercentageList<TheMaterial> block, TheMaterial ignore) {
		set(form, where, radius, new Blocking() {
			@Override
			public void set(Position pos) {
				if (ignore!=pos.getType())
					pos.setType(block.getRandom());
			}
		});
	}

	public static void set(Position from, Position to, TheMaterial block) {
		set(from, to, new Blocking() {
			@Override
			public void set(Position pos) {
				pos.setType(block);
			}
		});
	}

	public static void set(Position from, Position to, TheMaterial block, List<TheMaterial> ignore) {
		set(from, to, new Blocking() {
			@Override
			public void set(Position pos) {
				if(!ignore.contains(pos.getType()))
					pos.setType(block);
			}
		});
	}

	public static void set(Position from, Position to, TheMaterial block, TheMaterial ignore) {
		set(from, to, new Blocking() {
			@Override
			public void set(Position pos) {
				if(ignore!=pos.getType())
					pos.setType(block);
			}
		});
	}

	public static void set(Position from, Position to, List<TheMaterial> block) {
		set(from, to, new Blocking() {
			@Override
			public void set(Position pos) {
				pos.setType((TheMaterial) TheAPI.getRandomFromList(block));
			}
		});
	}

	public static void set(Position from, Position to, List<TheMaterial> block, List<TheMaterial> ignore) {
		set(from, to, new Blocking() {
			@Override
			public void set(Position pos) {
				if(!ignore.contains(pos.getType()))
					pos.setType((TheMaterial) TheAPI.getRandomFromList(block));
			}
		});
	}

	public static void set(Position from, Position to, List<TheMaterial> block, TheMaterial ignore) {
		set(from, to, new Blocking() {
			@Override
			public void set(Position pos) {
				if (ignore != pos.getType())
					pos.setType((TheMaterial) TheAPI.getRandomFromList(block));
			}
		});
	}

	public static void set(Position from, Position to, PercentageList<TheMaterial> block) {
		set(from, to, new Blocking() {
			@Override
			public void set(Position pos) {
				pos.setType(block.getRandom());
			}
		});
	}

	public static void set(Position from, Position to, PercentageList<TheMaterial> block, List<TheMaterial> ignore) {
		set(from, to, new Blocking() {
			@Override
			public void set(Position pos) {
				if(!ignore.contains(pos.getType()))
				pos.setType(block.getRandom());
			}
		});
	}

	public static void set(Position from, Position to, PercentageList<TheMaterial> block, TheMaterial ignore) {
		set(from, to, new Blocking() {
			@Override
			public void set(Position pos) {
				if(ignore!=pos.getType())
				pos.setType(block.getRandom());
			}
		});
	}

	public static boolean isInside(Entity entity, Position a, Position b) {
		return isInside(new Position(entity.getLocation()), a, b);
	}

	public static boolean isInside(Position loc, Position a, Position b) {
		int xMin = Math.min(a.getBlockX(), b.getBlockX());
		int yMin = Math.min(a.getBlockY(), b.getBlockY());
		int zMin = Math.min(a.getBlockZ(), b.getBlockZ());
		int xMax = Math.max(a.getBlockX(), b.getBlockX());
		int yMax = Math.max(a.getBlockY(), b.getBlockY());
		int zMax = Math.max(a.getBlockZ(), b.getBlockZ());
		return loc.getWorld() == a.getWorld() && loc.getBlockX() >= xMin && loc.getBlockX() <= xMax 
				&& loc.getBlockY() >= yMin && loc.getBlockY() <= yMax && loc
		       .getBlockZ() >= zMin && loc.getBlockZ() <= zMax;
	}

	public static boolean isInside(Entity entity, Location a, Location b) {
		return isInside(entity.getLocation(), a, b);
	}

	public static boolean isInside(Location loc, Location a, Location b) {
		return isInside(new Position(loc),new Position(a),new Position(b));
	}

	// Synchronized part
	public static void synchronizedSet(Position a, Position b, TheMaterial with) {
		synchronizedSet(a, b, new Runnable() {public void run() {}}, with);
	}

	public static void synchronizedSet(Position a, Position b, TheMaterial with, TheMaterial ignore) {
		synchronizedSet(a, b, new Runnable() {public void run() {}}, with, ignore);
	}

	public static void synchronizedSet(Position a, Position b, TheMaterial with, List<TheMaterial> ignore) {
		synchronizedSet(a, b, new Runnable() {public void run() {}}, with, ignore);
	}

	public static void synchronizedSet(Position a, Position b, List<TheMaterial> with, List<TheMaterial> ignore) {
		synchronizedSet(a, b, new Runnable() {public void run() {}}, with, ignore);
	}

	public static void synchronizedSet(Position a, Position b, List<TheMaterial> with) {
		synchronizedSet(a, b, new Runnable() {public void run() {}}, with);
	}

	public static void synchronizedSet(Position a, Position b, PercentageList<TheMaterial> with, List<TheMaterial> ignore) {
		synchronizedSet(a, b, new Runnable() {public void run() {}}, with, ignore);
	}

	public static void synchronizedSet(Position a, Position b, PercentageList<TheMaterial> with, TheMaterial ignore) {
		synchronizedSet(a, b, new Runnable() {public void run() {}}, with, ignore);
	}

	public static void synchronizedReplace(Position a, Position b, List<TheMaterial> block, TheMaterial with) {
		synchronizedReplace(a, b, new Runnable() {public void run() {}}, block, with);
	}

	public static void synchronizedReplace(Position a, Position b, TheMaterial block, TheMaterial with) {
		synchronizedReplace(a, b, new Runnable() {public void run() {}}, block, with);
	}

	public static void synchronizedReplace(Position a, Position b, TheMaterial block, PercentageList<TheMaterial> with) {
		synchronizedReplace(a, b, new Runnable() {public void run() {}}, block, with);
	}

	public static void synchronizedReplace(Position a, Position b, TheMaterial block, List<TheMaterial> with) {
		synchronizedReplace(a, b, new Runnable() {public void run() {}}, block, with);
	}

	public static void synchronizedReplace(Position a, Position b, List<TheMaterial> block, List<TheMaterial> with) {
		synchronizedReplace(a, b, new Runnable() {public void run() {}}, block, with);
	}

	public static void synchronizedReplace(Position a, Position b, PercentageList<TheMaterial> block,
			List<TheMaterial> with) {
		synchronizedReplace(a, b, new Runnable() {public void run() {}}, block, with);
	}

	public static void synchronizedReplace(Position a, Position b, PercentageList<TheMaterial> block,
			PercentageList<TheMaterial> with) {
		synchronizedReplace(a, b, new Runnable() {public void run() {}}, block, with);
	}

	public static void synchronizedReplace(Position a, Position b, PercentageList<TheMaterial> block, TheMaterial with) {
		synchronizedReplace(a, b, new Runnable() {public void run() {}}, block, with);
	}

	private final static BlockTask state = new BlockTask() {
		@Override
		public long set(Position block, TheMaterial toSet) {
			return block.setType(toSet);
		}
		
		@Override
		public long replace(Position block, List<TheMaterial> toReplace, TheMaterial toSet) {
			if(toReplace.contains(get(block)))
				return block.setType(toSet);
			return 0;
		}
		
		@Override
		public long replace(Position block, TheMaterial toReplace, TheMaterial toSet) {
			if(toReplace.equals(get(block)))
				return block.setType(toSet);
			return 0;
		}
		
		@Override
		public TheMaterial get(Position block) {
			return block.getType();
		}

		@Override
		public long set(Position block, TheMaterial toSet, TheMaterial ignore) {
			if(!ignore.equals(get(block)))
				return block.setType(toSet);
			return 0;
		}

		@Override
		public long set(Position block, TheMaterial toSet, List<TheMaterial> ignore) {
			if(!ignore.contains(get(block)))
				return block.setType(toSet);
			return 0;
		}
	};
	
	//Synchronized & Runnable on finish part
	public static void synchronizedSet(Position a, Position b, Runnable onFinish, TheMaterial with) {
		synchronizedSet(a, b, onFinish, Arrays.asList(with), Arrays.asList());
	}

	public static void synchronizedSet(Position a, Position b, Runnable onFinish, TheMaterial with, TheMaterial ignore) {
		synchronizedSet(a, b, onFinish, Arrays.asList(with), Arrays.asList(ignore));
	}

	public static void synchronizedSet(Position a, Position b, Runnable onFinish, TheMaterial with, List<TheMaterial> ignore) {
		synchronizedSet(a, b, onFinish, Arrays.asList(with), ignore);
	}

	public static void synchronizedSet(Position a, Position b, Runnable onFinish, List<TheMaterial> with, List<TheMaterial> ignore) {
		BlockGetter s = get(a, b);
		ConfigAPI caw = null;
		for(int iaa = 0; iaa > -1; ++iaa) {
			if(!new File("TheAPI/ChunkTask/"+iaa).exists()) {
				caw = new ConfigAPI("TheAPI/ChunkTask", ""+iaa);
				caw.create();
				break;
			}
		}
		ConfigAPI ca = caw;
		new Tasker() {
			@Override
			public void run() {
				for(int i = 0; i < amount; ++i) {
					if (s.has()) {
						Position get = s.get();
						long key = ignore!=null?state.set(get, (TheMaterial)TheAPI.getRandomFromList(with), ignore):state.set(get, (TheMaterial)TheAPI.getRandomFromList(with));
						ca.set(key+"", new int[] {get.getBlockX()>>4,get.getBlockZ()>>4});
					} else
						break;
				}
				if (!s.has()) {
					cancel();
					Object w = Ref.world(a.getWorld());
					for(String o : ca.getKeys(false)) {
						int[] cw = (int[])ca.get(o);
						Object a=Ref.newInstanceNms("PacketPlayOutMapChunk", Ref.invoke(w, "getChunkAt", cw[0], cw[1]), 65535);
						for(Player p : Bukkit.getOnlinePlayers())
							Ref.sendPacket(p, a);
					}
					if(onFinish!=null)
					onFinish.run();
				}
			}
		}.repeating(0, 3);
	}

	public static void synchronizedSet(Position a, Position b, Runnable onFinish, List<TheMaterial> with) {
		synchronizedSet(a,b,onFinish, with, Arrays.asList());
	}

	public static void synchronizedSet(Position a, Position b, Runnable onFinish, PercentageList<TheMaterial> with, List<TheMaterial> ignore) {
		BlockGetter s = get(a, b);
		ConfigAPI caw = null;
		for(int iaa = 0; iaa > -1; ++iaa) {
			if(!new File("TheAPI/ChunkTask/"+iaa).exists()) {
				caw = new ConfigAPI("TheAPI/ChunkTask", ""+iaa);
				caw.create();
				break;
			}
		}
		ConfigAPI ca = caw;
		new Tasker() {
			@Override
			public void run() {
				for(int i = 0; i < amount; ++i) {
					if (s.has()) {
						Position get = s.get();
						long key = ignore!=null?state.set(get, with.getRandom(), ignore):state.set(get, with.getRandom());
						ca.set(key+"", new int[] {get.getBlockX()>>4,get.getBlockZ()>>4});
					} else
						break;
				}
				if (!s.has()) {
					cancel();
					Object w = Ref.world(a.getWorld());
					for(String o : ca.getKeys(false)) {
						int[] cw = (int[])ca.get(o);
						Object a=Ref.newInstanceNms("PacketPlayOutMapChunk", Ref.invoke(w, "getChunkAt", cw[0], cw[1]), 65535);
						for(Player p : Bukkit.getOnlinePlayers())
							Ref.sendPacket(p, a);
					}
					if(onFinish!=null)
					onFinish.run();
				}
			}
		}.repeating(0, 3);
	}

	public static void synchronizedSet(Position a, Position b, Runnable onFinish, PercentageList<TheMaterial> with, TheMaterial ignore) {
		synchronizedSet(a, b, onFinish, with, Arrays.asList(ignore));
	}

	public static void synchronizedReplace(Position a, Position b, Runnable onFinish, List<TheMaterial> block, TheMaterial with) {
		synchronizedReplace(a, b, onFinish, block, Arrays.asList(with));
	}

	public static void synchronizedReplace(Position a, Position b, Runnable onFinish, TheMaterial block, TheMaterial with) {
		synchronizedReplace(a, b, onFinish, Arrays.asList(block), Arrays.asList(with));
	}

	public static void synchronizedReplace(Position a, Position b, Runnable onFinish, TheMaterial block, PercentageList<TheMaterial> with) {
		synchronizedReplace(a,b,onFinish, Arrays.asList(block), with);
	}
	
	public static void synchronizedReplace(Position a, Position b, Runnable onFinish, List<TheMaterial> block, PercentageList<TheMaterial> with) {
		BlockGetter s = get(a, b);
		ConfigAPI caw = null;
		for(int iaa = 0; iaa > -1; ++iaa) {
			if(!new File("TheAPI/ChunkTask/"+iaa).exists()) {
				caw = new ConfigAPI("TheAPI/ChunkTask", ""+iaa);
				caw.create();
				break;
			}
		}
		ConfigAPI ca = caw;
		new Tasker() {
			@Override
			public void run() {
				for(int i = 0; i < amount; ++i) {
					if (s.has()) {
						Position get = s.get();
						long key = state.replace(get, block, with.getRandom());
						ca.set(key+"", new int[] {get.getBlockX()>>4,get.getBlockZ()>>4});
					} else
						break;
				}
				if (!s.has()) {
					cancel();
					Object w = Ref.world(a.getWorld());
					for(String o : ca.getKeys(false)) {
						int[] cw = (int[])ca.get(o);
						Object a=Ref.newInstanceNms("PacketPlayOutMapChunk", Ref.invoke(w, "getChunkAt", cw[0], cw[1]), 65535);
						for(Player p : Bukkit.getOnlinePlayers())
							Ref.sendPacket(p, a);
					}
					if(onFinish!=null)
					onFinish.run();
				}
			}
		}.repeating(0, 3);
	}

	public static void synchronizedReplace(Position a, Position b, Runnable onFinish, TheMaterial block, List<TheMaterial> with) {
		synchronizedReplace(a, b, onFinish, Arrays.asList(block), with);
	}

	public static void synchronizedReplace(Position a, Position b, Runnable onFinish, List<TheMaterial> block, List<TheMaterial> with) {
		BlockGetter s = get(a, b);
		ConfigAPI caw = null;
		for(int iaa = 0; iaa > -1; ++iaa) {
			if(!new File("TheAPI/ChunkTask/"+iaa).exists()) {
				caw = new ConfigAPI("TheAPI/ChunkTask", ""+iaa);
				caw.create();
				break;
			}
		}
		ConfigAPI ca = caw;
		new Tasker() {
			@Override
			public void run() {
				for(int i = 0; i < amount; ++i) {
					if (s.has()) {
						Position get = s.get();
						long key = state.replace(get, block, (TheMaterial)TheAPI.getRandomFromList(with));
						ca.set(key+"", new int[] {get.getBlockX()>>4,get.getBlockZ()>>4});
					} else
						break;
				}
				if (!s.has()) {
					cancel();
					Object w = Ref.world(a.getWorld());
					for(String o : ca.getKeys(false)) {
						int[] cw = (int[])ca.get(o);
						Object a=Ref.newInstanceNms("PacketPlayOutMapChunk", Ref.invoke(w, "getChunkAt", cw[0], cw[1]), 65535);
						for(Player p : Bukkit.getOnlinePlayers())
							Ref.sendPacket(p, a);
					}
					if(onFinish!=null)
					onFinish.run();
				}
			}
		}.repeating(0, 3);
	}

	public static void synchronizedReplace(Position a, Position b, Runnable onFinish, PercentageList<TheMaterial> block, List<TheMaterial> with) {
		BlockGetter s = get(a, b);
		ConfigAPI caw = null;
		for(int iaa = 0; iaa > -1; ++iaa) {
			if(!new File("TheAPI/ChunkTask/"+iaa).exists()) {
				caw = new ConfigAPI("TheAPI/ChunkTask", ""+iaa);
				caw.create();
				break;
			}
		}
		ConfigAPI ca = caw;
		List<TheMaterial> blocks = block.toList();
		new Tasker() {
			@Override
			public void run() {
				for(int i = 0; i < amount; ++i) {
					if (s.has()) {
						Position get = s.get();
						long key = state.replace(get, blocks, (TheMaterial)TheAPI.getRandomFromList(with));
						ca.set(key+"", new int[] {get.getBlockX()>>4,get.getBlockZ()>>4});
					} else
						break;
				}
				if (!s.has()) {
					cancel();
					Object w = Ref.world(a.getWorld());
					for(String o : ca.getKeys(false)) {
						int[] cw = (int[])ca.get(o);
						Object a=Ref.newInstanceNms("PacketPlayOutMapChunk", Ref.invoke(w, "getChunkAt", cw[0], cw[1]), 65535);
						for(Player p : Bukkit.getOnlinePlayers())
							Ref.sendPacket(p, a);
					}
					if(onFinish!=null)
					onFinish.run();
				}
			}
		}.repeating(0, 3);
	}

	public static void synchronizedReplace(Position a, Position b, Runnable onFinish, PercentageList<TheMaterial> block, PercentageList<TheMaterial> with) {
		BlockGetter s = get(a, b);
		ConfigAPI caw = null;
		for(int iaa = 0; iaa > -1; ++iaa) {
			if(!new File("TheAPI/ChunkTask/"+iaa).exists()) {
				caw = new ConfigAPI("TheAPI/ChunkTask", ""+iaa);
				caw.create();
				break;
			}
		}
		ConfigAPI ca = caw;
		List<TheMaterial> blocks = block.toList();
		new Tasker() {
			@Override
			public void run() {
				for(int i = 0; i < amount; ++i) {
					if (s.has()) {
						Position get = s.get();
						long key = state.replace(get, blocks, with.getRandom());
						ca.set(key+"", new int[] {get.getBlockX()>>4,get.getBlockZ()>>4});
					} else
						break;
				}
				if (!s.has()) {
					cancel();
					Object w = Ref.world(a.getWorld());
					for(String o : ca.getKeys(false)) {
						int[] cw = (int[])ca.get(o);
						Object a=Ref.newInstanceNms("PacketPlayOutMapChunk", Ref.invoke(w, "getChunkAt", cw[0], cw[1]), 65535);
						for(Player p : Bukkit.getOnlinePlayers())
							Ref.sendPacket(p, a);
					}
					if(onFinish!=null)
					onFinish.run();
				}
			}
		}.repeating(0, 3);
	}

	public static void synchronizedReplace(Position a, Position b, Runnable onFinish, PercentageList<TheMaterial> block, TheMaterial with) {
		synchronizedReplace(a,b,onFinish,block,Arrays.asList(with));
	}
	// Asynchronized part
	public static void asynchronizedSet(Position a, Position b, TheMaterial with) {
		asynchronizedSet(a, b, new Runnable() {public void run() {}}, with);
	}

	public static void asynchronizedSet(Position a, Position b, TheMaterial with, TheMaterial ignore) {
		asynchronizedSet(a, b, new Runnable() {public void run() {}}, with, ignore);
	}

	public static void asynchronizedSet(Position a, Position b, TheMaterial with, List<TheMaterial> ignore) {
		asynchronizedSet(a, b, new Runnable() {public void run() {}}, with, ignore);
	}

	public static void asynchronizedSet(Position a, Position b, List<TheMaterial> with, List<TheMaterial> ignore) {
		asynchronizedSet(a, b, new Runnable() {public void run() {}}, with, ignore);
	}

	public static void asynchronizedSet(Position a, Position b, List<TheMaterial> with) {
		asynchronizedSet(a, b, new Runnable() {public void run() {}}, with);
	}

	public static void asynchronizedSet(Position a, Position b, PercentageList<TheMaterial> with, List<TheMaterial> ignore) {
		asynchronizedSet(a, b, new Runnable() {public void run() {}}, with, ignore);
	}

	public static void asynchronizedSet(Position a, Position b, PercentageList<TheMaterial> with, TheMaterial ignore) {
		asynchronizedSet(a, b, new Runnable() {public void run() {}}, with, ignore);
	}

	public static void asynchronizedReplace(Position a, Position b, List<TheMaterial> block, TheMaterial with) {
		asynchronizedReplace(a, b, new Runnable() {public void run() {}}, block, with);
	}

	public static void asynchronizedReplace(Position a, Position b, TheMaterial block, TheMaterial with) {
		asynchronizedReplace(a, b, new Runnable() {public void run() {}}, block, with);
	}

	public static void asynchronizedReplace(Position a, Position b, TheMaterial block, PercentageList<TheMaterial> with) {
		asynchronizedReplace(a, b, new Runnable() {public void run() {}}, block, with);
	}

	public static void asynchronizedReplace(Position a, Position b, TheMaterial block, List<TheMaterial> with) {
		asynchronizedReplace(a, b, new Runnable() {public void run() {}}, block, with);
	}

	public static void asynchronizedReplace(Position a, Position b, List<TheMaterial> block, List<TheMaterial> with) {
		asynchronizedReplace(a, b, new Runnable() {public void run() {}}, block, with);
	}

	public static void asynchronizedReplace(Position a, Position b, PercentageList<TheMaterial> block,
			List<TheMaterial> with) {
		asynchronizedReplace(a, b, new Runnable() {public void run() {}}, block, with);
	}

	public static void asynchronizedReplace(Position a, Position b, PercentageList<TheMaterial> block,
			PercentageList<TheMaterial> with) {
		asynchronizedReplace(a, b, new Runnable() {public void run() {}}, block, with);
	}

	public static void asynchronizedReplace(Position a, Position b, PercentageList<TheMaterial> block, TheMaterial with) {
		asynchronizedReplace(a, b, new Runnable() {public void run() {}}, block, with);
	}
	
	//Asynchronized & Runnable on finish part
	public static void asynchronizedSet(Position a, Position b, Runnable onFinish, TheMaterial with) {
		asynchronizedSet(a, b, onFinish, Arrays.asList(with), Arrays.asList());
	}

	public static void asynchronizedSet(Position a, Position b, Runnable onFinish, TheMaterial with, TheMaterial ignore) {
		asynchronizedSet(a, b, onFinish, Arrays.asList(with), Arrays.asList(ignore));
	}

	public static void asynchronizedSet(Position a, Position b, Runnable onFinish, TheMaterial with, List<TheMaterial> ignore) {
		asynchronizedSet(a, b, onFinish, Arrays.asList(with), ignore);
	}

	public static void asynchronizedSet(Position a, Position b, Runnable onFinish, List<TheMaterial> with, List<TheMaterial> ignore) {
		if(!AsyncCatcher.enabled)
			AsyncCatcher.enabled=false;
		BlockGetter s = get(a, b);
		ConfigAPI caw = null;
		for(int iaa = 0; iaa > -1; ++iaa) {
			if(!new File("TheAPI/ChunkTask/"+iaa).exists()) {
				caw = new ConfigAPI("TheAPI/ChunkTask", ""+iaa);
				caw.create();
				break;
			}
		}
		ConfigAPI ca = caw;
		new Tasker() {
			@Override
			public void run() {
				for(int i = 0; i < amount; ++i) {
					if (s.has()) {
						Position get = s.get();
						long key = ignore!=null?state.set(get, (TheMaterial)TheAPI.getRandomFromList(with), ignore):state.set(get, (TheMaterial)TheAPI.getRandomFromList(with));
						ca.set(key+"", new int[] {get.getBlockX()>>4,get.getBlockZ()>>4});
					} else
						break;
				}
				if (!s.has()) {
					cancel();
					Object w = Ref.world(a.getWorld());
					for(String o : ca.getKeys(false)) {
						int[] cw = (int[])ca.get(o);
						Object a=Ref.newInstanceNms("PacketPlayOutMapChunk", Ref.invoke(w, "getChunkAt", cw[0], cw[1]), 65535);
						for(Player p : Bukkit.getOnlinePlayers())
							Ref.sendPacket(p, a);
					}
					if(onFinish!=null)
					onFinish.run();
				}
			}
		}.repeatingAsync(0, 3);
	}

	public static void asynchronizedSet(Position a, Position b, Runnable onFinish, List<TheMaterial> with) {
		asynchronizedSet(a,b,onFinish, with, Arrays.asList());
	}

	public static void asynchronizedSet(Position a, Position b, Runnable onFinish, PercentageList<TheMaterial> with, List<TheMaterial> ignore) {
		if(!AsyncCatcher.enabled)
			AsyncCatcher.enabled=false;
		BlockGetter s = get(a, b);
		ConfigAPI caw = null;
		for(int iaa = 0; iaa > -1; ++iaa) {
			if(!new File("TheAPI/ChunkTask/"+iaa).exists()) {
				caw = new ConfigAPI("TheAPI/ChunkTask", ""+iaa);
				caw.create();
				break;
			}
		}
		ConfigAPI ca = caw;
		new Tasker() {
			@Override
			public void run() {
				for(int i = 0; i < amount; ++i) {
					if (s.has()) {
						Position get = s.get();
						long key = ignore!=null?state.set(get, with.getRandom(), ignore):state.set(get, with.getRandom());
						ca.set(key+"", new int[] {get.getBlockX()>>4,get.getBlockZ()>>4});
					} else
						break;
				}
				if (!s.has()) {
					cancel();
					Object w = Ref.world(a.getWorld());
					for(String o : ca.getKeys(false)) {
						int[] cw = (int[])ca.get(o);
						Object a=Ref.newInstanceNms("PacketPlayOutMapChunk", Ref.invoke(w, "getChunkAt", cw[0], cw[1]), 65535);
						for(Player p : Bukkit.getOnlinePlayers())
							Ref.sendPacket(p, a);
					}
					if(onFinish!=null)
					onFinish.run();
				}
			}
		}.repeatingAsync(0, 3);
	}

	public static void asynchronizedSet(Position a, Position b, Runnable onFinish, PercentageList<TheMaterial> with, TheMaterial ignore) {
		asynchronizedSet(a, b, onFinish, with, Arrays.asList(ignore));
	}

	public static void asynchronizedReplace(Position a, Position b, Runnable onFinish, List<TheMaterial> block, TheMaterial with) {
		asynchronizedReplace(a, b, onFinish, block, Arrays.asList(with));
	}

	public static void asynchronizedReplace(Position a, Position b, Runnable onFinish, TheMaterial block, TheMaterial with) {
		asynchronizedReplace(a, b, onFinish, Arrays.asList(block), Arrays.asList(with));
	}

	public static void asynchronizedReplace(Position a, Position b, Runnable onFinish, TheMaterial block, PercentageList<TheMaterial> with) {
		asynchronizedReplace(a,b,onFinish, Arrays.asList(block), with);
	}
	
	public static void asynchronizedReplace(Position a, Position b, Runnable onFinish, List<TheMaterial> block, PercentageList<TheMaterial> with) {
		if(!AsyncCatcher.enabled)
			AsyncCatcher.enabled=false;
		BlockGetter s = get(a, b);
		ConfigAPI caw = null;
		for(int iaa = 0; iaa > -1; ++iaa) {
			if(!new File("TheAPI/ChunkTask/"+iaa).exists()) {
				caw = new ConfigAPI("TheAPI/ChunkTask", ""+iaa);
				caw.create();
				break;
			}
		}
		ConfigAPI ca = caw;
		new Tasker() {
			@Override
			public void run() {
				for(int i = 0; i < amount; ++i) {
					if (s.has()) {
						Position get = s.get();
						long key = state.replace(get, block, with.getRandom());
						ca.set(key+"", new int[] {get.getBlockX()>>4,get.getBlockZ()>>4});
					} else
						break;
				}
				if (!s.has()) {
					cancel();
					Object w = Ref.world(a.getWorld());
					for(String o : ca.getKeys(false)) {
						int[] cw = (int[])ca.get(o);
						Object a=Ref.newInstanceNms("PacketPlayOutMapChunk", Ref.invoke(w, "getChunkAt", cw[0], cw[1]), 65535);
						for(Player p : Bukkit.getOnlinePlayers())
							Ref.sendPacket(p, a);
					}
					if(onFinish!=null)
					onFinish.run();
				}
			}
		}.repeatingAsync(0, 3);
	}

	public static void asynchronizedReplace(Position a, Position b, Runnable onFinish, TheMaterial block, List<TheMaterial> with) {
		asynchronizedReplace(a, b, onFinish, Arrays.asList(block), with);
	}

	public static void asynchronizedReplace(Position a, Position b, Runnable onFinish, List<TheMaterial> block, List<TheMaterial> with) {
		if(!AsyncCatcher.enabled)
			AsyncCatcher.enabled=false;
		BlockGetter s = get(a, b);
		ConfigAPI caw = null;
		for(int iaa = 0; iaa > -1; ++iaa) {
			if(!new File("TheAPI/ChunkTask/"+iaa).exists()) {
				caw = new ConfigAPI("TheAPI/ChunkTask", ""+iaa);
				caw.create();
				break;
			}
		}
		ConfigAPI ca = caw;
		new Tasker() {
			@Override
			public void run() {
				for(int i = 0; i < amount; ++i) {
					if (s.has()) {
						Position get = s.get();
						long key = state.replace(get, block, (TheMaterial)TheAPI.getRandomFromList(with));
						ca.set(key+"", new int[] {get.getBlockX()>>4,get.getBlockZ()>>4});
					} else
						break;
				}
				if (!s.has()) {
					cancel();
					Object w = Ref.world(a.getWorld());
					for(String o : ca.getKeys(false)) {
						int[] cw = (int[])ca.get(o);
						Object a=Ref.newInstanceNms("PacketPlayOutMapChunk", Ref.invoke(w, "getChunkAt", cw[0], cw[1]), 65535);
						for(Player p : Bukkit.getOnlinePlayers())
							Ref.sendPacket(p, a);
					}
					if(onFinish!=null)
					onFinish.run();
				}
			}
		}.repeatingAsync(0, 3);
	}

	public static void asynchronizedReplace(Position a, Position b, Runnable onFinish, PercentageList<TheMaterial> block, List<TheMaterial> with) {
		if(!AsyncCatcher.enabled)
			AsyncCatcher.enabled=false;
		BlockGetter s = get(a, b);
		ConfigAPI caw = null;
		for(int iaa = 0; iaa > -1; ++iaa) {
			if(!new File("TheAPI/ChunkTask/"+iaa).exists()) {
				caw = new ConfigAPI("TheAPI/ChunkTask", ""+iaa);
				caw.create();
				break;
			}
		}
		ConfigAPI ca = caw;
		List<TheMaterial> blocks = block.toList();
		new Tasker() {
			@Override
			public void run() {
				for(int i = 0; i < amount; ++i) {
					if (s.has()) {
						Position get = s.get();
						long key = state.replace(get, blocks, (TheMaterial)TheAPI.getRandomFromList(with));
						ca.set(key+"", new int[] {get.getBlockX()>>4,get.getBlockZ()>>4});
					} else
						break;
				}
				if (!s.has()) {
					cancel();
					Object w = Ref.world(a.getWorld());
					for(String o : ca.getKeys(false)) {
						int[] cw = (int[])ca.get(o);
						Object a=Ref.newInstanceNms("PacketPlayOutMapChunk", Ref.invoke(w, "getChunkAt", cw[0], cw[1]), 65535);
						for(Player p : Bukkit.getOnlinePlayers())
							Ref.sendPacket(p, a);
					}
					if(onFinish!=null)
					onFinish.run();
				}
			}
		}.repeatingAsync(0, 3);
	}

	public static void asynchronizedReplace(Position a, Position b, Runnable onFinish, PercentageList<TheMaterial> block, PercentageList<TheMaterial> with) {
		if(!AsyncCatcher.enabled)
			AsyncCatcher.enabled=false;
		BlockGetter s = get(a, b);
		ConfigAPI caw = null;
		for(int iaa = 0; iaa > -1; ++iaa) {
			if(!new File("TheAPI/ChunkTask/"+iaa).exists()) {
				caw = new ConfigAPI("TheAPI/ChunkTask", ""+iaa);
				caw.create();
				break;
			}
		}
		ConfigAPI ca = caw;
		List<TheMaterial> blocks = block.toList();
		new Tasker() {
			@Override
			public void run() {
				for(int i = 0; i < amount; ++i) {
					if (s.has()) {
						Position get = s.get();
						long key = state.replace(get, blocks, with.getRandom());
						ca.set(key+"", new int[] {get.getBlockX()>>4,get.getBlockZ()>>4});
					} else
						break;
				}
				if (!s.has()) {
					cancel();
					Object w = Ref.world(a.getWorld());
					for(String o : ca.getKeys(false)) {
						int[] cw = (int[])ca.get(o);
						Object a=Ref.newInstanceNms("PacketPlayOutMapChunk", Ref.invoke(w, "getChunkAt", cw[0], cw[1]), 65535);
						for(Player p : Bukkit.getOnlinePlayers())
							Ref.sendPacket(p, a);
					}
					if(onFinish!=null)
					onFinish.run();
				}
			}
		}.repeatingAsync(0, 3);
	}

	public static void asynchronizedReplace(Position a, Position b, Runnable onFinish, PercentageList<TheMaterial> block, TheMaterial with) {
		asynchronizedReplace(a,b,onFinish,block,Arrays.asList(with));
	}
	
}