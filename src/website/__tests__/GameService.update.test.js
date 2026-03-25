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
    const gameService = new GameService(tempFile);

    gameService.add({
        title: "Celeste",
        year: "2018",
        genre: "Platformer",
        image: "/images/celeste.jpg",
        description: "Platformer",
        platforms: ["PC", "Switch"],
        ageRating: "E10+",
        releaseDate: "2018-01-25",
        price: 19.99
    });

    const result = gameService.update("Celeste", { price: 0 });

    expect(gameService.getAll()[0].price).toBe(0);
});
