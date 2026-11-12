// src/main/java/com/example/metamorph/command/CommandMorph.java
package com.example.metamorph.command;

import com.example.metamorph.common.MorphApi;
import com.example.metamorph.common.MorphEntry;
import com.example.metamorph.common.MorphRegistry;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.WrongUsageException;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.ChatComponentText;

import java.util.Collections;
import java.util.List;

public class CommandMorph extends CommandBase {

    @Override
    public String getCommandName() {
        return "morph";
    }

    @Override
    public String getCommandUsage(ICommandSender sender) {
        return "/morph <set|clear|list> ...";
    }

    @Override
    public int getRequiredPermissionLevel() {
        return 2;
    }

    @Override
    public List<String> getCommandAliases() {
        return Collections.emptyList();
    }

    @Override
    public void processCommand(ICommandSender sender, String[] args) throws CommandException {
        if (args.length < 1) {
            throw new WrongUsageException(getCommandUsage(sender));
        }

        String sub = args[0].toLowerCase();

        if (sub.equals("set")) {
            if (args.length < 3) {
                throw new WrongUsageException("/morph set <player> <id|name>");
            }

            EntityPlayerMP target = getPlayer(sender, args[1]);
            int id = parseMorphId(args[2]);

            if (!MorphApi.setMorph(target, id, true)) {
                throw new CommandException("Failed to set morph.");
            }

            sender.addChatMessage(new ChatComponentText(
                    "Set " + target.getName() + " morph to " + id
            ));

        } else if (sub.equals("clear")) {
            if (args.length < 2) {
                throw new WrongUsageException("/morph clear <player>");
            }

            EntityPlayerMP target = getPlayer(sender, args[1]);

            MorphApi.setMorph(target, 0, true);

            sender.addChatMessage(new ChatComponentText(
                    "Cleared morph for " + target.getName()
            ));

        } else if (sub.equals("list")) {
            StringBuilder builder = new StringBuilder();

            for (MorphEntry entry : MorphRegistry.getAll()) {
                builder.append(entry.id)
                        .append("=")
                        .append(entry.name)
                        .append("; ");
            }

            sender.addChatMessage(new ChatComponentText(builder.toString()));

        } else {
            throw new WrongUsageException(getCommandUsage(sender));
        }
    }

    private int parseMorphId(String input) throws CommandException {
        int byName = EntityList.getIDFromString(input);

        if (byName >= 0 && MorphRegistry.get(byName) != null) {
            return byName;
        }

        try {
            int id = Integer.parseInt(input);

            if (id == 0 || MorphRegistry.get(id) != null) {
                return id;
            }

            throw new CommandException("Unknown morph id: " + input);

        } catch (NumberFormatException e) {
            throw new CommandException("Unknown morph: " + input);
        }
    }
}