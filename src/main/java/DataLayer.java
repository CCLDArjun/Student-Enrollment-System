import java.util.*;

// Entities
class Student {
    int studentID;
    String studentName;
    String password;

    public Student(int studentID, String studentName, String password) {
        this.studentID = studentID;
        this.studentName = studentName;
        this.password = password;
    }
}

class Professor {
    int professorID;
    String professorName;
    String password;

    public Professor(int professorID, String professorName, String password) {
        this.professorID = professorID;
        this.professorName = professorName;
        this.password = password;
    }
}

class Course {
    int courseID;
    String courseName;
    int credits;
    String semester;
    Professor professor;

    public Course(int courseID, String courseName, int credits, String semester, Professor professor) {
        this.courseID = courseID;
        this.courseName = courseName;
        this.credits = credits;
        this.semester = semester;
        this.professor = professor;
    }
}

class Enrollment {
    int studentID;
    int courseID;
    String grade;
    String enrollmentDate;

    public Enrollment(int studentID, int courseID, String grade, String enrollmentDate) {
        this.studentID = studentID;
        this.courseID = courseID;
        this.grade = grade;
        this.enrollmentDate = enrollmentDate;
    }
}

// Rich return types
class StudentCourseInfo {
    Course course;
    String grade;
    Professor professor;

    public StudentCourseInfo(Course course, String grade, Professor professor) {
        this.course = course;
        this.grade = grade;
        this.professor = professor;
    }
}

class CourseStudentInfo {
    Student student;
    String grade;

    public CourseStudentInfo(Student student, String grade) {
        this.student = student;
        this.grade = grade;
    }
}

enum LoginResultType {
    STUDENT,
    PROFESSOR,
    INVALID
}

class LoginResult {
    LoginResultType type;
    Optional<Student> student;
    Optional<Professor> professor;

    public LoginResult(LoginResultType type, Optional<Student> student, Optional<Professor> professor) {
        this.type = type;
        this.student = student;
        this.professor = professor;
    }
}

// Interface for the Data Layer
interface DataLayer {

    List<Course> allCourses();

    LoginResult login(String name, String password);

    boolean signUp(String name, String password);

    double studentGpa(int studentID);

    List<StudentCourseInfo> studentCourses(int studentID);

    boolean addCourse(int studentID, int courseID);

    boolean dropCourse(int studentID, int courseID);

    List<Course> professorCourses(int professorID);

    List<CourseStudentInfo> courseStudents(int courseID);

    boolean changeGrade(int courseID, int studentID, String newGrade);
}

// In-memory mock implementation
class InMemoryDataLayer implements DataLayer {
    private List<Student> students = new ArrayList<>();
    private List<Professor> professors = new ArrayList<>();
    private List<Course> courses = new ArrayList<>();
    private List<Enrollment> enrollments = new ArrayList<>();

    public void populateDummyData() {
        Professor prof1 = new Professor(1, "ProfSmith", "password1");
        Professor prof2 = new Professor(2, "ProfJohnson", "password2");
        professors.add(prof1);
        professors.add(prof2);

        courses.add(new Course(1, "Database Systems", 3, "Spring 2025", prof1));
        courses.add(new Course(2, "Algorithms", 4, "Spring 2025", prof2));

        Student student1 = new Student(1, "Alice", "password1");
        Student student2 = new Student(2, "Bob", "password2");
        students.add(student1);
        students.add(student2);

        enrollments.add(new Enrollment(1, 1, "A", new Date().toString()));
        enrollments.add(new Enrollment(2, 2, "B", new Date().toString()));
    }

    @Override
    public boolean signUp(String name, String password) {
        int newID = students.size() + 1;
        students.add(new Student(newID, name, password));
        return true;
    }

    @Override
    public List<Course> allCourses() {
        return new ArrayList<>(courses);
    }

    @Override
    public LoginResult login(String name, String password) {
        for (Student s : students) {
            if (s.studentName.equals(name) && s.password.equals(password)) {
                return new LoginResult(LoginResultType.STUDENT, Optional.of(s), Optional.empty());
            }
        }
        for (Professor p : professors) {
            if (p.professorName.equals(name)) { // Assume professor doesn't need password for now
                return new LoginResult(LoginResultType.PROFESSOR, Optional.empty(), Optional.of(p));
            }
        }
        return new LoginResult(LoginResultType.INVALID, Optional.empty(), Optional.empty());
    }

    @Override
    public double studentGpa(int studentID) {
        List<Enrollment> studentEnrollments = enrollments.stream()
            .filter(e -> e.studentID == studentID)
            .toList();

        double totalPoints = 0;
        int totalCourses = 0;
        for (Enrollment e : studentEnrollments) {
            totalPoints += gradeToPoints(e.grade);
            totalCourses++;
        }
        return totalCourses == 0 ? 0.0 : totalPoints / totalCourses;
    }

    private double gradeToPoints(String grade) {
        return switch (grade.toUpperCase()) {
            case "A" -> 4.0;
            case "B" -> 3.0;
            case "C" -> 2.0;
            case "D" -> 1.0;
            case "F" -> 0.0;
            default -> 0.0;
        };
    }

    @Override
    public List<StudentCourseInfo> studentCourses(int studentID) {
        List<StudentCourseInfo> result = new ArrayList<>();
        for (Enrollment e : enrollments) {
            if (e.studentID == studentID) {
                Course course = courses.stream()
                    .filter(c -> c.courseID == e.courseID)
                    .findFirst().orElse(null);
                if (course != null) {
                    result.add(new StudentCourseInfo(course, e.grade, course.professor));
                }
            }
        }
        return result;
    }

    @Override
    public boolean addCourse(int studentID, int courseID) {
        enrollments.add(new Enrollment(studentID, courseID, "", new Date().toString()));
        return true;
    }

    @Override
    public boolean dropCourse(int studentID, int courseID) {
        return enrollments.removeIf(e -> e.studentID == studentID && e.courseID == courseID);
    }

    @Override
    public List<Course> professorCourses(int professorID) {
        List<Course> result = new ArrayList<>();
        for (Course c : courses) {
            if (c.professor.professorID == professorID) {
                result.add(c);
            }
        }
        return result;
    }

    @Override
    public List<CourseStudentInfo> courseStudents(int courseID) {
        List<CourseStudentInfo> result = new ArrayList<>();
        for (Enrollment e : enrollments) {
            if (e.courseID == courseID) {
                Student student = students.stream()
                    .filter(s -> s.studentID == e.studentID)
                    .findFirst().orElse(null);
                if (student != null) {
                    result.add(new CourseStudentInfo(student, e.grade));
                }
            }
        }
        return result;
    }

    @Override
    public boolean changeGrade(int courseID, int studentID, String newGrade) {
        for (Enrollment e : enrollments) {
            if (e.courseID == courseID && e.studentID == studentID) {
                e.grade = newGrade;
                return true;
            }
        }
        return false;
    }
}
