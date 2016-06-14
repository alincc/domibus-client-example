/*
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

package eu.domibus.example;

import backend.ecodex.org._1_1.*;
import eu.domibus.example.ws.WebserviceExample;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.oasis_open.docs.ebxml_msg.ebms.v3_0.ns.core._200704.*;
import org.oasis_open.docs.ebxml_msg.ebms.v3_0.ns.core._200704.ObjectFactory;

import java.util.UUID;

/**
 * TODO: add class description
 */
public class Start {

    private static final Log LOG = LogFactory.getLog(Start.class);

    private static final String FROM_ROLE = "http://docs.oasis-open.org/ebxml-msg/ebms/v3.0/ns/core/200704/initiator";
    private static final String TO_ROLE = "http://docs.oasis-open.org/ebxml-msg/ebms/v3.0/ns/core/200704/responder";

    private static final String FROM_PARTY_ID_VALUE = "urn:oasis:names:tc:ebcore:partyid-type:unregistered:domibus-blue";
    private static final String TO_PARTY_ID_VALUE = "urn:oasis:names:tc:ebcore:partyid-type:unregistered:domibus-red";

    private static final String HREF_PAYLOAD_1 = "#payload_1";
    private static final String HREF_PAYLOAD_2 = "payload_2";

    private static final String PARTPROPERTY_MIMETYPE_NAME = "MimeType";
    private static final String PARTPROPERTY_MIMETYPE_VALUE = "application/xml";

    private static final String SERVICE_VALUE = "bdx:noprocess";
    private static final String SERVICE_TYPE = "tc1";
    private static final String ACTION = "TC1Leg1";

    private static final String MESSAGEPROPERTY_ORIGINAL_SENDER_NAME = "originalSender";
    private static final String MESSAGEPROPERTY_ORIGINAL_SENDER_VALUE = "urn:oasis:names:tc:ebcore:partyid-type:unregistered:C1";
    private static final String MESSAGEPROPERTY_FINAL_RECIPIENT_NAME = "finalRecipient";
    private static final String MESSAGEPROPERTY_FINAL_RECIPIENT_VALUE = "urn:oasis:names:tc:ebcore:partyid-type:unregistered:C4";


    private static WebserviceExample webserviceExample = new WebserviceExample();

    public static void main(String[] args) {

        LOG.info("====================================");
        LOG.info("====================================");
        sendMessage();

        LOG.info("====================================");
        LOG.info("====================================");
        downloadNextMessage();

        LOG.info("====================================");
        LOG.info("====================================");
        downloadMessageWithID();

        LOG.info("====================================");
        LOG.info("====================================");
        listPendingMessages();

        LOG.info("====================================");
        LOG.info("====================================");
        listMessageStatusForMessageWithID();

        LOG.info("====================================");
        LOG.info("====================================");
        listErrorsForMessageWithID();

    }

