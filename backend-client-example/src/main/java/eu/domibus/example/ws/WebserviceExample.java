package eu.domibus.example.ws;

import eu.domibus.example.ws.logging.MessageLoggingHandler;
import eu.domibus.plugin.webService.generated.BackendInterface;
import eu.domibus.plugin.webService.generated.BackendService11;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.xml.namespace.QName;
import javax.xml.ws.BindingProvider;
import javax.xml.ws.handler.Handler;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;


public class WebserviceExample {

    private static final Log LOG = LogFactory.getLog(WebserviceExample.class);

    private String wsdl;
    private static final String DEFAULT_WEBSERVICE_LOCATION = "http://localhost:8080/domibus/services/backend?wsdl";


    public WebserviceExample()  {
        this(DEFAULT_WEBSERVICE_LOCATION);
    }

    public WebserviceExample(String webserviceLocation)  {
        this.wsdl = webserviceLocation;
    }

    public BackendInterface getPort() throws MalformedURLException {
        if (wsdl == null || wsdl.isEmpty()) {
            throw new IllegalArgumentException("No webservice location specified");
        }

        BackendService11 backendService = new BackendService11(new URL(wsdl),  new QName("http://org.ecodex.backend/1_1/", "BackendService_1_1"));
        BackendInterface backendPort = backendService.getBACKENDPORT();

        BindingProvider bindingProvider = (BindingProvider) backendPort;

        List<Handler> handlers = bindingProvider.getBinding().getHandlerChain();
        handlers.add(new MessageLoggingHandler());//disable this if working with large files
        bindingProvider.getBinding().setHandlerChain(handlers);

        return backendPort;
    }

}