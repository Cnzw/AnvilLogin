package net.islandearth.anvillogin.listeners;

import com.convallyria.languagy.api.language.Language;
import com.github.games647.fastlogin.bukkit.FastLoginBukkit;
import com.github.games647.fastlogin.core.PremiumStatus;
import com.google.common.base.Enums;
import fr.xephi.authme.api.v3.AuthMeApi;
import net.islandearth.anvillogin.AnvilLogin;
import net.islandearth.anvillogin.translation.Translations;
import net.islandearth.anvillogin.util.Colors;
import net.islandearth.anvillogin.util.ItemStackBuilder;
import net.wesjd.anvilgui.AnvilGUI;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class PlayerListener implements Listener {

    private final AnvilLogin plugin;

    public PlayerListener(AnvilLogin plugin) {
        this.plugin = plugin;
    }
    
    @EventHandler
    public void onJoin(PlayerJoinEvent pje) {
        Player myPlayer = pje.getPlayer();
        if ((plugin.getConfig().getBoolean("disable-op-bypass", true)
                || !myPlayer.hasPermission("AnvilLogin.bypass"))
                && !plugin.getLoggedIn().contains(myPlayer.getUniqueId())) {
            if (plugin.isAuthme()
                    && (AuthMeApi.getInstance().isAuthenticated(myPlayer) || AuthMeApi.getInstance().isUnrestricted(myPlayer))) {
                return;
            }

            if (plugin.getConfig().getBoolean("fastlogin")) {
                if (Bukkit.getPluginManager().getPlugin("FastLogin") != null) {
                    FastLoginBukkit fastLogin = (FastLoginBukkit) Bukkit.getPluginManager().getPlugin("FastLogin");
                    if (fastLogin != null) {
                        PremiumStatus premiumStatus = fastLogin.getStatus(myPlayer.getUniqueId());
                        if (premiumStatus == PremiumStatus.PREMIUM) {
                            if (plugin.debug()) {
                                plugin.getLogger().info("Skipping player " + myPlayer.getName() + " because they are premium.");
                            }
                            return;
                        }
                    }
                }
            }

            plugin.getNotLoggedIn().add(myPlayer.getUniqueId());

            List<String> colouredLeftLore = new ArrayList<>();
            for (String leftLore : plugin.getConfig().getStringList("left_slot.lore")) {
                colouredLeftLore.add(Colors.color(leftLore));
            }

            List<String> colouredRightLore = new ArrayList<>();
            for (String rightLore : plugin.getConfig().getStringList("right_slot.lore")) {
                colouredRightLore.add(Colors.color(rightLore));
            }

            final ItemStack leftItem = ItemStackBuilder.of(Enums.getIfPresent(Material.class, plugin.getConfig().getString("left_slot.type", "ANVIL")).or(Material.ANVIL))
                    .addFlags(ItemFlag.HIDE_ATTRIBUTES)
                    .withLore(colouredLeftLore)
                    .withModel(plugin.getConfig().getInt("left_slot.model")).build();

            final Material rightType = Enums.getIfPresent(Material.class, plugin.getConfig().getString("right_slot.type", "AIR")).or(Material.AIR);
            final ItemStack rightItem = rightType == Material.AIR ? new ItemStack(Material.AIR) : ItemStackBuilder.of(rightType)
                    .addFlags(ItemFlag.HIDE_ATTRIBUTES)
                    .withLore(colouredRightLore)
                    .withName(Colors.color(plugin.getConfig().getString("right_slot.name", "")))
                    .withModel(plugin.getConfig().getInt("right_slot.model")).build();

            AnvilGUI.Builder anvilGUI = new AnvilGUI.Builder()
                    .onClick((slot, snapshot) -> {
                        if (slot != AnvilGUI.Slot.OUTPUT) {
                            return Collections.emptyList();
                        }

                        final Player player = snapshot.getPlayer();
                        final String text = snapshot.getText();
                        if (plugin.isAuthme() && plugin.getConfig().getBoolean("register") && !AuthMeApi.getInstance().isRegistered(player.getName())) {
                            AuthMeApi.getInstance().forceRegister(player, text, true);
                            plugin.getLoggedIn().add(player.getUniqueId());
                            plugin.getNotLoggedIn().remove(player.getUniqueId());
                            if (plugin.getConfig().getBoolean("login_messages")) {
                                Translations.LOGGED_IN.send(player);
                            }
                            return List.of(AnvilGUI.ResponseAction.close());
                        }

                        if (text.equalsIgnoreCase(plugin.getConfig().getString("Password"))
                                || (plugin.isAuthme() && AuthMeApi.getInstance().checkPassword(player.getName(), text))) {
                            plugin.getLoggedIn().add(player.getUniqueId());
                            plugin.getNotLoggedIn().remove(player.getUniqueId());
                            if (plugin.getConfig().getBoolean("login_messages")) {
                                Translations.LOGGED_IN.send(player);
                            }
                            if (plugin.isAuthme()) AuthMeApi.getInstance().forceLogin(player);
                            player.setLevel(player.getLevel());
                            return List.of(AnvilGUI.ResponseAction.close());
                        } else {
                            return List.of(AnvilGUI.ResponseAction.replaceInputText(Translations.GUI_WRONG.get(myPlayer).get(0)));
                        }
                    })
                    .preventClose()
                    .text(Translations.GUI_TEXT.get(myPlayer).get(0))
                    .itemLeft(leftItem)
                    .itemRight(rightItem)
                    .title(Translations.GUI_TITLE.get(myPlayer).get(0))  //only works in 1.14+
                    .plugin(plugin);
            Bukkit.getScheduler().runTaskLater(plugin, () -> anvilGUI.open(myPlayer), 20L);

            if (plugin.getConfig().getBoolean("Timeout")) {
                Bukkit.getScheduler().runTaskLater(plugin, () -> {
                    if (!plugin.getLoggedIn().contains(myPlayer.getUniqueId())) {
                        myPlayer.kickPlayer(Translations.KICKED.get(myPlayer).get(0));
                    }
                }, plugin.getConfig().getLong("Time"));
            }
        }
    }

    @EventHandler
    public void onMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();
        if (plugin.getNotLoggedIn().contains(player.getUniqueId())) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onClick(InventoryClickEvent event) {
        Player player = (Player) event.getView().getPlayer();
        if (plugin.getNotLoggedIn().contains(player.getUniqueId())) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onDamage(EntityDamageByEntityEvent event) {
        if (event.getEntity() instanceof Player player) {
            if (plugin.getNotLoggedIn().contains(player.getUniqueId())) {
                event.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent pqe) {
        Player player = pqe.getPlayer();
        plugin.getLoggedIn().remove(player.getUniqueId());
        plugin.getNotLoggedIn().remove(player.getUniqueId());
    }
}
