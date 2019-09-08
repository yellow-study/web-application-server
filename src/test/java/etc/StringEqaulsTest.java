package etc;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

public class StringEqaulsTest {
	private String newStr;
	private String newStr2;
	private String newStr3;
	private String newStr4;

	@Before
	public void setUp() {
		newStr = new String("first String");
		newStr2 = new String("first String");
		newStr3 = "first String";
		newStr4 = "first String";
	}

	@Test
	public void newString_연산자_주소값_비교_Test() {
		assertFalse(newStr == newStr2);
		assertTrue(newStr3 == newStr4);
	}

	@Test
	public void newString_Equals_값_비교_Test() {
		assertTrue(newStr.equals(newStr2));
		assertTrue(newStr3.equals(newStr4));
	}

}
