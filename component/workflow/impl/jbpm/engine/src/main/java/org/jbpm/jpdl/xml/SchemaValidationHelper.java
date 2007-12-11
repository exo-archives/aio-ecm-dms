/*
 * Copyright (C) 2003-2007 eXo Platform SAS.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Affero General Public License
 * as published by the Free Software Foundation; either version 3
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, see<http://www.gnu.org/licenses/>.
 */
package org.jbpm.jpdl.xml;

import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.io.SAXReader;
import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.helpers.DefaultHandler;

/**
 * Validate an XML document using JAXP techniques
 * and an XML Schema.  This helper class wraps the processing of a schema 
 * to aid in schema validation throughout 
 * the product.
 * 
 * @author Jim Rigsbee
 *
 */
public class SchemaValidationHelper
{
	private static final String JAXP_SCHEMA_LANGUAGE =
                    "http://java.sun.com/xml/jaxp/properties/schemaLanguage";
	private static final String W3C_XML_SCHEMA =
		            "http://www.w3.org/2001/XMLSchema";
  
  /*
	private static final String JAXP_SCHEMA_SOURCE = 
		            "http://java.sun.com/xml/jaxp/properties/schemaSource";
  */
	
	private Reader reader;
	private String schemaName;
	private String subject;
	private List errors = new ArrayList();
	private Document document;
	private boolean valid = true;
	
	/**
	 * Constructs the schema validation helper and prepares it for reading
	 * the XML document.
	 * 
	 * @param reader input reader containing the contents of the XML document
	 * @param schemaName physical file name of the XML schema to be used for validation 
	 * @param subject descriptive name of file we are parsing to put in error messages
	 */
	public SchemaValidationHelper(Reader reader, String schemaName, String subject)
	{
		this.reader = reader;
		this.schemaName = schemaName;
		this.subject = subject;
	}
	
	/**
	 * Performs the schema validation on the document contents in the reader.  Full schema
	 * checking is performed using the 2001 XML schema for schemas from W3C.  A document
	 * will be validated only if it is given a namespace in its root element.
	 * 
	 * @return true - if the document is valid, false - if not valid, use {@link #getProblems()}
	 * to retrieve a list of {@link Problem} objects detailing the schema validation errors.
	 */
	public boolean isValid()
	{
		try
		{
						
			SAXReader saxReader = new SAXReader( );
			
			// Set custom handler and entity resolver (schema lookup)
			saxReader.setErrorHandler( new Handler() );
			saxReader.setEntityResolver( new Resolver() );
					
			try
			{
				// Use XML Schema validation 
				saxReader.setFeature("http://apache.org/xml/features/validation/schema", true);
				// Do full type checking
				saxReader.setFeature("http://apache.org/xml/features/validation/schema-full-checking", true);
				// Only do schema validation if a schema is specified as a namespace
				saxReader.setFeature("http://apache.org/xml/features/validation/dynamic", true);
				// Set the language level for the schema
				saxReader.setProperty(JAXP_SCHEMA_LANGUAGE, W3C_XML_SCHEMA);
				// Force schema location to invoke entity resolver
				saxReader.setProperty("http://apache.org/xml/properties/schema/external-schemaLocation", "http://jbpm.org/3/jpdl jpdl-3.0.xsd");
			}
			catch(SAXException se)
			{
				log.warn("Unable to validate using schema.  Make sure Xerces is first in your class path.");
			}
			
			document = saxReader.read( reader );
			
		}		
		catch(DocumentException de)
		{
			log.error("Parsing problems", de);
		}
		
		return valid;
	}
	
	/**
	 * 
	 * @return a {@link java.util.List} of {@link Problem} objects detailing any errors
	 * found during schema validation
	 */
	public List getProblems()
	{
		return errors;
	}
	
	/**
	 * Accessor for the DOM model of the document.
	 * 
	 * @return {@link Document} representing the DOM model of the validated XML document
	 */
	public Document getDocument()
	{
		return document;
	}
	
	/**
	 * Specialized schema validation handler which creates {@link Problem} objects
	 * for each error encountered. 
	 *
	 */
	class Handler extends DefaultHandler
	{
		public void warning(SAXParseException pe)
		{
			errors.add(
					new Problem( Problem.LEVEL_WARNING,
			         subject + " line " + pe.getLineNumber() + 
			         ": " + pe.getMessage() ) );					           
		}
		
		public void error(SAXParseException pe)
		{
			errors.add(
					new Problem( Problem.LEVEL_ERROR,
			         subject + " line " + pe.getLineNumber() + 
			         ": " + pe.getMessage() ) );	
			valid = false;
		}
		
		public void fatalError(SAXParseException pe)
		{
			errors.add(
					new Problem( Problem.LEVEL_FATAL,
			         subject + " line " + pe.getLineNumber() + 
			         ": " + pe.getMessage() ) );	
			valid = false;
		}
	}
	
	/**
	 * Specialized entity resolver to load the schema from the same
	 * path as the validation class.
	 *
	 */
	class Resolver implements EntityResolver
	{
		public InputSource resolveEntity(String publicId, String SystemId)
	    		throws SAXException, IOException
	    {							
			return new InputSource( this.getClass()
					                    .getResourceAsStream( schemaName )
					              );
		}
	
	}
	
	private static final Log log = LogFactory.getLog( SchemaValidationHelper.class );
}
