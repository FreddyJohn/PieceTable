# PieceTable

This is a Java implementation of the sequence data structure known as a PieceTable
inspired from -> https://github.com/saiguy3/piece_table and without a Java implementation I created my own.

The only difference is that the "original" and the "added" buffers are stored externally in the same location
it has also been modified to work with any arbitrary byte streams. My intuition for the PieceTable is
that it is easier to reorganize a description of the data than the actual data. We always append the data to the end
of the buffer and do the expensive splice operation only on the description. 
Even though dynamic set operations add and find are linear time it is sufficiently negligible because
they potentially operate on exponentially smaller amounts of bytes to complete.


for example we could use integer arrays with the PieceTable

String path = "yourfilePath";
pieceTable = new PieceTable(path);
RandomAccessFile file = new RandomAccessFile(path,"rw");
byte[] int_bytes = new byte[40]; // 4 bytes for each int
ByteBuffer.wrap(int_bytes).order(ByteOrder.LITTLE_ENDIAN).asIntBuffer().put(new int[]{1,2,3,4,5,6,7,8,9,10});
file.write(int_bytes);
pieceTable.add_original(40);
byte[] bytes = pieceTable.find(0, pieceTable._text_len);
int[] ints = new int[bytes.length/4];
ByteBuffer.wrap(bytes).order(ByteOrder.LITTLE_ENDIAN).asIntBuffer().get(ints);
for(int i: ints)
{
   System.out.println(i);
}

we can also use String

file.write("hello".getBytes());
pieceTable.add_original(5);
System.out.println(new String(pieceTable.find(0, pieceTable._text_len)));

output: "hello"

to make additions

for(int i=2;i<5;i++){
   file.seek(pieceTable._text_len);
   file.write("goodbye".getBytes());
   pieceTable.add("goodbye".length(),i);
}
System.out.println(new String(pieceTable.find(0, pieceTable._text_len)));

output: "hegggoodbyeoodbyeoodbyello"


here are some more great resources on sequence data structures!

The Craft of Text Editing -> http://www.finseth.com/craft/
Data Structures for Text Editors 1998 -> https://www.cs.unm.edu/~crowley/papers/sds.pdf
A Fast Data Structure for Disk-Based Audio Editing -> https://www.cs.cmu.edu/~rbd/papers/audacity-icmc2001.pdf



