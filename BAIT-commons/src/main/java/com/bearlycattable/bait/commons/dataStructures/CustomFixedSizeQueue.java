package com.bearlycattable.bait.commons.dataStructures;

import java.util.AbstractQueue;
import java.util.ConcurrentModificationException;
import java.util.Iterator;
import java.util.NoSuchElementException;

public class CustomFixedSizeQueue<E> extends AbstractQueue<E> {

    final Object[] queue;
    int size;
    transient int modCount;

    public CustomFixedSizeQueue(int capacity) {
        super();

        queue = new Object[capacity];
        size = 0;
    }

    @Override
    public Iterator<E> iterator() {
        return new CustomFixedSizeQueueIterator();
    }

    @Override
    public int size() {
        return size;
    }

    @Override
    public boolean offer(E e) {
        if (e == null) {
            throw new NullPointerException("Queue doesn't allow nulls");
        }
        modCount++;

        while (size >= queue.length) {
            poll();
        }

        queue[size] = e;
        size++;

        return true;
    }

    @Override
    public E poll() {
        if (size <= 0) {
            return null;
        }

        E item = (E) queue[0];
        modCount++;
        shiftLeft();
        size--;

        return item;
    }

    private void shiftLeft() {
        int i = 1;
        while (i < queue.length) {
            if (queue[i] == null) {
                break;
            }
            queue[i - 1] = queue[i];
            if ((i + 1) == queue.length) {
                queue[queue.length - 1] = null;
            }
            i++;
        }
    }

    @Override
    public E peek() {
        if (size <= 0) {
            return null;
        }
        return (E) queue[0];
    }

    @Override
    public boolean add(E e) {
        return offer(e);
    }

    @Override
    public E remove() {
        return poll();
    }

    @Override
    public E element() {
        return peek();
    }

    @Override
    public void clear() {
        modCount++;
        for (int i = 0, n = size; i < n; i++) {
            queue[i] = null;
        }
        size = 0;
    }

    private final class CustomFixedSizeQueueIterator implements Iterator<E> {

        private int cursor;
        private int expectedModCount = modCount;

        CustomFixedSizeQueueIterator() {}

        public boolean hasNext() {
            return cursor < size;
        }

        @SuppressWarnings("unchecked")
        public E next() {
            if (expectedModCount != modCount) {
                throw new ConcurrentModificationException();
            }
            if (cursor < size) {
                return (E) queue[cursor++];
            }
            throw new NoSuchElementException();
        }

        public void remove() {
            throw new IllegalStateException("Removal while iterating is not allowed for CustomFixedSizeQueue");
            // if (expectedModCount != modCount) {
            //     throw new ConcurrentModificationException();
            // }
            //
            // if (lastRet != -1) {
            //     E moved = CustomFixedSizeQueue.this.removeAt(lastRet);
            //     lastRet = -1;
            //     if (moved == null) {
            //         cursor--;
            //     }
            // } else if (lastRetElt != null) {
            //     PriorityQueue.this.removeEq(lastRetElt);
            //     lastRetElt = null;
            // } else {
            //     throw new IllegalStateException();
            // }
            // expectedModCount = modCount;
        }
    }
}