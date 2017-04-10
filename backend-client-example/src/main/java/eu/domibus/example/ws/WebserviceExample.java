package eu.domibus.example.ws;

import eu.domibus.common.model.org.oasis_open.docs.ebxml_msg.ebms.v3_0.ns.core._200704.Messaging;
import eu.domibus.example.ws.logging.MessageLoggingHandler;
import eu.domibus.plugin.webService.generated.*;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.xml.ws.BindingProvider;
import javax.xml.ws.Holder;
import javax.xml.ws.handler.Handler;
import javax.xml.ws.soap.SOAPFaultException;
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

    public void downloadMessage(DownloadMessageRequest downloadMessageRequest, Holder<DownloadMessageResponse> response, Holder<Messaging> ebmsHeader) {

        try {
            backendPort.downloadMessage(downloadMessageRequest, response, ebmsHeader);
        } catch (DownloadMessageFault downloadMessageFault) {
            LOG.error("Error while downloading message");
        } catch (SOAPFaultException soapFaultException) {
            LOG.warn(soapFaultException);
        }

    }

    public ListPendingMessagesResponse listPendingMessages() {
        return backendPort.listPendingMessages("");
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