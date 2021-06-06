package piecetable;
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
 * PieceTable modified to have only one in RAM buffer, to work with any
 * arbitrary byte stream, and ability to set maximum piece length
 * 
 * 
 */
public class PieceTable implements Serializable{
    public  long _text_len;
    public  ArrayList<_Piece> pieces;
    private final int max_piece_length;
    public PieceTable(int max){
        pieces = new ArrayList<>();
        this.max_piece_length = max;
    }

    public void print_pieces(){
        this.pieces.forEach((piece) -> {
            System.out.println(piece.in_added+","+piece.length+","+piece.offset);
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
    private Pair get_pieces_and_offset(long index){
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
    /*
    public PieceTable add(int index, int length){
        //if (length<=0){
        //    return this;
        //}
        //_add(max_piece_length, index);
        for(int piece = max_piece_length; piece<length; piece+=max_piece_length){
            _add(max_piece_length, index+piece);
        }            
        if (length % max_piece_length != 0){
            _add(length % max_piece_length, index + length - (length % max_piece_length));
        }
        return this;
    }
    */

    //public void add_original(int length){
    //    _text_len = max_piece_length;
    //    pieces.add(new _Piece(false,0,max_piece_length));
    //    add(max_piece_length,length);
    //}  
   
    public void add_original(int length){
        _text_len = length;
        pieces.add(new _Piece(false,0,length));
    }  
    public PieceTable add(int length,int index){
    //public void _add(int length,int index){
        if (length==0){
            return this;
        }
        Pair pair = get_pieces_and_offset(index);
        int piece_index = (int) pair.first;
        _Piece curr_piece = pieces.get(piece_index);
        long piece_offset= (long) pair.second;
        long added_offset = _text_len;
        _text_len += length;
        if (curr_piece.in_added && piece_offset == curr_piece.offset + (curr_piece.length == added_offset ? 1:0)){
            curr_piece.length += length;
            return this;
        }
        ArrayList<_Piece> insert_pieces = new ArrayList<>();
        insert_pieces.add(new _Piece(curr_piece.in_added,curr_piece.offset, piece_offset - curr_piece.offset));
        insert_pieces.add(new _Piece(true, added_offset, length));
        insert_pieces.add(new _Piece(curr_piece.in_added,piece_offset,curr_piece.length-(piece_offset - curr_piece.offset)));
        insert_pieces =filter(insert_pieces);
        pieces = splice(piece_index,1,insert_pieces);
        return this;
    }
    public byte[] get_text(RandomAccessFile _edits){
        ByteBuffer doc = ByteBuffer.allocate((int) _text_len);
        pieces.forEach((piece) -> {
            doc.put(get_chunk(_edits, piece.offset, piece.offset + piece.length));
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
    public byte[] find(long index,long length,RandomAccessFile _edits){
        if(length<0){
            return find(index+length, -length, _edits);
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
    public PieceTable remove(long index, long length) {
        if(length==0){
            return this;
        }
        if(length<0){
            remove(index+length,-length);
        }
        if(index<0){
            try {
                throw new Exception("Index out of Bounds");
            } catch (Exception ex) {
                Logger.getLogger(PieceTable.class.getName()).log(Level.SEVERE, null, ex);
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
                return this;
            }
            else if (stop_piece_offset == piece.offset+piece.length){
                piece.length-=length;
                return this;
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
        return this;
    }
       public static class _Piece implements Serializable{
        public boolean in_added;
        public long offset;
        public long length;
        public _Piece(boolean in_added, long offset, long length){
            this.in_added=in_added;
            this.offset=offset;
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

