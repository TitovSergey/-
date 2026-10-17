# -*- coding: utf-8 -*-
"""Сеть Хэмминга распознаёт образ, уже восстановленный сетью Хопфилда.

Цепочка: повреждённая цифра → Хопфилд восстанавливает картинку →
Хэмминг называет её номер.

Первый слой считает близость к каждому из 10 образцов. Образцы и вход
записаны как ±1, веса w_i^k = p_i^k / 2, порог T = n/2. Тогда выход
нейрона равен n минус расстояние Хэмминга: чем выход больше, тем образец ближе.

Второй слой — конкуренция из лекции. У нейрона связь на себя равна +1,
остальные тормозят с весом ε, 0 < ε < 1/m:

    s_j = y_j − ε Σ_{k≠j} y_k
    y_j = s_j, если s_j > 0, иначе 0

Порог не насыщается. В конце активен один выход — номер цифры.
"""

from __future__ import annotations

import numpy as np

from hopfield_digits import (
    N,
    energy,
    erase_center,
    flip_pixels,
    hebb_projection,
    patterns,
    recall,
    side_by_side,
)


class HammingNetwork:
    def __init__(self, samples: np.ndarray) -> None:
        if samples.ndim != 2 or samples.shape[1] != N:
            raise ValueError("ожидались образцы длины 100")
        self.samples = np.where(samples > 0, 1.0, -1.0)
        self.count = self.samples.shape[0]
        self.weights = self.samples.T / 2.0
        self.threshold = N / 2.0
        self.epsilon = 1.0 / (2.0 * self.count)

    def first_layer(self, image: np.ndarray) -> np.ndarray:
        signal = np.where(image > 0, 1.0, -1.0)
        return self.weights.T @ signal + self.threshold

    def compete(self, activations: np.ndarray, limit: int = 100) -> np.ndarray:
        current = activations.astype(float).copy()
        for _ in range(limit):
            total = float(current.sum())
            updated = current - self.epsilon * (total - current)
            updated[updated < 0] = 0.0
            if int(np.sum(updated > 1e-9)) <= 1 or np.allclose(updated, current):
                return updated
            current = updated
        return current

    def recognize(self, image: np.ndarray) -> tuple[int, np.ndarray]:
        outputs = self.compete(self.first_layer(image))
        return int(outputs.argmax()), outputs


def restored_by_hopfield(weights: np.ndarray, image: np.ndarray) -> np.ndarray:
    return recall(weights, image)


def main() -> None:
    stored = patterns()
    hopfield = hebb_projection(stored)
    hamming = HammingNetwork(stored)

    print("Распознавание образов, восстановленных сетью Хопфилда")
    print(f"Хэмминг: {hamming.count} нейронов в каждом слое, ε = {hamming.epsilon:.3f}")

    clean = 0
    for digit, image in enumerate(stored):
        predicted, _ = hamming.recognize(image)
        clean += int(predicted == digit)
    print(f"Эталоны: {clean}/10")

    print("\nФрагмент без центра 4×4 → Хопфилд → номер цифры")
    fragment_ok = 0
    for digit, image in enumerate(stored):
        damaged = erase_center(image)
        restored = restored_by_hopfield(hopfield, damaged)
        predicted, outputs = hamming.recognize(restored)
        fragment_ok += int(predicted == digit)
        print(
            f"  цифра {digit}: ответ {predicted}, "
            f"активный выход {np.round(outputs, 1).tolist()}"
        )
    print(f"итог: {fragment_ok}/10")

    rng = np.random.default_rng(4)
    flips = 8
    trials = 20
    print(f"\nШум из {flips} пикселей → Хопфилд → номер цифры, {trials} попыток")
    noisy_ok = 0
    for digit, image in enumerate(stored):
        hits = 0
        for _ in range(trials):
            restored = restored_by_hopfield(hopfield, flip_pixels(image, flips, rng))
            predicted, _ = hamming.recognize(restored)
            hits += int(predicted == digit)
        noisy_ok += hits
        print(f"  цифра {digit}: {hits}/{trials}")
    print(f"итог: {noisy_ok}/{10 * trials}")

    print("\nПример: у цифры 4 испорчено 8 пикселей")
    damaged = flip_pixels(stored[4], flips, np.random.default_rng(4))
    restored = restored_by_hopfield(hopfield, damaged)
    predicted, outputs = hamming.recognize(restored)
    print(f"энергия {energy(hopfield, damaged):.1f} -> {energy(hopfield, restored):.1f}")
    print(f"Хэмминг выбрал цифру {predicted}")
    print("выходы второго слоя:", np.round(outputs, 2).tolist())
    side_by_side([stored[4], damaged, restored], ["эталон", "шум", "Хопфилд"])


if __name__ == "__main__":
    main()
