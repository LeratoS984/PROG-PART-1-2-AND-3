import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Arrays;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Manages  message arrays, searches, reports, and JSON data.
 */
public class MessageManager {

    private static final int INITIAL_CAPACITY = 10;
    private static final Path STORED_FILE = Path.of("stored_messages.json");

    private Message[] sentMessages = new Message[INITIAL_CAPACITY];
    private Message[] disregardedMessages = new Message[INITIAL_CAPACITY];
    private Message[] storedMessages = new Message[INITIAL_CAPACITY];
    private String[] messageHashes = new String[INITIAL_CAPACITY];
    private String[] messageIds = new String[INITIAL_CAPACITY];

    private int sentCount;
    private int disregardedCount;
    private int storedCount;
    private int metadataCount;
/*
     * Reads the JSON structure.
     * Regex API reference: https://docs.oracle.com/en/java/javase/17/docs/api/java.base/java/util/regex/package-summary.html
     */
    private static final Pattern JSON_MESSAGE_PATTERN = Pattern.compile(
            "\\{\\s*\"messageId\"\\s*:\\s*\"(.*?)\"\\s*,\\s*"
            + "\"messageHash\"\\s*:\\s*\"(.*?)\"\\s*,\\s*"
            + "\"recipient\"\\s*:\\s*\"(.*?)\"\\s*,\\s*"
            + "\"message\"\\s*:\\s*\"(.*?)\"\\s*\\}",
            Pattern.DOTALL
    );

    public void addSentMessage(Message message) {
        ensureSentCapacity();
        sentMessages[sentCount] = message;
        sentCount++;
        addMessageMetadata(message);
    }

    public void addDisregardedMessage(Message message) {
        ensureDisregardedCapacity();
        disregardedMessages[disregardedCount] = message;
        disregardedCount++;
        addMessageMetadata(message);
    }

    public void addStoredMessage(Message message) {
        ensureStoredCapacity();
        storedMessages[storedCount] = message;
        storedCount++;
        addMessageMetadata(message);
    }

    public void addMessageMetadata(Message message) {
        for (int index = 0; index < metadataCount; index++) {
            if (messageIds[index].equals(message.getMessageId())
                    && messageHashes[index].equals(message.getMessageHash())) {
                return;
            }
        }

        ensureMetadataCapacity();
        messageIds[metadataCount] = message.getMessageId();
        messageHashes[metadataCount] = message.getMessageHash();
        metadataCount++;
    }
/*
     * Reads stored_messages.json and places its messages into the stored array.
     */
    public String loadStoredMessagesFromJson() {
        clearStoredMessages();

        if (!Files.exists(STORED_FILE)) {
            return "No stored message file was found.";
        }

        try {
            String json = Files.readString(STORED_FILE, StandardCharsets.UTF_8);
            Matcher matcher = JSON_MESSAGE_PATTERN.matcher(json);

            while (matcher.find()) {
                String messageId = matcher.group(1);
                String messageHash = matcher.group(2);
                String recipient = unescapeJson(matcher.group(3));
                String messageText = unescapeJson(matcher.group(4));

                int messageNumber = extractMessageNumber(messageHash);

                Message message = new Message(
                        messageId,
                        messageNumber,
                        recipient,
                        messageText
                );

                addStoredMessage(message);
            }

            if (storedCount == 0) {
                return "No stored messages were found in the JSON file.";
            }

            return storedCount + " stored message(s) loaded successfully.";

        } catch (IOException exception) {
            return "Stored messages could not be loaded.";
        }
    }

    private int extractMessageNumber(String messageHash) {
        String[] hashParts = messageHash.split(":");

        if (hashParts.length >= 2) {
            try {
                return Integer.parseInt(hashParts[1]);
            } catch (NumberFormatException exception) {
                return 0;
            }
        }

        return 0;
    }

