package net.minecraft.command;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.event.ClickEvent;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.ChatComponentTranslation;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.MathHelper;

public class CommandHelp extends CommandBase
{
    private static final String __OBFID = "CL_00000529";

    public String getCommandName()
    {
        return "help";
    }

    /**
     * Return the required permission level for this command.
     */
    public int getRequiredPermissionLevel()
    {
        return 0;
    }

    public String getCommandUsage(ICommandSender sender)
    {
        return "commands.help.usage";
    }

    public List<String> getCommandAliases()
    {
        return Arrays.asList(new String[] {"?"});
    }

    public void processCommand(ICommandSender sender, String[] args)
    {
        List list = this.getSortedPossibleCommands(sender);
        byte b0 = 7;
        int i = (list.size() - 1) / b0;
        boolean flag = false;
        int k;

        try
        {
            k = args.length == 0 ? 0 : parseIntBounded(sender, args[0], 1, i + 1) - 1;
        }
        catch (NumberInvalidException numberinvalidexception)
        {
            Map map = this.getCommands();
            ICommand icommand = (ICommand)map.get(args[0]);

            if (icommand != null)
            {
                throw new WrongUsageException(icommand.getCommandUsage(sender), new Object[0]);
            }

            if (MathHelper.parseIntWithDefault(args[0], -1) != -1)
            {
                throw numberinvalidexception;
            }

            throw new CommandNotFoundException();
        }

        int j = Math.min((k + 1) * b0, list.size());
        ChatComponentTranslation chatcomponenttranslation1 = new ChatComponentTranslation("commands.help.header", new Object[] {Integer.valueOf(k + 1), Integer.valueOf(i + 1)});
        chatcomponenttranslation1.getChatStyle().setColor(EnumChatFormatting.DARK_GREEN);
        sender.addChatMessage(chatcomponenttranslation1);

        for (int l = k * b0; l < j; ++l)
        {
            ICommand icommand1 = (ICommand)list.get(l);
            ChatComponentTranslation chatcomponenttranslation = new ChatComponentTranslation(icommand1.getCommandUsage(sender), new Object[0]);
            chatcomponenttranslation.getChatStyle().setChatClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, "/" + icommand1.getCommandName() + " "));
            sender.addChatMessage(chatcomponenttranslation);
        }

        if (k == 0 && sender instanceof EntityPlayer)
        {
            ChatComponentTranslation chatcomponenttranslation2 = new ChatComponentTranslation("commands.help.footer", new Object[0]);
            chatcomponenttranslation2.getChatStyle().setColor(EnumChatFormatting.GREEN);
            sender.addChatMessage(chatcomponenttranslation2);
        }
    }

    /**
     * Returns a sorted list of all possible commands for the given ICommandSender.
     */
    protected List<net.minecraft.command.ICommand> getSortedPossibleCommands(ICommandSender p_71534_1_)
    {
        List list = MinecraftServer.getServer().getCommandManager().getPossibleCommands(p_71534_1_);
        Collections.sort(list);
        return list;
    }

    protected Map<String, net.minecraft.command.ICommand> getCommands()
    {
        return MinecraftServer.getServer().getCommandManager().getCommands();
    }
}