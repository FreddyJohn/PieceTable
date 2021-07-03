package piecetable;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Nick
 * 
 * PieceTable modified to have only one buffer and to work with any
 * arbitrary byte stream
 * 
 */
public class UnitTests {
    private static String path = "C:\\Users\\Nick\\Downloads\\audioResearch\\hello1.txt";
    private static String path1 = "C:\\Users\\Nick\\Downloads\\audioResearch\\hello.txt";
    private static String path2 = "C:\\Users\\Nick\\Downloads\\audioResearch\\hello2.txt";
    public static void main(String[] args) {
       test_persist();
       test_add_remove();
    }
     public static void test_add_remove(){
        persist piecetable = new persist(path1,path,path2,3);
        RandomAccessFile file = null;
        RandomAccessFile edits = null;
        try {
            file = new RandomAccessFile(path2,"rw");
            edits = new RandomAccessFile(path,"rw");
            file.write("hellogoodbye".getBytes());
        }
        catch (IOException ex) {
            Logger.getLogger(PieceTable.class.getName()).log(Level.SEVERE, null, ex);
        }
        System.out.println("adding original");
        piecetable.add_original(12);
        System.out.println(piecetable._text_len);
        
        System.out.println("removing a piece");
        piecetable.remove(piecetable._text_len-10, 3);
        
        System.out.println(piecetable._text_len);
        System.out.println(new String(piecetable.get_text()));
        piecetable.print_pieces();
        try {
            edits.seek(edits.length());
            edits.write("g".getBytes());
        }
        catch (IOException ex) {
            Logger.getLogger(PieceTable.class.getName()).log(Level.SEVERE, null, ex);
        }
        System.out.println("adding a piece after removing one");
        piecetable.add(1, 0,edits);

        System.out.println(new String(piecetable.get_text()));
        piecetable.print_pieces();

    }
    
    public static void test_persist(){
        persist piecetable = new persist(path1,path,path2,3);
        RandomAccessFile file = null;
        RandomAccessFile edits = null;
        try {
            file = new RandomAccessFile(path2,"rw");
            edits = new RandomAccessFile(path,"rw");
            file.write("hellogoodbye".getBytes());
        }
        catch (IOException ex) {
            Logger.getLogger(PieceTable.class.getName()).log(Level.SEVERE, null, ex);
        }
        System.out.println("adding original");
        piecetable.add_original(12);
        System.out.println(piecetable._text_len);
        System.out.println(new String(piecetable.get_text()));
        try {
            edits.write("goodbye".getBytes());
        }
        catch (IOException ex) {
            Logger.getLogger(PieceTable.class.getName()).log(Level.SEVERE, null, ex);
        }
        System.out.println("adding new piece");
        piecetable.add(7,0,edits);
 
        System.out.println(piecetable._text_len);
        System.out.println(new String(piecetable.get_text()));
        System.out.println("removing a piece");
        piecetable.remove(piecetable._text_len-10, 3);
        System.out.println(new String(piecetable.get_text()));
        piecetable.print_pieces();        
        try {
            edits.seek(edits.length());
            edits.write("g".getBytes());
        }
        catch (IOException ex) {
            Logger.getLogger(PieceTable.class.getName()).log(Level.SEVERE, null, ex);
        }
        System.out.println("adding a piece after removing one");
        piecetable.add(1, (int) piecetable._text_len,edits);
        System.out.println(new String(piecetable.get_text()));
        piecetable.print_pieces();        

        printFile(edits);
        
    }
    
    public static void printFile(RandomAccessFile file){
        try {
            byte[] bytes = new byte[(int)file.length()];
            file.seek(0);
            file.read(bytes);
            System.out.println(new String(bytes));
        } catch (IOException ex) {
            Logger.getLogger(UnitTests.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
