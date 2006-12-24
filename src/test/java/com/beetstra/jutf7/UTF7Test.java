/* ====================================================================
 * Copyright (c) 2006 J.T. Beetstra
 *
 * Permission is hereby granted, free of charge, to any person obtaining 
 * a copy of this software and associated documentation files (the 
 * "Software"), to deal in the Software without restriction, including 
 * without limitation the rights to use, copy, modify, merge, publish, 
 * distribute, sublicense, and/or sell copies of the Software, and to 
 * permit persons to whom the Software is furnished to do so, subject to 
 * the following conditions:
 *
 * The above copyright notice and this permission notice shall be 
 * included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, 
 * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF 
 * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. 
 * IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY 
 * CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, 
 * TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE 
 * SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 * ====================================================================
 */
package com.beetstra.jutf7;

import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CoderResult;

public class UTF7Test extends CharsetTest {
	protected void setUp() throws Exception {
		tested = new UTF7Charset("UTF-7", new String[] {}, false);
	}

	public void testContains() {
		assertTrue(tested.contains(Charset.forName("US-ASCII")));
		assertTrue(tested.contains(Charset.forName("ISO-8859-1")));
		assertTrue(tested.contains(Charset.forName("UTF-8")));
		assertTrue(tested.contains(Charset.forName("UTF-16")));
		assertTrue(tested.contains(Charset.forName("UTF-16LE")));
		assertTrue(tested.contains(Charset.forName("UTF-16BE")));
	}

	public void testEmpty() throws Exception {
		String string = "";
		assertEquals(string, decode(string));
		assertEquals(string, encode(string));
	}

	public void testDecodeSimple() throws Exception {
		assertEquals("abcdefghijklmnopqrstuvwxyz", decode("abcdefghijklmnopqrstuvwxyz"));
	}

	public void testDecodeShiftSequence() throws Exception {
		assertEquals("+", decode("+-"));
		assertEquals("+-", decode("+--"));
		assertEquals("++", decode("+-+-"));
	}

	public void testDecodeBase64() throws Exception {
		assertEquals("Hi Mom \u263A!", decode("Hi Mom +Jjo-!"));
		assertEquals("Hi Mom -\u263A-!", decode("Hi Mom -+Jjo--!"));
		assertEquals("\u65E5\u672C\u8A9E", decode("+ZeVnLIqe-"));
		assertEquals("Item 3 is £1.", decode("Item 3 is +AKM-1."));
	}

	public void testDecodeLong() throws Exception {
		assertEquals("xxxÿxÿxÿÿÿÿÿÿÿ", decode("xxx+AP8-x+AP8-x+AP8A/wD/AP8A/wD/AP8-"));
		assertEquals("\u2262\u0391123\u2262\u0391", decode("+ImIDkQ-123+ImIDkQ"));
	}

	public void testDecodeUnclosed() throws Exception {
		assertEquals("ÿÿÿ#", decode("+AP8A/wD/#"));
		assertEquals("ÿÿÿ#", decode("+AP8A/wD/#"));
		assertEquals("ÿÿ#", decode("+AP8A/w#"));
		assertEquals("#áá#ááá#", decode("#+AOEA4Q#+AOEA4QDh#"));
	}

	public void testDecodeNoUnshiftAtEnd() throws Exception {
		assertEquals("€áé", decode("+IKwA4QDp"));
		assertEquals("#ááá", decode("#+AOEA4QDh"));
	}

	public void testDecodeMalformed() throws Exception {
		verifyMalformed("+IKx#");
		verifyMalformed("+IKwA#");
		verifyMalformed("+IKwA4#");
		assertEquals("€á", decode("+IKwA4Q"));
	}

	public void testDecodeOptionalCharsUTF7() throws Exception {
		assertEquals("~!@", decode("+AH4AIQBA-"));
	}

	public void testDecodeOptionalCharsPlain() throws Exception {
		assertEquals("!\"#$%*;<=>@[]^_'{|}", decode("!\"#$%*;<=>@[]^_'{|}"));
	}

	public void testDecodeLimitedOutput() throws Exception {
		CharsetDecoder decoder = tested.newDecoder();
		ByteBuffer in = CharsetTestUtil.wrap("+IKwA4QDp-");
		CharBuffer out = CharBuffer.allocate(3);
		assertEquals(CoderResult.UNDERFLOW, decoder.decode(in, out, true));
		assertEquals(CoderResult.UNDERFLOW, decoder.flush(out));
		out.flip();
		assertEquals("€áé", out.toString());
		decoder.reset();
		in = CharsetTestUtil.wrap("A+ImIDkQ.");
		out = CharBuffer.allocate(4);
		assertEquals(CoderResult.UNDERFLOW, decoder.decode(in, out, true));
		out.flip();
		assertEquals("A\u2262\u0391.", out.toString());
	}

	private void verifyMalformed(final String string) throws UnsupportedEncodingException {
		ByteBuffer in = CharsetTestUtil.wrap(string);
		CharBuffer out = CharBuffer.allocate(1024);
		CoderResult result = tested.newDecoder().decode(in, out, false); //"€#"
		assertTrue(result.isMalformed());
	}

	public void testEncodeSimple() throws Exception {
		assertEquals("abcdefghijklmnopqrstuvwxyz", CharsetTestUtil.asString(tested
				.encode("abcdefghijklmnopqrstuvwxyz")));
		assertEquals(" \r\t\n", CharsetTestUtil.asString(tested.encode(" \r\t\n")));
	}

	public void testEncodeBase64() throws Exception {
		assertEquals("A+ImIDkQ.", encode("A\u2262\u0391."));
	}

	public void testEncodeBase64NoImplicitUnshift() throws Exception {
		assertEquals("A+ImIDkQ-A", encode("A\u2262\u0391A"));
	}

	public void testEncodeLong() throws Exception {
		assertEquals("+IKwA4QDpAPoA7QDzAP0A5ADrAO8A9gD8AP8-", encode("€áéúíóýäëïöüÿ"));
	}

	public void testEncodeShiftUnshift() throws Exception {
		assertEquals("+--", encode("+-"));
	}

	public void testEncodeAddUnshiftOnUnshift() throws Exception {
		assertEquals("+AO0AKw--", encode("í+-"));
	}

	public void testEncodeNormalAndDifferent() throws Exception {
		assertEquals("xxx+AP8-x+AP8-x+AP8A/wD/AP8A/wD/AP8-", encode("xxxÿxÿxÿÿÿÿÿÿÿ"));
		assertEquals("+AP8A/wD/AP8-x+AP8A/wD/AP8-", encode("ÿÿÿÿxÿÿÿÿ"));
		assertEquals("abc+AOEA6QDt-def+APMA+gDk-gh", encode("abcáéídefóúägh"));
	}

	public void testEncodeOptionalCharsUTF7() throws Exception {
		assertEquals("+ACEAIgAjACQAJQAqADsAPAA9AD4AQABbAF0AXgBfAGAAewB8AH0-",
				encode("!\"#$%*;<=>@[]^_`{|}"));
	}

	public void testEncodeAlphabet() throws Exception {
		assertEquals("+AL4AvgC+-", encode("¾¾¾"));
		assertEquals("+AL8AvwC/-", encode("¿¿¿"));
	}
}
