/*
 *  This file is a part of project QuickShop, the name is ConfigUpdateScript.java
 *  Copyright (C) Ghost_chu and contributors
 *
 *  This program is free software: you can redistribute it and/or modify it
 *  under the terms of the GNU General Public License as published by the
 *  Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful, but WITHOUT
 *  ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 *  FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License
 *  for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program. If not, see <http://www.gnu.org/licenses/>.
 *
 */

package com.ghostchu.quickshop.util.config;

import com.dumptruckman.bukkit.configuration.json.JsonConfiguration;
import com.ghostchu.quickshop.QuickShop;
import de.themoep.minedown.adventure.MineDown;
import de.themoep.minedown.adventure.MineDownParser;
import lombok.Getter;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.UUID;
import java.util.logging.Level;

@SuppressWarnings("unused")
public class ConfigUpdateScript {
    private final QuickShop plugin;
    @Getter
    private final FileConfiguration config;

    public ConfigUpdateScript(@NotNull FileConfiguration config, @NotNull QuickShop plugin) {
        this.config = config;
        this.plugin = plugin;
    }

    @UpdateScript(version = 1000)
    public void updateCustomTranslationKey() {
        getConfig().set("custom-translation-key", new ArrayList<>());
    }

    @UpdateScript(version = 1001)
    public void shopName() {
        getConfig().set("shop.name-fee", 0);
        getConfig().set("shop.name-max-length", 32);
        getConfig().set("matcher.item.bundle", true);
    }

    @UpdateScript(version = 1002)
    public void adventureMiniMessage() {
        getConfig().set("syntax-parser", 0);
    }

    @UpdateScript(version = 1003)
    public void metricAndPapiController() {
        getConfig().set("transaction-metric.enable", true);
        boolean papiEnabled = getConfig().getBoolean("plugin.PlaceHolderAPI", true);
        getConfig().set("plugin.PlaceHolderAPI", null);
        getConfig().set("plugin.PlaceHolderAPI.enable", papiEnabled);
        getConfig().set("plugin.PlaceHolderAPI.cache", 15 * 60 * 1000);
    }

    @UpdateScript(version = 1004)
    public void configurableDatabaseProperties() {
        getConfig().set("database.queue", null);
        getConfig().set("database.queue-commit-interval", null);
        getConfig().set("database.auto-fix-encoding-issue-in-database", null);
        getConfig().set("database.properties.connection-timeout", 60000);
        getConfig().set("database.properties.validation-timeout", 3000);
        getConfig().set("database.properties.idle-timeout", 60000);
        getConfig().set("database.properties.login-timeout", 10);
        getConfig().set("database.properties.maxLifeTime", 60000);
        getConfig().set("database.properties.maximum-pool-size", 8);
        getConfig().set("database.properties.minimum-idle", 2);
        getConfig().set("database.properties.cachePrepStmts", true);
        getConfig().set("database.properties.prepStmtCacheSize", 250);
        getConfig().set("database.properties.prepStmtCacheSqlLimit", 2048);
        getConfig().set("database.properties.useUnicode", true);
        getConfig().set("database.properties.characterEncoding", "utf8");
        getConfig().set("database.properties.connection-timeout", 60000);
    }

    @UpdateScript(version = 1006)
    public void migrateToMiniMessage() {
        File locales = new File(plugin.getDataFolder(), "overrides");
        if (!locales.exists())
            return;
        for (File file : locales.listFiles()) {
            if (!file.isDirectory())
                continue;
            File jsonFile = new File(file, "messages.json");
            if (!jsonFile.exists())
                continue;
            try {
                File yamlFile = new File(jsonFile.getParent(), jsonFile.getName().replace(".json", ".yml"));
                yamlFile.createNewFile();
                YamlConfiguration yamlConfiguration = new YamlConfiguration();
                JsonConfiguration jsonConfiguration = JsonConfiguration.loadConfiguration(jsonFile);
                jsonConfiguration.getKeys(true).forEach(key -> yamlConfiguration.set(key, translate(jsonConfiguration.get(key))));
                try {
                    Files.copy(jsonFile.toPath(), new File(jsonFile.getParent(), jsonFile.getName() + ".bak").toPath());
                } catch (IOException e) {
                    Files.copy(jsonFile.toPath(), new File(jsonFile.getParent(), jsonFile.getName() + ".bak." + UUID.randomUUID().toString().replace("-", "")).toPath());
                }
                jsonFile.deleteOnExit();
                yamlConfiguration.save(yamlFile);
            } catch (Exception e) {
                plugin.getLogger().log(Level.WARNING, "Failed to upgrade override translation file " + jsonFile.getName(), e);
            }
        }
        getConfig().set("syntax-parser", null);
    }

    @UpdateScript(version = 1007)
    public void refundFromTaxAccountOption() {
        getConfig().set("shop.refund-from-tax-account", false);
    }

    @UpdateScript(version = 1008)
    public void disableTaxForUnlimitedShop() {
        getConfig().set("tax-free-for-unlimited-shop", false);
    }

    private Object translate(Object o) {
        if (o instanceof String str) {
            Component component = MineDown.parse(str);
            return MiniMessage.miniMessage().serialize(component);
        }
        return o;
    }
}
