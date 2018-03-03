package com.differencer.pi;
import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;

import org.xml.sax.Attributes;
import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.helpers.DefaultHandler;

public class UserHandler extends DefaultHandler  {
    public void warning(SAXParseException exception) throws SAXException {
        System.out.println("**Parsing Warning**" + "  Line:    " + exception.getLineNumber() + "  URI:     " + exception.getSystemId() + "  Message: " + exception.getMessage()); 
        throw new SAXException("Warning encountered");
    }
    public void error(SAXParseException exception) throws SAXException {
        System.out.println("**Parsing Error**" + "  Line:    " + exception.getLineNumber() + "  URI:     " + exception.getSystemId() + "  Message: " + exception.getMessage());        
        throw new SAXException("Error encountered");
    }
    public void fatalError(SAXParseException exception) throws SAXException {
        System.out.println("**Parsing Fatal Error**" + "  Line:    " + exception.getLineNumber() + "  URI:     " + exception.getSystemId() + "  Message: " + exception.getMessage());        
        throw new SAXException("Fatal Error encountered");
    }
   public void startElement(String uri, String localName, String qName, Attributes attributes)
      throws SAXException {
        System.out.println("Start Element: " + qName);
   }
   public void endElement(String uri, String localName, String qName) throws SAXException {
        System.out.println("End Element: " + qName);
   }
   public void characters(char ch[], int start, int length) throws SAXException {
        System.out.println("Element: " + " at " + start + " length " + length + " :" + new String(ch, start, length));
   }
}