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

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.Level;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

/**
 *
 * @author mewin<mewin001@hotmail.de>
 * @author BangL <henno.rickowski@googlemail.com>
 */
public class DatabaseConnector {
    private static DatabaseConnector instance;

    private String dns, username, password, prefix;
    private Plugin plugin;
    private Connection connection;
    private boolean initialized;
    private SimpleDateFormat sdf;

    public DatabaseConnector()
    {
        sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    }

    // technical things
    public void init(String dns, String username, String password, String prefix, Plugin plugin)
    {
        this.dns = dns;
        this.username = username;
        this.password = password;
        this.prefix = prefix;
        this.plugin = plugin;

        instance = this;

        initialized = true;
    }

    public void destroy()
    {
        if (this.connected())
        {
            this.disconnect();
        }
        instance = null;
    }

    public void connect()
    {
        if (!initialized)
        {
            plugin.getLogger().log(Level.WARNING, "Trying to connect to database while not initialized.");
            return;
        }
        if (connection != null)
        {
            plugin.getLogger().log(Level.WARNING, "Trying to connect to database while already connected.");
            return;
        }
        plugin.getLogger().log(Level.INFO, "Connecting to database...");
        try {
            connection = DriverManager.getConnection(dns, username, password);
        } catch (SQLException ex) {
            plugin.getLogger().log(Level.SEVERE, "Could not connect to database: ", ex);
            return;
        }
        plugin.getLogger().log(Level.FINE, "Successfully connected.");
    }

    public void disconnect()
    {
        if (connection == null)
        {
            plugin.getLogger().log(Level.WARNING, "Trying to disconnect while not connected.");
            return;
        }
        try {
            connection.close();
        } catch (SQLException ex) {
            plugin.getLogger().log(Level.WARNING, "Could not close connection: ", ex);
        }
        connection = null;
    }

    public boolean connected()
    {
        if (connection == null)
        {
            return false;
        }
        else
        {
            try {
                if (connection.isClosed())
                {
                    return false;
                }
                else
                {
                    Statement st = connection.createStatement();
                    boolean result;

                    try
                    {
                        result = st.execute("SELECT 1");
                    }
                    catch(Exception ex)
                    {
                        return false;
                    }

                    return result;
                }
            } catch (SQLException ex) {
                plugin.getLogger().log(Level.SEVERE, "Could not change connection status: ", ex);
                return false;
            }
        }
    }

    public boolean execute(String sql, Object ... params)
    {
        if (!this.connected())
        {
            this.connect();
        }

        Statement statement;
        try {
            statement = connection.createStatement();
        } catch (SQLException ex) {
            plugin.getLogger().log(Level.SEVERE, "Could not create SQL statement: ", ex);
            return false;
        }
        try
        {
            statement.executeUpdate(replaceParams(sql, params));
        } catch(SQLException ex) {
            plugin.getLogger().log(Level.SEVERE, "Could not execute SQL statement: " + sql + " :", ex);
            return false;
        }
        return true;
    }

    public ResultSet executeQuery(String sql, Object ... params)
    {
        if (!this.connected())
        {
            this.connect();
        }
        Statement statement;
        try {
            statement = connection.createStatement();
        } catch (SQLException ex) {
            plugin.getLogger().log(Level.SEVERE, "Could not create SQL statement: ", ex);
            return null;
        }
        try
        {
            return statement.executeQuery(replaceParams(sql, params));
        } catch(SQLException ex) {
            plugin.getLogger().log(Level.SEVERE, "Could not execute SQL statement: " + sql + " :", ex);
            return null;
        }
    }

    private String replaceParams(String sql, Object[] params)
    {
        String returnString = sql;

        returnString = returnString.replace("{PREFIX}", prefix);

        for(int i = 0; i < params.length; i++)
        {
            if (params[i] != null)
            {
                String replaceString = params[i].toString();

                if (params[i] instanceof Boolean)
                {
                    replaceString = ((boolean) params[i] ? "1" : "0");
                }
                else if (params[i] instanceof Date)
                {
                    replaceString = sdf.format((Date) params[i]);
                }

                replaceString = replaceString.replace("'", "\\'");

                if (!(params[i] instanceof Integer ||
                    params[i] instanceof Double ||
                    params[i] instanceof Short ||
                    params[i] instanceof Byte ||
                    params[i] instanceof Long ||
                    params[i] instanceof Boolean))
                {
                    replaceString = "'" + replaceString + "'";
                }

                returnString = returnString.replace("{" + i + "}", replaceString);
            }
            else
            {
                returnString = returnString.replace("{" + i + "}", "NULL");
            }
        }

        return returnString;
    }

    // functional things

    public int getMemberId(final Player player, final Config config)
    {
        final ResultSet result = this.executeQuery("SELECT"
                + " id_member"
                + " FROM {PREFIX}themes"
                + " WHERE id_theme = 1"
                + " AND variable = 'cust_minecr'"
                + " AND value = {0}"
                , player.getName());
        try
        {
            while (result.next())
            {
                return result.getInt("id_member");
            }
        }
        catch (SQLException ex)
        {
            plugin.getLogger().log(Level.SEVERE,
                    "Error loading member id of player \"" + player.getName() + "\": ", ex);
        }
        return -1;
    }

    public boolean isValidated(final Player player, final Config config, final int memberId)
    {
        Boolean valid = false;
        final ResultSet result = this.executeQuery("SELECT"
                + " value"
                + " FROM {PREFIX}themes"
                + " WHERE id_theme = 1"
                + " AND variable = 'cust_valida'"
                + " AND id_member = {0}"
                , memberId);
        try
        {
            result.next();
            valid = result.getBoolean("value");
            result.close();
        }
        catch (SQLException ex) 
        {
            plugin.getLogger().log(Level.SEVERE,
                    "Error checking validation of player \"" + player.getName() + "\": ", ex);
        }
        return valid;
    }

    public boolean isValidCode(final Player player, final String code, final Config config, final int memberId)
    {
        Boolean valid = false;
        final ResultSet result = this.executeQuery("SELECT"
                + " value"
                + " FROM {PREFIX}themes"
                + " WHERE id_theme = 1"
                + " AND variable = 'cust_valida0'"
                + " AND id_member = {0}"
                , memberId);
        try
        {
            result.next();
            valid = result.getString("value").equalsIgnoreCase(code);
            result.close();
        }
        catch (SQLException ex)
        {
            plugin.getLogger().log(Level.SEVERE,
                    "Error checking validate code of player \"" + player.getName() + "\": ", ex);
        }
        return valid;
    }

    public boolean setValidated(final Player player, final Config config, final int memberId)
    {
        return (// Set validated to true
                this.execute(
                        "UPDATE {PREFIX}themes SET " +
                        "value = 1 " +
                        "WHERE id_theme = 1 " +
                        "AND id_member = {0} " +
                        "AND variable = 'cust_valida'",
                        memberId)
                &&
                // Delete validation code
                this.execute(
                        "DELETE FROM {PREFIX}themes " +
                        "WHERE id_theme = 1 " +
                        "AND id_member = {0} " +
                        "AND variable = 'cust_valida0'",
                        memberId)
                );
    }
}
