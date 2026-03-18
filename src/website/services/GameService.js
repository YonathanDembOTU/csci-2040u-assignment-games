const fs = require("fs");
const path = require("path");

class GameService {
    constructor(filePath, imagesDir = null) {
        this.filePath = filePath;
        this.imagesDir = imagesDir;
        if (!fs.existsSync(this.filePath)) {
            fs.writeFileSync(this.filePath, "title,year,genre,image\r\n");
        }
    }

    _readLines() {
        const data = fs.readFileSync(this.filePath, "utf-8");
        return data.split(/\r?\n/);
    }

    _safe(value) {
        return String(value).replace(/,/g, "");
    }

    getAll() {
        const lines = this._readLines().slice(1);
        return lines
            .filter(line => line.trim() !== "")
            .map(line => {
                const [title, year, genre, image] = line.split(",");
                return { title, year, genre, image };
            });
    }

    add(title, year, genre, imagePath) {
        if (!title || !year || !genre || !imagePath) {
            return { success: false, message: "All fields are required." };
        }
        fs.appendFileSync(
            this.filePath,
            `${this._safe(title)},${this._safe(year)},${this._safe(genre)},${imagePath}\r\n`
        );
        return { success: true, message: "Game added!", game: { title, year, genre, image: imagePath } };
    }

    delete(title) {
        if (!title) {
            return { success: false, message: "Title is required." };
        }
        const lines = this._readLines();
        const header = lines[0];
        let deletedImage = null;

        const remaining = lines.slice(1).filter(line => {
            if (!line.trim()) return false;
            const [t, , , img] = line.split(",");
            if (t === title) {
                deletedImage = img;
                return false;
            }
            return true;
        });

        if (deletedImage === null) {
            return { success: false, message: "Game not found." };
        }

        fs.writeFileSync(this.filePath, [header, ...remaining].join("\r\n") + "\r\n");

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