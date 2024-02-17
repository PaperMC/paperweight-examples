package de.verdox.mccreativelab.util;

import de.verdox.mccreativelab.events.ChunkDataEvent;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.util.Vector;

public class SectionPos extends Vector {
    public static final int SECTION_BITS = 4;
    public static final int SECTION_SIZE = 16;
    public static final int SECTION_MASK = 15;
    public static final int SECTION_HALF_SIZE = 8;
    public static final int SECTION_MAX_INDEX = 15;
    private static final int PACKED_X_LENGTH = 22;
    private static final int PACKED_Y_LENGTH = 20;
    private static final int PACKED_Z_LENGTH = 22;
    private static final long PACKED_X_MASK = 4194303L;
    private static final long PACKED_Y_MASK = 1048575L;
    private static final long PACKED_Z_MASK = PACKED_X_MASK;
    private static final int Y_OFFSET = 0;
    private static final int Z_OFFSET = 20;
    private static final int X_OFFSET = 42;
    private static final int RELATIVE_X_SHIFT = 8;
    private static final int RELATIVE_Y_SHIFT = 0;
    private static final int RELATIVE_Z_SHIFT = 4;

    SectionPos(int x, int y, int z) {
        super(x, y, z);
    }

    public static SectionPos of(int x, int y, int z) {
        return new SectionPos(x, y, z);
    }

    public static SectionPos of(Vector pos) {
        return new SectionPos(pos.getBlockX() >> 4, pos.getBlockY() >> 4, pos.getBlockZ() >> 4);
    }

    public static SectionPos of(ChunkDataEvent.ChunkPos chunkPos, int y) {
        return new SectionPos(chunkPos.x(), y, chunkPos.z());
    }

    public static SectionPos of(Location pos) {
        return new SectionPos(blockToSectionCoord(pos.blockX()), blockToSectionCoord(pos.blockY()), blockToSectionCoord(pos.blockZ()));
    }

    public static SectionPos of(long packed) {
        return new SectionPos((int)(packed >> 42), (int)(packed << 44 >> 44), (int)(packed << 22 >> 42));
    }

    public static SectionPos bottomOf(Chunk chunk) {
        return of(new ChunkDataEvent.ChunkPos(chunk.getX(), chunk.getZ()), getMinSection(chunk));
    }

    public static int getMinSection(Chunk chunk) {
        return SectionPos.blockToSectionCoord(chunk.getWorld().getMinHeight());
    }

    public static int blockToSectionCoord(int coord) {
        return coord >> SECTION_BITS;
    }

    public static int sectionRelative(int coord) {
        return coord & 15;
    }

    public static short sectionRelativePos(Location pos) {
        return (short)((pos.getBlockX() & 15) << RELATIVE_X_SHIFT | (pos.getBlockZ() & 15) << SECTION_BITS | pos.getBlockY() & 15);
    }

    public static int sectionRelativeX(short packedLocalPos) {
        return packedLocalPos >>> RELATIVE_X_SHIFT & 15;
    }

    public static int sectionRelativeY(short packedLocalPos) {
        return packedLocalPos >>> RELATIVE_Y_SHIFT & 15;
    }

    public static int sectionRelativeZ(short packedLocalPos) {
        return packedLocalPos >>> SECTION_BITS & 15;
    }

    public int relativeToBlockX(short packedLocalPos) {
        return this.minBlockX() + sectionRelativeX(packedLocalPos);
    }

    public int relativeToBlockY(short packedLocalPos) {
        return this.minBlockY() + sectionRelativeY(packedLocalPos);
    }

    public int relativeToBlockZ(short packedLocalPos) {
        return this.minBlockZ() + sectionRelativeZ(packedLocalPos);
    }

    public static int sectionToBlockCoord(int sectionCoord) {
        return sectionCoord << SECTION_BITS;
    }

    public static int sectionToBlockCoord(int chunkCoord, int offset) {
        return sectionToBlockCoord(chunkCoord) + offset;
    }

    public static int x(long packed) {
        return (int)(packed << 0 >> 42);
    }

    public static int y(long packed) {
        return (int)(packed << 44 >> 44);
    }

    public static int z(long packed) {
        return (int)(packed << 22 >> 42);
    }

    public int x() {
        return this.getBlockX();
    }

    public int y() {
        return this.getBlockY();
    }

    public int z() {
        return this.getBlockZ();
    }

    public final int minBlockX() {
        return this.getBlockX() << SECTION_BITS;
    }

    public final int minBlockY() {
        return this.getBlockY() << SECTION_BITS;
    }

    public int minBlockZ() {
        return this.getBlockZ() << SECTION_BITS;
    }

    public int maxBlockX() {
        return sectionToBlockCoord(this.x(), 15);
    }

    public int maxBlockY() {
        return sectionToBlockCoord(this.y(), 15);
    }

    public int maxBlockZ() {
        return sectionToBlockCoord(this.z(), 15);
    }

    public static long blockToSection(long blockPos) {
        return ((long)((int)(blockPos >> 42)) & PACKED_X_MASK) << 42 | (long)((int)(blockPos << 52 >> 56)) & PACKED_Y_MASK | ((long)((int)(blockPos << 26 >> 42)) & PACKED_X_MASK) << 20;
    }

    public static long getZeroNode(int x, int z) {
        return getZeroNode(asLong(x, 0, z));
    }

    public static long getZeroNode(long pos) {
        return pos & -1048576L;
    }

    public ChunkDataEvent.ChunkPos chunk() {
        return new ChunkDataEvent.ChunkPos(this.x(), this.z());
    }

    public static long blockPosAsSectionLong(int i, int j, int k) {
        return ((long)(i >> 4) & PACKED_X_MASK) << 42 | (long)(j >> 4) & PACKED_Y_MASK | ((long)(k >> 4) & PACKED_X_MASK) << 20;
    }

    public static long asLong(int x, int y, int z) {
        return ((long)x & PACKED_X_MASK) << 42 | (long)y & PACKED_Y_MASK | ((long)z & PACKED_X_MASK) << 20;
    }

    public long asLong() {
        return ((long)this.getX() & PACKED_X_MASK) << 42 | (long)this.getY() & PACKED_Y_MASK | ((long)this.getZ() & PACKED_X_MASK) << 20;
    }

    public SectionPos offset(int i, int j, int k) {
        return i == 0 && j == 0 && k == 0 ? this : new SectionPos(this.x() + i, this.y() + j, this.z() + k);
    }
}
