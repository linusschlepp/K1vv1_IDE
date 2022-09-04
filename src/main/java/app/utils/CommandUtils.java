package app.utils;

import app.backend.ClassType;
import app.backend.CustomItem;
import app.frontend.ClassBox;
import app.frontend.FrontendInit;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TreeItem;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.paint.Color;
import javafx.scene.text.*;
import javafx.scene.text.Font;
import javafx.stage.DirectoryChooser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.*;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

public class CommandUtils   {



    private static final Logger LOG = LoggerFactory.getLogger(CommandUtils.class);

    /**
     * Helper-method which copies ImageViews for the label
     *
     * @param treeItem treeItem, which image is getting copied
     */
    public static void setImageLabel(TreeItem<CustomItem> treeItem) {

        ImageView imageView;

        if (treeItem.getValue().getClassType().equals(ClassType.PACKAGE))
            imageView = new ImageView(new Image(Objects.requireNonNull(FrontendInit.class.getClassLoader().getResourceAsStream("images/packageIcon.png"))));
        else if (treeItem.getValue().getClassType().equals(ClassType.CLASS))
            imageView = new ImageView(new Image(Objects.requireNonNull(FrontendInit.class.getClassLoader().getResourceAsStream("images/classIcon.png"))));
        else if (treeItem.getValue().getClassType().equals(ClassType.INTERFACE))
            imageView = new ImageView(new Image(Objects.requireNonNull(FrontendInit.class.getClassLoader().getResourceAsStream("images/interfaceIcon.png"))));
        else
            imageView = new ImageView(new Image(Objects.requireNonNull(FrontendInit.class.getClassLoader().getResourceAsStream("images/enumIcon.png"))));
        imageView.setFitHeight(20);
        imageView.setPreserveRatio(true);
        FrontendConstants.label.setGraphic(imageView);


    }

    /**
     * Opens File-Explorer and selects location of the project
     *
     * @throws FileNotFoundException NIO-codesegments are getting used
     */
    public static void selectProject() throws FileNotFoundException {

        DirectoryChooser directoryChooser = new DirectoryChooser();

        try {
            FrontendConstants.textAreaStringHashMap = new HashMap<>();
            FrontendConstants.packageNameHashMap = new HashMap<>();
            FrontendConstants.listFiles.clear();
            File tempFile = directoryChooser.showDialog(FrontendConstants.primaryStage);
            FrontendConstants.path = tempFile != null ? tempFile.getPath() : FrontendConstants.path;
            FrontendConstants.fileName = tempFile != null ? tempFile.getName() : FrontendConstants.fileName;

            Files.writeString((Paths.get(FileUtils.getRelativePath() + Constants.FILE_SEPARATOR +
                    Constants.PROJECT_FILES + Constants.FILE_SEPARATOR + Constants.CURRENT_PROJECT)), FrontendConstants.path);


            //textArea gets resetted after selection
            FrontendConstants.textArea.setText(Constants.EMPTY_STRING);
        } catch (Exception e) {
            LOG.error("Project could not be selected");
            return;
        }


        Text projectText = new Text(Constants.HYPHEN + Constants.SPACE_STRING + FrontendConstants.fileName + Constants.SPACE_STRING);
        projectText.setFont(Font.font(Constants.CURRENT_FONT, FontWeight.BOLD, FontPosture.REGULAR, 15));
        projectText.setFill(Color.BLACK);
        Text pathText = new Text(FrontendConstants.path);
        pathText.setFill(Color.GRAY);
        pathText.setFont(Font.font(Constants.CURRENT_FONT, FontPosture.REGULAR, 15));


        FrontendConstants.textFlow.getChildren().addAll(projectText, pathText);
        FrontendConstants.gridPane.getChildren().add(FrontendConstants.textFlow);
        FrontendConstants.TreeItemProject = new TreeItem<>(new CustomItem(ClassType.PROJECT.getImage(), new Label(FrontendConstants.fileName), ClassType.PROJECT, FrontendConstants.path));
        FrontendConstants.treeView.setRoot(FrontendConstants.TreeItemProject);
        recreateRecProject(new File(FrontendConstants.path));

    }


