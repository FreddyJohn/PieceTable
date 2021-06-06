/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package piecetable;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.util.Arrays;
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
    public static void main(String[] args) {
       test_persist();
 
    }
    public static void test_persist(){
        persist piecetable = new persist(path1,path,3);
        RandomAccessFile file = null;
        try {
            file = new RandomAccessFile(path,"rw");
            file.write("hellogoodbye".getBytes());
        }
        catch (IOException ex) {
            Logger.getLogger(PieceTable.class.getName()).log(Level.SEVERE, null, ex);
        }
        piecetable.add_original(12);
        System.out.println(piecetable._text_len);
        System.out.println(new String(piecetable.get_text()));
        try {
            file.seek(file.length());
            file.write("goodbye".getBytes());
        }
        catch (IOException ex) {
            Logger.getLogger(PieceTable.class.getName()).log(Level.SEVERE, null, ex);
        }
        piecetable.add(7,0);
        System.out.println(new String(piecetable.get_text()));
        piecetable.remove(piecetable._text_len-1, 1);
        System.out.println(new String(piecetable.get_text()));
        System.out.println(piecetable._text_len);
    }
 
}
