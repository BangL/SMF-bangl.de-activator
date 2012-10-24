/*
 * Copyright (C) 2012 BangL <henno.rickowski@googlemail.com>
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

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

/**
 *
 * @author BangL <henno.rickowski@googlemail.com>
 */
public class SMFAccountValidatorPlugin extends JavaPlugin {

    @Override
    public void onEnable()
    {
        
    }

    @Override
    public void onDisable()
    {
        
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args)
    {
        if (cmd.getName().equalsIgnoreCase("va"))
        {
            if (!(sender instanceof Player))
            {
                sender.sendMessage(ChatColor.RED + "[" + this.getDescription().getName() + "] "
                        + "This command can only be run by a player");
                return false;
            }

            if (args.length != 1)
            {
                sender.sendMessage(ChatColor.RED + "[" + this.getDescription().getName() + "] "
                        + "Invalid argument count.");
                return false;
            }

            final Player player = (Player)sender;
            return Utils.Validate(player, args[0]);
        }
        return false;
    }
}
