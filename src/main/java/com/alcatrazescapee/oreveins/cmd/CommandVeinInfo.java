/*
 * Part of the Ore Veins Mod by alcatrazEscapee
 * Work under Copyright. Licensed under the GPL-3.0.
 * See the project LICENSE.md for more information.
 */

package com.alcatrazescapee.oreveins.cmd;

import java.util.Collections;
import java.util.List;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;

import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.WrongUsageException;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentString;

import com.alcatrazescapee.oreveins.api.IVeinType;
import com.alcatrazescapee.oreveins.vein.VeinRegistry;

@ParametersAreNonnullByDefault
public class CommandVeinInfo extends CommandBase
{
    @Override
    @Nonnull
    public String getName()
    {
        return "veininfo";
    }

    @Override
    @Nonnull
    public String getUsage(ICommandSender sender)
    {
        return "/veininfo [all|<vein name>] -> lists info about registered veins. Use 'all' to see all registered veins";
    }

    @Override
    public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException
    {
        if (args.length != 1) throw new WrongUsageException("Requires one argument: " + getUsage(sender));
        if (sender.getCommandSenderEntity() == null) throw new WrongUsageException("Can only be used by a player");

        sender.sendMessage(new TextComponentString("Registered Veins: "));
        if (args[0].equals("all"))
        {
            // Search for all veins
            VeinRegistry.getNames().forEach(x -> sender.sendMessage(new TextComponentString("> " + x)));
        }
        else
        {
            // Search for veins that match a type
            final IVeinType type = VeinRegistry.getVein(args[0]);
            if (type == null)
            {
                throw new WrongUsageException("Vein supplied does not match any valid vein names. Use /veininfo all to see valid vein names");
            }
            sender.sendMessage(new TextComponentString("> " + type.toString()));
        }
    }

    @Override
    @Nonnull
    public List<String> getTabCompletions(MinecraftServer server, ICommandSender sender, String[] args, @Nullable BlockPos targetPos)
    {
        if (args.length == 1)
        {
            return getListOfStringsMatchingLastWord(args, VeinRegistry.getNames());
        }
        return Collections.emptyList();
    }
}
