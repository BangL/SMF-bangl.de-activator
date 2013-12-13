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
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

/**
 *
 * @author BangL <henno.rickowski@googlemail.com>
 */
public class PlayerListener implements Listener {
    SMFAccountValidatorPlugin plugin;
  
    public PlayerListener(SMFAccountValidatorPlugin plugin) {
        this.plugin = plugin;
        this.plugin.getServer().getPluginManager().registerEvents(this, this.plugin);
    }

    @EventHandler(priority = EventPriority.LOW)
    public void onPlayerJoin(PlayerJoinEvent event) {
        final Player player = event.getPlayer();
        this.plugin.getServer().getScheduler().runTaskAsynchronously(this.plugin, new Runnable() {
            public void run() {
                String from = plugin.getCfg().getString("setgroup.from", "Forum");
                String to = plugin.getCfg().getString("setgroup.to", "Mitglied");
                if (plugin.isUserInGroup(player, from)) {
                    final List<Integer> memberIds = plugin.getDbc().getMemberIds(player, plugin.getCfg());
                    if (plugin.getDbc().isValidated(player, plugin.getCfg(), memberIds)) {
                        plugin.setUserGroup(player, to);
                        plugin.getLogger().log(Level.INFO, player.getName() + " is validated. Set to group " + to);
                    }
                } else {
                    List<Integer> memberIds = new ArrayList<Integer>();
                    try {
                        memberIds = plugin.getDbc().getMemberIds(player, plugin.getCfg());
                    } catch (Exception ex) {
                    }
                    if (!memberIds.isEmpty()) {
                        try {
                            if (plugin.getDbc().isValidated(player, plugin.getCfg(), memberIds)) {
                                plugin.getLogger().log(Level.INFO, player.getName() + " is validated.");
                                return;
                            }
                        } catch (Exception ex) {
                        }
                    }
                    plugin.setUserGroup(player, from);
                    plugin.getLogger().log(Level.INFO, player.getName() + " is not validated. Set to group " + from);
                }
            }
        });
    }
}
