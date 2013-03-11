/*******************************************************************************
 * Copyright (c) 2011 Earth System Grid Federation
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

import java.io.File;
import java.io.IOException;
import java.io.StringWriter;

import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.jdom.JDOMException;

/** 
 * Utility class for writing XML to output. It contains only static methods.
 */
public class Serializer {

   /** 
    * Method to write a DOM document to file.
    * NOTE: the XML will be re-formatted for pretty printing.
    * @param root the root element of the DOM document
    * @param outputFile the intended XML output file
    * @exception IOException
    */
   public static void DOMtoFile(org.w3c.dom.Element root, String outputFile) 
                 throws java.io.IOException,TransformerConfigurationException, TransformerException  {
        
        final Source source = new DOMSource(root);
        
        final File file = new File(outputFile);
        final Result result = new StreamResult(file);
        
        final Transformer xformer = TransformerFactory.newInstance().newTransformer();
        xformer.transform(source, result);
        
   }
   
   public static String DOMtoString(org.w3c.dom.Element root) 
   		  throws java.io.IOException,TransformerConfigurationException, TransformerException {
	   
	   final Source source = new DOMSource(root);
	   
	   final StringWriter writer = new StringWriter();
	   final Result result = new StreamResult(writer);
	   
	   final Transformer xformer = TransformerFactory.newInstance().newTransformer();
	   xformer.transform(source, result);
	   writer.close();
	   
       return writer.toString();
	   
   }

   /** 
    * Method to write a DOM document to System.out 
    * @param root the root element of the DOM document
    * @exception IOException
    */
   public static void DOMout(org.w3c.dom.Element root) 
          throws java.io.IOException,TransformerConfigurationException, TransformerException {

	   final Source source = new DOMSource(root);
	   
	   final StringWriter writer = new StringWriter();
	   final Result result = new StreamResult(System.out);
	   
	   final Transformer xformer = TransformerFactory.newInstance().newTransformer();
	   xformer.transform(source, result);
	   writer.close();

   } // DOMout

   /**
    * Method to write a JDOM document to file
    * @param jdoc the JDOM document
    * @param outputFile the intended xml output file
    * @exception IOException
    */
   public static void JDOMtoFile(org.jdom.Document jdoc, String outputFile)
                      throws java.io.IOException {

      org.jdom.output.XMLOutputter outputter = getXMLOutputter();
      java.io.FileWriter writer = new java.io.FileWriter(outputFile);
      outputter.output(jdoc, writer);
      writer.close();

   } // JDOMtoFile()

   /** 
    * Method to deserialize a JDOM document from a  String and write it to a file 
    * @param xml the String containig the serialized xml document
    * @param outputFile the intended xml output file
    * @exception IOException
    * @exception JDOMException
    */
   public static void JDOMtoFile(String xml, String outputFile)
                      throws java.io.IOException, org.jdom.JDOMException {

      java.io.StringReader sr = new java.io.StringReader(xml);
      org.jdom.input.SAXBuilder sb = new org.jdom.input.SAXBuilder();
      org.jdom.Document jdoc =  sb.build(sr);
      org.jdom.output.XMLOutputter outputter = getXMLOutputter();
      java.io.FileWriter writer = new java.io.FileWriter(outputFile);
      outputter.output(jdoc, writer);
      writer.close();

   } // JDOMtoFile
   
   private static org.jdom.output.XMLOutputter getXMLOutputter() {
   	org.jdom.output.Format format = org.jdom.output.Format.getPrettyFormat();
   	format.setLineSeparator(System.getProperty("line.separator"));
   	org.jdom.output.XMLOutputter outputter = new org.jdom.output.XMLOutputter(format);
   	return outputter;
   } // getXMLOutputter()

   /**
    * Method to write a JDOM document to System.out
    * @param jdoc the JDOM document
    * @exception IOException
    */
   public static void JDOMout(org.jdom.Document jdoc) 
                      throws java.io.IOException {

      org.jdom.output.XMLOutputter outputter = getXMLOutputter();
      outputter.output(jdoc, System.out);

   } // JDOMout()

   /** 
    * Method to deserialize a JDOM document from a  String and write it to System.out
    * @param xml the String containig the serialized xml document
    * @exception IOException
    * @exception JDOMException
    */
   public static void JDOMout(String xml) 
          throws java.io.IOException, org.jdom.JDOMException {

      java.io.StringReader sr = new java.io.StringReader(xml);
      org.jdom.input.SAXBuilder sb = new org.jdom.input.SAXBuilder();
      org.jdom.Document jdoc =  sb.build(sr);
      org.jdom.output.XMLOutputter outputter = getXMLOutputter();
      outputter.output(jdoc, System.out);

   } // JDOMtoFile

  /**
   * Method to serialize a JDOM document to a string representation
   * @param jdoc : XML document as org.jdom.Document object
   */
  public static String JDOMtoString(org.jdom.Document jdoc) throws org.jdom.JDOMException {
    return JDOMtoString(jdoc, false); // compact form
  } // JDOMtoString()
  
  /**
   * Method to serialize a JDOM document to a string representation
   * @param jdoc : XML document as org.jdom.Document object
   * @param pretty : true to output XML in pretty format
   */
  public static String JDOMtoString(org.jdom.Document jdoc, boolean pretty) throws org.jdom.JDOMException {
  	org.jdom.output.Format format = (pretty ? org.jdom.output.Format.getPrettyFormat() 
  			                                    : org.jdom.output.Format.getCompactFormat());
  	  org.jdom.output.XMLOutputter outputter = new org.jdom.output.XMLOutputter(format);
    return outputter.outputString(jdoc);
  } // JDOMtoString()  

  /**
   * Method to serialize a JDOM element to a string representation
   * @param jelem : XML element as JDOM Element object
   */
  public static String JDOMtoString(org.jdom.Element jelem) throws org.jdom.JDOMException {
    //org.jdom.output.XMLOutputter outputter= new org.jdom.output.XMLOutputter(org.jdom.output.Format.getCompactFormat());
    org.jdom.output.XMLOutputter outputter= new org.jdom.output.XMLOutputter();
    return outputter.outputString(jelem);
  } // JDOMtoString()

  /** Method to serialize a javax.xml.transform.Source object to a String */
  public static String SourceToString(javax.xml.transform.Source source) throws javax.xml.transform.TransformerException {

   javax.xml.transform.TransformerFactory tFactory = javax.xml.transform.TransformerFactory.newInstance();
   javax.xml.transform.Transformer transformer = tFactory.newTransformer(); // copies source --> result
   // execute transformation
   java.io.CharArrayWriter caw = new java.io.CharArrayWriter();
   javax.xml.transform.stream.StreamResult result = new javax.xml.transform.stream.StreamResult(caw);
   transformer.transform(source, result);
   return caw.toString();

  } // SourceToString()
  
  /** Method to escape XML for HTML display */
  public static String toHTML(String xml) throws Exception {
  		return xml.replaceAll("&","&amp;").replaceAll("<","&lt;").replaceAll(">","&gt;");
  } // toHTML()

} // class Serializer
