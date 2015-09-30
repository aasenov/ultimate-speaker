package com.aasenov.restapi.mapper;

import org.apache.log4j.Logger;
import org.restlet.Request;
import org.restlet.data.MediaType;
import org.restlet.data.Preference;
import org.restlet.representation.Representation;

import com.aasenov.restapi.resources.UserResource;

/**
 * This class provide correct {@link ObjectMapper} instances based on some negotiation between client and server.
 */
public class MapperProvider {

    /**
     * Logger instance.
     */
    private static Logger sLog = Logger.getLogger(UserResource.class);

    /**
     * Retrieve object mapper based on user request.
     * 
     * @param request - request to retrieve media types from.
     * @return Constructed object mapper, based on accepted media types.
     */
    public static ObjectMapper getMapper(Request request) {
        for (Preference<MediaType> type : request.getClientInfo().getAcceptedMediaTypes()) {
            if (MediaType.APPLICATION_JSON.equals(type.getMetadata())) {
                return JsonObjectMapper.getInstance();
            } else if (MediaType.APPLICATION_XML.equals(type.getMetadata())
                    || MediaType.TEXT_XML.equals(type.getMetadata())) {
                return XmlObjectMapper.getInstance();
            }
        }
        // Json mapper is default
        sLog.error("No valid media type found. Return default mapper!");
        return JsonObjectMapper.getInstance();
    }

    /**
     * Retrieve object mapper based on representation.
     * 
     * @param repr - representation to get media type from.
     * @return Constructed object mapper, based on representation media type.
     */
    public static ObjectMapper getMapper(Representation repr) {
        if (MediaType.APPLICATION_JSON.equals(repr.getMediaType())) {
            return JsonObjectMapper.getInstance();
        } else if (MediaType.APPLICATION_XML.equals(repr.getMediaType())
                || MediaType.TEXT_XML.equals(repr.getMediaType())) {
            return XmlObjectMapper.getInstance();
        }

        // Json mapper is default
        sLog.error("No valid media type found. Return default mapper!");
        return JsonObjectMapper.getInstance();

    }
}
