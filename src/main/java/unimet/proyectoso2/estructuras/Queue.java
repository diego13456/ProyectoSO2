/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package unimet.proyectoso2.estructuras;

public class Queue<T> {
    private Nodo<T> head;
    private Nodo<T> tail;
    private int size;

    public Queue() {
        this.head = null;
        this.tail = null;
        this.size = 0;
    }

    public synchronized void enqueue(T element) {
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

    public synchronized T dequeue() {
        if (isEmpty()) {
            return null; // Omitimos lanzar excepciones de java.util
        }
        T data = head.getData();
        head = head.getNext();
        if (head == null) {
            tail = null;
        }
        size--;
        return data;
    }

    public synchronized T peek() {
        return isEmpty() ? null : head.getData();
    }

    public synchronized boolean isEmpty() {
        return size == 0;
    }
    
    public synchronized int size() {
        return size;
    }
}