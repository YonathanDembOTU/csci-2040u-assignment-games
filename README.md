# Turns4Turns Development and Co.: Video Game Catalog
<p align="center" style="background-color:#f0f0f0; padding:2px;">
    <img src="assets/logo.png" alt="Turn4Turns Development and Co." width="200">
</p>

## Project Members and Roles:

* **Project Manager:** Yonathan Demb
* **Documentation Manager:** Chaudhry Shayan Qadir
* **App Development Lead:** Abrar Rahman
* **Website Development Lead:** Keiren Burbank
* **Software Quality Lead:** Gurashish Virdi
* **Developers:** All

---

## Project Overview:
This application serves as a centralized hub for Turn4Turns Dev. & Co. to track game metadata, including developer details, ESRB ratings, platform availability, and financial data. The system is designed to be used by internal administrators, external publishing partners, and guest viewers.

---

## Role-Based Access Control:
The application enforces strict permissions based on the user's role:

| Role | Access Level | Capabilities |
| :--- | :--- | :--- |
| Admin | Full Access | Can add/edit/delete all records and manage publisher passwords. |
| Publisher | Restricted | Can only view and edit games where they are listed as the Publisher. |
| Guest | View-Only+ | Can view, favourite, search and filter the entire database and their favourites but cannot save changes. |

### Credentials:

#### Administrative Accounts
* Username: admin | Password: admin123
* Username: owner | Password: turnforturn

#### Publisher Accounts (Developers)
Accounts are automatically generated from the Publisher column in data.csv. The username is the publisher's name in lowercase (no spaces), and the default password is the username + 123.

Examples from current data:
* Nintendo: nintendo / nintendo123
* Rockstar Games: rockstargames / rockstargames123
* CD Projekt: cdprojekt / cdprojekt123

---

## Technical Features:
* **AES-128 Encryption:** User credentials are encrypted in users.enc using a key stored in secret.key.
* **Dynamic Theming:** Toggle between Dark Mode and Light Mode instantly.
* **Advanced Filtering:** Real-time search and multi-category filters (Genre, Platform, ESRB Rating, etc.).
* **Smart UI:** Automatically hides technical columns like GameID and resizes table columns to fit content.

---


## How to Run the Program on Intellij:
* To run the program, ensure that you have Java 11 or higher installed, and then run the `DataController.java` file.
* This file is located within `src\app\mvc`.

## How to Run the Website
* To run the website, open your computer terminal, navigate to the src/website folder, and type `npm install`
* From there, type `node server.js`, and go to `localhost:3000` in your browser.

# App Installation Instructions

This project uses Maven to compile, run, and package the application from the project root folder, which is the folder that contains `pom.xml`.

## 1. Open the project folder in a terminal

Navigate into the root of the project before running any commands.

Example:
```bash
cd /path/to/files
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

# Website Installation Steps

To run the automated process of installing, enter the `website-deploy` folder in the repository, run `installation.bat` to set up your environment, and then run `start.bat` to start the server.

To **manually** install the website, follow the listed steps:

## 1. Navigate to the Website Directory
Enter the website directory:

`cd src/website`

## 2. Install Dependencies
Install all required Node.js packages:

`npm install`

## 3. (Optionally) Configure Test Script and Run Tests
Set up the Jest test script:

`npm pkg set scripts.test="jest --verbose"`

Run the tests:

`npm test`

## 4. Start the Server
Run the website locally:

`node server.js`

## Notes
- Make sure you have Node.js and npm installed.
- The server will start on http://localhost:3000 unless configured otherwise.

---

## Application Demo

<img src="assets/appdemo.gif" alt="Application Demo" width="500">

## Website Demo

![Website Demo](assets/webdemo.gif)

* _(The demos may take a while to load/appear)_
---

## AI Usage
AI tools were used to assist in development of the website's front-end.

It contributed to:
- Generating boilerplate HTML and CSS for the desired layout
- Generating sample data (data/website/games.json) & (data/)

Everything generated by AI was reviewed by a developer before being incorporated into the project.
