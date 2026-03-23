/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package unimet.proyectoso2.estructuras;

public class LinkedList<T> {
    private Nodo<T> head;
    private Nodo<T> tail;
    private int size;

    public LinkedList() {
        this.head = null;
        this.tail = null;
        this.size = 0;
    }

    public synchronized void add(T element) {
        Nodo<T> newNode = new Nodo<>(element);
        if (isEmpty()) {
            head = newNode;
            tail = newNode;
        } else {
            tail.setNext(newNode);
            tail = newNode;
        }
        size++;
    }

    public synchronized boolean remove(T element) {
        if (isEmpty()) return false;

        if (head.getData().equals(element)) {
            head = head.getNext();
            if (head == null) tail = null; 
            size--;
            return true;
        }

        Nodo<T> current = head;
        while (current.getNext() != null) {
            if (current.getNext().getData().equals(element)) {
                current.setNext(current.getNext().getNext());
                if (current.getNext() == null) {
                    tail = current; 
                }
                size--;
                return true;
            }
            current = current.getNext();
        }
        return false;
    }

    public synchronized T get(int index) {
        if (index < 0 || index >= size) {
            throw new IndexOutOfBoundsException("Índice fuera de rango: " + index);
        }
        Nodo<T> current = head;
        for (int i = 0; i < index; i++) {
            current = current.getNext();
        }
        return current.getData();
    }

    public synchronized int size() {
        return size;
    }

    public synchronized boolean isEmpty() {
        return size == 0;
    }
    
    public synchronized void repairList() {
        Nodo<T> current = head;
        int count = 0;
        tail = null;
        while (current != null) {
            tail = current;
            count++;
            current = current.getNext();
        }
        size = count;
    }
}