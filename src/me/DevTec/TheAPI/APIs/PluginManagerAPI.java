package me.DevTec.TheAPI.APIs;

import java.io.File;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandMap;
import org.bukkit.command.PluginCommand;
import org.bukkit.command.SimpleCommandMap;
import org.bukkit.permissions.Permission;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.SimplePluginManager;

import me.DevTec.TheAPI.Scheduler.Tasker;
import me.DevTec.TheAPI.Utils.DataKeeper.Collections.UnsortedList;
import me.DevTec.TheAPI.Utils.DataKeeper.Maps.UnsortedMap;
import me.DevTec.TheAPI.Utils.NMS.NMSAPI;
import me.DevTec.TheAPI.Utils.Reflections.Ref;

public class PluginManagerAPI {
	private static PluginManager spm = Bukkit.getPluginManager();

	public static boolean enablePlugin(String plugin) {
		if (!spm.isPluginEnabled(plugin)) {
			spm.enablePlugin(getPlugin(plugin));
			return true;
		}
		return false;
	}

	public static boolean enablePlugin(Plugin plugin) {
		if (!spm.isPluginEnabled(plugin)) {
			spm.enablePlugin(plugin);
			return true;
		}
		return false;
	}

	public static List<Plugin> getEnabledPlugins() {
		List<Plugin> a = new UnsortedList<Plugin>();
		for (Plugin p : Bukkit.getPluginManager().getPlugins()) {
			if (p.isEnabled())
				a.add(p);
		}
		return a;
	}

	public static List<Plugin> getDisabledPlugins() {
		List<Plugin> a = new UnsortedList<Plugin>();
		for (Plugin p : Bukkit.getPluginManager().getPlugins()) {
			if (!p.isEnabled())
				a.add(p);
		}
		return a;
	}

	public static List<String> getEnabledPluginsNames() {
		List<String> a = new UnsortedList<String>();
		for (Plugin p : Bukkit.getPluginManager().getPlugins()) {
			if (p.isEnabled())
				a.add(p.getName());
		}
		return a;
	}

	public static List<String> getDisabledPluginsNames() {
		List<String> a = new UnsortedList<String>();
		for (Plugin p : Bukkit.getPluginManager().getPlugins()) {
			if (!p.isEnabled())
				a.add(p.getName());
		}
		return a;
	}

	public static List<Plugin> getPlugins() {
		List<Plugin> a = new UnsortedList<Plugin>();
		for (Plugin p : Bukkit.getPluginManager().getPlugins())
			a.add(p);
		return a;
	}

	public static List<String> getPluginsNames() {
		List<String> a = new UnsortedList<String>();
		for (Plugin p : Bukkit.getPluginManager().getPlugins()) {
			a.add(p.getName());
		}
		return a;
	}

	public static Plugin getPlugin(String plugin) {
		Plugin p = null;
		for (Plugin s : spm.getPlugins()) {
			if (s.getName().equalsIgnoreCase(plugin)) {
				p = s;
				break;
			}
		}
		return p;
	}

	public static List<String> getDepend(String plugin) {
		Plugin p = getPlugin(plugin);
		if (p != null)
			if (p.getDescription().getDepend() != null && p.getDescription().getDepend().isEmpty() == false)
				return p.getDescription().getDepend();
		return new UnsortedList<String>();
	}

	public static List<String> getSoftDepend(String plugin) {
		Plugin p = getPlugin(plugin);
		if (p != null)
			if (p.getDescription().getSoftDepend() != null && p.getDescription().getSoftDepend().isEmpty() == false)
				return p.getDescription().getSoftDepend();
		return new UnsortedList<String>();
	}

	public static List<String> getAuthor(String plugin) {
		Plugin p = getPlugin(plugin);
		if (p != null)
			return p.getDescription().getAuthors();
		return new UnsortedList<String>();
	}

	public static String getAPIVersion(String plugin) {
		Plugin p = getPlugin(plugin);
		try {
			if (p != null && p.getDescription().getAPIVersion() != null)
				return p.getDescription().getAPIVersion();
		} catch (Exception e) {
		}
		return null;
	}

	public static String getVersion(String plugin) {
		Plugin p = getPlugin(plugin);
		if (p != null && p.getDescription().getVersion() != null)
			return p.getDescription().getVersion();
		return null;
	}

	public static String getWebsite(String plugin) {
		Plugin p = getPlugin(plugin);
		if (p != null && p.getDescription().getWebsite() != null)
			return p.getDescription().getWebsite();
		return null;
	}

	public static String getMainClass(String plugin) {
		Plugin p = getPlugin(plugin);
		if (p != null && p.getDescription().getMain() != null)
			return p.getDescription().getMain();
		return null;
	}

