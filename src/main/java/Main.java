import java.sql.*;
import java.util.ArrayList;
import java.util.EmptyStackException;
import java.util.Random;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class Main {
    public static void main(String[] args) {
        String url = "jdbc:mysql://localhost:3306/mydb";
        String user = "root";
        String password = "root";
        boolean runGui = false;
        if (runGui) {
            InMemoryDataLayer layer = new InMemoryDataLayer();
            layer.populateDummyData();
            SwingUtilities.invokeLater(() -> {
                try {
                    new StudentManagementGUI(new Retriever(DriverManager.getConnection(url, user, password)));
                } catch (SQLException e) {
                    throw new RuntimeException(e);
                }
            });
            return;
        }


        boolean regen = true; // Delete and regen DB
        if (regen) {
            try (Connection conn = DriverManager.getConnection(url, user, password)) { // w/o PS 2m 15s
                System.out.println("Connected to database!");

                Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery("SELECT NOW();");

                while (rs.next()) {
                    System.out.println("Current Time: " + rs.getString(1));
                }
                stmt.execute("DROP TABLE IF EXISTS Students;");
                stmt.execute("CREATE TABLE Students (studentID INT, studentName VARCHAR(100), password VARCHAR(64));");
                stmt.execute("DROP TABLE IF EXISTS Professors;");
                stmt.execute("CREATE TABLE Professors (professorID INT, professorName VARCHAR(100), password VARCHAR(64))");
                stmt.execute("DROP TABLE IF EXISTS Courses;");
                stmt.execute("CREATE TABLE Courses (courseID INT, courseName VARCHAR(100), credits INT, semester VARCHAR(5), professorID INT)");
                stmt.execute("DROP TABLE IF EXISTS Enrollments;");
                stmt.execute("CREATE TABLE Enrollments (studentID INT, courseID INT, grade FLOAT)");

                Generator.addData(conn);
                rs = stmt.executeQuery("SELECT NOW();");

                while (rs.next()) {
                    System.out.println("Current Time: " + rs.getString(1));
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
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
        static int enrollmentCount = studentCount * 4 * 2;
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
                professors.add(new Professor(studentCount + i,
                        firstNames[random.nextInt(firstNames.length)] + " " + lastNames[random.nextInt(lastNames.length)], passwords[random.nextInt(passwords.length)]));
            }
            for (int i = 1; i <= courseCount; i++) {
                courses.add(new Course(i,
                        depts[random.nextInt(depts.length)] + "-" + (random.nextInt(maxCourseNum + minCourseNum)),
                        random.nextInt(5),
                        sessions[random.nextInt(sessions.length)] + (random.nextInt(endYear - startYear) + startYear),
                        professors.get(random.nextInt(professorCount))));
            }
            for (int i = 1; i <= enrollmentCount; i++) {
                enrollments.add(new Enrollment(students.get(random.nextInt(students.size())),
                        courses.get(random.nextInt(courses.size())),
                        random.nextDouble() * 100.0));
            }
            System.out.println("Done Generating");
            Statement statement = conn.createStatement();
            PreparedStatement studentPS = conn.prepareStatement("INSERT INTO Students (studentID, studentName, password) VALUES( ?, ?, ?);");
            for (Student s : students) {
                studentPS.setInt(1, s.id);
                studentPS.setString(2, s.name);
                studentPS.setString(3, s.password);
                studentPS.execute();
//                statement.execute("INSERT INTO Students (studentID, studentName, password) VALUES(" + s.id + ", '" + s.name + "', '" + s.password + "');");
            }
            PreparedStatement professorPS = conn.prepareStatement("INSERT INTO Professors (professorID, professorName, password) VALUES(?, ?, ?);");
            for (Professor p : professors) {
                professorPS.setInt(1, p.id);
                professorPS.setString(2, p.name);
                professorPS.setString(3, p.password);
                professorPS.execute();
//                statement.execute("INSERT INTO Professors (professorID, professorName, password) VALUES(" + p.id + ", '" + p.name + "', '" + p.password + "');");
            }
            PreparedStatement coursePS = conn.prepareStatement("INSERT INTO Courses (courseID, courseName, credits, professorID, semester) VALUES(?, ?, ?, ?, ?);");
            for (Course c : courses) {
                coursePS.setInt(1, c.id);
                coursePS.setString(2, c.name);
                coursePS.setInt(3, c.credits);
                coursePS.setInt(4, c.professor.id);
                coursePS.setString(5, c.semester);
                coursePS.execute();
//                statement.execute("INSERT INTO Courses (courseID, courseName, credits, professorID, semester) VALUES(" + c.id + ", '" + c.name + "', " + c.credits + ", " + c.professor.id + ", '" + c.semester + "');");
            }
            PreparedStatement enrollmentPS = conn.prepareStatement("INSERT INTO Enrollments (studentID, courseID, grade) VALUES(?, ?, ?);");
            for (Enrollment e : enrollments) {
                enrollmentPS.setInt(1, e.student.id);
                enrollmentPS.setInt(2, e.course.id);
                enrollmentPS.setString(3, String.valueOf(e.grade));
                enrollmentPS.execute();
//                statement.execute("INSERT INTO Enrollments (studentID, courseID, grade) VALUES(" + e.student.id + ", " + e.course.id + ", " + e.grade + ");");
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
            String password;

            public Professor(int id, String name, String password) {
                this.id = id;
                this.name = name;
                this.password = password;
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


