package piecetable;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.RandomAccessFile;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Nick
 */
public class persist {
    private String originalPath;
    private String objectPath;
    private String editPath;
    private RandomAccessFile persistent_object;
    private RandomAccessFile originalPiece;
    private RandomAccessFile _edits;
    private PieceTable pieceTable;
    public long _text_len;
    private int max;
    public persist(String oPath,String ePath,String origPath, int max) {
        this.originalPath = origPath;
        this.objectPath = oPath;
        this.editPath = ePath;
        this.max = max;
        try {
            persistent_object = new RandomAccessFile(objectPath,"rw");
            originalPiece = new RandomAccessFile(originalPath,"rw");
            _edits = new RandomAccessFile(editPath, "rw");
            originalPiece.setLength(0);
            _edits.setLength(0);
            persistent_object.setLength(0);
        } catch (FileNotFoundException ex) {
            Logger.getLogger(PieceTable.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(PieceTable.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    public void add_original(int length) {
        pieceTable = new PieceTable(max);
        pieceTable.add_original(length);
        _text_len = pieceTable._text_len;
        serialize();
    }

    public void add(int length, int index,RandomAccessFile edits) {
        pieceTable = deserialize();
        pieceTable.add(length,index,edits);
        _text_len = pieceTable._text_len;
        serialize();
    }

    public byte[] get_text() {
        return pieceTable.get_text(_edits,originalPiece);
    }

    public byte[] find(long index, long length) {
        return pieceTable.find(index,length,_edits,originalPiece);
    }
    public void remove(long index, long length) {
        pieceTable = deserialize();
        pieceTable.remove(index,length);
        _text_len = pieceTable._text_len;
        serialize();
    }

    public void print_pieces() {
        pieceTable.print_pieces();
    }

    public void serialize() {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ObjectOutputStream out;
        byte[] bytes = null;
        try {
            out = new ObjectOutputStream(bos);
            out.writeObject(pieceTable);
            out.flush();
            bytes = bos.toByteArray();
            persistent_object.setLength(0);
            persistent_object.seek(0);
            persistent_object.write(bytes);} 
        catch (IOException ex) {
            Logger.getLogger(PieceTable.class.getName()).log(Level.SEVERE, null, ex);} 
        finally {
            try {
                bos.close();} 
            catch (IOException ex) {}
        }
    }

    public PieceTable deserialize(){
        try {
            int object_length = (int) persistent_object.length();
            byte[] object = new byte[object_length];
            persistent_object.seek(0);
            persistent_object.read(object);
            ByteArrayInputStream inputStream = new ByteArrayInputStream(object);
            ObjectInputStream objectInputStream = new ObjectInputStream(inputStream);
            pieceTable = (PieceTable) objectInputStream.readObject();
        } catch (IOException | ClassNotFoundException ex) {
            Logger.getLogger(PieceTable.class.getName()).log(Level.SEVERE, null, ex);
        }
        return pieceTable;
    }


}
