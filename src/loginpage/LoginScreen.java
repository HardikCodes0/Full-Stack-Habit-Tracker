package loginpage;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;

public class LoginScreen {
    private BorderPane root;
    private Runnable onLoginSuccess;
    private Runnable onSignup;

    public LoginScreen() {
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

        // Right panel with the login form
        VBox right = new VBox(10);
        right.setPadding(new Insets(40));
        right.setAlignment(Pos.CENTER_LEFT);
        right.setStyle("-fx-background-color: white;");

        Label heading = new Label("Login");
        heading.getStyleClass().add("form-heading");

        TextField emailField = new TextField();
        emailField.setPromptText("Email");

        PasswordField passwordField = new PasswordField();
        passwordField.setPromptText("Password");

        Label msg = new Label();
        msg.setStyle("-fx-text-fill: red;");

        Button loginBtn = new Button("Login");
        loginBtn.getStyleClass().add("login-btn");

        Hyperlink signupLink = new Hyperlink("Signup");
        signupLink.setOnAction(e -> {
            System.out.println("Signup link clicked");
            if (onSignup != null) {
                onSignup.run();
            } else {
                System.out.println("onSignup is null");
            }
        });

        loginBtn.setOnAction(e -> {
            String email = emailField.getText().trim();
            String password = passwordField.getText();

            if (!email.matches("^\\S+@\\S+\\.\\S+$")) {
                msg.setText("Invalid email format.");
            } else if (password.isEmpty()) {
                msg.setText("Please enter a password.");
            } else {
                Integer userId = UserDatabase.validateUser(email, password);
                if (userId != null) {
                    LoginScreen.setLoggedInUserId(userId);
                    if (onLoginSuccess != null) {
                        onLoginSuccess.run();
                    }
                    msg.setText("Login successful!");
                    msg.setStyle("-fx-text-fill: green;");
                } else {
                    msg.setText("Invalid email or password.");
                }
            }
        });

        right.getChildren().addAll(heading, emailField, passwordField, loginBtn, msg, signupLink);

        root.setLeft(left);
        root.setCenter(right);
    }

    public BorderPane getRoot() {
        return root;
    }

    public void setOnLoginSuccess(Runnable r) {
        this.onLoginSuccess = r;
    }

    public void setOnSignup(Runnable r) {
        this.onSignup = r;
    }

    public static void setLoggedInUserId(Integer userId) {
        LoginScreen.loggedInUserId = userId;
    }

    public static Integer getLoggedInUserId() {
        return loggedInUserId;
    }

    public static void clearLoggedInUser() {
        loggedInUserId = null;
    }

    private static Integer loggedInUserId = null;
}