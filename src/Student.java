import java.io.ObjectInputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class Student implements User, Serializable {
    private static final long serialVersionUID = 1L;

    private String name;
    protected String lastLoginDate;
    private String type;
    private int id;
    private int loginStreak;
    public ArrayList<Redemption> redemptionHistory;
    public ArrayList<String> rewards;
    private int points;
    private double money;

    public Student(String name, int studentId, int points, double money) {
        this.name = name;
        this.id = studentId;
        this.points = points;
        this.money = money;
        this.loginStreak = 0;
        this.redemptionHistory = new ArrayList<>();
        this.rewards = new ArrayList<>();
    }

    public Student(String name, int studentId, int points) {
        this(name, studentId, points, 50.0);
    }

    public Student(String name, int studentId) {
        this(name, studentId, 0, 50.0);
    }

    public Student(String name, int id, String lastLoginDate, String type, int points, int loginStreak) {
        this.name = name;
        this.id = id;
        this.lastLoginDate = lastLoginDate;
        this.type = type;
        this.points = points;
        this.loginStreak = loginStreak;
        this.money = 50.0;
        this.redemptionHistory = new ArrayList<>();
        this.rewards = new ArrayList<>();
    }

    public Student() {
        this.name = "";
        this.id = 0;
        this.points = 0;
        this.money = 50.0;
        this.loginStreak = 0;
        this.redemptionHistory = new ArrayList<>();
        this.rewards = new ArrayList<>();
    }

    public int getPoints() {
        return points;
    }

    public void setPoints(int points) {
        this.points = points;
    }

    public int getLoginStreak() {
        return loginStreak;
    }

    public void setLoginStreak(int loginStreak) {
        this.loginStreak = loginStreak;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getID() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getType() {
        return type;
    }

    public List<Redemption> getRedemptionHistory() {
        if (redemptionHistory == null) redemptionHistory = new ArrayList<>();
        return redemptionHistory;
    }

    public List<String> getRewards() {
        if (rewards == null) rewards = new ArrayList<>();
        return rewards;
    }

    public double getMoney() {
        return money;
    }

    public void setMoney(double money) {
        this.money = money;
    }

    private void readObject(ObjectInputStream in) throws Exception {
        in.defaultReadObject();
        if (redemptionHistory == null) redemptionHistory = new ArrayList<>();
        if (rewards == null) rewards = new ArrayList<>();
    }
}
