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
import java.nio.charset.CharacterCodingException;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CharsetEncoder;
import java.nio.charset.CoderResult;
import junit.framework.TestCase;

public class AcceptanceTest extends TestCase {
	private CharsetProvider provider;
	private Charset charset;
	private CharsetDecoder decoder;
	private CharsetEncoder encoder;

	protected void setUp() throws Exception {
		provider = new CharsetProvider();
	}

	public void testUTF7() throws Exception {
		init("UTF-7");
		assertEquals("A+ImIDkQ.", encodeGetBytes("A\u2262\u0391."));
		assertEquals("A+ImIDkQ.", encodeCharsetEncode("A\u2262\u0391."));
		assertEquals("+ACEAIgAj-", encodeGetBytes("!\"#"));
		verifyAll();
	}

	public void testUTF7o() throws Exception {
		init("X-UTF-7-OPTIONAL");
		assertEquals("A+ImIDkQ.", encodeGetBytes("A\u2262\u0391."));
		assertEquals("A+ImIDkQ.", encodeCharsetEncode("A\u2262\u0391."));
		assertEquals("!\"#", encodeGetBytes("!\"#"));
		verifyAll();
	}

	public void testModifiedUTF7() throws Exception {
		init("x-IMAP4-MODIFIED-UTF7");
		assertEquals("A&ImIDkQ-.", encodeGetBytes("A\u2262\u0391."));
		assertEquals("A&ImIDkQ-.", encodeCharsetEncode("A\u2262\u0391."));
		verifyAll();
	}

	private void init(final String init) {
		charset = provider.charsetForName(init);
		decoder = charset.newDecoder();
		encoder = charset.newEncoder();
	}

	private void verifyAll() throws Exception {
		verifySymmetrical("\u00E1\u00E9\u00ED\u00F3\u00FA\u00E4\u00EB\u00EF\u00F6\u00FC\u00E0\u00E8\u00EC\u00F2\u00F9\u00E2\u00EA\u00EE\u00F4\u00FB\u00E3\u00F5\u00E7\u00F1\u20AC");
		verifySymmetrical("a\u00E1b\u00E9c\u00EDd\u00F3e\u00FAf\u00E4g\u00EBh\u00EFi\u00F6j\u00FCk\u00E0l\u00E8m\u00ECn\u00F2o\u00F9p\u00E2q\u00EAr\u00EEs\u00F4t\u00FBu\u00E3v\u00F5w\u00E7x\u00F1y\u20ACz");
		verifySymmetrical("abc\u00E1\u00E9\u00EDdef\u00F3\u00FA\u00E4ghi\u00EB\u00EF\u00F6jkl\u00FC\u00E0\u00E8mno\u00EC\u00F2\u00F9pqr\u00E2\u00EA\u00EEstu\u00F4\u00FB\u00E3vwx\u00F5\u00E7\u00F1yz\u20AC");
		verifySymmetrical("abcdefghijklmnopqrstuvwyxz\u00E1\u00E9\u00ED\u00F3\u00FA\u00E4\u00EB\u00EF\u00F6\u00FC\u00E0\u00E8\u00EC\u00F2\u00F9\u00E2\u00EA\u00EE\u00F4\u00FB\u00E3\u00F5\u00E7\u00F1\u20ACabcdefghijklmnopqrstuvwyxz");
		verifySymmetrical("a\u00E1b+\u00E9c\u00ED+-d\u00F3e-\u00FAf\u00E4-+g\u00EBh+\u00EFi\u00F6+-j\u00FCk-\u00E0l\u00E8-+m\u00ECn+\u00F2o\u00F9+-p\u00E2q-\u00EAr\u00EE-+s\u00F4t+\u00FBu\u00E3+-v\u00F5w\u00E7-x\u00F1y-+\u20ACz+");
		verifySymmetrical("\u00E1+\u00E9\u00ED+\u00F3\u00FA\u00E4+\u00EB\u00EF\u00F6++\u00FC++\u00E0\u00E8++\u00EC\u00F2\u00F9+++\u00E2+++\u00EA\u00EE+++\u00F4\u00FB\u00E3+++\u00F5\u00E7\u00F1\u20AC");
		verifySymmetrical("\u00E1+-\u00E9\u00ED+-\u00F3\u00FA\u00E4+-\u00EB\u00EF\u00F6++-\u00FC++-\u00E0\u00E8++-\u00EC\u00F2\u00F9+++-\u00E2+++-\u00EA\u00EE+++-\u00F4\u00FB\u00E3+++-\u00F5\u00E7\u00F1\u20AC");
		verifySymmetrical("++++++++");
		verifySymmetrical("+-++--+++---++");
		verifySymmetrical("+\u00E1\u00E9\u00ED+");
		verifySymmetrical("`~!@#$%^&*()_+-=[]\\{}|;':\",./<>?\u0000\r\n\t\b\f\u20AC");
		verifySymmetrical("#a\u00E1a#\u00E1#\u00E1\u00E1#\u00E1\u00E1\u00E1#");
	}

	protected void verifySymmetrical(String s) throws Exception {
		final String encoded = encodeGetBytes(s);
		assertEquals(encoded, encodeCharsetEncode(s));
		assertEquals("problem decoding " + encoded, s, decode(encoded));
		for (int i = 4; i < encoded.length(); i++) {
			ByteBuffer in = CharsetTestUtil.wrap(encoded);
			decoder.reset();
			verifyChunkedOutDecode(i, in, s);
		}
		for (int i = 10; i < encoded.length(); i++) {
			CharBuffer in = CharBuffer.wrap(s);
			encoder.reset();
			verifyChunkedOutEncode(i, in, encoded);
		}
		for (int i = 10; i < encoded.length(); i++) {
			decoder.reset();
			verifyChunkedInDecode(i, encoded, s);
		}
		for (int i = 4; i < encoded.length(); i++) {
			encoder.reset();
			verifyChunkedInEncode(i, s, encoded);
		}
	}

