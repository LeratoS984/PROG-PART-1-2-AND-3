

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Tests t arrays, searches, deletion, and report.
 */
public class MessageManagerTest {

    private MessageManager messageManager;

    private Message messageOne;
    private Message messageTwo;
    private Message messageThree;
    private Message messageFour;
    private Message messageFive;

    @BeforeEach
    public void setUp() {
        messageManager = new MessageManager();

        messageOne = new Message(
                "1000000000",
                0,
                "+27834557896",
                "Did you get the cake?"
        );

        messageTwo = new Message(
                "2000000000",
                1,
                "+27838884567",
                "Where are you? You are late! I have asked you to be on time."
        );

        messageThree = new Message(
                "3000000000",
                2,
                "+27834484567",
                "Yohoooo, I am at your gate."
        );

        messageFour = new Message(
                "0838884567",
                3,
                "0838884567",
                "It is dinner time!"
        );

        messageFive = new Message(
                "5000000000",
                4,
                "+27838884567",
                "Ok, I am leaving without you."
        );

        messageManager.addSentMessage(messageOne);
        messageManager.addStoredMessage(messageTwo);
        messageManager.addDisregardedMessage(messageThree);
        messageManager.addSentMessage(messageFour);
        messageManager.addStoredMessage(messageFive);
    }

    @Test
    public void testSentMessagesArrayIsCorrectlyPopulated() {
        String[] expectedMessages = {
            "Did you get the cake?",
            "It is dinner time!"
        };

        assertArrayEquals(
                expectedMessages,
                messageManager.getSentMessageTexts()
        );
    }

    @Test
    public void testLongestStoredMessage() {
        assertEquals(
                "Where are you? You are late! I have asked you to be on time.",
                messageManager.getLongestStoredMessage()
        );
    }

    @Test
    public void testSearchForMessageId() {
        assertEquals(
                "It is dinner time!",
                messageManager.getMessageTextById("0838884567")
        );
    }

    @Test
    public void testSearchMessagesForRecipient() {
        String[] expectedMessages = {
            "Where are you? You are late! I have asked you to be on time.",
            "Ok, I am leaving without you."
        };

        assertArrayEquals(
                expectedMessages,
                messageManager.getMessagesForRecipient("+27838884567")
        );
    }

    @Test
    public void testDeleteStoredMessageUsingHash() {
        String expectedResult = "Message: \"Where are you? You are late! "
                + "I have asked you to be on time.\" successfully deleted.";

        assertEquals(
                expectedResult,
                messageManager.deleteStoredMessageByHash(
                        messageTwo.getMessageHash()
                )
        );
    }

    @Test
    public void testMessageReportContainsRequiredDetails() {
        String report = messageManager.createMessageReport();

        assertTrue(report.contains("Message Hash: "
                + messageOne.getMessageHash()));

        assertTrue(report.contains("Recipient: +27834557896"));

        assertTrue(report.contains("Message: Did you get the cake?"));
    }
}