    /**
     * Gets called if a class is only added to the TreeView and not in the fileSystem
     *
     * @param file      file which is getting added
     * @param classKind kind of the class enum, interface etc.
     */
    private static void addClass(File file, ClassType classKind) {
        TextArea tArea = new TextArea(FileUtils.getClassContent(file));
        TreeItem<CustomItem> treeItem = new TreeItem<>(new CustomItem(classKind.getImage(), new Label(file.getName().replaceAll(Constants.JAVA_FILE_EXTENSION, Constants.EMPTY_STRING)),
                tArea, file.getPath(), classKind));
        FrontendConstants.TreeItemProject.getChildren().add(treeItem);
        FrontendConstants.textAreaStringHashMap.put(tArea, file.getName().replaceAll(Constants.JAVA_FILE_EXTENSION, Constants.EMPTY_STRING));
    }


    /**
     * Gets called if a class only needs to be added in a package in the TreeView and not in the fileSystem
     *
     * @param packageName name of the package, in which the class is getting stored
     * @param filePath    path of the individual file
     * @param className   name of the class
     * @param classKind   kind of the class enum, interface etc.
     * @param file        individual file
     */
    public static void addToPackage(String packageName, String filePath, String className, ClassType
            classKind, File file) {

        TreeItem<CustomItem> treeItem;
        if (classKind.equals(ClassType.PACKAGE)) {
            treeItem = new TreeItem<>(new CustomItem(classKind.getImage(), new Label(className), ClassType.PACKAGE, file.getPath()));
            FrontendConstants.packageNameHashMap.put(className, treeItem);
            FrontendConstants.packageNameHashMap.get(packageName).getChildren().add(treeItem);
        } else {
            TextArea tArea = new TextArea(FileUtils.getClassContent(file));

            treeItem = new TreeItem<>(new CustomItem(classKind.getImage(), new Label(className),
                    tArea, filePath, classKind));

            FrontendConstants.textAreaStringHashMap.put(tArea, className);
            FrontendConstants.packageNameHashMap.get(packageName).getChildren().add(treeItem);
        }

    }

    /**
     * Adds package, but only to the TreeView and not in the file system
     *
     * @param packageName name of the package
     */
    public static void addPackage1(String packageName, File file) {

        TreeItem<CustomItem> treeItem = new TreeItem<>(new CustomItem(ClassType.PACKAGE.getImage(), new Label(packageName),
                ClassType.PACKAGE, file.getPath()));
        FrontendConstants.packageNameHashMap.put(packageName, treeItem);
        FrontendConstants.TreeItemProject.getChildren().add(treeItem);

    }

    /**
     * Recreates last used project
     */
    public static void recreateProject() {
        //if the project exists it gets added and recreated
        LOG.info("Starting to recreate project...");
        if (getProjectPath() != null) {
            addProject(getProjectPath());
            recreateRecProject(getProjectPath());
        }

        LOG.info("Successfully recreated project");

    }

    /**
     * Recreates the Project-structure in the TreeView by the given link
     *
     * @param file represents the project structure or is the project
     */
    private static void recreateRecProject(File file) {

        if (file.isDirectory()) {
            if (!file.getName().equals(Constants.OUTPUT_DIR) && !file.getName().equals(Constants.GIT_DIR)) {
                File[] entries = file.listFiles();
                if (entries != null) {
                    for (File entry : entries) {
                        if (file.getPath().equals(FrontendConstants.path)) {
                            if (entry.isDirectory() && !entry.getName().equals(Constants.OUTPUT_DIR) && !entry.getName().equals(Constants.GIT_DIR))
                                addPackage1(entry.getName(), entry);
                            else if (entry.isFile() && !entry.getName().equals(Constants.OUTPUT_DIR) && !entry.getName().equals(Constants.GIT_DIR))
                                addClass(entry, FileUtils.checkForClassType(entry));
                        } else {
                            if (entry.isDirectory() && !entry.getName().equals(Constants.OUTPUT_DIR) && !entry.getName().equals(Constants.GIT_DIR))
                                addToPackage(new File(entry.getParent()).getName(),
                                        entry.getPath(), entry.getName(), ClassType.PACKAGE, entry);
                            else
                                addToPackage(new File(entry.getParent()).getName(), entry.getPath(),
                                        entry.getName().replaceAll(Constants.JAVA_FILE_EXTENSION, Constants.EMPTY_STRING),
                                        FileUtils.checkForClassType(entry), entry);
                        }
                        if (entry.isDirectory())
                            recreateRecProject(entry);
                    }
                }
            }
        } else {
            if (file.getPath().equals(FrontendConstants.path)) {
                addClass(file, FileUtils.checkForClassType(file));
            } else {
                addToPackage(new File(file.getParent()).getName(), file.getPath(),
                        file.getName().replaceAll(Constants.JAVA_FILE_EXTENSION, Constants.EMPTY_STRING), FileUtils.checkForClassType(file), file);
            }

        }
    }

