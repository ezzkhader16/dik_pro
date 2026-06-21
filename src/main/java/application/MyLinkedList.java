package application;

public class MyLinkedList<T> {
    // One node stores one value and a reference to the next node.
    private static class Node<T> {
        T value;
        Node<T> next;

        Node(T value) {
            this.value = value;
        }
    }

    // First node in the list.
    private Node<T> head;
    // Last node in the list, used to add at the end quickly.
    private Node<T> tail;
    // Number of elements currently stored in the list.
    private int size;

    public void add(T value) {
        // Create a new node for the value.
        Node<T> node = new Node<>(value);

        // If the list is empty, the new node is both head and tail.
        if (head == null) {
            head = node;
            tail = node;
        } else {
            // Otherwise, connect the old tail to the new node.
            tail.next = node;
            tail = node;
        }

        size++;
    }

    public void add(int index, T value) {
        // Do not allow adding at an invalid index.
        if (index < 0 || index > size) {
            throw new IndexOutOfBoundsException("Index: " + index);
        }

        // Adding at the end is the normal add operation.
        if (index == size) {
            add(value);
            return;
        }

        Node<T> node = new Node<>(value);

        // Adding at index 0 means the new node becomes the head.
        if (index == 0) {
            node.next = head;
            head = node;
            if (tail == null) {
                tail = node;
            }
            size++;
            return;
        }

        // Find the node before the required index.
        Node<T> previous = nodeAt(index - 1);
        // Insert the new node between previous and previous.next.
        node.next = previous.next;
        previous.next = node;
        size++;
    }

    public T get(int index) {
        // Return the value stored at the requested index.
        return nodeAt(index).value;
    }

    public int size() {
        // Return the number of elements in the linked list.
        return size;
    }

    public boolean isEmpty() {
        // True when the linked list has no nodes.
        return size == 0;
    }

    public void clear() {
        // Removing the head and tail makes the old nodes unreachable.
        head = null;
        tail = null;
        size = 0;
    }

    private Node<T> nodeAt(int index) {
        // Check that the index is inside the valid range.
        if (index < 0 || index >= size) {
            throw new IndexOutOfBoundsException("Index: " + index);
        }

        // Linked lists do not jump directly to an index, so we move node by node.
        Node<T> current = head;
        for (int i = 0; i < index; i++) {
            current = current.next;
        }
        return current;
    }
}
