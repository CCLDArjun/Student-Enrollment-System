import java.sql.Connection;
import java.sql.DriverManager;
import java.util.ArrayList;
import java.util.List;

public class Tester {
    public static void main(String[] args) {
        String url = "jdbc:mysql://localhost:3306/mydb";
        String user = "root";
        String password = "root";
        boolean printInfo = true;
        try {
            Connection conn = DriverManager.getConnection(url, user, password);
            Retriever r = new Retriever(conn);
            List<Course> courses =  r.allCourses();
            boolean testGen = false; // Only use for fresh gen
            if (testGen && courses.size() != Main.Generator.courseCount) {
                throw new Exception("FAIL | All Courses - Wrong amount of courses");
            }
            int testStudentID = 2;
            double gpa = r.studentGpa(testStudentID);
            System.out.println(testStudentID + "'s GPA is " + gpa);
            List<StudentCourseInfo> studentCourses = r.studentCourses(testStudentID);
            if(printInfo) {
                for (StudentCourseInfo info : studentCourses) {
                    System.out.print(info.course.courseID + ", " + info.professor.professorID + ", " + info.grade + " |  ");
                }
                System.out.println();
            }
            int beginSize = studentCourses.size();
            int existingCourse = 151;
            int fakeCourse = 0x7FFFFFF;
            int newCourse = 1;
            r.addCourse(testStudentID, existingCourse); // Already enrolled
            r.addCourse(testStudentID, fakeCourse); // Course DNE
            r.addCourse(testStudentID, newCourse); // Succeed
            int newSize = r.studentCourses(testStudentID).size();
            if(beginSize + 1 != newSize) {
                throw new Exception("FAIL | Add - Wrong amount of enrollments");
            }
            r.dropCourse(testStudentID, newCourse);
            newSize = r.studentCourses(testStudentID).size();
            if(beginSize != newSize) {
                throw new Exception("FAIL | Remove - Wrong amount of enrollments");
            }
            int professorID = 16485;
            List<Course> professorCourses = r.professorCourses(professorID);
            if(printInfo) {
                for (Course course : professorCourses) {
                    System.out.print(course.courseID + ", " + course.credits + ", " + course.courseName + ", " + course.professor.professorID + ", " + course.semester + " | ");
                }
                System.out.println();
            }
            int testCourse = 450;
            List<CourseStudentInfo> courseStudentInfos = r.courseStudents(testCourse);
            if(courseStudentInfos.size() != 98) {
                throw new Exception("FAIL | Student Info - Wrong amount of enrollments");
            }
            int changeCourse = 551;
            int changeStudent = 217;
            int fakeStudent = 0x1ee7c0de;
            double oldGrade = 51.7444;
            r.changeGrade(fakeCourse, changeStudent, String.valueOf(oldGrade)); // Course DNE
            r.changeGrade(changeCourse, fakeStudent, String.valueOf(oldGrade)); // Student DNE
            r.changeGrade(existingCourse, changeStudent, String.valueOf(oldGrade)); // Student not enrolled
            r.changeGrade(changeCourse, changeStudent, String.valueOf(oldGrade)); // Succeed
            int studentLoginID = 10235;
            String wrongStudentPassword = "leetcode";
            String correctStudentPassword = "0xdeadbeef";
            LoginResult wrongStudentPasswordResult = r.login(studentLoginID, wrongStudentPassword);
            if(wrongStudentPasswordResult.type != LoginResultType.INVALID || wrongStudentPasswordResult.professor.isPresent() || wrongStudentPasswordResult.student.isPresent()) {
                throw new Exception("FAIL | Login Student - Should have failed");
            }
            LoginResult correctStudentPasswordResult = r.login(studentLoginID, correctStudentPassword);
            if(correctStudentPasswordResult.type != LoginResultType.STUDENT || correctStudentPasswordResult.professor.isPresent() || correctStudentPasswordResult.student.isEmpty()) {
                throw new Exception("FAIL | Login Student - Should have succeeded");
            }
            int professorLoginID = 16699;
            String wrongProfessorPassword = "failall";
            String correctProfessorPassword = "hunter2";
            LoginResult wrongProfessorPasswordResult = r.login(professorLoginID, wrongProfessorPassword);
            if(wrongProfessorPasswordResult.type != LoginResultType.INVALID || wrongProfessorPasswordResult.professor.isPresent() || wrongProfessorPasswordResult.student.isPresent()) {
                throw new Exception("FAIL | Login Professor - Should have failed");
            }
            LoginResult correctProfessorPasswordResult = r.login(professorLoginID, correctProfessorPassword);
            if(correctProfessorPasswordResult.type != LoginResultType.PROFESSOR || correctProfessorPasswordResult.professor.isEmpty() || correctProfessorPasswordResult.student.isPresent()) {
                throw new Exception("FAIL | Login Professor - Should have succeeded");
            }
            int newStudentID = r.signUpStudent("Mara Sov", "gambitsucks");
            if(printInfo) {
                System.out.println("New Student ID is " + newStudentID);
            }
            if(newStudentID <= 0) {
                throw new Exception("FAIL | Sign up student");
            }
            int newProfessorID = r.signUpProfessor("Shaw Han", "iamaztecross");
            if(printInfo) {
                System.out.println("New Professor ID is " + newProfessorID);
            }
            if(newProfessorID <= 0) {
                throw new Exception("FAIL | Sign up professor");
            }
            int oldCourseSize = r.courseCount();
            r.createCourse(newProfessorID, "CS146", 3, "SM25");
            if(oldCourseSize + 1 != r.courseCount()) {
                throw new Exception("FAIL | Course Count Mismatch");
            }
        } catch (Exception e) {
            e.printStackTrace();
            return;
        }
        System.out.println("Pass");
    }
}
