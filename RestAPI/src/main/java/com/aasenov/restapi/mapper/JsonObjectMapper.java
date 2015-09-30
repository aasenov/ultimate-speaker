package com.aasenov.restapi.mapper;

import java.io.IOException;

import org.restlet.data.MediaType;
import org.restlet.representation.Representation;
import org.restlet.representation.StringRepresentation;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.AnnotationIntrospector;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.introspect.AnnotationIntrospectorPair;
import com.fasterxml.jackson.databind.type.TypeFactory;
import com.fasterxml.jackson.module.jaxb.JaxbAnnotationIntrospector;

/**
 * {@link com.aasenov.restapi.mapper.ObjectMapper} implementation that works with JSON objects.
 */
public class JsonObjectMapper implements com.aasenov.restapi.mapper.ObjectMapper {

    /**
     * Object mapper used for JSON formatting.
     */
    private static ObjectMapper mJsonMapper;

    /**
     * Static instance of this mapper.
     */
    private static JsonObjectMapper sInstance;

    /**
     * Retrieve statically assigned instance of this mapper.
     * 
     * @return Initialized {@link JsonObjectMapper} object.
     */
    public static synchronized JsonObjectMapper getInstance() {
        if (sInstance == null) {
            // register both Jaxb and Json annotation introspectors.
            AnnotationIntrospector primaryIntrospector = new JaxbAnnotationIntrospector(TypeFactory.defaultInstance());
            AnnotationIntrospector secondaryIntropsector = new JaxbAnnotationIntrospector(TypeFactory.defaultInstance());
            AnnotationIntrospectorPair pair = new AnnotationIntrospectorPair(primaryIntrospector, secondaryIntropsector);

            mJsonMapper = new ObjectMapper();
            mJsonMapper.setAnnotationIntrospector(pair);

            sInstance = new JsonObjectMapper();
        }
        return sInstance;
    }

    /**
     * Private constructor to avoid initialization.
     */
    private JsonObjectMapper() {
    }

    @Override
    public Representation getRepresentation(Object value) throws MapperException {
        try {
            return new StringRepresentation(mJsonMapper.writeValueAsString(value), MediaType.APPLICATION_JSON);
        } catch (JsonProcessingException e) {
            throw new MapperException(e);
        }
    }

    @Override
    public <T> T fromRepresentation(Representation value, Class<T> type) throws MapperException {
        try {
            return mJsonMapper.reader(type).readValue(value.getStream());
        } catch (IOException e) {
            throw new MapperException(e);
        }
    }

}
