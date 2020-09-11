
package javaapplication7;

import com.sun.xml.internal.ws.util.StringUtils;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;
import java.util.Stack;
import javafx.scene.AccessibleAttribute;
import javafx.scene.input.Mnemonic;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

public class assembler {

    static HashMap<String, Operations> optab = new HashMap();
    static boolean base = false;
    static String baseaddress = "";
    static int errors;
    static int Base;
    static ArrayList<String> objcode = new ArrayList<>();
    HashMap<String, String> registers = new HashMap();
    HashMap<String, Integer> startaddress = new HashMap();
    static ArrayList<String> modficiation = new ArrayList<>();
    static HashMap<String, Symbol> SYMTAB = new HashMap();
    static String First;

    public void pass1() throws FileNotFoundException, IOException {
        FileWriter fw = new FileWriter("Intermediate file.txt", false);
        ArrayList<String> lines = new ArrayList();
        Scanner sc = new Scanner(new File("test.txt"));
        String mnemonic = "", label = "", operand = "", progname = "";
        int LOCCTR = 0, startingAddress = 0, i = 0, programLength = 0;
        String currentSection = "";
        Integer ORGLOCCTR = 0;
        boolean extended = false;
        boolean org = false;
        fillOPTAB();
        fillRegsiter();
        while (!mnemonic.equalsIgnoreCase("END")) {
            String line = sc.nextLine();
            if (line.isEmpty()) {
                continue;
            }
            if (isComment(line)) {
                continue;
            }
            lines.add(line);
            label = getLabel(lines.get(i));
            Integer OldLOCCTR = LOCCTR;
            //System.out.println(label);
            mnemonic = getOpcode(lines.get(i));
            if (mnemonic.charAt(0) == '+') {
                mnemonic = mnemonic.substring(1);
                extended = true;
                //System.out.println(mnemonic);
            } else {
                extended = false;
            }
            //System.out.println(mnemonic);
            operand = getOperand(lines.get(i));
            //System.out.println(operand);
            if (optab.containsKey(mnemonic)) {
                Operations op = optab.get(mnemonic);
                if (op._format != "1") {
                    if (mnemonic.compareTo("RSUB") != 0) {
                        if ("".equals(operand)) {
                            System.out.println("No Operand Found at line: " + (i + 1));
                            errors++;
                        }
                    }
                }
            }
            switch (mnemonic) {
                case "START":
                    LOCCTR = Integer.valueOf(operand, 16);
                    progname = label;
                    currentSection = label;
                    //System.out.println(LOCCTR);
                    startingAddress = LOCCTR;
                    break;
                case "WORD":
                    LOCCTR += 3;
                    writeIntermediate(mnemonic, operand, LOCCTR, extended, OldLOCCTR, currentSection);
                    break;
                case "BYTE":
                    String s = operand;
                    switch (s.charAt(0)) {
                        case 'C':
                            LOCCTR += (s.length() - 3);
                            writeIntermediate(mnemonic, operand, LOCCTR, extended, OldLOCCTR, currentSection);
                            break;
                        case 'X':
                            LOCCTR += Math.ceil((s.length() - 3) / 2);
                            writeIntermediate(mnemonic, operand, LOCCTR, extended, OldLOCCTR, currentSection);
                            // System.out.println("For X: " + LOCCTR);
                            break;
                    }
                    break;
                case "RESW":
                    if (!isNumeric(operand)) {
                        System.out.println("Operand at line " + (i + 1) + " must be a number");
                        errors++;
                    } else {

                        writeIntermediate(mnemonic, operand, LOCCTR, extended, OldLOCCTR, currentSection);
                        LOCCTR += 3 * Integer.parseInt(operand);
                    }
                    break;
                case "RESB":
                    if (!isNumeric(operand)) {
                        System.out.println("Operand at line " + (i + 1) + " must be a number");
                        errors++;
                    } else {

                        writeIntermediate(mnemonic, operand, LOCCTR, extended, OldLOCCTR, currentSection);
                        LOCCTR += Integer.parseInt(operand);
                    }
                    break;
                case "BASE":
                    base = true;
                    baseaddress = operand;
                    break;
                case "NOBASE":
                    base = false;
                    break;
                case "EQU":
                    if (isNumeric(operand)) {//constant value
                        writeIntermediate(mnemonic, operand, LOCCTR, extended, OldLOCCTR, currentSection);
                    } else if (operand.equals("*")) {
                        writeIntermediate(mnemonic, OldLOCCTR.toString(), LOCCTR, extended, OldLOCCTR, currentSection);
                    } else {
                        Integer value = evaluateExpression(operand);
                        writeIntermediate(mnemonic, value.toString(), LOCCTR, extended, OldLOCCTR, currentSection);
                    }
                    break;
                case "ORG":
                    if (isNumeric(operand)) {
                        ORGLOCCTR = Integer.valueOf(operand, 16);
                    } else if (operand.equals("*")) {
                        ORGLOCCTR = OldLOCCTR;
                    } else {
                        String Expression = operand;
                        ORGLOCCTR = evaluateExpression(Expression);
                    }
                    LOCCTR = ORGLOCCTR;
                    writeIntermediate(mnemonic, operand, LOCCTR, extended, OldLOCCTR, currentSection);
                    break;
                case "CSECT":
                    currentSection = label;
                    LOCCTR = 0;
                    break;
                case "EXTDEF":
                    break;
                case "EXTREF":
                    break;
                case "END":
                    writeIntermediate(mnemonic, operand, LOCCTR, extended, OldLOCCTR, currentSection);
                    break;
                default:
                    if (optab.containsKey(mnemonic)) {
                        Operations op = optab.get(mnemonic);
                        String format = op._format;
                        switch (format) {
                            case "1":
                                LOCCTR += 1;
                                writeIntermediate(mnemonic, operand, LOCCTR, extended, OldLOCCTR, currentSection);
                                break;
                            case "2":
                                LOCCTR += 2;
                                writeIntermediate(mnemonic, operand, LOCCTR, extended, OldLOCCTR, currentSection);
                                break;
                            case "3/4":
                                if (extended) {
                                    LOCCTR += 4;
                                    writeIntermediate(mnemonic, operand, LOCCTR, extended, OldLOCCTR, currentSection);
                                } else {
                                    LOCCTR += 3;
                                    writeIntermediate(mnemonic, operand, LOCCTR, extended, OldLOCCTR, currentSection);
                                }
                                break;
                        }

                    } else if (mnemonic == "z") {
                        System.out.println("Length At Line: " + (i + 1) + " Exceeded");
                        errors++;
                    } else if (!optab.containsKey(mnemonic) && !mnemonic.contains("END")) {
                        System.out.println("Opcode " + mnemonic + " At Line " + (i + 1) + " is incorrect");
                        errors++;
                    }
                //System.out.println("LOCCTR=" + LOCCTR);                //System.out.println("LOCCTR=" + LOCCTR);                //System.out.println("LOCCTR=" + LOCCTR);                //System.out.println("LOCCTR=" + LOCCTR);
            }
            if (label != null) {
                if (SYMTAB.containsKey(label)) {
                    System.out.println("ERROR!! Duplicate Label: " + label + " " + "At Line: " + (i + 1));
                    errors++;
                } else if ("EQU".equals(mnemonic)) {
                    if (isNumeric(operand)) {//constant value
                        SYMTAB.put(label, new Symbol(currentSection,label, 'A', Integer.parseInt(operand)));
                    } else if (operand.equals("*")) {
                        SYMTAB.put(label, new Symbol(currentSection,label, 'A', OldLOCCTR));
                    } else {
                        int value = evaluateExpression(operand);
                        SYMTAB.put(label, new Symbol(currentSection,label, 'A', value));
                    }
                    //System.out.println("Here: "+SYMTAB.get(label.trim()));
                } else {
                    SYMTAB.put(label.trim(), new Symbol(currentSection,label.trim(), 'R', OldLOCCTR));
                }
            }
            //System.out.println("Base: " + base);
            i++;
            if (!sc.hasNextLine() && !mnemonic.equalsIgnoreCase("END")) {
                System.out.println("No END Found");
                FileWriter fw3 = new FileWriter("Intermediate file.txt", false);
                errors++;
                System.out.println("Program Terminated With " + errors + " Errors");
                System.exit(errors);
            }
        }
        checkErrors();
        if (baseaddress != "") {
            Base = SYMTAB.get(baseaddress).value;
        }

        pass2();
        String StartingAddress = Integer.toHexString(startingAddress).toUpperCase();
        programLength = LOCCTR - startingAddress;
        String ProgramLength = Integer.toHexString(programLength).toUpperCase();
        //System.out.println(programLength);
        writeHeader(progname, StartingAddress, ProgramLength);
    }

