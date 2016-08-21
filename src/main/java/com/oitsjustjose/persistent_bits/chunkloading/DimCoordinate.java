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

	public DimCoordinate(BlockPos pos, int dim)
	{
		this.x = pos.getX();
		this.y = pos.getY();
		this.z = pos.getZ();
		this.dimension = dim;
	}

	public DimCoordinate(int x, int y, int z, int dim)
	{
		this.x = x;
		this.y = y;
		this.z = z;
		this.dimension = dim;
	}

	public BlockPos getPos()
	{
		return new BlockPos(x, y, z);
	}

	public int getDimensionID()
	{
		return this.dimension;
	}

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

	@Override
	public String toString()
	{
		return this.x + " " + this.y + " " + this.z + " dim = " + this.dimension;
	}
}