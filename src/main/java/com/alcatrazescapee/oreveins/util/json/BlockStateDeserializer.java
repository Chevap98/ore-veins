/*
 * Part of the Realistic Ore Veins Mod by AlcatrazEscapee
 * Work under Copyright. See the project LICENSE.md for details.
 */

package com.alcatrazescapee.oreveins.util.json;

import java.lang.reflect.Type;

import com.google.gson.*;
import net.minecraft.block.BlockState;
import net.minecraft.command.arguments.BlockStateParser;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;

public enum BlockStateDeserializer implements JsonDeserializer<BlockState>
{
    INSTANCE;

    @Override
    public BlockState deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException
    {
        if (json.isJsonPrimitive())
        {
            return readBlockState(json.getAsString());
        }
        else if (json.isJsonObject())
        {
            JsonObject jsonObj = json.getAsJsonObject();
            String name = jsonObj.get("block").getAsString();
            return readBlockState(name);
        }
        throw new JsonParseException("BlockState must be String or Object");
    }

    public BlockState readBlockState(String block)
    {
        StringReader reader = new StringReader(block);
        try
        {
            return new BlockStateParser(reader, true).parse(false).getState();
        }
        catch (CommandSyntaxException e)
        {
            throw new JsonParseException("Unable to parse block state", e);
        }
    }

    public boolean isBlockState(String block)
    {
        try
        {
            new BlockStateParser(new StringReader(block), true).parse(false);
            return true;
        }
        catch (CommandSyntaxException e)
        {
            return false;
        }
    }
}
