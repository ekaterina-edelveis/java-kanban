package taskmanagement;


public class Managers {

    /*
    Анна, привет! Я правильно понимаю, что метод getDefault в зависимости от условий будет возвращать
    объект конкретного класса, реализующего TaskManager?
    Т.е. будет типа
    if(...) return new InMemoryTaskManager()
    else if(...) return new CoolManager()
    и так далее?
     */
    public static TaskManager getDefault(){
        return new InMemoryTaskManager();
    }

    public static HistoryManager getDefaultHistory(){
        return new InMemoryHistoryManager();
    }



}
