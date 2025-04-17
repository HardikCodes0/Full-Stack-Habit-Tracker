package loginpage;

import dao.TaskDAO;
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.effect.DropShadow;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import model.Task;

import java.sql.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Objects;

public class Main extends Application {

    private VBox contentVBox;
    private Integer loggedInUserId;
    private Stage primaryStage;

    @Override
    public void start(Stage stage) {
        primaryStage = stage;
        initializeApplication();
    }

    private void initializeApplication() {
        loggedInUserId = LoginScreen.getLoggedInUserId();
        if (loggedInUserId == null) {
            LoginScreen loginScreen = new LoginScreen();
            loginScreen.setOnLoginSuccess(() -> {
                loggedInUserId = LoginScreen.getLoggedInUserId();
                System.out.println("Login successful, loggedInUserId: " + loggedInUserId);
                if (loggedInUserId != null) {
                    Scene mainScene = createScene(primaryStage);
                    if (mainScene != null) {
                        primaryStage.setScene(mainScene);
                    }
                }
            });
            loginScreen.setOnSignup(() -> {
                System.out.println("Navigating to SignupScreen");
                SignupScreen signupScreen = new SignupScreen();
                signupScreen.setOnBackToLogin(() -> initializeApplication());
                primaryStage.setScene(new Scene(signupScreen.getRoot(), 900, 700));
            });
            primaryStage.setScene(new Scene(loginScreen.getRoot(), 900, 700));
            primaryStage.setTitle("Habit Tracker");
            primaryStage.show();
            return;
        }

        Scene mainScene = createScene(primaryStage);
        if (mainScene != null) {
            primaryStage.setScene(mainScene);
        }
        primaryStage.setTitle("Habit Tracker");
        primaryStage.show();
    }

    private Scene createScene(Stage stage) {
        Image bg = null;
        try {
            bg = new Image(Objects.requireNonNull(getClass().getResource("/bg3.png")).toExternalForm());
        } catch (NullPointerException e) {
            System.err.println("Warning: bg3.png not found - " + e.getMessage());
            bg = new Image("https://via.placeholder.com/2500x2000");
        }
        ImageView bgView = new ImageView(bg);
        bgView.setFitWidth(2500);
        bgView.setFitHeight(2000);

        Font titleFont = Font.loadFont(getClass().getResourceAsStream("/title.ttf"), 28);
        if (titleFont == null) titleFont = Font.font("Arial", FontWeight.BOLD, 28);
        Font bodyFont = Font.loadFont(getClass().getResourceAsStream("/semititle.ttf"), 20);
        if (bodyFont == null) bodyFont = Font.font("Arial", FontWeight.NORMAL, 20);

        HBox navbar = new HBox(20);
        navbar.setPadding(new Insets(15));
        navbar.setAlignment(Pos.CENTER_LEFT);
        navbar.setStyle("-fx-background-color: rgba(255, 255, 255, 0.2); -fx-background-radius: 10;");

        Label logo = new Label("Habit Tracker");
        logo.setFont(Font.font("System", FontWeight.BOLD, 24));
        logo.setStyle("-fx-text-fill: #282828;");

        Button homebtn = new Button("Home");
        Button communitybtn = new Button("Community");
        Button settingsbtn = new Button("Settings");
        Button myGrowthBtn = new Button("MyGrowth");
        Button logoutBtn = new Button("Logout");

        for (Button btn : new Button[]{homebtn, communitybtn, settingsbtn, myGrowthBtn, logoutBtn}) {
            btn.setStyle("""
                -fx-background-color: white;
                -fx-text-fill: #8257d5;
                -fx-font-size: 14px;
                -fx-font-weight: bold;
                -fx-border-color:#8257d5;
                -fx-border-radius: 10px;
                -fx-background-radius: 10;
                -fx-padding: 8 16;
                -fx-cursor: hand;
            """);
        }

        homebtn.setOnAction(e -> {
            System.out.println("Navigating to Home page");
            contentVBox.getChildren().clear();
            contentVBox.getChildren().addAll(
                    createWelcomeContent(),
                    createAddTaskButton()
            );
            loadTasks();
        });

        communitybtn.setOnAction(e -> loadCommunityPage());

        myGrowthBtn.setOnAction(e -> {
            System.out.println("Navigating to MyGrowth page");
            contentVBox.getChildren().clear();
            contentVBox.getChildren().add(createGrowthContent());
        });

        logoutBtn.setOnAction(e -> {
            System.out.println("Logging out, clearing loggedInUserId");
            LoginScreen.clearLoggedInUser();
            loggedInUserId = null;
            initializeApplication();
        });

        navbar.getChildren().addAll(logo, homebtn, communitybtn, settingsbtn, myGrowthBtn, logoutBtn);

        contentVBox = new VBox(20);
        contentVBox.setPadding(new Insets(30));
        contentVBox.setAlignment(Pos.TOP_LEFT);
        contentVBox.setStyle("-fx-background-color: rgba(255,255,255,0.6); -fx-background-radius: 10;");

        contentVBox.getChildren().addAll(
                createWelcomeContent(),
                createAddTaskButton()
        );
        loadTasks();

        ScrollPane scrollPane = new ScrollPane(contentVBox);
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle("-fx-background: transparent; -fx-background-color: transparent;");
        scrollPane.getContent().setStyle("-fx-background-color: transparent;");

        BorderPane mainLayout = new BorderPane();
        mainLayout.setTop(navbar);
        mainLayout.setCenter(scrollPane);

        StackPane root = new StackPane(bgView, mainLayout);
        Scene scene = new Scene(root, 900, 700);

        try {
            String css = getClass().getResource("/style.css").toExternalForm();
            scene.getStylesheets().add(css);
        } catch (NullPointerException e) {
            System.err.println("Warning: style.css not found - " + e.getMessage());
        }

        return scene;
    }

