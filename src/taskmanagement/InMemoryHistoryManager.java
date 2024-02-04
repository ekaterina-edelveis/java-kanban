package taskmanagement;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class InMemoryHistoryManager implements HistoryManager {

    public class Node<E> {
        public E data;
        public Node<E> next;
        public Node<E> prev;


        public Node(Node<E> prev, E data, Node<E> next) {
            this.data = data;
            this.next = next;
            this.prev = prev;

        }
    }

    private final ArrayList<Task> taskHistory = new ArrayList<>();

    private Node<Task> head;
    private Node<Task> tail;

    private final HashMap<Integer, Node<Task>> linkedTaskHistory = new HashMap<>();


    public void linkLast(Task task) {
        Node<Task> oldTail = tail;
        Node<Task> newNode = new Node<>(oldTail, task, null);
        tail = newNode;
        if (oldTail == null)
            head = newNode;
        else
            oldTail.next = newNode;

        linkedTaskHistory.put(task.getId(), newNode);

    }


    public void removeNode(Node<Task> node) {

        //удаляем ноду и перенастраиваем ссылки

        Node<Task> next = node.next;
        Node<Task> prev = node.prev;
        if(prev == null){
            head = next;
        }
        else {
            prev.next = next;
            node.prev = null;
        }
        if (next == null) {
            tail = prev;
        } else {
            next.prev = prev;
            node.next = null;
        }

        node.data = null;


    }


    @Override
    public void add(Task task) {

        int id = task.getId();
        if (linkedTaskHistory.containsKey(id)) {
            Node<Task> node = linkedTaskHistory.get(id);
            removeNode(node);
            linkedTaskHistory.remove(id);
        }
        linkLast(task);
    }


    @Override
    public List<Task> getHistory() {

        //итерировать через ноды и сложить их в список
        Node<Task> temp = head;
        for(int i = 0; i < linkedTaskHistory.size(); i++){
            taskHistory.add(temp.data);
            temp = temp.next;
        }
        return List.copyOf(taskHistory);


    }

    @Override
    public void remove(int id) {
        if(linkedTaskHistory.containsKey(id)){
            Node<Task> node = linkedTaskHistory.get(id);
            removeNode(node);
            linkedTaskHistory.remove(id);
        }
    }

    /*
    я добавила метод по удалению пачки тасков из истории в случае,
    если в InMemoryTaskManager вызовут методы deleteAll*
     */
    @Override
    public void removeAll(List<Task> tasks){
        for(Task task : tasks){
            int id = task.getId();
            if(linkedTaskHistory.containsKey(id)) {
                Node<Task> node = linkedTaskHistory.get(id);
                removeNode(node);
                linkedTaskHistory.remove(id);
            }
        }
    }
}
