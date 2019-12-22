package xyz.nkomarn.Presents;

import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import com.sk89q.worldguard.protection.regions.RegionContainer;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Firework;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.meta.FireworkMeta;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

public class Presents extends JavaPlugin implements Listener {
    final String prefix = "&c&lPresents: &7";

    public void onEnable() {
        saveDefaultConfig();
        getServer().getPluginManager().registerEvents(this, this);
    }

    @EventHandler
    public void onInteract(final PlayerInteractEvent event) {
        if (event.getHand() != EquipmentSlot.HAND) return;
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK) return;
        final Block block = event.getClickedBlock();
        if (block.getType() != Material.PLAYER_HEAD) return;

        final RegionContainer regionContainer = WorldGuard.getInstance().getPlatform().getRegionContainer();
        final RegionManager regionManager = regionContainer.get(BukkitAdapter.adapt(block.getWorld()));
        final ApplicableRegionSet regionSet = regionManager.getApplicableRegions(BukkitAdapter.asBlockVector(block.getLocation()));
        for (ProtectedRegion protectedRegion : regionSet) {
            if (protectedRegion.getId().equals("christmastree")) {
                givePresent(event.getClickedBlock(), event.getPlayer());
                event.setCancelled(true);
            }
        }
    }

    public void givePresent(final Block block, final Player player) {
        final List<String> claimedPlayers = getConfig().getStringList("claimed");

        if (claimedPlayers.contains(player.getUniqueId().toString())) {
            player.sendMessage(ChatColor.translateAlternateColorCodes('&', String.format(
                    "%sYou've already claimed a present- don't be greedy!", prefix
            )));
            player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1.0f, 1.0f);
            return;
        }

        claimedPlayers.add(player.getUniqueId().toString());
        getConfig().set("claimed", claimedPlayers);
        saveConfig();
        player.sendMessage(ChatColor.translateAlternateColorCodes('&', String.format(
                "%sYou claimed your present. Happy Holidays!", prefix
        )));
        player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_DIDGERIDOO, 1.0f,1.0f);
        player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_BELL, 1.0f,1.0f);
        player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_BIT, 1.0f,1.0f);
        player.playSound(player.getLocation(), Sound.UI_TOAST_CHALLENGE_COMPLETE, 1.0f,1.0f);

        final Firework firework = (Firework) player.getWorld().spawnEntity(block.getLocation().add(0.5, 0, 0.5), EntityType.FIREWORK);
        FireworkMeta fireworkMeta = firework.getFireworkMeta();
        fireworkMeta.setPower(2);
        fireworkMeta.addEffect(FireworkEffect.builder().withColor(Color.RED).flicker(true).build());
        firework.setFireworkMeta(fireworkMeta);

        for (int i = 0; i < 4; i++) {
            firework.detonate();
            final List<String> rewards = getConfig().getStringList("rewards");
            final int rewardIndex = ThreadLocalRandom.current().nextInt(0, rewards.size());
            getServer().dispatchCommand(getServer().getConsoleSender(), rewards.get(rewardIndex).replace("[player]",
                    player.getName()));
        }

        block.setType(Material.AIR);
        Particle.DustOptions dustOptions = new Particle.DustOptions(Color.GREEN, 3);
        player.getWorld().spawnParticle(Particle.REDSTONE, block.getLocation().add(0.5, 0, 0.5), 100, dustOptions);
        player.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 100, 10));
        player.sendTitle(ChatColor.translateAlternateColorCodes('&', "&c&lHappy Holidays!"),
                ChatColor.translateAlternateColorCodes('&', "- Firestarter Staff"));
    }
}
