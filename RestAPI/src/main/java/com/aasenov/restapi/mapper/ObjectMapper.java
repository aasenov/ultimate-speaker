package com.aasenov.restapi.mapper;

import org.restlet.representation.Representation;

/**
 * Interface defining methods for objects mapping to/from representation..
 */
public interface ObjectMapper {

    /**
     * Serialize given object and prepare a representation, ready for sending.
     * 
     * @param value - object to serialize.
     * @return Constructed representation, containing properly formatted object.
     * @throws MapperException - in case of error.
     */
    public Representation getRepresentation(Object value) throws MapperException;

    /**
     * Deserialize object with given type from passed representation.
     * 
     * @param value - representation to retrieve value from.
     * @param type - type of object to deserialize.
     * @return Object red.
     * @throws MapperException in case of error.
     */
    public <T> T fromRepresentation(Representation value, Class<T> type) throws MapperException;
}
