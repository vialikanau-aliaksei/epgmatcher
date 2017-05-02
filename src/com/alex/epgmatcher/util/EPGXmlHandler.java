package com.alex.epgmatcher.util;

import com.alex.epgmatcher.beans.EPG;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import java.util.ArrayList;
import java.util.List;

/**
 * Parser for EPG xml.
 * This class parsing only {@link EPGXmlHandler#ELEMENT_CHANNEL},
 * {@link EPGXmlHandler#ELEMENT_DISPLAY_NAME}, {@link EPGXmlHandler#ID_ATTRIBUTE} and
 * {@link EPGXmlHandler#LANG_ATTRIBUTE} tags and attributes.
 * Created by Alex on 18.04.2017.
 */
public class EPGXmlHandler extends DefaultHandler {

    private final static String ELEMENT_CHANNEL = "channel";
    private final static String ELEMENT_DISPLAY_NAME = "display-name";
    private static final String ID_ATTRIBUTE = "id";
    private static final String LANG_ATTRIBUTE = "lang";

    private final StringBuilder content = new StringBuilder();


    private List<EPG> resultList;
    private EPG epgChannel;

    public EPGXmlHandler() {
        super();
    }

    @Override
    public void startDocument() throws SAXException {
        resultList = new ArrayList<>();
    }

    @Override
    public void characters(char[] ch, int start, int length) throws SAXException {
        content.append(ch, start, length);
    }

    @Override
    public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
        String elementName = localName != null ? localName.toLowerCase() : "";
        content.setLength(0);

        if (ELEMENT_CHANNEL.equals(elementName)) {
            epgChannel = new EPG();
            epgChannel.setId(attributes.getValue(ID_ATTRIBUTE));
        } else if (ELEMENT_DISPLAY_NAME.equals(elementName)) {
            if (epgChannel != null) {
                epgChannel.setLang(attributes.getValue(LANG_ATTRIBUTE));
            }
        }
    }

    @Override
    public void endElement(String uri, String localName, String qName) throws SAXException {
        String elementName = localName != null ? localName.toLowerCase() : "";
        if (epgChannel != null) {
            if (ELEMENT_DISPLAY_NAME.equals(elementName)) {
                epgChannel.setName(content.toString());
                resultList.add(epgChannel);
            } else {
                if (ELEMENT_CHANNEL.equals(elementName)) {
                    epgChannel = null;
                }
            }
        }
    }

    /**
     * @return list of {@link EPG} after EPG parsing.
     */
    public List<EPG> getResults() {
        return resultList;
    }
}
