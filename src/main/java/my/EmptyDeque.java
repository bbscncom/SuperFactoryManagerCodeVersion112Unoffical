package my;

import java.util.*;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

public class EmptyDeque<E> implements Iterable<E> {
    public EmptyDeque(int maxSize) {
    }

    public EmptyDeque(int maxSize, Consumer<E> evictionHandler) {
    }

    public void add(E e) {
    }

    public E getFirst() {
        return null;
    }

    public E getLast() {
        return null;
    }

    public boolean offer(E e) {
        return false;
    }

    public E poll() {
        return null;
    }

    public void addAll(Collection<? extends E> c) {
    }

    @Override
    public void forEach(Consumer<? super E> action) {
    }

    @Override
    public Iterator<E> iterator() {
        return Collections.emptyIterator(); // 返回空迭代器而非null
    }

    @Override
    public Spliterator<E> spliterator() {
        return Spliterators.emptySpliterator(); // 返回空分割器
    }


    public void clear() {
    }

    public int size() {
        return 0;
    }

    public boolean isEmpty() {
        return true;
    }

    public boolean isFull() {
        return false;
    }
}