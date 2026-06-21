package application;

public class MinPriorityQueue<T extends PriorityItem> {
    // Heap array; the smallest element is at index 0.
    private Object[] heap;
    // Number of elements in the heap.
    private int size;

    public MinPriorityQueue() {
        // Start with a small capacity and grow when needed.
        heap = new Object[10];
        size = 0;
    }

    public void add(T value) {
        // Grow the array if it is full.
        ensureCapacity();
        // Add the element at the end of the heap.
        heap[size] = value;
        // Move it upward until the min-heap order is correct.
        bubbleUp(size);
        size++;
    }

    @SuppressWarnings("unchecked")
    public T poll() {
        // If it is empty, there is no element to return.
        if (isEmpty()) {
            return null;
        }

        // The smallest element is always at the root.
        T min = (T) heap[0];
        size--;
        // Move the last element to the root.
        heap[0] = heap[size];
        heap[size] = null;
        // Move the element downward until heap order is correct.
        bubbleDown(0);
        return min;
    }

    public boolean isEmpty() {
        // True when there are no elements.
        return size == 0;
    }

    private void bubbleUp(int index) {
        // While the element is smaller than its parent, swap them.
        while (index > 0) {
            int parent = (index - 1) / 2;
            if (priority(parent) <= priority(index)) {
                break;
            }
            swap(parent, index);
            index = parent;
        }
    }

    private void bubbleDown(int index) {
        // Move the element down and swap it with the smaller child.
        while (true) {
            int left = index * 2 + 1;
            int right = index * 2 + 2;
            int smallest = index;

            if (left < size && priority(left) < priority(smallest)) {
                smallest = left;
            }
            if (right < size && priority(right) < priority(smallest)) {
                smallest = right;
            }
            if (smallest == index) {
                break;
            }

            swap(index, smallest);
            index = smallest;
        }
    }

    private void swap(int first, int second) {
        // Swap two elements inside the heap.
        Object temp = heap[first];
        heap[first] = heap[second];
        heap[second] = temp;
    }

    private double priority(int index) {
        // Read the element priority from the PriorityItem interface.
        return ((PriorityItem) heap[index]).getPriority();
    }

    private void ensureCapacity() {
        // If there is free space, do not grow.
        if (size < heap.length) {
            return;
        }

        // Double the heap size when it becomes full.
        Object[] newHeap = new Object[heap.length * 2];
        for (int i = 0; i < heap.length; i++) {
            newHeap[i] = heap[i];
        }
        heap = newHeap;
    }
}
