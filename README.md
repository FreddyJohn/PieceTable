# PieceTable

This is a Java implementation of the sequence data structure known as a PieceTable
inspired from -> https://github.com/saiguy3/piece_table and without a Java implementation I created my own.

The only difference is that the "original" and the "added" buffers are stored externally in the same location
it has also been modified to work with any arbitrary byte streams. My intuition for the PieceTable is
that it is easier to reorganize a description of the data than the actual data. We always append the data to the end
of the buffer and do the expensive splice operation only on the description. 
Even though dynamic set operations add, find, and remove are linear time it is sufficiently negligible because
they potentially operate on exponentially smaller amounts of bytes to complete in comparsion to actual data.



here are some more great resources on sequence data structures!

The Craft of Text Editing -> http://www.finseth.com/craft/
Data Structures for Text Editors 1998 -> https://www.cs.unm.edu/~crowley/papers/sds.pdf
A Fast Data Structure for Disk-Based Audio Editing -> https://www.cs.cmu.edu/~rbd/papers/audacity-icmc2001.pdf



