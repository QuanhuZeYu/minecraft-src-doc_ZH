package net.minecraft.command.server;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.SyntaxErrorException;
import net.minecraft.command.WrongUsageException;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.scoreboard.IScoreObjectiveCriteria;
import net.minecraft.scoreboard.Score;
import net.minecraft.scoreboard.ScoreObjective;
import net.minecraft.scoreboard.ScorePlayerTeam;
import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.ChatComponentTranslation;
import net.minecraft.util.EnumChatFormatting;

public class CommandScoreboard extends CommandBase
{
    private static final String __OBFID = "CL_00000896";

    public String getCommandName()
    {
        return "scoreboard";
    }

    /**
     * Return the required permission level for this command.
     */
    public int getRequiredPermissionLevel()
    {
        return 2;
    }

    public String getCommandUsage(ICommandSender sender)
    {
        return "commands.scoreboard.usage";
    }

    public void processCommand(ICommandSender sender, String[] args)
    {
        if (args.length >= 1)
        {
            if (args[0].equalsIgnoreCase("objectives"))
            {
                if (args.length == 1)
                {
                    throw new WrongUsageException("commands.scoreboard.objectives.usage", new Object[0]);
                }

                if (args[1].equalsIgnoreCase("list"))
                {
                    this.func_147196_d(sender);
                }
                else if (args[1].equalsIgnoreCase("add"))
                {
                    if (args.length < 4)
                    {
                        throw new WrongUsageException("commands.scoreboard.objectives.add.usage", new Object[0]);
                    }

                    this.func_147193_c(sender, args, 2);
                }
                else if (args[1].equalsIgnoreCase("remove"))
                {
                    if (args.length != 3)
                    {
                        throw new WrongUsageException("commands.scoreboard.objectives.remove.usage", new Object[0]);
                    }

                    this.func_147191_h(sender, args[2]);
                }
                else
                {
                    if (!args[1].equalsIgnoreCase("setdisplay"))
                    {
                        throw new WrongUsageException("commands.scoreboard.objectives.usage", new Object[0]);
                    }

                    if (args.length != 3 && args.length != 4)
                    {
                        throw new WrongUsageException("commands.scoreboard.objectives.setdisplay.usage", new Object[0]);
                    }

                    this.func_147198_k(sender, args, 2);
                }

                return;
            }

            if (args[0].equalsIgnoreCase("players"))
            {
                if (args.length == 1)
                {
                    throw new WrongUsageException("commands.scoreboard.players.usage", new Object[0]);
                }

                if (args[1].equalsIgnoreCase("list"))
                {
                    if (args.length > 3)
                    {
                        throw new WrongUsageException("commands.scoreboard.players.list.usage", new Object[0]);
                    }

                    this.func_147195_l(sender, args, 2);
                }
                else if (args[1].equalsIgnoreCase("add"))
                {
                    if (args.length != 5)
                    {
                        throw new WrongUsageException("commands.scoreboard.players.add.usage", new Object[0]);
                    }

                    this.func_147197_m(sender, args, 2);
                }
                else if (args[1].equalsIgnoreCase("remove"))
                {
                    if (args.length != 5)
                    {
                        throw new WrongUsageException("commands.scoreboard.players.remove.usage", new Object[0]);
                    }

                    this.func_147197_m(sender, args, 2);
                }
                else if (args[1].equalsIgnoreCase("set"))
                {
                    if (args.length != 5)
                    {
                        throw new WrongUsageException("commands.scoreboard.players.set.usage", new Object[0]);
                    }

                    this.func_147197_m(sender, args, 2);
                }
                else
                {
                    if (!args[1].equalsIgnoreCase("reset"))
                    {
                        throw new WrongUsageException("commands.scoreboard.players.usage", new Object[0]);
                    }

                    if (args.length != 3)
                    {
                        throw new WrongUsageException("commands.scoreboard.players.reset.usage", new Object[0]);
                    }

                    this.func_147187_n(sender, args, 2);
                }

                return;
            }

            if (args[0].equalsIgnoreCase("teams"))
            {
                if (args.length == 1)
                {
                    throw new WrongUsageException("commands.scoreboard.teams.usage", new Object[0]);
                }

                if (args[1].equalsIgnoreCase("list"))
                {
                    if (args.length > 3)
                    {
                        throw new WrongUsageException("commands.scoreboard.teams.list.usage", new Object[0]);
                    }

                    this.func_147186_g(sender, args, 2);
                }
                else if (args[1].equalsIgnoreCase("add"))
                {
                    if (args.length < 3)
                    {
                        throw new WrongUsageException("commands.scoreboard.teams.add.usage", new Object[0]);
                    }

                    this.func_147185_d(sender, args, 2);
                }
                else if (args[1].equalsIgnoreCase("remove"))
                {
                    if (args.length != 3)
                    {
                        throw new WrongUsageException("commands.scoreboard.teams.remove.usage", new Object[0]);
                    }

                    this.func_147194_f(sender, args, 2);
                }
                else if (args[1].equalsIgnoreCase("empty"))
                {
                    if (args.length != 3)
                    {
                        throw new WrongUsageException("commands.scoreboard.teams.empty.usage", new Object[0]);
                    }

                    this.func_147188_j(sender, args, 2);
                }
                else if (args[1].equalsIgnoreCase("join"))
                {
                    if (args.length < 4 && (args.length != 3 || !(sender instanceof EntityPlayer)))
                    {
                        throw new WrongUsageException("commands.scoreboard.teams.join.usage", new Object[0]);
                    }

                    this.func_147190_h(sender, args, 2);
                }
                else if (args[1].equalsIgnoreCase("leave"))
                {
                    if (args.length < 3 && !(sender instanceof EntityPlayer))
                    {
                        throw new WrongUsageException("commands.scoreboard.teams.leave.usage", new Object[0]);
                    }

                    this.func_147199_i(sender, args, 2);
                }
                else
                {
                    if (!args[1].equalsIgnoreCase("option"))
                    {
                        throw new WrongUsageException("commands.scoreboard.teams.usage", new Object[0]);
                    }

                    if (args.length != 4 && args.length != 5)
                    {
                        throw new WrongUsageException("commands.scoreboard.teams.option.usage", new Object[0]);
                    }

                    this.func_147200_e(sender, args, 2);
                }

                return;
            }
        }

        throw new WrongUsageException("commands.scoreboard.usage", new Object[0]);
    }

