import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.HashMap;

public class StudentManager {
    private HashMap<Integer, Student> students = new HashMap<>();
    private HashMap<Integer, String> managers = new HashMap<>();

    public StudentManager() {
        managers.put(100001, "Abdelrahman Manager");
        managers.put(100002, "jana Manager");
        managers.put(100003, "mostafa Manager");
        loadStudents();
    }

    public HashMap<Integer, Student> getStudents() {
        return students;
    }

    public boolean addStudent(String name, int id) {
        if (managers.containsKey(id)) return false;
        if (students.containsKey(id)) return false;
        Student s = new Student(name, id);
        students.put(id, s);
        saveStudents();
        return true;
    }

    public boolean studentExists(int id) {
        return students.containsKey(id);
    }

    public Student getStudent(int id) {
        return students.get(id);
    }

    public boolean isManager(int id) {
        return managers.containsKey(id);
    }

    public String getManagerName(int id) {
        return managers.get(id);
    }

    public void checkStreakRewards(Student student) {
        int point;
        if (student == null) return;
        if (student.getLoginStreak() == 7) {
            point = student.getPoints() + 25;
            student.setPoints(point);
            System.out.println("7-day login streak! +25 bonus points");
            saveStudents();
        } else if (student.getLoginStreak() == 30) {
            point = student.getPoints() + 100;
            student.setPoints(point);
            System.out.println("30-day login streak! +100 bonus points");
            saveStudents();
        }
    }

    public void saveStudents() {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream("students.dat"))) {
            oos.writeObject(students);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @SuppressWarnings("unchecked")
    public void loadStudents() {
        File f = new File("students.dat");
        if (!f.exists()) return;
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(f))) {
            Object obj = ois.readObject();
            if (obj instanceof HashMap) {
                students = (HashMap<Integer, Student>) obj;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
