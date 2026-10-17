# -*- coding: utf-8 -*-
"""Биоценоз: растения, травоядные, хищники.

Энергия идёт по цепи и на каждом переходе часть теряется.

    свет → растения → травоядные → хищники
                         \\            \\
                          гибель возвращает долю энергии растениям

Растения накапливают биомассу фотосинтезом. Травоядное съедает биомассу
клетки и получает только часть её энергии. Хищник съедает травоядное и
тоже усваивает не всё. Движение и основной обмен списывают энергию каждый
такт. Излишек выше предела сбрасывается. При гибели без поедания часть
энергии становится биомассой растений в той же клетке.

Активностью животного управляет однослойный перцептрон: отдельный нейрон
на каждое действие, ψ = [Σ w_i x_i > θ]. Если порог превысили несколько
нейронов, поедание важнее хода, иначе берётся ход с наибольшим
превышением. Потомок наследует веса с небольшим разбросом.

Запуск окна:  python biocenosis.py
Проверка модели без окна:  python biocenosis.py --check
"""

from __future__ import annotations

import random
import sys
import tkinter as tk
from tkinter import ttk

WIDTH = 46
HEIGHT = 32
CELL = 14

PHOTOSYNTHESIS = 0.28
PLANT_CAP = 8.0

HERBIVORES = 36
PREDATORS = 4
HERB_CAP = 140
PRED_CAP = 14

HERB_BITE = 1.8
HERB_EFFICIENCY = 0.62
PRED_EFFICIENCY = 0.4
HERB_BASAL = 0.08
HERB_MOVE = 0.05
PRED_BASAL = 0.2
PRED_MOVE = 0.1
HERB_START = 10.0
PRED_START = 12.0
HERB_MAX = 18.0
PRED_MAX = 22.0
HERB_BIRTH = 15.0
PRED_BIRTH = 20.0
HERB_GIFT = 5.5
PRED_GIFT = 8.0
BIRTH_PAUSE = 18
RETURNED = 0.35

SENSE = 4
ACTIONS = ("север", "восток", "юг", "запад", "есть", "ждать")
MOVES = ((0, -1), (1, 0), (0, 1), (-1, 0))


def clip(value: float, low: float, high: float) -> float:
    return low if value < low else high if value > high else value


class Perceptron:
    """Один слой нейронов: строка весов и порог на каждое действие."""

    def __init__(self, weights: list[list[float]], thresholds: list[float]) -> None:
        self.weights = [row[:] for row in weights]
        self.thresholds = thresholds[:]

    def excite(self, inputs: list[float]) -> list[tuple[float, int]]:
        fired = []
        for index, (row, threshold) in enumerate(zip(self.weights, self.thresholds)):
            net = sum(weight * value for weight, value in zip(row, inputs))
            if net > threshold:
                fired.append((net - threshold, index))
        fired.sort(reverse=True)
        return fired

    def spawn(self, rng: random.Random) -> Perceptron:
        weights = [
            [clip(weight + rng.gauss(0.0, 0.08), -4.0, 4.0) for weight in row]
            for row in self.weights
        ]
        thresholds = [clip(threshold + rng.gauss(0.0, 0.05), -1.0, 3.0) for threshold in self.thresholds]
        return Perceptron(weights, thresholds)


