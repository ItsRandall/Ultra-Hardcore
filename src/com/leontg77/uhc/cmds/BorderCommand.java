package com.leontg77.uhc.cmds;

import org.bukkit.ChatColor;
import org.bukkit.World.Environment;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.leontg77.uhc.Main;
import com.leontg77.uhc.utils.PlayerUtils;

public class BorderCommand implements CommandExecutor {

	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		if (!(sender instanceof Player)) {
			sender.sendMessage(ChatColor.RED + "Only players can view and set borders.");
			return true;
		}
		
		Player player = (Player) sender;
		
		if (cmd.getName().equalsIgnoreCase("border")) {
			if (args.length == 0) {
				player.sendMessage(Main.prefix() + "The border is currently: �a" + (((int) player.getWorld().getWorldBorder().getSize()) + 1) + "x" + (((int) player.getWorld().getWorldBorder().getSize()) + 1));
				return true;
			}
			
			if (player.hasPermission("uhc.border")) {
				int radius;
				
				try {
					radius = Integer.parseInt(args[0]);
				} catch (Exception e) {
					player.sendMessage(ChatColor.RED + "Invaild radius.");
					return true;
				}

				player.getWorld().getWorldBorder().setWarningDistance(0);
				player.getWorld().getWorldBorder().setDamageAmount(0.1);
				player.getWorld().getWorldBorder().setSize(radius - 1);
				player.getWorld().getWorldBorder().setDamageBuffer(30);
				player.getWorld().getWorldBorder().setWarningTime(60);
				
				if (player.getWorld().getEnvironment() == Environment.NETHER) {
					player.getWorld().getWorldBorder().setCenter(0, 0);
				} else {
					player.getWorld().getWorldBorder().setCenter(0.5, 0.5);
				}
				
				PlayerUtils.broadcast(Main.prefix() + "Border setup with radius of " + radius + "x" + radius + ".");
			} else {
				player.sendMessage(Main.prefix() + "You can't use that command.");
			}
		}
		return true;
	}
}