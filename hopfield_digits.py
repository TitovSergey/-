# -*- coding: utf-8 -*-
"""Сеть Хопфилда из 100 нейронов: восстановление цифр 10×10.

Динамика из лекции. Состояние бинарное, на входе оно записывается в выходы.
Нейроны обновляются по очереди:

    s_j = Σ_i w_ij y_i
    y_j = 1, если s_j > 0
    y_j = -1, если s_j < 0
    y_j не меняется, если s_j = 0

Ноль лекции здесь закодирован как -1, единица как +1: иначе внешнее
произведение образцов из нулей и единиц не создаёт минимумов энергии.

Правило Хебба w_ij = Σ_k x_i^k x_j^k при i ≠ j не вмещает все десять цифр.
Лекция сама предупреждает: похожие образы дают перекрёстные ассоциации,
а число образцов должно быть около 0,15 n и меньше, если они коррелированы.
Цифры 3, 5, 6, 8 и 9 отличаются лишь несколькими пикселями.

Поэтому веса строятся проекционным правилом — тем же внешним произведением,
но с учётом взаимного перекрытия образцов:

    W = X (X^T X)^+ X^T,   диагональ обнуляется

Матрица симметрична и с нулями на диагонали. Этого достаточно для
устойчивости: энергия не растёт и сеть останавливается в минимуме.
"""

from __future__ import annotations

import numpy as np

from digit_perceptron import HEIGHT, RAW_DIGITS, WIDTH, render, to_vector


N = WIDTH * HEIGHT


def patterns() -> np.ndarray:
    bits = [to_vector(rows) for rows in RAW_DIGITS]
    return np.array([[1.0 if bit else -1.0 for bit in image] for image in bits])


def hebb_projection(samples: np.ndarray) -> np.ndarray:
    """samples: матрица m×n, каждая строка — образец ±1."""
    matrix = samples.T
    gram = matrix.T @ matrix
    weights = matrix @ np.linalg.pinv(gram) @ matrix.T
    np.fill_diagonal(weights, 0.0)
    return weights


def energy(weights: np.ndarray, state: np.ndarray) -> float:
    """Функция Ляпунова при нулевых внешних входах и порогах."""
    return -0.5 * float(state @ weights @ state)


def recall(weights: np.ndarray, state: np.ndarray, limit: int = 50) -> np.ndarray:
    """Последовательная динамика до неподвижного состояния."""
    current = state.copy()
    for _ in range(limit):
        changed = False
        for neuron in range(N):
            field = float(weights[neuron] @ current)
            if field > 0:
                updated = 1.0
            elif field < 0:
                updated = -1.0
            else:
                updated = current[neuron]
            if updated != current[neuron]:
                current[neuron] = updated
                changed = True
        if not changed:
            return current
    return current


def as_bits(state: np.ndarray) -> list[int]:
    return [1 if value > 0 else 0 for value in state]


def erase_center(state: np.ndarray) -> np.ndarray:
    damaged = state.copy()
    damaged.reshape(HEIGHT, WIDTH)[3:7, 3:7] = -1.0
    return damaged


def flip_pixels(state: np.ndarray, count: int, rng: np.random.Generator) -> np.ndarray:
    damaged = state.copy()
    for index in rng.choice(N, size=count, replace=False):
        damaged[index] *= -1.0
    return damaged


def side_by_side(images: list[np.ndarray], titles: list[str]) -> None:
    print("  ".join(title.ljust(WIDTH) for title in titles))
    rows = [render(as_bits(image)).splitlines() for image in images]
    for line in range(HEIGHT):
        print("  ".join(rows[index][line] for index in range(len(images))))


def main() -> None:
    stored = patterns()
    weights = hebb_projection(stored)
    print("Сеть Хопфилда: 100 нейронов, образы цифр 0–9")
    print("Веса симметричны, диагональ нулевая.")

    clean_ok = 0
    for digit, image in enumerate(stored):
        if np.array_equal(recall(weights, image), image):
            clean_ok += 1
    print(f"Эталоны — неподвижные состояния: {clean_ok}/10")

    print("\nСтерт центр 4×4. Сеть достраивает образ по оставшемуся фрагменту.")
    center_ok = 0
    for digit, image in enumerate(stored):
        damaged = erase_center(image)
        restored = recall(weights, damaged)
        match = bool(np.array_equal(restored, image))
        center_ok += int(match)
        print(f"\nЦифра {digit}: энергия {energy(weights, damaged):.1f} -> {energy(weights, restored):.1f}")
        side_by_side(
            [image, damaged, restored],
            ["эталон", "фрагмент", "итог" if match else "ошибка"],
        )
    print(f"\nФрагменты: {center_ok}/10")

    rng = np.random.default_rng(4)
    flips = 8
    trials = 20
    print(f"\nШум: {flips} случайно инвертированных пикселей, {trials} попыток на цифру")
    noisy_ok = 0
    for digit, image in enumerate(stored):
        hits = 0
        for _ in range(trials):
            restored = recall(weights, flip_pixels(image, flips, rng))
            hits += int(np.array_equal(restored, image))
        noisy_ok += hits
        print(f"  цифра {digit}: {hits}/{trials}")
    print(f"итог: {noisy_ok}/{10 * trials}")

    print("\nОдин пример шума на каждую цифру")
    rng = np.random.default_rng(4)
    for digit, image in enumerate(stored):
        damaged = flip_pixels(image, flips, rng)
        restored = recall(weights, damaged)
        match = bool(np.array_equal(restored, image))
        print(f"\nЦифра {digit}")
        side_by_side(
            [image, damaged, restored],
            ["эталон", "шум", "итог" if match else "ошибка"],
        )


if __name__ == "__main__":
    main()
