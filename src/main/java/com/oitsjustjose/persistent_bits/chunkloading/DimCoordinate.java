package com.oitsjustjose.persistent_bits.chunkloading;

import java.io.Serializable;

import net.minecraft.util.math.BlockPos;

/**
 * A simple object which stores X, Y and Z coordinates, as well as the dimension - this is used to easily serialize the locations
 */

public class DimCoordinate implements Serializable
{
	private static final long serialVersionUID = -2309676475992160338L;
	private int x, y, z, dimension;

	/**
	 * @param pos
	 *            The BlockPos of the Block in question
	 * @param dim
	 *            The Dimension in which the BlockPos parameter is located
	 */
	public DimCoordinate(BlockPos pos, int dim)
	{
		this.x = pos.getX();
		this.y = pos.getY();
		this.z = pos.getZ();
		this.dimension = dim;
	}

	/**
	 * @param x
	 *            The x-coord of the block
	 * @param y
	 *            The y-coord of the block
	 * @param z
	 *            The z-coord of the block
	 * @param dim
	 *            The Dimension in which the block is located
	 */
	public DimCoordinate(int x, int y, int z, int dim)
	{
		this.x = x;
		this.y = y;
		this.z = z;
		this.dimension = dim;
	}

	/**
	 * @return The BlockPos of the DimCoordinate
	 */
	public BlockPos getPos()
	{
		return new BlockPos(x, y, z);
	}

	/**
	 * @return The Dimension ID of the DimCoordinate
	 */
	public int getDimensionID()
	{
		return this.dimension;
	}

	// Overridden to ensure proper functionality
	@Override
	public boolean equals(Object other)
	{
		if (other == this)
			return true;
		if (other instanceof DimCoordinate)
		{
			DimCoordinate detOther = (DimCoordinate) other;
			if (detOther.getDimensionID() == this.getDimensionID())
				if (detOther.getPos().getX() == this.x)
					if (detOther.getPos().getY() == this.y)
						if (detOther.getPos().getZ() == this.z)
							return true;
		}
		return false;
	}

	// Overridden to simplify logging / debug
	@Override
	public String toString()
	{
		return this.x + " " + this.y + " " + this.z + " dim = " + this.dimension;
	}
}