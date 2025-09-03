import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

public class HangManGame extends JFrame {
    private static final String[] WORDS = {
            "JAVA", "PROGRAMMING", "COMPUTER", "KEYBOARD", "MONITOR",
            "ALGORITHM", "DATABASE", "NETWORK", "SOFTWARE", "HARDWARE",
            "INTERNET", "BROWSER", "WEBSITE", "APPLICATION", "DEVELOPMENT"
    };

    private String currentWord;
    private StringBuilder guessedWord;
    private List<Character> wrongGuesses;
    private int wrongGuessCount;
    private final int MAX_WRONG_GUESSES = 6;

    // GUI Components
    private JLabel wordLabel;
    private JLabel wrongGuessesLabel;
    private JTextField guessInput;
    private JButton guessButton;
    private JButton newGameButton;
    private HangmanPanel hangmanPanel;
    private JLabel statusLabel;

    public HangManGame() {
        initializeGame();
        setupGUI();
    }

    private void initializeGame() {
        Random random = new Random();
        currentWord = WORDS[random.nextInt(WORDS.length)];
        guessedWord = new StringBuilder();
        for (int i = 0; i < currentWord.length(); i++) {
            guessedWord.append("_ ");
        }
        wrongGuesses = new ArrayList<>();
        wrongGuessCount = 0;
    }

    private void setupGUI() {
        setTitle("Hangman Game");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());
        setResizable(false);

        // Create main panels
        JPanel topPanel = new JPanel(new BorderLayout());
        JPanel centerPanel = new JPanel(new BorderLayout());
        JPanel bottomPanel = new JPanel(new FlowLayout());

        // Word display
        wordLabel = new JLabel(guessedWord.toString(), SwingConstants.CENTER);
        wordLabel.setFont(new Font("Monospaced", Font.BOLD, 24));
        wordLabel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        topPanel.add(wordLabel, BorderLayout.CENTER);

        // Status label
        statusLabel = new JLabel("Guess a letter!", SwingConstants.CENTER);
        statusLabel.setFont(new Font("Arial", Font.PLAIN, 16));
        topPanel.add(statusLabel, BorderLayout.SOUTH);

        // Hangman drawing
        hangmanPanel = new HangmanPanel();
        hangmanPanel.setPreferredSize(new Dimension(200, 250));
        centerPanel.add(hangmanPanel, BorderLayout.WEST);

        // Wrong guesses display
        wrongGuessesLabel = new JLabel("Wrong guesses: ", SwingConstants.CENTER);
        wrongGuessesLabel.setFont(new Font("Arial", Font.PLAIN, 14));
        wrongGuessesLabel.setVerticalAlignment(SwingConstants.TOP);
        wrongGuessesLabel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        centerPanel.add(wrongGuessesLabel, BorderLayout.CENTER);

        // Input panel
        JPanel inputPanel = new JPanel(new FlowLayout());
        JLabel inputLabel = new JLabel("Enter a letter: ");
        guessInput = new JTextField(5);
        guessButton = new JButton("Guess");
        newGameButton = new JButton("New Game");

        inputPanel.add(inputLabel);
        inputPanel.add(guessInput);
        inputPanel.add(guessButton);
        inputPanel.add(newGameButton);
        bottomPanel.add(inputPanel);

        // Add panels to frame
        add(topPanel, BorderLayout.NORTH);
        add(centerPanel, BorderLayout.CENTER);
        add(bottomPanel, BorderLayout.SOUTH);

        // Event listeners
        guessButton.addActionListener(new GuessButtonListener());
        newGameButton.addActionListener(new NewGameButtonListener());
        guessInput.addActionListener(new GuessButtonListener()); // Enter key support

        // Set focus to input field
        guessInput.requestFocusInWindow();

