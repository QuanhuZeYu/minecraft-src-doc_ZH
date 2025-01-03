package net.minecraft.command.server;

import com.mojang.authlib.GameProfile;
import java.util.List;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.WrongUsageException;
import net.minecraft.server.MinecraftServer;

public class CommandPardonPlayer extends CommandBase
{
    private static final String __OBFID = "CL_00000747";

    public String getCommandName()
    {
        return "pardon";
    }

    /**
     * Return the required permission level for this command.
     */
    public int getRequiredPermissionLevel()
    {
        return 3;
    }

    public String getCommandUsage(ICommandSender sender)
    {
        return "commands.unban.usage";
    }

    /**
     * Returns true if the given command sender is allowed to use this command.
     */
    public boolean canCommandSenderUseCommand(ICommandSender sender)
    {
        return MinecraftServer.getServer().getConfigurationManager().func_152608_h().func_152689_b() && super.canCommandSenderUseCommand(sender);
    }

    public void processCommand(ICommandSender sender, String[] args)
    {
        if (args.length == 1 && args[0].length() > 0)
        {
            MinecraftServer minecraftserver = MinecraftServer.getServer();
            GameProfile gameprofile = minecraftserver.getConfigurationManager().func_152608_h().func_152703_a(args[0]);

            if (gameprofile == null)
            {
                throw new CommandException("commands.unban.failed", new Object[] {args[0]});
            }
            else
            {
                minecraftserver.getConfigurationManager().func_152608_h().func_152684_c(gameprofile);
                func_152373_a(sender, this, "commands.unban.success", new Object[] {args[0]});
            }
        }
        else
        {
            throw new WrongUsageException("commands.unban.usage", new Object[0]);
        }
    }

    /**
     * Adds the strings available in this command to the given list of tab completion options.
     */
    public List<String> addTabCompletionOptions(ICommandSender sender, String[] args)
    {
        return args.length == 1 ? getListOfStringsMatchingLastWord(args, MinecraftServer.getServer().getConfigurationManager().func_152608_h().func_152685_a()) : null;
    }
}