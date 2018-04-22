package sample;

import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ObservableLongValue;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.Button;
import javafx.scene.control.Dialog;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.*;
import javafx.scene.layout.GridPane;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Callback;
import javafx.util.Pair;
import org.apache.commons.io.FileUtils;
import sun.util.calendar.BaseCalendar;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.Optional;
import java.util.ResourceBundle;

public class Controller {

    @FXML private Button delete;
    @FXML
    private Button baton;
    @FXML
    private TableView leftTableView;
    @FXML
    private TableView rightTableView;
    @FXML
    private ComboBox leftCombo;
    @FXML
    private ComboBox rightCombo;
    @FXML
    private TextField leftTextField;
    @FXML
    private TextField rightTextField;
    @FXML
    private Button addFile;
    @FXML
    private Button addFolder;
    @FXML
    private Button copyButton;
    @FXML
    private Button moveButton;

    private String rightCurrentDirectoryPath;
    private String leftCurrentDirectoryPath;
    private String lastDirectory;
    private File curDir;
    private File rightCurrentDirecoty;
    private String leftRootVolume;
    private String rightRootVolume;
    private TableView<File> lastSelectedTableView;

    private ResourceBundle resourceBundle;
    private Locale locale;

    @FXML MenuItem polishItem;
    @FXML MenuItem englishItem;
    @FXML MenuItem fileClose;
    @FXML Menu settings;
    @FXML Menu language;
    @FXML Menu menuFile;


    private void switchLanguage(){
        fileClose.setText(resourceBundle.getString("close"));
        settings.setText(resourceBundle.getString("settings"));
        language.setText(resourceBundle.getString("language"));
        menuFile.setText(resourceBundle.getString("file"));
        refreshView();
    }

    @FXML
    private void closeApp(){
        Platform.exit();
        System.exit(0);
    }

    @FXML
    private void polishLanguage(){
        locale = new Locale("pl","PL");
        resourceBundle = ResourceBundle.getBundle("lang", locale);
        switchLanguage();
    }

    @FXML
    private void englishLanguage(){
        locale = new Locale("en","US");
        resourceBundle = ResourceBundle.getBundle("lang", locale);
        switchLanguage();
    }

    @FXML
    public void initialize() {
        leftTableView.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        rightTableView.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        leftRootVolume = "E:\\";//new File(".").getAbsolutePath().substring(0, 3);
        rightRootVolume = "E:\\";//new File(".").getAbsolutePath().substring(0, 3);
        initCombo(leftCombo);
        initCombo(rightCombo);
        Object item = leftCombo.getSelectionModel().getSelectedItem();
        curDir = new File(item.toString());
        rightCurrentDirecoty = new File(item.toString());
        englishLanguage();
    }

    private void initCombo(ComboBox comboBox) {
        comboBox.setItems(getListOfDisks());
        comboBox.getSelectionModel().select(leftRootVolume);
    }

    private String getNewFileSide() {
        if (lastSelectedTableView == leftTableView) {
            return leftCurrentDirectoryPath;
        }
        return rightCurrentDirectoryPath;
    }

    private void copyBackground(ObservableList<File> items, String path) {

        ProgressBar bar = new ProgressBar();
        Task task = new Task<Void>() {
            Task self = this;
            Dialog<Pair<String, String>> dialog = null;

            @Override
            public Void call() throws InterruptedException, IOException {

                Platform.runLater(new Runnable() {
                    @Override
                    public void run() {
                        dialog = new Dialog<>();
                        dialog.setTitle("Copying files");
                        dialog.setHeaderText("Copying files");
                        dialog.initModality(Modality.NONE);
                        dialog.setOnCloseRequest(ev -> Platform.runLater(new Runnable() {
                            @Override
                            public void run() {
                                self.cancel();
                                System.out.println("canceled task");
                            }
                        }));

                        GridPane grid = new GridPane();
                        grid.setHgap(10);
                        grid.setVgap(10);
                        grid.add(bar, 0, 0);
                        dialog.getDialogPane().setContent(grid);
                        dialog.getDialogPane().getButtonTypes().add(ButtonType.CANCEL);
                        dialog.show();
                    }
                });

                for(int i =0;i<items.size();i++){
                    File f = items.get(i);
                    if (f.isFile()) {
                        FileUtils.copyFileToDirectory(f, new File(path));
                    } else if (f.isDirectory()) {
                        FileUtils.copyDirectoryToDirectory(f, new File(path));
                    }
                    updateProgress(i, items.size());
                }
                Platform.runLater(new Runnable() {
                    @Override
                    public void run() {
                        dialog.close();
                    }
                });
                return null;
            }
        };
        bar.progressProperty().bind(task.progressProperty());
        new Thread(task).start();

    }

