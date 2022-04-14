package net.islandearth.anvillogin.translation;

import com.convallyria.languagy.api.language.Language;
import com.convallyria.languagy.api.language.key.LanguageKey;
import com.convallyria.languagy.api.language.key.TranslationKey;
import com.convallyria.languagy.api.language.translation.Translation;
import me.clip.placeholderapi.PlaceholderAPI;
import net.islandearth.anvillogin.AnvilLogin;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public enum Translations {
	KICKED(TranslationKey.of("kicked")),
	LOGGED_IN(TranslationKey.of("logged_in")),
	GUI_TITLE(TranslationKey.of("gui_title")),
	GUI_TEXT(TranslationKey.of("gui_text")),
	GUI_WRONG(TranslationKey.of("gui_wrong"));

	private final TranslationKey key;
	private final boolean isList;

	Translations(TranslationKey key) {
		this.key = key;
		this.isList = false;
	}

	public boolean isList() {
		return isList;
	}

	private String getPath() {
		return this.toString().toLowerCase();
	}

	public void send(Player player, Object... values) {
		final Translation translation = AnvilLogin.getAPI().getTranslator().getTranslationFor(player, key);
		for (String translationString : translation.colour()) {
			player.sendMessage(this.setPapi(player, replaceVariables(translationString, values)));
		}
	}

	public List<String> get(Player player, Object... values) {
		final Translation translation = AnvilLogin.getAPI().getTranslator().getTranslationFor(player, key);
		List<String> transformed = new ArrayList<>();
		for (String translationString : translation.colour()) {
			transformed.add(this.setPapi(player, replaceVariables(translationString, values)));
		}
		return transformed;
	}

	public static void generateLang(AnvilLogin plugin) {
		File lang = new File(plugin.getDataFolder() + "/lang/");
		lang.mkdirs();

		for (Language language : Language.values()) {
			final LanguageKey languageKey = language.getKey();
			try {
				plugin.saveResource("lang/" + languageKey.getCode() + ".yml", false);
				plugin.getLogger().info("Generated " + languageKey.getCode() + ".yml");
			} catch (IllegalArgumentException ignored) { }

			File file = new File(plugin.getDataFolder() + "/lang/" + languageKey.getCode() + ".yml");
			if (file.exists()) {
				FileConfiguration config = YamlConfiguration.loadConfiguration(file);
				for (Translations key : values()) {
					if (config.get(key.toString().toLowerCase()) == null) {
						plugin.getLogger().warning("No value in translation file for key "
								+ key + " was found. Please regenerate or edit your language files with new values!");
					}
				}
			}
		}
	}

	private String replaceVariables(String message, Object... values) {
		String modifiedMessage = message;
		for (int i = 0; i < 10; i++) {
			if (values.length > i) modifiedMessage = modifiedMessage.replaceAll("%" + i, String.valueOf(values[i]));
			else break;
		}

		return modifiedMessage;
	}

	private String setPapi(Player player, String message) {
		if (Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null) {
			return PlaceholderAPI.setPlaceholders(player, message);
		}

		return message;
	}
}
