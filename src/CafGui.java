import javafx.animation.ScaleTransition;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.effect.DropShadow;
import javafx.scene.image.Image;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.stage.Stage;
import javafx.util.Duration;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class CafGui extends Application {

    private Stage primaryStage;
    private Stage studentStage;
    private Stage managerStage;
    private Stage registerStage;
    private Stage loginStage;

    private ObservableList<MenuItemModel> menuItems = FXCollections.observableArrayList();
    private VBox studentRightMenu;
    private VBox managerRightPane;
    private VBox studentCenterBox;
    private Random rng = new Random();

    private StudentManager studentManager = new StudentManager();

    private Label studentNameLabel;
    private Label moneyLabel;
    private Label loyaltyLabel;
    private int currentStudentId = -1;

    private Map<Integer, List<String>> orderRecords = new HashMap<>();
    private int nextOrderId = 1;

    public void start(Stage stage) {
        primaryStage = stage;
        loadItemsOrSeed();
        loadOrders();
        Scene s = new Scene(buildMain(), 1000, 700);
        stage.setScene(s);
        stage.setTitle("Cafeteria");
        stage.getIcons().add(new Image("file:C:/Users/zizo/Documents/GitHub/Cafeteria-System/Coffe.png"));
        stage.show();

        studentStage = new Stage();
        studentStage.setTitle("Student");
        studentStage.getIcons().add(new Image("file:C:/Users/zizo/Documents/GitHub/Cafeteria-System/Coffe.png"));
        studentStage.setScene(createStudentScene(studentStage));

        managerStage = new Stage();
        managerStage.setTitle("Manager");
        managerStage.getIcons().add(new Image("file:C:/Users/zizo/Documents/GitHub/Cafeteria-System/Coffe.png"));
        managerStage.setScene(createManagerScene(managerStage));

        registerStage = new Stage();
        registerStage.setTitle("Register");
        registerStage.getIcons().add(new Image("file:C:/Users/zizo/Documents/GitHub/Cafeteria-System/Coffe.png"));
        registerStage.setScene(createRegisterScene(registerStage));

        loginStage = new Stage();
        loginStage.setTitle("Login");
        loginStage.getIcons().add(new Image("file:C:/Users/zizo/Documents/GitHub/Cafeteria-System/Coffe.png"));
        loginStage.setScene(createLoginScene(loginStage));
    }

    private StackPane buildMain() {
        StackPane bg = createStripBg();

        Button register = styledButton("Register");
        Button login = styledButton("Login");
        Button exit = smallBackButton();
        exit.setText("Exit");
        exit.setOnAction(e -> {
            saveAll();
            Platform.exit();
        });

        register.setOnAction(e -> {
            registerStage.show();
            registerStage.toFront();
        });
        login.setOnAction(e -> {
            loginStage.show();
            loginStage.toFront();
        });

        HBox topRow = new HBox(20, register, login);
        topRow.setAlignment(Pos.CENTER);
        VBox stackButtons = new VBox(12, topRow, exit);
        stackButtons.setAlignment(Pos.CENTER);

        StackPane root = new StackPane(bg, stackButtons);
        return root;
    }

    private void loadItemsOrSeed() {
        File f = new File("items.dat");
        if (f.exists()) {
            try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(f))) {
                Object obj = ois.readObject();
                if (obj instanceof ArrayList) {
                    List<MenuItemModel> list = (ArrayList<MenuItemModel>) obj;
                    menuItems.clear();
                    menuItems.addAll(list);
                    return;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        seedItems();
        saveItems();
    }

    private void loadOrders() {
        File f = new File("orders.dat");
        if (!f.exists()) return;
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(f))) {
            Object obj = ois.readObject();
            if (obj instanceof Map) {
                Map<?, ?> raw = (Map<?, ?>) obj;
                orderRecords.clear();
                for (Map.Entry<?, ?> e : raw.entrySet()) {
                    Integer k = (Integer) e.getKey();
                    List<String> v = (List<String>) e.getValue();
                    orderRecords.put(k, new ArrayList<>(v));
                    if (k >= nextOrderId) nextOrderId = k + 1;
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private void saveOrders() {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream("orders.dat"))) {
            oos.writeObject(orderRecords);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void seedItems() {
        menuItems.clear();
        menuItems.addAll(
                new MenuItemModel("Black coffe", 2, 6),
                new MenuItemModel("Latte", 3, 8),
                new MenuItemModel("Cappuccino", 4, 5),
                new MenuItemModel("Bread Sticks", 5, 10),
                new MenuItemModel("Cup Cake", 7, 4)
        );
    }

    private int randomPrice() {
        return 1 + rng.nextInt(5);
    }

    private int randomStock() {
        return 1 + rng.nextInt(10);
    }

    private Scene createRegisterScene(Stage owner) {
        StackPane bg = createStripBg();
        BorderPane content = new BorderPane();
        content.setPrefSize(600, 300);

        VBox form = new VBox(10);
        form.setPadding(new Insets(20));
        TextField nameFld = new TextField();
        nameFld.setPromptText("Full name");
        TextField idFld = new TextField();
        idFld.setPromptText("ID");
        Button enter = formButton("Enter");
        enter.setOnAction(e -> {
            String n = nameFld.getText().trim();
            String id = idFld.getText().trim();
            if (n.isEmpty() || id.isEmpty()) {
                Alert a = new Alert(Alert.AlertType.WARNING);
                a.setContentText("Name and ID are required");
                a.showAndWait();
                return;
            }
            int parsedId;
            try {
                parsedId = Integer.parseInt(id);
            } catch (NumberFormatException ex) {
                Alert a = new Alert(Alert.AlertType.WARNING);
                a.setContentText("ID must be numeric");
                a.showAndWait();
                return;
            }
            if (studentManager.isManager(parsedId)) {
                Alert a = new Alert(Alert.AlertType.WARNING);
                a.setContentText("Cannot register with a manager ID");
                a.showAndWait();
                return;
            }
            if (studentManager.studentExists(parsedId)) {
                Alert a = new Alert(Alert.AlertType.WARNING);
                a.setContentText("Student ID already registered");
                a.showAndWait();
                return;
            }
            boolean ok = studentManager.addStudent(n, parsedId);
            if (!ok) {
                Alert a = new Alert(Alert.AlertType.ERROR);
                a.setContentText("Registration failed");
                a.showAndWait();
                return;
            }
            Alert a = new Alert(Alert.AlertType.INFORMATION);
            a.setContentText("Registered: " + n + " (" + id + ")");
            a.showAndWait();

            primaryStage.hide();
            loginStage.hide();
            registerStage.hide();

            currentStudentId = parsedId;
            Student s = studentManager.getStudent(parsedId);
            updateStudentLabels(s);
            refreshStudentMenu();
            studentStage.show();
            studentStage.toFront();
        });
        form.getChildren().addAll(nameFld, idFld, enter);
        form.setAlignment(Pos.CENTER);
        content.setCenter(form);

        Button back = smallBackButton();
        back.setOnAction(e -> owner.hide());
        HBox bottom = new HBox(8, back);
        bottom.setAlignment(Pos.CENTER);
        bottom.setPadding(new Insets(10));
        content.setBottom(bottom);

        StackPane root = new StackPane(bg, content);
        return new Scene(root, 600, 300);
    }

    private Scene createLoginScene(Stage owner) {
        StackPane bg = createStripBg();
        BorderPane content = new BorderPane();
        content.setPrefSize(400, 200);

        VBox form = new VBox(10);
        form.setPadding(new Insets(20));
        TextField idFld = new TextField();
        idFld.setPromptText("ID");
        Button enter = formButton("Enter");
        enter.setOnAction(e -> {
            String id = idFld.getText().trim();
            if (id.isEmpty()) {
                Alert a = new Alert(Alert.AlertType.WARNING);
                a.setContentText("Please enter an ID");
                a.showAndWait();
                return;
            }
            int parsedId;
            try {
                parsedId = Integer.parseInt(id);
            } catch (NumberFormatException ex) {
                Alert a = new Alert(Alert.AlertType.WARNING);
                a.setContentText("ID must be numeric");
                a.showAndWait();
                return;
            }
            if (studentManager.isManager(parsedId)) {
                primaryStage.hide();
                loginStage.hide();
                registerStage.hide();
                refreshManagerMenu();
                managerStage.show();
                managerStage.toFront();
                return;
            }
            if (studentManager.studentExists(parsedId)) {
                primaryStage.hide();
                loginStage.hide();
                registerStage.hide();
                currentStudentId = parsedId;
                Student s = studentManager.getStudent(parsedId);
                updateStudentLabels(s);
                refreshStudentMenu();
                studentStage.show();
                studentStage.toFront();
            } else {
                Alert a = new Alert(Alert.AlertType.WARNING);
                a.setContentText("User not found");
                a.showAndWait();
            }
        });
        form.getChildren().addAll(idFld, enter);
        form.setAlignment(Pos.CENTER);
        content.setCenter(form);

        Button back = smallBackButton();
        back.setOnAction(e -> owner.hide());
        HBox bottom = new HBox(8, back);
        bottom.setAlignment(Pos.CENTER);
        bottom.setPadding(new Insets(10));
        content.setBottom(bottom);

        StackPane root = new StackPane(bg, content);
        return new Scene(root, 400, 200);
    }

    public Scene createStudentScene(Stage owner) {
        StackPane bg = createStripBg();

        BorderPane content = new BorderPane();
        content.setPrefSize(1000, 700);

        HBox top = new HBox();
        VBox leftBox = new VBox(2);
        studentNameLabel = new Label("testname");
        studentNameLabel.setFont(Font.font(20));
        studentNameLabel.setStyle("-fx-border-color: rgba(0,0,0,0.12); -fx-border-width: 1; -fx-border-radius: 6; -fx-padding: 6; -fx-background-color: rgba(245,245,245,0.18);");
        studentNameLabel.setTextFill(Color.BLACK);
        moneyLabel = new Label("Money: $0.00");
        moneyLabel.setFont(Font.font(14));
        moneyLabel.setTextFill(Color.YELLOW);
        leftBox.getChildren().addAll(studentNameLabel, moneyLabel);
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        loyaltyLabel = new Label("Loyalty points: 0");
        loyaltyLabel.setFont(Font.font(20));
        loyaltyLabel.setStyle("-fx-border-color: rgba(0,0,0,0.12); -fx-border-width: 1; -fx-border-radius: 6; -fx-padding: 6; -fx-background-color: rgba(245,245,245,0.18);");
        loyaltyLabel.setTextFill(Color.BLACK);
        top.getChildren().addAll(leftBox, spacer, loyaltyLabel);
        top.setPadding(new Insets(12, 18, 12, 18));
        content.setTop(top);

        studentCenterBox = new VBox();
        studentCenterBox.setAlignment(Pos.CENTER);
        studentCenterBox.setPadding(new Insets(20));
        content.setCenter(studentCenterBox);

        studentRightMenu = new VBox(6);
        studentRightMenu.setPadding(new Insets(10));
        studentRightMenu.setPrefWidth(300);
        refreshStudentMenu();
        ScrollPane sp = new ScrollPane(studentRightMenu);
        sp.setFitToWidth(true);
        sp.setStyle("-fx-background: transparent; -fx-background-color: transparent;");
        content.setRight(sp);

        HBox bottom = new HBox(8);
        Button back = smallBackButton();
        back.setOnAction(e -> {
            studentStage.hide();
            primaryStage.show();
        });
        Button exit = smallBackButton();
        exit.setText("Exit");
        exit.setOnAction(e -> {
            saveAll();
            Platform.exit();
        });
        bottom.setAlignment(Pos.CENTER);
        bottom.getChildren().addAll(back, exit);
        bottom.setPadding(new Insets(12));
        content.setBottom(bottom);

        StackPane root = new StackPane(bg, content);
        return new Scene(root, 1000, 700);
    }

    private void updateStudentLabels(Student s) {
        if (s == null) return;
        studentNameLabel.setText(s.getName());
        loyaltyLabel.setText("Loyalty points: " + s.getPoints());
        moneyLabel.setText(String.format("Money: $%.2f", s.getMoney()));
    }

    private void refreshStudentMenu() {
        studentRightMenu.getChildren().clear();
        Label menuTitle = new Label("Menu");
        menuTitle.setFont(Font.font(18));
        menuTitle.setStyle("-fx-border-color: rgba(0,0,0,0.12); -fx-border-width: 1; -fx-border-radius: 6; -fx-padding: 6; -fx-background-color: rgba(245,245,245,0.22);");
        menuTitle.setTextFill(Color.BLACK);
        studentRightMenu.getChildren().add(menuTitle);
        for (MenuItemModel m : menuItems) {
            Button item = menuButton(m.getName() + "\n$" + m.getPrice() + "    Stock: " + m.getStock());
            item.setOnAction(e -> showOrderConfirm(m));
            studentRightMenu.getChildren().add(item);
        }
    }

    private void showOrderConfirm(MenuItemModel m) {
        StackPane rootPane = (StackPane) studentStage.getScene().getRoot();
        StackPane overlay = new StackPane();
        overlay.setStyle("-fx-background-color: rgba(0,0,0,0.12);");
        VBox card = new VBox(12);
        card.setPadding(new Insets(16));
        card.setStyle("-fx-background-color: transparent; -fx-border-color: rgba(255,255,255,0.65); -fx-border-width: 2; -fx-border-radius: 8; -fx-background-radius:8;");
        card.setAlignment(Pos.CENTER);
        Label msg = new Label("Are you sure you want to order " + m.getName() + "?");
        msg.setFont(Font.font(22));
        msg.setTextFill(Color.WHITE);
        msg.setWrapText(true);
        msg.setMaxWidth(360);
        HBox buttons = new HBox(10);
        buttons.setAlignment(Pos.CENTER);
        Button yes = formButton("Yes");
        Button no = smallBackButton();
        no.setOnAction(e -> rootPane.getChildren().remove(overlay));
        buttons.getChildren().addAll(yes, no);
        card.getChildren().addAll(msg, buttons);
        overlay.getChildren().add(card);
        overlay.setPickOnBounds(true);
        rootPane.getChildren().add(overlay);
        yes.setOnAction(e -> {
            rootPane.getChildren().remove(overlay);
            if (m.getStock() <= 0) {
                Alert a = new Alert(Alert.AlertType.WARNING);
                a.setContentText("Out of stock");
                a.showAndWait();
                return;
            }
            Student s = studentManager.getStudent(currentStudentId);
            if (s == null) return;
            if (s.getMoney() < m.getPrice()) {
                Alert a = new Alert(Alert.AlertType.WARNING);
                a.setContentText("Insufficient funds");
                a.showAndWait();
                return;
            }
            m.decrementStock();
            s.setMoney(s.getMoney() - m.getPrice());
            studentManager.saveStudents();
            saveItems();
            updateStudentLabels(s);
            int orderId = nextOrderId++;
            List<String> list = new ArrayList<>();
            list.add(s.getName());
            list.add(m.getName());
            orderRecords.put(orderId, list);
            saveOrders();
            int minutes = 1 + rng.nextInt(3);
            Alert a = new Alert(Alert.AlertType.INFORMATION);
            a.setContentText("Estimated time of arrival: " + minutes + " minutes");
            a.showAndWait();
            refreshStudentMenu();
            refreshManagerMenu();
        });
    }

    public Scene createManagerScene(Stage owner) {
        StackPane bg = createStripBg();

        BorderPane content = new BorderPane();
        content.setPrefSize(1000, 700);

        HBox top = new HBox();
        Label title = new Label("Manager");
        title.setFont(Font.font(20));
        title.setStyle("-fx-border-color: rgba(0,0,0,0.12); -fx-border-width: 1; -fx-border-radius: 6; -fx-padding: 6; -fx-background-color: rgba(245,245,245,0.18);");
        title.setTextFill(Color.BLACK);
        HBox.setHgrow(title, Priority.ALWAYS);
        top.getChildren().addAll(title);
        top.setPadding(new Insets(12, 18, 12, 18));
        content.setTop(top);

        managerRightPane = new VBox(6);
        managerRightPane.setPadding(new Insets(10));
        managerRightPane.setPrefWidth(300);
        refreshManagerMenu();
        ScrollPane sp = new ScrollPane(managerRightPane);
        sp.setFitToWidth(true);
        sp.setStyle("-fx-background: transparent; -fx-background-color: transparent;");
        content.setRight(sp);

        HBox bottom = new HBox(8);
        Button back = smallBackButton();
        back.setOnAction(e -> {
            managerStage.hide();
            primaryStage.show();
        });
        Button report = smallBackButton();
        report.setText("Report");
        report.setOnAction(e -> showOrdersReport());
        Button exit = smallBackButton();
        exit.setText("Exit");
        exit.setOnAction(e -> {
            saveAll();
            Platform.exit();
        });
        bottom.setAlignment(Pos.CENTER);
        bottom.getChildren().addAll(back, report, exit);
        bottom.setPadding(new Insets(12));
        content.setBottom(bottom);

        StackPane root = new StackPane(bg, content);
        return new Scene(root, 1000, 700);
    }

    private void showOrdersReport() {
        Stage s = new Stage();
        VBox root = new VBox(8);
        root.setPadding(new Insets(12));
        if (orderRecords.isEmpty()) {
            Label l = new Label("No orders yet");
            l.setFont(Font.font(16));
            root.getChildren().add(l);
        } else {
            for (Map.Entry<Integer, List<String>> e : orderRecords.entrySet()) {
                Integer id = e.getKey();
                List<String> data = e.getValue();
                StringBuilder sb = new StringBuilder();
                sb.append("Order ID: ").append(id).append("\n");
                if (data.size() >= 2) {
                    sb.append("User: ").append(data.get(0)).append("\n");
                    sb.append("Item: ").append(data.get(1)).append("\n");
                } else {
                    for (String part : data) sb.append(part).append("\n");
                }
                Label l = new Label(sb.toString());
                l.setFont(Font.font(14));
                l.setStyle("-fx-border-color: rgba(0,0,0,0.08); -fx-border-width: 1; -fx-padding: 6; -fx-background-color: rgba(245,245,245,0.12);");
                l.setTextFill(Color.BLACK);
                root.getChildren().add(l);
            }
        }
        ScrollPane sp = new ScrollPane(root);
        sp.setFitToWidth(true);
        Scene scene = new Scene(sp, 400, 400);
        s.setScene(scene);
        s.setTitle("Orders Report");
        s.show();
    }

    private void refreshManagerMenu() {
        managerRightPane.getChildren().clear();
        Label t = new Label("Items");
        t.setFont(Font.font(18));
        t.setStyle("-fx-border-color: rgba(0,0,0,0.12); -fx-border-width: 1; -fx-border-radius: 6; -fx-padding: 6; -fx-background-color: rgba(245,245,245,0.22);");
        t.setTextFill(Color.BLACK);
        managerRightPane.getChildren().add(t);
        for (MenuItemModel m : menuItems) {
            HBox row = new HBox(6);
            row.setAlignment(Pos.CENTER_LEFT);
            Button item = menuButton(m.getName() + "\n$" + m.getPrice() + "    Stock: " + m.getStock());

            Button rest = formButton("Restock");
            rest.setOnAction(e -> showRestockConfirm(m, rest));
            row.getChildren().addAll(item, rest);
            managerRightPane.getChildren().add(row);
        }
    }

    private void showRestockConfirm(MenuItemModel m, Button restButton) {
        StackPane rootPane = (StackPane) managerStage.getScene().getRoot();
        StackPane overlay = new StackPane();
        overlay.setStyle("-fx-background-color: rgba(0,0,0,0.12);");
        VBox card = new VBox(12);
        card.setPadding(new Insets(16));
        card.setStyle("-fx-background-color: transparent; -fx-border-color: rgba(255,255,255,0.65); -fx-border-width: 2; -fx-border-radius: 8; -fx-background-radius:8;");
        card.setAlignment(Pos.CENTER);
        Label msg = new Label("Restock " + m.getName() + " to 10? This will take 10 seconds.");
        msg.setFont(Font.font(22));
        msg.setTextFill(Color.WHITE);
        msg.setWrapText(true);
        msg.setMaxWidth(360);
        HBox buttons = new HBox(10);
        buttons.setAlignment(Pos.CENTER);
        Button yes = formButton("Yes");
        Button no = smallBackButton();
        no.setOnAction(e -> rootPane.getChildren().remove(overlay));
        buttons.getChildren().addAll(yes, no);
        card.getChildren().addAll(msg, buttons);
        overlay.getChildren().add(card);
        overlay.setPickOnBounds(true);
        rootPane.getChildren().add(overlay);
        yes.setOnAction(e -> {
            rootPane.getChildren().remove(overlay);
            restButton.setDisable(true);
            Timeline t = new Timeline(new KeyFrame(Duration.seconds(10), ev -> {
                m.setStock(10);
                restButton.setDisable(false);
                saveItems();
                refreshManagerMenu();
                refreshStudentMenu();
            }));
            t.play();
        });
    }

    private StackPane createStripBg() {
        HBox bg = new HBox();
        Region left = new Region();
        Region mid = new Region();
        Region right = new Region();
        left.setStyle("-fx-background-color: #8B5A2B;");
        mid.setStyle("-fx-background-color: #3B3B3B;");
        right.setStyle("-fx-background-color: #D2B48C;");
        HBox.setHgrow(left, Priority.ALWAYS);
        HBox.setHgrow(mid, Priority.ALWAYS);
        HBox.setHgrow(right, Priority.ALWAYS);
        bg.getChildren().addAll(left, mid, right);
        StackPane holder = new StackPane(bg);
        holder.setPrefSize(1000, 700);
        return holder;
    }

    private Button styledButton(String text) {
        Button b = new Button(text);
        b.setFont(Font.font(18));
        b.setPadding(new Insets(10, 20, 10, 20));
        b.setStyle("-fx-background-radius: 12; -fx-background-color: linear-gradient(#f0c27b, #4b2e1e); -fx-text-fill: white;");
        b.setEffect(new DropShadow(8, Color.color(0, 0, 0, 0.45)));
        addHoverAnimation(b);
        return b;
    }

    private Button menuButton(String text) {
        Button b = new Button(text);
        b.setFont(Font.font(14));
        b.setPadding(new Insets(8, 12, 8, 12));
        b.setStyle("-fx-background-radius: 10; -fx-background-color: linear-gradient(#8b5a2b, #6b4226); -fx-text-fill: white;");
        b.setMaxWidth(Double.MAX_VALUE);
        b.setAlignment(Pos.CENTER_LEFT);
        addHoverAnimation(b);
        return b;
    }

    private Button formButton(String text) {
        Button b = new Button(text);
        b.setFont(Font.font(15));
        b.setPadding(new Insets(8, 20, 8, 20));
        b.setStyle("-fx-background-radius: 10; -fx-background-color: linear-gradient(#6B4226, #A57C55); -fx-text-fill: white;");
        b.setEffect(new DropShadow(6, Color.color(0, 0, 0, 0.35)));
        addHoverAnimation(b);
        return b;
    }

    private Button smallBackButton() {
        Button b = new Button("Back");
        b.setFont(Font.font(13));
        b.setPadding(new Insets(6, 14, 6, 14));
        b.setStyle("-fx-background-radius: 8; -fx-background-color: #777777; -fx-text-fill: white;");
        addHoverAnimation(b);
        return b;
    }

    private void addHoverAnimation(Button b) {
        ScaleTransition stIn = new ScaleTransition(Duration.millis(140), b);
        stIn.setToX(1.06);
        stIn.setToY(1.06);
        ScaleTransition stOut = new ScaleTransition(Duration.millis(140), b);
        stOut.setToX(1.0);
        stOut.setToY(1.0);
        b.setOnMouseEntered(e -> stIn.playFromStart());
        b.setOnMouseExited(e -> stOut.playFromStart());
    }

    private void saveItems() {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream("items.dat"))) {
            ArrayList<MenuItemModel> list = new ArrayList<>(menuItems);
            oos.writeObject(list);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @SuppressWarnings("unchecked")
    private static class MenuItemModel implements Serializable {
        private static final long serialVersionUID = 1L;
        private String name;
        private int price;
        private int stock;
        MenuItemModel(String n, int p, int s) { name = n; price = p; stock = s; }
        String getName() { return name; }
        int getPrice() { return price; }
        int getStock() { return stock; }
        void setStock(int v) { stock = v; }
        void decrementStock() { if (stock>0) stock--; }
    }

    private void saveAll() {
        studentManager.saveStudents();
        saveItems();
        saveOrders();
    }

    @Override
    public void stop() {
        saveAll();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
