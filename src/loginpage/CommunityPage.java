package loginpage;

import dao.CommunityDAO;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

public class CommunityPage {

    private static final Color PRIMARY_PURPLE = Color.web("#7c4dff");
    private static final Color LIGHT_PURPLE_BG = Color.web("#f5f0ff");
    private static final Color DARK_PURPLE = Color.web("#30195a");
    private Integer loggedInUserId;
    private String username;
    private final Runnable onPostAdded; // Callback for posts, likes, comments

    public CommunityPage(Runnable onPostAdded) {
        this.loggedInUserId = LoginScreen.getLoggedInUserId();
        this.username = fetchUsername(loggedInUserId);
        this.onPostAdded = onPostAdded;
    }

    public VBox getCommunityContent() {
        // Check if user is logged in
        if (loggedInUserId == null) {
            VBox errorBox = new VBox(10);
            errorBox.setAlignment(Pos.CENTER);
            errorBox.setPadding(new Insets(20));
            Label errorLabel = new Label("Please log in to view the community page.");
            errorLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, 16));
            errorLabel.setTextFill(Color.RED);
            errorBox.getChildren().add(errorLabel);
            return errorBox;
        }

        // Root container
        VBox root = new VBox();
        root.setStyle("-fx-background-color: #f5f0ff;");

        // Header
        HBox header = createHeader();

        // Content area with posts
        VBox content = new VBox(20);
        content.setPadding(new Insets(20, 40, 20, 40));

        // Fetch and display posts
        for (CommunityDAO.Post post : CommunityDAO.getAllPosts()) {
            VBox postNode = createPost(
                    post.getUsername(),
                    "Habit Enthusiast", // Placeholder badge
                    post.getTimeInfo() + (post.getCategory() != null ? " • " + post.getCategory() : ""),
                    post.getContent(),
                    post.getLikes(),
                    post.getComments().size(),
                    post.getId(),
                    post.getComments()
            );
            content.getChildren().add(postNode);
        }

        // Scrollable content
        ScrollPane scrollPane = new ScrollPane(content);
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle("-fx-background-color: transparent; -fx-background: transparent;");

        root.getChildren().addAll(header, scrollPane);

        return root;
    }

    private HBox createHeader() {
        HBox header = new HBox();
        header.setPadding(new Insets(15, 40, 15, 40));
        header.setAlignment(Pos.CENTER_LEFT);
        header.setStyle("-fx-background-color: #f5f0ff;");

        // Left side with icon and title
        HBox leftSide = new HBox(10);
        leftSide.setAlignment(Pos.CENTER_LEFT);

        Circle personIcon = new Circle(15, PRIMARY_PURPLE);
        Label communityLabel = new Label("Community");
        communityLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, 22));
        communityLabel.setTextFill(DARK_PURPLE);

        leftSide.getChildren().addAll(personIcon, communityLabel);

        // Right side with share button
        Button shareButton = new Button("Share Progress");
        shareButton.setStyle("-fx-background-color: #7c4dff; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 10 20; -fx-background-radius: 20;");
        shareButton.setOnAction(e -> showShareProgressForm());

        // Spacer
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        header.getChildren().addAll(leftSide, spacer, shareButton);

        return header;
    }

    private VBox createPost(String name, String badge, String timeInfo, String contentText, int likes, int commentCount, int postId, List<CommunityDAO.Comment> comments) {
        VBox post = new VBox(15);
        post.setPadding(new Insets(20));
        post.setStyle("-fx-background-color: white; -fx-background-radius: 10;");

        // User info section
        VBox userInfo = new VBox(5);

        // Name and badge
        HBox nameSection = new HBox(10);
        nameSection.setAlignment(Pos.CENTER_LEFT);

        Label nameLabel = new Label(name);
        nameLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, 16));
        nameLabel.setTextFill(DARK_PURPLE);

        Label badgeLabel = new Label(badge);
        badgeLabel.setStyle("-fx-background-color: #e0e7ff; -fx-text-fill: #7c4dff; -fx-padding: 5 10; -fx-background-radius: 15;");

        nameSection.getChildren().addAll(nameLabel, badgeLabel);

        // Time info
        Label timeLabel = new Label(timeInfo);
        timeLabel.setTextFill(Color.GRAY);

        userInfo.getChildren().addAll(nameSection, timeLabel);

        // Content
        Label contentLabel = new Label(contentText);
        contentLabel.setWrapText(true);
        contentLabel.setTextFill(DARK_PURPLE);

        // Separator
        Region separator = new Region();
        separator.setPrefHeight(1);
        separator.setStyle("-fx-background-color: #f0f0f0;");
        separator.setPrefWidth(Double.MAX_VALUE);

        // Engagement section
        HBox engagement = new HBox(15);
        engagement.setPadding(new Insets(10, 0, 0, 0));

        // Like button with count
        HBox likeSection = new HBox(8);
        likeSection.setAlignment(Pos.CENTER_LEFT);

        Button likeButton = new Button("Like");
        boolean hasLiked = CommunityDAO.hasUserLikedPost(loggedInUserId, postId);
        likeButton.setStyle(hasLiked ?
                "-fx-background-color: #ff0000; -fx-text-fill: white; -fx-background-radius: 5;" :
                "-fx-background-color: #f5f5f5; -fx-text-fill: #30195a; -fx-background-radius: 5;");
        likeButton.setDisable(hasLiked);
        likeButton.setOnAction(e -> {
            if (CommunityDAO.addLike(loggedInUserId, postId)) {
                if (onPostAdded != null) {
                    onPostAdded.run(); // Refresh UI
                }
            }
        });

        Label likeCount = new Label("(" + likes + ")");
        likeCount.setTextFill(Color.GRAY);

        likeSection.getChildren().addAll(likeButton, likeCount);

        // Comment button with count
        HBox commentSection = new HBox(8);
        commentSection.setAlignment(Pos.CENTER_LEFT);

        Button commentButton = new Button("Comment");
        commentButton.setStyle("-fx-background-color: #f5f5f5; -fx-text-fill: #30195a; -fx-background-radius: 5;");
        commentButton.setOnAction(e -> showCommentForm(postId));

        Label commentCountLabel = new Label("(" + commentCount + ")");
        commentCountLabel.setTextFill(Color.GRAY);

        commentSection.getChildren().addAll(commentButton, commentCountLabel);

        // Share button (placeholder)
        Button shareButton = new Button("Share");
        shareButton.setStyle("-fx-background-color: #f5f5f5; -fx-text-fill: #30195a; -fx-background-radius: 5;");

        engagement.getChildren().addAll(likeSection, commentSection, shareButton);

        // Comments section
        VBox commentsSection = new VBox(10);
        commentsSection.setPadding(new Insets(10, 0, 0, 0));
        for (CommunityDAO.Comment comment : comments) {
            VBox commentNode = createCommentNode(comment);
            commentsSection.getChildren().add(commentNode);
        }

        post.getChildren().addAll(userInfo, contentLabel, separator, engagement, commentsSection);

        return post;
    }

    private VBox createCommentNode(CommunityDAO.Comment comment) {
        VBox commentNode = new VBox(5);
        commentNode.setPadding(new Insets(5, 10, 5, 10));
        commentNode.setStyle("-fx-background-color: #f8f8f8; -fx-background-radius: 5;");

        HBox header = new HBox(10);
        Label usernameLabel = new Label(comment.getUsername());
        usernameLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, 12));
        usernameLabel.setTextFill(DARK_PURPLE);

        Label timeLabel = new Label(comment.getTimeInfo());
        timeLabel.setFont(Font.font("Segoe UI", FontWeight.NORMAL, 10));
        timeLabel.setTextFill(Color.GRAY);

        header.getChildren().addAll(usernameLabel, timeLabel);

        Label contentLabel = new Label(comment.getContent());
        contentLabel.setWrapText(true);
        contentLabel.setFont(Font.font("Segoe UI", FontWeight.NORMAL, 12));
        contentLabel.setTextFill(DARK_PURPLE);

        commentNode.getChildren().addAll(header, contentLabel);
        return commentNode;
    }

    private void showShareProgressForm() {
        Stage popup = new Stage();
        popup.setTitle("Share Your Progress");

        VBox form = new VBox(15);
        form.setPadding(new Insets(20));
        form.setStyle("-fx-background-color: #f5f0ff; -fx-background-radius: 10;");
        form.setAlignment(Pos.CENTER);

        Label titleLabel = new Label("Share Your Progress");
        titleLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, 20));
        titleLabel.setTextFill(DARK_PURPLE);

        TextArea contentField = new TextArea();
        contentField.setPromptText("What's your progress? Share your achievements!");
        contentField.setPrefRowCount(5);
        contentField.setPrefColumnCount(30);
        contentField.setWrapText(true);
        contentField.setStyle("-fx-background-color: white; -fx-background-radius: 5; -fx-border-color: #e0e7ff; -fx-border-radius: 5;");

        ComboBox<String> categoryBox = new ComboBox<>();
        categoryBox.getItems().addAll("Meditation", "Exercise", "Study", "Productivity", "Other");
        categoryBox.setPromptText("Select Category");
        categoryBox.setStyle("-fx-background-color: white; -fx-background-radius: 5; -fx-border-color: #e0e7ff; -fx-border-radius: 5;");

        Button submitButton = new Button("Share");
        submitButton.setStyle("-fx-background-color: #7c4dff; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 10 20; -fx-background-radius: 20;");

        Label errorLabel = new Label();
        errorLabel.setTextFill(Color.RED);
        errorLabel.setFont(Font.font("Segoe UI", FontWeight.NORMAL, 12));

        submitButton.setOnAction(e -> {
            String content = contentField.getText().trim();
            String category = categoryBox.getValue();

            if (content.isEmpty()) {
                errorLabel.setText("Please enter some content.");
            } else {
                CommunityDAO.addPost(loggedInUserId, username, content, category);
                popup.close();
                if (onPostAdded != null) {
                    onPostAdded.run(); // Trigger UI refresh
                }
            }
        });

        form.getChildren().addAll(titleLabel, contentField, categoryBox, submitButton, errorLabel);

        Scene scene = new Scene(form, 400, 350);
        popup.setScene(scene);
        popup.show();
    }

    private void showCommentForm(int postId) {
        Stage popup = new Stage();
        popup.setTitle("Add Comment");

        VBox form = new VBox(15);
        form.setPadding(new Insets(20));
        form.setStyle("-fx-background-color: #f5f0ff; -fx-background-radius: 10;");
        form.setAlignment(Pos.CENTER);

        Label titleLabel = new Label("Add a Comment");
        titleLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, 20));
        titleLabel.setTextFill(DARK_PURPLE);

        TextArea contentField = new TextArea();
        contentField.setPromptText("Write your comment...");
        contentField.setPrefRowCount(4);
        contentField.setPrefColumnCount(30);
        contentField.setWrapText(true);
        contentField.setStyle("-fx-background-color: white; -fx-background-radius: 5; -fx-border-color: #e0e7ff; -fx-border-radius: 5;");

        Button submitButton = new Button("Submit Comment");
        submitButton.setStyle("-fx-background-color: #7c4dff; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 10 20; -fx-background-radius: 20;");

        Label errorLabel = new Label();
        errorLabel.setTextFill(Color.RED);
        errorLabel.setFont(Font.font("Segoe UI", FontWeight.NORMAL, 12));

        submitButton.setOnAction(e -> {
            String content = contentField.getText().trim();
            if (content.isEmpty()) {
                errorLabel.setText("Please enter a comment.");
            } else {
                CommunityDAO.addComment(postId, loggedInUserId, username, content);
                popup.close();
                if (onPostAdded != null) {
                    onPostAdded.run(); // Trigger UI refresh
                }
            }
        });

        form.getChildren().addAll(titleLabel, contentField, submitButton, errorLabel);

        Scene scene = new Scene(form, 400, 300);
        popup.setScene(scene);
        popup.show();
    }

    private String fetchUsername(Integer userId) {
        if (userId == null) return "Unknown";
        String sql = "SELECT username FROM users WHERE id = ?";
        try (Connection conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/habit_tracker", "root", "hardik888");
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, userId);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getString("username");
            }
        } catch (SQLException e) {
            System.err.println("Error fetching username for user_id " + userId + ": " + e.getMessage());
        }
        return "Unknown";
    }
}