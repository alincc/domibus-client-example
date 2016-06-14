package eu.domibus.example.ws;/*
 * Copyright 2014 e-CODEX Project
 *
 * Licensed under the EUPL, Version 1.1 or – as soon they
 * will be approved by the European Commission - subsequent
 * versions of the EUPL (the "Licence");
 * You may not use this work except in compliance with the
 * Licence.
 * You may obtain a copy of the Licence at:
 * http://ec.europa.eu/idabc/eupl5
 * Unless required by applicable law or agreed to in
 * writing, software distributed under the Licence is
 * distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied.
 * See the Licence for the specific language governing
 * permissions and limitations under the Licence.
 */

import backend.ecodex.org._1_1.*;
import eu.domibus.example.ws.logging.MessageLoggingHandler;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.oasis_open.docs.ebxml_msg.ebms.v3_0.ns.core._200704.Messaging;

import javax.xml.ws.BindingProvider;
import javax.xml.ws.Holder;
import javax.xml.ws.handler.Handler;
import javax.xml.ws.soap.SOAPFaultException;
import java.util.Collection;
import java.util.List;

/**
 * TODO: add class description
 */

public class WebserviceExample {

    private static final Log LOG = LogFactory.getLog(WebserviceExample.class);
    private static final String DEFAULT_WEBSERVICE_LOCATION = "http://localhost:8080/domibus/services/backend";

    private BackendInterface backendPort;


    public WebserviceExample() {
        this(DEFAULT_WEBSERVICE_LOCATION);
    }

    public WebserviceExample(String webserviceLocation) {
        backendPort = configureDomibusDefaultWSPort(webserviceLocation);
    }

    public SendResponse sendMessage(SendRequest sendRequest, Messaging ebmsHeader) {
        SendResponse response = null;

        try {
            response = backendPort.sendMessage(sendRequest, ebmsHeader);
        } catch (SendMessageFault sendMessageFault) {
            LOG.error("Error during message transmission", sendMessageFault);
        }

        return response;
    }

    public void downloadMessage(DownloadMessageRequest downloadMessageRequest, DownloadMessageResponse response, Messaging ebmsHeader) {
        Holder<DownloadMessageResponse> responseHolder = new Holder<DownloadMessageResponse>();
        Holder<Messaging> messagingHolder = new Holder<Messaging>();

        try {
            backendPort.downloadMessage(downloadMessageRequest, responseHolder, messagingHolder);
        } catch (DownloadMessageFault downloadMessageFault) {
            LOG.error("Error while downloading message");
        } catch (SOAPFaultException soapFaultException) {
            LOG.warn(soapFaultException);
        }

        response = responseHolder.value;
        ebmsHeader = messagingHolder.value;
    }

    public ListPendingMessagesResponse listPendingMessages() {
        return backendPort.listPendingMessages(null);
    }

    public MessageStatus getMessageStatus(GetStatusRequest messageStatusRequest) {
        return backendPort.getMessageStatus(messageStatusRequest);
    }

    public ErrorResultImplArray getMessageErrors(GetErrorsRequest messageErrorsRequest) {
        return backendPort.getMessageErrors(messageErrorsRequest);
    }


    private BackendInterface configureDomibusDefaultWSPort(String webserviceLocation) {
        if (webserviceLocation == null || webserviceLocation.isEmpty()) {
            throw new IllegalArgumentException("No webservice location specified");
        }

        BackendService11 backendService = new BackendService11();

        BackendInterface backendPort = backendService.getBACKENDPORT();

        BindingProvider bindingProvider = (BindingProvider) backendPort;

        List<Handler> handlers = bindingProvider.getBinding().getHandlerChain();
        handlers.add(new MessageLoggingHandler());
        bindingProvider.getBinding().setHandlerChain(handlers);
        bindingProvider.getRequestContext().put(BindingProvider.ENDPOINT_ADDRESS_PROPERTY, webserviceLocation);


        return backendPort;
    }
}