    /**
     * Writes path of file in currentProject
     *
     * @return File, which corresponds to the first line of the file currentProject
     */
    private static File getProjectPath() {

        try {
            // if the file does not exist yet, it gets created
            if (!Files.exists(Paths.get(FileUtils.getRelativePath() + Constants.FILE_SEPARATOR +
                    Constants.PROJECT_FILES + Constants.FILE_SEPARATOR + Constants.CURRENT_PROJECT))) {
                Files.createDirectory(Paths.get(FileUtils.getRelativePath() + Constants.FILE_SEPARATOR +
                        Constants.PROJECT_FILES));
                Files.createFile(Paths.get(FileUtils.getRelativePath() + Constants.FILE_SEPARATOR +
                        Constants.PROJECT_FILES + Constants.FILE_SEPARATOR + Constants.CURRENT_PROJECT));
            }

            try(BufferedReader br = new BufferedReader(new FileReader(FileUtils.getRelativePath() + Constants.FILE_SEPARATOR +
                    Constants.PROJECT_FILES + Constants.FILE_SEPARATOR + Constants.CURRENT_PROJECT))) {

                return new File(br.readLine());
            }
        } catch (Exception e) {
            if (e instanceof NullPointerException) {
                FrontendConstants.textFlow = new TextFlow();
                Text projectText = new Text(Constants.SELECT_FILE);
                projectText.setFont(Font.font(Constants.CURRENT_FONT, FontWeight.BOLD, FontPosture.REGULAR, 15));
                projectText.setFill(Color.BLACK);
                Text pathText = new Text(FrontendConstants.path);
                pathText.setFill(Color.GRAY);
                pathText.setFont(Font.font(Constants.CURRENT_FONT, FontPosture.REGULAR, 15));

                FrontendConstants.textFlow.getChildren().addAll(projectText, pathText);
            } else
                LOG.error("Path could not be written in currentProject");
        }
        return null;
    }


    /**
     * Creates/ overwrites the currentProject file
     */
    private static void createProjectFile() {

        try {
            Files.writeString(Paths.get(FileUtils.getRelativePath() + Constants.FILE_SEPARATOR +
                    Constants.PROJECT_FILES + Constants.FILE_SEPARATOR + Constants.CURRENT_PROJECT), FrontendConstants.path);
            // Clears text area after project file has been created
            FrontendConstants.textArea.setText(Constants.EMPTY_STRING);
        } catch (Exception ex) {
            LOG.error("Project file could not be created for path: [{}]", FrontendConstants.path);
        }
    }


