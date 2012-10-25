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

import de.bangl.smfav.listener.PlayerListener;
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
    private PlayerListener listener;
    private Boolean hasCommunityBridge;

    @Override
    public void onEnable()
    {
        this.hasCommunityBridge = this.getServer().getPluginManager().getPlugin("CommunityBridge") != null;
        this.config = new Config(this);
        this.dbc = new DatabaseConnector();
        this.dbc.init(config.getString("sql.dns", ""),
                config.getString("sql.username", ""),
                config.getString("sql.password", ""),
                config.getString("sql.prefix", ""),
                this);
        this.dbc.connect();
        if (this.hasCommunityBridge)
        {
            this.listener = new PlayerListener(this);
        }
    }

    @Override
    public void onDisable()
    {
        this.listener = null;
        //dbc.save();
        this.dbc.destroy();
        this.dbc = null;
        //config.save();
        this.config = null;
        this.hasCommunityBridge = false;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args)
    {
        Boolean handled = false;
        if (cmd.getName().equalsIgnoreCase("va")
                || cmd.getName().equalsIgnoreCase("av")
                || cmd.getName().equalsIgnoreCase("validate"))
        {
            try {
                final Player player = (Player)sender;
                if (!(sender instanceof Player))
                {
                    sender.sendMessage(ChatColor.RED + "This command can only be run by a player.");
                }
                else if (args.length != 1)
                {
                    sender.sendMessage(ChatColor.RED + "Invalid argument count.");
                }
                final int memberId = this.dbc.getMemberId(player, this.config);
                if (this.dbc.isValidated(player, this.config, memberId))
                {
                    sender.sendMessage(ChatColor.RED + "This minecraft name is already validated.");
                }
                else if (args[0].trim().isEmpty()
                        || !this.dbc.isValidCode(player, args[0].trim(), this.config, memberId))
                {
                    sender.sendMessage(ChatColor.RED + "Invalid code.");
                }
                else
                {
                    this.dbc.setValidated(player, config, memberId);

                    // optional CommunityBridge support
                    if (this.hasCommunityBridge
                            && this.config.getBoolean("cb.set", true))
                    {
                        Bukkit.getServer().dispatchCommand(Bukkit.getServer().getConsoleSender(), "cbrank " + player.getName() + " " + this.config.getString("cb.rank", "Validated"));
                    }
                    else
                    {
                        sender.sendMessage(ChatColor.GREEN + "This minecraft name is now validated!");
                    }
                }
                handled = true;
            } catch (Exception ex) {
                sender.sendMessage(ChatColor.RED
                        + "There was an error while validating your minecraft name. Please contact one of our admins.");
                Logger.getLogger(SMFAccountValidatorPlugin.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return handled;
    }

    public Config getCfg()
    {
        return this.config;
    }

    public DatabaseConnector getDbc()
    {
        return this.dbc;
    }
}