    public void pass2() throws FileNotFoundException, IOException {
        Scanner sc = new Scanner(new File("Intermediate file.txt"));
        String n = "", i = "", x = "", b = "", p = "", e = "";
        Integer BaseValue = 0;
        int disp = 0;
        int temp4 = 0;
        String displacement = "";
        ArrayList<String> objcode = new ArrayList<>();
        //HashMap<String, Integer> startaddress = new HashMap();
        while (sc.hasNextLine()) {
            String line = sc.nextLine();
            String[] codes = line.split(" ");
            String mnemonic = codes[0];
            String operand = codes[1];
            int pc = Integer.parseInt(codes[2]);
            String extended = codes[3];
            String Objcode = "";
            int OldLOCCTR = Integer.parseInt(codes[4]);
            String oldLOCCTR = Integer.toHexString(OldLOCCTR);
            //System.out.println("Opcode: "+mnemonic+" Label: "+label+" PC: "+pc);
            if (mnemonic.compareTo("BYTE") == 0) {
                String s = operand;
                char type = s.charAt(0);
                s = s.substring(2, s.length() - 1);
                switch (type) {
                    case 'C':
                        for (char ch : s.toCharArray()) {
                            Objcode += Integer.toHexString(ch).toUpperCase();
                        }
                        break;
                    case 'X':
                        Objcode = s;
                        break;
                }
                objcode.add(Objcode);
                continue;
            }
            if (mnemonic.compareTo("RESW") == 0) {
                objcode.add("break");
                continue;
            }
            if (mnemonic.compareTo("RESB") == 0) {
                objcode.add("break");
                continue;
            }
            if (mnemonic.compareTo("EQU") == 0) {
                objcode.add("break");
                continue;
            }
            if (mnemonic.compareTo("ORG") == 0) {
                objcode.add("break");
                continue;
            }
            if (mnemonic.compareTo("WORD") == 0) {
                if(isNumeric(operand)){
                int x2 = Integer.parseInt(operand);
                Objcode = Integer.toHexString(x2).toUpperCase();
                }else{
                    int x2 =evaluateExpression(operand);
                Objcode = Integer.toHexString(x2).toUpperCase();  
                }
                //System.out.println("Final Object Code: " + Objcode);
                objcode.add(Objcode);
                if (startaddress.containsKey(Objcode)) {
                    String temp23 = Objcode;
                    temp23 = "x" + temp23;
                    startaddress.put(temp23.trim(), OldLOCCTR);
                } else {
                    startaddress.put(Objcode.trim(), OldLOCCTR);
                }
                continue;
            }
            if (mnemonic.compareTo("END") == 0) {
                First = operand.trim();
                continue;
            }
            if (mnemonic.compareTo("WORD") == 0) {
                int y = Integer.parseInt(operand);
                Objcode += Integer.toHexString(y).toUpperCase();
                addZeroes(Objcode, 6);
                objcode.add(Objcode);
                continue;
            }
            if (mnemonic.equals("EXTDEF")) {
                String[] extdef = operand.split(",");
                ArrayList<Integer> address =new ArrayList<>();
                for (String ex : extdef) {
                    if (SYMTAB.containsKey(e)) {
                        SYMTAB.get(e).extdef = true;
                         address.add(SYMTAB.get(ex).value);
                    } else {
                        System.out.println("ERROR!! EXTDEF symbol undefined");
                        errors++;
                    }
                }
                writeDEF(extdef, address);
            }

            if (mnemonic.equals("EXTREF")) {
                String[] extref = operand.split(",");
                for (String ex : extref) {
                    if (SYMTAB.containsKey(e)) {
                        SYMTAB.get(e).extdef = true;
                    } else {
                        System.out.println("ERROR!! EXTDEF symbol undefined");
                        errors++;
                    }
                }
                writeREF(extref);
            }

            if (optab.containsKey(mnemonic)) {
                Operations op = optab.get(mnemonic);
                String Format = op._format;
                switch (Format) {
                    case "1":
                        Objcode = op._opcode;
                        //System.out.println("Object Code: " + Objcode);
                        break;
                    case "2":
                        Objcode = op._opcode;
                        if ((operand.contains(","))) {
                            String temp[] = operand.split(",");
                            String temp1 = temp[0];
                            String temp2 = temp[1];
                            if (registers.containsKey(temp1) && registers.containsKey(temp2)) {
                                String reg1 = (String) registers.get(temp1);
                                String reg2 = (String) registers.get(temp2);
                                Objcode += reg1 + reg2;
                                //System.out.println("Object Code: " + Objcode);
                            } else {
                                System.out.println("Wrong Register");
                            }
                        } else if (registers.containsKey(operand)) {
                            String temp3 = operand;
                            String reg1 = (String) registers.get(temp3);
                            Objcode += reg1 + "0";
                            //System.out.println("Object Code: " + Objcode);
                        } else {
                            System.out.println("Wrong register");
                        }
                        break;
                    case "3/4":
                        if (mnemonic.compareTo("RSUB") == 0) {
                            Objcode = "4F0000";
                            objcode.add(Objcode);
                            startaddress.put(Objcode, OldLOCCTR);
                            continue;
                        }
                        int temp = Integer.parseInt(op._opcode, 16);
                        Objcode = Integer.toBinaryString(temp);
                        Objcode = addZeroes(Objcode, 8);
                        Objcode = Objcode.substring(0, 6);
                        //System.out.println("opcode =" + Objcode);
                        if (operand.charAt(0) == '@') {
                            n = "1";
                            i = "0";
                            Objcode += n + i;
                            operand = operand.substring(1);
                        } else if (operand.charAt(0) == '#') {
                            n = "0";
                            i = "1";
                            Objcode += n + i;
                            operand = operand.substring(1);

                        } else {
                            n = "1";
                            i = "1";
                            Objcode += n + i;
                        }
                        if (operand.contains(",X")) {
                            x = "1";
                            Objcode += x;
                            operand = operand.substring(0, operand.indexOf(","));
                            //System.out.println("Operand: "+operand);
                        } else {
                            x = "0";
                            Objcode += x;
                        }
                        if (SYMTAB.containsKey(operand) || isNumeric(operand)) {
                            //System.out.println("operand   " + operand);

                            if (isNumeric(operand)) {
                                p = "0";
                                b = "0";
                                e = "0";
                                Objcode += b + p + e;
                                disp = Integer.parseInt(operand);
                                displacement = Integer.toHexString(disp).toUpperCase();
                                displacement = addZeroes(displacement, 3);
                                //objcode = 6-bit "opcode" + 6-bit "flags"
                                temp4 = Integer.parseInt(Objcode, 2);
                                String temp5 = Integer.toHexString(temp4).toUpperCase();
                                Objcode = temp5 + displacement;
                                Objcode = addZeroes(Objcode, 6);
                            }   else if (SYMTAB.get(operand).extref) {                                  
                                    p = "0";
                                    b = "0";
                                    e = "1";
                                    Objcode += b + p + e;
                                    temp4 = Integer.parseInt(Objcode, 2);
                                String temp5 = Integer.toHexString(temp4).toUpperCase();
                                    Objcode = temp5 + "00000";
                                }else {
                             
                                Integer TA = SYMTAB.get(operand.trim()).value;
                                //System.out.println("TA=" + TA);
                                if (extended.compareTo("false") == 0) {
                                    disp = TA - pc;
                                    //System.out.println("displacement in line   " + line + "=  " + disp);
                                    if (disp >= -2048 && disp <= 2047) {
                                        p = "1";
                                        b = "0";
                                        e = "0";
                                        Objcode += b + p + e;
                                        //System.out.println("pc");
                                    } else {
                                        if (base == false) {
                                            System.out.println("Must Use Base Directive "+line);
                                            errors++;
                                            checkErrors();
                                        }
                                        disp = TA - Base;
                                        //System.out.println("displacement in line base   " + line + "=" + disp);
                                        if (disp >= 0 && disp <= 4097) {
                                            p = "0";
                                            b = "1";
                                            e = "0";
                                            Objcode += b + p + e;
                                        }
                                    }
                                } else {
                                    disp = TA;
                                    p = "0";
                                    b = "0";
                                    e = "1";
                                    Objcode += b + p + e;
                                    if ("1".equals(i)) {
                                        createMod(OldLOCCTR);
                                    }
                                }
                                if (extended.compareTo("false") == 0) {
                                    if (disp < 0) {
                                        displacement = Integer.toBinaryString(disp);
                                        displacement = removeOnes(displacement);
                                        int disp2 = Integer.parseInt(displacement, 2);
                                        displacement = Integer.toHexString(disp2).toUpperCase();
                                        displacement = addZeroes(displacement, 3);
                                        //System.out.println("javaapplication7.assembler.pass2() " + displacement);
                                    } else {
                                        displacement = Integer.toHexString(disp).toUpperCase();
                                        displacement = addZeroes(displacement, 3);

                                    }
                                } else {
                                    displacement = Integer.toHexString(disp).toUpperCase();
                                    displacement = addZeroes(displacement, 5);
                                    //System.out.println("Format 4 disp  " + displacement);
                                }
                                temp4 = Integer.parseInt(Objcode, 2);
                                String temp5 = Integer.toHexString(temp4).toUpperCase();
                                Objcode = temp5 + displacement;
                                Objcode = addZeroes(Objcode, 6);
                                //System.out.println("For Opcode: " + mnemonic + " Objcode= " + Objcode + " Disp= " + disp);
                            }

                        }

                }

            }
            //System.out.println("Final Object Code: " + Objcode);
            objcode.add(Objcode);
            if (startaddress.containsKey(Objcode)) {
                String temp23 = Objcode;
                temp23 = "x" + temp23;
                startaddress.put(temp23.trim(), OldLOCCTR);
            } else {
                startaddress.put(Objcode.trim(), OldLOCCTR);
            }
        }
        System.out.println("javaapplication7.assembler.pass2()");
        splitText(objcode);
//        for (String string : objcode) {
//            System.out.println("Objcode "+objcode);
//        }
    }

