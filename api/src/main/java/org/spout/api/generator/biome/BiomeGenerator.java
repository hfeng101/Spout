/*
 * This file is part of Spout.
 *
 * Copyright (c) 2011 Spout LLC <http://www.spout.org/>
 * Spout is licensed under the Spout License Version 1.
 *
 * Spout is free software: you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation, either version 3 of the License, or (at your option)
 * any later version.
 *
 * In addition, 180 days after any changes are published, you can use the
 * software, incorporating those changes, under the terms of the MIT license,
 * as described in the Spout License Version 1.
 *
 * Spout is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE.  See the GNU Lesser General Public License for
 * more details.
 *
 * You should have received a copy of the GNU Lesser General Public License,
 * the MIT license and the Spout License Version 1 along with this program.
 * If not, see <http://www.gnu.org/licenses/> for the GNU Lesser General Public
 * License and see <http://spout.in/licensev1> for the full license, including
 * the MIT license.
 */
package org.spout.api.generator.biome;

import java.util.ArrayList;
import java.util.Set;

import com.google.common.collect.Lists;

import org.spout.api.generator.GeneratorPopulator;
import org.spout.api.generator.Populator;
import org.spout.api.generator.WorldGenerator;
import org.spout.api.geo.LoadOption;
import org.spout.api.geo.World;
import org.spout.api.geo.cuboid.Chunk;
import org.spout.api.math.Vector3;
import org.spout.api.util.cuboid.CuboidBlockMaterialBuffer;

/**
 * Abstract Biome Column Generator.
 */
public abstract class BiomeGenerator implements WorldGenerator {
	protected final BiomeMap biomes = new BiomeMap();
	private final ArrayList<Populator> populators = new ArrayList<>();
	private final ArrayList<GeneratorPopulator> generatorPopulators = new ArrayList<>();

	public BiomeGenerator() {
		registerBiomes();
		for (Populator populator : populators) {
			if (populator instanceof BiomePopulator) {
				return;
			}
		}
		populators.add(new BiomePopulator());
	}

	/**
	 * Called during biome generator's construction phase
	 */
	protected abstract void registerBiomes();

	protected void setSelector(BiomeSelector selector) {
		biomes.setSelector(selector);
	}

	public BiomeSelector getSelector() {
		return biomes.getSelector();
	}

	/**
	 * Register a new Biome Type to be generated by this generator
	 */
	public void register(Biome biome) {
		biomes.addBiome(biome);
	}

	@Override
	public void generate(CuboidBlockMaterialBuffer blockData, int chunkX, int chunkY, int chunkZ, World world) {
		final int x = chunkX << Chunk.BLOCKS.BITS;
		final int y = chunkY << Chunk.BLOCKS.BITS;
		final int z = chunkZ << Chunk.BLOCKS.BITS;
		final BiomeManager manager;
		if (blockData.getSize().getFloorX() <= Chunk.BLOCKS.SIZE && blockData.getSize().getFloorZ() <= Chunk.BLOCKS.SIZE) {
			BiomeManager temp = world.getBiomeManager(x, z, LoadOption.NO_LOAD);
			if (temp == null) {
				temp = generateBiomes(chunkX, chunkZ, world);
			}
			manager = temp;
		} else {
			final Vector3 size = blockData.getSize();
			final int xSize = size.getFloorX();
			final int zSize = size.getFloorZ();
			int xChunkSize = xSize >> Chunk.BLOCKS.BITS;
			int zChunkSize = zSize >> Chunk.BLOCKS.BITS;
			if (xSize % Chunk.BLOCKS.SIZE > 0) {
				xChunkSize++;
			}
			if (zSize % Chunk.BLOCKS.SIZE > 0) {
				zChunkSize++;
			}
			manager = new WrappedBiomeManager(this, world, chunkX, chunkZ, xChunkSize, zChunkSize);
		}
		final long seed = world.getSeed();
		generateTerrain(blockData, x, y, z, manager, seed);
		for (GeneratorPopulator generatorPopulator : generatorPopulators) {
			generatorPopulator.populate(blockData, x, y, z, manager, seed);
		}
	}

	public BiomeManager generateBiomes(int chunkX, int chunkZ, World world) {
		final int x = chunkX << Chunk.BLOCKS.BITS;
		final int z = chunkZ << Chunk.BLOCKS.BITS;
		final Simple2DBiomeManager biomeManager = new Simple2DBiomeManager(chunkX, chunkZ);
		byte[] biomeData = new byte[Chunk.BLOCKS.AREA];
		for (int dx = x; dx < x + Chunk.BLOCKS.SIZE; ++dx) {
			for (int dz = z; dz < z + Chunk.BLOCKS.SIZE; ++dz) {
				biomeData[(dz & Chunk.BLOCKS.MASK) << 4 | (dx & Chunk.BLOCKS.MASK)] =
						(byte) biomes.getBiome(dx, dz, world.getSeed()).getId();
			}
		}
		biomeManager.deserialize(biomeData);
		return biomeManager;
	}

	protected abstract void generateTerrain(CuboidBlockMaterialBuffer blockData, int x, int y, int z, BiomeManager manager, long seed);

	@Override
	public final Populator[] getPopulators() {
		return populators.toArray(new Populator[populators.size()]);
	}

	public void addPopulators(Populator... p) {
		populators.addAll(Lists.newArrayList(p));
	}

	public void addGeneratorPopulators(GeneratorPopulator... p) {
		generatorPopulators.addAll(Lists.newArrayList(p));
	}

	public Biome getBiome(int x, int y, int z, long seed) {
		return biomes.getBiome(x, y, z, seed);
	}

	public Biome getBiome(int x, int z, long seed) {
		return biomes.getBiome(x, z, seed);
	}

	public Set<Biome> getBiomes() {
		return biomes.getBiomes();
	}

	public BiomeMap getBiomeMap() {
		return biomes;
	}

	public int indexOf(Biome biome) {
		return biomes.indexOf(biome);
	}
}
