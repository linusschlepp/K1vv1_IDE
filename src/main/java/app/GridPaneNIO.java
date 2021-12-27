package app;

import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.Label;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TextArea;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import javafx.scene.paint.Color;
import javafx.scene.text.*;
import javafx.scene.text.Font;
import javafx.stage.Stage;

import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import static app.ClassType.*;


public class GridPaneNIO {

    Stage primaryStage;

    static StringBuilder sb = new StringBuilder();
    static TextFlow textFlow = new TextFlow();
    static String path = "";
    static HashMap<String, TreeItem<CustomItem>> packageNameHashMap = new HashMap<>();
    static HashMap<TextArea, String> textAreaStringHashMap = new HashMap<>();
    static TreeView<CustomItem> treeView = new TreeView<>();
    Menu menuClose = new Menu();
    MenuItem menuItemClose1 = new MenuItem("Close Program");
    Menu menuExecute = new Menu();
    MenuItem menuItemExec1 = new MenuItem("Execute Program");
    Menu menuAdd = new Menu();
    MenuItem menuItemAddClass = new MenuItem("Add Class");
    MenuItem menuItemAddInterface = new MenuItem("Add Interface");
    MenuItem menuItemAddEnum = new MenuItem("Add Enum");
    MenuItem menuItemAddPackage = new MenuItem("Add Package");
    MenuItem menuItemAddProject = new MenuItem("New Project");
    MenuItem menuItemSelectProject = new MenuItem("Select Project");
    static GridPane gridPane = new GridPane();
    static TreeItem<CustomItem> TreeItemProject;
    static String fileName = "";
    static TextArea textArea = new TextArea("Hello!");
    static List<File> listFiles = new ArrayList<>();

    /**
     * Constructor of class: GridPaneNIO
     * Calls method recreateProject
     * @param primaryStage mainStage, which is getting passed by the main class
     */
    GridPaneNIO(Stage primaryStage)  {
        this.primaryStage = primaryStage;
        recreateProject();
    }

