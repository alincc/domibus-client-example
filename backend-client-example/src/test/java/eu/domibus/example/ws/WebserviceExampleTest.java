package eu.domibus.example.ws;

import backend.ecodex.org._1_1.PayloadType;
import backend.ecodex.org._1_1.SendRequest;
import com.sun.xml.internal.ws.developer.JAXBContextFactory;
import com.sun.xml.internal.ws.util.ByteArrayDataSource;
import org.junit.Test;
import org.oasis_open.docs.ebxml_msg.ebms.v3_0.ns.core._200704.Messaging;
import org.oasis_open.docs.ebxml_msg.ebms.v3_0.ns.core._200704.PartInfo;

import javax.activation.DataHandler;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import java.io.File;
import java.io.IOException;

import static org.junit.Assert.*;

/**
 * TODO: add class description
 */
public class WebserviceExampleTest {


    @Test
    public void testSendMessage() throws Exception {

    }

    @Test
    public void testDownloadMessage() throws Exception {

    }

    @Test
    public void testListPendingMessages() throws Exception {

    }

    @Test
    public void testGetMessageStatus() throws Exception {

    }

    @Test
    public void testGetMessageErrors() throws Exception {

    }

    private Messaging createMessagingFromFile(String filename) throws IOException, JAXBException {
        JAXBContext jaxbMessagingContext = JAXBContext.newInstance("eu.domibus.common.model.org.oasis_open.docs.ebxml_msg.ebms.v3_0.ns.core._200704");

        Messaging messaging = ((JAXBElement<Messaging>)jaxbMessagingContext.createUnmarshaller().unmarshal(new File(filename))).getValue();


        return messaging;
    }
}