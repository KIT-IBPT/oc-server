package org.scictrl.mp.orbitcorrect.accessories;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StreamTokenizer;
import java.net.MalformedURLException;
import java.net.URL;
/**
 * The <code>FileTokenizer</code> class takes an input stream and
 * parses it into "tokens", allowing the tokens to be
 * read one at a time. The parsing process is controlled by a table
 * and a number of flags that can be set to various states. The
 * stream tokenizer can recognize identifiers, numbers, quoted
 * strings, and various comment styles.
 * FileTokenizer uses StreamTokenizer and its methodes.
 *
 * @author igor@scictrl.com
 */
public class FileTokenizer {
	private URL url;
	private StreamTokenizer st;
	private Reader reader;
	private boolean eolIsSignificant=false;

/**
 * After a call to the <code>nextToken</code> method, this field
 * contains the type of the token just read. For a single character
 * token, its value is the single character, converted to an integer.
 * For a quoted string token (see , its value is the quote character.
 * Otherwise, its value is one of the following:
 * <ul>
 * <li><code>TT_WORD</code> indicates that the token is a word.
 * <li><code>TT_NUMBER</code> indicates that the token is a number.
 * <li><code>TT_EOL</code> indicates that the end of line has been read.
 *     The field can only have this value if the
 *     <code>eolIsSignificant</code> method has been called with the
 *     argument <code>true</code>.
 * <li><code>TT_EOF</code> indicates that the end of the input stream
 *     has been reached.
 * </ul>
 *
 * @see     java.io.StreamTokenizer#eolIsSignificant(boolean)
 * @see     java.io.StreamTokenizer#nextToken()
 * @see     java.io.StreamTokenizer#quoteChar(int)
 * @see     java.io.StreamTokenizer#TT_EOF
 * @see     java.io.StreamTokenizer#TT_EOL
 * @see     java.io.StreamTokenizer#TT_NUMBER
 * @see     java.io.StreamTokenizer#TT_WORD
 */
	public int ttype;
/**
 * If the current token is a word token, this field contains a
 * string giving the characters of the word token. When the current
 * token is a quoted string token, this field contains the body of
 * the string.
 * <p>
 * The current token is a word when the value of the
 * <code>ttype</code> field is <code>TT_WORD</code>. The current token is
 * a quoted string token when the value of the <code>ttype</code> field is
 * a quote character.
 *
 * @see     java.io.StreamTokenizer#quoteChar(int)
 * @see     java.io.StreamTokenizer#TT_WORD
 * @see     java.io.StreamTokenizer#ttype
 */
	public String sval;
/**
 * If the current token is a number, this field contains the value
 * of that number. The current token is a number when the value of
 * the <code>ttype</code> field is <code>TT_NUMBER</code>.
 *
 * @see     java.io.StreamTokenizer#TT_NUMBER
 * @see     java.io.StreamTokenizer#ttype
*/
	public double nval;

