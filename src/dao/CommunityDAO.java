package dao;

import java.sql.*;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class CommunityDAO {
    private static final String URL = "jdbc:mysql://localhost:3306/habit_tracker";
    private static final String USER = "root";
    private static final String PASSWORD = "hardik888";

    public static class Post {
        private int id;
        private int userId;
        private String username;
        private String content;
        private String category;
        private int likes;
        private String timeInfo;
        private List<Comment> comments;

        public Post(int id, int userId, String username, String content, String category, int likes, String timeInfo, List<Comment> comments) {
            this.id = id;
            this.userId = userId;
            this.username = username;
            this.content = content;
            this.category = category;
            this.likes = likes;
            this.timeInfo = timeInfo;
            this.comments = comments != null ? comments : new ArrayList<>();
        }

        public int getId() { return id; }
        public int getUserId() { return userId; }
        public String getUsername() { return username; }
        public String getContent() { return content; }
        public String getCategory() { return category; }
        public int getLikes() { return likes; }
        public String getTimeInfo() { return timeInfo; }
        public List<Comment> getComments() { return comments; }
    }

    public static class Comment {
        private int id;
        private int postId;
        private int userId;
        private String username;
        private String content;
        private String timeInfo;

        public Comment(int id, int postId, int userId, String username, String content, String timeInfo) {
            this.id = id;
            this.postId = postId;
            this.userId = userId;
            this.username = username;
            this.content = content;
            this.timeInfo = timeInfo;
        }

        public int getId() { return id; }
        public int getPostId() { return postId; }
        public String getUsername() { return username; }
        public String getContent() { return content; }
        public String getTimeInfo() { return timeInfo; }
    }

    public static List<Post> getAllPosts() {
        List<Post> posts = new ArrayList<>();
        String sql = "SELECT p.id, p.user_id, p.username, p.content, p.category, " +
                "(SELECT COUNT(*) FROM post_likes pl WHERE pl.post_id = p.id) as likes, " +
                "p.created_at " +
                "FROM community_posts p ORDER BY p.created_at DESC";

        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {

            while (rs.next()) {
                int postId = rs.getInt("id");
                String timeInfo = formatTimeInfo(rs.getTimestamp("created_at"));
                List<Comment> comments = getCommentsForPost(postId);
                Post post = new Post(
                        postId,
                        rs.getInt("user_id"),
                        rs.getString("username"),
                        rs.getString("content"),
                        rs.getString("category"),
                        rs.getInt("likes"),
                        timeInfo,
                        comments
                );
                posts.add(post);
            }
        } catch (SQLException e) {
            System.err.println("Error fetching posts: " + e.getMessage());
            e.printStackTrace();
        }
        return posts;
    }

    public static void addPost(int userId, String username, String content, String category) {
        String sql = "INSERT INTO community_posts (user_id, username, content, category) VALUES (?, ?, ?, ?)";

        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, userId);
            pstmt.setString(2, username);
            pstmt.setString(3, content);
            pstmt.setString(4, category);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Error adding post for user_id " + userId + ": " + e.getMessage());
            e.printStackTrace();
        }
    }

    public static boolean hasUserLikedPost(int userId, int postId) {
        String sql = "SELECT 1 FROM post_likes WHERE user_id = ? AND post_id = ?";

        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, userId);
            pstmt.setInt(2, postId);
            try (ResultSet rs = pstmt.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            System.err.println("Error checking like for user_id " + userId + ", post_id " + postId + ": " + e.getMessage());
            return false;
        }
    }

    public static boolean addLike(int userId, int postId) {
        if (hasUserLikedPost(userId, postId)) {
            return false; // User already liked
        }

        String sql = "INSERT INTO post_likes (user_id, post_id) VALUES (?, ?)";

        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, userId);
            pstmt.setInt(2, postId);
            pstmt.executeUpdate();
            return true;
        } catch (SQLException e) {
            System.err.println("Error adding like for user_id " + userId + ", post_id " + postId + ": " + e.getMessage());
            return false;
        }
    }

    public static void addComment(int postId, int userId, String username, String content) {
        String sql = "INSERT INTO comments (post_id, user_id, username, content) VALUES (?, ?, ?, ?)";

        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, postId);
            pstmt.setInt(2, userId);
            pstmt.setString(3, username);
            pstmt.setString(4, content);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Error adding comment for post_id " + postId + ": " + e.getMessage());
            e.printStackTrace();
        }
    }

    public static List<Comment> getCommentsForPost(int postId) {
        List<Comment> comments = new ArrayList<>();
        String sql = "SELECT id, post_id, user_id, username, content, created_at " +
                "FROM comments WHERE post_id = ? ORDER BY created_at ASC";

        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, postId);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    String timeInfo = formatTimeInfo(rs.getTimestamp("created_at"));
                    Comment comment = new Comment(
                            rs.getInt("id"),
                            rs.getInt("post_id"),
                            rs.getInt("user_id"),
                            rs.getString("username"),
                            rs.getString("content"),
                            timeInfo
                    );
                    comments.add(comment);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error fetching comments for post_id " + postId + ": " + e.getMessage());
        }
        return comments;
    }

    private static String formatTimeInfo(Timestamp createdAt) {
        if (createdAt == null) return "Unknown time";
        long diff = System.currentTimeMillis() - createdAt.getTime();
        long hours = diff / (1000 * 60 * 60);
        if (hours < 24) {
            return hours + "h ago";
        } else {
            return createdAt.toLocalDateTime().format(DateTimeFormatter.ofPattern("dd MMM yyyy"));
        }
    }
}