    public String getLabel(String line) {
        if (line.charAt(0) == ' ') {
            return null;
        }
        String label = "";
        int i = 0;
        while (i < line.length() && line.charAt(i) != ' ') {
            label = label + line.charAt(i++);
        }
        if (i <= 8) {
            return label;
        }
        return "Length Exceeded";
    }

    public String getOpcode(String line) {
        String opcode = "";
        int i = 9;
//        if (line.charAt(9) == '+') {
//            
//            i++;
//        }
        while (i < line.length() && line.charAt(i) != ' ') {
            opcode = opcode + line.charAt(i++);
        }
        if (i <= 15) {
            return opcode;
        }
        return "z";
    }

    public String getOperand(String line) {
        String operand = "";
        int i = 17;
        while (i < line.length() && line.charAt(i) != ' ') {
            operand = operand + line.charAt(i++);
        }
        if (i <= 35) {
            return operand;
        }
        return "Length Exceeded";
    }

    public void fillOPTAB() throws FileNotFoundException {
        Scanner sc = new Scanner(new File("Insturction_Set.txt"));
        while (sc.hasNextLine()) {
            String line = sc.nextLine();
            String[] codes = line.split(" ");
            String mnemonic = codes[0];
            String opCode = codes[1];
            String format = codes[2];
            optab.put(mnemonic, new Operations(mnemonic, format, opCode));
        }
    }