def species_brain(kind: str) -> Perceptron:
    # Входы: энергия, растения здесь, добыча здесь, угроза здесь,
    # еда восточнее, еда южнее, угроза восточнее, угроза южнее.
    if kind == "herbivore":
        weights = [
            [0, 0, 0, 0, 0, -2.5, 0, 1.8],
            [0, 0, 0, 0, 2.5, 0, -1.8, 0],
            [0, 0, 0, 0, 0, 2.5, 0, -1.8],
            [0, 0, 0, 0, -2.5, 0, 1.8, 0],
            [0, 0, 3.4, -1.2, 0, 0, 0, 0],
            [1.4, -1.6, -1.2, -2.0, 0, 0, 0, 0],
        ]
        thresholds = [0.25, 0.25, 0.25, 0.25, 0.55, 1.05]
    else:
        weights = [
            [0, 0, 0, 0, 0, -2.7, 0, 0.6],
            [0, 0, 0, 0, 2.7, 0, -0.6, 0],
            [0, 0, 0, 0, 0, 2.7, 0, -0.6],
            [0, 0, 0, 0, -2.7, 0, 0.6, 0],
            [-0.4, 0, 3.6, 0, 0, 0, 0, 0],
            [1.3, 0, -2.2, -0.4, 0, 0, 0, 0],
        ]
        thresholds = [0.2, 0.2, 0.2, 0.2, 0.5, 1.15]
    return Perceptron(weights, thresholds)


class Animal:
    def __init__(self, kind: str, x: int, y: int, energy: float, brain: Perceptron) -> None:
        self.kind = kind
        self.x = x
        self.y = y
        self.energy = energy
        self.brain = brain
        self.pause = 0
        self.alive = True
        self.action = "ждать"
        self.item: int | None = None

    @property
    def limit(self) -> float:
        return HERB_MAX if self.kind == "herbivore" else PRED_MAX

    @property
    def basal(self) -> float:
        return HERB_BASAL if self.kind == "herbivore" else PRED_BASAL

    @property
    def move_cost(self) -> float:
        return HERB_MOVE if self.kind == "herbivore" else PRED_MOVE


