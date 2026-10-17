# -*- coding: utf-8 -*-
"""Когнитрон: слой возбуждающих и тормозящих нейронов.

Параметры из лекции: поле 12×12, область связи 5×5, конкуренция — ромб 5×5,
латеральное торможение 7×7, q = 16, q' = 2, начальные веса нулевые.
Обучается один слой поверх входа: на этих образах следующий слой уже
смешивает отклики в один и тот же рисунок.

Выход возбуждающего нейрона

    NET = (1 + E) / (1 + I) − 1
    OUT = NET, если NET ≥ 0, иначе 0

E — сумма возбуждающих входов, I = b·v, v — среднее в области связи.
В области конкуренции веса меняет только самый возбуждённый нейрон.
Пока веса нулевые и победителя нет, нейрон получает слабый шаг q'.
"""

from __future__ import annotations

import numpy as np


class Cognitron:
    def __init__(
        self,
        size: int = 12,
        trainable_layers: int = 1,
        connect: int = 5,
        compete: int = 5,
        lateral: int = 7,
        q: float = 16.0,
        q_prime: float = 2.0,
        seed: int = 1,
    ) -> None:
        self.size = size
        self.layers = trainable_layers
        self.connect_radius = connect // 2
        self.compete_radii = [compete // 2] * trainable_layers
        # Клетки без полного окна 5×5 не обучаются: иначе край поля
        # побеждает в конкуренции и запоминает обрывки всех образов.
        self.lateral_radius = lateral // 2
        self.q = q
        self.q_prime = q_prime
        self.rng = np.random.default_rng(seed)
        kernel = connect
        self.a = np.zeros((trainable_layers, size, size, kernel, kernel))
        self.b = np.zeros((trainable_layers, size, size))

    def train(self, patterns: list[np.ndarray], repeats: int = 20) -> None:
        for _ in range(repeats):
            for pattern in patterns:
                self._present(np.asarray(pattern, dtype=float), learn=True)

    def respond(self, pattern: np.ndarray) -> list[np.ndarray]:
        return self._present(np.asarray(pattern, dtype=float), learn=False)

    def reconstruct(self, pattern: np.ndarray) -> np.ndarray:
        """Обратный прогон: связи проводят назад, торможение выключено."""
        outputs = self.respond(pattern)
        signal = outputs[-1]
        for layer in range(self.layers - 1, -1, -1):
            signal = self._backward(layer, signal)
        return signal

    def _present(self, pattern: np.ndarray, learn: bool) -> list[np.ndarray]:
        outputs = [pattern]
        for layer in range(self.layers):
            inhibited, raw, excit, inhib = self._forward(layer, outputs[-1])
            if learn:
                self._learn(layer, outputs[-1], inhibited, excit, inhib)
            outputs.append(inhibited)
        return outputs

    def _inside(self, y: int, x: int) -> bool:
        radius = self.connect_radius
        return radius <= y < self.size - radius and radius <= x < self.size - radius

    def _patch(self, source: np.ndarray, y: int, x: int, radius: int) -> np.ndarray:
        kernel = radius * 2 + 1
        patch = np.zeros((kernel, kernel))
        for dy in range(-radius, radius + 1):
            for dx in range(-radius, radius + 1):
                yy, xx = y + dy, x + dx
                if 0 <= yy < self.size and 0 <= xx < self.size:
                    patch[dy + radius, dx + radius] = source[yy, xx]
        return patch

    def _forward(
        self, layer: int, source: np.ndarray
    ) -> tuple[np.ndarray, np.ndarray, np.ndarray, np.ndarray]:
        raw = np.zeros((self.size, self.size))
        excit = np.zeros_like(raw)
        inhib = np.zeros_like(raw)
        radius = self.connect_radius
        kernel = radius * 2 + 1
        for y in range(self.size):
            for x in range(self.size):
                if not self._inside(y, x):
                    continue
                patch = self._patch(source, y, x, radius)
                present = np.zeros((kernel, kernel))
                for dy in range(-radius, radius + 1):
                    for dx in range(-radius, radius + 1):
                        if 0 <= y + dy < self.size and 0 <= x + dx < self.size:
                            present[dy + radius, dx + radius] = 1.0
                # c заданы заранее и в сумме дают 1 на полном окне 5×5.
                # Не перенормировать край: иначе пограничные синапсы растут
                # быстрее и забирают всю конкуренцию.
                weight_c = np.full((kernel, kernel), 1.0 / (kernel * kernel))
                weight_c *= present
                inhibitory = float(np.sum(weight_c * patch))
                excitatory = float(np.sum(self.a[layer, y, x] * patch))
                net = (1.0 + excitatory) / (1.0 + self.b[layer, y, x] * inhibitory) - 1.0
                raw[y, x] = net if net > 0.0 else 0.0
                excit[y, x] = excitatory
                inhib[y, x] = inhibitory
        return self._lateral(raw), raw, excit, inhib

    def _lateral(self, raw: np.ndarray) -> np.ndarray:
        inhibited = np.zeros_like(raw)
        radius = self.lateral_radius
        for y in range(self.size):
            for x in range(self.size):
                if not self._inside(y, x):
                    continue
                total = 0.0
                count = 0
                for dy in range(-radius, radius + 1):
                    for dx in range(-radius, radius + 1):
                        yy, xx = y + dy, x + dx
                        if 0 <= yy < self.size and 0 <= xx < self.size and self._inside(yy, xx):
                            total += raw[yy, xx]
                            count += 1
                lat = total / count
                net = (1.0 + raw[y, x]) / (1.0 + lat) - 1.0
                inhibited[y, x] = net if net > 0.0 else 0.0
        return inhibited

    def _learn(
        self,
        layer: int,
        source: np.ndarray,
        output: np.ndarray,
        excit: np.ndarray,
        inhib: np.ndarray,
    ) -> None:
        compete = self.compete_radii[layer]
        noise = self.rng.random(output.shape) * 1e-8
        ranked = output + noise
        for y in range(self.size):
            for x in range(self.size):
                if not self._inside(y, x):
                    continue
                # Ромб: оставляем клетки с |dy| + |dx| <= compete.
                diamond = [
                    ranked[yy, xx]
                    for yy in range(max(0, y - compete), min(self.size, y + compete + 1))
                    for xx in range(max(0, x - compete), min(self.size, x + compete + 1))
                    if abs(yy - y) + abs(xx - x) <= compete and self._inside(yy, xx)
                ]
                if ranked[y, x] >= max(diamond) and output[y, x] > 0.0:
                    self._grow(layer, source, y, x, inhib[y, x], excit[y, x], self.q)
                elif not np.any(self.a[layer, y, x]):
                    # С нулевых весов нейрон ещё не может победить, поэтому
                    # первый шаг делается меньшим коэффициентом q'.
                    self._grow(layer, source, y, x, inhib[y, x], excit[y, x], self.q_prime)

    def _grow(
        self,
        layer: int,
        source: np.ndarray,
        y: int,
        x: int,
        inhibitory: float,
        excitatory: float,
        rate: float,
    ) -> None:
        radius = self.connect_radius
        kernel = radius * 2 + 1
        present = np.zeros((kernel, kernel))
        patch = np.zeros((kernel, kernel))
        for dy in range(-radius, radius + 1):
            for dx in range(-radius, radius + 1):
                yy, xx = y + dy, x + dx
                if 0 <= yy < self.size and 0 <= xx < self.size:
                    present[dy + radius, dx + radius] = 1.0
                    patch[dy + radius, dx + radius] = source[yy, xx]
        weight_c = np.full((kernel, kernel), 1.0 / (kernel * kernel))
        weight_c *= present
        self.a[layer, y, x] += rate * weight_c * patch
        # Победитель: δb = q·Σ c_j u_j / 2. Деление ещё и на v раздувает
        # торможение, когда вход слабый, и верхние слои гаснут.
        if rate == self.q:
            self.b[layer, y, x] += rate * float(np.sum(weight_c * patch)) / 2.0
        elif rate == self.q_prime:
            self.b[layer, y, x] += rate * inhibitory

    def _backward(self, layer: int, output: np.ndarray) -> np.ndarray:
        restored = np.zeros((self.size, self.size))
        radius = self.connect_radius
        for y in range(self.size):
            for x in range(self.size):
                if output[y, x] == 0.0:
                    continue
                for dy in range(-radius, radius + 1):
                    for dx in range(-radius, radius + 1):
                        yy, xx = y + dy, x + dx
                        if 0 <= yy < self.size and 0 <= xx < self.size:
                            restored[yy, xx] += self.a[layer, y, x, dy + radius, dx + radius] * output[y, x]
        return restored


def draw(pattern: np.ndarray, level: float | None = None) -> str:
    if level is None:
        shown = pattern > 0.5
    else:
        peak = float(pattern.max())
        shown = pattern >= level * peak if peak > 0 else pattern > 0
    return "\n".join("".join("#" if cell else "." for cell in row) for row in shown)


def similarity(left: np.ndarray, right: np.ndarray) -> float:
    a = left.ravel()
    b = right.ravel()
    denom = float(np.linalg.norm(a) * np.linalg.norm(b))
    if denom == 0.0:
        return 0.0
    return float(a @ b / denom)


def make_patterns(size: int = 12) -> list[tuple[str, np.ndarray]]:
    blank = np.zeros((size, size))

    plus = blank.copy()
    plus[size // 2, 2 : size - 2] = 1
    plus[2 : size - 2, size // 2] = 1

    frame = blank.copy()
    frame[2 : size - 2, 2] = 1
    frame[2 : size - 2, size - 3] = 1
    frame[2, 2 : size - 2] = 1
    frame[size - 3, 2 : size - 2] = 1

    diagonal = blank.copy()
    for index in range(2, size - 2):
        diagonal[index, index] = 1
        diagonal[index, index - 1] = 1

    tee = blank.copy()
    tee[2, 2 : size - 2] = 1
    tee[2 : size - 2, size // 2] = 1

    zigzag = blank.copy()
    zigzag[3, 2:10] = 1
    zigzag[8, 2:10] = 1
    for index in range(6):
        zigzag[3 + index, 8 - index] = 1

    return [("крест", plus), ("рамка", frame), ("диагональ", diagonal), ("буква Т", tee), ("зигзаг", zigzag)]


def corrupt(pattern: np.ndarray, flips: int, rng: np.random.Generator) -> np.ndarray:
    noisy = pattern.copy()
    coords = rng.choice(pattern.size, size=flips, replace=False)
    flat = noisy.ravel()
    flat[coords] = 1.0 - flat[coords]
    return noisy


def main() -> None:
    patterns = make_patterns()
    network = Cognitron()
    network.train([pattern for _, pattern in patterns], repeats=20)

    outputs = [(name, network.respond(pattern)[-1]) for name, pattern in patterns]
    print("Когнитрон обучен на пяти произвольных образах, каждый показан 20 раз.")
    print("\nСходство выходных образов (1 — тот же отклик)")
    header = "          " + "".join(f"{name[:6]:>8}" for name, _ in patterns)
    print(header)
    for name, output in outputs:
        row = "".join(f"{similarity(output, other):8.2f}" for _, other in outputs)
        print(f"{name[:8]:8}{row}")

    print("\nУзнавание чистых образов по ближайшему выходному отклику")
    correct = 0
    for name, output in outputs:
        found = max(outputs, key=lambda item: similarity(output, item[1]))[0]
        correct += int(found == name)
        print(f"  {name}: {found}")
    print(f"итог: {correct}/{len(patterns)}")

    rng = np.random.default_rng(3)
    flips = 4
    trials = 10
    hits = 0
    print(f"\nТе же образы с {flips} изменёнными клетками, {trials} попыток")
    for name, pattern in patterns:
        ok = 0
        for _ in range(trials):
            response = network.respond(corrupt(pattern, flips, rng))[-1]
            found = max(outputs, key=lambda item: similarity(response, item[1]))[0]
            ok += int(found == name)
        hits += ok
        print(f"  {name}: {ok}/{trials}")
    print(f"итог: {hits}/{len(patterns) * trials}")

    print("\nОбратный прогон: выход слоя возвращается ко входу")
    for name, pattern in patterns:
        restored = network.reconstruct(pattern)
        print(f"\n{name}  сходство с исходным {similarity(restored, pattern):.2f}")
        print(draw(pattern))
        print(draw(restored, level=0.35))


if __name__ == "__main__":
    main()
