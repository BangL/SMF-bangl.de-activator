/*
 * Copyright (C) 2012 mewin <mewin001@hotmail.de>
 *                    BangL <henno.rickowski@googlemail.com>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package de.bangl.smfav;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.logging.Level;
import org.bukkit.plugin.Plugin;
import org.yaml.snakeyaml.Yaml;

/**
 *
 * @author mewin<mewin001@hotmail.de>
 * @author BangL <henno.rickowski@googlemail.com>
 */
public class Config {
    private Map<String, Object> values;

    public Config(final Plugin plugin) 
    {
        final Yaml yaml = new Yaml();
        final File configFile = new File(plugin.getDataFolder(), "config.yml");

        if (!configFile.exists())
        {
            createDefaultConfig(plugin);
        }
        try (FileReader reader = new FileReader(configFile)) {
                values = (Map) yaml.load(reader);
        } catch (FileNotFoundException ex) {
            plugin.getLogger().log(Level.SEVERE, "Could not load plugin config file: ", ex);
            values = (Map) yaml.load(plugin.getResource("/config.yml"));
        } catch (IOException ex) {
            plugin.getLogger().log(Level.SEVERE, "Could not load plugin config file: ", ex);
            values = (Map) yaml.load(plugin.getResource("/config.yml"));
        }
    }

    private static void createDefaultConfig(Plugin plugin)
    {
        FileOutputStream out = null;
        InputStream input = null;
        final File configFile = new File(plugin.getDataFolder(), "/config.yml");

        if (plugin.getDataFolder().exists()
                || plugin.getDataFolder().mkdirs())
        {
            try
            {
                if (configFile.exists()
                        || configFile.createNewFile())
                {
                    out = new FileOutputStream(configFile);
                    input = plugin.getResource("config.yml");

                    int next;
                    while((next = input.read()) >= 0)
                    {
                        out.write(next);
                    }
                }
                else
                {
                    plugin.getLogger().log(Level.SEVERE, "Could not create default configuration file.");
                }
            }
            catch (IOException ex)
            {
                plugin.getLogger().log(Level.SEVERE, "Could not create default configuration file: ", ex);
            }
            finally
            {
                try
                {
                    if (input != null)
                    {
                        input.close();
                    }

                    if (out != null)
                    {
                        out.close();
                    }
                }
                catch (IOException ex)
                {}
            }
        }
        else
        {
            plugin.getLogger().log(Level.SEVERE, "Could not create config folder.");
        }
    }

    private Object getValue(final String name, final Object def, final Map<String, Object> map)
    {
        final int dot = name.indexOf(".");

        if (dot > 0)
        {
            final Map subMap = (Map) map.get(name.substring(0, dot));
            
            return this.getValue(name.substring(dot + 1), def, subMap);
        }
        if (!map.containsKey(name))
        {
            return def;
        }
        else
        {
            return map.get(name);
        }
    }

    public Object getObject(final String name, final Object def)
    {
        return this.getValue(name, def, values);
    }

    public String getString(final String name, final String def)
    {
        return (String) this.getObject(name, (Object) def);
    }

    public int getInt(final String name, final int def)
    {
        return (int) this.getObject(name, (Object) def);
    }

    public double getDouble(final String name, final double def)
    {
        return (double) this.getObject(name, (Object) def);
    }
}
