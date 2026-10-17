# -*- coding: utf-8 -*-
"""Многослойный перцептрон для исключающего ИЛИ и простых логических функций.

Однослойный перцептрон исключающее ИЛИ не представляет: точки (0,0) и (1,1)
нельзя отделить прямой от (1,0) и (0,1). Сеть 2 → 2 → 1 это умеет.

Скрытый слой правилом Розенблатта не обучается: лекция прямо говорит, что
теоремы сходимости верны только для последнего слоя. Веса всех слоёв
настраиваются обратным распространением ошибки. На выходе нейрона —
сигмоида, решение принимается по порогу 0,5.
"""

from __future__ import annotations

import math
import random
from typing import Sequence


Sample = tuple[Sequence[float], float]


def sigmoid(value: float) -> float:
    if value >= 0:
        exponent = math.exp(-value)
        return 1.0 / (1.0 + exponent)
    exponent = math.exp(value)
    return exponent / (1.0 + exponent)


class MLP:
    """Полносвязная сеть со сигмоидой в каждом слое и обучением по MSE."""

    def __init__(self, sizes: Sequence[int], seed: int) -> None:
        rng = random.Random(seed)
        self.sizes = list(sizes)
        self.weights: list[list[list[float]]] = []
        self.biases: list[list[float]] = []
        for n_in, n_out in zip(self.sizes, self.sizes[1:]):
            self.weights.append(
                [[rng.uniform(-1.0, 1.0) for _ in range(n_in)] for _ in range(n_out)]
            )
            self.biases.append([rng.uniform(-1.0, 1.0) for _ in range(n_out)])

    def forward(self, inputs: Sequence[float]) -> list[list[float]]:
        activations = [list(inputs)]
        for weights, biases in zip(self.weights, self.biases):
            current = activations[-1]
            activations.append(
                [
                    sigmoid(
                        bias + sum(weight * value for weight, value in zip(row, current))
                    )
                    for row, bias in zip(weights, biases)
                ]
            )
        return activations

    def predict_raw(self, inputs: Sequence[float]) -> float:
        return self.forward(inputs)[-1][0]

    def predict(self, inputs: Sequence[float]) -> int:
        return 1 if self.predict_raw(inputs) >= 0.5 else 0

    def train(
        self,
        samples: Sequence[Sample],
        learning_rate: float = 0.8,
        max_epochs: int = 8000,
        tolerance: float = 0.1,
    ) -> int | None:
        """Возвращает номер эпохи, на которой ответы стали уверенными, либо None."""
        for epoch in range(1, max_epochs + 1):
            for inputs, target in samples:
                activations = self.forward(inputs)
                output = activations[-1][0]
                # dL/dz для L = (a - y)^2 / 2, производная сигмоиды a(1-a).
                deltas = [[(output - target) * output * (1.0 - output)]]
                for layer in range(len(self.weights) - 2, -1, -1):
                    downstream = deltas[0]
                    downstream_weights = self.weights[layer + 1]
                    hidden = activations[layer + 1]
                    deltas.insert(
                        0,
                        [
                            sum(downstream[neuron] * downstream_weights[neuron][index]
                                for neuron in range(len(downstream)))
                            * hidden[index]
                            * (1.0 - hidden[index])
                            for index in range(len(hidden))
                        ],
                    )
                for layer, layer_delta in enumerate(deltas):
                    previous = activations[layer]
                    for neuron, delta in enumerate(layer_delta):
                        for index, value in enumerate(previous):
                            self.weights[layer][neuron][index] -= learning_rate * delta * value
                        self.biases[layer][neuron] -= learning_rate * delta
            if self.confident(samples, tolerance):
                return epoch
        return None

    def confident(self, samples: Sequence[Sample], tolerance: float) -> bool:
        for inputs, target in samples:
            raw = self.predict_raw(inputs)
            if target == 1 and raw < 1.0 - tolerance:
                return False
            if target == 0 and raw > tolerance:
                return False
        return True


def dataset(name: str) -> list[Sample]:
    pairs = [(0.0, 0.0), (0.0, 1.0), (1.0, 0.0), (1.0, 1.0)]
    functions = {
        "исключающее или": lambda x, y: float(x != y),
        "и": lambda x, y: float(x and y),
        "или": lambda x, y: float(x or y),
        # Та же сеть 2→2→1: второй вход на ответ не влияет.
        "не": lambda x, _y: float(not x),
    }
    function = functions[name]
    return [(pair, function(*pair)) for pair in pairs]


def learn(name: str, attempts: int = 40) -> tuple[MLP, int, int]:
    samples = dataset(name)
    for seed in range(1, attempts + 1):
        network = MLP((2, 2, 1), seed)
        epoch = network.train(samples)
        if epoch is not None:
            return network, seed, epoch
    raise RuntimeError(f"сеть не обучилась функции «{name}»")


def show(name: str, network: MLP, seed: int, epoch: int) -> None:
    print(f"\n{name}")
    print(f"  архитектура 2 → 2 → 1, начальные веса: зерно {seed}, эпоха {epoch}")
    print("  (x, y) -> ответ  сырой выход  нужно")
    for inputs, target in dataset(name):
        raw = network.predict_raw(inputs)
        output = network.predict(inputs)
        mark = "верно" if output == int(target) else "ошибка"
        print(f"    ({int(inputs[0])}, {int(inputs[1])}) -> {output}   {raw:.3f}         {int(target)}  {mark}")


def main() -> None:
    print("Одна и та же архитектура: два входа, два скрытых нейрона, один выход.")
    for name in ("исключающее или", "и", "или", "не"):
        network, seed, epoch = learn(name)
        show(name, network, seed, epoch)


if __name__ == "__main__":
    main()
