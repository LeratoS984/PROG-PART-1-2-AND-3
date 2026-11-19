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
