package eu.domibus.example.ws;

import eu.domibus.common.model.org.oasis_open.docs.ebxml_msg.ebms.v3_0.ns.core._200704.Messaging;
import eu.domibus.plugin.webService.generated.*;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;
import javax.xml.namespace.QName;
import javax.xml.soap.MessageFactory;
import javax.xml.soap.SOAPMessage;
import javax.xml.ws.Dispatch;
import javax.xml.ws.Holder;
import javax.xml.ws.soap.SOAPBinding;
import java.io.File;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.UUID;

import static org.junit.Assert.*;

/**
 * Created by muellers on 7/1/16.
 */
public class WebserviceExampleTest {

    //In case you MSH webservice is not running on "http://localhost:8080/domibus/services/msh", please change
    // this constant accordingly
    private static final String MSH_URL = "http://localhost:8080/domibus/services/msh";
    //In case you backend webservice is not running on "http://localhost:8080/domibus/services/backend", please change
    // this constant accordingly
    private static final String BACKENDWS_URL = "http://localhost:8080/domibus/services/backend";

    private static final String TESTSENDMESSAGE_LOCATION_SENDREQUEST = "src/test/resources/eu/domibus/example/ws/sendMessage_sendRequest.xml";
    private static final String TESTSENDMESSAGE_LOCATION_MESSAGING = "src/test/resources/eu/domibus/example/ws/sendMessage_messaging.xml";
    private static final String SAMPLE_MSH_MESSAGE = "src/test/resources/eu/domibus/example/ws/sampleMHSMessage.xml";

    private static WebserviceExample webserviceExample = new WebserviceExample(BACKENDWS_URL);


    @After
    public void cleanUp() throws Exception{
        ListPendingMessagesResponse listPendingMessagesResponse = webserviceExample.listPendingMessages();

        Thread.sleep(2000);

        for (String messageIdCurrentMessage : listPendingMessagesResponse.getMessageID()) {
            DownloadMessageRequest downloadMessageRequest = new DownloadMessageRequest();
            downloadMessageRequest.setMessageID(messageIdCurrentMessage);

            Holder<DownloadMessageResponse> responseHolder = new Holder<DownloadMessageResponse>();
            Holder<Messaging> messagingHolder = new Holder<Messaging>();

            webserviceExample.downloadMessage(downloadMessageRequest, responseHolder, messagingHolder);
        }
    }

    @Before
    public void prepare() throws Exception {
        Thread.sleep(5000);
    }


    @Test
    public void testSendMessage_CorrectRequest_NoErrorsExpected() throws Exception {
        SendRequest sendRequest = Helper.parseSendRequestXML(TESTSENDMESSAGE_LOCATION_SENDREQUEST);
        Messaging messaging = Helper.parseMessagingXML(TESTSENDMESSAGE_LOCATION_MESSAGING);

        SendResponse result = webserviceExample.sendMessage(sendRequest, messaging);

        assertNotNull(result.getMessageID());
        assertNotEquals("", result.getMessageID());
    }


    @Test
    public void testDownloadMessage_MessageIdProvided_MessageWithMessageIDExpected() throws Exception {
        //create new unique messageId
        String messageId = UUID.randomUUID().toString();

        //send message to domibus instance, but on the MSH side, in order to have a message that is available for download
        Helper.prepareMSHTestMessage(messageId, SAMPLE_MSH_MESSAGE);

        //wait until the message should be received
        Thread.sleep(2000);

        //send an additional message that would be the next message instead of the first one
        Helper.prepareMSHTestMessage(null, null);

        DownloadMessageRequest downloadMessageRequest = new DownloadMessageRequest();
        //the messageId has been set. In this case, only the messageID corresponding to this messageID must be downloaded
        downloadMessageRequest.setMessageID(messageId);

        //Since this method has two return values the response objects are passed over as method parameters.
        Holder<DownloadMessageResponse> responseHolder = new Holder<DownloadMessageResponse>();
        Holder<Messaging> messagingHolder = new Holder<Messaging>();


        webserviceExample.downloadMessage(downloadMessageRequest, responseHolder, messagingHolder);

        assertNotNull(responseHolder);
        assertNotNull(messagingHolder);

        DownloadMessageResponse downloadMessageResponse = responseHolder.value;
        Messaging ebMSHeaderResponse = messagingHolder.value;

        //Since the only message that should be available for download is the message we have sent at the beginning
        //of this test, the messageId of the downloaded message must be the same as the messageId of the message initially
        //sent to the MSH
        assertEquals(messageId, ebMSHeaderResponse.getUserMessage().getMessageInfo().getMessageId());
    }

    @Test
    public void testListPendingMessages_CorrectRequest_NoErrorsExpected() throws Exception {
        //create new unique messageId
        String messageId = UUID.randomUUID().toString();

        //send message to domibus instance, but on the MSH side, in order to have a message that is available for download
        Helper.prepareMSHTestMessage(messageId, SAMPLE_MSH_MESSAGE);

        ListPendingMessagesResponse listPendingMessagesResponse = webserviceExample.listPendingMessages();
        assertTrue(listPendingMessagesResponse.getMessageID().size() == 1);
        assertEquals(messageId, listPendingMessagesResponse.getMessageID().get(0));
    }

