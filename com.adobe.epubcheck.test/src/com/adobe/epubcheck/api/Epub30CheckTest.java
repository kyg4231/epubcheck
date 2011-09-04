/*
 * Copyright (c) 2011 Adobe Systems Incorporated
 *
 *  Permission is hereby granted, free of charge, to any person obtaining a copy of
 *  this software and associated documentation files (the "Software"), to deal in
 *  the Software without restriction, including without limitation the rights to
 *  use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of
 *  the Software, and to permit persons to whom the Software is furnished to do so,
 *  subject to the following conditions:
 *
 *  The above copyright notice and this permission notice shall be included in all
 *  copies or substantial portions of the Software.
 *
 *  THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 *  IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS
 *  FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR
 *  COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER
 *  IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN
 *  CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 *
 */

package com.adobe.epubcheck.api;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.io.IOException;

import org.junit.Test;

import com.adobe.epubcheck.opf.DocumentValidator;
import com.adobe.epubcheck.util.FileResourceProvider;
import com.adobe.epubcheck.util.GenericResourceProvider;
import com.adobe.epubcheck.util.URLResourceProvider;
import com.adobe.epubcheck.util.ValidationReport;

public class Epub30CheckTest {

	private ValidationReport testReport;

	private DocumentValidator epubCheck;

	private GenericResourceProvider resourceProvider;

	private boolean verbose;

	private static String path = "testdocs/30/epub/";
	
	/*
	 * TEST DEBUG FUNCTION
	 */
	
	public void testValidateDocument(String fileName, int errors, int warnings,
			boolean verbose) {
		if (verbose)
			this.verbose = verbose;
		testValidateDocument(fileName, errors, warnings);
	}

	public void testValidateDocument(String fileName, int errors, int warnings) {

		boolean fromFile = false;

		testReport = new ValidationReport(fileName);

		if (fileName.startsWith("http://") || fileName.startsWith("https://")) {
			resourceProvider = new URLResourceProvider(fileName);
		} else {
			resourceProvider = new FileResourceProvider(path + fileName);
			fromFile = true;
		}

		if (fromFile)
			epubCheck = new EpubCheck(new File(path + fileName), testReport);
		else
			try {
				epubCheck = new EpubCheck(
						resourceProvider.getInputStream(null), testReport);
			} catch (IOException e) {
				throw new RuntimeException(e);
			}

		epubCheck.validate();

		if (verbose) {
			verbose = false;
			System.out.println(testReport);
		}

		assertEquals(errors, testReport.getErrorCount());
		assertEquals(warnings, testReport.getWarningCount());
	}
//TODO -- check for fallback cycles
/*	@Test
	public void testValidateEPUBPFallbackCycle() {
		testValidateDocument("invalid/fallback-cycle.epub", 0, 0,true);
	}
*/	
	@Test
	public void testValidateEPUBPvalid30() {
		testValidateDocument("valid/lorem.epub", 0, 0);
	}
	
	@Test
	public void testValidateEPUBTestSvg() {
		testValidateDocument("valid/test_svg.epub", 0, 0);
	}

	@Test
	public void testValidateEPUBInvalidNcx() {
		testValidateDocument("invalid/invalid-ncx.epub", 2, 0);
	}
	
	@Test
	public void testValidateEPUBMp3() {
		testValidateDocument("valid/mp3-in-manifest.epub", 0, 0);
	}
	
	@Test
	public void testValidateEPUBInvalidMp3() {
		testValidateDocument("invalid/mp3-in-spine-no-fallback.epub", 1, 0);
	}
	
	@Test
	public void testValidateEPUBMp3WithFallback() {
		testValidateDocument("valid/mp3-with-fallback.epub", 0, 0);
	}
	
	@Test
	public void testValidateEPUBFontNoFallback() {
		testValidateDocument("invalid/font_no_fallback.epub", 1, 0);
	}
	
	@Test
	public void testValidateEPUBFontFallbackChain() {
		testValidateDocument("valid/font_fallback_chain.epub", 0, 0);
	}	
	
	@Test
	public void testValidateEPUBvalid30() {
		testValidateDocument("valid/lorem.epub", 0, 0);
	}
	
	@Test
	public void testValidateEPUB30_xhtmlsch() {
		//1 schematron error from xhtml validation
		testValidateDocument("invalid/lorem-xht-sch-1.epub", 1, 0);
	}
	
	@Test
	public void testValidateEPUB30_xhtmlrng() {
		//1 rng error from xhtml validation
		testValidateDocument("invalid/lorem-xht-rng-1.epub", 1, 0);
	}
}
