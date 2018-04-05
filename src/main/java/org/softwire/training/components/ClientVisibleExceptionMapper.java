package org.softwire.training.components;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;

public class ClientVisibleExceptionMapper implements ExceptionMapper<ClientVisibleException> {
    private static final Logger LOGGER = LoggerFactory.getLogger(ClientVisibleExceptionMapper.class);

    @Override
    public Response toResponse(ClientVisibleException exception) {
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("Returning ClientVisibleException: {}", exception.toString());
        }
        return Response
                .status(exception.getClientVisibleError().getCode())
                .type(MediaType.APPLICATION_JSON_TYPE)
                .entity(exception.getClientVisibleError())
                .build();
    }
}
