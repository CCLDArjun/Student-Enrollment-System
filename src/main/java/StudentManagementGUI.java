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

        showHomeScreen();

        frame.setVisible(true);
    }


    private void showHomeScreen() {
        frame.getContentPane().removeAll();
        frame.setLayout(new BorderLayout());

        JPanel panel = new JPanel(new GridLayout(3, 1));

        JButton loginBtn = new JButton("Login");
        JButton signupBtn = new JButton("Sign Up");
        JButton exitBtn = new JButton("Exit");

        panel.add(loginBtn);
        panel.add(signupBtn);
        panel.add(exitBtn);

        frame.getContentPane().add(panel, BorderLayout.CENTER);
        frame.revalidate();

        loginBtn.addActionListener(e -> loginScreen());
        signupBtn.addActionListener(e -> signupScreen());
        exitBtn.addActionListener(e -> System.exit(0));
    }

    private void loginScreen() {
        frame.getContentPane().removeAll();
        frame.setLayout(new BorderLayout());

        JPanel panel = new JPanel(new GridLayout(5, 2));

        JTextField idField = new JTextField();
        JPasswordField passField = new JPasswordField();
        JComboBox<String> roleComboBox = new JComboBox<>(new String[]{"Student", "Professor"});

        JButton loginBtn = new JButton("Login");
        JButton exit = new JButton("Home");

        panel.add(new JLabel("ID:"));
        panel.add(idField);
        panel.add(new JLabel("Password:"));
        panel.add(passField);

        panel.add(loginBtn);
        panel.add(exit);

        frame.getContentPane().add(panel, BorderLayout.CENTER);
        frame.revalidate();

        loginBtn.addActionListener(e -> {
            String id_str = idField.getText();
            int id = id_str.isEmpty() ? -1 : Integer.parseInt(id_str);
            if (id == -1) {
                JOptionPane.showMessageDialog(frame, "Invalid ID");
                return;
            }

            String password = new String(passField.getPassword());
            LoginResult result = dataLayer.login(id, password);
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

        exit.addActionListener(e -> showHomeScreen());
    }

    private void signupScreen() {
        frame.getContentPane().removeAll();
        frame.setLayout(new BorderLayout());

        JPanel panel = new JPanel(new GridLayout(5, 2));

        JTextField idField = new JTextField();
        JPasswordField passField = new JPasswordField();
        JComboBox<String> roleComboBox = new JComboBox<>(new String[]{"Student", "Professor"});

        JButton signupBtn = new JButton("Sign Up");
        JButton exit = new JButton("Home");

        panel.add(new JLabel("Name:"));
        panel.add(idField);
        panel.add(new JLabel("Password:"));
        panel.add(passField);
        panel.add(roleComboBox);

        panel.add(signupBtn);
        panel.add(exit);

        frame.getContentPane().add(panel, BorderLayout.CENTER);
        frame.revalidate();

        signupBtn.addActionListener(e -> {
            String name = idField.getText();
            String password = new String(passField.getPassword());
            int id = -1;

            if (roleComboBox.getSelectedItem().equals("Student")) {
                id = dataLayer.signUpStudent(name, password);
            } else if (roleComboBox.getSelectedItem().equals("Professor")) {
                id = dataLayer.signUpProfessor(name, password);
            }

            JOptionPane.showMessageDialog(frame, "Signup Successful! Your ID is: " + id);
        });

        exit.addActionListener(e -> showHomeScreen());
    }


    private void showStudentDashboard() {
        frame.getContentPane().removeAll();
        frame.setLayout(new BorderLayout());

        JPanel panel = new JPanel(new GridLayout(6, 1));

        JButton viewGradesBtn = new JButton("View My Grades and GPA");
        JButton enrollBtn = new JButton("Enroll in a Course");
        JButton dropBtn = new JButton("Drop a Course");
        JButton viewAllCoursesBtn = new JButton("View All Courses");
        JButton filterCoursesBtn = new JButton("Filter Courses (by units)");
        JButton logoutBtn = new JButton("Logout");

        panel.add(viewGradesBtn);
        panel.add(enrollBtn);
        panel.add(dropBtn);
        panel.add(viewAllCoursesBtn);
        panel.add(filterCoursesBtn);
        panel.add(logoutBtn);

        frame.getContentPane().add(panel, BorderLayout.CENTER);
        frame.revalidate();

        viewGradesBtn.addActionListener(e -> viewGrades());
        enrollBtn.addActionListener(e -> enrollInCourse());
        dropBtn.addActionListener(e -> dropCourse());
        viewAllCoursesBtn.addActionListener(e -> viewAllCourses());
        logoutBtn.addActionListener(e -> {
            currentStudentID = -1;
            showHomeScreen();
        });
        filterCoursesBtn.addActionListener(e -> filterCourses());
    }

    private void filterCourses() {
        String input = JOptionPane.showInputDialog(frame, "Enter minimum credits:");
        if (input != null) {
            int minCredits = Integer.parseInt(input);
            List<Course> filteredCourses = dataLayer.filterCourseCredits(minCredits);
            StringBuilder sb = new StringBuilder();
            for (Course c : filteredCourses) {
                sb.append(c.courseID).append(" - ").append(c.courseName).append("\n");
            }
            showScrollPane(sb.toString());
        }
    }

    private void showProfessorDashboard() {
        frame.getContentPane().removeAll();
        frame.setLayout(new BorderLayout());

        JPanel panel = new JPanel(new GridLayout(4, 1));

        JButton viewTeachingBtn = new JButton("View My Courses");
        JButton changeGradeBtn = new JButton("Change Student Grade");
        JButton logoutBtn = new JButton("Logout");
        JButton addCourseBtn = new JButton("Add Course");

        panel.add(viewTeachingBtn);
        panel.add(changeGradeBtn);
        panel.add(logoutBtn);
        panel.add(addCourseBtn);

        frame.getContentPane().add(panel, BorderLayout.CENTER);
        frame.revalidate();

        viewTeachingBtn.addActionListener(e -> viewProfessorCourses());
        changeGradeBtn.addActionListener(e -> changeStudentGradeImproved());
        logoutBtn.addActionListener(e -> {
            currentProfessorID = -1;
            showHomeScreen();
        });
        addCourseBtn.addActionListener(e -> addCourse());
    }

    private void addCourse() {
        String courseName = JOptionPane.showInputDialog(frame, "Enter Course Name:");
        String creditsStr = JOptionPane.showInputDialog(frame, "Enter Credits:");
        String semester = JOptionPane.showInputDialog(frame, "Enter Semester:");

        if (courseName != null && creditsStr != null && semester != null) {
            int credits = Integer.parseInt(creditsStr);
            dataLayer.createCourse(currentProfessorID, courseName, credits, semester);
            JOptionPane.showMessageDialog(frame, "Course added successfully!");
        }
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

    private void showScrollPane(String text) {
        JTextArea textArea = new JTextArea(text);
        textArea.setEditable(false);
        JScrollPane scrollPane = new JScrollPane(textArea);
        scrollPane.setPreferredSize(new Dimension(400, 300));
        JOptionPane.showMessageDialog(frame, scrollPane, "All Courses", JOptionPane.INFORMATION_MESSAGE);
    }

    private void viewAllCourses() {
        List<Course> courses = dataLayer.allCourses();
        StringBuilder sb = new StringBuilder();

        for (Course c : courses) {
            sb.append(c.courseID).append(" - ").append(c.courseName).append("\n");
        }
        
        showScrollPane(sb.toString());
    }

    private void viewProfessorCourses() {
        List<Course> courses = dataLayer.professorCourses(currentProfessorID);
        StringBuilder sb = new StringBuilder();
        for (Course c : courses) {
            sb.append(c.courseID).append(" - ").append(c.courseName).append("\n");
        }

        if (sb.length() == 0) {
            sb.append("No courses found.");
        }
        showScrollPane(sb.toString());
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
