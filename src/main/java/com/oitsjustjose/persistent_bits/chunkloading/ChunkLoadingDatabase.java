package com.oitsjustjose.persistent_bits.chunkloading;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.HashSet;

import com.oitsjustjose.persistent_bits.PersistentBits;

import net.minecraftforge.common.DimensionManager;

/**
 * A class for storing and serializing the locations of all chunk loaders on server start
 */

public class ChunkLoadingDatabase
{
	private HashSet<DimCoordinate> chunkLoaderCoords = new HashSet<DimCoordinate>();
	File fileLocation = new File(DimensionManager.getCurrentSaveRootDirectory(), "PersistentBits.dat");

	public void addChunkCoord(DimCoordinate newCoord)
	{
		this.chunkLoaderCoords.add(newCoord);
		this.serialize();
	}

	public void removeChunkCoord(DimCoordinate coordToRemove)
	{
		for (DimCoordinate d : this.chunkLoaderCoords)
		{
			if (d.equals(coordToRemove))
			{
				this.chunkLoaderCoords.remove(d);
				break;
			}
		}
		this.serialize();
	}

	public HashSet<DimCoordinate> getCoordinates()
	{
		return this.chunkLoaderCoords;
	}

	public void serialize()
	{
		try
		{
			FileOutputStream fileOut = new FileOutputStream(fileLocation);
			ObjectOutputStream out = new ObjectOutputStream(fileOut);
			out.writeObject(this.chunkLoaderCoords);
			out.close();
			fileOut.close();
		}
		catch (IOException i)
		{
			PersistentBits.LOGGER.error("There was an error saving PersistentBits.dat");
			return;
		}
	}

	@SuppressWarnings("unchecked")
	public void deserialize()
	{
		try
		{
			if (fileLocation.exists())
			{
				FileInputStream fileIn = new FileInputStream(fileLocation);
				ObjectInputStream in = new ObjectInputStream(fileIn);
				this.chunkLoaderCoords = (HashSet<DimCoordinate>) in.readObject();
				in.close();
				fileIn.close();
			}
		}
		catch (IOException i)
		{
			PersistentBits.LOGGER.error("There was an error loading PersistentBits.dat");
			return;
		}
		catch (ClassNotFoundException c)
		{
			PersistentBits.LOGGER.error("There was an error in the code for deserialization. Please contact oitsjustjose on GitHub with a log");
			PersistentBits.LOGGER.error(c.getMessage());
			return;
		}
	}
}