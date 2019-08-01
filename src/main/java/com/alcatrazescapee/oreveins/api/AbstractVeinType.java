/*
 * Part of the Ore Veins Mod by alcatrazEscapee
 * Work under Copyright. Licensed under the GPL-3.0.
 * See the project LICENSE.md for more information.
 */

package com.alcatrazescapee.oreveins.api;

import java.util.Collection;
import java.util.List;
import java.util.Random;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;

import com.google.gson.annotations.SerializedName;
import net.minecraft.block.BlockState;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.dimension.Dimension;
import net.minecraftforge.common.BiomeDictionary;

import com.alcatrazescapee.oreveins.Config;
import com.alcatrazescapee.oreveins.util.IWeightedList;
import com.alcatrazescapee.oreveins.world.indicator.Indicator;
import com.alcatrazescapee.oreveins.world.veins.VeinManager;

@SuppressWarnings({"unused", "WeakerAccess"})
@ParametersAreNonnullByDefault
public abstract class AbstractVeinType<V extends AbstractVein<?>> implements IVeinType<V>
{
    protected int count = 1;
    protected int rarity = 10;
    @SerializedName("min_y")
    protected int minY = 16;
    @SerializedName("max_y")
    protected int maxY = 64;
    @SerializedName("vertical_size")
    protected int verticalSize = 8;
    @SerializedName("horizontal_size")
    protected int horizontalSize = 15;
    protected float density = 20;

    @SerializedName("dimensions_is_whitelist")
    protected boolean dimensionIsWhitelist = true;
    @SerializedName("biomes_is_whitelist")
    protected boolean biomesIsWhitelist = true;

    @SerializedName("stone")
    private List<BlockState> stoneStates = null;
    @SerializedName("ore")
    private IWeightedList<BlockState> oreStates = null;

    private List<String> biomes = null;
    private List<String> dimensions = null;
    private List<IRule> rules = null;
    private IWeightedList<Indicator> indicator = null;

    @Nonnull
    @Override
    public BlockState getStateToGenerate(Random rand)
    {
        return oreStates.get(rand);
    }

    @Nonnull
    @Override
    public Collection<BlockState> getOreStates()
    {
        return oreStates.values();
    }

    @Nullable
    @Override
    public Indicator getIndicator(Random random)
    {
        return indicator != null ? indicator.get(random) : null;
    }

    @Override
    public boolean canGenerateAt(IBlockReader world, BlockPos pos)
    {
        BlockState stoneState = world.getBlockState(pos);
        if (stoneStates.contains(stoneState))
        {
            if (rules != null)
            {
                for (IRule rule : rules)
                {
                    if (!rule.test(world, pos))
                    {
                        return false;
                    }
                }
            }
            return true;
        }
        return false;
    }

    @Override
    public boolean inRange(V vein, int xOffset, int zOffset)
    {
        return xOffset * xOffset + zOffset * zOffset < horizontalSize * horizontalSize * vein.getSize();
    }

    @Override
    @SuppressWarnings("deprecation")
    public boolean matchesDimension(Dimension dimension)
    {
        ResourceLocation loc = Registry.DIMENSION_TYPE.getKey(dimension.getType());
        if (loc == null)
        {
            System.out.println("Loc is null");
            return true;
        }
        String name = loc.toString();
        if (dimensions == null)
        {
            return "minecraft:overworld".equals(name);
        }
        for (String dim : dimensions)
        {
            if (dim.equals(name))
            {
                return dimensionIsWhitelist;
            }
        }
        return !dimensionIsWhitelist;
    }

    @Override
    public boolean matchesBiome(Biome biome)
    {
        if (biomes == null) return true;
        for (String s : biomes)
        {
            //noinspection ConstantConditions
            String biomeName = biome.getRegistryName().getPath();
            if (biomeName.equals(s))
            {
                return biomesIsWhitelist;
            }
            for (BiomeDictionary.Type type : BiomeDictionary.getTypes(biome))
            {
                if (s.equalsIgnoreCase(type.getName()))
                {
                    return biomesIsWhitelist;
                }
            }
        }
        return !biomesIsWhitelist;
    }

    @Override
    public boolean isValid()
    {
        return oreStates != null && !oreStates.isEmpty() &&
                stoneStates != null && !stoneStates.isEmpty() &&
                (indicator == null || (!indicator.isEmpty() && indicator.values().stream().map(Indicator::isValid).reduce((x, y) -> x && y).orElse(false))) &&
                maxY > minY && minY >= 0 &&
                count > 0 &&
                rarity > 0 &&
                verticalSize > 0 && horizontalSize > 0 && density > 0;

    }

    @Override
    public int getMinY()
    {
        return minY;
    }

    @Override
    public int getMaxY()
    {
        return maxY;
    }

    @Override
    public int getCount()
    {
        return count;
    }

    @Override
    public int getRarity()
    {
        return rarity;
    }

    @Override
    public int getChunkRadius()
    {
        return 1 + (horizontalSize >> 4);
    }

    @Override
    public String toString()
    {
        return String.format("[%s: Count: %d, Rarity: %d, Y: %d - %d, Size: %d / %d, Density: %2.2f, Ores: %s, Stones: %s]", VeinManager.INSTANCE.getName(this), count, rarity, minY, maxY, horizontalSize, verticalSize, density, oreStates, stoneStates);
    }

    protected final BlockPos defaultStartPos(int chunkX, int chunkZ, Random rand)
    {
        int spawnRange = maxY - minY, minRange = minY;
        if (Config.COMMON.avoidVeinCutoffs.get())
        {
            if (verticalSize * 2 < spawnRange)
            {
                spawnRange -= verticalSize * 2;
                minRange += verticalSize;
            }
            else
            {
                minRange = minY + (maxY - minY) / 2;
                spawnRange = 1;
            }
        }
        return new BlockPos(
                chunkX * 16 + rand.nextInt(16),
                minRange + rand.nextInt(spawnRange),
                chunkZ * 16 + rand.nextInt(16)
        );
    }
}