    public void fillRegsiter() throws FileNotFoundException {
        Scanner sc = new Scanner(new File("Registers.txt"));
        while (sc.hasNextLine()) {
            String line = sc.nextLine();
            String[] codes = line.split(",");
            String register = codes[0];
            String number = codes[1];
            registers.put(register, number);
        }
    }

    public void writeIntermediate(String mnemonic, String Operand, int LOCCTR, boolean extended, int OldLOCCTR, String currentSection) throws IOException {
        BufferedReader br = new BufferedReader(new FileReader("Intermediate File.txt"));
        BufferedWriter writer = new BufferedWriter(new FileWriter("Intermediate File.txt", true));
        if (br.readLine() == null) {
            writer.write(mnemonic + " " + Operand + " " + LOCCTR + " " + extended + " " + OldLOCCTR + " " + currentSection);
            writer.close();
        } else {
            writer.append('\n');
            writer.append(mnemonic + " " + Operand + " " + LOCCTR + " " + extended + " " + OldLOCCTR + " " + currentSection);
            writer.close();
        }
    }

    public String addZeroes(String x, int n) {
        while (x.length() < n) {
            x = "0" + x;
        }
        return x;
    }

    public static boolean isNumeric(String str) {
        return str.matches("-?\\d+(\\.\\d+)?");  //match a number with optional '-' and decimal.
    }

