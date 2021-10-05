package piecetable;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.io.Serializable;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

/*
 * @author Nick
 * 
 * 
 */
public class PieceTable implements Serializable{
    public long byte_length;
    public ArrayList<_Piece> pieces;
    public ArrayList<_Piece> redoStack;
    public ArrayList<_Piece> undoStack;
    public long index;
    public PieceTable(){
        pieces = new ArrayList<>();
    }
    public void print_pieces(){
        System.out.println("PieceTable Pieces Stack");
        this.pieces.forEach((piece) -> {
            System.out.println(piece.in_added+","+piece.length+","+piece.sequenceIndex);
        });
    }
    public long get_length(RandomAccessFile f){
        long length=0;
        try {
            length= f.length();
        } catch (IOException ex) {
            Logger.getLogger(PieceTable.class.getName()).log(Level.SEVERE, null, ex);
        }
        return length;
    }
    private ArrayList filter(ArrayList<_Piece> pieces){
        ArrayList<_Piece> filtered =new ArrayList<>();
        pieces.stream().filter((piece) -> (piece.length > 0)).forEachOrdered((piece) -> {
            filtered.add(piece);
        });
        return filtered;
    }
    private ArrayList splice(int start,int count,ArrayList<_Piece> items){
        ArrayList<_Piece> list = new ArrayList<>();
        for(int i=0;i<start;i++){
            list.add(pieces.get(i));
        }
        items.forEach((piece) -> {
            list.add((_Piece) piece);
        });
        for(int i =start+count; i<pieces.size(); i++){
            list.add(pieces.get(i));
        }
        return list;
    }
    private Pair get_pieces_and_offset(long index, int length){
        if (index<0){
           return null;
        }
        long remainingOffset=index;
        for(int i=0;i<pieces.size();i++){
            _Piece p=pieces.get(i);
            if (remainingOffset <= p.length){
                return new Pair(i, p.sequenceIndex + remainingOffset);
            }    
            remainingOffset -= p.length;   
        }
        return null;
    }
    
