/*******************************************************************************
 * Copyright (c) 2010 Earth System Grid Federation
 * ALL RIGHTS RESERVED. 
 * U.S. Government sponsorship acknowledged.
 * 
 * Redistribution and use in source and binary forms, with or without modification, are permitted provided that the following conditions are met:
 * 
 * Redistributions of source code must retain the above copyright notice, this list of conditions and the following disclaimer.
 * 
 * Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the following disclaimer in the documentation and/or other materials provided with the distribution.
 * 
 * Neither the name of the <ORGANIZATION> nor the names of its contributors may be used to endorse or promote products derived from this software without specific prior written permission.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. 
 * IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES 
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;  LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
 * HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 ******************************************************************************/
package esg.search.utils;

import java.io.IOException;
import java.io.StringReader;

import org.jdom.Document;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;

/**
 * Utility class to handle XML documents.
 */
public class XmlParser {
	

	/**
	 * Flag to validate the XML document.
	 */
	private final boolean validate;

	/**
	 * Constructor.
	 */
	public XmlParser(final boolean validate) {
		this.validate = validate;
	}
	
	/**
	 * Method to parse an XML string into a JDOM document.
	 * @param xml
	 * @return
	 * @throws IOException
	 * @throws JDOMException
	 */
	public Document parseString(final String xml) throws IOException, JDOMException {
		final StringReader sr = new StringReader(xml);
		return this.getBuilder().build(sr); 
	}

	/**
	 * Method to parse an XML file into a JDOM document.
	 * @param filepath
	 * @return
	 * @throws IOException
	 * @throws JDOMException
	 */
	public Document parseFile(final String filepath) throws IOException, JDOMException {
		return this.getBuilder().build(filepath);
	}
	
	/**
	 * Method to obtain an XML parser.
	 * Note: the XML parser is NOT thread-safe, so it must be re-instantiated every time.
	 */
	private SAXBuilder getBuilder() {
		
		final SAXBuilder builder = new SAXBuilder(); 
		builder.setValidation(validate); 
		builder.setIgnoringElementContentWhitespace(true); 
		return builder;
		
	}

}