	public static List<String> getCommands(String plugin) {
		List<String> list = new UnsortedList<String>();
		for (String s : getPlugin(plugin).getDescription().getCommands().keySet())
			list.add(s);
		return list;
	}

	public static List<String> getCommands(Plugin plugin) {
		return getCommands(plugin.getName());
	}

	public static List<Permission> getPermissions(String plugin) {
		return getPlugin(plugin).getDescription().getPermissions();
	}

	public static List<Permission> getPermissions(Plugin plugin) {
		return getPermissions(plugin.getName());
	}

	public static boolean isEnabledPlugin(String plugin) {
		if (plugin == null || getPlugin(plugin) == null)
			return false;
		return getPlugin(plugin).isEnabled();
	}

	public static boolean isEnabledPlugin(Plugin plugin) {
		if (plugin == null)
			return false;
		return isEnabledPlugin(plugin.getName());
	}

	public static boolean isDisabledPlugin(String plugin) {
		if (plugin == null || getPlugin(plugin) == null)
			return true;
		return !getPlugin(plugin).isEnabled();
	}

	public static boolean isDisabledPlugin(Plugin plugin) {
		if (plugin == null)
			return true;
		return !isEnabledPlugin(plugin.getName());
	}

	public static List<String> getPluginsToLoad() {
		List<String> list = new UnsortedList<String>();
		if (new File("plugins").isDirectory()) // is folder
			for (File f : new File("plugins").listFiles()) {
				if (!f.isDirectory() && f.getName().endsWith(".jar")) {
					for (Plugin p : getPlugins()) {
						if (!getFileOfPlugin(p).getName().equals(f.getName()))
							if (!spm.isPluginEnabled(f.getName().substring(0, f.getName().length() - 4)))
								list.add(f.getName());
						break;
					}
				}
			}
		return list;
	}

	public static String getPluginFileByName(String pluginName) {
		String pl = null;
		Map<String, String> load = getPluginsToLoadWithNames();
		for (String s : load.keySet()) {
			if (s.equals(pluginName)) {
				pl = load.get(s);
				break;
			}
			if (load.get(s).equals(pluginName)) {
				pl = load.get(s);
				break;
			}
		}
		if (pl.endsWith(".jar"))
			pl = pl.substring(0, pl.length() - 4);
		return pl;
	}

	public static String getPluginNameByFile(String pluginFile) {
		String pluginName = null;
		Map<String, String> load = getPluginsToLoadWithNames();
		for (String s : load.keySet()) {
			if (s.equals(pluginFile)) {
				pluginName = pluginFile;
				break;
			}
			if (load.get(s).equals(pluginFile)) {
				pluginName = s;
				break;
			}
		}
		return pluginName;
	}

	public static File getFileOfPlugin(Plugin p) {
		return new File("plugins/" + new File(Ref.getClass(p.getDescription().getMain()).getProtectionDomain()
				.getCodeSource().getLocation().getPath()).getName());
	}

	/**
	 * @return Map<PluginName, FileName>
	 */
	public static Map<String, String> getPluginsToLoadWithNames() {
		UnsortedMap<String, String> a = new UnsortedMap<>();
		if (new File("plugins").isDirectory()) // is folder
			for (File f : new File("plugins").listFiles()) {
				if (!f.isDirectory() && f.getName().endsWith(".jar")) {
					Plugin loaded = null;
					for (Plugin p : getPlugins()) {
						if (getFileOfPlugin(p).getName().equals(f.getName())) {
							loaded = p;
							break;
						}
					}
					if (loaded == null) {
						String name = null;
						String[] text = readPlugin(f);
						for (String find : text) {
							if (find.contains("name: ")) {
								String[] str = find.split("name: ");
								name = str[1];
								break;
							}
						}
						a.put(name, f.getName());
					}
				}
			}
		return a;
	}

	public static List<String> getRawPluginsToLoad() {
		List<String> list = new UnsortedList<String>();
		if (new File("plugins").isDirectory()) // is folder
			for (File f : new File("plugins").listFiles()) {
				if (!f.isDirectory() && f.getName().endsWith(".jar")) {
					Plugin loaded = null;
					for (Plugin p : getPlugins()) {
						if (new java.io.File(Ref.getClass(p.getDescription().getMain()).getProtectionDomain()
								.getCodeSource().getLocation().getPath()).getName().equals(f.getName())) {
							loaded = p;
							break;
						}
					}
					if (loaded == null) {
						String name = null;
						String[] text = readPlugin(f);
						for (String find : text) {
							if (find.contains("name: ")) {
								String[] str = find.split("name: ");
								name = str[1];
								break;
							}
						}
						list.add(name);
					}
				}
			}
		return list;
	}

