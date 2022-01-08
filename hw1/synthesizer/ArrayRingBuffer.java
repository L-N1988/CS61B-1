package synthesizer;

import java.util.Iterator;

//TODO: Make sure to make this class and all of its methods public
public class ArrayRingBuffer<T> extends AbstractBoundedQueue<T> {
    /* Index for the next dequeue or peek. */
    private int first;
    /* Index for the next enqueue. */
    private int last;
    /* Array for storing the buffer data. */
    private T[] rb;

    /**
     * Create a new ArrayRingBuffer with the given capacity.
     */
    public ArrayRingBuffer(int capacity) {
        rb = (T[]) new Object[capacity];
        first = 0;
        last = 0;
        fillCount = 0;
        this.capacity = capacity;
        // Note that the local variable here shadows the field
        // we inherit from AbstractBoundedQueue, so you'll
        // need to use this.capacity to set the capacity.
    }

    /**
     * Adds x to the end of the ring buffer. If there is no room, then
     * throw new RuntimeException("Ring buffer overflow"). Exceptions
     * covered Monday.
     */
    public void enqueue(T x) {
        if (fillCount < capacity) {
            rb[last] = x;
            last = (last + 1) % capacity;
            fillCount++;
        }
        //throw new Exception();
    }

    /**
     * Dequeue oldest item in the ring buffer. If the buffer is empty, then
     * throw new RuntimeException("Ring buffer underflow"). Exceptions
     * covered Monday.
     */
    public T dequeue() {
        if (fillCount != 0) {
            T item = rb[first];
            rb[first] = null;
            first = (first + 1) % capacity;
            fillCount--;
            return item;
        }
        return null; // exception
    }

    /**
     * Return oldest item, but don't remove it.
     */
    public T peek() {
        if (fillCount != 0) {
            T item = rb[first];
            return item;
        }
        return null; // exception
    }

    public Iterator<T> iterator() {
        return new BufferIterator();
    }

    public class BufferIterator implements Iterator<T> {
        private int ptr;

        public BufferIterator() {
            ptr = 0;
        }

        public boolean hasNext() {
            return (ptr != fillCount);
        }

        public T next() {
            T item = rb[(first + ptr) % capacity];
            ptr++;
            return item;
        }
    }


}
