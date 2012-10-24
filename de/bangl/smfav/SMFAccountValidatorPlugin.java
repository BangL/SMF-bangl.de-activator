/*
 * Copyright (C) 2012 BangL <henno.rickowski@googlemail.com>
 *                    mewin <mewin001@hotmail.de>
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

import java.util.logging.Level;
import java.util.logging.Logger;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

/**
 *
 * @author BangL <henno.rickowski@googlemail.com>
 * @author mewin<mewin001@hotmail.de>
 */
public class SMFAccountValidatorPlugin extends JavaPlugin
{
    private Config config;
    private DatabaseConnector dbc;

    @Override
    public void onEnable()
    {
        config = new Config(this);
        dbc = new DatabaseConnector();
        dbc.init(config.getString("sql.dns", ""),
                config.getString("sql.username", ""),
                config.getString("sql.password", ""),
                config.getString("sql.prefix", ""),
                this);
        dbc.connect();
    }

    @Override
    public void onDisable()
    {
        //dbc.save();
        dbc.destroy();
        dbc = null;
        //config.save();
        config = null;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args)
    {
        Boolean handled = false;
        if (cmd.getName().equalsIgnoreCase("va"))
        {
            try {
                final Player player = (Player)sender;
                if (!(sender instanceof Player))
                {
                    sender.sendMessage(ChatColor.RED + "[" + this.getDescription().getName() + "] "
                            + "This command can only be run by a player.");
                }
                else if (args.length != 1)
                {
                    sender.sendMessage(ChatColor.RED + "[" + this.getDescription().getName() + "] "
                            + "Invalid argument count.");
                }
                else if (dbc.isValidated(player, config))
                {
                    sender.sendMessage(ChatColor.RED + "[" + this.getDescription().getName() + "] "
                            + "This minecraft name is already validated.");
                }
                else if (args[0].trim().isEmpty()
                        || !dbc.isValidCode(player, args[0].trim(), config))
                {
                    sender.sendMessage(ChatColor.RED + "[" + this.getDescription().getName() + "] "
                            + "Invalid code.");
                }
                else
                {
                    dbc.setValidated(player, config);
                    sender.sendMessage(ChatColor.DARK_GREEN + "[" + this.getDescription().getName() + "] "
                            + "This minecraft name is now validated!");

                    // optional CommunityBridge support
                    if (this.getServer().getPluginManager().getPlugin("CommunityBridge") != null
                            && config.getBoolean("cb.set", true))
                    {
                        Bukkit.getServer().dispatchCommand(Bukkit.getServer().getConsoleSender(), "/cbrank " + player.getName() + " " + config.getString("cb.rank", "Validated"));
                    }
                }
                handled = true;
            } catch (Exception ex) {
                sender.sendMessage(ChatColor.RED + "[" + this.getDescription().getName() + "] "
                        + "There was an error while validating your minecraft name. Please contact one of our admins.");
                Logger.getLogger(SMFAccountValidatorPlugin.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return handled;
    }
}
