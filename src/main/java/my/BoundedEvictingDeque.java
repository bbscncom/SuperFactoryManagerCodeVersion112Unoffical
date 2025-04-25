package my;

import java.util.Collection;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Spliterator;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

public class BoundedEvictingDeque<E> implements  Iterable<E>{
    private final ConcurrentLinkedDeque<E> deque;
    private final AtomicInteger size;
    private final int maxSize;
    private final Consumer<E> evictionHandler;

    public BoundedEvictingDeque(int maxSize) {
        this(maxSize, null);
    }

    public BoundedEvictingDeque(int maxSize, Consumer<E> evictionHandler) {
        this.deque = new ConcurrentLinkedDeque<>();
        this.size = new AtomicInteger(0);
        this.maxSize = maxSize;
        this.evictionHandler = evictionHandler;
    }

    public void add(E e) {
        while (true) {
            int current = size.get();
            if (current < maxSize) {
                if (size.compareAndSet(current, current + 1)) {
                    deque.addLast(e);
                    return;
                }
            } else {
                E removed = deque.pollFirst();
                if (removed != null && evictionHandler != null) {
                    evictionHandler.accept(removed);
                }
                deque.addLast(e);
                return;
            }
        }
    }

    // 首尾元素访问
    public E getFirst() {
        E e = deque.peekFirst();
        return e;
    }

    public E getLast() {
        E e = deque.peekLast();
        return e;
    }
    public boolean offer(E e) {
        add(e);
        return true;
    }

    public E poll() {
        E e = deque.pollFirst();
        if (e != null) {
            size.decrementAndGet();
        }
        return e;
    }


    // 批量操作方法
    public void addAll(Collection<? extends E> c) {
        for (E e : c) {
           add(e);
        }
    }
    // 迭代支持
    @Override
    public Iterator<E> iterator() {
        return deque.iterator();
    }

    @Override
    public void forEach(Consumer<? super E> action) {
        deque.forEach(action);
    }

    @Override
    public Spliterator<E> spliterator() {
        return deque.spliterator();
    }

    // 清空队列
    public void clear() {
        deque.clear();
        size.set(0);
    }

    // 其他实用方法
    public int size() {
        return size.get();
    }

    public boolean isEmpty() {
        return size.get() == 0;
    }

    public boolean isFull() {
        return size.get() >= maxSize;
    }
}