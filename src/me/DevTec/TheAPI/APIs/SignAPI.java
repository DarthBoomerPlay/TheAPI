package me.DevTec.TheAPI.APIs;

import java.util.List;
import java.util.Map;

import org.bukkit.block.Sign;

import me.DevTec.TheAPI.Utils.Position;
import me.DevTec.TheAPI.Utils.DataKeeper.Collections.UnsortedList;
import me.DevTec.TheAPI.Utils.DataKeeper.Maps.UnsortedMap;
import me.DevTec.TheAPI.Utils.TheAPIUtils.LoaderClass;

public class SignAPI {

	// List<String> commands = Arrays.asList("string.here","next.string");
	public static enum SignAction {
		CONSOLE_COMMANDS, PLAYER_COMMANDS, BROADCAST, MESSAGES
	}

	public static void removeSign(Position loc) {
		LoaderClass.data.set("Sign." + loc.toString(), null);
		LoaderClass.data.save();
	}

	public static List<Position> getRegistredSigns() {
		List<Position> l = new UnsortedList<Position>();
		if (LoaderClass.data.exists("Sign"))
			for (String s : LoaderClass.data.getKeys("Sign")) {
				Position d = Position.fromString(s);
				if (d.getBlock().getType().name().contains("SIGN"))
					l.add(d);
				else
					removeSign(d);
			}
		return l;
	}

	public static Sign getSignState(Position loc) {
		Sign s = null;
		if (getRegistredSigns().contains(loc))
			s = (Sign) loc.getBlock().getState();
		return s;
	}

	public static void setActions(Sign state, Map<SignAction, List<String>> options) {
		String l = new Position(state.getLocation()).toString();
		for (SignAction s : options.keySet()) {
			switch (s) {
			case CONSOLE_COMMANDS:
				if (options.get(s) instanceof List)
					LoaderClass.data.set("Sign." + l + ".CONSOLE_COMMANDS", options.get(s));
				break;
			case PLAYER_COMMANDS:
				if (options.get(s) instanceof List)
					LoaderClass.data.set("Sign." + l + ".PLAYER_COMMANDS", options.get(s));
				break;
			case MESSAGES:
				if (options.get(s) instanceof List)
					LoaderClass.data.set("Sign." + l + ".MESSAGES", options.get(s));
				break;
			case BROADCAST:
				if (options.get(s) instanceof List)
					LoaderClass.data.set("Sign." + l + ".BROADCAST", options.get(s));
				break;
			}
		}
		LoaderClass.data.save();
	}

	public static Map<SignAction, List<String>> getSignActions(Sign state) {
		UnsortedMap<SignAction, List<String>> a = new UnsortedMap<SignAction, List<String>>();
		Position l = new Position(state.getLocation());
		String ff = l.toString();
		if (getRegistredSigns().contains(l))
			for (String s : LoaderClass.data.getKeys("Sign." + ff))
				a.put(SignAction.valueOf(s), LoaderClass.data.getStringList("Sign." + ff + "." + s));
		return a;
	}

}