    public void checkErrors() throws IOException {
        if (errors > 0) {
            System.out.println("Program Terminated With " + errors + " Errors");
            FileWriter fw2 = new FileWriter("Intermediate file.txt", false);
            System.exit(errors);
        }
    }

    public String removeOnes(String x) {
        char[] c = x.toCharArray();
        int j = x.length() - 1;
        String temp = "";
        int i = 12;
        while (i > 0) {
            temp = c[j--] + temp;
            i--;
        }
        return temp;
    }

    public void writeHeader(String Progname, String StartingAddress, String ProgLength) throws IOException {
        (new File("HTME.txt")).delete();
        BufferedWriter writer = new BufferedWriter(new FileWriter("HTME.txt", true));
        while (Progname.length() < 6) {
            Progname += " ";
        }
        StartingAddress = addZeroes(StartingAddress, 6);
        ProgLength = addZeroes(ProgLength, 6);
        writer.write("H" + Progname + StartingAddress + ProgLength);
        writer.close();
        writeText();
    }
        public void writeREF(String[] r) throws IOException {
   BufferedWriter writer = new BufferedWriter(new FileWriter("HTME.txt", true)); 
   String ref="";
   for(String s:r){
       ref+=s;
   }
    writer.append("/n"+"R"+ref);
     writer.close();
        writeEnd();
    }
       public void writeDEF(String []  defLabel ,ArrayList<Integer> defAddress) throws IOException {
      BufferedWriter writer = new BufferedWriter(new FileWriter("HTME.txt", true)); 
         String def="";
   for(int i=0 ; i<defLabel.length;i++){
       def+=defLabel[i];
       def+=defAddress.get(i).toString();
   }
   writer.append("/n"+"R"+def);
     writer.close();
        writeEnd();
    }