	private static String[] readPlugin(File a) {
		try {
			JarFile file = new JarFile(a);
			if (file != null) {
				Enumeration<JarEntry> er = file.entries();
				while (er.hasMoreElements()) {
					JarEntry entry = er.nextElement();
					if (entry.getName().equals("plugin.yml")) {
						InputStream is = file.getInputStream(entry);
						int readBytes;
						StringBuffer f = new StringBuffer();
						while ((readBytes = is.read()) != -1)
							f.append((char) readBytes);
						return f.toString().split("\r");
					}
				}
			}
			file.close();
		} catch (Exception erer) {
		}
		return null;
	}

	public static void reloadPlugin(Plugin plugin) {
		reloadPlugin(plugin.getName());
	}

	public static void reloadPlugin(String plugin) {
		new Tasker() {
			public void run() {
				String pl = plugin;
				unloadPlugin(getPlugin(pl));
				new Thread(new Runnable() {
					public void run() {
						try {
							Thread.sleep(100);
						} catch (InterruptedException e) {
						}
						NMSAPI.postToMainThread(new Runnable() {
							public void run() {
								loadPlugin(pl);
							}
						});
					}
				}).start();
			}
		}.runTaskSync();
	}

	public static void unloadPlugin(Plugin plugin) {
		if (plugin == null)
			return;
		unloadPlugin(plugin.getName());
	}

	@SuppressWarnings("unchecked")
	public static void unloadPlugin(String pluginName) {
		if (pluginName == null)
			return;
		new Tasker() {
			public void run() {
				List<Plugin> plugins = (List<Plugin>) Ref.get(((SimplePluginManager) spm),
						Ref.field(SimplePluginManager.class, "plugins"));
				Map<String, Plugin> lookupNames = (Map<String, Plugin>) Ref.get(((SimplePluginManager) spm),
						Ref.field(SimplePluginManager.class, "lookupNames"));
				SimpleCommandMap commandMap = (SimpleCommandMap) Ref.get(((SimplePluginManager) spm),
						Ref.field(SimplePluginManager.class, "commandMap"));
				Map<String, Command> knownCommands = (Map<String, Command>) Ref.get(commandMap,
						Ref.field(commandMap.getClass(), "knownCommands"));
				Plugin pl = getPlugin(pluginName);
				disablePlugin(pl);
				if (plugins != null && plugins.contains(pl))
					plugins.remove(pl);
				if (lookupNames != null && lookupNames.containsKey(pluginName))
					lookupNames.remove(pluginName);
				if (commandMap != null) {
					for (Iterator<Map.Entry<String, Command>> it = knownCommands.entrySet().iterator(); it.hasNext();) {
						Map.Entry<String, Command> entry = it.next();
						if (entry.getValue() instanceof PluginCommand) {
							PluginCommand c = (PluginCommand) entry.getValue();
							if (c.getPlugin() == pl) {
								c.unregister(commandMap);
								it.remove();
							}
						}
					}
				}
				try {
					List<Permission> permissionlist = pl.getDescription().getPermissions();
					Iterator<Permission> p = permissionlist.iterator();
					while (p.hasNext()) {
						spm.removePermission(p.next().toString());
					}
				} catch (NoSuchMethodError e) {
				}
			}
		}.runTaskSync();
	}

	public static void loadPlugin(String n) {
		if (n == null)
			return;
		new Tasker() {
			public void run() {
				String pluginName = getPluginFileByName(n);
				if (pluginName == null)
					pluginName = getPluginNameByFile(n);
				try {
					Plugin p = Bukkit.getPluginManager().loadPlugin(new File("plugins/" + pluginName + ".jar"));
					p.onLoad();
					Bukkit.getPluginManager().enablePlugin(p);
					CommandMap commandMap = (CommandMap) Ref.get(Bukkit.getServer(),
							Ref.field(Bukkit.getServer().getClass(), "commandMap"));
					for (String s : p.getDescription().getCommands().keySet())
						commandMap.register(s, null);
				} catch (Exception e) {
				}
			}
		}.runTaskSync();
	}

	public static boolean disablePlugin(String plugin) {
		if (plugin == null)
			return false;
		if (isEnabledPlugin(plugin)) {
			spm.disablePlugin(getPlugin(plugin));
			return true;
		}
		return false;
	}

	public static boolean disablePlugin(Plugin plugin) {
		if (plugin == null)
			return false;
		return disablePlugin(plugin.getName());
	}
}