    /**
     * Creates the Stage as well as the layout
     *
     * @throws FileNotFoundException due to the initialization of files
     */
    public void start() throws FileNotFoundException {


        menuExecute.getItems().add(menuItemExec1);
        menuAdd.getItems().addAll(menuItemAddClass, menuItemAddInterface, menuItemAddEnum, menuItemAddPackage, menuItemAddProject, menuItemSelectProject);
        menuClose.getItems().add(menuItemClose1);

        primaryStage.setTitle("Linus IDE");


        ImageView viewMenu = new ImageView(new Image(new FileInputStream(getRelativePath() + File.separator + "pictures/greenplay.png")));
        menuExecute.setGraphic(viewMenu);
        viewMenu.setFitHeight(20);
        viewMenu.setPreserveRatio(true);
        menuItemExec1.setOnAction(e -> execute());

        viewMenu = new ImageView(new Image(new FileInputStream(getRelativePath() + File.separator  + "pictures/terminate.png")));
        menuClose.setGraphic(viewMenu);
        menuItemClose1.setOnAction(e -> primaryStage.close());
        viewMenu.setFitHeight(20);
        viewMenu.setPreserveRatio(true);


        ImageView viewMenuItem = CLASS.getImage();
        viewMenu = new ImageView(new Image(new FileInputStream(getRelativePath() +  File.separator + "pictures/plus.png")));
        viewMenuItem.setFitHeight(30);
        viewMenuItem.setPreserveRatio(true);
        menuAdd.setGraphic(viewMenu);
        menuItemAddClass.setOnAction(e -> ClassWindow.display(CLASS, false));
        menuItemAddClass.setGraphic(viewMenuItem);
        viewMenuItem = INTERFACE.getImage();
        viewMenuItem.setFitHeight(30);
        viewMenuItem.setPreserveRatio(true);
        menuItemAddInterface.setGraphic(viewMenuItem);
        menuItemAddInterface.setOnAction(e -> ClassWindow.display(INTERFACE, false));
        viewMenuItem = ENUM.getImage();
        viewMenuItem.setFitHeight(30);
        viewMenuItem.setPreserveRatio(true);
        menuItemAddEnum.setGraphic(viewMenuItem);
        menuItemAddEnum.setOnAction(e -> ClassWindow.display(ENUM, false));
        viewMenuItem = PACKAGE.getImage();
        viewMenuItem.setFitHeight(17);
        viewMenuItem.setPreserveRatio(true);
        menuItemAddPackage.setGraphic(viewMenuItem);
        menuItemAddPackage.setOnAction(e -> ClassWindow.display(PACKAGE, true));
        viewMenu.setFitHeight(20);
        Label label = new Label();
        viewMenu.setPreserveRatio(true);
        menuItemAddProject.setOnAction(e -> {
            try {
                addProject();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        });
        viewMenuItem = new ImageView(new Image(new FileInputStream(getRelativePath() + File.separator  + "pictures/projectpicture.png")));
        viewMenuItem.setFitHeight(17);
        viewMenuItem.setPreserveRatio(true);
        menuItemAddProject.setGraphic(viewMenuItem);
        MenuBar menuBar = new MenuBar();
        menuBar.getMenus().addAll(menuAdd, menuExecute, menuClose);
        gridPane.add(menuBar, 0, 0);
        GridPane.setColumnSpan(menuBar, 2);
        gridPane.add(textFlow, 0, 1);
        GridPane.setColumnSpan(textFlow, 2);
        gridPane.add(treeView, 0, 2);
        gridPane.add(label, 1, 0);
        GridPane.setRowSpan(label, 1);
        gridPane.add(textArea, 1, 2);
        Scene scene = new Scene(gridPane, 800, 800);
        menuItemSelectProject.setOnAction(e -> {
            try {
                selectProject();
            } catch (FileNotFoundException ignored) {

            }
        });
        viewMenuItem = new ImageView(new Image(new FileInputStream(getRelativePath() + File.separator  + "pictures/selectprojecticon.png")));
        viewMenuItem.setFitHeight(17);
        viewMenuItem.setPreserveRatio(true);
        menuItemSelectProject.setGraphic(viewMenuItem);

        AtomicReference<TreeItem<CustomItem>> tempTreeItem = new AtomicReference<>();
        treeView.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue.getValue().getTextArea() != null) {
                tempTreeItem.set(newValue);
                textArea.setText(newValue.getValue().getTextArea().getText());
            }
        });

        /*
        everytime the text of textArea of a class is getting changed,
         the text in the file is getting updated as well
         */

        textArea.textProperty().addListener((observableValue, s, t1) -> {
            tempTreeItem.get().getValue().setText(t1);
            updateFile( tempTreeItem.get().getValue().getTextArea().getText(), tempTreeItem.get().getValue().getPath() );
            textAreaStringHashMap.forEach((k, v) -> {
                if (v.equals(tempTreeItem.get().getValue().getLabelText()))
                    k = tempTreeItem.get().getValue().getTextArea();
            });
            label.setGraphic(tempTreeItem.get().getValue().getImage());
            label.setText(tempTreeItem.get().getValue().getLabelText());
        });



        //scene.getStylesheets().add(getClass().getResource("styles/style.css").toExternalForm());

        primaryStage.setScene(scene);
        primaryStage.show();

    }

    /**
     *
     * @return relativePath of the stored files
     */
    public static String getRelativePath() {

        return new File("").getAbsolutePath()+"//"+"src"+"//"+"main"+"//"+"java";
    }

    /**
     *When the text in the textArea of each class changes, the content of the file is getting changed as well
     *
     * @param fileContent content of the file/ class
     * @param pathOfFile the location of the file
     */
    private static void updateFile(String fileContent, String pathOfFile){

        Path newPath;
        if(pathOfFile.contains(".java"))
            newPath = Paths.get(pathOfFile);
        else
            newPath = Paths.get(pathOfFile + ".java");
        try {
            Files.writeString(newPath, fileContent);
        }catch(Exception ex){
            ex.printStackTrace();
        }

    }

    /**
     * Opens File-Explorer and selects location of the project
     *
     * @throws FileNotFoundException NIO-codesegments are getting used
     */
    private static void selectProject() throws FileNotFoundException {

        JFileChooser f;

        try {
            textAreaStringHashMap = new HashMap<>();
            packageNameHashMap = new HashMap<>();
            listFiles.clear();

            f = new JFileChooser();
            f.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
            f.showSaveDialog(null);
            path = f.getCurrentDirectory() + File.separator + f.getSelectedFile().getName();
            fileName = f.getSelectedFile().getName();

            Files.writeString((Paths.get(getRelativePath() +  File.separator +
                    "projectfiles" + File.separator + "currentProject")), path);
        } catch (Exception e) {
            e.printStackTrace();
            return;
        }

        textFlow = new TextFlow();
        Text projectText = new Text("- " + f.getSelectedFile().getName() + " ");
        projectText.setFont(Font.font("verdana", FontWeight.BOLD, FontPosture.REGULAR, 15));
        projectText.setFill(Color.BLACK);
        Text pathText = new Text(path);
        pathText.setFill(Color.GRAY);
        pathText.setFont(Font.font("verdana", FontPosture.REGULAR, 15));

        textFlow.getChildren().addAll(projectText, pathText);
        TreeItemProject = new TreeItem<>(new CustomItem(PROJECT.getImage(), new Label(f.getSelectedFile().getName())));
        treeView.setRoot(TreeItemProject);
        recreateRecProject(new File(path));


    }

    /**
     * Creates files in the requested locations
     *
     * @param classContent content of the class/ file
     * @param className name of the class/ file
     */
    private static void createFile(String classContent, String className) {
        try {
            if(!Files.exists(Paths.get(path + File.separator +File.separator +  File.separator+  className + ".java")))
                Files.createFile(Paths.get(path + File.separator +File.separator +  File.separator+  className + ".java"));
            Files.writeString(Paths.get(path + File.separator +File.separator +  File.separator+  className + ".java"), classContent);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Gets called if a class is only added to the TreeView and not in the fileSystem
     *
     * @param file file which is getting added
     * @param classKind kind of the class enum, interface etc.
     */
    private static void addClass(File file, ClassType classKind) {
        TextArea tArea = new TextArea(getClassContent(file));
        TreeItem<CustomItem> treeItem = new TreeItem<>(new CustomItem(classKind.getImage(), new Label(file.getName().replaceAll(".java", "")), tArea, file.getPath()));
        TreeItemProject.getChildren().add(treeItem);
        textAreaStringHashMap.put(tArea, file.getName().replaceAll(".java", ""));
    }

    private static String getClassContent(File file) {
        StringBuilder sb = new StringBuilder();
        try {
            Files.lines(Paths.get(String.valueOf(file))).forEach(s -> sb.append(s).append("\n"));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return sb.toString();
    }

    /**
     * Gets called if a class only needs to be added in a package in the TreeView and not in the fileSystem
     *
     * @param packageName name of the package, in which the class is getting stored
     * @param filePath path of the individual file
     * @param className name of the class
     * @param classKind kind of the class enum, interface etc.
     * @param file individual file
     */
    public static void addToPackage1(String packageName, String filePath,  String className, ClassType classKind, File file)  {

        TreeItem<CustomItem> treeItem;
        if (classKind.equals(PACKAGE)) {
            treeItem = new TreeItem<>(new CustomItem(classKind.getImage(), new Label(className)));
            packageNameHashMap.put(className, treeItem);
            packageNameHashMap.get(packageName).getChildren().add(treeItem);
        } else {
            TextArea tArea = new TextArea(getClassContent(file));
//            treeItem = new TreeItem(new CustomItem(classKind.getImage(), new Label(className),
//                    tArea, path + File.separator + packageName +File.separator+ className));

            treeItem = new TreeItem<>(new CustomItem(classKind.getImage(), new Label(className),
                    tArea, filePath));

            textAreaStringHashMap.put(tArea, className);
            packageNameHashMap.get(packageName).getChildren().add(treeItem);
            // treeItem.getValue().setPath(getCorrectPath(treeItem));
        }

    }

    public static void addPackage1(String packageName) {

        TreeItem<CustomItem> treeItem = new TreeItem<>(new CustomItem(PACKAGE.getImage(), new Label(packageName)));
        packageNameHashMap.put(packageName, treeItem);
        TreeItemProject.getChildren().add(treeItem);

    }

    /**
     * Recreates last used project
     *
     */
    private static void recreateProject(){
        //if the project exists it gets added and recreated
        if(getProjectPath() != null) {
            addProject(getProjectPath());
            recreateRecProject(getProjectPath());
        }

    }

    private static void recreateRecProject(File file)  {

        if (file.isDirectory()) {
            if (!file.getName().equals("output")) {
                File[] entries = file.listFiles();
                if (entries != null) {
                    for (File entry : entries) {
                        if (file.getPath().equals(path)) {
                            if (entry.isDirectory() && !entry.getName().equals("output"))
                                addPackage1(entry.getName());
                            else if(entry.isFile() && !entry.getName().equals("output"))
                                addClass(entry, CLASS);
                        } else {
                            if (entry.isDirectory() && !entry.getName().equals("output"))
                                // addToPackage1(new File(entry.getParent()).getName(), entry.getName(), PACKAGE, entry);
                                addToPackage1(new File(entry.getParent()).getName(),
                                        entry.getPath(), entry.getName(), PACKAGE, entry);
                            else
                                addToPackage1(new File(entry.getParent()).getName(), entry.getPath(),
                                        entry.getName().replaceAll(".java", ""), CLASS, entry);
                        }

                        if (entry.isDirectory())
                            recreateRecProject(entry);
                    }
                }
            }
        } else {
            if (file.getPath().equals(path)) {
                addClass(file, CLASS);
            } else {
                // addToPackage1(new File(file.getParent()).getName(), file.getName().replaceAll(".java", ""), CLASS, file);
                addToPackage1(new File(file.getParent()).getName(), file.getPath(),
                        file.getName().replaceAll(".java", ""), CLASS, file);
            }

        }
    }

    /**
     *Writes path of file in currentProject
     *
     * @return File, which corresponds to the first line of the file currentProject
     */
    private static File getProjectPath() {

        try {
            BufferedReader br = new BufferedReader(new FileReader(getRelativePath() + File.separator +
                    "projectfiles" + File.separator + "currentProject"));
            return new File(br.readLine());
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Creates/ overwrites the currentProject file
     *
     */
    private static void createProjectFile() {

        try {
            Files.writeString(Paths.get(getRelativePath() + File.separator +
                    "projectfiles" + File.separator + "currentProject"), path);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    /**
     * Creates Files within packages  in the fileSystem
     *
     * @param classContent content of the class/ file
     * @param packageName name of the package/ directory
     * @param className name of the file
     * @param isPackage checks if file is a package and if a directory has to be created
     */
    private static void createFile(String classContent, String packageName, String className, boolean isPackage) {

        try {
            if (!isPackage) {
                Path newFile = Files.createFile(Paths.get(path + packageName + className + ".java"));
                Files.writeString(newFile, classContent);
            } else {
                if (!Files.exists(Paths.get(path + packageName + className)))
                    Files.createDirectory(Paths.get(path + packageName + className));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Opens fileDialog window and saves  the project in the requested location
     *
     * @throws IOException NIO code-segments are getting used
     */
    private static void addProject() throws IOException {


        textAreaStringHashMap = new HashMap<>();
        packageNameHashMap = new HashMap<>();
        listFiles.clear();

        FileDialog fd = new FileDialog(new Frame(), "Select the location for your project", FileDialog.SAVE);
        fd.setDirectory("C:\\");
        fd.setVisible(true);
        fileName = fd.getFile();
        path = fd.getDirectory() + fd.getFile();
        if (!Files.exists(Paths.get(path)))
            Files.createDirectory(Paths.get(path));
        createProjectFile();
        Text projectText = new Text("- " + fd.getFile() + " ");
        projectText.setFont(Font.font("verdana", FontWeight.BOLD, FontPosture.REGULAR, 15));
        projectText.setFill(Color.BLACK);
        Text pathText = new Text(path);
        pathText.setFill(Color.GRAY);
        pathText.setFont(Font.font("verdana", FontPosture.REGULAR, 15));

        textFlow.getChildren().addAll(projectText, pathText);
        TreeItemProject = new TreeItem<>(new CustomItem(PROJECT.getImage(), new Label(fd.getFile())));
        treeView.setRoot(TreeItemProject);
    }


    private static void addProject(File currentPath) {
        path = currentPath.getPath();
        fileName = currentPath.getName();
        textFlow = new TextFlow();
        Text projectText = new Text("- " + currentPath.getName() + " ");
        projectText.setFont(Font.font("verdana", FontWeight.BOLD, FontPosture.REGULAR, 15));
        projectText.setFill(Color.BLACK);
        Text pathText = new Text(path);
        pathText.setFill(Color.GRAY);
        pathText.setFont(Font.font("verdana", FontPosture.REGULAR, 15));



        textFlow.getChildren().addAll(projectText, pathText);
        TreeItemProject = new TreeItem<>(new CustomItem(PROJECT.getImage(), new Label(currentPath.getName())));
        treeView.setRoot(TreeItemProject);

    }

    /**
     *
     *
     * @param content Content of class/ file
     * @return True if file is main, False if it isn't
     */
    private boolean isMain(String content) {

        return content.contains("publicstaticvoidmain(String[]args)");
    }

    /**
     * New class/ file gets created in the fileSystem as well as in TreeView
     *
     * @param className name of the class/ file
     * @param classKind kind of the class e.g. enum, interface  etc.
     */
    public static void addClass(String className, ClassType classKind) {


        TextArea tArea = new TextArea(getClassContent(className, classKind.getClassType()));
//        tArea.textProperty().addListener((ObservableValue<? extends String> o, String oldValue, String newValue) ->
//        {
//
//            if (isValid(newValue)) {
//                tArea.setStyle("-fx-text-inner-color: #BA55D3;");
//            } else {
//                tArea.setStyle(null);
//            }
//        });
        //TreeItem is getting created
        TreeItem<CustomItem> treeItem = new TreeItem<>(new CustomItem(classKind.getImage(), new Label(className), tArea, path + File.separator+ className));

        TreeItemProject.getChildren().add(treeItem);
        textAreaStringHashMap.put(tArea, className);
        createFile(tArea.getText(), className);
    }

    /**
     * New package/ directory is getting created in the fileSystem and TreeView
     * @param packageName name of the package
     * @throws IOException due to the creation of a directory
     */
    public static void addPackage(String packageName) throws IOException {


        TreeItem<CustomItem> treeItem = new TreeItem<>(new CustomItem(PACKAGE.getImage() , new Label( packageName)));
        packageNameHashMap.put(packageName, treeItem);
        if (!Files.exists(Paths.get(path + File.separator + packageName)))
            Files.createDirectory(Paths.get(path + File.separator + packageName));
        TreeItemProject.getChildren().add(treeItem);

    }

    /**
     * Classes/ files are getting added to the directories  in the fileSystem
     *
     * @param packageName name of the individual-package
     * @param className name of the class/ file, which is getting stored in the package
     * @param classKind kind of the class enum, interface etc.
     * @throws FileNotFoundException gets thrown because createFile-method is getting called
     */
    public static void addToPackage(String packageName, String className, ClassType classKind) throws FileNotFoundException {

        TreeItem<CustomItem> treeItem;

        if (classKind.equals(PACKAGE)) {
            treeItem = new TreeItem<>(new CustomItem(classKind.getImage(), new Label(className)));
            packageNameHashMap.put(className, treeItem);
            packageNameHashMap.get(packageName).getChildren().add(treeItem);


            createFile("", getCorrectPath(treeItem), className, true);
        } else {
            TextArea tArea = generateTextAreaContent(packageName, className, classKind);
            treeItem = new TreeItem<>(new CustomItem(classKind.getImage(), new Label(className),
                    tArea, path + File.separator+packageName +File.separator+className ));
            textAreaStringHashMap.put(tArea, className);
            packageNameHashMap.get(packageName).getChildren().add(treeItem);
            treeItem.getValue().setPath(path+getCorrectPath(treeItem)+className);
            createFile(tArea.getText(), getCorrectPath(treeItem), className, false);

        }
    }


    private static TextArea generateTextAreaContent(String packageName, String className, ClassType classKind) {
        TreeItem<CustomItem> dummyItem = new TreeItem<>();
        packageNameHashMap.get(packageName).getChildren().add(dummyItem);
        getCorrectPath(dummyItem);
        packageNameHashMap.get(packageName).getChildren().remove(dummyItem);

        return new TextArea(getClassContent(className, classKind.getClassType()));
    }

    /**
     * gets right path of packages
     */
    private static String getCorrectPath(TreeItem<CustomItem> treeItem) {
        StringBuilder stringBuilder = new StringBuilder();
        List<String> stringList = new ArrayList<>();
        sb.setLength(0);

        try {
            while (treeItem.getParent() != null) {
                if (!(treeItem.getParent().getValue().getBoxText().getText().equals(fileName)))
                    stringList.add(treeItem.getParent().getValue().getBoxText().getText());
                treeItem = treeItem.getParent();
            }
        } catch (ClassCastException ex) {
            ex.printStackTrace();
        }

        sb.append("package ");

        stringBuilder.append(File.separator);
        for (int i = stringList.size() - 1; i >= 0; i--) {
            stringBuilder.append(stringList.get(i)).append(File.separator);
            if (!stringList.get(i).equals(fileName))
                sb.append(stringList.get(i));
            if (i > 0)
                sb.append(".");
        }


        sb.append(";").append("\n\n");

        if (stringBuilder.toString().contains(fileName))
            return stringBuilder.toString().replaceAll(fileName, "");

        return stringBuilder.toString();
    }


//    private static boolean isValid(String text) {
//        return text.contains("public");
//    }

    /**
     * Creates the contents of the classes right after their creation
     *
     * @param classContent creates standard class content of each class
     * @param className name of the class/ file
     * @return standard content of each class
     */
    private static String getClassContent(String classContent, String className) {

        sb.append("public ").append(className).append(" ").append(classContent).append("{").append("\n\n");
        //if it's the main-class main-method is getting added
        if (ClassWindow.isSelected)
            sb.append("public static void main (String[] args) {").append("\n\n").append("}").append("\n\n");
        sb.append("}");
        String retString = sb.toString();
        sb.setLength(0);


        return retString;
    }

    /**
     * Opens cmd, compiles each files and runs them
     *
     */
    public void execute() {

        listFiles = new ArrayList<>();
        try {
            findFilesRec(new File(path));
            generateOutputFolder();
            copyDirectory(path, path + File.separator + "output");
            findPairs();
            AtomicReference<String> nameMain = new AtomicReference<>("");
            //nameMain is getting initialized
            textAreaStringHashMap.forEach((k, v) -> {
                if (isMain(k.getText().replaceAll(" ", "")))
                    nameMain.set(textAreaStringHashMap.get(k));
            });
            //cmd is getting called
            Runtime.getRuntime().exec("cmd /c start cmd.exe /K \"cd " + path +
                    File.separator + "output"+ "&&" + "javac *.java && java " + nameMain + ".java \"");
        } catch (Exception ex) {
            ex.printStackTrace();
        }

    }

    /**
     * All files of dir Directory are getting stored in fileList
     *
     * @param dir directory, where the project is located
     */
    private static void findFilesRec(File dir)  {
        if (dir.isDirectory()) {
            if (!dir.getName().equals("output")) {
                File[] entries = dir.listFiles();
                if (entries != null) {
                    for (File entry : entries) {
                        if (dir.getPath().equals(path)) {
                            if (entry.isFile())
                                listFiles.add(entry);
                        } else {
                            if (entry.isFile())
                                listFiles.add(entry);
                        }
                        if (entry.isDirectory())
                            findFilesRec(entry);
                    }
                }
            }
        } else {
            if (dir.getPath().equals(path)) {
                listFiles.add(dir);
            } else {
                listFiles.add(dir);
            }

        }
    }

    /**
     * Output-folder is getting created, files are getting stored in it
     *
     * @throws IOException new files are getting instantiated
     */
    private void generateOutputFolder() throws IOException {

        StringBuilder sb = new StringBuilder();
        //if the output-folder doesn't exist yet, it is getting created
        if(!Files.exists(Paths.get(path + File.separator + "output")))
            Files.createDirectory(Paths.get(path + File.separator + "output"));

        //all files of listFiles are stored in the output folder
        listFiles.forEach((f) -> {
            sb.setLength(0);
            try {
                if(!Files.exists(Paths.get(path + File.separator + "output" + File.separator + f.getName())))
                    Files.createFile(Paths.get(path + File.separator + "output" + File.separator + f.getName()));
                Files.lines(Paths.get(String.valueOf(f))).forEach(s -> sb.append(s).append("\n"));
                Files.writeString(Paths.get(path + File.separator + "output" + File.separator + f.getName()), sb.toString());
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }

    /**
     * Copies directories the contents of the source-directory into the directory
     *
     * @param sourceDirectoryLocation path of the individual project
     * @param destinationDirectoryLocation path of the output folder
     * @throws IOException NIO-segments are getting used
     */
    public static void copyDirectory(String sourceDirectoryLocation, String destinationDirectoryLocation) throws IOException {

        Files.walk(Paths.get(sourceDirectoryLocation))
                .forEach(source -> {
                    Path destination = Paths.get(destinationDirectoryLocation, source.toString()
                            .substring(sourceDirectoryLocation.length()));
                    try {
                        if(!source.getFileName().toString().equals("output"))
                            Files.copy(source, destination,  StandardCopyOption.REPLACE_EXISTING);
                    } catch (IOException ignored) {

                    }
                });
    }

    /**
     * Pairs of .java and .class files are getting found and created
     *
     */
    private static void findPairs(){

        File[] dir = new File(path + File.separator + "output").listFiles();
        int counter = 0;


        for(File f : listFiles){
            String tempPath;
            Path tempFile;

            assert dir != null;
            for(File f1 : dir){
                if(f1.isDirectory() ||!f1.getName().contains(".class"))
                    continue;
                if(f.getName().replaceAll(".java","").
                        equals(f1.getName().replaceAll(".class",""))){
                    tempPath = listFiles.get(counter).getPath().replace(path, path+"\\"+
                            "output");
                    tempFile = Paths.get(f1.getPath());
                    try {
                        Files.copy(tempFile,
                                Paths.get(tempPath.replaceAll(".java",".class")),
                                StandardCopyOption.REPLACE_EXISTING);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    break;
                }
            }

            counter++;
        }

    }
}