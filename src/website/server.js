const express = require("express");
const bodyParser = require("body-parser");
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

const USERS_FILE  = path.join(__dirname, "../../data/website/users.csv");
const GAMES_FILE  = path.join(__dirname, "../../data/website/games.json");
const IMAGES_DIR  = path.join(__dirname, "public/images");

const userService = new UserService(USERS_FILE);
const gameService = new GameService(GAMES_FILE, IMAGES_DIR);

const storage = multer.diskStorage({
    destination: (req, file, cb) => cb(null, IMAGES_DIR),
    filename: (req, file, cb) => {
        const ext   = path.extname(file.originalname);
        const title = (req.body.title || "game").toLowerCase().replace(/[^a-z0-9]/g, "_");
        cb(null, `${title}_${Date.now()}${ext}`);
    }
});

const upload = multer({
    storage,
    limits: { fileSize: 10 * 1024 * 1024 },
    fileFilter: (req, file, cb) => {
        const allowed = /jpeg|jpg|png|webp|gif/;
        const ok = allowed.test(path.extname(file.originalname).toLowerCase())
                && allowed.test(file.mimetype);
        ok ? cb(null, true) : cb(new Error("Only image files are allowed"));
    }
});

app.get("/", (req, res) => {
    res.sendFile(path.join(__dirname, "views/index.html"));
});

app.get("/dashboard", (req, res) => {
    res.sendFile(path.join(__dirname, "views/dashboard.html"));
});

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

app.get("/games", (req, res) => {
    res.json(gameService.getAll());
});

app.post("/games", upload.single("image"), (req, res) => {
    const { title, year, genre, description, ageRating, releaseDate, price } = req.body;
    let platforms;

    try {
        platforms = JSON.parse(req.body.platforms || "[]");
    } catch {
        return res.status(400).json({ success: false, message: "Platforms must be a valid list." });
    }

    if (!req.file) {
        return res.status(400).json({ success: false, message: "Image is required." });
    }

    const imagePath = `/images/${req.file.filename}`;
    const result = gameService.add({
        title,
        year,
        genre,
        description,
        platforms,
        ageRating,
        releaseDate,
        price: Number(price),
        image: imagePath
    });
    res.status(result.success ? 200 : 400).json(result);
});

app.delete("/games/:title", (req, res) => {
    const title = decodeURIComponent(req.params.title);
    const result = gameService.delete(title);
    res.status(result.success ? 200 : 404).json(result);
});

app.listen(PORT, () => {
    console.log(`Server running at http://localhost:${PORT}`);
});
