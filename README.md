# SIC-XE-Assembler
The project is a 2-pass assembler for SIC/XE machine implementation, written in JAVA, producing code for the relocating loader used in the SIC/XE programming assignments.
# Specifications
1.  The pass1 is to execute by entering pass1 <source-file-name>
2.  The source file for the main program for this phase 
3.  A parser that is capable of handling source lines that are instructions, storage declaration, comments, and assembler directives 

**For instructions, the parser is to be minimally capable of decoding 1, 2, 3 and 4- byte instructions as follows:**

  * 1-byte (e.g. NORM)
  * 2-byte with 1 or 2 symbolic register reference (e.g., TIXR A, ADDR S,A)
  * RSUB (ignoring any operand or perhaps issuing a warning)
  * 3-byte PC-relative with symbolic operand to include immediate, indirect and indexed addressing
  * 3-byte absolute with non-symbolic operand to include immediate, indirect and indexed addressing
  * 4-byte absolute with symbolic or non-symbolic operand to include immediate, indirect, and indexed addressing.

The parser is to handle all storage directives (BYTE, WORD, RESW, and RESB), in addition to START and END directives.
Hexadecimal addresses that would begin with ‘A’ through ‘F’ must start with a leading ‘0’ to distinguish them from labels.
Instructions and assembler directives in the source program may be written using either uppercase or lowercase letters.
The source program to be assembled must be in fixed format as follows:
1. bytes 1–8 label
2. 9 blank
3. 10–15 operation code
4. 16–17 blank
5. 18–35 operand
6. 36–66 comment

If a source line contains “.” in the first byte, the entire line is treated as a comment

4. The output of this phase should contain (at least):
a) The symbol table.
b) The source program 
Example input
```
TERMPROJ START 3A0
.THIS IS A COMMENT LINE
LBL1 BYTE C'ABCDEF'
LBL2 RESB 4
LBL2 RESW 1
TOP LDA ZERO
LDX #INDEX
```
## Phase 2 implementation of Pass2 of the assembler. 
The output of this phase should contain the object code formatted as H T M E records where
* H stands for Header record.
* T stands for Text record.
* M stands for Modification record.
* E stands for End record.
