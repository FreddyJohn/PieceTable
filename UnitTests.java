package piecetable;
import java.io.IOException;
import java.io.RandomAccessFile;
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
public class UnitTests {
    private static PieceTable pieceTable;
    public static void main(String[] args) {
       String path ="C:\\Users\\Nick\\Downloads\\audioResearch\\hello1.txt";
       RandomAccessFile file = null;
       pieceTable = new PieceTable(path);
       pieceTable.set_max_piece_length(3);

       try {
            file = new RandomAccessFile(path,"rw");
        } 
        catch (IOException ex) {
            Logger.getLogger(PieceTable.class.getName()).log(Level.SEVERE, null, ex);
        }
       write_piece("hello",0,1,file,pieceTable);
       write_piece("hellogood12byehello",5,1,file,pieceTable);
       write_piece("...testing123 ...",12,1,file,pieceTable);
       write_piece("hello",1,1,file,pieceTable);
       System.out.print(new String(pieceTable.find(0, pieceTable._text_len)));

       
    }
    public static void write_piece(String piece,int index,int convert, RandomAccessFile file, PieceTable pieceTable){
        if (pieceTable._text_len==0){
            try {
                file.seek(pieceTable._text_len);
                file.write(piece.getBytes());
            } 
            catch (IOException ex) {
                Logger.getLogger(PieceTable.class.getName()).log(Level.SEVERE, null, ex);
            }
            pieceTable.add_original(piece.length());
        }
        else{
            try {
                file.seek(pieceTable._text_len);
                file.write(piece.getBytes());
            } catch (IOException ex) {
                Logger.getLogger(PieceTable.class.getName()).log(Level.SEVERE, null, ex);
            }
            pieceTable.add(index,piece.length()*convert);
        }
    }

}
