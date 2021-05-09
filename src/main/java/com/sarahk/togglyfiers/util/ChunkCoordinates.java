package com.sarahk.togglyfiers.util;

import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3i;
import org.jetbrains.annotations.NotNull;

public class ChunkCoordinates implements Comparable<ChunkCoordinates>
{
	public ChunkCoordinates() {}
	
	public ChunkCoordinates(int par1, int par2, int par3)
	{
		this.x = par1;
		this.y = par2;
		this.z = par3;
	}
	
	public ChunkCoordinates(ChunkCoordinates par1ChunkCoordinates)
	{
		this.x = par1ChunkCoordinates.getX();
		this.y = par1ChunkCoordinates.getY();
		this.z = par1ChunkCoordinates.getZ();
	}
	
	public ChunkCoordinates(final Vec3i pos)
	{
		this(pos.getX(), pos.getY(), pos.getZ());
	}
	
	public int getX()
	{
		return x;
	}
	
	/**
	 * the y coordinate
	 */
	public int getY()
	{
		return y;
	}
	
	/**
	 * the z coordinate
	 */
	public int getZ()
	{
		return z;
	}
	
	/**
	 * Compare the coordinate with another coordinate
	 */
	@Override
	public int compareTo(@NotNull final ChunkCoordinates that)
	{
		if(this.getY() == that.getY())
		{
			return (this.getZ()==that.getZ()
						? this.getX() - that.getX()
						: this.getZ() - that.getZ());
		}
		else
		{
			return this.getY() - that.getY();
		}
	}
	
	/**
	 * Returns the squared distance between this coordinates and the coordinates given as argument.
	 */
	public int getDistanceSquared(int par1, int par2, int par3)
	{
		int var4 = this.getX() - par1;
		int var5 = this.getY() - par2;
		int var6 = this.getZ() - par3;
		return (var4*var4 + var5*var5 + var6*var6);
	}
	
	@Override
	public int hashCode()
	{
		return this.getX() + this.getZ() << 8 + this.getY() << 16;
	}
	
	@Override
	public boolean equals(Object value)
	{
		boolean result = false;
		if(value instanceof ChunkCoordinates)
		{
			ChunkCoordinates that = (ChunkCoordinates) value;
			result = this.getX()==that.getX() && this.getY()==that.getY() && this.getZ()==that.getZ();
		}
		return result;
	}
	
	public void set(int par1, int par2, int par3)
	{
		this.x = par1;
		this.y = par2;
		this.z = par3;
	}
	
	@NotNull
	public BlockPos toBlockPos()
	{
		return new BlockPos(getX(), getY(), getZ());
	}
	private int x;
	private int y;
	private int z;
}