    public void splitText(ArrayList objcode) throws IOException {
        int counter = 0;
        String z = "";
        boolean flag = false;
        boolean flag2 = false;
        for (int i = 0; i < objcode.size(); i++) {
            String temp = (String) objcode.get(i);
            counter += temp.length();
            if (counter > 60) {
                flag = true;
            }
            if (temp.contains("break")) {
                if (z.length() > 0) {
                    assembler.objcode.add(z);
                }
                counter = 0;
                z = "";
                continue;
            }
            if (flag) {
                assembler.objcode.add(z);
                flag = false;
                counter = 0;
                z = "";
                i--;
            } else {
                String s = (String) objcode.get(i);
                z = z + s + ",";
            }
            if (i == objcode.size() - 1) {
                assembler.objcode.add(z);
            }
        }
        for (Object object : assembler.objcode) {
            System.out.println("javaapplication7.assembler.checkText() " + object);
        }
    }

    public void writeText() throws IOException {
        BufferedWriter writer = new BufferedWriter(new FileWriter("HTME.txt", true));
        for (int i = 0; i < assembler.objcode.size(); i++) {
            String text = assembler.objcode.get(i);
            String[] c = text.split(",");
            String find = c[0];
            int startadress;
            if (find.charAt(0) == 'x') {
                startadress = startaddress.get(find);
                find.substring(find.indexOf('x'));
            }
            String text1 = "";
            for (String c1 : c) {
                text1 += c1;
            }
            startadress = startaddress.get(find);
            String StartAdress = Integer.toHexString(startadress).toUpperCase();
            StartAdress = addZeroes(StartAdress, 6);
            System.out.println("javaapplication7.assembler.writeText()" + StartAdress);
            double x = text1.length() / 2.0;
            int length = (int) Math.ceil(x);
            String textlength = Integer.toHexString(length).toUpperCase();
            textlength = addZeroes(textlength, 2);
            writer.append('\n' + "T" + StartAdress + textlength + text1);
        }
        writer.close();
        writeModification();
    }