    private static void sendMessage() {
        LOG.info("Call Webservice Method: sendMessage");

        PayloadType payload_1 = new PayloadType();
        payload_1.setPayloadId(HREF_PAYLOAD_1);
        payload_1.setValue("<root><payload_1></payload_1></root>".getBytes());

        PayloadType payload_2 = new PayloadType();
        payload_2.setPayloadId(HREF_PAYLOAD_2);
        payload_2.setValue("<root><payload_2></payload_2></root>".getBytes());

        SendRequest sendRequest = new SendRequest();
        sendRequest.setBodyload(payload_1);
        sendRequest.getPayload().add(payload_2);

        //=========== Create Messaging ===========
        ObjectFactory objectFactory = new ObjectFactory();

        PartyId fromPartyId = objectFactory.createPartyId();
        fromPartyId.setValue(FROM_PARTY_ID_VALUE);

        PartyId toPartyId = objectFactory.createPartyId();
        toPartyId.setValue(TO_PARTY_ID_VALUE);

        From from = objectFactory.createFrom();
        from.setRole(FROM_ROLE);
        from.getPartyId().add(fromPartyId);

        To to = objectFactory.createTo();
        to.setRole(TO_ROLE);
        to.getPartyId().add(toPartyId);

        PartyInfo partyInfo = objectFactory.createPartyInfo();
        partyInfo.setFrom(from);
        partyInfo.setTo(to);

        Property mimeProperty = objectFactory.createProperty();
        mimeProperty.setName(PARTPROPERTY_MIMETYPE_NAME);
        mimeProperty.setValue(PARTPROPERTY_MIMETYPE_VALUE);

        PartProperties examplePartProperties = objectFactory.createPartProperties();
        examplePartProperties.getProperty().add(mimeProperty);

        PartInfo partInfo_payload1_inbody = objectFactory.createPartInfo();
        //this payload will be located in the soapBody of the message send by the MSH
        partInfo_payload1_inbody.setHref(HREF_PAYLOAD_1);
        //userdefined property for example mimetype
        partInfo_payload1_inbody.setPartProperties(examplePartProperties);

        PartInfo partInfo_payload2_swa = objectFactory.createPartInfo();
        partInfo_payload2_swa.setHref(HREF_PAYLOAD_2);
        partInfo_payload2_swa.setPartProperties(examplePartProperties);

        PayloadInfo payloadInfo = objectFactory.createPayloadInfo();
        payloadInfo.getPartInfo().add(partInfo_payload1_inbody);
        payloadInfo.getPartInfo().add(partInfo_payload2_swa);

        Service service = objectFactory.createService();
        service.setValue(SERVICE_VALUE);
        service.setType(SERVICE_TYPE);

        CollaborationInfo collaborationInfo = objectFactory.createCollaborationInfo();
        collaborationInfo.setAction(ACTION);
        collaborationInfo.setService(service);

        Property originalSender = objectFactory.createProperty();
        originalSender.setName(MESSAGEPROPERTY_ORIGINAL_SENDER_NAME);
        originalSender.setValue(MESSAGEPROPERTY_ORIGINAL_SENDER_VALUE);

        Property finalRecipient = objectFactory.createProperty();
        finalRecipient.setName(MESSAGEPROPERTY_FINAL_RECIPIENT_NAME);
        finalRecipient.setValue(MESSAGEPROPERTY_FINAL_RECIPIENT_VALUE);

        MessageProperties messageProperties = objectFactory.createMessageProperties();
        messageProperties.getProperty().add(originalSender);
        messageProperties.getProperty().add(finalRecipient);

        UserMessage userMessage = objectFactory.createUserMessage();
        userMessage.setCollaborationInfo(collaborationInfo);
        userMessage.setPartyInfo(partyInfo);
        userMessage.setPayloadInfo(payloadInfo);
        userMessage.setMessageProperties(messageProperties);

        Messaging messaging = objectFactory.createMessaging();
        messaging.setUserMessage(userMessage);
        //============================================

        SendResponse response = webserviceExample.sendMessage(sendRequest, messaging);

        LOG.info("Message was sent to the gateway. MessageId: " + response.getMessageID());
    }

    private static void downloadNextMessage() {
        LOG.info("Call Webservice Method: downloadMessage (without messageId)");

        DownloadMessageRequest downloadMessageRequest = new DownloadMessageRequest();

        DownloadMessageResponse response = null;
        Messaging ebMSHeaderResponse = null;


        webserviceExample.downloadMessage(downloadMessageRequest, response, ebMSHeaderResponse);

        if (response == null || ebMSHeaderResponse == null) {
            return;
        }

        LOG.info("Message with messageId " + ebMSHeaderResponse.getUserMessage().getMessageInfo().getMessageId() + " received");
        LOG.info("Number of payloads attached to this message: " + response.getPayload().size());
    }