    protected Scoreboard func_147192_d()
    {
        return MinecraftServer.getServer().worldServerForDimension(0).getScoreboard();
    }

    protected ScoreObjective func_147189_a(String name, boolean edit)
    {
        Scoreboard scoreboard = this.func_147192_d();
        ScoreObjective scoreobjective = scoreboard.getObjective(name);

        if (scoreobjective == null)
        {
            throw new CommandException("commands.scoreboard.objectiveNotFound", new Object[] {name});
        }
        else if (edit && scoreobjective.getCriteria().isReadOnly())
        {
            throw new CommandException("commands.scoreboard.objectiveReadOnly", new Object[] {name});
        }
        else
        {
            return scoreobjective;
        }
    }

    protected ScorePlayerTeam func_147183_a(String name)
    {
        Scoreboard scoreboard = this.func_147192_d();
        ScorePlayerTeam scoreplayerteam = scoreboard.getTeam(name);

        if (scoreplayerteam == null)
        {
            throw new CommandException("commands.scoreboard.teamNotFound", new Object[] {name});
        }
        else
        {
            return scoreplayerteam;
        }
    }

    protected void func_147193_c(ICommandSender sender, String[] args, int index)
    {
        String s = args[index++];
        String s1 = args[index++];
        Scoreboard scoreboard = this.func_147192_d();
        IScoreObjectiveCriteria iscoreobjectivecriteria = (IScoreObjectiveCriteria)IScoreObjectiveCriteria.field_96643_a.get(s1);

        if (iscoreobjectivecriteria == null)
        {
            throw new WrongUsageException("commands.scoreboard.objectives.add.wrongType", new Object[] {s1});
        }
        else if (scoreboard.getObjective(s) != null)
        {
            throw new CommandException("commands.scoreboard.objectives.add.alreadyExists", new Object[] {s});
        }
        else if (s.length() > 16)
        {
            throw new SyntaxErrorException("commands.scoreboard.objectives.add.tooLong", new Object[] {s, Integer.valueOf(16)});
        }
        else if (s.length() == 0)
        {
            throw new WrongUsageException("commands.scoreboard.objectives.add.usage", new Object[0]);
        }
        else
        {
            if (args.length > index)
            {
                String s2 = func_147178_a(sender, args, index).getUnformattedText();

                if (s2.length() > 32)
                {
                    throw new SyntaxErrorException("commands.scoreboard.objectives.add.displayTooLong", new Object[] {s2, Integer.valueOf(32)});
                }

                if (s2.length() > 0)
                {
                    scoreboard.addScoreObjective(s, iscoreobjectivecriteria).setDisplayName(s2);
                }
                else
                {
                    scoreboard.addScoreObjective(s, iscoreobjectivecriteria);
                }
            }
            else
            {
                scoreboard.addScoreObjective(s, iscoreobjectivecriteria);
            }

            func_152373_a(sender, this, "commands.scoreboard.objectives.add.success", new Object[] {s});
        }
    }

