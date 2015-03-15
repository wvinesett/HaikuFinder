package haiku;

import java.util.HashMap;
import java.io.*;
import java.util.StringTokenizer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * <code>SyllableCounter</code> provides a dictionary of English words found <a href='http://www.gutenberg.org/ebooks/3204'>here<a>.
 * The dictionary is used as a lookup table to find the number of syllables in a word.  If the word is not contained in the dictionary,
 * a rudimentary means of finding the number of syllables is provided by this class.
 * The algorithm used is as follows. 
 * First, the number of vowels in the word are counted.  To this, add the number of 'y's that sound like vowels.  Next, subtract 
 * the number of silent vowels, and the number of diphthrongs and triphthrongs that are found in the word.  Finally, add 1 if
 * the word ends in "le" or "les".  
 * <br><br>
 * This class provides two methods: <code>setWord(String)</code> and <code>countSyllables()</code>.  To use the latter,
 * the former method must first be invoked.  If <code>setWord(String)</code> is never invoked, <code>countSyllables()</code>
 * returns 0.
 * 
 * @author William M. Vinesett
 */
public class SyllableCounter {
	private String word = "";
	private static final HashMap<String, Integer> DICTIONARY = new HashMap<>(42443);
	static {
		if (DICTIONARY.size() == 0){
			loadDictionary();
		}
	}

	
	/**
	 * Sets the word contained by this <code>SyllableCounter</code> instance.
	 * @param word The string whose syllables will be counted on the next invocation of <code>countSyllables()</code>.
	 * @since Version 1.0
	 */
	public void setWord(String word){
		if (word != null){
			this.word = stripPunctuation(word.toLowerCase());
		}
	}
	
	@Deprecated
	/**
	 * Returns the number of syllables in the word this <code>SyllableCounter</code> contains.  Syllables
	 * are counted by determining the number of all-vowel substrings in the word being checked.  
	 * @return The number of syllables in the word contained by this <code>SyllableCounter</code>.
	 * @deprecated Replaced by the more correct & robust algorithm used by <code>countSyllables()</code>.
	 * @since Version 1.0
	 */
	public int numSyllables(){
		int numTokens = new StringTokenizer(word, "bcdfghjklmnpqrstvwxyz-").countTokens();
		if (word.endsWith("e")){ // silent 'e'
			numTokens--;
		}
		if (word.endsWith("y") && numTokens == 0){ // 'y' is the only vowel in word
			numTokens++;
		}
		return numTokens;
	}	
	
	/**
	 * Counts and returns the number of syllables in this <code>SyllableCounter</code>'s current word.
	 * If the word is not found in the dictionary, the dictionary is checked to see if the word is a plural or past-tense
	 * form of a word in the dictionary.  If this check also fails, or if the dictionary could not be loaded,
	 * the syllable counting algorithm is used.  The word is then added to the dictionary.  The algorithm is described 
	 * in the description of the <code>SyllableCounter</code> class.
	 * @return The number of syllables found in this <code>SyllableCounter</code>'s word.
	 */
	public int countSyllables(){
		// check the dictionary for the word, or its singular or present-tense form
		if (DICTIONARY.containsKey(word)){
			return DICTIONARY.get(word);
		}
		if (word.length() >= 2){
			String root = word.substring(0, word.length() - 2);
			if (DICTIONARY.containsKey(root)){ 
				if (word.endsWith("ed")){
					return DICTIONARY.get(root);
				}
				else if (word.endsWith("es")){
					return DICTIONARY.get(root) + 1;
				}
			}
		}
	
		String root = word.substring(0, word.length() - 1);
		if (DICTIONARY.containsKey(root) && word.endsWith("s")){
			return DICTIONARY.get(root);
		}
		
		String original = new String(word);
//		http://www.howmanysyllables.com/howtocountsyllables.html
		int num = 0;
		// step 1: count the number of vowels
		for(int i = 0; i < word.length(); i++){
			if (isVowel(word.charAt(i))){
				num++;
			}
		}
		// step 2: add the number of 'y's that sound like vowels
		if ((num == 0 && word.contains("y")) || word.endsWith("y")) // 'y' y is a vowel
			num++;
		// step 3: subtract the number of silent vowels
		if (word.endsWith("e") && num != 0)	// 'e' is silent
			num--;
		// step 4: subtract 1 for each diphthrong or triphtrong 
		num -= removeDiphthrongs();
		num -= removeTriphthrongs();
		// step 5: add 1 if the word ends in "le" or "les"
		if (word.matches("[a-z]+les?"))
			num++;
		DICTIONARY.put(original, num);
		return num;
	}
	
