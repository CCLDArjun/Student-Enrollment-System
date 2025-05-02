import java.sql.*;
import java.util.ArrayList;
import java.util.EmptyStackException;
import java.util.Random;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class Main {
    public static void main(String[] args) {
        boolean runGui = true;
        if (runGui) {
            InMemoryDataLayer layer = new InMemoryDataLayer();
            layer.populateDummyData();
            SwingUtilities.invokeLater(() -> new StudentManagementGUI(layer));
            return;
        }

        String url = "jdbc:mysql://localhost:3306/mydb";
        String user = "root";
        String password = "root";

        try (Connection conn = DriverManager.getConnection(url, user, password)) {
            System.out.println("Connected to database!");

            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT NOW();");

            while (rs.next()) {
                System.out.println("Current Time: " + rs.getString(1));
            }
            stmt.execute("CREATE TABLE Students (studentID INT, studentName VARCHAR(100), password VARCHAR(64));");
            stmt.execute("CREATE TABLE Professors (professorID INT, professorName VARCHAR(100))");
            stmt.execute("CREATE TABLE Courses (courseID INT, courseName VARCHAR(100), credits INT, semester VARCHAR(5), professorID INT)");
            stmt.execute("CREATE TABLE Enrollments (studentID INT, courseID INT, grade FLOAT)"); // shouldnt be varchar

            Generator.addData(conn);
            rs = stmt.executeQuery("SELECT NOW();");

            while (rs.next()) {
                System.out.println("Current Time: " + rs.getString(1));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }


    static class Generator {
        static int studentCount = 16384;
        static int professorCount = 1024;
        static String[] firstNames = new String[]{"David", "Noah", "Liam", "Jacob", "William", "Mason", "Ethan", "Michael", "Alexander", "James", "Elijah", "Benjamin", "Daniel", "Aiden", "Logan", "Jayden", "Emma", "Olivia", "Sophia", "Isabella", "Ava", "Mia", "Abigail", "Emily", "Charlotte", "Madison", "Elizabeth", "Amelia", "Evelyn", "Ella", "Chloe", "Angelo", "Muhammad", "Amirezza", "Arian", "Kelly", "Arnav", "Joshua", "Mick", "Tiana", "Harun", "Pranav", "Alan", "Monica"};
        static String[] lastNames = new String[]{"Tsao", "Zhou", "Tran", "Nguyen", "Singh", "Park", "Lin", "Le", "Howard", "Barman", "Evans", "Shakrovsky", "Taylor", "Rao", "Yrigoven", "Bacon", "Smith", "Lopez", "Williams", "Perez", "Harris", "Scott", "Hall", "Cruz", "Cook", "Reed", "Watson", "Mendoza", "Patel", "Sanders", "Walter", "Bach", "Mozart", "Einstein", "Beethoven", "Mahler", "Elbertson", "Kimmel", "Rovenpera", "Kasparov", "Truong", "Gould", "Holst", "Hirose", "Bonds"};
        static String[] depts = new String[]{"CS", "ENGL", "MATH", "KIN", "PHYS", "BIOL", "CHEM", "GEOL", "ART", "EE", "CMPE", "HIST", "MUSC"};
        static String[] passwords = new String[]{"123456", "123456789", "12345678", "password", "qwerty123", "0xdeadbeef", "ssladded", "removedhere", "admin", "root", "dogname", "catname", "racecar", "abc123", "000000", "hunter2", "*******", "lanciastratos", "audiquattrogrb", "leetcode"};
        static int minCourseNum = 0;
        static int maxCourseNum = 300;
        static int courseCount = 1000;
        static int startYear = 10;
        static int endYear = 25;
        static int enrollmentCount = courseCount * 4 * 2;
        static String[] sessions = new String[]{"SP", "SM", "FA", "WN"};

        public static void addData(Connection conn) throws SQLException {
            ArrayList<Student> students = new ArrayList<>();
            ArrayList<Professor> professors = new ArrayList<>();
            ArrayList<Course> courses = new ArrayList<>();
            ArrayList<Enrollment> enrollments = new ArrayList<>();
            Random random = new Random(157);

            long prime = (int) (1e9 + 7);
            for (int i = 1; i <= studentCount; i++) {
                students.add(new Student(i,
                        firstNames[random.nextInt(firstNames.length)] + " " + lastNames[random.nextInt(lastNames.length)],
                        passwords[(int) ((i * prime) % passwords.length)]));
            }
            for (int i = 1; i <= professorCount; i++) {
                professors.add(new Professor(i,
                        firstNames[random.nextInt(firstNames.length)] + " " + lastNames[random.nextInt(lastNames.length)]));
            }
            for (int i = 1; i <= courseCount; i++) {
                courses.add(new Course(i,
                        depts[random.nextInt(depts.length)] + "-" + (random.nextInt(maxCourseNum + minCourseNum)),
                        random.nextInt(5),
                        sessions[random.nextInt(sessions.length)] + (random.nextInt((endYear - startYear) + startYear)),
                        professors.get(random.nextInt(professorCount))));
            }
            for (int i = 1; i <= enrollmentCount; i++) {
                enrollments.add(new Enrollment(students.get(random.nextInt(students.size())),
                        courses.get(random.nextInt(courses.size())),
                        random.nextDouble() * 100.0));
            }
            System.out.println("Done Generating");
            Statement statement = conn.createStatement();
            for (Student s : students) {
                statement.execute("INSERT INTO Students (studentID, studentName, password) VALUES(" + s.id + ", '" + s.name + "', '" + s.password + "');");
            }
            for (Professor p : professors) {
                statement.execute("INSERT INTO Professors (professorID, professorName) VALUES(" + p.id + ", '" + p.name + "');");
            }
            for (Course c : courses) {
                statement.execute("INSERT INTO Courses (courseID, courseName, credits, professorID) VALUES(" + c.id + ", '" + c.name + "', " + c.credits + ", " + c.professor.id + ");");
            }
            for (Enrollment e : enrollments) {
                statement.execute("INSERT INTO Enrollments (studentID, courseID, grade) VALUES(" + e.student.id + ", " + e.course.id + ", " + e.grade + ");");
            }
            System.out.println("Done Adding");
        }

        static class Student {
            int id;
            String name;
            String password;

            public Student(int id, String name, String password) {
                this.id = id;
                this.name = name;
                this.password = password;
            }
        }

        static class Professor {
            int id;
            String name;

            public Professor(int id, String name) {
                this.id = id;
                this.name = name;
            }
        }

        static class Course {
            int id;
            String name;
            int credits;
            String semester;
            Professor professor;

            public Course(int id, String name, int credits, String semester, Professor professor) {
                this.id = id;
                this.name = name;
                this.credits = credits;
                this.semester = semester;
                this.professor = professor;
            }
        }

        static class Enrollment {
            Student student;
            Course course;
            double grade;

            public Enrollment(Student student, Course course, double grade) {
                this.student = student;
                this.course = course;
                this.grade = grade;
            }

        }
    }

}


