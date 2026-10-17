# -*- coding: utf-8 -*-
"""Однослойный перцептрон и обучение функциям И, ИЛИ, НЕ.

Модель из лекции:
    ψ = [Σ w_i x_i > θ]
Ложь = 0, истина = 1. Веса и порог целые, шаг обучения равен 1.

Правила (Розенблатт):
    ошибка δ = правильный ответ − ответ сети
    если вход активен (x_i = 1), вес меняется на δ
    порог меняется на −δ
Обучение с учителем. Эпоха — один проход по всей выборке.
"""

from __future__ import annotations

import random
from typing import Iterable, Sequence


Sample = tuple[Sequence[int], int]


class Perceptron:
    """Один нейрон: взвешенная сумма входов и порог."""

    def __init__(self, n_inputs: int, seed: int | None = None) -> None:
        rng = random.Random(seed)
        self.weights = [rng.choice((-1, 0, 1)) for _ in range(n_inputs)]
        self.threshold = rng.choice((-1, 0, 1))

    def net(self, inputs: Sequence[int]) -> int:
        return sum(weight * value for weight, value in zip(self.weights, inputs))

    def predict(self, inputs: Sequence[int]) -> int:
        return 1 if self.net(inputs) > self.threshold else 0

    def train(self, samples: Iterable[Sample], max_epochs: int = 100) -> int:
        """Подстройка весов. Возвращает число эпох до полной сходимости."""
        data = list(samples)
        for epoch in range(1, max_epochs + 1):
            errors = 0
            for inputs, target in data:
                output = self.predict(inputs)
                delta = target - output
                if delta == 0:
                    continue
                errors += 1
                for index, value in enumerate(inputs):
                    if value == 1:
                        self.weights[index] += delta
                self.threshold -= delta
            if errors == 0:
                return epoch - 1
        raise RuntimeError(
            "за отведённое число эпох перцептрон не обучился: "
            "функция, скорее всего, линейно неразделима"
        )


def truth_table(n_inputs: int, function) -> list[Sample]:
    rows = []
    total = 1 << n_inputs
    for mask in range(total):
        inputs = [(mask >> shift) & 1 for shift in range(n_inputs - 1, -1, -1)]
        rows.append((inputs, function(*inputs)))
    return rows


def show(name: str, perceptron: Perceptron, samples: list[Sample], epochs: int) -> None:
    print(f"\n{name}")
    print(f"  веса {perceptron.weights}, порог {perceptron.threshold}")
    print(f"  эпох до сходимости: {epochs}")
    print("  вход -> ответ сети (нужно)")
    for inputs, target in samples:
        output = perceptron.predict(inputs)
        mark = "верно" if output == target else "ошибка"
        joined = ", ".join(str(value) for value in inputs)
        print(f"    ({joined}) -> {output} ({target})  {mark}")


def main() -> None:
    tasks = [
        ("И", 2, lambda x, y: int(x and y)),
        ("ИЛИ", 2, lambda x, y: int(x or y)),
        ("НЕ", 1, lambda x: int(not x)),
    ]
    for name, n_inputs, function in tasks:
        samples = truth_table(n_inputs, function)
        perceptron = Perceptron(n_inputs, seed=1)
        epochs = perceptron.train(samples)
        show(name, perceptron, samples, epochs)


if __name__ == "__main__":
    main()
