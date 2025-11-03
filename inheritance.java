// Stack НЕ является Vector - плохой дизайн

class Stack<E> extends Vector<E> {

    public void push(E element) {

        add(element);

    }

    public E pop() {

        if (isEmpty()) throw new EmptyStackException();

        return remove(size() - 1);

    }

}

// Проблема:

Stack<String> stack = new Stack<>();

stack.push("A");

stack.push("B");    // Нарушение инвариантов стека - можно удалить элемент из середины.

