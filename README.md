🧠 Habit Tracker - JavaFX Desktop App

A stylish Habit Tracker desktop application built using JavaFX, FlatLaf, JDBC, and MySQL. This project allows users to track daily habits, store data persistently in a database, and interact with a clean, responsive UI.

🚀 Features

Add, update, and delete daily habits

Track habit progress

Clean and modern UI with FlatLaf

Persistent data storage using MySQL

Basic authentication support (optional if implemented)

🛠 Tech Stack

Tool

Description

JavaFX

GUI framework for building the UI

FlatLaf

A modern Look and Feel for Java apps

JDBC

Java database connectivity layer

MySQL

Relational database to store data

IntelliJ

IDE used for development

🏗️ Project Structure

HabitTracker/
- src/
  - loginpage/
  - dao/
  - util/
  - habbittracker/
- resources/
- lib/
- .gitignore
- README.md
- habittrackerfinal.iml

⚙️ Setup Instructions

1. Clone the Repository

git clone https://github.com/yourusername/habit-tracker-javafx.git
cd habit-tracker-javafx

2. Configure the Database

Create a MySQL database and run your table creation scripts.

Update your database connection details in DatabaseUtil.java (or wherever you manage DB connection):

String url = "jdbc:mysql://localhost:3306/habittracker";
String user = "your_username";
String password = "your_password";

3. Run the App

Open the project in IntelliJ IDEA

Make sure JavaFX SDK is properly configured in your project SDK settings

Run the Main.java file (or your main app launcher class)

📦 Dependencies

Java 11 or above

JavaFX SDK

FlatLaf

MySQL Connector/J (JDBC Driver)

✨ Credits

Developed by [Your Name]Designed and built with ❤️ using JavaFX

📄 License

This project is licensed under the MIT License.

