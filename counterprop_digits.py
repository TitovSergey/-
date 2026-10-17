# -*- coding: utf-8 -*-
"""Сеть встречного распространения для цифр 0–9 и сравнение с перцептроном.

Слой Кохонена обучается без учителя: победитель — нейрон с максимальным
скалярным произведением, его веса поворачиваются к входу

    w_н = w_с + α (x − w_с)

Слой Гроссберга обучается с учителем и только от ненулевого выхода Кохонена

    v_н = v_с + β (y − v_с) k

Входы нормализуются. Начальные веса Кохонена равны 1/√n, а сами входы
на старте стягиваются к этой же точке и постепенно отпускаются к истинным
значениям — метод выпуклой комбинации. Нейрон, который побеждает чаще
доли 1/M, временно штрафуется: это «чувство справедливости» из лекции.
"""

from __future__ import annotations

import math
import random
import time
from typing import Sequence

from digit_perceptron import (
    N_DIGITS,
    N_INPUTS,
    RAW_DIGITS,
    DigitPerceptron,
    corrupt,
    to_vector,
)


def normalize(vector: Sequence[float]) -> list[float]:
    length = math.sqrt(sum(value * value for value in vector))
    if length == 0:
        return list(vector)
    return [value / length for value in vector]


class CounterPropagation:
    """Прямая сеть: вход → Кохонен (победитель забирает всё) → Гроссберг."""

    def __init__(self, n_inputs: int, n_kohonen: int, n_outputs: int) -> None:
        scale = 1.0 / math.sqrt(n_inputs)
        self.n_inputs = n_inputs
        self.kohonen = [[scale] * n_inputs for _ in range(n_kohonen)]
        self.grossberg = [[0.0] * n_outputs for _ in range(n_kohonen)]
        self.wins = [0 for _ in range(n_kohonen)]
        self.shown = 0

    def _blend(self, image: Sequence[float], factor: float) -> list[float]:
        """Выпуклая комбинация: factor=0 стягивает вход к начальным весам."""
        scale = 1.0 / math.sqrt(self.n_inputs)
        mixed = [factor * value + (1.0 - factor) * scale for value in normalize(image)]
        return normalize(mixed)

    def _scores(self, image: Sequence[float], fair: bool) -> list[float]:
        scores = []
        share = 1.0 / len(self.kohonen)
        for index, weights in enumerate(self.kohonen):
            dot = sum(value * weight for value, weight in zip(image, weights))
            if fair and self.shown:
                excess = self.wins[index] / self.shown - share
                if excess > 0:
                    dot -= excess
            scores.append(dot)
        return scores

    def winner(self, image: Sequence[float], fair: bool = False) -> int:
        scores = self._scores(image, fair)
        return max(range(len(scores)), key=scores.__getitem__)

    def predict(self, image: Sequence[float]) -> int:
        neuron = self.winner(normalize(image))
        outputs = self.grossberg[neuron]
        return max(range(len(outputs)), key=outputs.__getitem__)

    def train(
        self,
        images: Sequence[Sequence[float]],
        targets: Sequence[Sequence[float]],
        epochs: int = 30,
    ) -> int:
        """Возвращает первую эпоху, на которой все обучающие образы узнаны."""
        ready = epochs
        for epoch in range(epochs):
            progress = epoch / max(epochs - 1, 1)
            factor = progress
            alpha = 0.7 * (1.0 - progress) + 0.05
            beta = 0.1 * (1.0 - progress) + 0.01
            order = list(range(len(images)))
            for index in order:
                sample = self._blend(images[index], factor)
                neuron = self.winner(sample, fair=True)
                self.wins[neuron] += 1
                self.shown += 1
                self.kohonen[neuron] = normalize(
                    [
                        weight + alpha * (value - weight)
                        for weight, value in zip(self.kohonen[neuron], sample)
                    ]
                )
                desired = targets[index]
                self.grossberg[neuron] = [
                    weight + beta * (target - weight)
                    for weight, target in zip(self.grossberg[neuron], desired)
                ]
            if ready == epochs and all(
                self.predict(image) == targets[index].index(1.0)
                for index, image in enumerate(images)
            ):
                ready = epoch + 1
        return ready


def one_hot(digit: int) -> list[float]:
    return [1.0 if index == digit else 0.0 for index in range(N_DIGITS)]


def noise_score(recognize, images: list[list[int]], flips: int, trials: int, seed: int) -> int:
    rng = random.Random(seed)
    hits = 0
    for digit, image in enumerate(images):
        for _ in range(trials):
            if recognize(corrupt(image, flips, rng)) == digit:
                hits += 1
    return hits


def main() -> None:
    images = [to_vector(rows) for rows in RAW_DIGITS]
    targets = [one_hot(digit) for digit in range(N_DIGITS)]
    flips = 5
    trials = 20

    started = time.perf_counter()
    counter = CounterPropagation(N_INPUTS, N_DIGITS, N_DIGITS)
    counter_epochs = counter.train(images, targets)
    counter_time = time.perf_counter() - started
    counter_clean = sum(counter.predict(image) == digit for digit, image in enumerate(images))
    counter_noise = noise_score(counter.predict, images, flips, trials, seed=7)

    started = time.perf_counter()
    perceptron = DigitPerceptron(seed=1)
    perceptron_epochs = perceptron.train(images)
    perceptron_time = time.perf_counter() - started
    perceptron_clean = sum(perceptron.recognize(image) == digit for digit, image in enumerate(images))
    perceptron_noise = noise_score(perceptron.recognize, images, flips, trials, seed=7)

    print("Распознавание цифр 0–9, поле 10×10")
    print("\nКакой нейрон Кохонена отвечает за цифру")
    for digit, image in enumerate(images):
        print(f"  {digit} → нейрон {counter.winner(normalize(image))}")

    print("\nСравнение")
    print(f"{'':22} {'встречное':>12} {'перцептрон':>12}")
    print(f"{'эпохи до узнавания':22} {counter_epochs:12} {max(perceptron_epochs):12}")
    print(f"{'время обучения, мс':22} {counter_time * 1000:12.2f} {perceptron_time * 1000:12.2f}")
    print(f"{'эталоны':22} {counter_clean:12}/10 {perceptron_clean:10}/10")
    total = N_DIGITS * trials
    print(f"{'шум, 5 пикселей':22} {counter_noise:12}/{total} {perceptron_noise:9}/{total}")


if __name__ == "__main__":
    main()
