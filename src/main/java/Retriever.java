import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

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

    private Student retreiveStudent(int id) { // maybe change to optional
        try {
            ResultSet rs = conn.createStatement().executeQuery("SELECT COUNT(*) as count FROM students WHERE studentID = " + id + ";");
            rs.next();
            if (rs.getInt("count") != 1) {
                return null;
            }
            rs = conn.createStatement().executeQuery("SELECT * FROM students WHERE studentID = " + id + ";");
            rs.next();
            return new Student(rs.getInt("studentID"), rs.getString("studentName"), rs.getString("password"));
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private Professor retreiveProfessor(int id) {
        try {
            ResultSet rs = conn.createStatement().executeQuery("SELECT COUNT(*) as count FROM professors WHERE professorID = " + id + ";");
            rs.next();
            if (rs.getInt("count") != 1) {
                return null;
            }
            rs = conn.createStatement().executeQuery("SELECT * FROM professors WHERE professorID = " + id + ";");
            rs.next();
            return new Professor(rs.getInt("professorID"), rs.getString("professorName"), rs.getString("password"));
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

    public int idCount() {
        try {
            int result = 0;
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT COUNT(*) as count FROM professors;");
            rs.next();
            result += rs.getInt("count");
            rs = stmt.executeQuery("SELECT COUNT(*) as count FROM students;");
            rs.next();
            result += rs.getInt("count");
            return result;
        } catch (Exception e) {
            e.printStackTrace();
            return -1;
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

    public LoginResult loginID(int id, String password) {
        Student student = retreiveStudent(id);
        Professor professor = retreiveProfessor(id);
        if (student != null && password.equals(student.password)) {
            return new LoginResult(LoginResultType.STUDENT, Optional.of(student), Optional.empty());
        } else if (professor != null && password.equals(professor.password)) {
            return new LoginResult(LoginResultType.PROFESSOR, Optional.empty(), Optional.of(professor));
        }
        return new LoginResult(LoginResultType.INVALID, Optional.empty(), Optional.empty());
    }

    @Override
    public boolean signUp(String name, String password) {
        return false;
    }


    /**
     * @param name Student's name
     * @param password Student's password
     * @return The student ID if process succeeds, -1 otherwise
     */
    public int signUpID(String name, String password) {
        int id = idCount();
        if(id <= 0) {
            return 0;
        }
        while((retreiveStudent(id) != null || retreiveProfessor(id) != null) && id > 0) {
            id++;
        }
        if(id <= 0) {
            return -1; // Overflow
        }
        try {
            conn.createStatement().execute("INSERT INTO students (studentID, studentName, password) VALUES (" + id + ", '" + name + "', '" + password + "');");
            return id;
        } catch (Exception e) {
            e.printStackTrace();
            return -1;
        }
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
                innerRs.next();
                int credits = innerRs.getInt("credits");
                totalCredits += credits * 100.0;
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
                innerRs.next();
                Pair<Course, Professor> pair = createCourseProfessor(innerRs);
                assert pair != null;
                result.add(new StudentCourseInfo(pair.first, Double.toString(grade), pair.second));
            }
            return result;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * @param studentID Student to add course to
     * @param courseID  ID of desired course
     * @return
     */
    @Override
    public boolean addCourse(int studentID, int courseID) {
        try {
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT COUNT(*) as count FROM enrollments WHERE studentID = " + studentID + " AND courseID = " + courseID + ";");
            rs.next();
            if (rs.getInt("count") > 0) {
                throw new Exception("Add Course - Student is already enrolled");
            }
            rs = stmt.executeQuery("SELECT COUNT(*) as count FROM courses WHERE courseID = " + courseID + ";");
            rs.next();
            if (rs.getInt("count") != 1) {
                throw new Exception("Add Course - Course does not exist");
            }
            stmt.execute("INSERT INTO enrollments (studentID, courseID, grade) VALUES (" + studentID + ", " + courseID + ", " + 0xdeadbeef + ");");
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * @param studentID ID of the student
     * @param courseID  ID of the course
     * @return True if the course exists, False otherwise
     */
    @Override
    public boolean dropCourse(int studentID, int courseID) {
        try {
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT COUNT(*) as count FROM enrollments WHERE studentID = " + studentID + " AND courseID = " + courseID + ";");
            rs.next();
            if (rs.getInt("count") == 0) {
                throw new Exception("Student is not enrolled");
            }
            stmt.execute("DELETE FROM enrollments WHERE studentID = " + studentID + " AND courseID = " + courseID + ";");
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
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
            while (rs.next()) {
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
            ResultSet rs = stmt.executeQuery("SELECT studentID, grade FROM enrollments WHERE courseID = + " + courseID + ";");
            List<CourseStudentInfo> result = new ArrayList<>();
            while (rs.next()) {
                result.add(new CourseStudentInfo(retreiveStudent(rs.getInt("studentID")), Float.toString(rs.getFloat("grade"))));
            }
            return result;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }


    /**
     * @param courseID  ID of course to change
     * @param studentID ID of student whose grade to change
     * @param newGrade  The new grade for that course
     * @return True if the student and course exists and was updated, false otherwise
     */
    @Override
    public boolean changeGrade(int courseID, int studentID, String newGrade) {
        try {
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT COUNT(*) as count FROM students WHERE studentID = " + studentID + ";");
            rs.next();
            if (rs.getInt("count") != 1) {
                throw new Exception("Change Grade - Student does not exist");
            }
            rs = stmt.executeQuery("SELECT COUNT(*) as count FROM enrollments WHERE courseID = " + courseID + ";");
            rs.next();
            if (rs.getInt("count") == 0) {
                throw new Exception("Change Grade - Course does not exist");
            }
            rs = stmt.executeQuery("SELECT COUNT(*) as count FROM enrollments WHERE studentID = " + studentID + " AND courseID = " + courseID + ";");
            rs.next();
            if (rs.getInt("count") == 0) {
                throw new Exception("Change Grade - Student is not enrolled in course");
            }
            return stmt.execute("UPDATE enrollments SET grade = " + Float.parseFloat(newGrade) + " WHERE studentID = " + studentID + " AND courseID = " + courseID + ";"); // False warning from intelliJ
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    static class Pair<T, K> {
        public T first;
        public K second;

        public Pair(T first, K second) {
            this.first = first;
            this.second = second;
        }
    }
}
