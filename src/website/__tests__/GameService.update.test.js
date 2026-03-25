const fs = require("fs");
const os = require("os");
const path = require("path");
const GameService = require("../services/GameService");

let tempFile;

beforeEach(() => {
    tempFile = path.join(os.tmpdir(), `games_update_test_${Date.now()}.json`);
});

afterEach(() => {
    if (fs.existsSync(tempFile)) fs.unlinkSync(tempFile);
});

test("updates an existing game by title", () => {
    const svc = new GameService(tempFile);

    svc.add({
        title: "Celeste",
        year: "2018",
        genre: "Platformer",
        image: "/images/celeste.jpg",
        description: "Precision platformer",
        platforms: ["PC", "Switch"],
        ageRating: "E10+",
        releaseDate: "2018-01-25",
        price: 19.99
    });

    const result = svc.update("Celeste", { price: 0 });

    expect(result.success).toBe(true);
});