    /**
     * Opens fileDialog window and saves in the project in the requested location
     *
     * @throws IOException NIO code-segments are getting used
     */
    public static void addProject() throws IOException {


        LOG.info("Open FileDialog and adding project");

        FrontendConstants.textAreaStringHashMap = new HashMap<>();
        FrontendConstants.packageNameHashMap = new HashMap<>();
        FrontendConstants.listFiles.clear();

        FileDialog fd = new FileDialog(new Frame(), Constants.SELECT_LOCATION, FileDialog.SAVE);
        fd.setDirectory(Constants.C_ROOT);
        fd.setVisible(true);

        FrontendConstants.fileName = fd.getFile() != null ? fd.getFile() : FrontendConstants.fileName;
        FrontendConstants.path = fd.getFile() != null && fd.getDirectory() != null ? fd.getDirectory() + fd.getFile() : FrontendConstants.path;
        if (!Files.exists(Paths.get(FrontendConstants.path)))
            Files.createDirectory(Paths.get(FrontendConstants.path));

        createProjectFile();
        FrontendConstants.textFlow = new TextFlow();
        Text projectText = new Text(Constants.HYPHEN + Constants.SPACE_STRING + FrontendConstants.fileName + Constants.SPACE_STRING);
        projectText.setFont(Font.font(Constants.CURRENT_FONT, FontWeight.BOLD, FontPosture.REGULAR, 15));
        projectText.setFill(Color.BLACK);
        Text pathText = new Text(FrontendConstants.path);
        pathText.setFill(Color.GRAY);
        pathText.setFont(Font.font(Constants.CURRENT_FONT, FontPosture.REGULAR, 15));

        FrontendConstants.textFlow.getChildren().addAll(projectText, pathText);
        FrontendConstants.TreeItemProject = new TreeItem<>(new CustomItem(ClassType.PROJECT.getImage(), new Label(FrontendConstants.fileName),
                ClassType.PROJECT, FrontendConstants.path));
        FrontendConstants.treeView.setRoot(FrontendConstants.TreeItemProject);

        LOG.info("Successfully added project");
    }

    /**
     * It just adds the project to the TreeView and creates the whole layout
     *
     * @param currentPath path, of the project, which is getting recreated in the TreeView
     */
    private static void addProject(File currentPath) {

        LOG.info("Adding project to stage...");
        FrontendConstants.path = currentPath.getPath();
        FrontendConstants.fileName = currentPath.getName();
        FrontendConstants.textFlow = new TextFlow();
        Text projectText = new Text(Constants.HYPHEN + Constants.SPACE_STRING + currentPath.getName() + Constants.SPACE_STRING);
        projectText.setFont(Font.font(Constants.CURRENT_FONT, FontWeight.BOLD, FontPosture.REGULAR, 15));
        projectText.setFill(Color.BLACK);
        Text pathText = new Text(FrontendConstants.path);
        pathText.setFill(Color.GRAY);
        pathText.setFont(Font.font(Constants.CURRENT_FONT, FontPosture.REGULAR, 15));


        FrontendConstants.textFlow.getChildren().addAll(projectText, pathText);
        FrontendConstants.TreeItemProject = new TreeItem<>(new CustomItem(ClassType.PROJECT.getImage(), new Label(currentPath.getName()),
                ClassType.PROJECT, FrontendConstants.path));
        FrontendConstants.treeView.setRoot(FrontendConstants.TreeItemProject);

        LOG.info("Successfully added project to stage");
    }

    /**
     * Checks class content for Main-class header. This method basically determines the main class
     *
     * @param content Content of class/ file
     * @return True if file is main, False if it isn't
     */
    private static boolean isMain(String content) {

        return content.contains(Constants.PSVM);
    }

    /**
     * New class/ file gets created in the fileSystem as well as in TreeView
     *
     * @param className name of the class/ file
     * @param classKind kind of the class e.g. enum, interface  etc.
     */
    public static void addClass(String className, ClassType classKind) {

        LOG.info("Adding class: [{}]", className);

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
        TreeItem<CustomItem> treeItem = new TreeItem<>(new CustomItem(classKind.getImage(),
                new Label(className), tArea, FrontendConstants.path + Constants.FILE_SEPARATOR + className + Constants.JAVA_FILE_EXTENSION, classKind));

        FrontendConstants.TreeItemProject.getChildren().add(treeItem);
        FrontendConstants.textAreaStringHashMap.put(tArea, className);
        FileUtils.createFile(FrontendConstants.path, tArea.getText(), className);

        LOG.info("Successfully added class: [{}]", className);
    }