    protected void func_147185_d(ICommandSender p_147185_1_, String[] p_147185_2_, int p_147185_3_)
    {
        String s = p_147185_2_[p_147185_3_++];
        Scoreboard scoreboard = this.func_147192_d();

        if (scoreboard.getTeam(s) != null)
        {
            throw new CommandException("commands.scoreboard.teams.add.alreadyExists", new Object[] {s});
        }
        else if (s.length() > 16)
        {
            throw new SyntaxErrorException("commands.scoreboard.teams.add.tooLong", new Object[] {s, Integer.valueOf(16)});
        }
        else if (s.length() == 0)
        {
            throw new WrongUsageException("commands.scoreboard.teams.add.usage", new Object[0]);
        }
        else
        {
            if (p_147185_2_.length > p_147185_3_)
            {
                String s1 = func_147178_a(p_147185_1_, p_147185_2_, p_147185_3_).getUnformattedText();

                if (s1.length() > 32)
                {
                    throw new SyntaxErrorException("commands.scoreboard.teams.add.displayTooLong", new Object[] {s1, Integer.valueOf(32)});
                }

                if (s1.length() > 0)
                {
                    scoreboard.createTeam(s).setTeamName(s1);
                }
                else
                {
                    scoreboard.createTeam(s);
                }
            }
            else
            {
                scoreboard.createTeam(s);
            }

            func_152373_a(p_147185_1_, this, "commands.scoreboard.teams.add.success", new Object[] {s});
        }
    }