class World:
    def __init__(self, seed: int = 1) -> None:
        self.rng = random.Random(seed)
        self.plants = [[0.0 for _ in range(WIDTH)] for _ in range(HEIGHT)]
        self.animals: list[Animal] = []
        self.herds = [[0 for _ in range(WIDTH)] for _ in range(HEIGHT)]
        self.packs = [[0 for _ in range(WIDTH)] for _ in range(HEIGHT)]
        self.tick = 0
        self.born = {"herbivore": 0, "predator": 0}
        self.died = {"herbivore": 0, "predator": 0}
        self.eaten_plants = 0.0
        self.eaten_herbivores = 0
        self.reset()

    def reset(self) -> None:
        self.tick = 0
        self.born = {"herbivore": 0, "predator": 0}
        self.died = {"herbivore": 0, "predator": 0}
        self.eaten_plants = 0.0
        self.eaten_herbivores = 0
        self.animals.clear()
        for row in self.herds:
            for x in range(WIDTH):
                row[x] = 0
        for row in self.packs:
            for x in range(WIDTH):
                row[x] = 0
        for y in range(HEIGHT):
            for x in range(WIDTH):
                self.plants[y][x] = self.rng.uniform(1.5, 5.5) if self.rng.random() < 0.85 else 0.0
        for _ in range(HERBIVORES):
            self._place("herbivore", HERB_START)
        for _ in range(PREDATORS):
            self._place("predator", PRED_START)

    def _place(self, kind: str, energy: float) -> None:
        brain = species_brain(kind).spawn(self.rng)
        animal = Animal(kind, self.rng.randrange(WIDTH), self.rng.randrange(HEIGHT), energy, brain)
        self.animals.append(animal)
        self._enter(animal)

    def wrap(self, x: int, y: int) -> tuple[int, int]:
        return x % WIDTH, y % HEIGHT

    def grow(self) -> None:
        for y in range(HEIGHT):
            row = self.plants[y]
            for x in range(WIDTH):
                if row[x] < PLANT_CAP:
                    row[x] = min(PLANT_CAP, row[x] + PHOTOSYNTHESIS)

    def _gradient(self, x: int, y: int, read) -> tuple[float, float]:
        east = west = south = north = 0.0
        for step in range(1, SENSE + 1):
            weight = SENSE + 1 - step
            east += weight * read(*self.wrap(x + step, y))
            west += weight * read(*self.wrap(x - step, y))
            south += weight * read(*self.wrap(x, y + step))
            north += weight * read(*self.wrap(x, y - step))
        horizontal = east + west
        vertical = south + north
        gx = 0.0 if horizontal == 0 else (east - west) / horizontal
        gy = 0.0 if vertical == 0 else (south - north) / vertical
        return gx, gy

    def sense(self, animal: Animal) -> list[float]:
        x, y = animal.x, animal.y
        plants_here = self.plants[y][x] / PLANT_CAP
        prey_here = 0.0
        threat_here = 0.0
        for other in self.animals:
            if not other.alive or other is animal:
                continue
            dx = min(abs(other.x - x), WIDTH - abs(other.x - x))
            dy = min(abs(other.y - y), HEIGHT - abs(other.y - y))
            if max(dx, dy) > 1:
                continue
            if animal.kind == "herbivore" and other.kind == "predator":
                threat_here = 1.0
            elif animal.kind == "predator" and other.kind == "herbivore":
                prey_here = 1.0
            elif animal.kind == "predator" and other.kind == "predator":
                threat_here = max(threat_here, 0.4)
        if animal.kind == "herbivore":
            prey_here = 1.0 if self.plants[y][x] >= 0.4 else plants_here
            food = lambda cx, cy: self.plants[cy][cx]
        else:
            food = lambda cx, cy: 1.0 if self._occupied(cx, cy, "herbivore") else 0.0
        threat = lambda cx, cy: 1.0 if self._occupied(cx, cy, "predator") else 0.0
        food_x, food_y = self._gradient(x, y, food)
        threat_x, threat_y = self._gradient(x, y, threat)
        return [
            animal.energy / animal.limit,
            plants_here,
            prey_here,
            threat_here,
            food_x,
            food_y,
            threat_x,
            threat_y,
        ]

    def _grid(self, animal: Animal) -> list[list[int]]:
        return self.herds if animal.kind == "herbivore" else self.packs

    def _enter(self, animal: Animal) -> None:
        self._grid(animal)[animal.y][animal.x] += 1

    def _leave(self, animal: Animal) -> None:
        self._grid(animal)[animal.y][animal.x] -= 1

    def _occupied(self, x: int, y: int, kind: str) -> bool:
        grid = self.herds if kind == "herbivore" else self.packs
        return grid[y][x] > 0

    def _herbivore_at(self, x: int, y: int) -> Animal | None:
        if self.herds[y][x] == 0:
            return None
        for animal in self.animals:
            if animal.alive and animal.kind == "herbivore" and animal.x == x and animal.y == y:
                return animal
        return None

    def act(self, animal: Animal, births: list[Animal]) -> None:
        if animal.pause > 0:
            animal.pause -= 1
        inputs = self.sense(animal)
        fired = animal.brain.excite(inputs)
        action = "ждать"
        eat = next((item for item in fired if item[1] == 4), None)
        moves = [item for item in fired if item[1] < 4]
        if eat is not None and self._can_eat(animal):
            action = "есть"
        elif moves:
            action = ACTIONS[moves[0][1]]
        animal.action = action
        if action in ACTIONS[:4]:
            dx, dy = MOVES[ACTIONS.index(action)]
            self._leave(animal)
            animal.x, animal.y = self.wrap(animal.x + dx, animal.y + dy)
            self._enter(animal)
            animal.energy -= animal.move_cost
        elif action == "есть":
            self._eat(animal)
        animal.energy -= animal.basal
        if animal.energy > animal.limit:
            animal.energy = animal.limit
        if animal.energy <= 0:
            self._die(animal, returned=True)
            return
        self._maybe_born(animal, births)

    def _can_eat(self, animal: Animal) -> bool:
        if animal.kind == "herbivore":
            return self.plants[animal.y][animal.x] >= 0.4
        return self._herbivore_at(animal.x, animal.y) is not None

    def _eat(self, animal: Animal) -> None:
        if animal.kind == "herbivore":
            bite = min(HERB_BITE, self.plants[animal.y][animal.x])
            self.plants[animal.y][animal.x] -= bite
            animal.energy += bite * HERB_EFFICIENCY
            self.eaten_plants += bite
            return
        prey = self._herbivore_at(animal.x, animal.y)
        if prey is None:
            return
        animal.energy += max(prey.energy, 0.0) * PRED_EFFICIENCY
        self.eaten_herbivores += 1
        self._die(prey, returned=False)

    def _die(self, animal: Animal, returned: bool) -> None:
        if not animal.alive:
            return
        animal.alive = False
        self._leave(animal)
        self.died[animal.kind] += 1
        if returned and animal.energy > 0:
            self.plants[animal.y][animal.x] = min(
                PLANT_CAP, self.plants[animal.y][animal.x] + animal.energy * RETURNED
            )
        animal.energy = 0.0

    def _maybe_born(self, animal: Animal, births: list[Animal]) -> None:
        if animal.pause > 0:
            return
        gift = HERB_GIFT if animal.kind == "herbivore" else PRED_GIFT
        need = HERB_BIRTH if animal.kind == "herbivore" else PRED_BIRTH
        cap = HERB_CAP if animal.kind == "herbivore" else PRED_CAP
        alive = sum(other.kind == animal.kind and other.alive for other in self.animals)
        if animal.energy < need or alive + len(births) >= cap:
            return
        animal.energy -= gift
        animal.pause = BIRTH_PAUSE
        child = Animal(
            animal.kind,
            *self.wrap(animal.x + self.rng.choice((-1, 0, 1)), animal.y + self.rng.choice((-1, 0, 1))),
            gift,
            animal.brain.spawn(self.rng),
        )
        child.pause = BIRTH_PAUSE
        births.append(child)
        self._enter(child)
        self.born[animal.kind] += 1

    def step(self) -> None:
        self.grow()
        order = self.animals[:]
        self.rng.shuffle(order)
        births: list[Animal] = []
        for animal in order:
            if animal.alive:
                self.act(animal, births)
        self.animals = [animal for animal in self.animals if animal.alive]
        self.animals.extend(births)
        self.tick += 1

    def counts(self) -> tuple[int, int, float]:
        herbs = preds = 0
        for animal in self.animals:
            if animal.kind == "herbivore":
                herbs += 1
            else:
                preds += 1
        biomass = sum(sum(row) for row in self.plants)
        return herbs, preds, biomass