    private void move(ObservableList<File> items, String path) {
        ProgressBar bar = new ProgressBar();
        Task task = new Task<Void>() {
            Task self = this;
            Dialog<Pair<String, String>> dialog = null;

            @Override
            public Void call() throws InterruptedException, IOException {

                Platform.runLater(new Runnable() {
                    @Override
                    public void run() {
                        dialog = new Dialog<>();
                        dialog.setTitle("Moving files");
                        dialog.setHeaderText("Moving files");
                        dialog.initModality(Modality.NONE);
                        dialog.setOnCloseRequest(ev -> Platform.runLater(new Runnable() {
                            @Override
                            public void run() {
                                self.cancel();
                                System.out.println("canceled task");
                            }
                        }));

                        GridPane grid = new GridPane();
                        grid.setHgap(10);
                        grid.setVgap(10);
                        grid.add(bar, 0, 0);
                        dialog.getDialogPane().setContent(grid);
                        dialog.getDialogPane().getButtonTypes().add(ButtonType.CANCEL);
                        dialog.show();
                    }
                });

                for(int i =0;i<items.size();i++){
                    File f = items.get(i);
                    if (f.isFile()) {
                        FileUtils.moveFileToDirectory(f, new File(path), true);
                    } else if (f.isDirectory()) {
                        FileUtils.moveDirectoryToDirectory(f, new File(path), true);
                    }
                    updateProgress(i, items.size());
                }
                Platform.runLater(new Runnable() {
                    @Override
                    public void run() {
                        dialog.close();
                    }
                });
                return null;
            }
        };
        bar.progressProperty().bind(task.progressProperty());
        new Thread(task).start();
    }


    @FXML
    private void copyFile() {
        TextInputDialog dialog = new TextInputDialog(leftCurrentDirectoryPath);
        dialog.setTitle("Copy files");
        dialog.setHeaderText("Enter destination path for selected files");
        Optional<String> result = dialog.showAndWait();
        ObservableList<File> items = lastSelectedTableView.getSelectionModel().getSelectedItems();
        result.ifPresent(path -> {
            copyBackground(items, path);
        });
    }

    @FXML
    private void moveFile() {
        TextInputDialog dialog = new TextInputDialog(leftCurrentDirectoryPath);
        dialog.setTitle("Move files");
        dialog.setHeaderText("Enter destination path for selected files");
        Optional<String> result = dialog.showAndWait();
        ObservableList<File> items = lastSelectedTableView.getSelectionModel().getSelectedItems();
        result.ifPresent(path -> {
            move(items, path);
        });
        refreshView();
    }

