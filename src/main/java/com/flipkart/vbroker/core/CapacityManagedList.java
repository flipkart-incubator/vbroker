package com.flipkart.vbroker.core;

import com.flipkart.vbroker.entities.Message;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.util.*;

@Slf4j
@Getter
public class CapacityManagedList<Item> extends Observable implements List {
    private final List<Item> itemList = new LinkedList<>();
    private String groupId;
    private int listUsedCapacity = 0;
    private Level level;

    public CapacityManagedList(String groupId) {
        this.groupId = groupId;
        this.level = Level.L2;
    }

    public void setLevel(Level level) {
        this.level = level;
    }

    @Override
    public int size() {
        return itemList.size();
    }

    @Override
    public boolean isEmpty() {
        return itemList.isEmpty();
    }

    @Override
    public boolean contains(Object o) {
        return itemList.contains(o);
    }

    @Override
    public Iterator iterator() {
        return itemList.iterator();
    }

    @Override
    public Object[] toArray() {
        return itemList.toArray();
    }

    @Override
    public boolean add(Object o) {
        boolean b = itemList.add((Item) o);
        if (b && o.getClass() == Message.class) {
            this.listUsedCapacity += ((Message) o).bodyLength() + ((Message) o).headersLength();
            setChanged();
            notifyObservers(this.listUsedCapacity);
        }
        return b;
    }

    @Override
    public boolean remove(Object o) {
        return itemList.remove(o);
    }

    @Override
    public boolean addAll(Collection c) {
        return itemList.addAll(c);
    }

    @Override
    public boolean addAll(int index, Collection c) {
        return itemList.addAll(index, c);
    }

    @Override
    public void clear() {
        itemList.clear();
    }

    @Override
    public Object get(int index) {
        return itemList.get(index);
    }

    @Override
    public Object set(int index, Object element) {
        return itemList.set(index, (Item) element);
    }

    @Override
    public void add(int index, Object element) {
        itemList.add(index, (Item) element);

    }

    @Override
    public Object remove(int index) {
        return itemList.remove(index);
    }

    @Override
    public int indexOf(Object o) {
        return itemList.indexOf(o);
    }

    @Override
    public int lastIndexOf(Object o) {
        return itemList.lastIndexOf(o);
    }

    @Override
    public ListIterator listIterator() {
        log.info("getting listIterator for groupId {} from level {}", this.groupId, level);
        return itemList.listIterator();
    }

    @Override
    public ListIterator listIterator(int index) {
        log.info("getting listIterator(index) for groupId {} from level {}", this.groupId, level);
        return itemList.listIterator(index);
    }

    @Override
    public List subList(int fromIndex, int toIndex) {
        return itemList.subList(fromIndex, toIndex);
    }

    @Override
    public boolean retainAll(Collection c) {
        return itemList.retainAll(c);
    }

    @Override
    public boolean removeAll(Collection c) {
        return itemList.removeAll(c);
    }

    @Override
    public boolean containsAll(Collection c) {
        return itemList.containsAll(c);
    }

    @Override
    public Object[] toArray(Object[] a) {
        return itemList.toArray(a);
    }

    protected enum Level {
        L1, L2, L3
    }
}
