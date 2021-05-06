package piecetree;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.io.Serializable;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Nick
 * 
 * PieceTable modified to have only one buffer and to work with any
 * arbitrary byte stream
 * 
 * 
 */
public class PieceTableAPI implements Serializable{
    public static long _text_len;
    private static ArrayList<_Piece> pieces;
    private static RandomAccessFile _edits;
    public PieceTableAPI(String editPath){
        pieces = new ArrayList<>();
        try {
            _edits = new RandomAccessFile(editPath,"rw");
            _edits.setLength(0);
        } catch (FileNotFoundException ex) {
            Logger.getLogger(PieceTableAPI.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(PieceTableAPI.class.getName()).log(Level.SEVERE, null, ex);
        }
        
    }
    public static long get_length(RandomAccessFile f){
        long length=0;
        try {
            length= f.length();
        } catch (IOException ex) {
            Logger.getLogger(PieceTableAPI.class.getName()).log(Level.SEVERE, null, ex);
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
    private static Pair get_pieces_and_offset(long index){
        if (index<0){
           return null;
        }
        long remainingOffset=index;
        int i;
        for(i=0;i<pieces.size();i++){
            _Piece p=pieces.get(i);
            if (remainingOffset <= p.length){
                return new Pair(i,p.offset+remainingOffset);
            }    
            else{
                remainingOffset-=p.length;
            }
        }
        return null;
    }
    public void add_original(int length){
        _text_len = length;
        pieces.add(new _Piece(false,0,length));
    }
    public void add(int length,int index){
        if (length==0){
            return;
        }
        Pair pair = get_pieces_and_offset(index);
        int piece_index = (int) pair.first;
        _Piece curr_piece = pieces.get(piece_index);
        long piece_offset= (long) pair.second;
        long added_offset = _text_len;
        _text_len += length;
        if (curr_piece.in_added && piece_offset == curr_piece.offset + (curr_piece.length == added_offset ? 1:0)){
            curr_piece.length += length;
            return;
        }
        ArrayList<_Piece> insert_pieces = new ArrayList<>();
        insert_pieces.add(new _Piece(curr_piece.in_added,curr_piece.offset, piece_offset - curr_piece.offset));
        insert_pieces.add(new _Piece(true, added_offset, length));
        insert_pieces.add(new _Piece(curr_piece.in_added,piece_offset,curr_piece.length-(piece_offset - curr_piece.offset)));  
        insert_pieces =filter(insert_pieces);   
        pieces = splice(piece_index,1,insert_pieces);

        
    }
    public byte[] get_text(){
        ByteBuffer doc = ByteBuffer.allocate((int) _text_len);
        pieces.forEach((piece) -> {
            doc.put(get_chunk(_edits,piece.offset,piece.offset+piece.length));
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
            Logger.getLogger(PieceTableAPI.class.getName()).log(Level.SEVERE, null, ex);
        }
        return bytes;
    }
    public byte[] find(long index,long length){
        if(length<0){
            return find(index+length, -length);
        }
        ByteBuffer doc = ByteBuffer.allocate((int) length);
        Pair start_pair = get_pieces_and_offset(index);
        Pair stop_pair = get_pieces_and_offset(index+length);
        int start_piece_index=(int)start_pair.first;
        long start_piece_offset=(long)start_pair.second;
        int stop_piece_index=(int)stop_pair.first;
        long stop_piece_offset=(long)stop_pair.second;
        _Piece start_piece = pieces.get(start_piece_index);
        RandomAccessFile buffer = _edits;
        if(start_piece_index==stop_piece_index){
            doc.put(get_chunk(buffer,start_piece_offset,start_piece_offset + length));
        }
        else{
            doc.put(get_chunk(buffer,start_piece_offset,start_piece.offset + start_piece.length));
            for(int i =start_piece_index+1;i<stop_piece_index+1;i++){
                _Piece cur_piece=pieces.get(i);
                if (i==stop_piece_index){
                    doc.put(get_chunk(buffer,cur_piece.offset,stop_piece_offset));
                }
                else{
                    doc.put(get_chunk(buffer,cur_piece.offset,cur_piece.offset+cur_piece.length));
                }
            }

        }
        return doc.array();
    }
    public void remove(long index, long length) {
        if(length==0){
            return;
        }
        if(length<0){
            remove(index+length,-length);
        }
        if(index<0){
            try {
                throw new Exception("Index out of Bounds");
            } catch (Exception ex) {
                Logger.getLogger(PieceTableAPI.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        Pair start_pair = get_pieces_and_offset(index);
        Pair stop_pair = get_pieces_and_offset(index+length);
        int start_piece_index=(int)start_pair.first;
        long start_piece_offset=(long)start_pair.second;
        int stop_piece_index=(int)stop_pair.first;
        long stop_piece_offset=(long)stop_pair.second; 
        _text_len -= length;
        if (start_piece_index == stop_piece_index){
            _Piece piece = pieces.get(start_piece_index);
            if (start_piece_offset==piece.offset){
                piece.offset+=length;
                piece.length-=length;
                return;
            }
            else if (stop_piece_offset == piece.offset+piece.length){
                piece.length-=length;
                return;
            }
        }
        _Piece start_piece = pieces.get(start_piece_index);
        _Piece end_piece = pieces.get(stop_piece_index);
        ArrayList<_Piece> delete_pieces = new ArrayList<>();
        delete_pieces.add(new _Piece(start_piece.in_added,start_piece.offset, start_piece_offset - start_piece.offset));
        delete_pieces.add(new _Piece(end_piece.in_added, stop_piece_offset, end_piece.length -(stop_piece_offset-end_piece.offset))); 
        
        delete_pieces = filter(delete_pieces);
        int delete_count = stop_piece_index - start_piece_index + 1;
        pieces = splice(start_piece_index,delete_count,delete_pieces);
    }
    }
class _Piece{
    public boolean in_added;
    public long offset;
    public long length;
    public _Piece(boolean in_added,long offset, long length){
        this.in_added=in_added;
        this.offset=offset;
        this.length =length;
    }
}
class Pair<U, V>
{
    public final U first;     
    public final V second;
    public Pair(U first, V second)
    {
        this.first = first;
        this.second = second;
    }
}