    @Test
    public void testGetMessageStatus_MessageIdProvided_NoErrorsExpected() throws Exception {
        //create new unique messageId
        String messageId = UUID.randomUUID().toString();

        //send message to domibus instance, but on the MSH side, in order to have a message that is available for download
        Helper.prepareMSHTestMessage(messageId, SAMPLE_MSH_MESSAGE);

        //wait until the message should be received
        Thread.sleep(2000);

        GetStatusRequest messageStatusRequest = new GetStatusRequest();
        //The messageId determines the message for which the status is requested
        messageStatusRequest.setMessageID(messageId);

        MessageStatus response = webserviceExample.getMessageStatus(messageStatusRequest);

        assertEquals(MessageStatus.RECEIVED, response);
    }

    @Test
    public void testGetMessageErrors_MessageIdProvided_ErrorForMessageExpected() throws Exception {
        //create new unique messageId
        String messageId = UUID.randomUUID().toString();

        //send message to domibus instance, but on the MSH side, in order to have a message that is available for download
        Helper.prepareMSHTestMessage(messageId, SAMPLE_MSH_MESSAGE);

        //wait until the message should be received
        Thread.sleep(2000);

        GetErrorsRequest messageErrorsRequest = new GetErrorsRequest();
        //The messageId determines the message for which the list of errors is requested
        messageErrorsRequest.setMessageID(UUID.randomUUID().toString());

        ErrorResultImplArray response = webserviceExample.getMessageErrors(messageErrorsRequest);

        String errorString = Helper.errorResultAsFormattedString(response);

        assertNotNull(errorString);
    }


    private static class Helper {
        private static JAXBContext jaxbMessagingContext;
        private static JAXBContext jaxbWebserviceContext;
        private static MessageFactory messageFactory = new com.sun.xml.internal.messaging.saaj.soap.ver1_2.SOAPMessageFactory1_2Impl();
        private static final String LINE_SEPARATOR = System.getProperty("line.separator");

        static {
            try {
                jaxbMessagingContext = JAXBContext.newInstance("eu.domibus.common.model.org.oasis_open.docs.ebxml_msg.ebms.v3_0.ns.core._200704");
                jaxbWebserviceContext = JAXBContext.newInstance("eu.domibus.plugin.webService.generated");
            } catch (JAXBException e) {
                throw new RuntimeException("Initialization of Helper class failed.");
            }

        }

        private static SendRequest parseSendRequestXML(String uriSendRequestXML) throws Exception {
            return (SendRequest) jaxbWebserviceContext.createUnmarshaller().unmarshal(new File(uriSendRequestXML));
        }

        private static Messaging parseMessagingXML(String uriMessagingXML) throws Exception {
            return ((JAXBElement<Messaging>) jaxbMessagingContext.createUnmarshaller().unmarshal(new File(uriMessagingXML))).getValue();
        }

        private static SOAPMessage dispatchMessage(Messaging messaging) throws Exception {
            final QName serviceName = new QName("http://domibus.eu", "msh-dispatch-service");
            final QName portName = new QName("http://domibus.eu", "msh-dispatch");
            final javax.xml.ws.Service service = javax.xml.ws.Service.create(serviceName);
            service.addPort(portName, SOAPBinding.SOAP12HTTP_BINDING, MSH_URL);
            final Dispatch<SOAPMessage> dispatch = service.createDispatch(portName, SOAPMessage.class, javax.xml.ws.Service.Mode.MESSAGE);

            SOAPMessage soapMessage = messageFactory.createMessage();

//            jaxbMessagingContext.createMarshaller().marshal(messaging, soapMessage.getSOAPHeader());
            jaxbMessagingContext.createMarshaller().marshal(new JAXBElement(new QName("http://docs.oasis-open.org/ebxml-msg/ebms/v3.0/ns/core/200704/", "Messaging"), Messaging.class, messaging), soapMessage.getSOAPHeader());
            soapMessage.getSOAPBody().addTextNode("This is the content of the body");
            soapMessage.saveChanges();

            return dispatch.invoke(soapMessage);
        }

        private static XMLGregorianCalendar getCurrentDate() throws Exception {
            GregorianCalendar c = new GregorianCalendar();
            c.setTime(new Date());
            XMLGregorianCalendar currentDate = DatatypeFactory.newInstance().newXMLGregorianCalendar(c);

            return currentDate;
        }

        private static String prepareMSHTestMessage(String messageId, String uriMessagingXML) throws Exception {
            //if the messageId is null, create new unique messageId
            if(messageId == null) {
                messageId = UUID.randomUUID().toString();
            }

            //if uriMessagingXML is null, use the SAMPLE_MSH_MESSAGE instead
            if(uriMessagingXML == null) {
                uriMessagingXML = SAMPLE_MSH_MESSAGE;
            }

            Messaging messaging = Helper.parseMessagingXML(uriMessagingXML);
            //set messageId
            messaging.getUserMessage().getMessageInfo().setMessageId(messageId);
            //set timestamp
            messaging.getUserMessage().getMessageInfo().setTimestamp(Helper.getCurrentDate());

            SOAPMessage responseFromMSH = Helper.dispatchMessage(messaging);

            assertNotNull(responseFromMSH);
            assertNotNull(responseFromMSH.getSOAPBody());
            //response is no SOAPFault
            assertNull(responseFromMSH.getSOAPBody().getFault());

            return messageId;
        }

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