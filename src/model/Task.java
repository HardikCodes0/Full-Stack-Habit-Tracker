package model;

public class Task {
    private int id;
    private String title;
    private String note;
    private String time;
    private boolean done;

    // Existing constructor
    public Task(String title, String note, String time) {
        this.title = title;
        this.note = note;
        this.time = time;
        this.done = false;
    }

    // New constructor to match database retrieval
    public Task(int id, String title, String note, String time, boolean done) {
        this.id = id;
        this.title = title;
        this.note = note;
        this.time = time;
        this.done = done;
    }

    // Getters and Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public boolean isDone() {
        return done;
    }

    public void setDone(boolean done) {
        this.done = done;
    }
}