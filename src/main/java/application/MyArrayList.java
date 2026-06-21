package application;

public class MyArrayList<T> {
    // Internal array used to store elements.
    private Object[] data;
    // Current number of actual elements.
    private int size;

    public MyArrayList() {
        // Start with capacity 10, similar to ArrayList, but implemented by us.
        data = new Object[10];
        size = 0;
    }

    public void add(T value) {
        // If the array is full, grow it.
        ensureCapacity();
        // Put the element in the first free position.
        data[size] = value;
        size++;
    }

    public void add(int index, T value) {
        // Do not allow adding at an invalid index.
        if (index < 0 || index > size) {
            throw new IndexOutOfBoundsException("Index: " + index);
        }

        ensureCapacity();
        // Shift elements one step to the right to open space.
        for (int i = size; i > index; i--) {
            data[i] = data[i - 1];
        }
        // Place the value at the requested position.
        data[index] = value;
        size++;
    }

    @SuppressWarnings("unchecked")
    public T get(int index) {
        // Check that the index is inside the valid range.
        if (index < 0 || index >= size) {
            throw new IndexOutOfBoundsException("Index: " + index);
        }
        // Cast Object back to type T.
        return (T) data[index];
    }

    public int size() {
        // Return the number of elements, not the internal array length.
        return size;
    }

    public boolean isEmpty() {
        // True when there are no elements.
        return size == 0;
    }

    public void clear() {
        // Clear references so objects do not stay unnecessarily in memory.
        for (int i = 0; i < size; i++) {
            data[i] = null;
        }
        size = 0;
    }

    private void ensureCapacity() {
        // If there is still space, do nothing.
        if (size < data.length) {
            return;
        }

        // If full, create a new array with double capacity.
        Object[] newData = new Object[data.length * 2];
        for (int i = 0; i < data.length; i++) {
            newData[i] = data[i];
        }
        // Replace the old array with the new one.
        data = newData;
    }
}