	/**
	 * Uses regular expressions to match and remove triphthrongs from this <code>SyllableCounter</code>'s current word.
	 * A diphthrong is a string of two vowels that produces the sound of one vowel.  Use of this method causes this
	 * <code>SyllableCounter</code>'s current word to change.  That is, the word gets shorter each time a triphthrong is matched
	 * because it is removed.
	 * @return The number of triphthrongs that were found and removed from the current word.
	 * @since Version 2.0
	 */
	private int removeTriphthrongs() {
//		http://colasula.com/?p=530
		int count = 0;
		String[] triphthrongs = {"aye", "i[^aeiou]e", "oya", "ay", "owe"};
		for(String regex : triphthrongs){
			count += removeSubstring(Pattern.compile(regex)) ? 1 : 0;
		}
		return count;
	}


	/**
	 * Uses regular expressions to match and remove diphthrongs from this <code>SyllableCounter</code>'s current word.
	 * A diphthrong is a string of two vowels that produces the sound of one vowel.  Use of this method causes this
	 * <code>SyllableCounter</code>'s current word to change.  That is, the word gets shorter each time a diphthrong is matched
	 * because it is removed.
	 * @return The number of diphthrongs that were found and removed from the current word.
	 * @since Version 2.0
	 */
	private int removeDiphthrongs(){
//		http://www.english-for-students.com/images/Diphthongs.jpg
//		http://stackoverflow.com/questions/10761501/remove-part-of-string-following-regex-match-in-java
		int count = 0;
		String[] diphthrongs = {"(ea|ee)","(ai|ei|a[^aeiou]e)", "(ou|oo|u[^aeiou]e)", "ay", 
				"(igh|ie|[aeiou]y[aeiou])", "(oi|oy)", "(ai|ei|a[^aeiou]e)", "ou"};
		for(String regex : diphthrongs){
			count += removeSubstring(Pattern.compile(regex)) ? 1 : 0;
		}
		return count;
	}
	
	/**
	 * Alters this <code>SyllableCounter</code>'s current word by removing the substring
	 * that matches the <code>Pattern</code> provided as a parameter.
	 * @param p A <code>Pattern</code> compiled using a regex that matches a diphthrong or triphthrong.
	 * @return <code>true</code> if <code>p</code> was found and removed from the current word.
	 * @since Version 2.0
	 */
	private boolean removeSubstring(Pattern p){
		Matcher m = p.matcher(word);
		if (m.find()){
			StringBuilder sb = new StringBuilder(word.substring(0, m.start()));
			sb.append(word.substring(m.end(), word.length()));
			word = sb.toString();
			return true;
		}
		return false;
	}
	
	/**
	 * Removes periods, apostrophes, and other punctuation from parameter <code>s</code>.
	 * @param s The word to be removed of punctuation characters.
	 * @return A copy of <code>s</code> with punctuation characters removed.
	 * @since Version 2.1
	 */
	private static String stripPunctuation(String s){
		StringBuilder stripped = new StringBuilder();
		for(int i = 0; i < s.length(); i++){
			if (!isPunctuationCharacter(s.charAt(i))){
				stripped.append(s.charAt(i));
			}
		}
		return stripped.toString();
	}
	
	/**
	 * Checks if parameter <code>c</code> is a punctuation character.
	 * @param c The character to check.
	 * @return <code>true</code> if <code>c</code> is an exclamation point, period, question mark, comma, colon, semicolon, 
	 * apostrophe, or quotation mark.
	 */
	private static boolean isPunctuationCharacter(char c){
		return c == '!' || c == '.' || c == '?' || c == ',' || c == ':' || c == ';' || c == '"' || c == '\''; 
	}
	
	/**
	 * Returns whether or not parameter <code>c</code> is a vowel.
	 * @param c The character to check.
	 * @return <code>true</code> if <code>c</code> is 'a', 'e', 'i', 'o', or 'u'.
	 * @since Version 2.0
	 */
	private static boolean isVowel(char c){
		return c == 'a' || c == 'e' || c == 'i' || c == 'o' || c == 'u';
	}
	
	/**
	 * Reads the dictionary file into a lookup table implemented as a HashMap.  The table maps each English word in the dictionary
	 * to its number of syllables.
	 * @since Version 2.1
	 */
	private static void loadDictionary(){
		try(BufferedReader br = new BufferedReader(new FileReader("bin/haiku/dictionary.txt"))){
			String line = "";
			while ((line = br.readLine()) != null){
				StringTokenizer st = new StringTokenizer(line, "=");
				String word = st.nextToken();
				String hyph = st.nextToken();
				int syllables = new StringTokenizer(hyph, "-").countTokens();
				DICTIONARY.put(word, syllables);
			}
		}
		catch (FileNotFoundException fnfe){
			System.err.println("Could not find the dictionary.");
		}
		catch (IOException ioe){
			System.err.println("Could not load the dictionary.");
		}
	}
}