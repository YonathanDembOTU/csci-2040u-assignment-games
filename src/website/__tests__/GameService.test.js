const fs = require("fs");
const path = require("path");
const os = require("os");
const GameService = require("../services/GameService");

let tempFile;
let tempImagesDir;

beforeEach(() => {
    const id = Date.now();
    tempFile = path.join(os.tmpdir(), `games_test_${id}.csv`);
    tempImagesDir = path.join(os.tmpdir(), `images_test_${id}`);
    fs.mkdirSync(tempImagesDir);
});

afterEach(() => {
    if (fs.existsSync(tempFile)) fs.unlinkSync(tempFile);
    fs.readdirSync(tempImagesDir).forEach(f => fs.unlinkSync(path.join(tempImagesDir, f)));
    fs.rmdirSync(tempImagesDir);
});

// constructor
describe("GameService - constructor", () => {
    test("creates CSV with header if file does not exist", () => {
        new GameService(tempFile);
        expect(fs.existsSync(tempFile)).toBe(true);
        expect(fs.readFileSync(tempFile, "utf-8")).toContain("title,year,genre,image");
    });

    test("does not overwrite an existing CSV", () => {
        fs.writeFileSync(tempFile, "title,year,genre,image\r\nCeleste,2018,Platformer,/images/celeste.jpg\r\n");
        new GameService(tempFile);
        expect(fs.readFileSync(tempFile, "utf-8")).toContain("Celeste");
    });
});

// getAll()
describe("GameService - getAll()", () => {
    test("returns empty array when no games exist", () => {
        const svc = new GameService(tempFile);
        expect(svc.getAll()).toEqual([]);
    });

    test("returns all added games", () => {
        const svc = new GameService(tempFile);
        svc.add("Celeste", "2018", "Platformer", "/images/celeste.jpg");
        svc.add("Hades", "2020", "Roguelike", "/images/hades.jpg");
        expect(svc.getAll()).toHaveLength(2);
    });

    test("returns games with all correct fields", () => {
        const svc = new GameService(tempFile);
        svc.add("Celeste", "2018", "Platformer", "/images/celeste.jpg");
        expect(svc.getAll()[0]).toMatchObject({
            title: "Celeste", year: "2018", genre: "Platformer", image: "/images/celeste.jpg"
        });
    });
});

// add()
describe("GameService - add()", () => {
    test("returns success for a valid game", () => {
        const svc = new GameService(tempFile);
        expect(svc.add("Celeste", "2018", "Platformer", "/images/celeste.jpg"))
            .toMatchObject({ success: true, message: "Game added!" });
    });

    test("returns the added game object", () => {
        const svc = new GameService(tempFile);
        const result = svc.add("Celeste", "2018", "Platformer", "/images/celeste.jpg");
        expect(result.game).toMatchObject({ title: "Celeste", year: "2018" });
    });

    test("game is retrievable via getAll() after add", () => {
        const svc = new GameService(tempFile);
        svc.add("Celeste", "2018", "Platformer", "/images/celeste.jpg");
        expect(svc.exists("Celeste")).toBe(true);
    });

    test("fails when title is missing", () => {
        const svc = new GameService(tempFile);
        expect(svc.add("", "2018", "Platformer", "/images/celeste.jpg").success).toBe(false);
    });

    test("fails when year is missing", () => {
        const svc = new GameService(tempFile);
        expect(svc.add("Celeste", "", "Platformer", "/images/celeste.jpg").success).toBe(false);
    });

    test("fails when genre is missing", () => {
        const svc = new GameService(tempFile);
        expect(svc.add("Celeste", "2018", "", "/images/celeste.jpg").success).toBe(false);
    });

    test("fails when imagePath is missing", () => {
        const svc = new GameService(tempFile);
        expect(svc.add("Celeste", "2018", "Platformer", "").success).toBe(false);
    });

    test("strips commas from fields to protect CSV structure", () => {
        const svc = new GameService(tempFile);
        svc.add("Celeste, Director's Cut", "2018", "Platformer", "/images/celeste.jpg");
        expect(fs.readFileSync(tempFile, "utf-8")).toContain("Celeste Director's Cut");
    });

    test("can add multiple games independently", () => {
        const svc = new GameService(tempFile);
        svc.add("Celeste", "2018", "Platformer", "/images/celeste.jpg");
        svc.add("Hades", "2020", "Roguelike", "/images/hades.jpg");
        expect(svc.getAll()).toHaveLength(2);
    });
});

// delete()
describe("GameService - delete()", () => {
    test("returns success when deleting an existing game", () => {
        const svc = new GameService(tempFile);
        svc.add("Celeste", "2018", "Platformer", "/images/celeste.jpg");
        expect(svc.delete("Celeste")).toMatchObject({ success: true, message: "Game deleted." });
    });

    test("game no longer exists after deletion", () => {
        const svc = new GameService(tempFile);
        svc.add("Celeste", "2018", "Platformer", "/images/celeste.jpg");
        svc.delete("Celeste");
        expect(svc.exists("Celeste")).toBe(false);
    });

    test("only removes the targeted game, leaving others intact", () => {
        const svc = new GameService(tempFile);
        svc.add("Celeste", "2018", "Platformer", "/images/celeste.jpg");
        svc.add("Hades", "2020", "Roguelike", "/images/hades.jpg");
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
        svc.add("Celeste", "2018", "Platformer", "/images/celeste.jpg");
        svc.delete("Celeste");
        expect(fs.existsSync(fakeImg)).toBe(false);
    });

    test("does not throw if the image file is already missing", () => {
        const svc = new GameService(tempFile, tempImagesDir);
        svc.add("Celeste", "2018", "Platformer", "/images/celeste.jpg");
        expect(() => svc.delete("Celeste")).not.toThrow();
    });
});

// exists()
describe("GameService - exists()", () => {
    test("returns true for a game that was added", () => {
        const svc = new GameService(tempFile);
        svc.add("Celeste", "2018", "Platformer", "/images/celeste.jpg");
        expect(svc.exists("Celeste")).toBe(true);
    });

    test("returns false for a game that was never added", () => {
        const svc = new GameService(tempFile);
        expect(svc.exists("Celeste")).toBe(false);
    });

    test("returns false for a game that was deleted", () => {
        const svc = new GameService(tempFile);
        svc.add("Celeste", "2018", "Platformer", "/images/celeste.jpg");
        svc.delete("Celeste");
        expect(svc.exists("Celeste")).toBe(false);
    });
});