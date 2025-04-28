import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class StudentManagementGUI {
    private final DataLayer dataLayer;

    public StudentManagementGUI(DataLayer dataLayer) {
        this.dataLayer = dataLayer;
        createAndShowGUI();
    }

    private void createAndShowGUI() {
        JFrame frame = new JFrame("Student Management System");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(600, 400);

        JPanel panel = new JPanel();
        panel.setLayout(new GridLayout(5, 1, 10, 10));

        JButton loginButton = new JButton("Login");
        JButton viewCoursesButton = new JButton("View Courses");
        JButton addDropCourseButton = new JButton("Add/Drop Course");
        JButton viewGPAButton = new JButton("View GPA");
        JButton exitButton = new JButton("Exit");

        panel.add(loginButton);
        panel.add(viewCoursesButton);
        panel.add(addDropCourseButton);
        panel.add(viewGPAButton);
        panel.add(exitButton);

        frame.getContentPane().add(panel);
        frame.setVisible(true);

        loginButton.addActionListener(e -> showLoginDialog());
        viewCoursesButton.addActionListener(e -> showAllCourses());
        addDropCourseButton.addActionListener(e -> showAddDropDialog());
        viewGPAButton.addActionListener(e -> showGpaDialog());
        exitButton.addActionListener(e -> System.exit(0));
    }

    private void showLoginDialog() {
        JTextField nameField = new JTextField();
        JPasswordField passField = new JPasswordField();

        Object[] fields = {
            "Name:", nameField,
            "Password:", passField
        };

        int option = JOptionPane.showConfirmDialog(null, fields, "Login", JOptionPane.OK_CANCEL_OPTION);
        if (option == JOptionPane.OK_OPTION) {
            String name = nameField.getText();
            String password = new String(passField.getPassword());
            LoginResult result = dataLayer.login(name, password);

            JOptionPane.showMessageDialog(null, "Login Result: " + result.type);
        }
    }

    private void showAllCourses() {
        java.util.List<Course> courses = dataLayer.allCourses();
        StringBuilder builder = new StringBuilder("Courses:\n");
        for (Course c : courses) {
            builder.append(c.courseName).append(" taught by ").append(c.professor.professorName).append("\n");
        }
        JOptionPane.showMessageDialog(null, builder.toString());
    }

    private void showAddDropDialog() {
        JTextField studentIdField = new JTextField();
        JTextField courseIdField = new JTextField();

        Object[] fields = {
            "Student ID:", studentIdField,
            "Course ID:", courseIdField
        };

        String[] options = {"Add Course", "Drop Course", "Cancel"};
        int option = JOptionPane.showOptionDialog(null, fields, "Add/Drop Course", JOptionPane.DEFAULT_OPTION,
                JOptionPane.INFORMATION_MESSAGE, null, options, options[0]);

        int studentId = Integer.parseInt(studentIdField.getText());
        int courseId = Integer.parseInt(courseIdField.getText());

        if (option == 0) {
            boolean success = dataLayer.addCourse(studentId, courseId);
            JOptionPane.showMessageDialog(null, success ? "Course Added" : "Failed to Add Course");
        } else if (option == 1) {
            boolean success = dataLayer.dropCourse(studentId, courseId);
            JOptionPane.showMessageDialog(null, success ? "Course Dropped" : "Failed to Drop Course");
        }
    }

    private void showGpaDialog() {
        String studentIdStr = JOptionPane.showInputDialog("Enter Student ID:");
        int studentId = Integer.parseInt(studentIdStr);
        double gpa = dataLayer.studentGpa(studentId);
        JOptionPane.showMessageDialog(null, "GPA: " + gpa);
    }

    // public static void main(String[] args) {
    //     SwingUtilities.invokeLater(() -> new StudentManagementGUI(new InMemoryDataLayer()));
    // }
}