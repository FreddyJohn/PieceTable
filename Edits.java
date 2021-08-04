package piecetable;

import java.io.RandomAccessFile;
import java.util.ArrayList;

/**
 *
 * @author Nick
 * 
 * Implementation of Linear Undo Model
 * 
 * 1.)user makes edits and PieceTable buffer has n pieces of variable size
 * 
 * |--|---|--|-|------|---|
 * 
 * 2.)user wants to undo this series of edits in a linear fashion 
 *  so for lets imagine the buffer shown in (1) are only pieces added directly
 *  after one another.
 * 
 * |--|---|--|
 * 
 * 3.) user wants to redo after a series of undo actions. Clearly we can see that we must store each undo.
 * 
 *
 * |--|---|--|-|  
 *            ^    ^     ^
 *              |------|---|
 * 
 * 
 */
public class Edits{
    private final ArrayList<Edit> edits = new ArrayList<>();
    private ArrayList<Edit> redos = new ArrayList<>();
    private Edit currentEdit;
    private Edit currentRedo;
    public int editIndex;
    public int redoIndex;
    
    public void pushEdit(Edit newEdit){
        edits.add(newEdit);
        redos = new ArrayList<>();
        redoIndex= -1;
        editIndex+=1;
    }
    
    private void push(Edit edit){
        edits.add(edit);
        editIndex+=1;
    }
    private void pop(){
        edits.remove(editIndex-1);
        editIndex-=1;
    }
    public persist undo(persist sequence, RandomAccessFile buffer){
        if(editIndex>0){
            currentEdit = edits.get(editIndex-1);
            switch(currentEdit.editType)
            {
                case "addition":
                    sequence.remove(currentEdit.offset,currentEdit.length);
                    break;
                case "remove":
                    sequence.add(currentEdit.offset,currentEdit.length,buffer);
            }
            redos.add(currentEdit);
            redoIndex+=1;
            pop();
        }
        return sequence;
    }
    public persist redo(persist sequence, RandomAccessFile buffer){ 
        if(redoIndex>=0){
            currentRedo = redos.get(redoIndex);
            switch(currentRedo.editType){
                case "addition":
                    sequence.add(currentRedo.length,currentRedo.offset, buffer);
                    break;
                case "remove":
                    sequence.remove(currentRedo.offset, currentRedo.length);
            }
            push(currentRedo);
            redos.remove(redoIndex); 
            redoIndex-=1;
        }
        return sequence;
    }
}

