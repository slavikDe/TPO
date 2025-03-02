package Journal;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class Register {

    private HashMap<Student, ArrayList<Integer>> register;
    private Student[] students;

    private Lock lock = new ReentrantLock();

    Register(Student[] students) {
        register = new HashMap<>();
        this.students = students;

        for (Student student : students) {
            register.put(student, new ArrayList<>());
        }
    }

    public void prettyPrint() {
        for (Student student : students) {
            if(register.containsKey(student)) {
                System.out.println(student.name + " " + register.get(student).toString());
            }
        }
    }

    public void setMark(Student student, int mark) {
        lock.lock();
        if (register.containsKey(student)) {
            register.get(student).add(mark);
        }
        lock.unlock();
    }

    public int getRegisterSize(){
        int size = 0;
        for (Student student : register.keySet()) {
            size += register.get(student).size();
        }
        return size;
    }


    public Student[] getStudents() {
        return students;
    }

    public ArrayList<Integer> getMarks(Student st){
        return register.get(st);
    }
}
