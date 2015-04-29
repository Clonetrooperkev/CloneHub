import java.io.*; //Libraries
import java.util.Scanner;
import java.util.regex.*;

//Kevin Friel 
//0881401 
//kevin.friel@my.und.edu

	class Lumiere { //Language Scanner
		
		public enum State
		{
		  PROGRAM, 
		  STMT_LIST,
		  STMT,
		  STMTID,
		  STMTREAD,
		  EXPR, 
		  TERM_TAIL,
		  TERM,
		  FACT_TAIL,
		  FACTOR,
		  ADD_OP,
		  MULT_OP
		};
		public enum TokenTypes
		{
			READ,
			WRITE,
			NUMBER,
			ADDOP,
			MULTOP,
			ASSIGN,
			ID,
			LPAREN,
			RPAREN,
			DONE
			
		}
		
		
		static String XVar = "read" ;
		static String YVar = "write";
		static String ZVar = "(?:\\d*\\.)?\\d+";
		static String addopstring = "(\\+|-)";
		static String multopstring = "(\\*|/|%|//)";
		static String assignstring = ":=";
		static String idstring = "(-?[a-zA-Z][a-zA-Z0-9]*)";
		static String lparenstring = "\\(";
		static String rparenstring = "\\)";
		static String patternstring = (readstring+ "|"+
				writestring+ "|"+
				numstring+ "|"+
				addopstring+ "|"+
				multopstring+ "|"+
				assignstring+ "|"+
				idstring+ "|"+
				lparenstring+ "|"+
				rparenstring); 
		static String [] tokenarray = new String[2000];
		static int tokenarrayindex = 0;
		static int end = 0;
		static int tk;
		static boolean endoffile = false;
		static String currenttoken;
		private static Exception error() {
			if(tk < tokenarrayindex){
				System.out.println("Grammar error, invalid text: " + (tokenarray[tk].substring(tokenarray[tk].lastIndexOf(",")+1)) );
			}
			else{
				System.out.println("Grammar error at end of of file");
			}
			System.exit(0);
			return null;
			
		}	
		private static Exception Lexerror() {
			System.out.println("Lexical error, invalid text: " + (tokenarray[tk].substring(tokenarray[tk].lastIndexOf(",")+1)));
			System.exit(0);
			return null;
		}	
		//Create the predict sets
		private static void getNextToken() throws Exception{
			if(tk < tokenarrayindex){
				currenttoken = (tokenarray[tk].substring(0, tokenarray[tk].lastIndexOf(",")));
				if(currenttoken.equals("<error>"))
				{
					throw Lexerror();
				}
				//System.out.println(currenttoken);		//Debugging
				tk++;					
			}
			else
			{
				currenttoken = "<e>";
				endoffile = true;
			}
		
		}
/*
		  	#Grammar
		 	program -> stmt_list $$$
			stmt_list -> stmt stmt_list | e
			stmt -> id := expr | read id | write expr
			expr -> term term_tail
			term_tail -> add op term term_tail | e
			term -> factor fact_tail
			fact_tail -> mult_op fact fact_tail | e
			factor -> ( expr ) | <id> | <number>
			add_op -> + | -
			mult_op -> * | /
*/
		
		
/*#	Derived Predict Set
1	program ? stmt_list $$$				e, id, read, write
2	stmt_list ? stmt stmt_list			id, read, write
3	stmt_list ? e						e
4	stmt ? id := expr					id
5	stmt ? read id						read
6	stmt ? write expr					write
7	expr ? term term_tail				(, <id>, <number>
8	term_tail ? add op term term_tail	add
9	term_tail ? e						e
10	term ? factor fact_tail				(, <id>, <number>
11	fact_tail ? mult_op fact fact_tail	*, /
12	fact_tail ? e						e
13	factor ? ( expr )					(
14	factor ? <id>						<id>
15	factor ? <number>					<number>
16	add_op ? +							+
17	add_op ? -							-
18	mult_op ? *							*
19	mult_op ? /							/
*/
		//These functions implement the derived predict sets
		   private static void program()throws Exception{ 
			   getNextToken();							//Get next token from scanner		
  		 		if (currenttoken.equals("<read>") 		//Looks for these tokens
   		 				||currenttoken.equals("<write>")
   		 				||currenttoken.equals("<e>")
   		 				||currenttoken.equals("<id>"))			
   		 		{
				    System.err.println("program --> stmt_list $$");
				    stmt_list(); //Goes to stmnt_list function
				    if (currenttoken.equals("<e>")){ 		//Look for end of file
						System.err.println("stmt_list --> e");				    	
				    	return;
				    }
				   else
					   throw error();	//If no end of file, exit with error
				}
  		 		else
  		 			throw error(); //If no legal match, exit with error
			    }

				private static void stmt_list() throws Exception{
					if (currenttoken.equals("<read>")		//Look for these tokens
	   		 				||currenttoken.equals("<write>")
	   		 				||currenttoken.equals("<id>"))
	   		 		{
						System.err.println("stmt_list --> stmt stmt_list");
						stmt();								//Go to these functions
						stmt_list();
						return;
	   		 		}
					if(currenttoken.equals("<e>"))
					{ 
						System.err.println("stmt_list --> e");
						return;
					}
					throw error();	  		 		
				}

			    private static void stmt() throws Exception{
			    	if (currenttoken.equals("<id>")){			//Checks to see
			    		
			    		System.err.println("stmt --> ID := expr");
			    		getNextToken();							//Gets the next token
				    	if (currenttoken.equals("<assign>")){	//If it's what we're looking for
				    		getNextToken();						//Get the next token
				    		expr();								//Go to expr
				    		return;								//Return
				    	}
				    	else
				    		throw error();					
			    	}
			    	if (currenttoken.equals("<read>")){				//Checks to see
			    		System.err.println( "stmt --> READ ID" );
			    		getNextToken();								//Gets the next token
			    		if (currenttoken.equals("<id>")){			//If it's what its looking for
				    		getNextToken();							//Get the next token
			    			return;									//Return
			    		}
			    		else
			    			throw error();
			    	}
			    	if (currenttoken.equals("<write>")){			//Checks to see
			    		System.err.println("stmt --> WRITE expr");
			    		getNextToken();								//Get the next token
			    		expr();										//Go to expr
			    		return;										//return
			    	}
				    throw error();
				}

			    private static void expr()throws Exception{
					if (currenttoken.equals("<id>")						//Look for these tokens
	   		 				||currenttoken.equals("<number>")
	   		 				||currenttoken.equals("<lparen>"))
	   		 		{
						System.err.println("expr --> term term_tail");
				    	term();											//Go to term
				    	term_tail();									//Go to term
				    	return;
	   		 		}
				    throw error();
				}
			    

			    private static void term_tail()throws Exception{
					if (currenttoken.equals("<addop>"))				//Looks for addop
					{
						System.err.println("term_tail --> add_op term term_tail");
						add_op();									//Go to addop
						term();										//Go to term
						term_tail();								//Go to term tail
						return;
					}
					if (currenttoken.equals("<rparen>")				//Looks for any of these cases
	   		 				||currenttoken.equals("<id>")
	   		 				||currenttoken.equals("<read>")
	   		 				||currenttoken.equals("<write>")
   		 					||currenttoken.equals("<e>")) 				
	   		 		{
						System.err.println("term_tail --> e");
						return;
	   		 		}

				    throw error();
				}

			    private static void term() throws Exception{
					if (currenttoken.equals("<id>")					//Looks for these cases
	   		 				||currenttoken.equals("<number>")
	   		 				||currenttoken.equals("<lparen>"))
					{
						System.err.println("term --> factor factor_tail");
						factor();									//Go to factor
						factor_tail();								//Go to factor tail
						return;
					}
				    throw error();
				}
			    

			    private static void factor_tail() throws Exception {
					if (currenttoken.equals("<multop>"))			//Looks for these cases
					{				    
						System.err.println("factor_tail --> mult_op factor factor_tail");
						mult_op();									//Go to multop
						factor();									//Go to factor
						factor_tail();								//Go to factor_tao;
						return;
					}
					if (currenttoken.equals("<addop>")
	   		 				||currenttoken.equals("<rparen>")
	   		 				||currenttoken.equals("<id>")
	   		 				||currenttoken.equals("<read>")
	   		 				||currenttoken.equals("<write>")	   		 				
							||currenttoken.equals("<e>"))
					{
						System.err.println("factor_tail --> e");
						return;
					}
				    throw error();
				}
			    

			    private static void factor() throws Exception {
			    	if (currenttoken.equals("<id>"))					//If it's any of these cases
			    	{
			    		System.err.println( "factor --> id" );
			    		getNextToken();									//Get the next token
			    		return;
			    	}
			    	if (currenttoken.equals("<number>"))
			    	{
			    		System.err.println( "factor --> number" );
			    		getNextToken();									//Get the next token
			    		return;
			    	}
			    	if (currenttoken.equals("<lparen>"))
			    	{
			    		System.err.println( "factor --> lparen" );
			    		getNextToken();									//Get the next token
					    expr(); 										//Go to expr
				    	if (currenttoken.equals("<rparen>"))
				    	{
						    getNextToken();								//Get the next token
				    		return;
				    	}
				    	throw error();
			    	}
				    throw error();
				}

			    private static void add_op() throws Exception{
			    	if (currenttoken.equals("<addop>"))				//If it's addop
			    	{
			    		System.err.println( "addop --> +/-" );
			    		getNextToken();								//Go to next token
			    		return;
			    	}
				    throw error();
				}

			    private static void mult_op() throws Exception {
			    	if (currenttoken.equals("<multop>"))				//If it's multop
			    	{
			    		System.err.println("multop --> *|/");
			    		getNextToken();									//Go to next token
			    		return;
			    	}
				    throw error();
				}	
		public static void main(String args[]) throws Exception { 

		OutputStream output = new FileOutputStream("/dev/null");			//Used for debugging
			PrintStream nullOut = new PrintStream(output);
			System.setErr(nullOut);	
			
			FileInputStream file; //Declares input file
			
			if (args.length >= 1){//Look for file name as first argument
				
				try { 
					file = new FileInputStream(args[0]); // Opens file 
					} 
					catch(FileNotFoundException e) { //Case for when a file isn't found
					System.out.println("File Not Found"); 
					return; 
					} 
				}
				else { 
					System.out.println("No Input File Specified."); 
					return; 
				} //Case for when no input file was specified
			
			Scanner scanner = new Scanner(file); // Starts scanner
			 
            while (scanner.hasNextLine()) { // While a line exists.
            	end = 0;
                String line = scanner.nextLine(); //Load the line
                //System.out.println(line);
                Pattern pattern = Pattern.compile(patternstring); //Looking for tokens
                Matcher matcher = pattern.matcher(line); //Match that in the loaded line
                
                while (matcher.find()) { //Step through all tokens from this line
                	 String TestSTR = (line.substring(end,matcher.start()));
                	// System.out.println(matcher.start());
                	 end = matcher.end();
                	 if (TestSTR.trim().length() > 0) {
                		 //System.out.println("<error>, " + TestSTR);
                		 tokenarray[tokenarrayindex++] = ("<error>, " + TestSTR);

                    }
                	 Pattern readpattern = Pattern.compile(readstring); //Look for read exactly
                     Matcher readmatcher = readpattern.matcher(matcher.group(0)); //Match that in the current token
                     if (readmatcher.find()) {//If there was a match
                       //System.out.println("<read>, " + readmatcher.group(0)); //Print it
                         tokenarray[tokenarrayindex++] = ("<read>, " + readmatcher.group(0));

                       continue; // Go to next token in line
                     }
                     Pattern writepattern = Pattern.compile(writestring); //Look for write exactly
                     Matcher writematcher = writepattern.matcher(matcher.group(0));
                     if (writematcher.find()) {
                     //  System.out.println("<write>, " + writematcher.group(0));
                       tokenarray[tokenarrayindex++] = ("<write>, " + writematcher.group(0));
                       continue;
                     }                        
                  Pattern numpattern = Pattern.compile(numstring); //Look for any integer or floating point number
                  Matcher nummatcher = numpattern.matcher(matcher.group(0));
                  if (nummatcher.find()) {
                  //  System.out.println("<number>, " + nummatcher.group(0));
                	  if(nummatcher.start() == 0){ //Make sure it's a number
                          tokenarray[tokenarrayindex++] = ("<number>, " + nummatcher.group(0));
                          continue;               		  
                	  }

                  }
                  Pattern addoppattern = Pattern.compile(addopstring); // Look for plus sign or minus sign
                  Matcher addopmatcher = addoppattern.matcher(matcher.group(0));
                  if (addopmatcher.find()) {
                  //  System.out.println("<addop>, " + addopmatcher.group(0));
                    tokenarray[tokenarrayindex++] = ("<addop>, " + addopmatcher.group(0));
                    continue;
                  } 
                  Pattern multoppattern = Pattern.compile(multopstring); //Look for multiplier op
                  Matcher multopmatcher = multoppattern.matcher(matcher.group(0));
                  if (multopmatcher.find()) {
                 //   System.out.println("<multop>, " + multopmatcher.group(0));
                    tokenarray[tokenarrayindex++] = ("<multop>, " + multopmatcher.group(0));
                    continue;
                  }
                  Pattern assignpattern = Pattern.compile(assignstring); //Look for assign op
                  Matcher assignmatcher = assignpattern.matcher(matcher.group(0));
                  if (assignmatcher.find()) {
                  //  System.out.println("<assign>, " + assignmatcher.group(0));
                    tokenarray[tokenarrayindex++] = ("<assign>, " + assignmatcher.group(0));
                    continue;
                  }
                  Pattern idpattern = Pattern.compile(idstring); //Look for alphanumeric strings
                  Matcher idmatcher = idpattern.matcher(matcher.group(0));
                  if (idmatcher.find()) {
                   // System.out.println("<id>, " + idmatcher.group(0));
                    tokenarray[tokenarrayindex++] = ("<id>, " + idmatcher.group(0));
                    continue;
                  }
                  Pattern lparenpattern = Pattern.compile(lparenstring); //Look for a left parenthesis 
                  Matcher lparenmatcher = lparenpattern.matcher(matcher.group(0));
                  if (lparenmatcher.find()) {
                    //System.out.println("<lparen>, " + lparenmatcher.group(0));
                    tokenarray[tokenarrayindex++] = ("<lparen>, " + lparenmatcher.group(0));
                    continue;
                  }
                  Pattern rparenpattern = Pattern.compile(rparenstring); //Look for a right parenthesis
                  Matcher rparenmatcher = rparenpattern.matcher(matcher.group(0));
                  if (rparenmatcher.find()) {
                    //System.out.println("<rparen>, " + rparenmatcher.group(0));
                    tokenarray[tokenarrayindex++] = ("<rparen>, " + rparenmatcher.group(0));
                    continue;
                  }
                }
                if (end != line.length()){
                	String TestSTR = (line.substring(end,line.length()));
               	 	if (TestSTR.trim().length() > 0) {
               	 		//System.out.println("<error>, " + TestSTR);
               	 		tokenarray[tokenarrayindex++] = ("<error>, " + TestSTR);
               	 	} 	
                }          
            }
            scanner.close(); //When finished, close the scanner
			file.close(); //And then close the file	
/*
			for (int i = 0; i < tokenarrayindex; i++){			//Used for debugging
				System.out.println(tokenarray[i]);
			}
*/
			program();	//Calls program
			System.out.println("Program is correct");
		}
	}
		
//Done