    private void loadCommunityPage() {
        System.out.println("Navigating to Community page");
        contentVBox.getChildren().clear();
        CommunityPage communityPage = new CommunityPage(this::loadCommunityPage); // Pass refresh callback
        contentVBox.getChildren().add(communityPage.getCommunityContent());
    }

    private VBox createWelcomeContent() {
        VBox welcomeBox = new VBox(10);
        Label welcomeLabel = new Label("Welcome to Habit Tracker");
        welcomeLabel.setFont(Font.font("System", FontWeight.BOLD, 28));
        welcomeLabel.setStyle("-fx-text-fill: #282828;");

        LocalDate currentDate = LocalDate.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd MMMM yyyy");
        Label date = new Label("Date: " + currentDate.format(formatter));
        date.setFont(Font.font("System", FontWeight.NORMAL, 14));
        date.setStyle("-fx-text-fill: #787878;");

        Label todayLabel = new Label("Today's Tasks");
        todayLabel.setFont(Font.font("System", FontWeight.BOLD, 20));
        todayLabel.setStyle("-fx-text-fill: #282828;");

        welcomeBox.getChildren().addAll(welcomeLabel, date, todayLabel);
        return welcomeBox;
    }

    private Button createAddTaskButton() {
        Button addtask = new Button("Add Task");
        addtask.setStyle("""
            -fx-background-color: #8257d5;
            -fx-text-fill: white;
            -fx-font-weight: bold;
            -fx-padding: 10px 20px;
            -fx-background-radius: 20;
            """);
        addtask.setOnAction(e -> showAddTaskForm());
        return addtask;
    }

    private VBox createGrowthContent() {
        VBox growthContent = new VBox(20);
        growthContent.setStyle("-fx-background-color: #f5f0ff;");
        growthContent.setPadding(new Insets(20));

        HBox header = new HBox();
        header.setAlignment(Pos.CENTER_LEFT);
        header.setPrefHeight(60);
        header.setSpacing(20);

        Label title = new Label("Growth");
        title.setFont(Font.font("Segoe UI", FontWeight.BOLD, 28));
        title.setStyle("-fx-text-fill: #30195a;");

        HBox searchBox = new HBox();
        searchBox.setAlignment(Pos.CENTER_LEFT);
        searchBox.setStyle("-fx-background-color: white; -fx-background-radius: 20; -fx-border-radius: 20;");
        searchBox.setPadding(new Insets(5, 15, 5, 15));

        Text searchIcon = new Text("🔍");
        searchIcon.setFont(Font.font("Segoe UI", FontWeight.NORMAL, 14));

        TextField searchField = new TextField();
        searchField.setPromptText("Search growth metrics...");
        searchField.setPrefWidth(300);
        searchField.setFont(Font.font("Segoe UI", FontWeight.NORMAL, 14));
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
        levelTitle.setFont(Font.font("Segoe UI", FontWeight.BOLD, 14));
        levelTitle.setStyle("-fx-text-fill: #30195a;");

        int currentLevel = (loggedInUserId != null) ? TaskDAO.getUserLevel(loggedInUserId) : 0;
        String levelName = getLevelName(currentLevel);
        double progress = (loggedInUserId != null) ? TaskDAO.getUserProgress(loggedInUserId) : 0.0;

        Label levelInfo = new Label("Level " + currentLevel + " - " + levelName);
        levelInfo.setFont(Font.font("Segoe UI", FontWeight.BOLD, 28));
        levelInfo.setStyle("-fx-text-fill: #7c4dff;");

        StackPane progressBar = new StackPane();
        Rectangle track = new Rectangle(700, 10);
        track.setFill(Color.LIGHTGRAY);
        track.setArcWidth(10);
        track.setArcHeight(10);

        Rectangle bar = new Rectangle(700 * progress, 10);
        bar.setFill(Color.web("#7c4dff"));
        bar.setArcWidth(10);
        bar.setArcHeight(10);

        progressBar.getChildren().addAll(track, bar);
        StackPane.setAlignment(bar, Pos.CENTER_LEFT);

        Label progressLabel = new Label(String.format("%.0f%% to Level %d", progress * 100, currentLevel + 1));
        progressLabel.setFont(Font.font("Segoe UI", FontWeight.NORMAL, 12));
        progressLabel.setStyle("-fx-text-fill: #787878;");

        levelSection.getChildren().addAll(levelTitle, levelInfo, progressBar, progressLabel);

        VBox performanceSection = new VBox(15);
        performanceSection.setStyle("-fx-background-color: white; -fx-background-radius: 10;");
        performanceSection.setPadding(new Insets(20));

        cardShadow = new DropShadow();
        cardShadow.setRadius(5.0);
        cardShadow.setOffsetX(0.0);
        cardShadow.setOffsetY(2.0);
        cardShadow.setColor(Color.rgb(0, 0, 0, 0.1));
        performanceSection.setEffect(cardShadow);

        Label performanceTitle = new Label("Performance (Last 30 Days)");
        performanceTitle.setFont(Font.font("Segoe UI", FontWeight.BOLD, 14));
        performanceTitle.setStyle("-fx-text-fill: #30195a;");

        int habitsCompleted = (loggedInUserId != null) ? getHabitsCompletedLast30Days() : 0;
        int streaksMaintained = 3; // Placeholder
        int goalsAchieved = 2; // Placeholder

        HBox metricsContainer = new HBox(20);
        metricsContainer.setAlignment(Pos.CENTER);

        metricsContainer.getChildren().addAll(
                createMetricCard("Habits Completed", habitsCompleted, "✅"),
                createMetricCard("Streaks Maintained", streaksMaintained, "🔥"),
                createMetricCard("Goals Achieved", goalsAchieved, "🎯")
        );

        performanceSection.getChildren().addAll(performanceTitle, metricsContainer);

        growthContent.getChildren().addAll(header, levelSection, performanceSection);

        return growthContent;
    }

