package piecetable;

/**
 *
 * @author Nick
 */
public class Edit{
    public int length;
    public int offset;
    public String editType;
    public Edit(int length, int offset, String editType){
        this.length = length;
        this.offset = offset;
        this.editType = editType;
    }
}
