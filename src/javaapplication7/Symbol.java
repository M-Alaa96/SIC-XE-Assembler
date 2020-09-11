
package javaapplication7;

public class Symbol {
    int value;
    String label;
    char type;
    boolean extdef=false,extref=false;
    String section;

    public Symbol( String section,String label,char type,int value) {
        this.value = value;
        this.label = label;
        this.section= section;
        this.type = type;
    }

 
    
}
