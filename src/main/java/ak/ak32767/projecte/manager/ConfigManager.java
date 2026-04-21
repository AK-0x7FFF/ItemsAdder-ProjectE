package ak.ak32767.projecte.manager;

import ak.ak32767.projecte.ProjectE;
import org.bukkit.configuration.file.FileConfiguration;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;

public class ConfigManager {
    private final ProjectE plugin;
    private final FileConfiguration config;

    public ConfigManager(ProjectE plugin) {
        this.plugin = plugin;

        plugin.saveDefaultConfig();
        this.config = plugin.getConfig();

        this.setupConfig();
    }
    public void setupConfig() {
        String[] fileNames = new String[]{"custom_emc.yml"};
        for (String name : fileNames) {
            File file = new File(plugin.getDataFolder(), name);
            if (file.exists())
                continue;

            try (InputStream stream = plugin.getResource("config/" + name)) {
                if (stream == null)
                    throw new FileNotFoundException(file.toPath().toString());

                Files.copy(stream, file.toPath(), StandardCopyOption.REPLACE_EXISTING);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }


}
