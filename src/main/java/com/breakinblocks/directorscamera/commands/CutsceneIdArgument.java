package com.breakinblocks.directorscamera.commands;

import com.breakinblocks.directorscamera.cutscene.CutsceneRegistry;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.resources.Identifier;

import java.util.Collection;
import java.util.List;

public class CutsceneIdArgument implements ArgumentType<String> {
    private static final Collection<String> EXAMPLES = List.of("directorscamera:intro", "mypack:boss_reveal");

    public static CutsceneIdArgument id() {
        return new CutsceneIdArgument();
    }

    public static String getId(CommandContext<?> context, String name) {
        return context.getArgument(name, String.class);
    }

    @Override
    public String parse(StringReader reader) throws CommandSyntaxException {
        int start = reader.getCursor();
        while (reader.canRead() && Identifier.isAllowedInIdentifier(reader.peek())) {
            reader.skip();
        }
        String raw = reader.getString().substring(start, reader.getCursor());
        if (raw.isEmpty()) {
            throw Identifier.ERROR_INVALID.createWithContext(reader);
        }
        String normalized = CutsceneRegistry.normalize(raw);
        if (Identifier.tryParse(normalized) == null) {
            reader.setCursor(start);
            throw Identifier.ERROR_INVALID.createWithContext(reader);
        }
        return normalized;
    }

    @Override
    public Collection<String> getExamples() {
        return EXAMPLES;
    }
}
