const express = require("express");
const bodyParser = require("body-parser");
const fs = require("fs");
const path = require("path");
const multer = require("multer");

const UserService = require("./services/UserService");
const GameService = require("./services/GameService");

const app = express();
const PORT = 3000;

app.use(bodyParser.urlencoded({ extended: true }));
app.use(express.json());

app.use(express.static(path.join(__dirname, "public")));
app.use(express.static(path.join(__dirname, "views")));

// File paths for data storage
const USERS_FILE  = path.join(__dirname, "../../data/website/users.csv");
const GAMES_FILE  = path.join(__dirname, "../../data/website/games.json");
const IMAGES_DIR  = path.join(__dirname, "public/images");

// Initialize services
const userService = new UserService(USERS_FILE);
const gameService = new GameService(GAMES_FILE, IMAGES_DIR);

const storage = multer.diskStorage({
    // Save uploaded images to images directory
    destination: (req, file, cb) => cb(null, IMAGES_DIR),

    // Generate unique filename based on game title and timestamp
    filename: (req, file, cb) => {
        const ext   = path.extname(file.originalname);
        const title = (req.body.title || "game").toLowerCase().replace(/[^a-z0-9]/g, "_");
        cb(null, `${title}_${Date.now()}${ext}`);
    }
});

const upload = multer({
    storage,
    limits: { fileSize: 10 * 1024 * 1024 }, // 10MB max
    fileFilter: (req, file, cb) => {
        const allowed = /jpeg|jpg|png|webp|gif/;

        // Validate file extension and type
        const ok = allowed.test(path.extname(file.originalname).toLowerCase())
                && allowed.test(file.mimetype);

        ok ? cb(null, true) : cb(new Error("Only image files are allowed"));
    }
});

// Parse platforms
function parsePlatforms(value) {
    try {
        return {
            success: true,
            platforms: JSON.parse(value || "[]")
        };
    } catch {
        return {
            success: false,
            message: "Platforms must be a valid list."
        };
    }
}

// Routes for main pages
app.get("/", (req, res) => {
    res.sendFile(path.join(__dirname, "views/index.html"));
});

app.get("/dashboard", (req, res) => {
    res.sendFile(path.join(__dirname, "views/dashboard.html"));
});

// User authentication routes
app.post("/register", (req, res) => {
    const { username, password } = req.body;
    const result = userService.register(username, password);
    res.json(result);
});

app.post("/login", (req, res) => {
    const { username, password } = req.body;
    const result = userService.login(username, password);
    res.json(result);
});

// Get all games
app.get("/games", (req, res) => {
    res.json(gameService.getAll());
});

// Create a new game with image upload
app.post("/games", upload.single("image"), (req, res) => {
    const { title, year, genre, description, ageRating, releaseDate, price, purchaseLink, video } = req.body;

    // Parse platforms field
    const parsedPlatforms = parsePlatforms(req.body.platforms);
    if (!parsedPlatforms.success) {
        return res.status(400).json(parsedPlatforms);
    }

    // Ensure image was uploaded
    if (!req.file) {
        return res.status(400).json({ success: false, message: "Image is required." });
    }

    const imagePath = `/images/${req.file.filename}`;

    // Add game using service
    const result = gameService.add({
        title,
        year,
        genre,
        description,
        platforms: parsedPlatforms.platforms,
        ageRating,
        releaseDate,
        price: Number(price),
        image: imagePath,
        purchaseLink,
        video
    });

    res.status(result.success ? 200 : 400).json(result);
});

// Update an existing game
app.put("/games/:title", upload.single("image"), (req, res) => {
    const originalTitle = decodeURIComponent(req.params.title);

    // Find existing game
    const existingGame = gameService.getAll().find(game => game.title === originalTitle);
    if (!existingGame) {
        return res.status(404).json({ success: false, message: "Game not found." });
    }

    // Parse platforms
    const parsedPlatforms = parsePlatforms(req.body.platforms);
    if (!parsedPlatforms.success) {
        return res.status(400).json(parsedPlatforms);
    }

    const updates = {
        title: req.body.title,
        year: req.body.year,
        genre: req.body.genre,
        description: req.body.description,
        platforms: parsedPlatforms.platforms,
        ageRating: req.body.ageRating,
        releaseDate: req.body.releaseDate,
        price: Number(req.body.price),
        purchaseLink: req.body.purchaseLink,
        video: req.body.video
    };

    // Include new image if uploaded
    if (req.file) {
        updates.image = `/images/${req.file.filename}`;
    }

    const result = gameService.update(originalTitle, updates);

    // If update fails, delete newly uploaded image
    if (!result.success) {
        if (req.file) {
            const uploadedImagePath = path.join(IMAGES_DIR, req.file.filename);
            if (fs.existsSync(uploadedImagePath)) {
                fs.unlinkSync(uploadedImagePath);
            }
        }
        return res.status(400).json(result);
    }

    // Delete old image if a new one replaced it
    if (
        req.file &&
        existingGame.image &&
        existingGame.image !== result.game.image &&
        existingGame.image.startsWith("/images/")
    ) {
        const previousImagePath = path.join(IMAGES_DIR, path.basename(existingGame.image));
        if (fs.existsSync(previousImagePath)) {
            fs.unlinkSync(previousImagePath);
        }
    }

    res.json(result);
});

// Delete a game by title
app.delete("/games/:title", (req, res) => {
    const title = decodeURIComponent(req.params.title);
    const result = gameService.delete(title);
    res.status(result.success ? 200 : 404).json(result);
});

// Start server
app.listen(PORT, () => {
    console.log(`Server running at http://localhost:${PORT}`);
});