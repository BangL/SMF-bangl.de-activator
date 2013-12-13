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
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

/**
 *
 * @author BangL <henno.rickowski@googlemail.com>
 * @author mewin <mewin001@hotmail.de>
 */
public class SMFAccountValidatorPlugin extends JavaPlugin
{
    private Config config;
    private DatabaseConnector dbc;
    private PlayerListener listener;
    private ru.tehkode.permissions.bukkit.PermissionsEx Pex;

    @Override
    public void onEnable() {
        this.config = new Config(this);
        this.dbc = new DatabaseConnector();
        this.dbc.init(config.getString("sql.dns", ""),
                config.getString("sql.username", ""),
                config.getString("sql.password", ""),
                config.getString("sql.prefix", ""),
                this);
        this.dbc.connect();

        PluginManager pm = this.getServer().getPluginManager();
        this.Pex = (ru.tehkode.permissions.bukkit.PermissionsEx) pm.getPlugin("PermissionsEx");
        this.listener = new PlayerListener(this);
    }

    @Override
    public void onDisable() {
        this.listener = null;
        this.dbc.destroy();
        this.dbc = null;
        this.config = null;
    }

    public Boolean isUserInGroup(Player player, String group) {
        return this.Pex.getUser(player).inGroup(group, false);
    }
    
    public void setUserGroup(Player player, String group) {
        String[] groups = new String[1];
        groups[0] = group;
        this.Pex.getUser(player).setGroups(groups);
    }
    
    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        Boolean handled = false;
        if (cmd.getName().equalsIgnoreCase("va")
                || cmd.getName().equalsIgnoreCase("av")
                || cmd.getName().equalsIgnoreCase("validate")) {
            try {
                final Player player = (Player)sender;
                if (sender instanceof Player) {
                    final List<Integer> memberIds = this.dbc.getMemberIds(player, this.config);
                    if (this.dbc.isValidated(player, this.config, memberIds)) {
                        sender.sendMessage(ChatColor.RED + config.getString("messages.alreadyvalid", "This minecraft name is already validated."));
                    } else if (args == null
                            || args[0].trim().isEmpty()) {
                        sender.sendMessage(ChatColor.RED + config.getString("messages.invalidcount", "Invalid argument count."));
                        return false;
                    } else {
                        Integer validMemberId = this.dbc.isValidCode(player, args[0].trim(), this.config, memberIds);
                        if (validMemberId == -1) {
                            sender.sendMessage(ChatColor.RED + config.getString("messages.invalidcode", "Invalid code."));
                        } else {
                            this.dbc.setValidated(player, config, validMemberId);
                            String group = this.config.getString("setgroup.to", "Mitglied");
                            setUserGroup(player, group);
                            getLogger().log(Level.INFO, player.getName() + " typed in a valid code and is now validated. Set to group " + group);
                            sender.sendMessage(ChatColor.GREEN + config.getString("messages.validated", "This minecraft name is now validated!"));
                        }
                    }
                } else {
                    sender.sendMessage(ChatColor.RED + config.getString("messages.playeronly", "This command can only be run by a player."));
                }
                handled = true;
            } catch (Exception ex) {
                sender.sendMessage(ChatColor.RED
                        + config.getString("messages.errorvalidate", "There was an error while validating your minecraft name. Please contact one of our admins."));
                Logger.getLogger(SMFAccountValidatorPlugin.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return handled;
    }

    public Config getCfg() {
        return this.config;
    }

    public DatabaseConnector getDbc() {
        return this.dbc;
    }
}
