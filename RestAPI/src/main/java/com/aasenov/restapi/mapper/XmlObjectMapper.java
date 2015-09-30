package com.aasenov.restapi.mapper;

import java.io.IOException;

import org.restlet.data.MediaType;
import org.restlet.representation.Representation;
import org.restlet.representation.StringRepresentation;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.AnnotationIntrospector;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.introspect.AnnotationIntrospectorPair;
import com.fasterxml.jackson.databind.type.TypeFactory;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.fasterxml.jackson.module.jaxb.JaxbAnnotationIntrospector;

/**
 * {@link com.aasenov.restapi.mapper.ObjectMapper} implementation that works with XMLs.
 */
public class XmlObjectMapper implements com.aasenov.restapi.mapper.ObjectMapper {

    /**
     * Object mapper used for XML formatting.
     */
    private static XmlMapper mXmlMapper;

    /**
     * Static instance of this mapper.
     */
    private static XmlObjectMapper sInstance;

    /**
     * Retrieve statically assigned instance of this mapper.
     * 
     * @return Initialized {@link XmlObjectMapper} object.
     */
    public static synchronized XmlObjectMapper getInstance() {
        if (sInstance == null) {
            // register both Jaxb and Json annotation introspectors.
            AnnotationIntrospector primaryIntrospector = new JaxbAnnotationIntrospector(TypeFactory.defaultInstance());
            AnnotationIntrospector secondaryIntropsector = new JaxbAnnotationIntrospector(TypeFactory.defaultInstance());
            AnnotationIntrospectorPair pair = new AnnotationIntrospectorPair(primaryIntrospector, secondaryIntropsector);

            mXmlMapper = new XmlMapper();
            mXmlMapper.enable(SerializationFeature.INDENT_OUTPUT);
            mXmlMapper.setAnnotationIntrospector(pair);

            sInstance = new XmlObjectMapper();
        }
        return sInstance;
    }

    /**
     * Private constructor to avoid initialization.
     */
    private XmlObjectMapper() {
    }

    @Override
    public Representation getRepresentation(Object value) throws MapperException {
        try {
            return new StringRepresentation(mXmlMapper.writeValueAsString(value), MediaType.APPLICATION_XML);
        } catch (JsonProcessingException e) {
            throw new MapperException(e);
        }
    }

    @Override
    public <T> T fromRepresentation(Representation value, Class<T> type) throws MapperException {
        try {
            return mXmlMapper.reader(type).readValue(value.getStream());
        } catch (IOException e) {
            throw new MapperException(e);
        }
    }

}
