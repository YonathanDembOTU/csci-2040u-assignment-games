const fs = require("fs");
const path = require("path");
const os = require("os");
const GameService = require("../services/GameService");

let tempFile;
let tempImagesDir;

function buildGame(overrides = {}) {
    return {
        title: "Celeste",
        year: "2018",
        genre: "Platformer",
        image: "/images/celeste.jpg",
        description: "Precision platformer",
        platforms: ["PC", "Switch"],
        ageRating: "E10+",
        releaseDate: "2018-01-25",
        price: 19.99,
        ...overrides
    };
}

beforeEach(() => {
    const id = Date.now();
    tempFile = path.join(os.tmpdir(), `games_test_${id}.json`);
    tempImagesDir = path.join(os.tmpdir(), `images_test_${id}`);
    fs.mkdirSync(tempImagesDir);
});

afterEach(() => {
    if (fs.existsSync(tempFile)) fs.unlinkSync(tempFile);
    fs.readdirSync(tempImagesDir).forEach(f => fs.unlinkSync(path.join(tempImagesDir, f)));
    fs.rmdirSync(tempImagesDir);
});

describe("GameService - constructor", () => {
    test("creates an empty JSON array if file does not exist", () => {
        new GameService(tempFile);
        expect(fs.existsSync(tempFile)).toBe(true);
        expect(JSON.parse(fs.readFileSync(tempFile, "utf-8"))).toEqual([]);
    });

    test("does not overwrite an existing JSON file", () => {
        fs.writeFileSync(tempFile, JSON.stringify([buildGame()], null, 2));
        new GameService(tempFile);
        expect(fs.readFileSync(tempFile, "utf-8")).toContain("Celeste");
    });
});

describe("GameService - getAll()", () => {
    test("returns empty array when no games exist", () => {
        const svc = new GameService(tempFile);
        expect(svc.getAll()).toEqual([]);
    });

    test("returns all added games", () => {
        const svc = new GameService(tempFile);
        svc.add(buildGame());
        svc.add(buildGame({
            title: "Hades",
            year: "2020",
            genre: "Roguelike",
            image: "/images/hades.jpg",
            description: "Escape the underworld",
            platforms: ["PC", "Switch"],
            ageRating: "T",
            releaseDate: "2020-09-17",
            price: 24.99
        }));
        expect(svc.getAll()).toHaveLength(2);
    });

    test("returns games with all correct fields", () => {
        const svc = new GameService(tempFile);
        svc.add(buildGame());
        expect(svc.getAll()[0]).toMatchObject({
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
    });
});

describe("GameService - add()", () => {
    test("returns success for a valid game", () => {
        const svc = new GameService(tempFile);
        expect(svc.add(buildGame()))
            .toMatchObject({ success: true, message: "Game added!" });
    });

    test("returns the added game object", () => {
        const svc = new GameService(tempFile);
        const result = svc.add(buildGame());
        expect(result.game).toMatchObject({ title: "Celeste", year: "2018" });
    });

    test("game is retrievable via getAll() after add", () => {
        const svc = new GameService(tempFile);
        svc.add(buildGame());
        expect(svc.exists("Celeste")).toBe(true);
    });

    test("fails when title is missing", () => {
        const svc = new GameService(tempFile);
        expect(svc.add(buildGame({ title: "" })).success).toBe(false);
    });

    test("fails when year is missing", () => {
        const svc = new GameService(tempFile);
        expect(svc.add(buildGame({ year: "" })).success).toBe(false);
    });

    test("fails when genre is missing", () => {
        const svc = new GameService(tempFile);
        expect(svc.add(buildGame({ genre: "" })).success).toBe(false);
    });

    test("fails when imagePath is missing", () => {
        const svc = new GameService(tempFile);
        expect(svc.add(buildGame({ image: "" })).success).toBe(false);
    });

    test("preserves commas in fields now that JSON is used", () => {
        const svc = new GameService(tempFile);
        svc.add(buildGame({ title: "Celeste, Director's Cut" }));
        expect(svc.getAll()[0].title).toBe("Celeste, Director's Cut");
    });

    test("supports optional metadata fields in the stored game object", () => {
        const svc = new GameService(tempFile);
        const result = svc.add(buildGame({
            developer: "Matt Makes Games"
        }));

        expect(result.success).toBe(true);
        expect(svc.getAll()[0]).toMatchObject({
            title: "Celeste",
            developer: "Matt Makes Games",
            platforms: ["PC", "Switch"],
            description: "Precision platformer"
        });
    });

    test("can add multiple games independently", () => {
        const svc = new GameService(tempFile);
        svc.add(buildGame());
        svc.add(buildGame({
            title: "Hades",
            year: "2020",
            genre: "Roguelike",
            image: "/images/hades.jpg",
            description: "Escape the underworld",
            platforms: ["PC", "Switch"],
            ageRating: "T",
            releaseDate: "2020-09-17",
            price: 24.99
        }));
        expect(svc.getAll()).toHaveLength(2);
    });
});

describe("GameService - delete()", () => {
    test("returns success when deleting an existing game", () => {
        const svc = new GameService(tempFile);
        svc.add(buildGame());
        expect(svc.delete("Celeste")).toMatchObject({ success: true, message: "Game deleted." });
    });

    test("game no longer exists after deletion", () => {
        const svc = new GameService(tempFile);
        svc.add(buildGame());
        svc.delete("Celeste");
        expect(svc.exists("Celeste")).toBe(false);
    });

    test("only removes the targeted game, leaving others intact", () => {
        const svc = new GameService(tempFile);
        svc.add(buildGame());
        svc.add(buildGame({
            title: "Hades",
            year: "2020",
            genre: "Roguelike",
            image: "/images/hades.jpg",
            description: "Escape the underworld",
            platforms: ["PC", "Switch"],
            ageRating: "T",
            releaseDate: "2020-09-17",
            price: 24.99
        }));
        svc.delete("Celeste");
        expect(svc.exists("Hades")).toBe(true);
        expect(svc.getAll()).toHaveLength(1);
    });

    test("fails for a title that does not exist", () => {
        const svc = new GameService(tempFile);
        expect(svc.delete("NonExistent")).toMatchObject({ success: false, message: "Game not found." });
    });

    test("fails when title is empty", () => {
        const svc = new GameService(tempFile);
        expect(svc.delete("").success).toBe(false);
    });

    test("deletes the associated image file when present", () => {
        const svc = new GameService(tempFile, tempImagesDir);
        const fakeImg = path.join(tempImagesDir, "celeste.jpg");
        fs.writeFileSync(fakeImg, "fake");
        svc.add(buildGame());
        svc.delete("Celeste");
        expect(fs.existsSync(fakeImg)).toBe(false);
    });

    test("does not throw if the image file is already missing", () => {
        const svc = new GameService(tempFile, tempImagesDir);
        svc.add(buildGame());
        expect(() => svc.delete("Celeste")).not.toThrow();
    });
});

describe("GameService - exists()", () => {
    test("returns true for a game that was added", () => {
        const svc = new GameService(tempFile);
        svc.add(buildGame());
        expect(svc.exists("Celeste")).toBe(true);
    });

    test("returns false for a game that was never added", () => {
        const svc = new GameService(tempFile);
        expect(svc.exists("Celeste")).toBe(false);
    });

    test("returns false for a game that was deleted", () => {
        const svc = new GameService(tempFile);
        svc.add(buildGame());
        svc.delete("Celeste");
        expect(svc.exists("Celeste")).toBe(false);
    });
});