    public void writeModification() throws IOException {
        BufferedWriter writer = new BufferedWriter(new FileWriter("HTME.txt", true));
        for (int i = 0; i < modficiation.size(); i++) {
            String text = modficiation.get(i);
            writer.append('\n' + "M" + text + "05");
        }
        writer.close();
        writeEnd();
    }


    public void writeEnd() throws IOException {
        int x = SYMTAB.get(First).value;
        String c = Integer.toHexString(x);
        c = addZeroes(c, 6);
        BufferedWriter writer = new BufferedWriter(new FileWriter("HTME.txt", true));
        writer.append('\n' + "E" + c);
        writer.close();
    }

    public void createMod(int OldLOCCTR) {
        int address = OldLOCCTR + 1;
        String Address = Integer.toHexString(address).toUpperCase();
        Address = addZeroes(Address, 6);
        System.out.println("javaapplication7.assembler.createMod() " + Address);
        modficiation.add(Address);
    }

    private boolean isComment(String line) {
        if (line.contains(".")) {
            return true;
        }
        return false;
    }

    private int evaluateExpression(String expression) {
        String[] terms = expression.split("[// + */-]");
        String exp = "";
        int j = 0, i = 0;
        while (i < expression.length() - 1) {
            if (i < expression.length()) {
                if (expression.charAt(i) == '+' || expression.charAt(i) == '-' || expression.charAt(i) == '*' || expression.charAt(i) == '/') {
                    exp = exp + expression.charAt(i);
                    i++;
                } else {
                    if (isNumeric(terms[j])) {
                        exp = exp + terms[j];
                    } else if (SYMTAB.containsKey(terms[j])) {
                        exp = exp + SYMTAB.get(terms[j]).value;
                    } else {
                        System.out.println(" ERROR!! symbols used in expression MUST be previously defined");
                        errors++;
                        System.exit(errors);
                    }
                    i += terms[j].length();
                    j++;
                }
            }
        }
        //System.out.println(exp);
        int value = compute(exp);
        return value;
    }

    static int compute(String expression) {

        // Get JavaScript engine
        ScriptEngine engine = new ScriptEngineManager().getEngineByExtension("js");

        try {
            // Evaluate the expression
            Object result = engine.eval(expression);
            //System.out.println(expression + " = " + result);
            try {
                return Integer.parseInt(result.toString());
            } catch (NullPointerException e) {
                e.getMessage();
            }
        } catch (ScriptException e) {
            // Something went wrong
            e.printStackTrace();
        }
        return 0;
    }

    public void printSYMTAB() {
        for (String key : SYMTAB.keySet()) {
            System.out.println(key + "        " + SYMTAB.get(key).type + "        " + Integer.toHexString(SYMTAB.get(key).value));
        }
    }
}
