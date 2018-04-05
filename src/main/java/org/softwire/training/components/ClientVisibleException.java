package org.softwire.training.components;

import org.softwire.training.api.ClientVisibleError;

import javax.ws.rs.core.Response;

public class ClientVisibleException extends Exception {

    private final ClientVisibleError clientVisibleError;

    private ClientVisibleException(ClientVisibleError clientVisibleError) {
        super(clientVisibleError.toString());
        this.clientVisibleError = clientVisibleError;
    }

    public ClientVisibleError getClientVisibleError() {
        return clientVisibleError;
    }

    public static ClientVisibleException construct(Response.Status status, String clientVisibleDetail) {
        return new ClientVisibleException(new ClientVisibleError(status.getStatusCode(), clientVisibleDetail));
    }
}
