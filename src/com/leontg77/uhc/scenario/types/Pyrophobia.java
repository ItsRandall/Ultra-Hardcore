package com.leontg77.uhc.scenario.types;

import java.util.ArrayList;
import java.util.Random;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockIgniteEvent;
import org.bukkit.event.block.BlockIgniteEvent.IgniteCause;
import org.bukkit.event.block.LeavesDecayEvent;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.player.PlayerBucketFillEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;

import com.leontg77.uhc.Main;
import com.leontg77.uhc.scenario.Scenario;
import com.leontg77.uhc.utils.PlayerUtils;

/**
 * @author Bergasms
 */
public class Pyrophobia extends Scenario implements Listener {
	private ArrayList<Location> locations;
	private boolean enabled = false;
	private int generateTaskID;
	private int totalChunks;

	public Pyrophobia() {
		super("Pyrophobia", "All water and ice is replaced with lava, redstone and lapis is replaced by obsidian and leaves drop sugar canes.");
	    this.generateTaskID = -1;
	    this.totalChunks = 0;
	    this.locations = new ArrayList<Location>();
	}

	public void setEnabled(boolean enable) {
		enabled = enable;
	}

	public boolean isEnabled() {
		return enabled;
	}

	@EventHandler
	public void onPlayerBucketFill(PlayerBucketFillEvent event) {
		if (!isEnabled()) {
			return;
		}
		
		Player player = event.getPlayer();
		
		if (event.getItemStack().getType() == Material.WATER_BUCKET) {
			player.sendMessage(Main.prefix().replace("UHC", "Pyrophobia") + ChatColor.RED + "You cannot have water in PyroPhobia.");
			event.setItemStack(new ItemStack (Material.BUCKET));
			event.setCancelled(true);
		}
	}

	@EventHandler
	public void onBlockIgnite(BlockIgniteEvent event) {
		if (!isEnabled()) {
			return;
		}
		
		IgniteCause cause = event.getCause();

		if (cause == IgniteCause.LAVA) {
			event.setCancelled(true);
			return;
		}

		if (cause == IgniteCause.SPREAD) {
			event.setCancelled(true);
		}
	}

	@EventHandler(ignoreCancelled = true)
	public void CreatureSpawnEvent(CreatureSpawnEvent event) {
		if (!isEnabled()) {
			return;
		}
		
		event.getEntity().addPotionEffect(new PotionEffect(PotionEffectType.FIRE_RESISTANCE, Integer.MAX_VALUE, 2));
	}

	@EventHandler(ignoreCancelled = true)
	public void LeavesDecayEvent(LeavesDecayEvent event) {
		if (!isEnabled()) {
			return;
		}
		
		Random r = new Random();
		if (r.nextInt(100) < 2) {
			Item item = event.getBlock().getWorld().dropItem(event.getBlock().getLocation().add(0.5, 0.7, 0.5), new ItemStack(Material.SUGAR_CANE, 1 + r.nextInt(1)));
			item.setVelocity(new Vector(0, 0.2, 0));
		}
	}

	@EventHandler
	public void onPlayerCommandPreprocess(PlayerCommandPreprocessEvent event) {
		Player player = event.getPlayer();
		
		if (event.getMessage().split(" ")[0].equalsIgnoreCase("/genpyro")) {
			event.setCancelled(true);
			
			ArrayList<String> ar = new ArrayList<String>();
			for (String arg : event.getMessage().split(" ")) {
				ar.add(arg);
			}
			ar.remove(0);
			String[] args = ar.toArray(new String[ar.size()]);
			
			if (args.length == 0) {
				player.sendMessage(Main.prefix().replaceAll("UHC", "Pyrophobia") + "Starting pyrophobia convertion.");
				convertToPyro(player.getWorld(), 1100);
			}
			
			int radius;
			
			try {
				radius = Integer.parseInt(args[0]);
			} catch (Exception e) {
				player.sendMessage(ChatColor.RED + "Invaild radius.");
				return;
			}

			player.sendMessage(Main.prefix().replaceAll("UHC", "Pyrophobia") + "Starting pyrophobia convertion.");
			convertToPyro(player.getWorld(), radius);
		}
	}