	private String encodeCharsetEncode(String string) throws UnsupportedEncodingException {
		String charsetEncode = CharsetTestUtil.asString(charset.encode(string));
		return charsetEncode;
	}

	/*
	 * simulate what is done in String.getBytes
	 * (cannot be used directly since Charset is not installed while testing)
	 */
	private String encodeGetBytes(String string) throws CharacterCodingException,
			UnsupportedEncodingException {
		ByteBuffer bb = ByteBuffer.allocate((int) (encoder.maxBytesPerChar() * string.length()));
		CharBuffer cb = CharBuffer.wrap(string);
		encoder.reset();
		CoderResult cr = encoder.encode(cb, bb, true);
		if (!cr.isUnderflow())
			cr.throwException();
		cr = encoder.flush(bb);
		if (!cr.isUnderflow())
			cr.throwException();
		bb.flip();
		String stringGetBytes = CharsetTestUtil.asString(bb);
		return stringGetBytes;
	}

	protected String decode(String string) throws UnsupportedEncodingException {
		final ByteBuffer buffer = CharsetTestUtil.wrap(string);
		final CharBuffer decoded = charset.decode(buffer);
		return decoded.toString();
	}

	protected void verifyChunkedInDecode(int i, String encoded, String decoded)
			throws UnsupportedEncodingException {
		ByteBuffer in = ByteBuffer.allocate(i);
		CharBuffer out = CharBuffer.allocate(decoded.length() + 5);
		int pos = 0;
		CoderResult result = CoderResult.UNDERFLOW;
		while (pos < encoded.length()) {
			int end = Math.min(encoded.length(), pos + i);
			in.put(CharsetTestUtil.wrap(encoded.substring(pos + in.position(), end)));
			in.flip();
			result = decoder.decode(in, out, false);
			assertEquals("at position: " + pos, CoderResult.UNDERFLOW, result);
			assertTrue("no progress after " + pos + " of " + encoded.length(), in.position() > 0);
			pos += in.position();
			in.compact();
		}
		in.limit(0);
		result = decoder.decode(in, out, true);
		assertEquals(CoderResult.UNDERFLOW, result);
		result = decoder.flush(out);
		assertEquals(CoderResult.UNDERFLOW, result);
		assertEquals(encoded.length(), pos);
		assertEquals(decoded.length(), out.position());
		out.flip();
		assertEquals("for length: " + i, decoded, out.toString());
	}

	protected void verifyChunkedInEncode(int i, String decoded, String encoded)
			throws UnsupportedEncodingException {
		CharBuffer in = CharBuffer.allocate(i);
		ByteBuffer out = ByteBuffer.allocate(encoded.length() + 40);
		int pos = 0;
		CoderResult result = CoderResult.UNDERFLOW;
		while (pos < decoded.length()) {
			int end = Math.min(decoded.length(), pos + i);
			in.put(decoded.substring(pos + in.position(), end));
			in.flip();
			assertTrue("unexpected end at " + pos, in.limit() > 0);
			result = encoder.encode(in, out, false);
			if (result.isUnderflow())
				assertTrue("no progress after " + pos + " of " + decoded.length() + " in "
						+ decoded, in.position() > 0);
			pos += in.position();
			in.compact();
		}
		pos += in.position();
		in.limit(0);
		result = encoder.encode(in, out, true);
		result = encoder.flush(out);
		assertEquals(CoderResult.UNDERFLOW, result);
		out.flip();
		assertEquals("for length: " + i, encoded, CharsetTestUtil.asString(out));
	}

	protected void verifyChunkedOutEncode(int i, CharBuffer in, String encoded)
			throws UnsupportedEncodingException {
		ByteBuffer out = ByteBuffer.allocate(i);
		int encodeCount = 0;
		StringBuffer sb = new StringBuffer();
		CoderResult result;
		while (in.hasRemaining()) {
			result = encoder.encode(in, out, false);
			encodeCount += out.position();
			if (in.hasRemaining()) {
				assertEquals("at position: " + encodeCount, CoderResult.OVERFLOW, result);
				assertTrue("at position: " + encodeCount, out.position() > 0);
			}
			CharsetTestUtil.outToSB(out, sb);
		}
		result = encoder.encode(in, out, true);
		assertFalse(!result.isOverflow() && in.hasRemaining());
		CharsetTestUtil.outToSB(out, sb);
		result = encoder.flush(out);
		CharsetTestUtil.outToSB(out, sb);
		assertEquals(encoded, sb.toString());
		in.rewind();
	}

	protected void verifyChunkedOutDecode(int i, ByteBuffer in, String decoded) {
		CharBuffer out = CharBuffer.allocate(i);
		int decodeCount = 0;
		StringBuffer sb = new StringBuffer();
		CoderResult result = CoderResult.OVERFLOW;
		while (decodeCount < decoded.length()) {
			assertEquals("at position: " + decodeCount, CoderResult.OVERFLOW, result);
			result = decoder.decode(in, out, true);
			assertTrue(out.position() > 0);
			decodeCount += out.position();
			out.flip();
			sb.append(out.toString());
			out.clear();
		}
		assertEquals(decoded, sb.toString());
		in.rewind();
	}
}