    protected void func_147200_e(ICommandSender p_147200_1_, String[] p_147200_2_, int p_147200_3_)
    {
        ScorePlayerTeam scoreplayerteam = this.func_147183_a(p_147200_2_[p_147200_3_++]);

        if (scoreplayerteam != null)
        {
            String s = p_147200_2_[p_147200_3_++].toLowerCase();

            if (!s.equalsIgnoreCase("color") && !s.equalsIgnoreCase("friendlyfire") && !s.equalsIgnoreCase("seeFriendlyInvisibles"))
            {
                throw new WrongUsageException("commands.scoreboard.teams.option.usage", new Object[0]);
            }
            else if (p_147200_2_.length == 4)
            {
                if (s.equalsIgnoreCase("color"))
                {
                    throw new WrongUsageException("commands.scoreboard.teams.option.noValue", new Object[] {s, joinNiceStringFromCollection(EnumChatFormatting.getValidValues(true, false))});
                }
                else if (!s.equalsIgnoreCase("friendlyfire") && !s.equalsIgnoreCase("seeFriendlyInvisibles"))
                {
                    throw new WrongUsageException("commands.scoreboard.teams.option.usage", new Object[0]);
                }
                else
                {
                    throw new WrongUsageException("commands.scoreboard.teams.option.noValue", new Object[] {s, joinNiceStringFromCollection(Arrays.asList(new String[]{"true", "false"}))});
                }
            }
            else
            {
                String s1 = p_147200_2_[p_147200_3_++];

                if (s.equalsIgnoreCase("color"))
                {
                    EnumChatFormatting enumchatformatting = EnumChatFormatting.getValueByName(s1);

                    if (enumchatformatting == null || enumchatformatting.isFancyStyling())
                    {
                        throw new WrongUsageException("commands.scoreboard.teams.option.noValue", new Object[] {s, joinNiceStringFromCollection(EnumChatFormatting.getValidValues(true, false))});
                    }

                    scoreplayerteam.setNamePrefix(enumchatformatting.toString());
                    scoreplayerteam.setNameSuffix(EnumChatFormatting.RESET.toString());
                }
                else if (s.equalsIgnoreCase("friendlyfire"))
                {
                    if (!s1.equalsIgnoreCase("true") && !s1.equalsIgnoreCase("false"))
                    {
                        throw new WrongUsageException("commands.scoreboard.teams.option.noValue", new Object[] {s, joinNiceStringFromCollection(Arrays.asList(new String[]{"true", "false"}))});
                    }

                    scoreplayerteam.setAllowFriendlyFire(s1.equalsIgnoreCase("true"));
                }
                else if (s.equalsIgnoreCase("seeFriendlyInvisibles"))
                {
                    if (!s1.equalsIgnoreCase("true") && !s1.equalsIgnoreCase("false"))
                    {
                        throw new WrongUsageException("commands.scoreboard.teams.option.noValue", new Object[] {s, joinNiceStringFromCollection(Arrays.asList(new String[]{"true", "false"}))});
                    }

                    scoreplayerteam.setSeeFriendlyInvisiblesEnabled(s1.equalsIgnoreCase("true"));
                }

                func_152373_a(p_147200_1_, this, "commands.scoreboard.teams.option.success", new Object[] {s, scoreplayerteam.getRegisteredName(), s1});
            }
        }
    }

    protected void func_147194_f(ICommandSender p_147194_1_, String[] p_147194_2_, int p_147194_3_)
    {
        Scoreboard scoreboard = this.func_147192_d();
        ScorePlayerTeam scoreplayerteam = this.func_147183_a(p_147194_2_[p_147194_3_++]);

        if (scoreplayerteam != null)
        {
            scoreboard.removeTeam(scoreplayerteam);
            func_152373_a(p_147194_1_, this, "commands.scoreboard.teams.remove.success", new Object[] {scoreplayerteam.getRegisteredName()});
        }
    }

    protected void func_147186_g(ICommandSender p_147186_1_, String[] p_147186_2_, int p_147186_3_)
    {
        Scoreboard scoreboard = this.func_147192_d();

        if (p_147186_2_.length > p_147186_3_)
        {
            ScorePlayerTeam scoreplayerteam = this.func_147183_a(p_147186_2_[p_147186_3_++]);

            if (scoreplayerteam == null)
            {
                return;
            }

            Collection collection = scoreplayerteam.getMembershipCollection();

            if (collection.size() <= 0)
            {
                throw new CommandException("commands.scoreboard.teams.list.player.empty", new Object[] {scoreplayerteam.getRegisteredName()});
            }

            ChatComponentTranslation chatcomponenttranslation = new ChatComponentTranslation("commands.scoreboard.teams.list.player.count", new Object[] {Integer.valueOf(collection.size()), scoreplayerteam.getRegisteredName()});
            chatcomponenttranslation.getChatStyle().setColor(EnumChatFormatting.DARK_GREEN);
            p_147186_1_.addChatMessage(chatcomponenttranslation);
            p_147186_1_.addChatMessage(new ChatComponentText(joinNiceString(collection.toArray())));
        }
        else
        {
            Collection collection1 = scoreboard.getTeams();

            if (collection1.size() <= 0)
            {
                throw new CommandException("commands.scoreboard.teams.list.empty", new Object[0]);
            }

            ChatComponentTranslation chatcomponenttranslation1 = new ChatComponentTranslation("commands.scoreboard.teams.list.count", new Object[] {Integer.valueOf(collection1.size())});
            chatcomponenttranslation1.getChatStyle().setColor(EnumChatFormatting.DARK_GREEN);
            p_147186_1_.addChatMessage(chatcomponenttranslation1);
            Iterator iterator = collection1.iterator();

            while (iterator.hasNext())
            {
                ScorePlayerTeam scoreplayerteam1 = (ScorePlayerTeam)iterator.next();
                p_147186_1_.addChatMessage(new ChatComponentTranslation("commands.scoreboard.teams.list.entry", new Object[] {scoreplayerteam1.getRegisteredName(), scoreplayerteam1.func_96669_c(), Integer.valueOf(scoreplayerteam1.getMembershipCollection().size())}));
            }
        }
    }

