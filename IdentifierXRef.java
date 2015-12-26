package identifierxref;

import java.util.*; //used to get the scanner object
import java.io.*; //used for output

public class IdentifierXRef
    {

        /*THIS PROGRAM CREATES A CROSS REFERENCE LIST OF IDENTIFIERS OF A JAVA PROGRAM*/

        static String simplifyLine(String line)
            { /* replace comments, literal strings, character constants, numeric constants 
                 opertors, punctuations, (i.e those characters not psrt of identifiers)
                 in "line" with blanks */ 
                line = deleteComments(line);
                line = deleteLiteralStrings(line);
                line = deleteCharacterConstants(line);
                return deleteNumericConstantsANDPunctuationsANDOperators(line);
            } //end simplifyLine
        
        static String deleteComments(String line)
            { /*REPLACE "/*....* / STYLE COMMENTS BY A SINGLE BLANK, AND REMOVE
              "//" STYLE COMMENTS */
                int i;
                while((i = line.indexOf("/*")) > -1) //while line contains "/*"), find position of the first "/*"
                    line = line.substring(0,i) + " " + line.substring(line.indexOf("*/")+2);  //find the position of the first "*/", replace the substring from /* to */ with space
                if ((i = line.indexOf("//")) > -1)//if(line contains "//")
                    line = line.substring(0,i); //get rid of // comment
                return line;
            } // end deleteComments
        
        static String deleteLiteralStrings(String line)
            {/*REPLACE LITERAL STRINGS "" WITH NOTHING*/
                int i;
                while((i = line.indexOf("\"")) > -1)//while line contains "\"")
                    {
                        String start = line.substring(0,i); //everything in line up to 1st "
                        String end = line.substring(i+1); // everything in line after 1st "
                        line = start + " " + end.substring(end.indexOf("\"")+1); //delete out " string literals
                    }
                return line;
            } // end deleteLiteralStrings
        
        static String deleteCharacterConstants(String line)
            {/*REPLACE CHARACTER CONSTANTS '' WITH NOTHING*/
                int i;
                while((i = line.indexOf("\'")) > -1)//while line contains a character constant)
                    line = line.substring(0,i) + " " + line.substring(i+3); //everything in line up to 1st ', everything in line after 1st ', delete out ' character constants
                return line;
            } // end deleteCharacterConstants
        
        static String deleteNumericConstantsANDPunctuationsANDOperators(String line)
            { /*REPLACES EACH NUMERIC CONSTANT BY A SINGLE BLANK*/
                final int NOT_IDENTIFIER = 0;
                final int IDENTIFIER = NOT_IDENTIFIER +1;
                String output = ""; //Starting this string as empty, will build output char by char one at a time
                int state = NOT_IDENTIFIER;
                for(int i = 0; i < line.length(); i++) //look at each character in the string
                    {
                        char c = line.charAt(i); //current input character to be processed
                        if (!Character.isLetterOrDigit(c) && c!='_' && c!='$')
                            c=' ';
                        switch(state)
                            {
                                case NOT_IDENTIFIER:
                                    output += Character.isDigit(c) ? " ":Character.toString(c); //adds to output either blank or current character depending on if character is a number
                                    if(Character.isLetter(c) || c=='_' || c=='$')
                                        state = IDENTIFIER;
                                    break;
                                case IDENTIFIER:
                                    output += Character.toString(c);
                                    if(c == ' ')
                                        state = NOT_IDENTIFIER;
                                    break;
                            }
                        
                    }
                return output;
            } // end deleteNumericConstantsANDPunctuationsANDOperators

        public static void main(String[] args) throws IOException 
            {
                //throws IOException will let the user know if the input/output fails
                
                Scanner inputFile = new Scanner (new File(args[0]+".java")); //the inputFile is the java program we want to feed into the cross reference
                BufferedWriter outputFile = new BufferedWriter (new FileWriter(args[args.length-1]+".xref")); //theoutputFile is the .xref file we create from the input
                Xref xRef = new Xref();
                for(int lineNumber=1; inputFile.hasNextLine();lineNumber++) //while the inputFile has another line...
                    {
                        String line = inputFile.nextLine();//use the scanner to wrap around the file
                        outputFile.write(lineNumber+"   "+line);//output the line with line number, pre-appended
                        outputFile.newLine(); //this will turn the line
                        StringTokenizer words = new StringTokenizer(simplifyLine(line)); //this will use the method simplifyLine to make the line only have identifiers
                        while (words.hasMoreTokens()) //for(each word in line)
                            xRef.add(words.nextToken(), lineNumber);  //add/update word to the xRef
                    }
                xRef.alphabetize(); //alphabetize the Xref
                xRef.output(outputFile); //output the Xref
                outputFile.close(); //you are supposed to close the file
            }//end main

    } //end IdentifierXRef class

class Xref
    {   
        /*STORES AND MANAGES IDENTIFIER DATA*/

        final int MAX_IDENTIFIERS = 1000; //maximum number of identifiers
        String[] identifierInformation = new String[MAX_IDENTIFIERS]; //storage space for identifier data
        int numberOfStoredWords; //current number of entries in database
        
        void add (String word, int lineNumber)
            {/*IF NOT A JAVA RESERVED WORD, INSERT word INTO THE DATABASE, INDICATING THAT IT IS REFERENCED ON LINE lineNumber */
                String[]javaReservedWord = {"abstract","assert","boolean","break","byte","case","catch","char","class","const",
                                            "continue","default","do","double","else","enum","extends","false","final","finally",
                                            "float","for","goto","if","implements","import","instanceof","int","interface","long",
                                            "native","new","null","package","private","protected","public","return","short",
                                            "static","strictfp","super","switch","synchronized","this","throw","throws","transient",
                                            "true","try","void","volatile","while",word}; //all java reserved words; word at end guarentees termination of for loop
                int i;
                for(i=0;!word.equals(javaReservedWord[i]);i++); //traverse javaReservedWord and see if any entries made match a word
                if(i< javaReservedWord.length-1) return; //matches a reserved word, so don't add database
                
                for(i=0;i < numberOfStoredWords+1;i++) //traverse the identifiers
                    if(word.equals(identifierInformation[i])) //see if any entries match the current word
                        identifierInformation[i] = identifierInformation[i] + ", " + Integer.toString(lineNumber); //word match, so add the line number it appears on
                    if(i==numberOfStoredWords) //no word match
                        {
                            identifierInformation[i] = word; //add the word
                            numberOfStoredWords++; //increment the counter
                        }
            } // end add

        void alphabetize()
            {/*BUBBLE SORT THE ARRAY IN ASCENDING ORDER*/
                String temp;
                for(int i = 1; i < identifierInformation.length; i++)
                    for(int j =0; j < identifierInformation.length - i; j++)
                        if(identifierInformation[j].compareToIgnoreCase(identifierInformation[j+1]) > 0 ) //compare the current word with the word after it in the array. 
                                                                                                          //If one is smaller than the other switch them (ascending)
                            {
                                temp = identifierInformation[j];
                                identifierInformation[j] = identifierInformation[j+1];
                                identifierInformation[j+1] = temp;        
                            }
            } // end alphabetize
        
        void output(BufferedWriter outputFile) throws IOException
            {/*WRITE ALL IDENTIFIERS TO THE .XREF FILE*/
                for(int i=0;i < numberOfStoredWords+1;i++) 
                    {
                        outputFile.write(identifierInformation[i]);
                        outputFile.newLine();
                    }
            } // end output

    } //end Xref Class
        