	/**
	 * A constant indicating that the end of the file has been read.
	 */
	public static final int TT_EOF=  StreamTokenizer.TT_EOF;
/**
 * A constant indicating that the end of the line has been read.
 */
	public static final int TT_EOL=  StreamTokenizer.TT_EOL;
/**
 * A constant indicating that a number token has been read.
 */
	public static final int TT_NUMBER=  StreamTokenizer.TT_NUMBER;
/**
 * A constant indicating that a word token has been read.
 */
	public static final int TT_WORD=  StreamTokenizer.TT_WORD;
	/**
	 * Creates new FileTokenizer that parses the specified file..
	 *
	 * @param file a {@link java.io.File} object
	 * @throws java.io.FileNotFoundException if any.
	 */
	public FileTokenizer(File file) throws FileNotFoundException {
		super();
		reader= new FileReader(file);
		st= newStreamTokenizer();
		try {
			url= new URL("file:///"+file.toString());
		} catch (MalformedURLException e) {throw new FileNotFoundException("Unable to create URL from file name: "+e.toString());}
	}
	/**
	 * <p>Constructor for FileTokenizer.</p>
	 *
	 * @param reader a {@link java.io.Reader} object
	 * @param url a {@link java.net.URL} object
	 * @throws java.io.IOException if any.
	 */
	public FileTokenizer(Reader reader, URL url) throws IOException {
		super();
		this.url= url;
		this.reader= reader;
		st= newStreamTokenizer();
	}
	/**
	 * Creates new FileTokenizer that parses the specified file..
	 *
	 * @param url a {@link java.net.URL} object
	 * @throws java.io.IOException if any.
	 */
	public FileTokenizer(URL url) throws IOException {
		super();
		this.url= url;
		reader= new InputStreamReader(url.openStream());
		st= newStreamTokenizer();
	}
	/**
	 * Closes Reader.File is opened with InputStreamReader in constructor of this class.
	 *
	 * @see #FileTokenizer(File)
	 * @see #FileTokenizer(URL)
	 * @throws java.io.IOException if any.
	 */
	public void close() throws IOException {
		reader.close();
	}
	/**
	 * Specified that the character argument starts a single-line
	 * comment. All characters from the comment character to the end of
	 * the line are ignored by this stream tokenizer.
	 *
	 * @param   ch   the character.
	 */
	public void commentChar(int ch) {
		st.commentChar(ch);
	}
	/**
	 * Determines whether or not ends of line are treated as tokens.
	 * If the flag argument is true, this tokenizer treats end of lines
	 * as tokens; the <code>nextToken</code> method returns
	 * <code>TT_EOL</code> and also sets the <code>ttype</code> field to
	 * this value when an end of line is read.
	 * <p>
	 * A line is a sequence of characters ending with either a
	 * carriage-return character (<code>'&#92;r'</code>) or a newline
	 * character (<code>'&#92;n'</code>). In addition, a carriage-return
	 * character followed immediately by a newline character is treated
	 * as a single end-of-line token.
	 * <p>
	 * If the <code>flag</code> is false, end-of-line characters are
	 * treated as white space and serve only to separate tokens.
	 *
	 * @param   flag   <code>true</code> indicates that end-of-line characters
	 *                 are separate tokens; <code>false</code> indicates that
	 *                 end-of-line characters are white space.
	 * @see     java.io.StreamTokenizer#nextToken()
	 * @see     java.io.StreamTokenizer#ttype
	 * @see     java.io.StreamTokenizer#TT_EOL
	 */
	public void eolIsSignificant(boolean flag) {
		eolIsSignificant=flag;
	}
	/**
	 * Returns URL.
	 *
	 * @return java.net.URL
	 */
	public URL getURL() {
		return url;
	}
	/**
	 * Return the current line number.
	 *
	 * @return  the current line number of this stream tokenizer.
	 */
	public int lineno() {
		return st.lineno();
	}
	/**
	 * Determines whether or not word token are automatically lowercased.
	 * If the flag argument is <code>true</code>, then the value in the
	 * <code>sval</code> field is lowercased whenever a word token is
	 * returned (the <code>ttype</code> field has the
	 * value <code>TT_WORD</code> by the <code>nextToken</code> method
	 * of this tokenizer.
	 * <p>
	 * If the flag argument is <code>false</code>, then the
	 * <code>sval</code> field is not modified.
	 *
	 * @param   fl   <code>true</code> indicates that all word tokens should
	 *               be lowercased.
	 * @see     java.io.StreamTokenizer#nextToken()
	 * @see     java.io.StreamTokenizer#ttype
	 * @see     java.io.StreamTokenizer#TT_WORD
	 */
	public void lowerCaseMode(boolean fl) {
		st.lowerCaseMode(fl);
	}
	/**
	 *
	 */
	private void mergeFields() {
			ttype= st.ttype;
			sval= st.sval;
			nval= st.nval;
	}
	private StreamTokenizer newStreamTokenizer() {
		StreamTokenizer s= new StreamTokenizer(reader);
		s.resetSyntax();
		s.wordChars('A','Z');
		s.wordChars('a','z');
		s.wordChars('\u00A0','\u00FF');
		s.wordChars('0','9');
		s.wordChars('-','-');
		s.wordChars('.','.');
		s.whitespaceChars('\u0000','\u0020');
		s.commentChar('/');
		s.quoteChar('"');
		s.quoteChar('\'');
		s.eolIsSignificant(true);
		s.slashSlashComments(true);
		s.slashStarComments(true);
		return s;
	}

