package de.verdox.mccreativelab.util;

import org.bukkit.Bukkit;
import org.bukkit.Instrument;
import org.bukkit.Material;
import org.bukkit.Note;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.type.NoteBlock;
import org.jetbrains.annotations.ApiStatus;

import java.util.Iterator;
import java.util.NoSuchElementException;

public abstract class BlockStateIterator<T extends BlockData> implements Iterator<T> {
    private final T initValue;
    protected T next;


    public BlockStateIterator(T next) {
        this.initValue = next;
        this.next = next;
    }

    @ApiStatus.Internal
    public void reset(){
        this.next = this.initValue;
    }

    @Override
    public boolean hasNext() {
        return this.next != null;
    }

    public static class NoteBlockStatesIterator extends BlockStateIterator<NoteBlock> {
        private int currentInstrument = 0;
        private int currentNote = 1;
        private boolean powered = false;

        public NoteBlockStatesIterator() {
            super((NoteBlock) Bukkit.createBlockData(Material.NOTE_BLOCK));
        }

        @Override
        public void reset() {
            super.reset();
            this.currentInstrument = 0;
            this.currentNote = 1;
            this.powered = false;
        }

        @Override
        public NoteBlock next() {
            if (!hasNext())
                throw new NoSuchElementException("Iterator is empty");

            NoteBlock returnValue = this.next;
            NoteBlock nextValue = (NoteBlock) returnValue.clone();
            int amountInstruments = Instrument.values().length;

            nextValue.setNote(new Note(currentNote));
            nextValue.setInstrument(Instrument.values()[currentInstrument]);
            nextValue.setPowered(powered);

            this.next = nextValue;
            if(powered && currentNote >= 25 && currentInstrument >= amountInstruments)
                this.next = null;
            else {
                currentNote++;
                if(currentNote >= 25){
                    currentNote = 0;
                    currentInstrument++;
                }

                if(currentInstrument >= amountInstruments){
                    if(!powered){
                        currentInstrument = 0;
                        powered = true;
                    }
                    else {
                        this.next = null;
                    }

                }
            }
            return returnValue;
        }
    }
}
