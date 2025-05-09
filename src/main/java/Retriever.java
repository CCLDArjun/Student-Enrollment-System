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

    /**
     * @param id Student's ID
     * @return The student with the corresponding ID, null otherwise
     */
    private Student retreiveStudent(int id) {
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


    /**
     * @param id The professor's ID
     * @return The professor with the corresponding ID, null otherwise
     */
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

    /**
     * @param id The course ID
     * @return The course with the ID, null otherwise
     */
    private Course retreiveCourse(int id) {
        try {
            ResultSet rs = conn.createStatement().executeQuery("SELECT COUNT(*) as count FROM courses WHERE courseID = " + id + ";");
            rs.next();
            if (rs.getInt("count") != 1) {
                return null;
            }
            rs = conn.createStatement().executeQuery("SELECT * FROM courses WHERE courseID = " + id + ";");
            rs.next();
            return new Course(rs.getInt("courseID"), rs.getString("courseName"), rs.getInt("credits"), rs.getString("semester"), retreiveProfessor(rs.getInt("professorID")));
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }


    /**
     * @return The next available ID for a student or professor, returns -1 if none available in sequence
     */
    private int nextPersonID() {
        int id = idCount();
        if(id <= 0) {
            return -1;
        }
        while((retreiveStudent(id) != null || retreiveProfessor(id) != null) && id > 0) {
            id++;
        }
        if(id <= 0) {
            return -1; // Overflow
        }
        return id;
    }


    /**
     * @param rs ResultSet of Query like SELECT * FROM courses WHERE courseID = ___;
     * @return The professor teaching the course and the course itself if they exist, null otherwise
     */
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

    /**
     * @return The amount of entries in Professors and Students
     */
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

    /**
     * @return The amount of entries in Courses
     */
    public int courseCount() {
        try {
            ResultSet rs = conn.createStatement().executeQuery("SELECT COUNT(*) as count FROM courses;");
            rs.next();
            return rs.getInt("count");
        } catch (Exception e) {
            e.printStackTrace();
            return -1;
        }
    }

    /**
     * @return All courses in database
     */
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

    /**
     * @param id The user's ID
     * @param password The user's password
     * @return A login result corresponding to what user type if successful, invalid otherwise
     */
    @Override
    public LoginResult login(int id, String password) {
        Student student = retreiveStudent(id);
        Professor professor = retreiveProfessor(id);
        if (student != null && password.equals(student.password)) {
            return new LoginResult(LoginResultType.STUDENT, Optional.of(student), Optional.empty());
        } else if (professor != null && password.equals(professor.password)) {
            return new LoginResult(LoginResultType.PROFESSOR, Optional.empty(), Optional.of(professor));
        }
        return new LoginResult(LoginResultType.INVALID, Optional.empty(), Optional.empty());
    }


    /**
     * @param name Student's name
     * @param password Student's password
     * @return The student ID if process succeeds, -1 otherwise
     */
    @Override
    public int signUpStudent(String name, String password) {
        int id = nextPersonID();
        if(id <= 0) {
            return -1;
        }
        try {
            conn.createStatement().execute("INSERT INTO students (studentID, studentName, password) VALUES (" + id + ", '" + name + "', '" + password + "');");
            return id;
        } catch (Exception e) {
            e.printStackTrace();
            return -1;
        }
    }

    /**
     * @param name Professor's name
     * @param password Professor's password
     * @return The professor ID if process succeeds, -1 otherwise
     */
    @Override
    public int signUpProfessor(String name, String password) {
        int id = nextPersonID();
        if(id <= 0) {
            return -1;
        }
        try {
            conn.createStatement().execute("INSERT INTO professors (professorID, professorName, password) VALUES (" + id + ", '" + name + "', '" + password + "');");
            return id;
        } catch (Exception e) {
            e.printStackTrace();
            return -1;
        }
    }

    /**
     * @param studentID The target student
     * @return The student's GPA on a 4.0 grade scale
     */
    @Override
    public double studentGpa(int studentID) {
        try {
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT grade, courseID FROM enrollments WHERE studentID = " + studentID + ";");
            double totalCredits = 0;
            double weightedCredits = 0;
            while (rs.next()) {
                double grade = rs.getFloat("grade");
                if(grade < 0) {
                    continue;
                }
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

    /**
     * @param studentID The target student
     * @return All the course info for a given student
     */
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
     * @return True if successful, false otherwise
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
            stmt.execute("UPDATE enrollments SET grade = " + Float.parseFloat(newGrade) + " WHERE studentID = " + studentID + " AND courseID = " + courseID + ";");
            return true; // False warning from intelliJ
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * @param profID The professor teaching the course
     * @param courseName The name of the course
     * @param credits The credits this course has
     * @param semester What semester this course is taught in
     */
    @Override
    public void createCourse(int profID, String courseName, int credits, String semester) {
        int id = courseCount();
        while(retreiveCourse(id) != null && id > 0) {
            id++;
        }
        if(id < 0) {
            return;
        }
        try {
            Statement stmt = conn.createStatement();
            stmt.execute("INSERT INTO courses (courseID, courseName, credits, semester, professorID) VALUES (" + id + ", '" + courseName + "', " + credits + ", '" + semester + "', " + profID + ");");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * @param <T> First object of pair
     * @param <K> Second object of pair
     */
    static class Pair<T, K> {
        public T first;
        public K second;

        public Pair(T first, K second) {
            this.first = first;
            this.second = second;
        }
    }

    public List<Course> filterCourseCredits(int min) {
        try {
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT * FROM courses WHERE credits >= " + min +  ";");
            List<Course> result = new ArrayList<>();
            while(rs.next()) {
                result.add(createCourse(rs));
            }
            return result;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
