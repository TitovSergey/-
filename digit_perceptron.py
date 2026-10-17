# -*- coding: utf-8 -*-
"""Однослойный перцептрон 100 входов (10×10) и 10 выходов (цифры 0–9).

Каждый выход — отдельный нейрон слоя. Он должен дать 1 на своей цифре
и 0 на остальных. Правило обучения то же, что в лекции:

    ψ = [Σ w_i x_i > θ]
    δ = нужный ответ − ответ нейрона
    активные веса меняются на δ, порог — на −δ
"""

from __future__ import annotations

import random
from typing import Sequence

from perceptron_logic import Perceptron


WIDTH = 10
HEIGHT = 10
N_INPUTS = WIDTH * HEIGHT
N_DIGITS = 10

# '#' — чёрный пиксель (1), '.' — фон (0).
RAW_DIGITS = [
    [
        ".########.",
        "#........#",
        "#........#",
        "#........#",
        "#........#",
        "#........#",
        "#........#",
        "#........#",
        "#........#",
        ".########.",
    ],
    [
        "....##....",
        "...###....",
        "....##....",
        "....##....",
        "....##....",
        "....##....",
        "....##....",
        "....##....",
        "....##....",
        "..######..",
    ],
    [
        ".########.",
        ".........#",
        ".........#",
        ".........#",
        ".########.",
        "#.........",
        "#.........",
        "#.........",
        "#.........",
        ".########.",
    ],
    [
        ".########.",
        ".........#",
        ".........#",
        ".........#",
        ".########.",
        ".........#",
        ".........#",
        ".........#",
        ".........#",
        ".########.",
    ],
    [
        "#........#",
        "#........#",
        "#........#",
        "#........#",
        "##########",
        ".........#",
        ".........#",
        ".........#",
        ".........#",
        ".........#",
    ],
    [
        ".########.",
        "#.........",
        "#.........",
        "#.........",
        ".########.",
        ".........#",
        ".........#",
        ".........#",
        ".........#",
        ".########.",
    ],
    [
        ".########.",
        "#.........",
        "#.........",
        "#.........",
        ".########.",
        "#........#",
        "#........#",
        "#........#",
        "#........#",
        ".########.",
    ],
    [
        "##########",
        ".........#",
        "........#.",
        ".......#..",
        "......#...",
        ".....#....",
        ".....#....",
        ".....#....",
        ".....#....",
        ".....#....",
    ],
    [
        ".########.",
        "#........#",
        "#........#",
        "#........#",
        ".########.",
        "#........#",
        "#........#",
        "#........#",
        "#........#",
        ".########.",
    ],
    [
        ".########.",
        "#........#",
        "#........#",
        "#........#",
        ".########.",
        ".........#",
        ".........#",
        ".........#",
        ".........#",
        ".########.",
    ],
]


def to_vector(rows: Sequence[str]) -> list[int]:
    if len(rows) != HEIGHT or any(len(row) != WIDTH or set(row) - {".", "#"} for row in rows):
        raise ValueError("образ цифры должен быть матрицей 10×10 из '.' и '#'")
    return [1 if pixel == "#" else 0 for row in rows for pixel in row]


def render(vector: Sequence[int]) -> str:
    lines = []
    for row in range(HEIGHT):
        start = row * WIDTH
        lines.append("".join("#" if vector[start + col] else "." for col in range(WIDTH)))
    return "\n".join(lines)


class DigitPerceptron:
    """Один слой из 10 нейронов на общем рецепторном поле 10×10."""

    def __init__(self, seed: int = 1) -> None:
        self.neurons = [Perceptron(N_INPUTS, seed=seed + index) for index in range(N_DIGITS)]

    def outputs(self, image: Sequence[int]) -> list[int]:
        return [neuron.predict(image) for neuron in self.neurons]

    def margins(self, image: Sequence[int]) -> list[int]:
        return [neuron.net(image) - neuron.threshold for neuron in self.neurons]

    def recognize(self, image: Sequence[int]) -> int:
        fired = [index for index, bit in enumerate(self.outputs(image)) if bit == 1]
        if len(fired) == 1:
            return fired[0]
        margins = self.margins(image)
        return max(range(N_DIGITS), key=margins.__getitem__)

    def train(self, images: Sequence[Sequence[int]], max_epochs: int = 500) -> list[int]:
        """Каждый нейрон учится отличать свою цифру от остальных."""
        epochs = []
        for digit, neuron in enumerate(self.neurons):
            samples = [(image, int(label == digit)) for label, image in enumerate(images)]
            epochs.append(neuron.train(samples, max_epochs=max_epochs))
        return epochs


def corrupt(image: Sequence[int], flips: int, rng: random.Random) -> list[int]:
    noisy = list(image)
    for index in rng.sample(range(N_INPUTS), flips):
        noisy[index] = 1 - noisy[index]
    return noisy


def main() -> None:
    images = [to_vector(rows) for rows in RAW_DIGITS]
    network = DigitPerceptron(seed=1)
    epochs = network.train(images)

    print("Образы цифр 10×10")
    for digit, image in enumerate(images):
        print(f"\n{digit}")
        print(render(image))

    print("\nОбучение")
    print("нейронов:", N_DIGITS, "входов:", N_INPUTS)
    print("эпох по выходам:", epochs)

    print("\nРаспознавание обучающих образов")
    correct = 0
    for digit, image in enumerate(images):
        bits = network.outputs(image)
        predicted = network.recognize(image)
        ok = predicted == digit and bits == [int(index == digit) for index in range(N_DIGITS)]
        correct += int(ok)
        print(f"  цифра {digit}: выходы {bits} -> {predicted}  {'верно' if ok else 'ошибка'}")
    print(f"итог: {correct}/{N_DIGITS}")

    rng = random.Random(7)
    trials = 20
    flips = 5
    noisy_ok = 0
    print(f"\nПроверка на образах с {flips} инвертированными пикселями ({trials} на цифру)")
    for digit, image in enumerate(images):
        hits = 0
        for _ in range(trials):
            if network.recognize(corrupt(image, flips, rng)) == digit:
                hits += 1
        noisy_ok += hits
        print(f"  цифра {digit}: {hits}/{trials}")
    print(f"итог: {noisy_ok}/{N_DIGITS * trials}")


if __name__ == "__main__":
    main()