    /**
     * New package/ directory is getting created in the fileSystem and TreeView
     *
     * @param packageName name of the package
     * @throws IOException due to the creation of a directory
     */
    public static void addPackage(String packageName, File file) throws IOException {

        LOG.info("Adding package: [{}]", packageName);

        TreeItem<CustomItem> treeItem = new TreeItem<>(new CustomItem(ClassType.PACKAGE.getImage(),
                new Label(packageName), ClassType.PACKAGE, file.getPath()));
        FrontendConstants.packageNameHashMap.put(packageName, treeItem);
        if (!Files.exists(Paths.get(FrontendConstants.path + Constants.FILE_SEPARATOR + packageName)))
            Files.createDirectory(Paths.get(FrontendConstants.path + Constants.FILE_SEPARATOR + packageName));
        FrontendConstants.TreeItemProject.getChildren().add(treeItem);

        LOG.info("Successfully added package: [{}]", packageName);

    }


    /**
     * Classes/ files are getting added to the directories in the fileSystem
     *
     * @param packageName name of the individual-package
     * @param className   name of the class/ file, which is getting stored in the package
     * @param classKind   kind of the class enum, interface etc.
     * @throws FileNotFoundException gets thrown because createFile-method is getting called
     */
    public static void addToPackage(String packageName, String className, ClassType classKind, File file) throws
            FileNotFoundException {

        LOG.info("Adding class: [{}] to package: [{}]", className, packageName);

        TreeItem<CustomItem> treeItem;

        if (classKind.equals(ClassType.PACKAGE)) {
            treeItem = new TreeItem<>(new CustomItem(classKind.getImage(), new Label(className), ClassType.PACKAGE, file.getPath()));
            FrontendConstants.packageNameHashMap.put(className, treeItem);
            FrontendConstants.packageNameHashMap.get(packageName).getChildren().add(treeItem);


            FileUtils.createFile(FrontendConstants.path, Constants.EMPTY_STRING, getCorrectPath(treeItem), className, true);
        } else {
            TextArea tArea = generateTextAreaContent(packageName, className, classKind);
            treeItem = new TreeItem<>(new CustomItem(classKind.getImage(), new Label(className),
                    tArea, FrontendConstants.path + Constants.FILE_SEPARATOR + packageName +
                    Constants.FILE_SEPARATOR + className + Constants.JAVA_FILE_EXTENSION, classKind));

            FrontendConstants.textAreaStringHashMap.put(tArea, className);
            FrontendConstants.packageNameHashMap.get(packageName).getChildren().add(treeItem);
            treeItem.getValue().setPath(FrontendConstants.path + getCorrectPath(treeItem) + className);
            FileUtils.createFile(FrontendConstants.path, tArea.getText(), getCorrectPath(treeItem), className, false);

        }

        LOG.info("Successfully added class: [{}] to package: [{}]", className, packageName);
    }

    /**
     * Adds the class headers to the individual TextAreas e.g. package1.package2 by analyzing the file-structures of the files;
     *
     * @param packageName name of the packages which are getting added
     * @param className   name of the class/enum or interface
     * @param classKind   classKind e.g. enum
     * @return instance of TextArea with corresponding content
     */
    private static TextArea generateTextAreaContent(String packageName, String className, ClassType classKind) {

        LOG.info("Creating file-content of: [{}]", className);

        TreeItem<CustomItem> dummyItem = new TreeItem<>();
        FrontendConstants.packageNameHashMap.get(packageName).getChildren().add(dummyItem);
        getCorrectPath(dummyItem);
        FrontendConstants.packageNameHashMap.get(packageName).getChildren().remove(dummyItem);

        LOG.info("Successfully created file-content of: [{}]", className);

        return new TextArea(getClassContent(className, classKind.getClassType()));
    }