	private void completedPyro(final World w, int radius) {
		Bukkit.getServer().getScheduler().cancelTask(this.generateTaskID);
		this.generateTaskID = -1;
		Bukkit.getServer().broadcastMessage(Main.prefix().replaceAll("UHC", "Pyrophobia") + "World mid Converted");

		this.locations = new ArrayList<Location>();
		for (int i = -1 * radius; i < radius; i += 16) {
			for (int j = -1 * radius; j < radius; j += 16) {
				this.locations.add(new Location(w, i, 1.0D, j));
			}
		}
		this.totalChunks = this.locations.size();

		this.generateTaskID = Bukkit.getServer().getScheduler().scheduleSyncRepeatingTask(Main.plugin, new Runnable() {
			public void run() {
				if (locations.size() > 0) {
					Location l = (Location) locations.remove(locations.size() - 1);
					postPyronChunk(w.getChunkAt(l));
				} else {
					completedForReal();
				}
			}
		}, 1L, 1L);
	}

	protected void completedForReal() {
		Bukkit.getServer().getScheduler().cancelTask(this.generateTaskID);
		this.generateTaskID = -1;
		Bukkit.getServer().broadcastMessage(Main.prefix().replaceAll("UHC", "Pyrophobia") + "World Converted");
	}

	private void convertToPyro(final World w, final int radius) {
		if (this.generateTaskID != -1) {
			Bukkit.getServer().getScheduler().cancelTask(this.generateTaskID);
		}
		this.locations = new ArrayList<Location>();
		for (int i = -1 * radius; i < radius; i += 16) {
			for (int j = -1 * radius; j < radius; j += 16) {
				this.locations.add(new Location(w, i, 1.0D, j));
			}
		}
		this.totalChunks = this.locations.size();

		this.generateTaskID = Bukkit.getServer().getScheduler().scheduleSyncRepeatingTask(Main.plugin, new Runnable() {
			public void run() {
				if (locations.size() > 0) {
					Location l = (Location) locations.remove(locations.size() - 1);
					pyroChunk(w.getChunkAt(l));
				} else {
					completedPyro(w, radius);
				}
			}
		}, 1L, 1L);
	}

	protected void postPyronChunk(Chunk chunkAt) {
		Random r = new Random();
		for (int y = 0; y < 128; y++) {
			for (int x = 0; x < 16; x++) {
				for (int z = 0; z < 17; z++) {
					Block b = chunkAt.getBlock(x, y, z);
					if (b.getType() == Material.OBSIDIAN) {
						chunkAt.getBlock(x, y, z).setType(Material.STATIONARY_LAVA);
					}
					if ((b.getType() == Material.LAPIS_ORE) && (r.nextInt(50) < 4)) {
						chunkAt.getBlock(x, y, z).setType(Material.OBSIDIAN);
					}
					if ((b.getType() == Material.REDSTONE_ORE) && (r.nextInt(50) < 24)) {
						chunkAt.getBlock(x, y, z).setType(Material.OBSIDIAN);
					}
				}
			}
		}

		int one = ((this.totalChunks - this.locations.size())*100 / totalChunks);
		
		for (Player online : PlayerUtils.getPlayers()) {
			PlayerUtils.sendAction(online, "§4§lPyrophobia §8» §7Processed: §6" + ((one / 2) + 50) + "%");
		}
	}

	protected void pyroChunk(Chunk chunkAt) {
		for (int y = 0; y < 128; y++) {
			for (int x = 0; x < 16; x++) {
				for (int z = 0; z < 17; z++) {
					Block b = chunkAt.getBlock(x, y, z);
					if (b.getType() == Material.STATIONARY_WATER) {
						chunkAt.getBlock(x, y, z).setType(Material.OBSIDIAN);
					}
					if (b.getType() == Material.ICE) {
						chunkAt.getBlock(x, y, z).setType(Material.OBSIDIAN);
					}
					if (b.getType() == Material.PACKED_ICE) {
						chunkAt.getBlock(x, y, z).setType(Material.OBSIDIAN);
					}
					if (b.getType() == Material.WATER) {
						chunkAt.getBlock(x, y, z).setType(Material.OBSIDIAN);
					}
				}
			}
		}

		int one = ((totalChunks - locations.size())*100 / totalChunks);
		
		for (Player online : PlayerUtils.getPlayers()) {
			PlayerUtils.sendAction(online, "§4§lPyrophobia §8» §7Processed: §6" + (one / 2) + "%");
		}
	}
}