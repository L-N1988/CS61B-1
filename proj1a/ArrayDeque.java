public class ArrayDeque<T> {
    private T[] items;
    private int size;
    private int nextFirst;
    private int nextLast;
    private int length;
    public ArrayDeque() {
        length = 8;
        items = (T[]) (new Object[length]);
        size = 0;
        nextFirst = length / 2;
        nextLast = length / 2 + 1;
    }

    public int size() {
        return size;
    }

    private int getIndex(int i) {
        return (length + i) % length;
    }

    public boolean isEmpty() {
        return size == 0;
    }

    public void addFirst(T item) {
        // TODO: if full, resize, else:
        items[nextFirst] = item;
        nextFirst = getIndex(nextFirst - 1);
        size++;
    }

    public void addLast(T item) {
        // TODO: if full, resize, else:
        items[nextLast] = item;
        nextLast = getIndex(nextLast + 1);
        size++;
    }

    public T removeFirst() {
        int nextIndex = getIndex(nextFirst + 1);
        T result = items[nextIndex];
        if (result != null) {
            items[nextIndex] = null;
            nextFirst = nextIndex;
            size--;
        }
        return result;
    }

    public T removeLast() {
        int nextIndex = getIndex(nextLast - 1);
        T result = items[nextIndex];
        if (result != null) {
            items[nextIndex] = null;
            nextLast = getIndex(nextLast - 1);
            size--;
        }
        return result;
    }

    public void printDeque() {
        int begin = getIndex(nextFirst + 1);
        int end = getIndex(nextLast - 1);
        while (begin != end) {
            System.out.print(get(begin) + " ");
            begin = getIndex(begin + 1);
        }
        System.out.print(get(end));
    }

    public T get(int index) {
        int i = getIndex(index);
        return items[i];
    }

}