    private static void downloadMessageWithID() {
        LOG.info("Call Webservice Method: downloadMessage (with specific messageId as parameter)");

        DownloadMessageRequest downloadMessageRequest = new DownloadMessageRequest();

        //this is the messageId of the message you want to download
        downloadMessageRequest.setMessageID(UUID.randomUUID().toString());

        DownloadMessageResponse response = null;
        Messaging ebMSHeaderResponse = null;


        webserviceExample.downloadMessage(downloadMessageRequest, response, ebMSHeaderResponse);

        if (response == null || ebMSHeaderResponse == null) {
            return;
        }

        LOG.info("Message with messageId " + ebMSHeaderResponse.getUserMessage().getMessageInfo().getMessageId() + " received");
        LOG.info("Number of payloads attached to this message: " + response.getPayload().size());
    }

    private static void listPendingMessages() {
        LOG.info("Call Webservice Method: listPendingMessages");

        ListPendingMessagesResponse listPendingMessagesResponse = webserviceExample.listPendingMessages();


        LOG.info("The following messages are currently pending (downloadable):");

        if (listPendingMessagesResponse.getMessageID().isEmpty()) {
            LOG.info("No messages pending");
        }

        //ListpendingMessages returns a {@code List<String>} of messageIds
        for (String messageId : listPendingMessagesResponse.getMessageID()) {
            LOG.info("MessageId: " + messageId);
        }
    }

    private static void listMessageStatusForMessageWithID() {
        LOG.info("Call Webservice Method: getMessageStatus");

        GetStatusRequest messageStatusRequest = new GetStatusRequest();
        //The messageId determines the message for which the status is requested
        messageStatusRequest.setMessageID(UUID.randomUUID().toString());

        MessageStatus response = webserviceExample.getMessageStatus(messageStatusRequest);

        LOG.info("Message status for messsage with messageId " + messageStatusRequest.getMessageID() + ": " + response.value());
    }

    private static void listErrorsForMessageWithID() {
        LOG.info("Call Webservice Method: getMessageErrors");

        GetErrorsRequest messageErrorsRequest = new GetErrorsRequest();
        //The messageId determines the message for which the list of errors is requested
        messageErrorsRequest.setMessageID(UUID.randomUUID().toString());


        ErrorResultImplArray response = webserviceExample.getMessageErrors(messageErrorsRequest);

        LOG.info("List of errors for message with messageId " + messageErrorsRequest.getMessageID() + ": ");
        String errorString = Helper.errorResultAsFormattedString(response);
        if(errorString.isEmpty()) {
            LOG.info("No errors found");
        } else {
            LOG.info(errorString);
        }
    }


    private static class Helper {

        private static final String LINE_SEPARATOR = System.getProperty("line.separator");

        public static  String errorResultAsFormattedString(ErrorResultImplArray errorResultArray) {
            StringBuilder formattedOutput = new StringBuilder();

            for (ErrorResultImpl errorResult : errorResultArray.getItem()) {
                formattedOutput.append(LINE_SEPARATOR);
                formattedOutput.append("==========================================================");
                formattedOutput.append(LINE_SEPARATOR);
                formattedOutput.append("EBMS3 error code: " + errorResult.getErrorCode());
                formattedOutput.append(LINE_SEPARATOR);
                formattedOutput.append("Error details: " + errorResult.getErrorDetail());
                formattedOutput.append(LINE_SEPARATOR);
                formattedOutput.append("Error is related to message with messageId: " + errorResult.getMessageInErrorId());
                formattedOutput.append(LINE_SEPARATOR);
                formattedOutput.append("Role of MSH in context of this message transmission: " + errorResult.getMshRole());
                formattedOutput.append(LINE_SEPARATOR);
                formattedOutput.append("Time of notification: " + errorResult.getNotified());
                formattedOutput.append(LINE_SEPARATOR);
                formattedOutput.append("Message was sent/received: " + errorResult.getTimestamp());
                formattedOutput.append(LINE_SEPARATOR);
                formattedOutput.append("==========================================================");
                formattedOutput.append(LINE_SEPARATOR);
            }

            return formattedOutput.toString();
        }
    }

}
