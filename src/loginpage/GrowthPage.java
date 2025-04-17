package loginpage;

import dao.TaskDAO;
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.scene.effect.DropShadow;
import javafx.scene.Cursor;
import loginpage.LoginScreen;

import java.sql.*;

public class GrowthPage extends Application {

    private static final Color PRIMARY_PURPLE = Color.web("#7c4dff");
    private static final Color LIGHT_PURPLE_BG = Color.web("#f5f0ff");
    private static final Color DARK_PURPLE = Color.web("#30195a");
    private static final Color LIGHT_PURPLE = Color.web("#f8f3ff");

    private Font titleFont;
    private Font regularFont;
    private Font boldFont;
    private Font smallFont;
    private Integer loggedInUserId;

    @Override
    public void start(Stage primaryStage) {
        loggedInUserId = LoginScreen.getLoggedInUserId();
        if (loggedInUserId == null) {
            new Alert(Alert.AlertType.ERROR, "Please log in to view growth stats").show();
            primaryStage.close();
            return;
        }

        initializeFonts();

        VBox root = new VBox(20);
        root.setStyle("-fx-background-color: #f5f0ff;");
        root.setPadding(new Insets(20));

        HBox header = createHeader();
        VBox levelSection = createLevelSection();
        VBox performanceSection = createPerformanceSection();

        root.getChildren().addAll(header, levelSection, performanceSection);

        ScrollPane scrollPane = new ScrollPane(root);
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle("-fx-background-color: #f5f0ff;");
        scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);

        Scene scene = new Scene(scrollPane, 800, 600);
        primaryStage.setTitle("Growth");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    private void initializeFonts() {
        try {
            titleFont = Font.font("Segoe UI", FontWeight.BOLD, 28);
            regularFont = Font.font("Segoe UI", FontWeight.NORMAL, 14);
            boldFont = Font.font("Segoe UI", FontWeight.BOLD, 14);
            smallFont = Font.font("Segoe UI", FontWeight.NORMAL, 12);
        } catch (Exception e) {
            try {
                titleFont = Font.font("SF Pro", FontWeight.BOLD, 28);
                regularFont = Font.font("SF Pro", FontWeight.NORMAL, 14);
                boldFont = Font.font("SF Pro", FontWeight.BOLD, 14);
                smallFont = Font.font("SF Pro", FontWeight.NORMAL, 12);
            } catch (Exception ex) {
                titleFont = Font.font("System", FontWeight.BOLD, 28);
                regularFont = Font.font("System", FontWeight.NORMAL, 14);
                boldFont = Font.font("System", FontWeight.BOLD, 14);
                smallFont = Font.font("System", FontWeight.NORMAL, 12);
            }
        }
    }

    private HBox createHeader() {
        HBox header = new HBox();
        header.setAlignment(Pos.CENTER_LEFT);
        header.setPrefHeight(60);
        header.setSpacing(20);

        Label title = new Label("Growth");
        title.setFont(titleFont);
        title.setTextFill(DARK_PURPLE);

        HBox searchBox = new HBox();
        searchBox.setAlignment(Pos.CENTER_LEFT);
        searchBox.setStyle("-fx-background-color: white; -fx-background-radius: 20; -fx-border-radius: 20;");
        searchBox.setPadding(new Insets(5, 15, 5, 15));

        Text searchIcon = new Text("🔍");
        searchIcon.setFont(regularFont);

        TextField searchField = new TextField();
        searchField.setPromptText("Search growth metrics...");
        searchField.setPrefWidth(300);
        searchField.setFont(regularFont);
        searchField.setStyle("-fx-background-color: transparent; -fx-border-color: transparent;");

        searchBox.getChildren().addAll(searchIcon, searchField);

        DropShadow dropShadow = new DropShadow();
        dropShadow.setRadius(2.0);
        dropShadow.setOffsetX(0.0);
        dropShadow.setOffsetY(1.0);
        dropShadow.setColor(Color.rgb(0, 0, 0, 0.1));
        searchBox.setEffect(dropShadow);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        header.getChildren().addAll(title, spacer, searchBox);
        return header;
    }

    private VBox createLevelSection() {
        VBox levelSection = new VBox(15);
        levelSection.setStyle("-fx-background-color: white; -fx-background-radius: 10;");
        levelSection.setPadding(new Insets(20));

        DropShadow cardShadow = new DropShadow();
        cardShadow.setRadius(5.0);
        cardShadow.setOffsetX(0.0);
        cardShadow.setOffsetY(2.0);
        cardShadow.setColor(Color.rgb(0, 0, 0, 0.1));
        levelSection.setEffect(cardShadow);

        Label levelTitle = new Label("Your Level");
        levelTitle.setFont(boldFont);
        levelTitle.setTextFill(DARK_PURPLE);

        int currentLevel = TaskDAO.getUserLevel(loggedInUserId);
        String levelName = getLevelName(currentLevel);
        double progressToNextLevel = TaskDAO.getUserProgress(loggedInUserId);

        Label levelInfo = new Label("Level " + currentLevel + " - " + levelName);
        levelInfo.setFont(titleFont);
        levelInfo.setTextFill(PRIMARY_PURPLE);

        StackPane progressBar = new StackPane();
        Rectangle track = new Rectangle(700, 10);
        track.setFill(Color.LIGHTGRAY);
        track.setArcWidth(10);
        track.setArcHeight(10);

        Rectangle bar = new Rectangle(700 * progressToNextLevel, 10);
        bar.setFill(PRIMARY_PURPLE);
        bar.setArcWidth(10);
        bar.setArcHeight(10);

        progressBar.getChildren().addAll(track, bar);
        StackPane.setAlignment(bar, Pos.CENTER_LEFT);

        Label progressLabel = new Label(String.format("%.0f%% to Level %d", progressToNextLevel * 100, currentLevel + 1));
        progressLabel.setFont(smallFont);
        progressLabel.setTextFill(Color.GRAY);

        levelSection.getChildren().addAll(levelTitle, levelInfo, progressBar, progressLabel);
        return levelSection;
    }