    /**
     * Gets right path of packages
     */
    public static String getCorrectPath(TreeItem<CustomItem> treeItem) {

        LOG.info("Creating correct path...");
        StringBuilder stringBuilder = new StringBuilder();
        List<String> stringList = new ArrayList<>();
        FrontendConstants.sb.setLength(0);

        try {
            while (treeItem.getParent() != null) {
                if (!(treeItem.getParent().getValue().getBoxText().getText().equals(FrontendConstants.fileName)))
                    stringList.add(treeItem.getParent().getValue().getBoxText().getText());
                treeItem = treeItem.getParent();
            }
        } catch (ClassCastException ex) {
            LOG.error("Path could not be re/created");
        }

        FrontendConstants.sb.append(Constants.PACKAGE_STRING +Constants.SPACE_STRING);

        stringBuilder.append(Constants.FILE_SEPARATOR);
        for (int i = stringList.size() - 1; i >= 0; i--) {
            stringBuilder.append(stringList.get(i)).append(Constants.FILE_SEPARATOR);
            if (!stringList.get(i).equals(FrontendConstants.fileName))
                FrontendConstants.sb.append(stringList.get(i));
            if (i > 0)
                FrontendConstants.sb.append(Constants.DOT);
        }


        FrontendConstants.sb.append(Constants.SEMI_COLON).append(Constants.DOUBLE_NEW_LINE);

        LOG.info("Successfully created path");


        return stringBuilder.toString().contains(FrontendConstants.fileName) ? stringBuilder.toString().replaceAll(FrontendConstants.fileName, Constants.EMPTY_STRING) :
                stringBuilder.toString();
    }


    /**
     * Creates the contents of the classes right after their creation
     *
     * @param classContent creates standard class content of each class
     * @param className    name of the class/ file
     * @return standard content of each class
     */
    private static String getClassContent(String classContent, String className) {

        FrontendConstants.sb.append(Constants.PUBLIC+Constants.SPACE_STRING).append(className).append(Constants.SPACE_STRING).append(classContent).append(Constants.CURLY_BRACKETS_OPEN)
                .append(Constants.DOUBLE_NEW_LINE);
        //if it's the main-class, the main-method-head is getting added
        if (ClassBox.isSelected)
            FrontendConstants.sb.append(Constants.PSVM_INPUT).append(Constants.DOUBLE_NEW_LINE).append(Constants.CURLY_BRACKETS_CLOSE).append(Constants.DOUBLE_NEW_LINE);
        FrontendConstants.sb.append(Constants.CURLY_BRACKETS_CLOSE);
        String retString = FrontendConstants.sb.toString();
        FrontendConstants.sb.setLength(0);


        return retString;
    }

    /**
     * Opens cmd, compiles each file and runs them
     */
    public static void execute() {

        LOG.info("Trying to execute code...");

        FrontendConstants.listFiles = new ArrayList<>();
        try {
            findFilesRec(new File(FrontendConstants.path));
            FileUtils.generateOutputFolder(FrontendConstants.path);
            FileUtils.copyDirectory(FrontendConstants.path, FrontendConstants.path + Constants.FILE_SEPARATOR + Constants.OUTPUT_DIR);
            findPairs();
            AtomicReference<String> nameMain = new AtomicReference<>(Constants.EMPTY_STRING);
            AtomicReference<String> pathMain = new AtomicReference<>(Constants.EMPTY_STRING);
            //nameMain is getting initialized
            FrontendConstants.textAreaStringHashMap.forEach((k, v) -> {
                if (isMain(k.getText().replaceAll(Constants.SPACE_STRING, Constants.EMPTY_STRING)))
                    nameMain.set(FrontendConstants.textAreaStringHashMap.get(k));
            });
            nameMain.set(nameMain + Constants.JAVA_FILE_EXTENSION);
            FrontendConstants.listFiles.forEach(f -> {
                if (nameMain.get().equals(f.getName())) {
                    pathMain.set(f.getPath());
                }
            });
            //cmd is getting called, java files are compiled and executed
            String relativePathMain = pathMain.get().replace(FrontendConstants.path + Constants.FILE_SEPARATOR, Constants.EMPTY_STRING);

//            Runtime.getRuntime().exec("cmd /c start cmd.exe /K \"cd " + path +
//                    Constants.FILE_SEPARATOR + Constants.OUTPUT_DIR + "&&" + "javac -cp " + path + Constants.FILE_SEPARATOR + Constants.OUTPUT_DIR +
//                    Constants.SPACE_STRING + path + Constants.FILE_SEPARATOR + Constants.OUTPUT_DIR + Constants.FILE_SEPARATOR + relativePathMain +
//                    "&&" + "java " + relativePathMain + "\Constants.EMPTY_STRING);

            ProcessBuilder processBuilder = new ProcessBuilder();
            processBuilder.directory(new File(FrontendConstants.path + Constants.FILE_SEPARATOR + Constants.OUTPUT_DIR));
            processBuilder.command(Constants.CMD, Constants.C, Constants.START, Constants.JAVA_COMPILE, Constants.CP,
                    FrontendConstants.path + Constants.FILE_SEPARATOR + Constants.OUTPUT_DIR, FrontendConstants.path +
                            Constants.FILE_SEPARATOR + Constants.OUTPUT_DIR + Constants.FILE_SEPARATOR + relativePathMain).start();
            TimeUnit.MILLISECONDS.sleep(1000);
            processBuilder.command(Constants.CMD, Constants.C, Constants.START, Constants.CMD, Constants.K, Constants.JAVA, relativePathMain + "\"").start();

        } catch (Exception ex) {
            LOG.error("A problem while executing the code occurred");
        }

        LOG.info("Successfully executed code");
    }