    /*
     * Displays the sender and recipient of each stored message.
     */
    public String displayStoredSendersAndRecipients(String senderName) {
        if (storedCount == 0) {
            return "No stored messages are available.";
        }

        StringBuilder output = new StringBuilder();

        for (int index = 0; index < storedCount; index++) {
            output.append("Sender: ")
                    .append(senderName)
                    .append(System.lineSeparator());

            output.append("Recipient: ")
                    .append(storedMessages[index].getRecipient())
                    .append(System.lineSeparator())
                    .append(System.lineSeparator());
        }

        return output.toString();
    }

    public String getLongestStoredMessage() {
        if (storedCount == 0) {
            return "No stored messages are available.";
        }

        Message longestMessage = storedMessages[0];

        for (int index = 1; index < storedCount; index++) {
            if (storedMessages[index].getMessageText().length()
                    > longestMessage.getMessageText().length()) {
                longestMessage = storedMessages[index];
            }
        }

        return longestMessage.getMessageText();
    }

    public String searchMessageById(String messageId) {
        Message message = findMessageById(messageId);

        if (message == null) {
            return "No message found with ID: " + messageId;
        }

        return "Recipient: " + message.getRecipient()
                + System.lineSeparator()
                + "Message: " + message.getMessageText();
    }

    public String getMessageTextById(String messageId) {
        Message message = findMessageById(messageId);

        if (message == null) {
            return "";
        }

        return message.getMessageText();
    }

    private Message findMessageById(String messageId) {
        Message message = findInArray(sentMessages, sentCount, messageId);

        if (message != null) {
            return message;
        }

        message = findInArray(storedMessages, storedCount, messageId);

        if (message != null) {
            return message;
        }

        return findInArray(disregardedMessages, disregardedCount, messageId);
    }

    private Message findInArray(Message[] messages, int count, String messageId) {
        for (int index = 0; index < count; index++) {
            if (messages[index].getMessageId().equals(messageId)) {
                return messages[index];
            }
        }

        return null;
    }

    public String searchMessagesForRecipient(String recipient) {
        String[] messages = getMessagesForRecipient(recipient);

        if (messages.length == 0) {
            return "No sent or stored messages were found for: " + recipient;
        }

        StringBuilder output = new StringBuilder();

        for (String message : messages) {
            output.append(message)
                    .append(System.lineSeparator());
        }

        return output.toString();
    }

    public String[] getMessagesForRecipient(String recipient) {
        String[] matchingMessages = new String[INITIAL_CAPACITY];
        int matchCount = 0;

        matchCount = addMatchingMessages(
                sentMessages,
                sentCount,
                recipient,
                matchingMessages,
                matchCount
        );

        matchCount = addMatchingMessages(
                storedMessages,
                storedCount,
                recipient,
                matchingMessages,
                matchCount
        );

        return Arrays.copyOf(matchingMessages, matchCount);
    }

    private int addMatchingMessages(Message[] source, int sourceCount,
            String recipient, String[] destination, int destinationCount) {

        for (int index = 0; index < sourceCount; index++) {
            if (source[index].getRecipient().equals(recipient)) {
                if (destinationCount == destination.length) {
                    destination = Arrays.copyOf(
                            destination,
                            destination.length * 2
                    );
                }

                destination[destinationCount] = source[index].getMessageText();
                destinationCount++;
            }
        }

        return destinationCount;
    }

    public String deleteStoredMessageByHash(String messageHash) {
        for (int index = 0; index < storedCount; index++) {
            if (storedMessages[index].getMessageHash().equals(messageHash)) {
                String deletedMessage = storedMessages[index].getMessageText();

                for (int moveIndex = index; moveIndex < storedCount - 1; moveIndex++) {
                    storedMessages[moveIndex] = storedMessages[moveIndex + 1];
                }

                storedCount--;
                storedMessages[storedCount] = null;

                Message.removeStoredMessageFromSession(messageHash);
                writeStoredMessagesToJson();

                return "Message: \"" + deletedMessage
                        + "\" successfully deleted.";
            }
        }

        return "No stored message found with hash: " + messageHash;
    }