    protected void func_147190_h(ICommandSender p_147190_1_, String[] p_147190_2_, int p_147190_3_)
    {
        Scoreboard scoreboard = this.func_147192_d();
        String s = p_147190_2_[p_147190_3_++];
        HashSet hashset = new HashSet();
        HashSet hashset1 = new HashSet();
        String s1;

        if (p_147190_1_ instanceof EntityPlayer && p_147190_3_ == p_147190_2_.length)
        {
            s1 = getCommandSenderAsPlayer(p_147190_1_).getCommandSenderName();

            if (scoreboard.func_151392_a(s1, s))
            {
                hashset.add(s1);
            }
            else
            {
                hashset1.add(s1);
            }
        }
        else
        {
            while (p_147190_3_ < p_147190_2_.length)
            {
                s1 = func_96332_d(p_147190_1_, p_147190_2_[p_147190_3_++]);

                if (scoreboard.func_151392_a(s1, s))
                {
                    hashset.add(s1);
                }
                else
                {
                    hashset1.add(s1);
                }
            }
        }

        if (!hashset.isEmpty())
        {
            func_152373_a(p_147190_1_, this, "commands.scoreboard.teams.join.success", new Object[] {Integer.valueOf(hashset.size()), s, joinNiceString(hashset.toArray(new String[0]))});
        }

        if (!hashset1.isEmpty())
        {
            throw new CommandException("commands.scoreboard.teams.join.failure", new Object[] {Integer.valueOf(hashset1.size()), s, joinNiceString(hashset1.toArray(new String[0]))});
        }
    }

    protected void func_147199_i(ICommandSender p_147199_1_, String[] p_147199_2_, int p_147199_3_)
    {
        Scoreboard scoreboard = this.func_147192_d();
        HashSet hashset = new HashSet();
        HashSet hashset1 = new HashSet();
        String s;

        if (p_147199_1_ instanceof EntityPlayer && p_147199_3_ == p_147199_2_.length)
        {
            s = getCommandSenderAsPlayer(p_147199_1_).getCommandSenderName();

            if (scoreboard.removePlayerFromTeams(s))
            {
                hashset.add(s);
            }
            else
            {
                hashset1.add(s);
            }
        }
        else
        {
            while (p_147199_3_ < p_147199_2_.length)
            {
                s = func_96332_d(p_147199_1_, p_147199_2_[p_147199_3_++]);

                if (scoreboard.removePlayerFromTeams(s))
                {
                    hashset.add(s);
                }
                else
                {
                    hashset1.add(s);
                }
            }
        }

        if (!hashset.isEmpty())
        {
            func_152373_a(p_147199_1_, this, "commands.scoreboard.teams.leave.success", new Object[] {Integer.valueOf(hashset.size()), joinNiceString(hashset.toArray(new String[0]))});
        }

        if (!hashset1.isEmpty())
        {
            throw new CommandException("commands.scoreboard.teams.leave.failure", new Object[] {Integer.valueOf(hashset1.size()), joinNiceString(hashset1.toArray(new String[0]))});
        }
    }

