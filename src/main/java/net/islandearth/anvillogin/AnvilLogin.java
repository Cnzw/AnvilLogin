package net.islandearth.anvillogin;

import com.convallyria.languagy.api.language.Translator;
import net.islandearth.anvillogin.api.AnvilLoginAPI;
import net.islandearth.anvillogin.listeners.PlayerListener;
import net.islandearth.anvillogin.translation.Translations;
import net.wesjd.anvilgui.version.VersionMatcher;
import org.bukkit.Bukkit;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class AnvilLogin extends JavaPlugin implements AnvilLoginAPI {
    
    private final List<UUID> loggedIn = new ArrayList<>();

    public List<UUID> getLoggedIn() {
        return loggedIn;
    }

    public List<UUID> getNotLoggedIn() {
        return notLoggedIn;
    }

    @Override
    public Translator getTranslator() {
        return translator;
    }

    public boolean isAuthme() {
        return authme;
    }

    private final List<UUID> notLoggedIn = new ArrayList<>();

    private Translator translator;

    private boolean authme;
    private static AnvilLogin plugin;

    @Override
    public void onEnable() {
        try {
            new VersionMatcher().match();
        } catch (RuntimeException e) {
            this.getLogger().severe("Your server version is not supported! Please update to the latest version!");
            Bukkit.getPluginManager().disablePlugin(this);
            return;
        }
        
        if (Bukkit.getPluginManager().getPlugin("AuthMe") != null) {
            this.getLogger().info("Found authme!");
            this.authme = true;
        } else this.authme = false;

        plugin = this;
        createFiles();

        this.translator = Translator.of(this).debug(debug());

        registerListeners();
        this.getLogger().info("[AnvilLogin] Enabled & registered events!");
    }
    
    private void createFiles() {
        saveDefaultConfig();
        Translations.generateLang(this);
    }
    
    private void registerListeners() {
        PluginManager pm = Bukkit.getPluginManager();
        pm.registerEvents(new PlayerListener(this), this);
    }

    public boolean debug() {
        return this.getConfig().getBoolean("debug");
    }

    public static AnvilLoginAPI getAPI() {
        return plugin;
    }
}
