package com.leontg77.uhc.scenario.types;

import java.util.ArrayList;
import java.util.Random;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.scoreboard.Team;

import com.leontg77.uhc.Main;
import com.leontg77.uhc.Teams;
import com.leontg77.uhc.scenario.Scenario;
import com.leontg77.uhc.utils.PlayerUtils;

/**
 * Captains scenario class
 * 
 * @author LeonTG77
 */
public class Captains extends Scenario implements Listener, CommandExecutor {
	private ArrayList<String> captains = new ArrayList<String>();
	private boolean enabled = false;
	private String chooser = "none";
	private boolean cycle = false;
	private int current = -1;

	public Captains() {
		super("Captains", "Theres X amount of captains, there will be rounds where one captain will choose a player until it reaches the teamsize.");
		Main main = Main.plugin;
		
		main.getCommand("addcaptain").setExecutor(this);
		main.getCommand("removecaptain").setExecutor(this);
		main.getCommand("randomcaptain").setExecutor(this);
		main.getCommand("cycle").setExecutor(this);
		main.getCommand("choose").setExecutor(this);
	}

	public void setEnabled(boolean enable) {
		enabled = enable;
	}

	public boolean isEnabled() {
		return enabled;
	}

	public boolean onCommand(CommandSender player, Command cmd, String label, String[] args) {
		if (cmd.getName().equalsIgnoreCase("addcaptain")) {
			if (!isEnabled()) {
				player.sendMessage(Main.PREFIX + "\"Captains\" is not enabled.");
				return true;
			}
			
			if (!player.hasPermission("uhc.captains")) {
				player.sendMessage(Main.NO_PERM_MSG);
				return true;
			}
			
			if (args.length == 0) {
				player.sendMessage(ChatColor.RED + "Usage: /addcaptain <player>");
				return true;
			}
			
			if (captains.contains(args[0])) {
				player.sendMessage(ChatColor.RED + args[0] + " is already an captain.");
				return true;
			}

			Team team = Teams.getInstance().findAvailableTeam();
			
			if (team == null) {
				player.sendMessage(ChatColor.RED + "No more available teams.");
				return true;
			}
			
			team.addEntry(args[0]);
			
			captains.add(args[0]);
			PlayerUtils.broadcast(Main.PREFIX + ChatColor.GREEN + args[0] + " �7is now an captain.");
		}
		
		if (cmd.getName().equalsIgnoreCase("removecaptain")) {
			if (!isEnabled()) {
				player.sendMessage(Main.PREFIX + "\"Captains\" is not enabled.");
				return true;
			}
			
			if (!player.hasPermission("uhc.captains")) {
				player.sendMessage(Main.NO_PERM_MSG);
				return true;
			}
			
			if (args.length == 0) {
				player.sendMessage(ChatColor.RED + "Usage: /removecaptain <player>");
				return true;
			}
			
			if (!captains.contains(args[0])) {
				player.sendMessage(ChatColor.RED + args[0] + " is not an captain.");
				return true;
			}
			
			Team t = Teams.getInstance().getTeam(args[0]);
			
			if (t != null) {
				t.removeEntry(args[0]);
			}
			
			captains.remove(args[0]);
			PlayerUtils.broadcast(Main.PREFIX + args[0] + ChatColor.GREEN + " �7is no longer an captain.");
		}
		
		if (cmd.getName().equalsIgnoreCase("randomcaptain")) {
			if (!isEnabled()) {
				player.sendMessage(Main.PREFIX + "\"Captains\" is not enabled.");
				return true;
			}
			
			if (!player.hasPermission("uhc.captains")) {
				player.sendMessage(Main.NO_PERM_MSG);
				return true;
			}
			
			if (args.length == 0) {
				player.sendMessage(ChatColor.RED + "Usage: /randomcaptains <amount>");
				return true;
			}
			
			int amount;
			
			try {
				amount = Integer.parseInt(args[0]);
			} catch (Exception e) {
				player.sendMessage(ChatColor.RED + "Invaild number.");
				return true;
			}
			
			for (int i = 1; i <= amount; i++) {
				ArrayList<String> list = new ArrayList<String>();
				for (Player online : PlayerUtils.getPlayers()) {
					if (captains.contains(online.getName())) {
						continue;
					}
					
					list.add(online.getName());
				}
				
				String s = list.get(new Random().nextInt(list.size()));
				captains.add(s);
				
				Team t = Teams.getInstance().findAvailableTeam();
				
				if (t == null) {
					return true;
				}
				
				t.addEntry(s);
				PlayerUtils.broadcast(Main.PREFIX + ChatColor.GREEN + s + " �7is now an captain.");
			}
		}
		
		if (cmd.getName().equalsIgnoreCase("cycle")) {
			if (!isEnabled()) {
				player.sendMessage(Main.PREFIX + "\"Captains\" is not enabled.");
				return true;
			}
			
			if (!player.hasPermission("uhc.captains")) {
				player.sendMessage(Main.NO_PERM_MSG);
				return true;
			}
			
			if (cycle) {
				cycle = false;
				PlayerUtils.broadcast(Main.PREFIX + "Captains can no longer choose players.");
				chooser = "none";
				current = -1;
			} else {
				cycle = true;
				PlayerUtils.broadcast(Main.PREFIX + "Captains can now choose players.");
				String cap = captains.get(0);
				PlayerUtils.broadcast(Main.PREFIX + "First captain to choose is �a" + cap);
				chooser = cap;
				current = 0;
			}
		}
		
		if (cmd.getName().equalsIgnoreCase("choose")) {
			if (!isEnabled()) {
				player.sendMessage(Main.PREFIX + "\"Captains\" is not enabled.");
				return true;
			}
			
			if (!captains.contains(player.getName())) {
				player.sendMessage(ChatColor.RED + "You are not an captain");
				return true;
			}
			
			if (!player.getName().equalsIgnoreCase(chooser)) {
				player.sendMessage(ChatColor.RED + "You are not the one choosing.");
				return true;
			}
			
			if (args.length == 0) {
				player.sendMessage(ChatColor.RED + "Usage: /choose <player>");
				return true;
			}
			
			Player target = Bukkit.getServer().getPlayer(args[0]);
			
			if (target == null) {
				player.sendMessage(ChatColor.RED + "That player is not online.");
				return true;
			}
			
			if (target == player) {
				player.sendMessage(ChatColor.RED + "You cannot choose yourselves.");
				return true;
			}
			
			if (target.getScoreboard().getEntryTeam(target.getName()) != null) {
				player.sendMessage(ChatColor.RED + "That player is already taken.");
				return true;
			}
			
			Team team = Teams.getInstance().getTeam(player.getName());
			
			if (team != null) {
				team.addEntry(target.getName());
			}
			
			current++;
			if (current >= captains.size()) {
				current = 0;
			}
			
			PlayerUtils.broadcast(Main.PREFIX + player.getName() + ChatColor.GREEN + " �7has picked �a" + target.getName() + "�7, next captain to choose is �a" + captains.get(current));
			chooser = captains.get(current);
		}
		return true;
	}
}