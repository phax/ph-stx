package java_cup.runtime;

/**
 * Defines the Symbol class, which is used to represent all terminals and
 * nonterminals while parsing. The lexer should pass CUP Symbols and CUP returns
 * a Symbol.
 *
 * @version last updated: 7/3/96
 * @author Frank Flannery
 */

/*
 * **************************************************************** Class Symbol
 * what the parser expects to receive from the lexer. the token is identified as
 * follows: sym: the symbol type parse_state: the parse state. value: is the
 * lexical value of type Object left : is the left position in the original
 * input file right: is the right position in the original input file xleft: is
 * the left position Object in the original input file xright: is the left
 * position Object in the original input file
 ******************************************************************/

public class Symbol
{

  // TUM 20060327: Added new Constructor to provide more flexible way
  // for location handling
  /*******************************
   *******************************/
  public Symbol (final int id, final Symbol left, final Symbol right, final Object o)
  {
    this (id, left.m_nLeft, right.m_nRight, o);
  }

  public Symbol (final int id, final Symbol left, final Symbol right)
  {
    this (id, left.m_nLeft, right.m_nRight);
  }

  public Symbol (final int id, final Symbol left, final Object o)
  {
    this (id, left.m_nRight, left.m_nRight, o);
  }

  /*******************************
   * Constructor for l,r values
   *******************************/

  public Symbol (final int id, final int l, final int r, final Object o)
  {
    this (id);
    m_nLeft = l;
    m_nRight = r;
    m_aValue = o;
  }

  /*******************************
   * Constructor for no l,r values
   ********************************/

  public Symbol (final int id, final Object o)
  {
    this (id, -1, -1, o);
  }

  /*****************************
   * Constructor for no value
   ***************************/

  public Symbol (final int id, final int l, final int r)
  {
    this (id, l, r, null);
  }

  /***********************************
   * Constructor for no value or l,r
   ***********************************/

  public Symbol (final int sym_num)
  {
    this (sym_num, -1);
    m_nLeft = -1;
    m_nRight = -1;
  }

  /***********************************
   * Constructor to give a start state
   ***********************************/
  Symbol (final int sym_num, final int state)
  {
    sym = sym_num;
    parse_state = state;
  }

  /* . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . */

  /** The symbol number of the terminal or non terminal being represented */
  public int sym;

  /* . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . */

  /**
   * The parse state to be recorded on the parse stack with this symbol. This
   * field is for the convenience of the parser and shouldn't be modified except
   * by the parser.
   */
  public int parse_state;
  /**
   * This allows us to catch some errors caused by scanners recycling symbols.
   * For the use of the parser only. [CSA, 23-Jul-1999]
   */
  boolean used_by_parser = false;

  /*******************************
   * The data passed to parser
   *******************************/

  public int m_nLeft, m_nRight;
  public Object m_aValue;

  /*****************************
   * Printing this token out. (Override for pretty-print).
   ****************************/
  @Override
  public String toString ()
  {
    return "#" + sym;
  }
}
