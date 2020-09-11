/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package javaapplication7;

/**
 *
 * @author Zizo
 */
public class Operations {
    public String _mnemonic;
    public String _opcode;
    public String _format;

    public Operations() {
    }
    
    public Operations(String mnemonic, String format, String opcode) {
        _mnemonic = mnemonic;
        _opcode = opcode;
        _format = format;
    }
    
}
