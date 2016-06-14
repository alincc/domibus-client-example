package eu.domibus.example.ws.logging;/*
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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.xml.namespace.QName;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPMessage;
import javax.xml.ws.handler.MessageContext;
import javax.xml.ws.handler.soap.SOAPHandler;
import javax.xml.ws.handler.soap.SOAPMessageContext;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Set;

/**
 * TODO: add class description
 */
public class MessageLoggingHandler implements SOAPHandler<SOAPMessageContext> {

    private static final Log LOG = LogFactory.getLog(MessageLoggingHandler.class);

    @Override
    public Set<QName> getHeaders() {
        return null;
    }

    @Override
    public boolean handleMessage(SOAPMessageContext context) {

        boolean isRequest = (boolean) context.get(MessageContext.MESSAGE_OUTBOUND_PROPERTY);

        if (isRequest) {
            LOG.info("======== Logging Request ========");
        } else {
            LOG.info("======== Logging Response ========");
        }

        logSOAPMessage(context.getMessage());

        //continue with message processing
        return true;
    }

    @Override
    public boolean handleFault(SOAPMessageContext context) {

        LOG.info("======== Logging SOAPFault ========");
        logSOAPMessage(context.getMessage());

        return true;
    }

    @Override
    public void close(MessageContext context) {

    }

    private void logSOAPMessage(SOAPMessage message) {
        String output = null;

        try {
            output = convertToString(message);
        } catch (IOException e) {
            LOG.error("", e);
        } catch (SOAPException e) {
            LOG.error("", e);
        }

        if(output != null) {
            LOG.info(output);
        }
    }

    private String convertToString(SOAPMessage message) throws IOException, SOAPException {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        message.writeTo(stream);
        String convertedMessage = new String(stream.toByteArray(), "utf-8");

        return convertedMessage;
    }
}