    private VBox createPerformanceSection() {
        VBox performanceSection = new VBox(15);
        performanceSection.setStyle("-fx-background-color: white; -fx-background-radius: 10;");
        performanceSection.setPadding(new Insets(20));

        DropShadow cardShadow = new DropShadow();
        cardShadow.setRadius(5.0);
        cardShadow.setOffsetX(0.0);
        cardShadow.setOffsetY(2.0);
        cardShadow.setColor(Color.rgb(0, 0, 0, 0.1));
        performanceSection.setEffect(cardShadow);

        Label performanceTitle = new Label("Performance (Last 30 Days)");
        performanceTitle.setFont(boldFont);
        performanceTitle.setTextFill(DARK_PURPLE);

        int habitsCompleted = getHabitsCompletedLast30Days();
        int streaksMaintained = 3; // Implement streak logic if needed
        int goalsAchieved = 2; // Implement goal logic if needed

        HBox metricsContainer = new HBox(20);
        metricsContainer.setAlignment(Pos.CENTER);

        metricsContainer.getChildren().addAll(
                createMetricCard("Habits Completed", habitsCompleted, "✅"),
                createMetricCard("Streaks Maintained", streaksMaintained, "🔥"),
                createMetricCard("Goals Achieved", goalsAchieved, "🎯")
        );

        performanceSection.getChildren().addAll(performanceTitle, metricsContainer);
        return performanceSection;
    }

    private VBox createMetricCard(String title, int value, String iconText) {
        VBox card = new VBox(10);
        card.setStyle("-fx-background-color: " + toHexString(LIGHT_PURPLE) + "; -fx-background-radius: 10;");
        card.setPadding(new Insets(15));
        card.setAlignment(Pos.CENTER);
        card.setPrefWidth(200);

        DropShadow cardShadow = new DropShadow();
        cardShadow.setRadius(3.0);
        cardShadow.setOffsetX(0.0);
        cardShadow.setOffsetY(1.0);
        cardShadow.setColor(Color.rgb(0, 0, 0, 0.1));
        card.setEffect(cardShadow);

        Text icon = new Text(iconText);
        icon.setFont(Font.font("System", FontWeight.NORMAL, 24));

        Label valueLabel = new Label(String.valueOf(value));
        valueLabel.setFont(titleFont);
        valueLabel.setTextFill(PRIMARY_PURPLE);

        Label titleLabel = new Label(title);
        titleLabel.setFont(regularFont);
        titleLabel.setTextFill(DARK_PURPLE);
        titleLabel.setWrapText(true);
        titleLabel.setMaxWidth(180);
        titleLabel.setAlignment(Pos.CENTER);

        card.getChildren().addAll(icon, valueLabel, titleLabel);
        card.setCursor(Cursor.HAND);

        card.setOnMouseEntered(e -> {
            card.setStyle("-fx-background-color: " + toHexString(LIGHT_PURPLE.darker()) + "; -fx-background-radius: 10;");
        });
        card.setOnMouseExited(e -> {
            card.setStyle("-fx-background-color: " + toHexString(LIGHT_PURPLE) + "; -fx-background-radius: 10;");
        });

        return card;
    }

    private String toHexString(Color color) {
        return String.format("#%02X%02X%02X",
                (int) (color.getRed() * 255),
                (int) (color.getGreen() * 255),
                (int) (color.getBlue() * 255));
    }

    private String getLevelName(int level) {
        switch (level) {
            case 1: return "Beginner";
            case 2: return "Novice";
            case 3: return "Habit Builder";
            case 4: return "Pro";
            case 5: return "Master";
            default: return "Legend";
        }
    }

    private int getHabitsCompletedLast30Days() {
        String sql = "SELECT COUNT(*) as completed FROM tasks WHERE user_id = ? AND done = TRUE AND time >= DATE_SUB(CURDATE(), INTERVAL 30 DAY)";
        try (Connection conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/habit_tracker", "root", "hardik888");
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, loggedInUserId);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) return rs.getInt("completed");
        } catch (SQLException e) {
            System.err.println("Error fetching habits completed: " + e.getMessage());
        }
        return 0;
    }

    public static void main(String[] args) {
        launch(args);
    }
}