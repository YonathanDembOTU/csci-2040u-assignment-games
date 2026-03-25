const fs = require("fs");
const path = require("path");

class GameService {
    constructor(filePath, imagesDir = null) {
        this.filePath = filePath;
        this.imagesDir = imagesDir;
        if (!fs.existsSync(this.filePath)) {
            this._writeGames([]);
        }
    }

    _readGames() {
        const data = fs.readFileSync(this.filePath, "utf-8").trim();

        if (!data) {
            return [];
        }

        const parsed = JSON.parse(data);
        return Array.isArray(parsed) ? parsed : [];
    }

    _writeGames(games) {
        fs.writeFileSync(this.filePath, JSON.stringify(games, null, 2));
    }

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

    getAll() {
        return this._readGames();
    }

    add(gameOrTitle, year, genre, imagePath) {
        const game = this._normalizeGame(gameOrTitle, year, genre, imagePath);

        if (
            !game.title ||
            !game.year ||
            !game.genre ||
            !game.image ||
            !game.description ||
            !game.releaseDate ||
            !game.ageRating ||
            !Array.isArray(game.platforms) ||
            game.platforms.length === 0 ||
            typeof game.price !== "number" ||
            Number.isNaN(game.price)
        ) {
            return { success: false, message: "All fields are required." };
        }

        const games = this._readGames();
        games.push(game);
        this._writeGames(games);

        return { success: true, message: "Game added!", game };
    }

    delete(title) {
        if (!title) {
            return { success: false, message: "Title is required." };
        }

        let deletedImage = null;
        const games = this._readGames();

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

        if (this.imagesDir && deletedImage && deletedImage.startsWith("/images/")) {
            const imgPath = path.join(this.imagesDir, path.basename(deletedImage));
            if (fs.existsSync(imgPath)) {
                fs.unlinkSync(imgPath);
            }
        }

        return { success: true, message: "Game deleted." };
    }

    exists(title) {
        return this.getAll().some(g => g.title === title);
    }
}

module.exports = GameService;