    protected void func_147188_j(ICommandSender p_147188_1_, String[] p_147188_2_, int p_147188_3_)
    {
        Scoreboard scoreboard = this.func_147192_d();
        ScorePlayerTeam scoreplayerteam = this.func_147183_a(p_147188_2_[p_147188_3_++]);

        if (scoreplayerteam != null)
        {
            ArrayList arraylist = new ArrayList(scoreplayerteam.getMembershipCollection());

            if (arraylist.isEmpty())
            {
                throw new CommandException("commands.scoreboard.teams.empty.alreadyEmpty", new Object[] {scoreplayerteam.getRegisteredName()});
            }
            else
            {
                Iterator iterator = arraylist.iterator();

                while (iterator.hasNext())
                {
                    String s = (String)iterator.next();
                    scoreboard.removePlayerFromTeam(s, scoreplayerteam);
                }

                func_152373_a(p_147188_1_, this, "commands.scoreboard.teams.empty.success", new Object[] {Integer.valueOf(arraylist.size()), scoreplayerteam.getRegisteredName()});
            }
        }
    }

    protected void func_147191_h(ICommandSender p_147191_1_, String p_147191_2_)
    {
        Scoreboard scoreboard = this.func_147192_d();
        ScoreObjective scoreobjective = this.func_147189_a(p_147191_2_, false);
        scoreboard.func_96519_k(scoreobjective);
        func_152373_a(p_147191_1_, this, "commands.scoreboard.objectives.remove.success", new Object[] {p_147191_2_});
    }

    protected void func_147196_d(ICommandSender p_147196_1_)
    {
        Scoreboard scoreboard = this.func_147192_d();
        Collection collection = scoreboard.getScoreObjectives();

        if (collection.size() <= 0)
        {
            throw new CommandException("commands.scoreboard.objectives.list.empty", new Object[0]);
        }
        else
        {
            ChatComponentTranslation chatcomponenttranslation = new ChatComponentTranslation("commands.scoreboard.objectives.list.count", new Object[] {Integer.valueOf(collection.size())});
            chatcomponenttranslation.getChatStyle().setColor(EnumChatFormatting.DARK_GREEN);
            p_147196_1_.addChatMessage(chatcomponenttranslation);
            Iterator iterator = collection.iterator();

            while (iterator.hasNext())
            {
                ScoreObjective scoreobjective = (ScoreObjective)iterator.next();
                p_147196_1_.addChatMessage(new ChatComponentTranslation("commands.scoreboard.objectives.list.entry", new Object[] {scoreobjective.getName(), scoreobjective.getDisplayName(), scoreobjective.getCriteria().func_96636_a()}));
            }
        }
    }

    protected void func_147198_k(ICommandSender p_147198_1_, String[] p_147198_2_, int p_147198_3_)
    {
        Scoreboard scoreboard = this.func_147192_d();
        String s = p_147198_2_[p_147198_3_++];
        int j = Scoreboard.getObjectiveDisplaySlotNumber(s);
        ScoreObjective scoreobjective = null;

        if (p_147198_2_.length == 4)
        {
            scoreobjective = this.func_147189_a(p_147198_2_[p_147198_3_++], false);
        }

        if (j < 0)
        {
            throw new CommandException("commands.scoreboard.objectives.setdisplay.invalidSlot", new Object[] {s});
        }
        else
        {
            scoreboard.func_96530_a(j, scoreobjective);

            if (scoreobjective != null)
            {
                func_152373_a(p_147198_1_, this, "commands.scoreboard.objectives.setdisplay.successSet", new Object[] {Scoreboard.getObjectiveDisplaySlot(j), scoreobjective.getName()});
            }
            else
            {
                func_152373_a(p_147198_1_, this, "commands.scoreboard.objectives.setdisplay.successCleared", new Object[] {Scoreboard.getObjectiveDisplaySlot(j)});
            }
        }
    }

