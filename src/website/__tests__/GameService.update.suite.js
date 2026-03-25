const fs = require("fs");
const os = require("os");
const path = require("path");
const GameService = require("../services/GameService");

let tempFile;

function buildGame(overrides = {}) {
    return {
        title: "Celeste",
        year: "2018",
        genre: "Platformer",
        image: "/images/celeste.jpg",
        description: "Platformer",
        platforms: ["PC", "Switch"],
        ageRating: "E10+",
        releaseDate: "2018-01-25",
        price: 19.99,
        ...overrides
    };
}

beforeEach(() => {
    tempFile = path.join(os.tmpdir(), `games_update_suite_${Date.now()}.json`);
});

afterEach(() => {
    if (fs.existsSync(tempFile)) {
        fs.unlinkSync(tempFile);
    }
});

describe("GameService - update() test suite", () => {
    test("updates an existing game by title", () => {
        const svc = new GameService(tempFile);
        svc.add(buildGame());

        const result = svc.update("Celeste", { price: 0 });

        expect(result).toMatchObject({ success: true });
        expect(svc.getAll()[0].price).toBe(0);
    });

    test("updates only the provided fields", () => {
        const svc = new GameService(tempFile);
        svc.add(buildGame());

        svc.update("Celeste", { description: "Updated description" });

        expect(svc.getAll()[0]).toMatchObject({
            title: "Celeste",
            price: 19.99,
            description: "Updated description"
        });
    });

    test("returns failure when the title does not exist", () => {
        const svc = new GameService(tempFile);
        svc.add(buildGame());

        const result = svc.update("Hades", { price: 24.99 });

        expect(result).toMatchObject({ success: false });
    });

    test("returns failure when the title is empty", () => {
        const svc = new GameService(tempFile);
        svc.add(buildGame());

        const result = svc.update("", { price: 24.99 });

        expect(result).toMatchObject({ success: false });
    });

    test("allows updating price to zero", () => {
        const svc = new GameService(tempFile);
        svc.add(buildGame());

        svc.update("Celeste", { price: 0 });

        expect(svc.getAll()[0].price).toBe(0);
    });
});
