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