    protected void func_147195_l(ICommandSender p_147195_1_, String[] p_147195_2_, int p_147195_3_)
    {
        Scoreboard scoreboard = this.func_147192_d();

        if (p_147195_2_.length > p_147195_3_)
        {
            String s = func_96332_d(p_147195_1_, p_147195_2_[p_147195_3_++]);
            Map map = scoreboard.func_96510_d(s);

            if (map.size() <= 0)
            {
                throw new CommandException("commands.scoreboard.players.list.player.empty", new Object[] {s});
            }

            ChatComponentTranslation chatcomponenttranslation = new ChatComponentTranslation("commands.scoreboard.players.list.player.count", new Object[] {Integer.valueOf(map.size()), s});
            chatcomponenttranslation.getChatStyle().setColor(EnumChatFormatting.DARK_GREEN);
            p_147195_1_.addChatMessage(chatcomponenttranslation);
            Iterator iterator = map.values().iterator();

            while (iterator.hasNext())
            {
                Score score = (Score)iterator.next();
                p_147195_1_.addChatMessage(new ChatComponentTranslation("commands.scoreboard.players.list.player.entry", new Object[] {Integer.valueOf(score.getScorePoints()), score.func_96645_d().getDisplayName(), score.func_96645_d().getName()}));
            }
        }
        else
        {
            Collection collection = scoreboard.getObjectiveNames();

            if (collection.size() <= 0)
            {
                throw new CommandException("commands.scoreboard.players.list.empty", new Object[0]);
            }

            ChatComponentTranslation chatcomponenttranslation1 = new ChatComponentTranslation("commands.scoreboard.players.list.count", new Object[] {Integer.valueOf(collection.size())});
            chatcomponenttranslation1.getChatStyle().setColor(EnumChatFormatting.DARK_GREEN);
            p_147195_1_.addChatMessage(chatcomponenttranslation1);
            p_147195_1_.addChatMessage(new ChatComponentText(joinNiceString(collection.toArray())));
        }
    }

    protected void func_147197_m(ICommandSender p_147197_1_, String[] p_147197_2_, int p_147197_3_)
    {
        String s = p_147197_2_[p_147197_3_ - 1];
        String s1 = func_96332_d(p_147197_1_, p_147197_2_[p_147197_3_++]);
        ScoreObjective scoreobjective = this.func_147189_a(p_147197_2_[p_147197_3_++], true);
        int j = s.equalsIgnoreCase("set") ? parseInt(p_147197_1_, p_147197_2_[p_147197_3_++]) : parseIntWithMin(p_147197_1_, p_147197_2_[p_147197_3_++], 1);
        Scoreboard scoreboard = this.func_147192_d();
        Score score = scoreboard.func_96529_a(s1, scoreobjective);

        if (s.equalsIgnoreCase("set"))
        {
            score.setScorePoints(j);
        }
        else if (s.equalsIgnoreCase("add"))
        {
            score.increseScore(j);
        }
        else
        {
            score.decreaseScore(j);
        }

        func_152373_a(p_147197_1_, this, "commands.scoreboard.players.set.success", new Object[] {scoreobjective.getName(), s1, Integer.valueOf(score.getScorePoints())});
    }

    protected void func_147187_n(ICommandSender p_147187_1_, String[] p_147187_2_, int p_147187_3_)
    {
        Scoreboard scoreboard = this.func_147192_d();
        String s = func_96332_d(p_147187_1_, p_147187_2_[p_147187_3_++]);
        scoreboard.func_96515_c(s);
        func_152373_a(p_147187_1_, this, "commands.scoreboard.players.reset.success", new Object[] {s});
    }

