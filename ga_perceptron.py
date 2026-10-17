# -*- coding: utf-8 -*-
"""Обучение перцептрона генетическим алгоритмом.

Сначала один нейрон, ψ = [Σ w_i x_i > θ], на функциях И, ИЛИ, НЕ.
Затем сеть 2 → 2 → 1 с тем же пороговым правилом: исключающее ИЛИ
линейно неразделимо, поэтому одного нейрона мало. Скрытый слой правилом
Розенблатта не обучается, веса ищет генетический алгоритм.

Хромосома — двоичная запись целых весов и порогов. Отбор элитный,
кроссинговер одноточечный, мутация инвертирует один случайный бит.
Фитнесс — число верных строк таблицы истинности.
"""

from __future__ import annotations

import numpy as np

BITS = 5
POPULATION = 60
GENERATIONS = 250
CROSSOVER = 0.8
MUTATION = 0.3
ELITE = 12


def truth_table(n_inputs: int, function) -> list[tuple[tuple[int, ...], int]]:
    rows = []
    for mask in range(1 << n_inputs):
        inputs = tuple((mask >> shift) & 1 for shift in range(n_inputs - 1, -1, -1))
        rows.append((inputs, function(*inputs)))
    return rows


def decode_weights(chromosome: np.ndarray) -> list[int]:
    """Каждые BITS бит — целое от −16 до 15."""
    weights = []
    for start in range(0, len(chromosome), BITS):
        code = 0
        for bit in chromosome[start : start + BITS]:
            code = (code << 1) | int(bit)
        weights.append(code - 16)
    return weights


def single_correct(chromosome: np.ndarray, samples) -> int:
    weights = decode_weights(chromosome)
    threshold = weights[-1]
    synapses = weights[:-1]
    correct = 0
    for inputs, target in samples:
        total = sum(weight * value for weight, value in zip(synapses, inputs))
        output = 1 if total > threshold else 0
        correct += int(output == target)
    return correct


def layer_correct(chromosome: np.ndarray, samples) -> int:
    """Два скрытых нейрона и один выход, у каждого свои веса и порог."""
    raw = decode_weights(chromosome)
    hidden = [(raw[0:2], raw[2]), (raw[3:5], raw[5])]
    output_weights, output_threshold = raw[6:8], raw[8]
    correct = 0
    for inputs, target in samples:
        signals = []
        for weights, threshold in hidden:
            total = sum(weight * value for weight, value in zip(weights, inputs))
            signals.append(1 if total > threshold else 0)
        total = sum(weight * value for weight, value in zip(output_weights, signals))
        output = 1 if total > output_threshold else 0
        correct += int(output == target)
    return correct


def evolve(score, n_weights: int, samples, seed: int) -> tuple[np.ndarray, int, int]:
    rng = np.random.default_rng(seed)
    length = n_weights * BITS
    population = rng.integers(0, 2, size=(POPULATION, length))
    target = len(samples)
    best = population[0].copy()
    best_score = -1
    for generation in range(1, GENERATIONS + 1):
        scores = np.array([score(chromosome, samples) for chromosome in population])
        winner = int(np.argmax(scores))
        if int(scores[winner]) > best_score:
            best_score = int(scores[winner])
            best = population[winner].copy()
        if best_score == target:
            return best, generation - 1, best_score
        order = np.argsort(scores)[::-1]
        parents = population[order[:ELITE]]
        chosen = rng.choice(ELITE, size=POPULATION, replace=True)
        children = parents[chosen].copy()
        children[0] = population[order[0]]
        order = rng.permutation(POPULATION)
        children = children[order]
        for index in range(0, POPULATION - 1, 2):
            if rng.random() > CROSSOVER:
                continue
            point = int(rng.integers(1, length))
            left = children[index].copy()
            right = children[index + 1].copy()
            children[index, point:] = right[point:]
            children[index + 1, point:] = left[point:]
        for chromosome in children:
            if rng.random() < MUTATION:
                gene = int(rng.integers(0, length))
                chromosome[gene] = 1 - chromosome[gene]
        worst = int(np.argmin([score(chromosome, samples) for chromosome in children]))
        children[worst] = best
        population = children
    return best, GENERATIONS, best_score


def show_table(samples, answers: list[int]) -> None:
    print("  вход -> ответ сети (нужно)")
    for (inputs, target), output in zip(samples, answers):
        mark = "верно" if output == target else "ошибка"
        joined = ", ".join(str(value) for value in inputs)
        print(f"    ({joined}) -> {output} ({target})  {mark}")


def train_single(name: str, n_inputs: int, function) -> None:
    samples = truth_table(n_inputs, function)
    chromosome, generation, score = evolve(single_correct, n_inputs + 1, samples, seed=1)
    weights = decode_weights(chromosome)
    print(f"\n{name}, один нейрон")
    print(f"  веса {weights[:-1]}, порог {weights[-1]}")
    print(f"  поколение {generation}, верных строк {score}/{len(samples)}")
    answers = []
    for inputs, _target in samples:
        total = sum(weight * value for weight, value in zip(weights[:-1], inputs))
        answers.append(1 if total > weights[-1] else 0)
    show_table(samples, answers)


def train_layer(name: str, function) -> None:
    samples = truth_table(2, function)
    chromosome, generation, score = evolve(layer_correct, 9, samples, seed=1)
    weights = decode_weights(chromosome)
    print(f"\n{name}, сеть 2 → 2 → 1")
    print(f"  скрытый 1: веса {weights[0:2]}, порог {weights[2]}")
    print(f"  скрытый 2: веса {weights[3:5]}, порог {weights[5]}")
    print(f"  выход: веса {weights[6:8]}, порог {weights[8]}")
    print(f"  поколение {generation}, верных строк {score}/{len(samples)}")
    answers = []
    hidden = [(weights[0:2], weights[2]), (weights[3:5], weights[5])]
    for inputs, _target in samples:
        signals = []
        for synapses, threshold in hidden:
            total = sum(weight * value for weight, value in zip(synapses, inputs))
            signals.append(1 if total > threshold else 0)
        total = sum(weight * value for weight, value in zip(weights[6:8], signals))
        answers.append(1 if total > weights[8] else 0)
    show_table(samples, answers)


def main() -> None:
    print("Файл: ga_perceptron.py")
    print("Веса ищет генетический алгоритм: элиты, одноточечный кроссинговер, мутация бита.")
    print("\nОднослойный перцептрон")
    train_single("И", 2, lambda x, y: int(x and y))
    train_single("ИЛИ", 2, lambda x, y: int(x or y))
    train_single("НЕ", 1, lambda x: int(not x))
    train_single("исключающее ИЛИ", 2, lambda x, y: int(x != y))
    print("\nМногослойный перцептрон")
    train_layer("исключающее ИЛИ", lambda x, y: int(x != y))
    train_layer("И", lambda x, y: int(x and y))
    train_layer("ИЛИ", lambda x, y: int(x or y))
    train_layer("НЕ", lambda x, y: int(not x))


if __name__ == "__main__":
    main()
