/*
 * Copyright (c) 2007 Adobe Systems Incorporated
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

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.zip.ZipFile;

import com.adobe.epubcheck.ocf.OCFPreflight;
import com.adobe.epubcheck.util.CheckUtil;
import com.adobe.epubcheck.util.DefaultReportImpl;
import com.adobe.epubcheck.util.WriterReportImpl;

/**
 * Public interface to epub validator.
 */
public class Preflight {
	/* VERSION number is duplicated in the build.xml file, so you'll need to change it twice */
	public static final String VERSION = "0.1.0";

	File epubFile;

	Report userReport;

	int warningCount;

	int errorCount;

	static String fixMessage(String message) {
		return message.replaceAll("\r\n", " ").replaceAll("\r", " ")
				.replaceAll("\n", " ");
	}

	class ProxyReport implements Report {

		public void error(String resource, int line, String message) {
			errorCount++;
			userReport.error(resource, line, fixMessage(message));
		}

		public void warning(String resource, int line, String message) {
			warningCount++;
			userReport.warning(resource, line, fixMessage(message));
		}

	}

	/*
	 * Create an epub validator to validate the given file. Issues will be
	 * reported to standard error.
	 */
	public Preflight(File epubFile) {
		this.epubFile = epubFile;
		this.userReport = new DefaultReportImpl(epubFile.getName());
	}

	/*
	 * Create an epub validator to validate the given file. Issues will be
	 * reported to the given PrintWriter.
	 */
	public Preflight(File epubFile, PrintWriter out) {
		this.epubFile = epubFile;
		this.userReport = new WriterReportImpl(out);
	}

	/*
	 * Create an epub validator to validate the given file and report issues to
	 * a given Report object.
	 */
	public Preflight(File epubFile, Report report) {
		this.epubFile = epubFile;
		this.userReport = report;
	}

	/**
	 * Validate the file. Return true if no errors or warnings found.
	 */
	public boolean validate() {
		Report report = new ProxyReport();
		try {
			FileInputStream epubIn = new FileInputStream(epubFile);

			byte[] header = new byte[58];

			if (epubIn.read(header) != header.length) {
				report.error(null, 0, "cannot read header");
			} else {
				if (header[0] != 'P' && header[1] != 'K') {
					report.error(null, 0, "corrupted ZIP header");
				} else if (!CheckUtil.checkString(header, 30, "mimetype")) {
					report
							.error(null, 0,
									"mimetype entry missing or not the first in archive");
				} else if (!CheckUtil.checkString(header, 38,
						"application/epub+zip")) {
					report
							.error(null, 0,
									"mimetype contains wrong type (application/epub+zip expected)");
				}
			}

			epubIn.close();

			ZipFile zip = new ZipFile(epubFile);

			OCFPreflight checker = new OCFPreflight(zip, report);

			checker.runChecks();

			zip.close();

		} catch (IOException e) {
			report.error(null, 0, "I/O error: " + e.getMessage());
		}
		return warningCount == 0 && errorCount == 0;
	}
}