    /**
     * All files of dir Directory are getting stored in fileList
     *
     * @param dir directory, where the project is located
     */
    static void findFilesRec(File dir) {
        if (dir.isDirectory()) {
            if (!dir.getName().equals(Constants.OUTPUT_DIR) && !dir.getName().equals(Constants.GIT_DIR)) {
                File[] entries = dir.listFiles();
                if (entries != null) {
                    for (File entry : entries) {
                        if (entry.isFile())
                            FrontendConstants.listFiles.add(entry);
                        if (entry.isDirectory())
                            findFilesRec(entry);
                    }
                }
            }
        } else
            FrontendConstants.listFiles.add(dir);
    }


    /**
     * Pairs of .java and .class files are getting found and created
     */
    private static void findPairs() {

        LOG.info(String.format("Creating pairs for %s and %s-files...", Constants.CLASS_FILE_EXTENSION, Constants.JAVA_FILE_EXTENSION));

        File[] dir = new File(FrontendConstants.path + Constants.FILE_SEPARATOR + Constants.OUTPUT_DIR).listFiles();
        int counter = 0;

        if (dir == null)
            return;

        for (File f : FrontendConstants.listFiles) {
            String tempPath;
            Path tempFile;

            for (File f1 : dir) {
                if (f1.isDirectory() || !f1.getName().contains(Constants.CLASS_FILE_EXTENSION))
                    continue;
                if (f.getName().replaceAll(Constants.JAVA_FILE_EXTENSION, Constants.EMPTY_STRING).
                        equals(f1.getName().replaceAll(Constants.CLASS_FILE_EXTENSION, Constants.EMPTY_STRING))) {
                    tempPath = FrontendConstants.listFiles.get(counter).getPath().replace(FrontendConstants.path,
                            FrontendConstants.path + Constants.FILE_SEPARATOR + Constants.OUTPUT_DIR);
                    tempFile = Paths.get(f1.getPath());
                    try {
                        Files.copy(tempFile,
                                Paths.get(tempPath.replaceAll(Constants.JAVA_FILE_EXTENSION, Constants.CLASS_FILE_EXTENSION)),
                                StandardCopyOption.REPLACE_EXISTING);
                    } catch (IOException e) {
                        LOG.error("A problem while finding pairs occurred");
                    }
                    break;
                }
            }
            counter++;
        }

        LOG.info(String.format("Successfully created pairs for %s and %s-files...", Constants.CLASS_FILE_EXTENSION, Constants.JAVA_FILE_EXTENSION));

    }

    //needed for renaming and deleting purposes, treeItem which is getting changed
    public static TreeItem<CustomItem> getRetTreeItem() {
        return FrontendConstants.retTreeItem;
    }

    public static void setRetTreeItem(TreeItem<CustomItem> newRetTreeItem) {
        FrontendConstants.retTreeItem = newRetTreeItem;
    }


}




