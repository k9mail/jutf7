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

import java.nio.charset.Charset;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Locale;

import junit.framework.TestCase;

public class CharsetProviderTest extends TestCase {
	private CharsetProvider tested = new CharsetProvider();

	public void testOK() throws Exception {
		assertTrue(true);
	}

	public void testModifiedUTF7() throws Exception {
		Charset charset = tested.charsetForName("x-modified-UTF-7");
		assertNotNull("charset not found", charset);
		assertEquals(ModifiedUTF7Charset.class, charset.getClass());
		assertEquals(charset, tested.charsetForName("x-imap-modified-utf-7"));
		assertEquals(charset, tested.charsetForName("x-imap4-modified-utf7"));
		assertEquals(charset, tested.charsetForName("x-imap4-modified-utf-7"));
		assertEquals(charset, tested.charsetForName("x-RFC3501"));
		assertEquals(charset, tested.charsetForName("x-RFC-3501"));
	}

	public void testUTF7() throws Exception {
		Charset charset = tested.charsetForName("UTF-7");
		assertNotNull("charset not found", charset);
		assertEquals(UTF7Charset.class, charset.getClass());
		assertEquals(charset, tested.charsetForName("utf-7"));
		assertEquals(charset, tested.charsetForName("UNICODE-1-1-UTF-7"));
		assertEquals(charset, tested.charsetForName("csUnicode11UTF7"));
		assertEquals(charset, tested.charsetForName("x-RFC2152"));
		assertEquals(charset, tested.charsetForName("x-RFC-2152"));
	}

	public void testUTF7optional() throws Exception {
		Charset charset = tested.charsetForName("X-UTF-7-OPTIONAL");
		assertNotNull("charset not found", charset);
		assertEquals(UTF7Charset.class, charset.getClass());
		assertEquals(charset, tested.charsetForName("x-utf-7-optional"));
		assertEquals(charset, tested.charsetForName("x-RFC2152-optional"));
		assertEquals(charset, tested.charsetForName("x-RFC-2152-optional"));
	}

	public void testNotHere() throws Exception {
		assertNull(tested.charsetForName("X-DOES-NOT-EXIST"));
	}

	public void testIterator() throws Exception {
		Iterator iterator = tested.charsets();
		HashSet found = new HashSet();
		while (iterator.hasNext())
			found.add(iterator.next());
		assertEquals(3, found.size());
		Charset charset1 = tested.charsetForName("x-IMAP4-modified-UTF7");
		Charset charset2 = tested.charsetForName("UTF-7");
		Charset charset3 = tested.charsetForName("X-UTF-7-OPTIONAL");
		assertTrue(found.contains(charset1));
		assertTrue(found.contains(charset2));
		assertTrue(found.contains(charset3));
	}

	public void testTurkish() throws Exception {
		Locale.setDefault(new Locale("tr", "TR"));
		assertEquals(tested.charsetForName("UTF-7"), tested.charsetForName("unicode-1-1-utf-7"));
	}
}