    public void add_original(int offset, int length){
        pieces.add(new _Piece(false,offset,length));
        byte_length = length;
    }
    public PieceTable add(int length,int index, RandomAccessFile buffer){
        if (length==0){
            return this;
        }
        Pair pair = get_pieces_and_offset(index,length);
        int piece_index = (int) pair.first;
        long piece_offset= (long) pair.second;
        _Piece curr_piece = pieces.get(piece_index);     
        
        try {
            this.index=buffer.length();
        } catch (IOException ex) {
            Logger.getLogger(PieceTable.class.getName()).log(Level.SEVERE, null, ex);
        }
        long added_offset = this.index - length;
        byte_length += length;
        /*
        if (curr_piece.in_added & piece_offset == curr_piece.sequenceIndex + (curr_piece.length == added_offset ? 1:0)){
            System.out.println("this");
            curr_piece.length += length;
            return this;
        }*/
        
                
        ArrayList<_Piece> insert_pieces = new ArrayList<>();
        insert_pieces.add(new _Piece(curr_piece.in_added,curr_piece.sequenceIndex, piece_offset - curr_piece.sequenceIndex));
        insert_pieces.add(new _Piece(true, added_offset, length));
        insert_pieces.add(new _Piece(curr_piece.in_added,piece_offset,curr_piece.length-(piece_offset - curr_piece.sequenceIndex)));
        insert_pieces = filter(insert_pieces);
        pieces = splice(piece_index,1,insert_pieces);
        return this;
    }
    public byte[] get_text(RandomAccessFile _edits, RandomAccessFile _origPiece){
        ByteBuffer doc = ByteBuffer.allocate((int) byte_length);
        pieces.forEach((piece) -> {
            if (piece.in_added){
                doc.put(get_chunk(_edits, piece.sequenceIndex, piece.sequenceIndex + piece.length));
            }else{
                doc.put(get_chunk(_origPiece, piece.sequenceIndex, piece.sequenceIndex + piece.length));
            }
        });
        return doc.array();
    }
    private byte[] get_chunk(RandomAccessFile file,long start,long stop){
        long length = stop-start;
        byte[] bytes = new byte[(int)length];
        try {
            file.seek(start);
            file.read(bytes,0, (int) length);
        } catch (IOException ex) {
            Logger.getLogger(PieceTable.class.getName()).log(Level.SEVERE, null, ex);
        }
        return bytes;
    }
    public byte[] find(long index,long length,RandomAccessFile _edits,RandomAccessFile origPiece){
        if(length<0){
            return null;
        }
        ByteBuffer doc = ByteBuffer.allocate((int) length);
        Pair start_pair = get_pieces_and_offset(index, (int) length);
        Pair stop_pair = get_pieces_and_offset(index+length, (int) length);
        int start_piece_index=(int)start_pair.first;
        long start_piece_offset=(long)start_pair.second;
        int stop_piece_index=(int)stop_pair.first;
        long stop_piece_offset=(long)stop_pair.second;
        
        _Piece start_piece = pieces.get(start_piece_index);
        RandomAccessFile buffer = start_piece.in_added ? _edits : origPiece;
        if(start_piece_index==stop_piece_index){
            doc.put(get_chunk(buffer,start_piece_offset,start_piece_offset + length));
        }
        else{
            doc.put(get_chunk(buffer,start_piece_offset,start_piece.sequenceIndex + start_piece.length));
            for(int i =start_piece_index+1;i<stop_piece_index+1;i++){
                _Piece cur_piece=pieces.get(i);
                buffer = cur_piece.in_added ? _edits : origPiece;
                if (i==stop_piece_index){
                    doc.put(get_chunk(buffer,cur_piece.sequenceIndex,stop_piece_offset));
                }
                else{
                    doc.put(get_chunk(buffer,cur_piece.sequenceIndex,cur_piece.sequenceIndex+cur_piece.length));
                }
            }
        }
        return doc.array();
    }
    public PieceTable remove(long index, long length) {
        if(length==0){
            return this;
        }

        if(index<0){
            try {
                throw new Exception("Index out of Bounds");
            } catch (Exception ex) {
                Logger.getLogger(PieceTable.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        Pair start_pair = get_pieces_and_offset(index, (int) length);
        Pair stop_pair = get_pieces_and_offset(index+length, (int) length);
        int start_piece_index=(int)start_pair.first;
        long start_piece_offset=(long)start_pair.second;
        int stop_piece_index=(int)stop_pair.first;
        long stop_piece_offset=(long)stop_pair.second;
        byte_length -= length;
        if (start_piece_index == stop_piece_index){
            _Piece piece = pieces.get(start_piece_index);
            if (start_piece_offset==piece.sequenceIndex){
                piece.sequenceIndex+=length;
                piece.length-=length;
                return this;
            }
            else if (stop_piece_offset == piece.sequenceIndex+piece.length){
                piece.length-=length;
                return this;
            }
        }
        
        _Piece start_piece = pieces.get(start_piece_index);
        _Piece end_piece = pieces.get(stop_piece_index);
        ArrayList<_Piece> delete_pieces = new ArrayList<>();
        delete_pieces.add(new _Piece(start_piece.in_added,start_piece.sequenceIndex, start_piece_offset - start_piece.sequenceIndex));
        delete_pieces.add(new _Piece(end_piece.in_added, stop_piece_offset, end_piece.length -(stop_piece_offset-end_piece.sequenceIndex)));
        delete_pieces = filter(delete_pieces);
        int delete_count = stop_piece_index - start_piece_index + 1;
        pieces = splice(start_piece_index,delete_count,delete_pieces);
        return this;
    }
       public static class _Piece implements Serializable{
        public boolean in_added;
        public long sequenceIndex;
        public long length;
        public _Piece(boolean in_added, long offset,
                long length){
            this.in_added=in_added;
            this.sequenceIndex=offset;
            this.length=length;
              
        }
    }
}
class Pair<U, V> implements Serializable
{
    public final U first;     
    public final V second;
    public Pair(U first, V second)
    {
        this.first = first;
        this.second = second;
    }
}

