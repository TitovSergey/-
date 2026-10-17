# -*- coding: utf-8 -*-
"""Простой генетический алгоритм для f(x, y) = 1 / (1 + x^2 + y^2).

Максимум равен 1 и достигается в точке (0, 0). Хромосома — два двоичных
кода, по одному на x и y, как в п. 1.3 лекции. Сравниваются два оператора
репродукции: колесо рулетки и элитный отбор.
"""

from __future__ import annotations

import time

import numpy as np

LOW = -10.0
HIGH = 10.0
BITS = 15
GENES = BITS * 2
POPULATION = 40
GENERATIONS = 80
CROSSOVER = 0.8
MUTATION = 0.02
ELITE = 10
TARGET = 0.99
TRIALS = 20


def fitness_of(x: float, y: float) -> float:
    return 1.0 / (1.0 + x * x + y * y)


def decode(chromosome: np.ndarray) -> tuple[float, float]:
    """Двоичный номер отрезка переводится в число из [LOW, HIGH]."""
    span = HIGH - LOW
    scale = span / (2**BITS - 1)
    values = []
    for start in (0, BITS):
        code = 0
        for bit in chromosome[start : start + BITS]:
            code = (code << 1) | int(bit)
        values.append(LOW + code * scale)
    return values[0], values[1]


def fitness(population: np.ndarray) -> np.ndarray:
    scores = np.empty(len(population))
    for index, chromosome in enumerate(population):
        x, y = decode(chromosome)
        scores[index] = fitness_of(x, y)
    return scores


def roulette(population: np.ndarray, scores: np.ndarray, rng: np.random.Generator) -> np.ndarray:
    """Сектор колеса пропорционален фитнессу, пул собирается N запусками."""
    total = float(scores.sum())
    probabilities = scores / total
    chosen = rng.choice(len(population), size=len(population), replace=True, p=probabilities)
    return population[chosen].copy()


def elite(population: np.ndarray, scores: np.ndarray, rng: np.random.Generator) -> np.ndarray:
    """Родителями становятся только лучшие особи. Первая в пуле — лучшая без замены."""
    order = np.argsort(scores)[::-1]
    best = population[order[:ELITE]]
    chosen = rng.choice(ELITE, size=len(population), replace=True)
    pool = best[chosen].copy()
    pool[0] = population[order[0]]
    return pool


def crossover(pool: np.ndarray, rng: np.random.Generator) -> np.ndarray:
    children = pool.copy()
    order = rng.permutation(len(children))
    children = children[order]
    for index in range(0, len(children) - 1, 2):
        if rng.random() > CROSSOVER:
            continue
        point = int(rng.integers(1, GENES))
        left = children[index].copy()
        right = children[index + 1].copy()
        children[index, point:] = right[point:]
        children[index + 1, point:] = left[point:]
    return children


def mutate(population: np.ndarray, rng: np.random.Generator) -> None:
    for chromosome in population:
        if rng.random() < MUTATION:
            gene = int(rng.integers(0, GENES))
            chromosome[gene] = 1 - chromosome[gene]


def evolve(selector, seed: int) -> dict:
    rng = np.random.default_rng(seed)
    population = rng.integers(0, 2, size=(POPULATION, GENES))
    history = []
    reached = None
    started = time.perf_counter()
    for generation in range(1, GENERATIONS + 1):
        scores = fitness(population)
        best_index = int(np.argmax(scores))
        best_score = float(scores[best_index])
        best = population[best_index].copy()
        history.append(best_score)
        if reached is None and best_score >= TARGET:
            reached = generation
        pool = selector(population, scores, rng)
        children = crossover(pool, rng)
        mutate(children, rng)
        if selector is elite:
            child_scores = fitness(children)
            worst = int(np.argmin(child_scores))
            if best_score >= child_scores[worst]:
                children[worst] = best
        population = children
    scores = fitness(population)
    best_index = int(np.argmax(scores))
    x, y = decode(population[best_index])
    return {
        "x": x,
        "y": y,
        "value": float(scores[best_index]),
        "reached": reached,
        "seconds": time.perf_counter() - started,
        "history": history,
    }


def summarize(name: str, runs: list[dict]) -> None:
    reached = [run["reached"] for run in runs if run["reached"] is not None]
    values = [run["value"] for run in runs]
    seconds = [run["seconds"] for run in runs]
    print(f"\n{name}")
    print(f"  дошли до f ≥ {TARGET}: {len(reached)}/{len(runs)}")
    if reached:
        print(f"  поколений до порога, среднее: {np.mean(reached):.1f}")
    print(f"  лучший фитнесс, среднее: {np.mean(values):.6f}")
    print(f"  время одного прогона, среднее: {np.mean(seconds) * 1000:.1f} мс")
    winner = max(runs, key=lambda run: run["value"])
    print(
        f"  лучшая найденная точка: x = {winner['x']:.6f}, y = {winner['y']:.6f}, "
        f"f = {winner['value']:.6f}"
    )


def main() -> None:
    print("f(x, y) = 1 / (1 + x² + y²), максимум 1 в точке (0, 0)")
    print(
        f"поиск на [{LOW:g}, {HIGH:g}]², {BITS} бит на переменную, "
        f"особей {POPULATION}, поколений {GENERATIONS}"
    )
    print(f"кроссинговер {CROSSOVER}, мутация {MUTATION}, элита — {ELITE} лучших особей")
    roulette_runs = [evolve(roulette, seed) for seed in range(TRIALS)]
    elite_runs = [evolve(elite, seed) for seed in range(TRIALS)]
    summarize("Рулетка", roulette_runs)
    summarize("Элитный отбор", elite_runs)

    print("\nОдин и тот же старт, семя 1")
    for name, selector in (("рулетка", roulette), ("элиты", elite)):
        run = evolve(selector, seed=1)
        mark = run["reached"] if run["reached"] is not None else "не достигнут"
        print(
            f"  {name}: x = {run['x']:.6f}, y = {run['y']:.6f}, "
            f"f = {run['value']:.6f}, порог на поколении {mark}, {run['seconds'] * 1000:.1f} мс"
        )
        step = 10
        row = " ".join(f"{score:.3f}" for score in run["history"][step - 1 :: step])
        print(f"    лучший фитнесс каждое {step}-е поколение: {row}")


if __name__ == "__main__":
    main()