def check(steps: int = 500) -> None:
    world = World(seed=1)
    print("Файл: biocenosis.py")
    print("такты  растения  травоядные  хищники")
    for moment in range(0, steps + 1, 50):
        while world.tick < moment:
            world.step()
        herbs, preds, biomass = world.counts()
        print(f"{world.tick:5}  {biomass:8.0f}  {herbs:11}  {preds:7}")
    print(
        f"съедено биомассы {world.eaten_plants:.0f}, "
        f"съедено травоядных {world.eaten_herbivores}, "
        f"родилось {world.born['herbivore']}/{world.born['predator']}, "
        f"погибло {world.died['herbivore']}/{world.died['predator']}"
    )


class App(tk.Tk):
    def __init__(self) -> None:
        super().__init__()
        self.title("Биоценоз — biocenosis.py")
        self.configure(bg="#101612")
        self.world = World(seed=1)
        self.playing = True
        self.delay = 60
        self.history: list[tuple[int, int, float]] = []
        self.selected: Animal | None = None
        self._build()
        self._draw_plants()
        self._sync_animals()
        self.after(self.delay, self._tick)

    def _build(self) -> None:
        style = ttk.Style(self)
        style.theme_use("clam")
        style.configure("Dark.TFrame", background="#17211b")
        style.configure("Dark.TLabel", background="#17211b", foreground="#e7f0e4", font=("Segoe UI", 10))
        style.configure("Title.TLabel", background="#17211b", foreground="#f3f7f1", font=("Segoe UI", 14, "bold"))
        style.configure("Muted.TLabel", background="#17211b", foreground="#9aab9c", font=("Segoe UI", 9))
        style.configure("Run.TButton", font=("Segoe UI", 10))

        board = tk.Frame(self, bg="#101612")
        board.pack(side=tk.LEFT, padx=12, pady=12)
        self.canvas = tk.Canvas(
            board,
            width=WIDTH * CELL,
            height=HEIGHT * CELL,
            bg="#122016",
            highlightthickness=0,
        )
        self.canvas.pack()
        self.canvas.bind("<Button-1>", self._select)

        panel = ttk.Frame(self, style="Dark.TFrame", padding=14)
        panel.pack(side=tk.RIGHT, fill=tk.BOTH, expand=True)
        ttk.Label(panel, text="Растения — травоядные — хищники", style="Title.TLabel").pack(anchor=tk.W)
        ttk.Label(
            panel,
            text="Свет кормит растения. Животные решают,\nкуда идти и когда есть, однослойным\nперцептроном. Часть энергии теряется.",
            style="Muted.TLabel",
        ).pack(anchor=tk.W, pady=(4, 10))

        self.stat = ttk.Label(panel, text="", style="Dark.TLabel", justify=tk.LEFT)
        self.stat.pack(anchor=tk.W)
        self.plot = tk.Canvas(panel, width=250, height=92, bg="#101612", highlightthickness=0)
        self.plot.pack(anchor=tk.W, pady=10)

        buttons = ttk.Frame(panel, style="Dark.TFrame")
        buttons.pack(anchor=tk.W, pady=(0, 8))
        ttk.Button(buttons, text="Старт", style="Run.TButton", command=self._play).pack(side=tk.LEFT, padx=(0, 6))
        ttk.Button(buttons, text="Пауза", style="Run.TButton", command=self._pause).pack(side=tk.LEFT, padx=(0, 6))
        ttk.Button(buttons, text="Шаг", style="Run.TButton", command=self._once).pack(side=tk.LEFT, padx=(0, 6))
        ttk.Button(buttons, text="Сброс", style="Run.TButton", command=self._reset).pack(side=tk.LEFT)

        speed = ttk.Frame(panel, style="Dark.TFrame")
        speed.pack(anchor=tk.W, fill=tk.X)
        ttk.Label(speed, text="Скорость", style="Muted.TLabel").pack(side=tk.LEFT)
        self.speed = tk.Scale(
            speed,
            from_=1,
            to=8,
            orient=tk.HORIZONTAL,
            showvalue=False,
            bg="#17211b",
            fg="#e7f0e4",
            highlightthickness=0,
            troughcolor="#2b3b30",
            length=150,
        )
        self.speed.set(2)
        self.speed.pack(side=tk.LEFT, padx=8)

        ttk.Label(
            panel,
            text="Жёлтые — травоядные, красные — хищники.\nЯркость клетки — биомасса растений.\nЩелчок по животному показывает решение\nего перцептрона.",
            style="Muted.TLabel",
        ).pack(anchor=tk.W, pady=(8, 4))
        self.detail = ttk.Label(panel, text="Животное не выбрано.", style="Dark.TLabel", justify=tk.LEFT)
        self.detail.pack(anchor=tk.W, pady=(6, 0))

        self.plant_items = [
            [
                self.canvas.create_rectangle(
                    x * CELL, y * CELL, (x + 1) * CELL, (y + 1) * CELL, width=0
                )
                for x in range(WIDTH)
            ]
            for y in range(HEIGHT)
        ]

    def _plant_color(self, mass: float) -> str:
        ratio = clip(mass / PLANT_CAP, 0.0, 1.0)
        red = int(16 + 120 * ratio)
        green = int(36 + 190 * ratio)
        blue = int(24 + 40 * ratio)
        return f"#{red:02x}{green:02x}{blue:02x}"

    def _draw_plants(self) -> None:
        for y in range(HEIGHT):
            for x in range(WIDTH):
                self.canvas.itemconfig(self.plant_items[y][x], fill=self._plant_color(self.world.plants[y][x]))

    def _sync_animals(self) -> None:
        for animal in self.world.animals:
            color = "#e6c15a" if animal.kind == "herbivore" else "#e25b4a"
            radius = 4 if animal.kind == "herbivore" else 5
            cx = animal.x * CELL + CELL // 2
            cy = animal.y * CELL + CELL // 2
            if animal.item is None:
                animal.item = self.canvas.create_oval(
                    cx - radius,
                    cy - radius,
                    cx + radius,
                    cy + radius,
                    fill=color,
                    outline="#101612",
                    tags="animal",
                )
            else:
                self.canvas.coords(animal.item, cx - radius, cy - radius, cx + radius, cy + radius)
        living = {animal.item for animal in self.world.animals}
        for item in self.canvas.find_withtag("animal"):
            if item not in living:
                self.canvas.delete(item)
        if self.selected is not None and self.selected not in self.world.animals:
            self.selected = None

    def _stats(self) -> None:
        herbs, preds, biomass = self.world.counts()
        self.history.append((herbs, preds, biomass))
        if len(self.history) > 180:
            self.history.pop(0)
        self.stat.config(
            text=(
                f"Такт {self.world.tick}\n"
                f"Растения, биомасса {biomass:.0f}\n"
                f"Травоядные {herbs}\n"
                f"Хищники {preds}\n"
                f"Съедено растений {self.world.eaten_plants:.0f}\n"
                f"Съедено травоядных {self.world.eaten_herbivores}"
            )
        )
        self._plot()
        if self.selected is None or not self.selected.alive:
            self.detail.config(text="Животное не выбрано.")
        else:
            name = "Травоядное" if self.selected.kind == "herbivore" else "Хищник"
            self.detail.config(
                text=(
                    f"{name}\n"
                    f"Энергия {self.selected.energy:.1f}\n"
                    f"Действие: {self.selected.action}"
                )
            )

    def _plot(self) -> None:
        self.plot.delete("all")
        if len(self.history) < 2:
            return
        width, height = 250, 92
        herbs = [item[0] for item in self.history]
        preds = [item[1] for item in self.history]
        plants = [item[2] for item in self.history]
        self._line(plants, max(plants) or 1, "#7dce63", width, height)
        self._line(herbs, max(max(herbs), 1), "#e6c15a", width, height)
        self._line(preds, max(max(preds), 1), "#e25b4a", width, height)

    def _line(self, values: list[float], scale: float, color: str, width: int, height: int) -> None:
        step = (width - 8) / max(len(self.history) - 1, 1)
        points = []
        for index, value in enumerate(values):
            points.extend((4 + index * step, height - 6 - (value / scale) * (height - 16)))
        if len(points) >= 4:
            self.plot.create_line(*points, fill=color, width=2)

    def _select(self, event: tk.Event) -> None:
        x = event.x // CELL
        y = event.y // CELL
        found = None
        for animal in self.world.animals:
            if animal.x == x and animal.y == y:
                found = animal
                break
        self.selected = found
        self._stats()

    def _play(self) -> None:
        self.playing = True

    def _pause(self) -> None:
        self.playing = False

    def _once(self) -> None:
        self.playing = False
        self.world.step()
        self._refresh()

    def _reset(self) -> None:
        for animal in self.world.animals:
            if animal.item is not None:
                self.canvas.delete(animal.item)
        self.world = World(seed=1)
        self.history.clear()
        self.selected = None
        self.playing = True
        self._refresh()

    def _refresh(self) -> None:
        self._draw_plants()
        self._sync_animals()
        self._stats()

    def _tick(self) -> None:
        if self.playing:
            for _ in range(int(self.speed.get())):
                self.world.step()
            self._refresh()
        self.after(self.delay, self._tick)


def main() -> None:
    if "--check" in sys.argv:
        check()
        return
    app = App()
    app.mainloop()


if __name__ == "__main__":
    main()
