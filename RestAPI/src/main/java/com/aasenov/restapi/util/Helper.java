package com.aasenov.restapi.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.AnnotationIntrospector;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.introspect.AnnotationIntrospectorPair;
import com.fasterxml.jackson.databind.type.TypeFactory;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.fasterxml.jackson.module.jaxb.JaxbAnnotationIntrospector;

public class Helper {
    /**
     * Object mapper used for JSON formatting.
     */
    private static final ObjectMapper mJsonMapper;

    /**
     * Object mapper used for XML formatting.
     */
    private static final XmlMapper mXmlMapper;

    static {
        // register both Jaxb and Json annotation introspectors.
        AnnotationIntrospector primaryIntrospector = new JaxbAnnotationIntrospector(TypeFactory.defaultInstance());
        AnnotationIntrospector secondaryIntropsector = new JaxbAnnotationIntrospector(TypeFactory.defaultInstance());
        AnnotationIntrospectorPair pair = new AnnotationIntrospectorPair(primaryIntrospector, secondaryIntropsector);

        mXmlMapper = new XmlMapper();
        mXmlMapper.enable(SerializationFeature.INDENT_OUTPUT);
        mXmlMapper.setAnnotationIntrospector(pair);

        mJsonMapper = new ObjectMapper();
        mJsonMapper.setAnnotationIntrospector(pair);
    }

    /**
     * Serialize given object in JSON format.
     * 
     * @param objectToSerialize - object to be formatted.
     * @return JSON formatted string.
     * @throws JsonProcessingException - in case of error.
     */
    public static String formatJSONOutputResult(Object objectToSerialize) throws JsonProcessingException {
        return mJsonMapper.writeValueAsString(objectToSerialize);
    }

    /**
     * Serialize given object in XML form using JAXB.
     * 
     * @param objectToSerialize - object to be formatted.
     * @return XML formatted string.
     * @throws JsonProcessingException - in case of error.
     */
    public static String formatXMLOutputResult(Object objectToSerialize) throws JsonProcessingException {
        return mXmlMapper.writeValueAsString(objectToSerialize);
    }
}
