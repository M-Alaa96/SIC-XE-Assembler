
package javaapplication7;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.lang.Object;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException; 

/**
 *
 * @author Zizo
 */
public class JavaApplication7 {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws FileNotFoundException, IOException {
        assembler x = new assembler();
            x.pass1(); 
            x.printSYMTAB();
    }
  
    }
 


