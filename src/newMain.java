import java.io.FileInputStream;
import java.io.ObjectInputStream;
import java.sql.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class newMain {

    // === DATABASE CONFIG - عدّلي هنا حسب إعداداتك ===
    private static final String DB_URL = "jdbc:mysql://localhost:3306/Cafeteria_System";
    private static final String DB_USER = "root";      // <-- غيّري لو لازم
    private static final String DB_PASS = "janahegazy2372005"; // <-- لا ترفعيه لمكان عام

    public static void main(String[] args) {
        // 1) حاول تتصل بقاعدة البيانات أولاً (عرض رسالة نجاح / فشل)
        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS)) {
            System.out.println("✅ Connected to the database: " + DB_URL);

            // 2) أنشئ Managers و انطلق بالـ demo logic
            StudentManager studentManager = new StudentManager(); // سيتم تحميل students.dat داخليًا إذا موجود
            Menu menu = new Menu(); // أفترض عندك كلاس Menu يقوم بتهيئة بعض العناصر افتراضياً
            MenuManager menuManager = new MenuManager(menu);

            // demo operations in-memory / business logic
            runDemoOperations(studentManager, menu, menuManager);

            // 3) مثال على مهاجرة الطلاب من ملف students.dat إلى قاعدة البيانات
            //    (الميثود يتعامل مع HashMap<Integer, Student> كما في مشروعك)
            migrateStudentsToDB(conn, "students.dat");

            // اختياري: يمكنك فعل migrations أخرى (items.dat, orders.dat) بنفس الأسلوب
        } catch (SQLException ex) {
            System.out.println("❌ Database connection failed. Check URL/credentials.");
            ex.printStackTrace();
        } catch (Exception e) {
            System.out.println("❌ Unexpected error:");
            e.printStackTrace();
        }
    }

    /**
     * Demo operations: يوضّح كيفية استخدام StudentManager, Menu, MenuManager, OrderItem, OrderProcessing
     * - نستخدم نفس OrderItem instance
     * - بعد كل addItemtoOrder(...) نحدّث الطالب في StudentManager من orderItem.getNewpoints()
     */
    private static void runDemoOperations(StudentManager studentManager, Menu menu, MenuManager menuManager) {
        System.out.println("== Display the menu ==");
        menu.displayFullMenu();

        // إضافة عنصر جديد عبر المدير
        try {
            MenuItem newItem = new MenuItem("Pizza", "Cheese Pizza", 90.0, "Main Courses", 3);
            menuManager.addItem(newItem);
            System.out.println("\nAfter adding Pizza:");
            menu.displayFullMenu();
        } catch (IllegalArgumentException e) {
            System.out.println("Could not add Pizza: " + e.getMessage());
        }

        // تأكد أن لدينا طالب في StudentManager وإلا أضفه
        int demoStudentId = 1;
        if (!studentManager.studentExists(demoStudentId)) {
            studentManager.addStudent("Jana", demoStudentId);
            // لو بتحبي تغيري إعدادات الطالب الافتراضية (points, money) بعد الإضافة:
            Student s0 = studentManager.getStudent(demoStudentId);
            if (s0 != null) {
                s0.setPoints(3);
                s0.setMoney(200.0);
                // lastLoginDate إذا حابة تضعي قيمة بصيغة ISO yyyy-MM-dd:
                s0.lastLoginDate = "2025-05-01";
                studentManager.saveStudents();
            }
        }

        Student student = studentManager.getStudent(demoStudentId);
        if (student == null) {
            System.out.println("Student not available, aborting demo.");
            return;
        }

        // نستخدم instance واحد من OrderItem لكل التطبيق
        OrderItem orderItem = new OrderItem();

        // OrderProcessing يتوقع OrderItem في الكونستركتور حسب كودك
        OrderProcessing orderProcessing = new OrderProcessing(orderItem);

        System.out.println("\n=== Adding items to order ===");
        System.out.println("Student points before: " + student.getPoints());

        // مثال: إضافة Coffee و Tiramisu إلى طلب واحد (id 1001)
        try {
            MenuItem coffee = menu.findItemByName("Coffee").orElse(null);
            MenuItem tiramisu = menu.findItemByName("Tiramisu").orElse(null);

            if (coffee != null) {
                System.out.println(orderItem.addItemtoOrder(student.getName(), 1001, student.getID(), student.getPoints(), coffee));
                // بعد حساب النقاط داخليًا، OrderItem يحتفظ بآخر قيمة newpoints
                student.setPoints(orderItem.getNewpoints()); // مزامنة النقاط مع Student الحقيقي
            }

            if (tiramisu != null) {
                System.out.println(orderItem.addItemtoOrder(student.getName(), 1001, student.getID(), student.getPoints(), tiramisu));
                student.setPoints(orderItem.getNewpoints());
            }

            // حفظ التغييرات
            studentManager.saveStudents();

            System.out.println("Total cost of the order 1001 = " + orderItem.getTotalCostOfOneOrder(1001));
            // تأكيد الطلب
            orderProcessing.confirmOrder(1001, student.getID());

        } catch (Exception ex) {
            System.out.println("Error while adding items: " + ex.getMessage());
            ex.printStackTrace();
        }

        System.out.println("Student points after order 1001: " + student.getPoints());

        // مثال: إنشاء طلبات إضافية واحتساب إجمالي تكلفة كل الطلبات
        try {
            MenuItem burger = menu.findItemByName("Burger").orElse(null);
            MenuItem tea = menu.findItemByName("Tea").orElse(null);

            if (tea != null) {
                System.out.println(orderItem.addItemtoOrder(student.getName(), 1002, student.getID(), student.getPoints(), tea));
                student.setPoints(orderItem.getNewpoints());
            }
            if (burger != null) {
                System.out.println(orderItem.addItemtoOrder(student.getName(), 1002, student.getID(), student.getPoints(), burger));
                student.setPoints(orderItem.getNewpoints());
            }

            orderProcessing.confirmOrder(1002, student.getID());

            System.out.println("Total cost of all orders = " + orderProcessing.getTotalCostOfAllOrders());
            studentManager.saveStudents();
        } catch (Exception ex) {
            System.out.println("Error while creating additional orders: " + ex.getMessage());
        }

        // فحص الـ sold out
        for (MenuItem item : menu.getMenuItems()) {
            if (item.getNumOfItemleft() == 0) {
                System.out.println(item.getName() + " is SOLD OUT!");
            }
        }

        // تجربة fulfillment queue
        OrderFulfillment fulfillment = new OrderFulfillment();
        String msg = fulfillment.addToWaitingList(1002);
        System.out.println(msg);
        System.out.println("Waiting list size = " + fulfillment.getWaitingList().size());
    }

    /**
     * Migrate students from a serialized students.dat (HashMap<Integer, Student>) into DB table `students`.
     * - يتوقع جدول students موجود كما في السكربت SQL الذي تحدثنا عنه (id, name, type, points, money, last_login, login_streak)
     */
    private static void migrateStudentsToDB(Connection conn, String studentsDatPath) {
        System.out.println("\n== Migrating students from " + studentsDatPath + " to database ==");
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(studentsDatPath))) {
            Object obj = ois.readObject();
            if (!(obj instanceof HashMap)) {
                System.out.println("The file does not contain expected HashMap<Integer, Student>");
                return;
            }

            @SuppressWarnings("unchecked")
            Map<Integer, Student> students = (HashMap<Integer, Student>) obj;

            String insert = "INSERT INTO students (id, name, type, points, money, last_login, login_streak, created_at, updated_at) "
                    + "VALUES (?, ?, ?, ?, ?, ?, ?, NOW(), NOW()) "
                    + "ON DUPLICATE KEY UPDATE name=VALUES(name), points=VALUES(points), money=VALUES(money), last_login=VALUES(last_login), login_streak=VALUES(login_streak)";

            try (PreparedStatement ps = conn.prepareStatement(insert)) {
                DateTimeFormatter[] acceptedFormats = new DateTimeFormatter[] {
                        DateTimeFormatter.ISO_LOCAL_DATE,             // 2025-05-01
                        DateTimeFormatter.ofPattern("d/M/yyyy"),      // 1/5/2025
                        DateTimeFormatter.ofPattern("d-M-yyyy")       // 1-5-2025
                };

                for (Student s : students.values()) {
                    ps.setInt(1, s.getID());
                    ps.setString(2, s.getName());
                    ps.setString(3, s.getType() == null ? "student" : s.getType());
                    ps.setInt(4, s.getPoints());
                    ps.setDouble(5, s.getMoney());

                    // try parse lastLoginDate in multiple patterns, else set NULL
                    java.sql.Timestamp ts = null;
                    if (s.lastLoginDate != null && !s.lastLoginDate.trim().isEmpty()) {
                        String txt = s.lastLoginDate.trim();
                        LocalDate parsed = null;
                        for (DateTimeFormatter fmt : acceptedFormats) {
                            try {
                                parsed = LocalDate.parse(txt, fmt);
                                break;
                            } catch (Exception ignore) { }
                        }
                        if (parsed != null) {
                            ts = java.sql.Timestamp.valueOf(parsed.atStartOfDay());
                        } else {
                            // not parseable; log and continue with null
                            System.out.println("Warning: could not parse lastLoginDate '" + s.lastLoginDate + "' for student id " + s.getID());
                        }
                    }
                    if (ts != null) ps.setTimestamp(6, ts); else ps.setNull(6, Types.TIMESTAMP);

                    ps.setInt(7, s.getLoginStreak());

                    ps.addBatch();
                }

                int[] results = ps.executeBatch();
                System.out.println("Migration finished. rows affected (batch length): " + results.length);
            }

        } catch (java.io.FileNotFoundException fnf) {
            System.out.println("students.dat not found. Skipping migration.");
        } catch (Exception ex) {
            System.out.println("Error during migration:");
            ex.printStackTrace();
        }
    }
}
