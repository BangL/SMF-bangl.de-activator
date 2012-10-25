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
package de.bangl.smfav.listener;

import de.bangl.smfav.SMFAccountValidatorPlugin;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import ru.tehkode.permissions.bukkit.PermissionsEx;

/**
 *
 * @author BangL <henno.rickowski@googlemail.com>
 */
public class PlayerListener implements Listener {
    SMFAccountValidatorPlugin plugin;
    PermissionsEx pex;

    public PlayerListener(SMFAccountValidatorPlugin plugin)
    {
        this.plugin = plugin;
        this.plugin.getServer().getPluginManager().registerEvents(this, this.plugin);
        this.pex = (PermissionsEx) plugin.getServer().getPluginManager().getPlugin("PermissionsEx");
    }

    @EventHandler(priority = EventPriority.LOW)
    public void onPlayerJoin(PlayerJoinEvent event)
    {
        if (this.plugin.getCfg().getBoolean("cb.set", true))
        {
            Player player = event.getPlayer();
            if (PermissionsEx.getUser(player).inGroup(this.plugin.getCfg().getString("cb.rank", "Validated")))
            {
                int memberId = -1;
                try
                {
                    memberId = this.plugin.getDbc().getMemberId(player, this.plugin.getCfg());
                }
                catch (Exception ex)
                {
                }
                if (memberId >= 0)
                {
                    try
                    {
                        if (this.plugin.getDbc().isValidated(player, this.plugin.getCfg(), memberId))
                        {
                            return;
                        }
                    }
                    catch (Exception ex)
                    {
                    }
                }
                Bukkit.getServer().dispatchCommand(Bukkit.getServer().getConsoleSender(), "cbrank " + player.getName() + " " + this.plugin.getCfg().getString("cb.unrank", "Forum"));
            }
        }
    }
}
