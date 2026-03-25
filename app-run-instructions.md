# App Run Instructions

This project uses Maven to compile, run, and package the application from the project root folder, which is the folder that contains `pom.xml`.

## 1. Open the project folder in a terminal

Navigate into the root of the project before running any commands.

Example:
```bash
cd /path/to/your/project
```

You should be in the folder where `pom.xml` is located. That is the correct place to run all Maven commands for this project.

## 2. Check whether Maven is already available

Before doing anything else, check whether Maven is installed and available in your terminal.

```bash
mvn -version
```

If Maven is already set up correctly, you can skip any installation steps and continue directly to the build and run commands below.

## 3. Compile the project

Use this command to compile the Java source files:

```bash
mvn compile
```

This step checks the code and builds the compiled `.class` files for the project.

## 4. Run the application

Use this command to launch the application through Maven:

```bash
mvn exec:java
```

This runs the main class defined in the `pom.xml`, which is configured for this application.

## 5. Package the project

Use this command to build the full application package:

```bash
mvn package
```

This step compiles the project, creates the `.jar` file, and runs any packaging steps configured in the Maven build file.

## 6. Find the generated `.jar` file

After packaging, the built application file can usually be found inside the `target` folder.

Example:
```bash
target/turn-for-turn-database-editor-1.0-SNAPSHOT.jar
```

You can check the contents of the `target` folder with:

```bash
ls target
```

## 7. Run the packaged `.jar` manually

After `mvn package` finishes, you can run by clicking or in terminal by doing the packaged application manually with:

```bash
java -jar target/turn-for-turn-database-editor-1.0-SNAPSHOT.jar
```

## 8. Notes

- Run all commands from the project root folder, not from inside `src` or another subfolder.
- The project root is the folder containing `pom.xml`.
- If `mvn -version` works, Maven is already available and you do not need to set it up again.
- If a command fails, read the terminal output carefully because Maven usually reports which file, class, or path caused the problem.
