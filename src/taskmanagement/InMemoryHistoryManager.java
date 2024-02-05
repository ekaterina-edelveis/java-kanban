package taskmanagement;

import java.util.*;

public class InMemoryHistoryManager implements HistoryManager {

    public class Node {
        public Task data;
        public Node next;
        public Node prev;


        public Node(Node prev, Task data, Node next) {
            this.data = data;
            this.next = next;
            this.prev = prev;

        }
    }

    private Node head;
    private Node tail;

    private final HashMap<Integer, Node> linkedTaskHistory = new HashMap<>();


    public void linkLast(Task task) {
        Node oldTail = tail;
        Node newNode = new Node(oldTail, task, null);
        tail = newNode;
        if (oldTail == null) {
            head = newNode;
        }
        else {
            oldTail.next = newNode;
        }

        linkedTaskHistory.put(task.getId(), newNode);

    }


    public void removeNode(Node node) {

        //удаляем ноду и перенастраиваем ссылки

        Node next = node.next;
        Node prev = node.prev;
        if(prev == null){
            head = next;
        }
        else {
            prev.next = next;
        }
        if (next == null) {
            tail = prev;
        } else {
            next.prev = prev;
        }
    }


    @Override
    public void add(Task task) {

        int id = task.getId();
        if (linkedTaskHistory.containsKey(id)) {
            Node node = linkedTaskHistory.get(id);
            removeNode(node);
            linkedTaskHistory.remove(id);
        }
        linkLast(task);
    }


    @Override
    public List<Task> getHistory() {

        ArrayList<Task> taskHistory = new ArrayList<>();
        Node temp = head;
        for(int i = 0; i < linkedTaskHistory.size(); i++){
            taskHistory.add(temp.data);
            temp = temp.next;
        }
        return taskHistory;
    }

    @Override
    public void remove(int id) {
        if(linkedTaskHistory.containsKey(id)){
            Node node = linkedTaskHistory.get(id);
            removeNode(node);
            linkedTaskHistory.remove(id);
        }
    }

    @Override
    public void removeAll(List<Task> tasks){

        for(Task task : tasks){
            remove(task.getId());
        }


    }
}
