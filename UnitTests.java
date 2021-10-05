/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package piecetable;

import java.io.IOException;

import java.io.RandomAccessFile;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Nick
 * 
 * 
 */
     
public class UnitTests {
    private static final String editsBufferPath = "C:\\Users\\Nick\\Downloads\\audioResearch\\hello1.txt";
    private static final String objectPath = "C:\\Users\\Nick\\Downloads\\audioResearch\\hello.txt";
    private static final String editsPath = "C:\\Users\\Nick\\Downloads\\audioResearch\\hello3.txt";
    private static final String originalBufferPath = "C:\\Users\\Nick\\Downloads\\audioResearch\\hello2.txt";
    private static final String removeBufferPath = "C:\\Users\\Nick\\Downloads\\audioResearch\\hello96.txt";

    

    private static PieceTableAPI piecetable;
    private static RandomAccessFile edits;
    private static RandomAccessFile original;
    
    public static void main(String[] args) {

       intialize();
       test_add_remove();
       
       intialize();
       test_original_remove_add_find();
       rigourously_test_undo_redo(piecetable,edits);
       
       intialize();
       add_original_add_at_zero_add_add_at_zero();
       
       intialize();
       test_undo_redo_original_then_more_edits();

    }
    
    public static void intialize(){
        piecetable = new PieceTableAPI(objectPath,editsBufferPath,editsPath,originalBufferPath,removeBufferPath);
        edits = null;
        try {
            original = new RandomAccessFile(originalBufferPath,"rw");
            edits = new RandomAccessFile(editsBufferPath,"rw");
            edits.setLength(0);
            original.setLength(0);
        }
        catch (IOException ex) {
            Logger.getLogger(PieceTable.class.getName()).log(Level.SEVERE, null, ex);
        } 
    }
    
    public static boolean test_original_remove_add_find(){
        
        try {
            original.write("hellogoodbye".getBytes());
        }
        catch (IOException ex) {
            Logger.getLogger(PieceTable.class.getName()).log(Level.SEVERE, null, ex);
        }
        piecetable.add_original(12);
        assert new String(piecetable.get_text()).equals(new String(piecetable.find(0,piecetable.byte_length))) : "get_text() != find(0, length)";
        assert new String(piecetable.get_text()).equals("hellogoodbye"): "an edit sequence occured with an error";
        //"hellogoodbye"
        
        piecetable.remove(piecetable.byte_length-7, 3);
        assert new String(piecetable.get_text()).equals(new String(piecetable.find(0,piecetable.byte_length))) : "get_text() != find(0, length)";
        assert new String(piecetable.get_text()).equals("hellodbye"): "an edit sequence occured with an error";
        //"hellodbye"
        
        piecetable.undo();
        assert new String(piecetable.get_text()).equals(new String(piecetable.find(0,piecetable.byte_length))) : "get_text() != find(0, length)";
        assert new String(piecetable.get_text()).equals("hellogoodbye"): "an edit sequence occured with an error";
        //"hellogoodbye"

        piecetable.redo();
        assert new String(piecetable.get_text()).equals(new String(piecetable.find(0,piecetable.byte_length))) : "get_text() != find(0, length)";
        assert new String(piecetable.get_text()).equals("hellodbye"): "an edit sequence occured with an error";
        //"hellodbye"
        
        piecetable.undo();
        assert new String(piecetable.get_text()).equals(new String(piecetable.find(0,piecetable.byte_length))) : "get_text() != find(0, length)";
        assert new String(piecetable.get_text()).equals("hellogoodbye"): "an edit sequence occured with an error";
        //"hellogoodbye"
        
        try {
            edits.seek(edits.length());
            edits.write("adding I love programming".getBytes());
        }
        catch (IOException ex) {
            Logger.getLogger(PieceTable.class.getName()).log(Level.SEVERE, null, ex);
        }

        piecetable.add(25, (int) piecetable.byte_length);
        assert new String(piecetable.find(0,piecetable.byte_length)).equals(new String (piecetable.get_text())) : "get_text() != find(0, length)";
        assert new String(piecetable.get_text()).equals("hellogoodbyeadding I love programming"): "an edit sequence occured with an error";
        //"hellogoodbyeadding I love programming"
        
        piecetable.remove(piecetable.byte_length-7, 3);
        assert new String(piecetable.find(0,piecetable.byte_length)).equals(new String (piecetable.get_text())) : "get_text() != find(0, length)";
        assert new String(piecetable.get_text()).equals("hellogoodbyeadding I love progming"): "an edit sequence occured with an error";
        //"hellogoodbyeadding I love progming