	/*All byte values 'A' through 'Z', 'a' through
			  'z', and '\u00A0' through '\u00FF' are
			  considered to be alphabetic.
			  All byte values '\u0000' through '\u0020' are
			  considered to be white space.
			  '/' is a comment character.
			  Single quote '\'' and double quote '"' are
			  string quote characters.
			  Numbers are parsed.
			  Ends of lines are treated as white space, not as
			  separate tokens.
			  C-style and C++-style comments are not
			  recognized.

			  */
	/**
	 * Parses the next token from the input stream of this tokenizer.
	 * The type of the next token is returned in the <code>ttype</code>
	 * field. Additional information about the token may be in the
	 * <code>nval</code> field or the <code>sval</code> field of this
	 * tokenizer.
	 *
	 * @return a int
	 * @throws java.io.IOException if any.
	 */
	public int nextToken() throws IOException {
		int i= st.nextToken();
		if (!eolIsSignificant && i==StreamTokenizer.TT_EOL)
			while (i == StreamTokenizer.TT_EOL) i=st.nextToken();
		mergeFields();
		return i;
	}
	/**
	 * Specifies that the character argument is "ordinary"
	 * in this tokenizer. It removes any special significance the
	 * character has as a comment character, word component, string
	 * delimiter, white space, or number character. When such a character
	 * is encountered by the parser, the parser treates it as a
	 * single-character token and sets <code>ttype</code> field to the
	 * character value.
	 *
	 * @param   ch   the character.
	 * @see     java.io.StreamTokenizer#ttype
	 */
	public void ordinaryChar(int ch) {
		st.ordinaryChar(ch);
	}
	/**
	 * Specifies that numbers should be parsed by this tokenizer. The
	 * syntax table of this tokenizer is modified so that each of the twelve
	 * characters:
	 * <blockquote><pre>
	 *      0 1 2 3 4 5 6 7 8 9 . -
	 * </pre></blockquote>
	 * <p>
	 * has the "numeric" attribute.
	 * <p>
	 * When the parser encounters a word token that has the format of a
	 * double precision floating-point number, it treats the token as a
	 * number rather than a word, by setting the the <code>ttype</code>
	 * field to the value <code>TT_NUMBER</code> and putting the numeric
	 * value of the token into the <code>nval</code> field.
	 *
	 * @see     java.io.StreamTokenizer#nval
	 * @see     java.io.StreamTokenizer#TT_NUMBER
	 * @see     java.io.StreamTokenizer#ttype
	 */
	public void parseNumbers() {
		st.parseNumbers();
	}
	/**
	 * Causes the next call to the <code>nextToken</code> method of this
	 * tokenizer to return the current value in the <code>ttype</code>
	 * field, and not to modify the value in the <code>nval</code> or
	 * <code>sval</code> field.
	 *
	 * @see     java.io.StreamTokenizer#nextToken()
	 * @see     java.io.StreamTokenizer#nval
	 * @see     java.io.StreamTokenizer#sval
	 * @see     java.io.StreamTokenizer#ttype
	 */
	public void pushBack() {
		st.pushBack();
	}
	/**
	 * Specifies that matching pairs of this character delimit string
	 * constants in this tokenizer.
	 * <p>
	 * When the <code>nextToken</code> method encounters a string
	 * constant, the <code>ttype</code> field is set to the string
	 * delimiter and the <code>sval</code> field is set to the body of
	 * the string.
	 * <p>
	 * If a string quote character is encountered, then a string is
	 * recognized, consisting of all characters after (but not including)
	 * the string quote character, up to (but not including) the next
	 * occurrence of that same string quote character, or a line
	 * terminator, or end of file. The usual escape sequences such as
	 * <code>"&#92;n"</code> and <code>"&#92;t"</code> are recognized and
	 * converted to single characters as the string is parsed.
	 *
	 * @param   ch   the character.
	 * @see     java.io.StreamTokenizer#nextToken()
	 * @see     java.io.StreamTokenizer#sval
	 * @see     java.io.StreamTokenizer#ttype
	 */
	public void quoteChar(int ch) {
		st.quoteChar(ch);
	}
	/**
	 * Sets URL to new value.
	 *
	 * @param u java.net.URL
	 */
	public void setURL(URL u) {
		url= u;
	}
	/**
	 * Moves reading position to end of current line. ttype
	 * field becomes TT_EOL. Next call to nextToken() will return
	 * first token in next line.
	 *
	 * @throws java.io.IOException if any.
	 * @throws java.text.ParseException if any.
	 */
	public void skipLine() throws IOException, java.text.ParseException {
		int i= st.nextToken();
		while (i!=StreamTokenizer.TT_EOL && i!= StreamTokenizer.TT_EOF) i= st.nextToken();
		if (!eolIsSignificant && i==StreamTokenizer.TT_EOL)
			while (i == StreamTokenizer.TT_EOL) i=st.nextToken();
		mergeFields();
	}
	/**
	 * Determines whether or not the tokenizer recognizes C++-style comments.
	 * If the flag argument is <code>true</code>, this stream tokenizer
	 * recognizes C++-style comments. Any occurrence of two consecutive
	 * slash characters (<code>'/'</code>) is treated as the beginning of
	 * a comment that extends to the end of the line.
	 * <p>
	 * If the flag argument is <code>false</code>, then C++-style
	 * comments are not treated specially.
	 *
	 * @param   flag   <code>true</code> indicates to recognize and ignore
	 *                 C++-style comments.
	 */
	public void slashSlashComments(boolean flag) {
		st.slashSlashComments(flag);
	}
	/**
	 * Determines whether or not the tokenizer recognizes C-style comments.
	 * If the flag argument is <code>true</code>, this stream tokenizer
	 * recognizes C-style comments. All text between successive
	 * occurrences of <code>/*</code> and <code>*&#47;</code> are discarded.
	 * <p>
	 * If the flag argument is <code>false</code>, then C-style comments
	 * are not treated specially.
	 *
	 * @param   flag   <code>true</code> indicates to recognize and ignore
	 *                 C-style comments.
	 */
	public void slashStarComments(boolean flag) {
		st.slashStarComments(flag);
	}
	/**
	 * {@inheritDoc}
	 *
	 * Returnes information about unknown source from where FileTokenizer should read.
	 */
	@Override
	public String toString() {
		if (url!=null) return "["+  url.toString() + ": "+st.toString()+"]";
		return "[unknown source: "+st.toString()+"]";
	}
	/**
	 * Specifies that all characters <i>c</i> in the range
	 * <code>low&nbsp;&lt;=&nbsp;<i>c</i>&nbsp;&lt;=&nbsp;high</code>
	 * are white space characters. White space characters serve only to
	 * separate tokens in the input stream.
	 *
	 * @param   low   the low end of the range.
	 * @param   hi    the high end of the range.
	 */
	public void whitespaceChars(int low, int hi) {
		st.whitespaceChars(low,hi);
	}
	/**
	 * Specifies that all characters <i>c</i> in the range
	 * <code>low&nbsp;&lt;=&nbsp;<i>c</i>&nbsp;&lt;=&nbsp;high</code>
	 * are word constituents. A word token consists of a word constituent
	 * followed by zero or more word constituents or number constituents.
	 *
	 * @param   low   the low end of the range.
	 * @param   hi    the high end of the range.
	 */
	public void wordChars(int low, int hi) {
		st.wordChars(low,hi);
	}
}
