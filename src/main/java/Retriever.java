import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class Retriever implements DataLayer {

    Connection conn;

    public Retriever(Connection connection) {
        this.conn = connection;
    }

    /**
     * Expects a 'SELECT * FROM Courses ___' type query
     */
    private Course createCourse(ResultSet rs) {
        try {
            Statement innerStmt = conn.createStatement();
            ResultSet innerRs = innerStmt.executeQuery("SELECT * FROM professors WHERE professorID = " + rs.getString("professorID") + ";");
            innerRs.next();
            Professor temp = new Professor(innerRs.getInt("professorID"), innerRs.getString("professorName"), innerRs.getString("password"));
            return new Course(rs.getInt("courseID"), rs.getString("courseName"), rs.getInt("credits"), rs.getString("semester"), temp);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private Pair<Course, Professor> createCourseProfessor(ResultSet rs) {
        try {
            Statement innerStmt = conn.createStatement();
            ResultSet innerRs = innerStmt.executeQuery("SELECT * FROM professors WHERE professorID = " + rs.getString("professorID") + ";");
            innerRs.next();
            Professor temp = new Professor(innerRs.getInt("professorID"), innerRs.getString("professorName"), innerRs.getString("password"));
            return new Pair<>(new Course(rs.getInt("courseID"), rs.getString("courseName"), rs.getInt("credits"), rs.getString("semester"), temp), temp);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public List<Course> allCourses() {
        try {
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT * FROM Courses;");
            ArrayList<Course> result = new ArrayList<>();
            while (rs.next()) {
                int professorId = rs.getInt("professorID");
                Statement innerStmt = conn.createStatement();
                ResultSet innerRs = innerStmt.executeQuery("SELECT * FROM professors WHERE professorID = " + professorId + ";");
                innerRs.next();
                Professor temp = new Professor(innerRs.getInt("professorID"), innerRs.getString("professorName"), innerRs.getString("password"));
                result.add(new Course(rs.getInt("courseID"), rs.getString("courseName"), rs.getInt("credits"), rs.getString("semester"), temp));
            }
            return result;
        } catch (Exception e) {
            e.printStackTrace();
            return new ArrayList<>();
        }
    }

    @Override
    public LoginResult login(String name, String password) {
        return null;
    }

    @Override
    public boolean signUp(String name, String password) {
        return false;
    }

    @Override
    public double studentGpa(int studentID) {
        try {
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT grade, courseID FROM enrollments WHERE studentID = " + studentID + ";");
            double totalCredits = 0;
            double weightedCredits = 0;
            while (rs.next()) {
                double grade = rs.getFloat("grade");
                int courseID = rs.getInt("courseID");
                Statement innerStmt = conn.createStatement();
                ResultSet innerRs = innerStmt.executeQuery("SELECT credits FROM courses WHERE courseID = " + courseID + ";");
                int credits = innerRs.getInt("credits");
                totalCredits += credits;
                weightedCredits += credits * grade;
            }
            return (weightedCredits / totalCredits) * 4.0; // 4.0 grade scale?
        } catch (Exception e) {
            e.printStackTrace();
            return -1.0;
        }
    }

    //Course course;
    //    String grade;
    //    Professor professor;
    @Override
    public List<StudentCourseInfo> studentCourses(int studentID) {
        try {
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT courseID, grade FROM enrollments WHERE studentID = " + studentID + ";");
            ArrayList<StudentCourseInfo> result = new ArrayList<>();
            while (rs.next()) {
                int courseID = rs.getInt("courseID");
                double grade = rs.getFloat("grade");
                Statement innerStmt = conn.createStatement();
                ResultSet innerRs = innerStmt.executeQuery("SELECT * FROM courses WHERE courseID = " + courseID + ";");
                Pair<Course, Professor> pair = createCourseProfessor(innerRs);
                assert pair != null;
                result.add(new StudentCourseInfo(pair.first, Double.toString(grade), pair.second));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public boolean addCourse(int studentID, int courseID) {
        try {
            Statement stmt = conn.createStatement();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * @param studentID ID of the student
     * @param courseID ID of the course
     * @return True if the course exists, False otherwise
     */
    @Override
    public boolean dropCourse(int studentID, int courseID) {
        try {
            Statement stmt = conn.createStatement();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * @param professorID The professor's ID
     * @return A list of courses that the professor is teaching
     */
    @Override
    public List<Course> professorCourses(int professorID) {
        try {
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT * FROM courses WHERE professorID = " + professorID + ";");
            ArrayList<Course> result = new ArrayList<>();
            while(rs.next()) {
                result.add(createCourse(rs));
            }
            return result;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * @param courseID The course ID
     * @return A list of all students enrolled into the course
     */
    @Override
    //SELECT s.studentID, s.studentName, s.password, e.grade, courseID
    //FROM students s, enrollments e
    //WHERE s.studentID IN (SELECT studentID from enrollments WHERE courseID = 1) AND e.courseID = 1;
    public List<CourseStudentInfo> courseStudents(int courseID) { // student, grade
        try {
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT studentID, studentName, password, grade FROM students, enrollments WHERE courseID = + " + courseID + ";");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public boolean changeGrade(int courseID, int studentID, String newGrade) {
        try {
            Statement stmt = conn.createStatement();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    class Pair<T, K> {
        public T first;
        public K second;

        public Pair(T first, K second) {
            this.first = first;
            this.second = second;
        }
    }
}
