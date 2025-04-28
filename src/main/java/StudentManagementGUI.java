import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.List;
import java.util.Optional;

public class StudentManagementGUI {

    private JFrame frame;
    private DataLayer dataLayer;

    private int currentStudentID = -1;
    private int currentProfessorID = -1;

    public StudentManagementGUI(DataLayer dataLayer) {
        this.dataLayer = dataLayer;
        initialize();
    }

    private void initialize() {
        frame = new JFrame("University Portal");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(600, 400);

        showLoginScreen();

        frame.setVisible(true);
    }

    private void showLoginScreen() {
        frame.getContentPane().removeAll();

        JPanel panel = new JPanel(new GridLayout(5, 2));

        JTextField nameField = new JTextField();
        JPasswordField passField = new JPasswordField();

        JButton loginBtn = new JButton("Login");
        JButton signupBtn = new JButton("Sign Up");

        panel.add(new JLabel("Name:"));
        panel.add(nameField);
        panel.add(new JLabel("Password:"));
        panel.add(passField);

        panel.add(loginBtn);
        panel.add(signupBtn);

        frame.getContentPane().add(panel);
        frame.revalidate();

        loginBtn.addActionListener(e -> {
            String name = nameField.getText();
            String password = new String(passField.getPassword());
            LoginResult result = dataLayer.login(name, password);
            if (result.type == LoginResultType.STUDENT && result.student.isPresent()) {
                currentStudentID = result.student.get().studentID;
                showStudentDashboard();
            } else if (result.type == LoginResultType.PROFESSOR && result.professor.isPresent()) {
                currentProfessorID = result.professor.get().professorID;
                showProfessorDashboard();
            } else {
                JOptionPane.showMessageDialog(frame, "Login Failed");
            }
        });

        signupBtn.addActionListener(e -> {
            String name = nameField.getText();
            String password = new String(passField.getPassword());
            boolean success = dataLayer.signUp(name, password);
            if (success) {
                JOptionPane.showMessageDialog(frame, "Signup Successful!");
            } else {
                JOptionPane.showMessageDialog(frame, "Signup Failed");
            }
        });
    }

    private void showStudentDashboard() {
        frame.getContentPane().removeAll();
        frame.setLayout(new BorderLayout());

        JPanel panel = new JPanel(new GridLayout(6, 1));

        JButton viewGradesBtn = new JButton("View My Grades and GPA");
        JButton enrollBtn = new JButton("Enroll in a Course");
        JButton dropBtn = new JButton("Drop a Course");
        JButton viewAllCoursesBtn = new JButton("View All Courses");
        JButton logoutBtn = new JButton("Logout");

        panel.add(viewGradesBtn);
        panel.add(enrollBtn);
        panel.add(dropBtn);
        panel.add(viewAllCoursesBtn);
        panel.add(logoutBtn);

        frame.getContentPane().add(panel, BorderLayout.CENTER);
        frame.revalidate();

        viewGradesBtn.addActionListener(e -> viewGrades());
        enrollBtn.addActionListener(e -> enrollInCourse());
        dropBtn.addActionListener(e -> dropCourse());
        viewAllCoursesBtn.addActionListener(e -> viewAllCourses());
        logoutBtn.addActionListener(e -> {
            currentStudentID = -1;
            showLoginScreen();
        });
    }

    private void showProfessorDashboard() {
        frame.getContentPane().removeAll();
        frame.setLayout(new BorderLayout());

        JPanel panel = new JPanel(new GridLayout(4, 1));

        JButton viewTeachingBtn = new JButton("View My Courses");
        JButton changeGradeBtn = new JButton("Change Student Grade");
        JButton logoutBtn = new JButton("Logout");

        panel.add(viewTeachingBtn);
        panel.add(changeGradeBtn);
        panel.add(logoutBtn);

        frame.getContentPane().add(panel, BorderLayout.CENTER);
        frame.revalidate();

        viewTeachingBtn.addActionListener(e -> viewProfessorCourses());
        changeGradeBtn.addActionListener(e -> changeStudentGradeImproved());
        logoutBtn.addActionListener(e -> {
            currentProfessorID = -1;
            showLoginScreen();
        });
    }

    private void viewGrades() {
        List<StudentCourseInfo> courses = dataLayer.studentCourses(currentStudentID);
        double gpa = dataLayer.studentGpa(currentStudentID);

        StringBuilder sb = new StringBuilder();
        for (StudentCourseInfo info : courses) {
            sb.append(info.course.courseName).append(" - Grade: ").append(info.grade).append("\n");
        }
        sb.append("\nGPA: ").append(gpa);

        JOptionPane.showMessageDialog(frame, sb.toString());
    }