    /**
     * Adds the strings available in this command to the given list of tab completion options.
     */
    public List<String> addTabCompletionOptions(ICommandSender sender, String[] args)
    {
        if (args.length == 1)
        {
            return getListOfStringsMatchingLastWord(args, new String[] {"objectives", "players", "teams"});
        }
        else
        {
            if (args[0].equalsIgnoreCase("objectives"))
            {
                if (args.length == 2)
                {
                    return getListOfStringsMatchingLastWord(args, new String[] {"list", "add", "remove", "setdisplay"});
                }

                if (args[1].equalsIgnoreCase("add"))
                {
                    if (args.length == 4)
                    {
                        Set set = IScoreObjectiveCriteria.field_96643_a.keySet();
                        return getListOfStringsFromIterableMatchingLastWord(args, set);
                    }
                }
                else if (args[1].equalsIgnoreCase("remove"))
                {
                    if (args.length == 3)
                    {
                        return getListOfStringsFromIterableMatchingLastWord(args, this.func_147184_a(false));
                    }
                }
                else if (args[1].equalsIgnoreCase("setdisplay"))
                {
                    if (args.length == 3)
                    {
                        return getListOfStringsMatchingLastWord(args, new String[] {"list", "sidebar", "belowName"});
                    }

                    if (args.length == 4)
                    {
                        return getListOfStringsFromIterableMatchingLastWord(args, this.func_147184_a(false));
                    }
                }
            }
            else if (args[0].equalsIgnoreCase("players"))
            {
                if (args.length == 2)
                {
                    return getListOfStringsMatchingLastWord(args, new String[] {"set", "add", "remove", "reset", "list"});
                }

                if (!args[1].equalsIgnoreCase("set") && !args[1].equalsIgnoreCase("add") && !args[1].equalsIgnoreCase("remove"))
                {
                    if ((args[1].equalsIgnoreCase("reset") || args[1].equalsIgnoreCase("list")) && args.length == 3)
                    {
                        return getListOfStringsFromIterableMatchingLastWord(args, this.func_147192_d().getObjectiveNames());
                    }
                }
                else
                {
                    if (args.length == 3)
                    {
                        return getListOfStringsMatchingLastWord(args, MinecraftServer.getServer().getAllUsernames());
                    }

                    if (args.length == 4)
                    {
                        return getListOfStringsFromIterableMatchingLastWord(args, this.func_147184_a(true));
                    }
                }
            }
            else if (args[0].equalsIgnoreCase("teams"))
            {
                if (args.length == 2)
                {
                    return getListOfStringsMatchingLastWord(args, new String[] {"add", "remove", "join", "leave", "empty", "list", "option"});
                }

                if (args[1].equalsIgnoreCase("join"))
                {
                    if (args.length == 3)
                    {
                        return getListOfStringsFromIterableMatchingLastWord(args, this.func_147192_d().getTeamNames());
                    }

                    if (args.length >= 4)
                    {
                        return getListOfStringsMatchingLastWord(args, MinecraftServer.getServer().getAllUsernames());
                    }
                }
                else
                {
                    if (args[1].equalsIgnoreCase("leave"))
                    {
                        return getListOfStringsMatchingLastWord(args, MinecraftServer.getServer().getAllUsernames());
                    }

                    if (!args[1].equalsIgnoreCase("empty") && !args[1].equalsIgnoreCase("list") && !args[1].equalsIgnoreCase("remove"))
                    {
                        if (args[1].equalsIgnoreCase("option"))
                        {
                            if (args.length == 3)
                            {
                                return getListOfStringsFromIterableMatchingLastWord(args, this.func_147192_d().getTeamNames());
                            }

                            if (args.length == 4)
                            {
                                return getListOfStringsMatchingLastWord(args, new String[] {"color", "friendlyfire", "seeFriendlyInvisibles"});
                            }

                            if (args.length == 5)
                            {
                                if (args[3].equalsIgnoreCase("color"))
                                {
                                    return getListOfStringsFromIterableMatchingLastWord(args, EnumChatFormatting.getValidValues(true, false));
                                }

                                if (args[3].equalsIgnoreCase("friendlyfire") || args[3].equalsIgnoreCase("seeFriendlyInvisibles"))
                                {
                                    return getListOfStringsMatchingLastWord(args, new String[] {"true", "false"});
                                }
                            }
                        }
                    }
                    else if (args.length == 3)
                    {
                        return getListOfStringsFromIterableMatchingLastWord(args, this.func_147192_d().getTeamNames());
                    }
                }
            }

            return null;
        }
    }

    protected List<String> func_147184_a(boolean p_147184_1_)
    {
        Collection collection = this.func_147192_d().getScoreObjectives();
        ArrayList arraylist = new ArrayList();
        Iterator iterator = collection.iterator();

        while (iterator.hasNext())
        {
            ScoreObjective scoreobjective = (ScoreObjective)iterator.next();

            if (!p_147184_1_ || !scoreobjective.getCriteria().isReadOnly())
            {
                arraylist.add(scoreobjective.getName());
            }
        }

        return arraylist;
    }

    /**
     * Return whether the specified command parameter index is a username parameter.
     */
    public boolean isUsernameIndex(String[] args, int index)
    {
        return args[0].equalsIgnoreCase("players") ? index == 2 : (!args[0].equalsIgnoreCase("teams") ? false : index == 2 || index == 3);
    }
}