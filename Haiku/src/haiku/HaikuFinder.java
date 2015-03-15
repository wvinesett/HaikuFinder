package haiku;
import java.io.*;
import java.util.*;

/**
 * An instance of this class loads a text file, whose path is provided to its sole constructor, into memory and
 * traverses the structure of words to find haikus.  Discovered haikus are printed to standard output.   
 * 
 * @author William M. Vinesett
 */
public class HaikuFinder {
	private List<String> words = new ArrayList<String>();
	private SyllableCounter counter = new SyllableCounter();

	public static void main(String[] args) {
		new HaikuFinder("bin/haiku/kant.txt");
	}
	
	/**
	 * Constructs a <code>HaikuFinder</code> object.  Words from a plain text file are loaded into a collection of strings
	 * and the collection is traversed.  Haikus are printed to standard output as they are encountered. 
	 * @param path The file path of a plain text file, whose contents are scanned for haikus.  
	 * @since Version 1.0
	 */
	public HaikuFinder(String path){
		try (BufferedReader input = new BufferedReader(new FileReader(path))){
			loadFile(input);
		}
		catch (FileNotFoundException fnfe){
			System.err.println("File not found.");
			fnfe.printStackTrace();
		}
		catch (IOException ioe){
			System.err.println("IO error.");
			ioe.printStackTrace();
		}
		findHaikus();
	}
	
	/**
	 * Loads the contents of a text file into a list.  Called by this class's constructor before the search for haikus
	 * begins.
	 * @param br The <code>BufferedReader</code> constructed using the text file provided to this class's constructor.
	 * @throws IOException if IO error occurs
	 * @since Version 1.0
	 */
	private void loadFile(BufferedReader br) throws IOException{
		String line = "";
		while ((line = br.readLine()) != null){
			StringTokenizer st = new StringTokenizer(line, " ");
			while (st.hasMoreTokens()){
				words.add(st.nextToken());
			}
		}
	}
	
	/**
	 * Traverses the list of words loaded in by the <code>loadFile(BufferedReader)</code> method looking for
	 * haikus.  If one is found, it is printed to the console.
	 * @since Version 1.0
	 */
	private void findHaikus(){
		int line1Count, line2Count, line3Count;
		int numHaikus = 0;
//		StringBuilder line1, line2, line3;
		for (int i = 0; i < words.size(); i++){
			if (! isWord(words.get(i))){
				continue;	
			}
			counter.setWord(words.get(i));
			int count = counter.countSyllables();
			if (count > 5){
				continue;	// haiku cannot start at words[i]
			}
			// reset the lines for this iteration
//			line1 = new StringBuilder();
//			line2 = new StringBuilder();
//			line3 = new StringBuilder();
//			
			line1Count = count;
//			line1.append(words.get(i)).append(" ");
			line2Count = line3Count = 0;
			// check if a haiku starts at words[i]
			for(int j = i + 1; j < words.size(); j++){
				if (! isWord(words.get(j))){
					break;
				}
				counter.setWord(words.get(j));
				count = counter.countSyllables();
				if (count + line1Count <= 5){ // fill line 1
					line1Count += count;
//					line1.append(words.get(j)).append(" ");
				}
				else if (count + line2Count <= 7 && line1Count == 5){ // fill line 2
					line2Count += count;
//					line2.append(words.get(j)).append(" ");
				}
				else if (count + line3Count <= 5 && line2Count == 7 && line1Count == 5){ // fill line 3
					line3Count += count;
//					line3.append(words.get(j)).append(" ");
				}
				else if (line1Count == 5 && line2Count == 7 && line3Count == 5){ // found haiku at words[i : k - 1]
//					System.out.println(line1);
//					System.out.println(line2);
//					System.out.println(line3);
					for (int k = i; k < j; k++){
						System.out.print(words.get(k) + " ");
					}
					System.out.println();
					numHaikus++;
					break;
				}
				else {
					break; // exceeded haiku syllable limits; go to the next iteration of the outer loop
				}
			}
		}
		System.out.printf("Found %d haikus.\n", numHaikus);
	}
	
	/**
	 * Checks if the provided string is a word.  A word is defined as a sequence of only letters.  Thus,
	 * if a word from a text file is formatted with other characters to denote bold, italics, etc in
	 * the original printed source, it will not be considered in the haiku check.  
	 * @param s The string to be checked.
	 * @return <code>true</code> if <code>s</code> is a word.
	 * @since Version 1.0
	 */
	private boolean isWord(String s){
		return s.matches("[a-zA-Z]+");
	}
	
}