    private void enrollInCourse() {
        List<Course> all = dataLayer.allCourses();
        List<StudentCourseInfo> enrolledCourses = dataLayer.studentCourses(currentStudentID);
        String[] courseOptions = all.stream()
                .map(c -> c.courseID + ": " + c.courseName)
                .toArray(String[]::new);

        String choice = (String) JOptionPane.showInputDialog(frame, "Select course to enroll:",
                "Enroll", JOptionPane.PLAIN_MESSAGE, null, courseOptions, courseOptions[0]);

        if (choice != null) {
            int courseId = Integer.parseInt(choice.split(":")[0]);
            boolean alreadyEnrolled = enrolledCourses.stream().anyMatch(ec -> ec.course.courseID == courseId);
            if (alreadyEnrolled) {
                JOptionPane.showMessageDialog(frame, "You are already enrolled in this course.");
                return;
            }
            if (dataLayer.addCourse(currentStudentID, courseId)) {
                JOptionPane.showMessageDialog(frame, "Enrolled successfully!");
            } else {
                JOptionPane.showMessageDialog(frame, "Enrollment failed.");
            }
        }
    }

    private void dropCourse() {
        List<StudentCourseInfo> courses = dataLayer.studentCourses(currentStudentID);
        String[] courseOptions = courses.stream()
                .map(c -> c.course.courseID + ": " + c.course.courseName)
                .toArray(String[]::new);

        String choice = (String) JOptionPane.showInputDialog(frame, "Select course to drop:",
                "Drop Course", JOptionPane.PLAIN_MESSAGE, null, courseOptions, courseOptions[0]);

        if (choice != null) {
            int courseId = Integer.parseInt(choice.split(":")[0]);
            if (dataLayer.dropCourse(currentStudentID, courseId)) {
                JOptionPane.showMessageDialog(frame, "Dropped successfully!");
            } else {
                JOptionPane.showMessageDialog(frame, "Drop failed.");
            }
        }
    }

    private void viewAllCourses() {
        List<Course> all = dataLayer.allCourses();
        StringBuilder sb = new StringBuilder();
        for (Course c : all) {
            sb.append(c.courseID).append(" - ").append(c.courseName).append("\n");
        }
        JOptionPane.showMessageDialog(frame, sb.toString());
    }

    private void viewProfessorCourses() {
        List<Course> courses = dataLayer.professorCourses(currentProfessorID);

        StringBuilder sb = new StringBuilder();
        for (Course c : courses) {
            sb.append(c.courseID).append(" - ").append(c.courseName).append("\n");
        }
        JOptionPane.showMessageDialog(frame, sb.toString());
    }

    private void changeStudentGradeImproved() {
        List<Course> courses = dataLayer.professorCourses(currentProfessorID);
        if (courses.isEmpty()) {
            JOptionPane.showMessageDialog(frame, "You are not teaching any courses.");
            return;
        }

        String[] courseOptions = courses.stream()
                .map(c -> c.courseID + ": " + c.courseName)
                .toArray(String[]::new);

        String selectedCourse = (String) JOptionPane.showInputDialog(frame, "Select a course:",
                "Select Course", JOptionPane.PLAIN_MESSAGE, null, courseOptions, courseOptions[0]);

        if (selectedCourse != null) {
            int courseID = Integer.parseInt(selectedCourse.split(":")[0]);
            List<CourseStudentInfo> students = dataLayer.courseStudents(courseID);

            if (students.isEmpty()) {
                JOptionPane.showMessageDialog(frame, "No students enrolled in this course.");
                return;
            }

            String[] studentOptions = students.stream()
                    .map(s -> s.student.studentID + ": " + s.student.studentName)
                    .toArray(String[]::new);

            String selectedStudent = (String) JOptionPane.showInputDialog(frame, "Select a student:",
                    "Select Student", JOptionPane.PLAIN_MESSAGE, null, studentOptions, studentOptions[0]);

            if (selectedStudent != null) {
                int studentID = Integer.parseInt(selectedStudent.split(":")[0]);
                String newGrade = JOptionPane.showInputDialog(frame, "Enter new grade:");

                if (newGrade != null && !newGrade.trim().isEmpty()) {
                    if (dataLayer.changeGrade(courseID, studentID, newGrade)) {
                        JOptionPane.showMessageDialog(frame, "Grade changed successfully!");
                    } else {
                        JOptionPane.showMessageDialog(frame, "Failed to change grade.");
                    }
                } else {
                    JOptionPane.showMessageDialog(frame, "Invalid grade input.");
                }
            }
        }
    }

    public static void main(String[] args) {
        DataLayer dataLayer = null; // Replace with your actual DataLayer implementation
        new StudentManagementGUI(dataLayer);
    }
}
