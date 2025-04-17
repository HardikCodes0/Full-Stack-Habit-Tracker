package dao;

import model.Task;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class TaskDAO {
    private static final String URL = "jdbc:mysql://localhost:3306/habit_tracker";
    private static final String USER = "root";
    private static final String PASSWORD = "hardik888";

    public static List<Task> getTasksByUser(int userId) {
        List<Task> tasks = new ArrayList<>();
        String sql = "SELECT * FROM tasks WHERE user_id = ?";

        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, userId);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                Task task = new Task(
                        rs.getInt("id"),
                        rs.getString("title"),
                        rs.getString("note"),
                        rs.getString("time"),
                        rs.getBoolean("done")
                );
                tasks.add(task);
            }
        } catch (SQLException e) {
            System.err.println("Error fetching tasks for user_id " + userId + ": " + e.getMessage());
            e.printStackTrace();
        }
        return tasks;
    }

    public static void insertTask(Task task, int userId) {
        String sql = "INSERT INTO tasks (title, note, time, done, user_id) VALUES (?, ?, ?, ?, ?)";

        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, task.getTitle());
            pstmt.setString(2, task.getNote());
            pstmt.setString(3, task.getTime());
            pstmt.setBoolean(4, task.isDone());
            pstmt.setInt(5, userId);
            pstmt.executeUpdate();
            updateUserProgress(userId);
        } catch (SQLException e) {
            System.err.println("Error inserting task for user_id " + userId + ": " + e.getMessage());
            e.printStackTrace();
        }
    }

    public static void markTaskAsDone(int taskId) {
        String sqlTask = "UPDATE tasks SET done = TRUE WHERE id = ? AND done = FALSE";
        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
             PreparedStatement pstmtTask = conn.prepareStatement(sqlTask)) {
            pstmtTask.setInt(1, taskId);
            int rowsAffected = pstmtTask.executeUpdate();
            if (rowsAffected > 0) {
                int userId = getUserIdFromTask(taskId);
                if (userId != -1) {
                    updateUserProgress(userId);
                    System.out.println("Marked task " + taskId + " as done for user_id: " + userId);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error marking task as done for task_id " + taskId + ": " + e.getMessage());
            e.printStackTrace();
        }
    }

    public static void deleteTask(int taskId) {
        String sql = "DELETE FROM tasks WHERE id = ?";

        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, taskId);
            pstmt.executeUpdate();
            int userId = getUserIdFromTask(taskId);
            if (userId != -1) updateUserProgress(userId);
        } catch (SQLException e) {
            System.err.println("Error deleting task for task_id " + taskId + ": " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static int getUserIdFromTask(int taskId) {
        String sql = "SELECT user_id FROM tasks WHERE id = ?";
        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, taskId);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) return rs.getInt("user_id");
        } catch (SQLException e) {
            System.err.println("Error fetching user_id for task_id " + taskId + ": " + e.getMessage());
            e.printStackTrace();
        }
        return -1;
    }

    public static void updateUserProgress(int userId) {
        String sqlSelect = "SELECT COUNT(*) as total, SUM(CASE WHEN done = TRUE THEN 1 ELSE 0 END) as completed FROM tasks WHERE user_id = ?";
        String sqlUpdate = "INSERT INTO user_progress (user_id, xp, level, progress) VALUES (?, ?, ?, ?) " +
                "ON DUPLICATE KEY UPDATE xp = ?, level = ?, progress = ?";

        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
             PreparedStatement pstmtSelect = conn.prepareStatement(sqlSelect);
             PreparedStatement pstmtUpdate = conn.prepareStatement(sqlUpdate)) {

            pstmtSelect.setInt(1, userId);
            ResultSet rs = pstmtSelect.executeQuery();
            int completedTasks = 0;
            if (rs.next()) {
                completedTasks = rs.getInt("completed");
            }

            int xp = completedTasks * 10;
            int level = (xp / 100) + 1;
            double progress = (xp % 100) / 100.0;

            pstmtUpdate.setInt(1, userId);
            pstmtUpdate.setInt(2, xp);
            pstmtUpdate.setInt(3, level);
            pstmtUpdate.setDouble(4, progress);
            pstmtUpdate.setInt(5, xp);
            pstmtUpdate.setInt(6, level);
            pstmtUpdate.setDouble(7, progress);
            pstmtUpdate.executeUpdate();

            System.out.println("Updated progress for user " + userId + ": XP=" + xp + ", Level=" + level + ", Progress=" + progress);

        } catch (SQLException e) {
            System.err.println("Error updating user progress for user_id " + userId + ": " + e.getMessage());
            e.printStackTrace();
        }
    }

    public static int getUserLevel(int userId) {
        String sql = "SELECT xp, level, progress FROM user_progress WHERE user_id = ?";
        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, userId);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                int xp = rs.getInt("xp");
                int level = rs.getInt("level");
                double progress = rs.getDouble("progress");
                int calculatedLevel = (xp / 100) + 1;
                double calculatedProgress = (xp % 100) / 100.0;
                if (level != calculatedLevel || Math.abs(progress - calculatedProgress) > 0.01) {
                    System.err.println("Inconsistency for user " + userId + ": DB XP=" + xp +
                            ", DB Level=" + level + ", Calc Level=" + calculatedLevel +
                            ", DB Progress=" + progress + ", Calc Progress=" + calculatedProgress);
                    updateUserProgress(userId);
                    return calculatedLevel;
                }
                System.out.println("Fetched for user " + userId + ": XP=" + xp + ", Level=" + level + ", Progress=" + progress);
                return level;
            }
            System.err.println("No progress record for user_id: " + userId);
            updateUserProgress(userId);
            return 1;
        } catch (SQLException e) {
            System.err.println("Error fetching user level for user_id " + userId + ": " + e.getMessage());
            e.printStackTrace();
        }
        return 0;
    }

    public static double getUserProgress(int userId) {
        String sql = "SELECT xp, level, progress FROM user_progress WHERE user_id = ?";
        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, userId);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                int xp = rs.getInt("xp");
                double progress = rs.getDouble("progress");
                double calculatedProgress = (xp % 100) / 100.0;
                if (Math.abs(progress - calculatedProgress) > 0.01) {
                    System.err.println("Progress inconsistency for user " + userId + ": DB Progress=" + progress +
                            ", Calc Progress=" + calculatedProgress + ", XP=" + xp);
                    updateUserProgress(userId);
                    return calculatedProgress;
                }
                System.out.println("Fetched for user " + userId + ": XP=" + xp + ", Progress=" + progress);
                return progress;
            }
            System.err.println("No progress record for user_id: " + userId);
            updateUserProgress(userId);
            return 0.0;
        } catch (SQLException e) {
            System.err.println("Error fetching user progress for user_id " + userId + ": " + e.getMessage());
            e.printStackTrace();
        }
        return 0.0;
    }

    public static int getTotalXp(int userId) {
        String sql = "SELECT xp, level, progress FROM user_progress WHERE user_id = ?";
        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, userId);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                int xp = rs.getInt("xp");
                System.out.println("Fetched for user " + userId + ": XP=" + xp);
                return xp;
            }
            System.err.println("No progress record for user_id: " + userId);
            updateUserProgress(userId);
            return 0;
        } catch (SQLException e) {
            System.err.println("Error fetching total XP for user_id " + userId + ": " + e.getMessage());
            e.printStackTrace();
        }
        return 0;
    }
}