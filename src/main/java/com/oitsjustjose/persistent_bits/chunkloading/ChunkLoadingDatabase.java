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
 * 
 * @author oitsjustjose
 * 
 *         A class for storing and serializing the locations of all chunk loaders on server start Files are rewritten each time a Chunk Loader is broken or placed, preventing world corruption in the case of an unexpected world close / server shutdown
 */

public class ChunkLoadingDatabase
{
	private HashSet<DimCoordinate> chunkLoaderCoords = new HashSet<DimCoordinate>();
	File fileLocation = new File(DimensionManager.getCurrentSaveRootDirectory(), "PersistentBits.dat");

	/**
	 * @param newCoord
	 *            Coordinate to be added to the serialized file
	 */
	public void addChunkCoord(DimCoordinate newCoord)
	{
		this.chunkLoaderCoords.add(newCoord);
		this.serialize();
	}

	/**
	 * @param coordToRemove
	 *            Coordinate to be removed from the serialized file
	 */
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

	// This method outputs the chunkLoaderCoords
	// object to a .dar file which cannot be read
	// in an editor.
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

	// This method specifically takes the serialized
	// (i.e. exported) object from the .dat file
	// and reads it in as an initialized object.
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