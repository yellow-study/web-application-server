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
		newStr = new String("first String"); //객체
		newStr2 = new String("first String");
		newStr3 = "first String"; //리터럴 String pool 생성
		newStr4 = "first String";
	}

	@Test
	public void newString_연산자_주소값_비교() {
		assertFalse(newStr == newStr2);
		assertTrue(newStr3 == newStr4);
	}

	@Test
	public void newString_Equals_값_비교() {
		assertTrue(newStr.equals(newStr2));
		assertTrue(newStr3.equals(newStr4));
	}

	@Test
	public void newString_객체_리터럴_비교() {
		assertFalse(newStr == newStr3);
		assertTrue(newStr.equals(newStr3));
	}

}
