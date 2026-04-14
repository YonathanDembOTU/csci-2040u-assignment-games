const fs = require("fs");
const path = require("path");

class GameService {
    constructor(filePath, imagesDir = null) {
        this.filePath = filePath;
        this.imagesDir = imagesDir;

        // Ensure the JSON file exists, initialize if it doesn't
        if (!fs.existsSync(this.filePath)) {
            this._writeGames([]);
        }
    }

    // Read and parse games from the JSON file
    _readGames() {
        const data = fs.readFileSync(this.filePath, "utf-8").trim();

        // Return empty list if file is empty
        if (!data) {
            return [];
        }

        const parsed = JSON.parse(data);

        // Ensure the parsed data is an array
        return Array.isArray(parsed) ? parsed : [];
    }

    // Write the games array back to the JSON file
    _writeGames(games) {
        fs.writeFileSync(this.filePath, JSON.stringify(games, null, 2));
    }

    // Normalize input
    _normalizeGame(gameOrTitle, year, genre, imagePath) {
        const input = typeof gameOrTitle === "object" && gameOrTitle !== null
            ? gameOrTitle
            : { title: gameOrTitle, year, genre, image: imagePath };

        const normalized = {};

        for (const [key, value] of Object.entries(input)) {
            if (value === undefined || value === null) {
                continue;
            }
            normalized[key] = typeof value === "string" ? value.trim() : value;
        }

        return normalized;
    }

    // Validate that a game object contains all required fields
    _isValidGame(game) {
        return Boolean(
            game.title &&
            game.year &&
            game.genre &&
            game.image &&
            game.description &&
            game.releaseDate &&
            game.ageRating &&
            Array.isArray(game.platforms) &&
            game.platforms.length > 0 &&
            typeof game.price === "number" &&
            !Number.isNaN(game.price)
        );
    }

    // Get all stored games
    getAll() {
        return this._readGames();
    }

    // Add a new game to the collection
    add(gameOrTitle, year, genre, imagePath) {
        const game = this._normalizeGame(gameOrTitle, year, genre, imagePath);

        // Return if required fields are missing
        if (!this._isValidGame(game)) {
            return { success: false, message: "All fields are required." };
        }

        const games = this._readGames();
        games.push(game);
        this._writeGames(games);

        return { success: true, message: "Game added!", game };
    }

    // Update an existing game by title
    update(title, updates = {}) {
        if (!title) {
            return { success: false, message: "Title is required." };
        }

        const normalizedUpdates = this._normalizeGame(updates);
        const games = this._readGames();

        // Find the game index by title
        const index = games.findIndex(game => game.title === title);

        if (index === -1) {
            return { success: false, message: "Game not found." };
        }

        // Merge updates with existing data
        const updatedGame = { ...games[index], ...normalizedUpdates };

        // Validate updated game before saving
        if (!this._isValidGame(updatedGame)) {
            return { success: false, message: "All fields are required." };
        }

        games[index] = updatedGame;
        this._writeGames(games);

        return { success: true, message: "Game updated.", game: updatedGame };
    }

    // Delete a game by title
    delete(title) {
        if (!title) {
            return { success: false, message: "Title is required." };
        }

        let deletedImage = null;
        const games = this._readGames();

        // Filter out the game to delete
        const remaining = games.filter(game => {
            if (game.title === title) {
                deletedImage = game.image;
                return false;
            }
            return true;
        });

        if (deletedImage === null) {
            return { success: false, message: "Game not found." };
        }

        this._writeGames(remaining);

        // Delete associated image file if it exists
        if (this.imagesDir && deletedImage && deletedImage.startsWith("/images/")) {
            const imgPath = path.join(this.imagesDir, path.basename(deletedImage));
            if (fs.existsSync(imgPath)) {
                fs.unlinkSync(imgPath);
            }
        }

        return { success: true, message: "Game deleted." };
    }

    // Check if a game exists by its title
    exists(title) {
        return this.getAll().some(g => g.title === title);
    }
}

module.exports = GameService;