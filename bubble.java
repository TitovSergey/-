public class BubbleSort {

    /**
     * Классическая пузырьковая сортировка (без оптимизаций)
     * @param array массив для сортировки
     */
    public static void bubbleSortBasic(int[] array) {
        int n = array.length;
        
        // Проходим по массиву n-1 раз
        for (int i = 0; i < n - 1; i++) {
            // Сравниваем соседние элементы
            for (int j = 0; j < n - 1; j++) {
                // Если текущий элемент больше следующего, меняем их местами
                if (array[j] > array[j + 1]) {
                    swap(array, j, j + 1);
                }
            }
        }
    }

    /**
     * Оптимизированная пузырьковая сортировка
     * @param array массив для сортировки
     */
    public static void bubbleSortOptimized(int[] array) {
        int n = array.length;
        boolean swapped;
        
        for (int i = 0; i < n - 1; i++) {
            swapped = false;
            
            // После каждой итерации самый большой элемент "всплывает" в конец,
            // поэтому можно уменьшать диапазон сравнения
            for (int j = 0; j < n - 1 - i; j++) {
                if (array[j] > array[j + 1]) {
                    swap(array, j, j + 1);
                    swapped = true;
                }
            }
            
            // Если за проход не было обменов, массив уже отсортирован
            if (!swapped) {
                break;
            }
        }
    }

    /**
     * Пузырьковая сортировка с двунаправленным проходом (шейкерная сортировка)
     * @param array массив для сортировки
     */
    public static void bubbleSortBidirectional(int[] array) {
        int left = 0;
        int right = array.length - 1;
        boolean swapped;
        
        while (left < right) {
            swapped = false;
            
            // Проход слева направо - большие элементы "всплывают" вправо
            for (int i = left; i < right; i++) {
                if (array[i] > array[i + 1]) {
                    swap(array, i, i + 1);
                    swapped = true;
                }
            }
            right--; // Уменьшаем правую границу
            
            // Проход справа налево - маленькие элементы "всплывают" влево
            for (int i = right; i > left; i--) {
                if (array[i] < array[i - 1]) {
                    swap(array, i, i - 1);
                    swapped = true;
                }
            }
            left++; // Увеличиваем левую границу
            
            // Если не было обменов, массив отсортирован
            if (!swapped) {
                break;
            }
        }
    }

    /**
     * Вспомогательный метод для обмена элементов массива
     */
    private static void swap(int[] array, int i, int j) {
        int temp = array[i];
        array[i] = array[j];
        array[j] = temp;
    }

    /**
     * Метод для вывода массива на экран
     */
    public static void printArray(int[] array) {
        for (int value : array) {
            System.out.print(value + " ");
        }
        System.out.println();
    }

    /**
     * Метод для проверки отсортированности массива
     */
    public static boolean isSorted(int[] array) {
        for (int i = 0; i < array.length - 1; i++) {
            if (array[i] > array[i + 1]) {
                return false;
            }
        }
        return true;
    }
}
