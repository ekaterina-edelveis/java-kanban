package taskmanagement;

import java.util.ArrayList;
import java.util.HashMap;

public class TaskManager {

    public HashMap<Integer, Task> tasks;
    public HashMap<Integer, Subtask> subtasks;
    public HashMap<Integer, Epic> epics;
    private int counter = 1;

    public TaskManager() {
        tasks = new HashMap<>();
        subtasks = new HashMap<>();
        epics = new HashMap<>();
    }

    public void createTask(Task task) {
        task.setId(counter);
        tasks.put(task.getId(), task);
        counter++;
    }

    /*у меня два метода для добавления эпика с подзадачами. Наверное, можно было бы обойтись одним,
    который с ArrayList, но в первом случае программа выполняет меньше действий. Соотв.,
    если одна подзадача, почему бы не воспользоваться менее энергозатратным методом.
     */

    public void createEpic(Epic epic, Subtask subtask) {
        epic.setId(counter);
        counter++;

        subtask.setId(counter);
        subtask.setEpic(epic);
        subtasks.put(subtask.getId(), subtask);
        counter++;

        epic.getSubtasks().add(subtask);
        epics.put(epic.getId(), epic);
    }

    public void createEpicWithMultipleSubtasks(Epic epic, ArrayList<Subtask> epicSubtasks) {
        epic.setId(counter);
        counter++;

        for (Subtask subtask : epicSubtasks) {
            subtask.setId(counter);
            subtask.setEpic(epic);
            subtasks.put(subtask.getId(), subtask);
            counter++;

            epic.getSubtasks().add(subtask);
        }

        epics.put(epic.getId(), epic);

    }

    public void updateTask(int id, Task task, Status status) {
        task.setId(id);
        tasks.put(task.getId(), task);
        tasks.get(id).setStatus(status);

    }


    public void updateEpic(int id, Epic epic) {
        String newName = epic.getName();
        String newDescription = epic.getDescription();
        Epic epicToBeUpdated = epics.get(id);
        epicToBeUpdated.setName(newName);
        epicToBeUpdated.setDescription(newDescription);

    }

    public void updateSubtask(int id, Subtask subtask, Status status) {

        //сначала обновляем задачу в hashmap

        Epic epicToBind = subtasks.get(id).getEpic();

        subtasks.remove(id);

        subtask.setId(id);
        subtask.setEpic(epicToBind);
        subtask.setStatus(status);
        subtasks.put(id, subtask);

        //теперь нам нужнообновить задачу в arraylist соответствующего эпика
        for (Epic epic : epics.values()) {

            if (epic.equals(subtask.getEpic())) {
                Epic epicToBeUpdated = epics.get(epic.getId());

                epicToBeUpdated.getSubtasks().remove(subtask);

                subtask.setStatus(status);
                epicToBeUpdated.getSubtasks().add(subtask);

                //проверяем статусы подзадач и при необходимости меняем статус эпика
                //для этого собираем все статусы подзадач в список и работаем с ним
                ArrayList<Status> subtasksStatuses = new ArrayList<>();
                for (Subtask sub : epicToBeUpdated.getSubtasks()) {
                    subtasksStatuses.add(sub.getStatus());
                }
                if (subtasksStatuses.contains(Status.NEW)
                        && !subtasksStatuses.contains(Status.IN_PROGRESS)
                        && !subtasksStatuses.contains(Status.DONE)) {
                    epicToBeUpdated.setStatus(Status.NEW);
                } else if (subtasksStatuses.contains(Status.DONE)
                        && !subtasksStatuses.contains(Status.IN_PROGRESS)
                        && !subtasksStatuses.contains(Status.NEW)) {
                    epicToBeUpdated.setStatus(Status.DONE);
                } else {
                    epicToBeUpdated.setStatus(Status.IN_PROGRESS);
                }
                break;
            }
        }

    }

    public ArrayList<Task> getAllTasks() {
        return new ArrayList<>(tasks.values());
    }

    public ArrayList<Epic> getAllEpics() {
        return new ArrayList<>(epics.values());
    }

    public ArrayList<Subtask> getAllSubtasksForEpic(int epicId) {
        ArrayList<Subtask> requiredSubtasks = new ArrayList<>();
        if (epics.containsKey(epicId)) {
            requiredSubtasks.addAll(epics.get(epicId).getSubtasks());
        }
        return requiredSubtasks;
    }

    public Task findTaskById(int id) {
        if (tasks.containsKey(id)) {
            return tasks.get(id);
        }
        return null;
    }

    public Epic findEpicById(int id) {
        if (epics.containsKey(id)) {
            return epics.get(id);
        }
        return null;
    }

    public Subtask findSubtaskById(int id) {
        if (subtasks.containsKey(id)) {
            return subtasks.get(id);
        }
        return null;
    }

    public void deleteAllTasks() {
        tasks.clear();
    }

    public void deleteAllEpics() {
        epics.clear();
        subtasks.clear();
    }

    public void deleteTaskById(int id) {
        tasks.remove(id);
    }


    public void deleteEpicById(int id) {
        //сначал собираем все задачи, которые принадлежат эпику, чтобы их удалить
        ArrayList<Integer> subtaskIdsToBeDeleted = new ArrayList<>();
        if (epics.containsKey(id)) {
            Epic epic = epics.get(id);
            for (Subtask subtask : subtasks.values()) {
                if (epic.equals(subtask.getEpic())) {
                    subtaskIdsToBeDeleted.add(subtask.getId());
                }
            }
            //удаляем подзадачи из hashmap
            for (int subtaskId : subtaskIdsToBeDeleted) {
                subtasks.remove(subtaskId);
            }

            //наконец, удаляем эпик
            epics.remove(id);
        }
    }

    public void deleteSubtaskById(int id) {
        if (subtasks.containsKey(id)) {
            Subtask subtask = subtasks.get(id);
            //нужно удалить задачу из arraylist эпика
            for (Epic epic : epics.values()) {
                if (epic.getSubtasks().contains(subtask)) {
                    epic.getSubtasks().remove(subtask);
                    break;
                }
            }
            //теперь можно удалить из hashmap
            subtasks.remove(id);
        }
    }


}
