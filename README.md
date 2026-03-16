# Turns4Turns Development and Co.: Video Game Catalog
<p align="center" style="background-color:#f0f0f0; padding:2px;">
    <img src="logo.png" alt="Turn4Turns Development and Co." width="200">
</p>

## Project Overview:
This application serves as a centralized hub for Turn for Turn Co. to track game metadata, including developer details, ESRB ratings, platform availability, and financial data. The system is designed to be used by internal administrators, external publishing partners, and guest viewers.

---

## Role-Based Access Control:
The application enforces strict permissions based on the user's role:

| Role | Access Level | Capabilities |
| :--- | :--- | :--- |
| Admin | Full Access | Can add/edit/delete all records and manage publisher passwords. |
| Publisher | Restricted | Can only view and edit games where they are listed as the Publisher. |
| Guest | View-Only | Can search and filter the entire database but cannot save changes. |

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
* AES-128 Encryption: User credentials are encrypted in users.enc using a key stored in secret.key.
* Dynamic Theming: Toggle between Dark Mode and Light Mode instantly.
* Advanced Filtering: Real-time search and multi-category filters (Genre, Platform, ESRB Rating, etc.).
* Smart UI: Automatically hides technical columns like GameID and resizes table columns to fit content.

---

## File Structure:
* StartUp.java: The entry point and login gateway.
* AuthManager.java: Handles security, encryption, and account logic.
* DataModel.java: Manages the CSV data and in-memory row operations.
* DataView.java: The graphical interface and theming engine.
* DataController.java: The logic bridge between the UI and the data.
* data.csv: The flat-file database containing game records.

---

## How to Run the Program:
* To run the program, ensure that you have Java 11 or higher installed, and then run the DataController.java file.
---