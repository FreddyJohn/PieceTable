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
    public Edit currentEdit;
    public Edit currentRedo;
    public int editIndex = -1;
    public int redoIndex = -1;
    
    
    public void pushEdit(Edit newEdit){
        edits.add(newEdit);
        editIndex+=1;
    }
    
    private void push(Edit edit){
        edits.add(edit);
        editIndex+=1;
    }
    private void pop(){
        edits.remove(editIndex);
        editIndex-=1;
    }
    public PieceTableAPI undo(PieceTableAPI sequence){
        if(editIndex>0){
            currentEdit = edits.get(editIndex);
            switch(currentEdit.editType)
            {
                case "addition":
                    sequence.remove(currentEdit.offset,currentEdit.length);
                    break;
                case "remove":
                    sequence.add(currentRedo.length,currentRedo.offset);
                    break;
            }
            
            redos.add(currentEdit);
            redoIndex+=1;
            pop();
        }
        return sequence;
    }
    public PieceTableAPI redo(PieceTableAPI sequence){ 
        if(redoIndex>=0){
            currentRedo = redos.get(redoIndex);
            switch(currentRedo.editType){
                case "addition":
                    sequence.add(currentRedo.length,currentRedo.offset);
                    break;
                case "remove":
                    sequence.remove(currentRedo.offset, currentRedo.length);
                    break;
            }
            push(currentRedo);
            redos.remove(redoIndex); 
            redoIndex-=1;
        }
        return sequence;
    }
    public void printEdits(){
        System.out.println("Edit Stack");
        edits.forEach((edit) -> {
            System.out.println("editType: "+edit.editType+", editOffset: "+edit.offset+", editLength: "+edit.length);
        });
    }
    public void printRedo(){
        System.out.println("Redo Stack");
        redos.forEach((edit) -> {
            System.out.println("editType: "+edit.editType+", editOffset: "+edit.offset+", editLength: "+edit.length);
        });
    }
}