    private VBox createMetricCard(String title, int value, String iconText) {
        VBox card = new VBox(10);
        card.setStyle("-fx-background-color: #f8f3ff; -fx-background-radius: 10;");
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
        valueLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, 28));
        valueLabel.setStyle("-fx-text-fill: #7c4dff;");

        Label titleLabel = new Label(title);
        titleLabel.setFont(Font.font("Segoe UI", FontWeight.NORMAL, 14));
        titleLabel.setStyle("-fx-text-fill: #30195a;");
        titleLabel.setWrapText(true);
        titleLabel.setMaxWidth(180);
        titleLabel.setAlignment(Pos.CENTER);

        card.getChildren().addAll(icon, valueLabel, titleLabel);
        card.setCursor(Cursor.HAND);

        card.setOnMouseEntered(e -> {
            card.setStyle("-fx-background-color: #e6d8ff; -fx-background-radius: 10;");
        });
        card.setOnMouseExited(e -> {
            card.setStyle("-fx-background-color: #f8f3ff; -fx-background-radius: 10;");
        });

        return card;
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
            if (loggedInUserId != null) {
                pstmt.setInt(1, loggedInUserId);
                ResultSet rs = pstmt.executeQuery();
                if (rs.next()) return rs.getInt("completed");
            }
        } catch (SQLException e) {
            System.err.println("Error fetching habits completed: " + e.getMessage());
        }
        return 0;
    }

    private void showAddTaskForm() {
        if (loggedInUserId == null) {
            new Alert(Alert.AlertType.ERROR, "Please log in to add tasks").show();
            return;
        }

        Stage popup = new Stage();
        VBox form = new VBox(15);
        form.setPadding(new Insets(20));
        form.setStyle("-fx-background-color: white; -fx-background-radius: 10;");
        form.setAlignment(Pos.CENTER);

        TextField titleField = new TextField();
        titleField.setPromptText("Enter task title");

        TextField noteField = new TextField();
        noteField.setPromptText("Enter note");

        Spinner<Integer> hourSpinner = new Spinner<>(1, 12, 6);
        Spinner<Integer> minuteSpinner = new Spinner<>(0, 59, 0);
        ComboBox<String> ampmBox = new ComboBox<>();
        ampmBox.getItems().addAll("AM", "PM");
        ampmBox.setValue("PM");

        HBox timeBox = new HBox(10, new Label("Time:"), hourSpinner, new Label(":"), minuteSpinner, ampmBox);
        timeBox.setAlignment(Pos.CENTER);

        Button saveBtn = new Button("Save Task");
        saveBtn.setStyle("""
            -fx-background-color: #8257d5;
            -fx-text-fill: white;
            -fx-background-radius: 8;
            -fx-cursor: hand;
            """);

        saveBtn.setOnAction(e -> {
            String title = titleField.getText();
            String note = noteField.getText();
            String time = String.format("%02d:%02d %s", hourSpinner.getValue(), minuteSpinner.getValue(), ampmBox.getValue());

            if (!title.isEmpty() && !time.isEmpty()) {
                Task task = new Task(title, note, time);
                TaskDAO.insertTask(task, loggedInUserId);
                popup.close();
                loadTasks();
            }
        });

        form.getChildren().addAll(new Label("Add New Task"), titleField, noteField, timeBox, saveBtn);
        Scene scene = new Scene(form, 320, 300);
        popup.setScene(scene);
        popup.setTitle("Add Task");
        popup.show();
    }

    private void loadTasks() {
        contentVBox.getChildren().removeIf(node -> node instanceof VBox && ((VBox) node).getChildren().size() > 0);
        if (loggedInUserId != null) {
            List<Task> tasks = TaskDAO.getTasksByUser(loggedInUserId);
            for (Task task : tasks) {
                VBox taskCard = createTaskCard(task);
                contentVBox.getChildren().add(taskCard);
            }
        }
    }

    private VBox createTaskCard(Task task) {
        VBox card = new VBox(8);
        card.setPadding(new Insets(15));
        card.setPrefWidth(300);
        card.setStyle("""
                -fx-background-color: rgba(255,255,255,0.85);
                -fx-border-color: #f0f0f0;
                -fx-border-width: 1px;
                -fx-border-radius: 10;
                -fx-background-radius: 10;
            """);

        Label lblTitle = new Label(task.getTitle());
        lblTitle.setFont(Font.font("System", FontWeight.BOLD, 16));
        lblTitle.setStyle("-fx-text-fill: #282828;");

        if (task.isDone() && loggedInUserId != null) {
            lblTitle.setStyle("-fx-text-fill: #4caf50; -fx-font-weight: bold;");
            int currentLevel = TaskDAO.getUserLevel(loggedInUserId);
            int previousLevel = currentLevel - 1;
            int xpThreshold = previousLevel * 100;
            int newXp = TaskDAO.getTotalXp(loggedInUserId);
            if (newXp >= currentLevel * 100) {
                showLevelUpPopup(currentLevel);
            }
        }

        Label lblNote = new Label(task.getNote());
        lblNote.setFont(Font.font("System", FontWeight.NORMAL, 14));
        lblNote.setStyle("-fx-text-fill: #787878;");

        Label lblTime = new Label(task.getTime());
        lblTime.setFont(Font.font("System", FontWeight.NORMAL, 14));
        lblTime.setStyle("-fx-text-fill: #787878;");

        HBox btnBox = new HBox(10);
        btnBox.setAlignment(Pos.CENTER_LEFT);

        Button doneBtn = new Button("Mark Done");
        doneBtn.setStyle("""
                -fx-background-color: #8257d5;
                -fx-text-fill: white;
                -fx-padding: 8px 15px;
                -fx-background-radius: 20;
                """);
        doneBtn.setDisable(task.isDone());

        doneBtn.setOnAction(e -> {
            TaskDAO.markTaskAsDone(task.getId());
            loadTasks();
        });

        Button removeBtn = new Button("Remove");
        removeBtn.setStyle("""
                -fx-background-color: #e6f7e9;
                -fx-text-fill: #4caf50;
                -fx-border-color: #4caf50;
                -fx-border-width: 1px;
                -fx-padding: 8px 15px;
                -fx-background-radius: 20;
                -fx-border-radius: 20;
                """);

        removeBtn.setOnAction(e -> {
            TaskDAO.deleteTask(task.getId());
            contentVBox.getChildren().remove(card);
        });

        btnBox.getChildren().addAll(doneBtn, removeBtn);
        card.getChildren().addAll(lblTitle, lblNote, lblTime, btnBox);
        return card;
    }

    private void showLevelUpPopup(int newLevel) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Level Up!");
        alert.setHeaderText("Congratulations!");
        alert.setContentText("You’ve reached Level " + newLevel + "! Keep up the great work!");

        DialogPane dialogPane = alert.getDialogPane();
        dialogPane.setStyle("""
            -fx-background-color: #f5f0ff;
            -fx-text-fill: #30195a;
            -fx-font-family: "Segoe UI";
            -fx-padding: 20;
            -fx-background-radius: 10;
        """);
        dialogPane.lookupButton(ButtonType.OK).setStyle("""
            -fx-background-color: #7c4dff;
            -fx-text-fill: white;
            -fx-font-weight: bold;
            -fx-padding: 8 16;
            -fx-background-radius: 20;
        """);

        alert.showAndWait();
    }

    public static void main(String[] args) {
        launch(args);
    }
}