    @FXML
    private void createFile() {
        String path = getNewFileSide();
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Create file");
        dialog.setHeaderText("Enter file name");

        Optional<String> result = dialog.showAndWait();
        result.ifPresent(name -> {
            try {
                new File(path + name).createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
        refreshView();
    }

    @FXML
    private void createDir() {
        String path = getNewFileSide();
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Create folder");
        dialog.setHeaderText("Enter folder name");

        Optional<String> result = dialog.showAndWait();
        result.ifPresent(name -> {
            new File(path + name).mkdir();
        });
        refreshView();
    }

    @FXML
    private void leftTextFieldAction() {
        File f = new File(leftTextField.getText());
        if (f.isDirectory()) {
            showDirectory(f, leftTableView);
        }
    }

    @FXML
    private void rightTextFieldAction() {
        File f = new File(leftTextField.getText());
        if (f.isDirectory()) {
            showDirectory(f, rightTableView);
        }
    }

    @FXML
    private void leftTableDragDetected(MouseEvent e) {
        System.out.println("drag detected");
        TableView<File> list = (TableView<File>) e.getSource();
        Dragboard db = list.startDragAndDrop(TransferMode.ANY);

        ClipboardContent content = new ClipboardContent();
        content.putString(list.getSelectionModel().getSelectedItem().getName());
        db.setContent(content);
        e.consume();
    }

    @FXML
    private void deleteItem() {
        ObservableList<File> items = null;
        if (leftTableView.getSelectionModel().getSelectedItems().size() > rightTableView.getSelectionModel().getSelectedItems().size()) {
            items = leftTableView.getSelectionModel().getSelectedItems();
        } else {
            items = rightTableView.getSelectionModel().getSelectedItems();
        }

        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Delete operation");
        alert.setHeaderText("Please confirm the deletion");

        Optional<ButtonType> result = alert.showAndWait();
        if (result.get() == ButtonType.OK) {
            for (File item : items) {
                try {
                    if (item.isFile()) {
                        Files.delete(item.toPath());
                    } else if (item.isDirectory()) {
                        FileUtils.deleteDirectory(item);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            refreshView();
        }
    }

    private void refreshView() {
        showDirectory(curDir, leftTableView);
        showDirectory(rightCurrentDirecoty, rightTableView);
    }

    @FXML
    private void rightTableDragOver(DragEvent event) {
        if (event.getGestureSource() != event.getTarget() && event.getDragboard().hasString()) {
            event.acceptTransferModes(TransferMode.COPY_OR_MOVE);
        }
        event.consume();
    }

    @FXML
    private void rightTableDragDropped(DragEvent event) {
        TableView<File> listTarget = (TableView<File>) event.getGestureTarget();
        Dragboard db = event.getDragboard();
        boolean success = false;
        if (db.hasString()) {
            File s = new File(leftCurrentDirectoryPath + db.getString());
            listTarget.getItems().add(new File(db.getString()));
            success = true;
            try {
                if (s.isDirectory()) {
                    FileUtils.copyDirectory(s, new File(rightCurrentDirectoryPath + db.getString()));
                } else if (s.isFile()) {
                    FileUtils.copyFileToDirectory(s, new File(rightCurrentDirectoryPath));
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        event.setDropCompleted(success);
        event.consume();
        System.out.println("drag released");
        refreshView();
    }

    @FXML
    private void leftClick(MouseEvent e) {
        lastSelectedTableView = leftTableView;
        selectItem(e, leftCurrentDirectoryPath, leftTableView, rightTableView);
    }

    @FXML
    private void rightClick(MouseEvent e) {
        lastSelectedTableView = rightTableView;
        selectItem(e, rightCurrentDirectoryPath, rightTableView, leftTableView);
    }

    @FXML
    private void leftComboSelect() {
        Object item = leftCombo.getSelectionModel().getSelectedItem();
        showDirectory(new File(item.toString()), leftTableView);
    }

    @FXML
    private void rightComboSelect() {
        Object item = rightCombo.getSelectionModel().getSelectedItem();
        showDirectory(new File(item.toString()), rightTableView);
    }

    @FXML
    private void rightRootDirClick() {
        rootDirClick(rightTableView, rightCurrentDirecoty, rightRootVolume);
    }

    @FXML
    private void leftRootDirClick() {
        rootDirClick(leftTableView, curDir, leftRootVolume);
    }

    private void rootDirClick(TableView listView, File currentDirectory, String rootVolume) {
        String curDirS = currentDirectory.getAbsolutePath();
        String backDir = curDirS.substring(0, curDirS.lastIndexOf("\\"));
        if (backDir.length() == 2) {
            showDirectory(new File(rootVolume), listView);
        } else {
            showDirectory(new File(backDir), listView);
        }
    }

    private ObservableList<String> getListOfDisks() {
        ObservableList<String> items = FXCollections.observableArrayList();
        for (File f : File.listRoots()) {
            items.add(f.getPath());
        }
        return items;
    }

    private void showDirectory(File file, TableView tableView) {
        String dirPath = null;
        if (tableView.getId().equals("leftTableView")) {
            curDir = file;
            if (leftCurrentDirectoryPath != null)
                lastDirectory = leftCurrentDirectoryPath;
            leftCurrentDirectoryPath = file.getPath();
            if (!leftCurrentDirectoryPath.endsWith("\\"))
                leftCurrentDirectoryPath += "\\";
            dirPath = leftCurrentDirectoryPath;
            leftRootVolume = leftCurrentDirectoryPath.substring(0, 3);
            leftTextField.setText(curDir.getPath());
        } else {
            rightCurrentDirecoty = file;
            if (rightCurrentDirectoryPath != null)
                lastDirectory = rightCurrentDirectoryPath;
            rightCurrentDirectoryPath = file.getPath();
            if (!rightCurrentDirectoryPath.endsWith("\\"))
                rightCurrentDirectoryPath += "\\";
            dirPath = rightCurrentDirectoryPath;
            rightRootVolume = rightCurrentDirectoryPath.substring(0, 3);
            rightTextField.setText(rightCurrentDirecoty.getPath());
        }

        final File[] filesList = file.listFiles();
        ObservableList<File> items = FXCollections.observableArrayList();
        for (File f : filesList) {
            items.add(f);
        }

        tableView.setItems(items);

        final String pa = dirPath;
        TableColumn<File, String> firstNameCol = new TableColumn<File, String>(resourceBundle.getString("name"));
        firstNameCol.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getPath().replace(pa, "")));

        TableColumn<File, String> secondNameCol = new TableColumn<File, String>(resourceBundle.getString("size"));
        secondNameCol.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().isFile() ? Long.toString(cellData.getValue().length()) : "<DIR>"));

        DateFormat formatter = DateFormat.getDateTimeInstance(
                DateFormat.SHORT,
                DateFormat.SHORT,
                locale);
        TableColumn<File, String> thirdNameCol = new TableColumn<File, String>(resourceBundle.getString("time"));
        thirdNameCol.setCellValueFactory(cellData -> new SimpleStringProperty(formatter.format(
                new java.util.Date(cellData.getValue().lastModified())))
        );
        tableView.getColumns().setAll(firstNameCol, secondNameCol, thirdNameCol);
    }


    private void selectItem(MouseEvent e, String path, TableView listView, TableView deselectListView) {
        deselectListView.getSelectionModel().clearSelection();
        if (e.getClickCount() == 2) {
            Object item = listView.getSelectionModel().getSelectedItem();
            File f = new File(item.toString());
            if (f.isDirectory()) {
                showDirectory(f, listView);
            } else if (f.isFile()) {
                try {
                    Desktop.getDesktop().open(f);
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
            }
        }
    }

}