    public String createMessageReport() {
        StringBuilder report = new StringBuilder();
        report.append("MESSAGE REPORT")
                .append(System.lineSeparator());

        appendReportSection(report, "Sent Messages", sentMessages, sentCount);
        appendReportSection(report, "Stored Messages", storedMessages, storedCount);

        return report.toString();
    }

    private void appendReportSection(StringBuilder report, String heading,
            Message[] messages, int count) {

        report.append(System.lineSeparator())
                .append(heading)
                .append(System.lineSeparator());

        if (count == 0) {
            report.append("None")
                    .append(System.lineSeparator());
            return;
        }

        for (int index = 0; index < count; index++) {
            report.append("Message Hash: ")
                    .append(messages[index].getMessageHash())
                    .append(System.lineSeparator());

            report.append("Recipient: ")
                    .append(messages[index].getRecipient())
                    .append(System.lineSeparator());

            report.append("Message: ")
                    .append(messages[index].getMessageText())
                    .append(System.lineSeparator())
                    .append(System.lineSeparator());
        }
    }

    private void writeStoredMessagesToJson() {
        StringBuilder json = new StringBuilder("[\n");

        for (int index = 0; index < storedCount; index++) {
            Message message = storedMessages[index];

            json.append("  {\n");
            json.append("    \"messageId\": \"")
                    .append(message.getMessageId()).append("\",\n");

            json.append("    \"messageHash\": \"")
                    .append(message.getMessageHash()).append("\",\n");

            json.append("    \"recipient\": \"")
                    .append(message.getRecipient()).append("\",\n");

            json.append("    \"message\": \"")
                    .append(escapeJson(message.getMessageText()))
                    .append("\"\n");

            json.append("  }");

            if (index < storedCount - 1) {
                json.append(",");
            }

            json.append("\n");
        }

        json.append("]");

        try {
            Files.writeString(
                    STORED_FILE,
                    json.toString(),
                    StandardCharsets.UTF_8,
                    StandardOpenOption.CREATE,
                    StandardOpenOption.TRUNCATE_EXISTING
            );
        } catch (IOException exception) {
            // The delete operation still succeeds in the arrays for this session.
        }
    }

    private String escapeJson(String text) {
        return text.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n");
    }

    private String unescapeJson(String text) {
        return text.replace("\\n", "\n")
                .replace("\\\"", "\"")
                .replace("\\\\", "\\");
    }

    private void clearStoredMessages() {
        storedMessages = new Message[INITIAL_CAPACITY];
        storedCount = 0;
    }

    private void ensureSentCapacity() {
        if (sentCount == sentMessages.length) {
            sentMessages = Arrays.copyOf(
                    sentMessages,
                    sentMessages.length * 2
            );
        }
    }

    private void ensureDisregardedCapacity() {
        if (disregardedCount == disregardedMessages.length) {
            disregardedMessages = Arrays.copyOf(
                    disregardedMessages,
                    disregardedMessages.length * 2
            );
        }
    }

    private void ensureStoredCapacity() {
        if (storedCount == storedMessages.length) {
            storedMessages = Arrays.copyOf(
                    storedMessages,
                    storedMessages.length * 2
            );
        }
    }

    private void ensureMetadataCapacity() {
        if (metadataCount == messageIds.length) {
            messageIds = Arrays.copyOf(
                    messageIds,
                    messageIds.length * 2
            );

            messageHashes = Arrays.copyOf(
                    messageHashes,
                    messageHashes.length * 2
            );
        }
    }

    public String[] getSentMessageTexts() {
        String[] messages = new String[sentCount];

        for (int index = 0; index < sentCount; index++) {
            messages[index] = sentMessages[index].getMessageText();
        }

        return messages;
    }
}