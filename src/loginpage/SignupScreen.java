package loginpage;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;

public class SignupScreen {
    private BorderPane root;
    private Runnable onBackToLogin;

    public SignupScreen() {
        root = new BorderPane();

        // Left panel with gradient and "HabitTracker" text
        VBox left = new VBox();
        left.setAlignment(Pos.CENTER);
        left.setStyle("-fx-background-color: radial-gradient(" +
                "focus-angle 0deg, focus-distance 0%, center -2% 101%, radius 125%," +
                "rgba(238, 174, 202, 1) 40%, rgba(202, 179, 214, 1) 65%, rgba(148, 201, 233, 1) 100%)");
        left.setPrefWidth(350);

        Font pacificoFont = Font.loadFont(getClass().getResourceAsStream("/Pacifico-Regular.ttf"), 38);
        if (pacificoFont == null) {
            pacificoFont = new Font("Arial", 38); // Fallback font
        }
        Label habitTitle = new Label("HabitTracker");
        habitTitle.setFont(pacificoFont);
        habitTitle.setStyle("-fx-text-fill: black;");
        left.getChildren().add(habitTitle);

        // Right panel with the signup form
        VBox right = new VBox(10);
        right.setPadding(new Insets(40));
        right.setAlignment(Pos.CENTER_LEFT);
        right.setStyle("-fx-background-color: white;");

        Label heading = new Label("Create Account");
        heading.getStyleClass().add("form-heading");

        TextField nameField = new TextField();
        nameField.setPromptText("Name");

        TextField emailField = new TextField();
        emailField.setPromptText("Email");

        PasswordField passwordField = new PasswordField();
        passwordField.setPromptText("Password");

        PasswordField confirmField = new PasswordField();
        confirmField.setPromptText("Confirm Password");

        Label msg = new Label();
        msg.setStyle("-fx-text-fill: red;");

        Button signupBtn = new Button("Signup");
        signupBtn.getStyleClass().add("login-btn");

        Hyperlink backToLogin = new Hyperlink("Back to Login");
        backToLogin.setOnAction(e -> {
            System.out.println("Back to Login clicked");
            if (onBackToLogin != null) {
                onBackToLogin.run();
            } else {
                System.out.println("onBackToLogin is null");
            }
        });

        signupBtn.setOnAction(e -> {
            String username = nameField.getText().trim();
            String email = emailField.getText().trim();
            String password = passwordField.getText();
            String confirm = confirmField.getText();

            if (username.isEmpty()) {
                msg.setText("Please enter your username.");
                msg.setStyle("-fx-text-fill: red;");
            } else if (!email.matches("^\\S+@\\S+\\.\\S+$")) {
                msg.setText("Invalid email format.");
                msg.setStyle("-fx-text-fill: red;");
            } else if (!isStrongPassword(password)) {
                msg.setText("Password must be 6+ chars and must include uppercase, lowercase, digits & symbols.");
                msg.setStyle("-fx-text-fill: red;");
            } else if (!password.equals(confirm)) {
                msg.setText("Passwords do not match.");
                msg.setStyle("-fx-text-fill: red;");
            } else if (UserDatabase.emailExists(email)) {
                msg.setText("Email already registered.");
                msg.setStyle("-fx-text-fill: red;");
            } else {
                if (UserDatabase.saveUser(username, email, password)) {
                    msg.setText("Signup successful! You can now log in.");
                    msg.setStyle("-fx-text-fill: green;");
                    nameField.clear();
                    emailField.clear();
                    passwordField.clear();
                    confirmField.clear();
                } else {
                    msg.setText("Signup failed. Please try again.");
                    msg.setStyle("-fx-text-fill: red;");
                }
            }
        });

        right.getChildren().addAll(
                heading,
                nameField,
                emailField,
                passwordField,
                confirmField,
                signupBtn,
                msg,
                backToLogin
        );

        root.setLeft(left);
        root.setCenter(right);
    }

    private boolean isStrongPassword(String password) {
        return password.length() >= 6 &&
                password.matches(".*[A-Z].*") &&
                password.matches(".*[a-z].*") &&
                password.matches(".*\\d.*") &&
                password.matches(".*[!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>/?].*");
    }

    public BorderPane getRoot() {
        return root;
    }

    public void setOnBackToLogin(Runnable r) {
        this.onBackToLogin = r;
    }
}