        pack();
        setLocationRelativeTo(null);
    }

    private class GuessButtonListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            String input = guessInput.getText().trim().toUpperCase();
            guessInput.setText("");

            if (input.length() != 1 || !Character.isLetter(input.charAt(0))) {
                statusLabel.setText("Please enter a single letter!");
                return;
            }

            char guess = input.charAt(0);

            // Check if already guessed
            if (wrongGuesses.contains(guess) || guessedWord.toString().contains(String.valueOf(guess))) {
                statusLabel.setText("You already guessed that letter!");
                return;
            }

            // Process the guess
            if (currentWord.contains(String.valueOf(guess))) {
                // Correct guess
                for (int i = 0; i < currentWord.length(); i++) {
                    if (currentWord.charAt(i) == guess) {
                        guessedWord.setCharAt(i * 2, guess);
                    }
                }
                wordLabel.setText(guessedWord.toString());

                // Check if word is complete
                if (!guessedWord.toString().contains("_")) {
                    statusLabel.setText("Congratulations! You won!");
                    guessButton.setEnabled(false);
                    guessInput.setEnabled(false);
                } else {
                    statusLabel.setText("Good guess! Keep going!");
                }
            } else {
                // Wrong guess
                wrongGuesses.add(guess);
                wrongGuessCount++;
                updateWrongGuessesDisplay();
                hangmanPanel.setWrongGuesses(wrongGuessCount);

                if (wrongGuessCount >= MAX_WRONG_GUESSES) {
                    statusLabel.setText("Game Over! The word was: " + currentWord);
                    guessButton.setEnabled(false);
                    guessInput.setEnabled(false);
                } else {
                    statusLabel.setText("Wrong guess! Try again!");
                }
            }

            guessInput.requestFocusInWindow();
        }
    }

    private class NewGameButtonListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            initializeGame();
            wordLabel.setText(guessedWord.toString());
            updateWrongGuessesDisplay();
            hangmanPanel.setWrongGuesses(0);
            statusLabel.setText("Guess a letter!");
            guessButton.setEnabled(true);
            guessInput.setEnabled(true);
            guessInput.requestFocusInWindow();
        }
    }

    private void updateWrongGuessesDisplay() {
        StringBuilder wrongGuessText = new StringBuilder("Wrong guesses: ");
        for (Character guess : wrongGuesses) {
            wrongGuessText.append(guess).append(" ");
        }
        wrongGuessText.append("(").append(wrongGuessCount).append("/").append(MAX_WRONG_GUESSES).append(")");
        wrongGuessesLabel.setText(wrongGuessText.toString());
    }

    // Custom panel for drawing the hangman
    private class HangmanPanel extends JPanel {
        private int wrongGuesses = 0;

        public void setWrongGuesses(int count) {
            this.wrongGuesses = count;
            repaint();
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2d = (Graphics2D) g;
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2d.setStroke(new BasicStroke(3));
            g2d.setColor(Color.BLACK);

            int width = getWidth();
            int height = getHeight();

            // Base
            g2d.drawLine(20, height - 20, width - 20, height - 20);

            // Pole
            g2d.drawLine(50, height - 20, 50, 30);

            // Top beam
            g2d.drawLine(50, 30, 120, 30);

            // Noose
            g2d.drawLine(120, 30, 120, 60);

            // Draw hangman parts based on wrong guesses
            if (wrongGuesses >= 1) {
                // Head
                g2d.drawOval(105, 60, 30, 30);
            }

            if (wrongGuesses >= 2) {
                // Body
                g2d.drawLine(120, 90, 120, 160);
            }

            if (wrongGuesses >= 3) {
                // Left arm
                g2d.drawLine(120, 110, 90, 130);
            }

            if (wrongGuesses >= 4) {
                // Right arm
                g2d.drawLine(120, 110, 150, 130);
            }

            if (wrongGuesses >= 5) {
                // Left leg
                g2d.drawLine(120, 160, 90, 190);
            }

            if (wrongGuesses >= 6) {
                // Right leg
                g2d.drawLine(120, 160, 150, 190);
                // Face (X eyes and frown)
                g2d.setColor(Color.RED);
                g2d.drawLine(112, 68, 118, 74);
                g2d.drawLine(118, 68, 112, 74);
                g2d.drawLine(122, 68, 128, 74);
                g2d.drawLine(128, 68, 122, 74);
                g2d.drawArc(112, 78, 16, 8, 0, -180);
            }
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception e) {
                e.printStackTrace();
            }

            new HangManGame().setVisible(true);
        });
    }
}