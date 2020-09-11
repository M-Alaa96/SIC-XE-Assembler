/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package javaapplication7;

/**
 *
 * @author RS
 */
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
