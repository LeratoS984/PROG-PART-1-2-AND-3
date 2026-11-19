

import java.util.Scanner;

/**
 * Runs the combined Part 1, Part 2, and Part 3 QuickChat application.
 */
public class Main {

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        Login login = new Login();
        MessageManager messageManager = new MessageManager();

        System.out.println("Welcome to QuickChat.");
        System.out.println();

        String senderName = registerAndLogIn(scanner, login);

        if (senderName == null) {
            System.out.println("You must log in successfully before sending messages.");
            return;
        }

        int allowedMessages = readPositiveNumber(
                scanner,
                "How many messages would you like to enter? "
        );

        int enteredMessages = 0;
        boolean applicationRunning = true;

        while (applicationRunning) {
            System.out.println();
            System.out.println("1) Send Messages");
            System.out.println("2) Show recently sent messages");
            System.out.println("3) Quit");
            System.out.println("4) Stored Messages");
            System.out.print("Choose an option: ");

            String menuChoice = scanner.nextLine().trim();

            switch (menuChoice) {
                case "1":
                    if (enteredMessages >= allowedMessages) {
                        System.out.println("You have already entered the allowed number of messages.");
                    } else {
                        enteredMessages = captureMessage(
                                scanner,
                                enteredMessages,
                                allowedMessages,
                                messageManager
                        );
                    }
                    break;

                case "2":
                    System.out.println("Coming Soon.");
                    break;

                case "3":
                    System.out.println("Total messages successfully sent: "
                            + Message.getTotalSentMessages());
                    System.out.println("Thank you for using QuickChat.");
                    applicationRunning = false;
                    break;

                case "4":
                    manageStoredMessages(
                            scanner,
                            messageManager,
                            senderName
                    );
                    break;

                default:
                    System.out.println("Please choose option 1, 2, 3, or 4.");
                    break;
            }
        }
    }

    private static String registerAndLogIn(Scanner scanner, Login login) {
        System.out.print("Enter your first name: ");
        String firstName = scanner.nextLine().trim();

        System.out.print("Enter your last name: ");
        String lastName = scanner.nextLine().trim();

        System.out.print("Create a username: ");
        String username = scanner.nextLine().trim();

        System.out.print("Create a password: ");
        String password = scanner.nextLine();

        System.out.print("Enter your cell number with international code: ");
        String cellNumber = scanner.nextLine().trim();

        System.out.println();
        System.out.println(login.registerUser(
                username,
                password,
                cellNumber,
                firstName,
                lastName
        ));

        if (!login.isRegistered()) {
            System.out.println("Registration was unsuccessful. Restart the program and correct your details.");
            return null;
        }

        System.out.println();
        System.out.println("Please log in.");

        System.out.print("Username: ");
        String enteredUsername = scanner.nextLine().trim();

        System.out.print("Password: ");
        String enteredPassword = scanner.nextLine();

        boolean loginSuccessful = login.loginUser(
                enteredUsername,
                enteredPassword
        );

        System.out.println(login.returnLoginStatus(loginSuccessful));

        if (!loginSuccessful) {
            return null;
        }

        return firstName;
    }

    private static int captureMessage(Scanner scanner,
            int enteredMessages, int allowedMessages,
            MessageManager messageManager) {

        System.out.println();
        System.out.println("Message " + (enteredMessages + 1)
                + " of " + allowedMessages);

        System.out.print("Enter recipient number: ");
        String recipient = scanner.nextLine().trim();

        System.out.print("Enter your message: ");
        String messageText = scanner.nextLine();

        Message message = new Message(
                enteredMessages,
                recipient,
                messageText
        );

        String recipientResult = message.checkRecipientCell();
        System.out.println(recipientResult);

        if (!recipientResult.equals("Cell phone number successfully captured.")) {
            return enteredMessages;
        }

        String lengthResult = message.checkMessageLength();
        System.out.println(lengthResult);

        if (!lengthResult.equals("Message ready to send.")) {
            return enteredMessages;
        }

        messageManager.addMessageMetadata(message);

        System.out.println("Message ID generated: " + message.getMessageId());
        System.out.println("Message Hash: " + message.getMessageHash());

        int sendingChoice = readSendingChoice(scanner);

        switch (sendingChoice) {
            case 1:
                System.out.println(message.SentMessage(1));
                messageManager.addSentMessage(message);

                System.out.println();
                System.out.println(message.formatMessageDetails());
                break;

            case 2:
                System.out.println(message.SentMessage(2));
                messageManager.addDisregardedMessage(message);
                break;

            case 3:
                System.out.println(message.SentMessage(3));
                System.out.println(messageManager.loadStoredMessagesFromJson());
                break;

            default:
                break;
        }

        int newEnteredMessages = enteredMessages + 1;

        if (newEnteredMessages == allowedMessages) {
            System.out.println();
            System.out.println("You have entered all " + allowedMessages + " messages.");
            System.out.println("Total messages successfully sent: "
                    + Message.getTotalSentMessages());
        }

        return newEnteredMessages;
    }

    private static void manageStoredMessages(Scanner scanner,
            MessageManager messageManager, String senderName) {

        System.out.println();
        System.out.println(messageManager.loadStoredMessagesFromJson());

        boolean storedMenuRunning = true;

        while (storedMenuRunning) {
            System.out.println();
            System.out.println("STORED MESSAGES MENU");
            System.out.println("1) Display stored message sender and recipient");
            System.out.println("2) Display longest stored message");
            System.out.println("3) Search for a message ID");
            System.out.println("4) Search messages for a recipient");
            System.out.println("5) Delete a message using a message hash");
            System.out.println("6) Display message report");
            System.out.println("7) Return to the main menu");
            System.out.print("Choose an option: ");

            String storedChoice = scanner.nextLine().trim();

            switch (storedChoice) {
                case "1":
                    System.out.println(
                            messageManager.displayStoredSendersAndRecipients(
                                    senderName
                            )
                    );
                    break;

                case "2":
                    System.out.println("Longest stored message: "
                            + messageManager.getLongestStoredMessage());
                    break;

                case "3":
                    System.out.print("Enter the message ID: ");
                    String messageId = scanner.nextLine().trim();

                    System.out.println(
                            messageManager.searchMessageById(messageId)
                    );
                    break;

                case "4":
                    System.out.print("Enter the recipient number: ");
                    String recipient = scanner.nextLine().trim();

                    System.out.println(
                            messageManager.searchMessagesForRecipient(recipient)
                    );
                    break;

                case "5":
                    System.out.print("Enter the message hash: ");
                    String messageHash = scanner.nextLine().trim();

                    System.out.println(
                            messageManager.deleteStoredMessageByHash(messageHash)
                    );
                    break;

                case "6":
                    System.out.println(
                            messageManager.createMessageReport()
                    );
                    break;

                case "7":
                    storedMenuRunning = false;
                    break;

                default:
                    System.out.println("Please choose an option from 1 to 7.");
                    break;
            }
        }
    }

    private static int readPositiveNumber(Scanner scanner, String prompt) {
        while (true) {
            System.out.print(prompt);

            try {
                int number = Integer.parseInt(scanner.nextLine().trim());

                if (number > 0) {
                    return number;
                }

                System.out.println("Please enter a number greater than zero.");
            } catch (NumberFormatException exception) {
                System.out.println("Please enter a valid whole number.");
            }
        }
    }

    private static int readSendingChoice(Scanner scanner) {
        while (true) {
            System.out.println();
            System.out.println("1) Send Message");
            System.out.println("2) Disregard Message");
            System.out.println("3) Store Message to send later");
            System.out.print("Choose an option: ");

            String choice = scanner.nextLine().trim();

            if (choice.equals("1") || choice.equals("2") || choice.equals("3")) {
                return Integer.parseInt(choice);
            }

            System.out.println("Please choose option 1, 2, or 3.");
        }
    }
}