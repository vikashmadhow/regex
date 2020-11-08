package ma.vi.regex;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Vikash Madhow (vikash.madhow@gmail.com)
 */
class RegExTest {
  @Test
  void simpleRegex() {
    RegEx r = new RegEx("abc*");
    assertTrue(r.match("ab"));
    assertTrue(r.match("abc"));
    assertTrue(r.match("abcccc"));
    assertFalse(r.match("a"));
    assertFalse(r.match("ac"));
    assertFalse(r.match(""));
    assertFalse(r.match("abd"));
  }
}