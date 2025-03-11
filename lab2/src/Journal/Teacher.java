package Journal;

import java.util.ArrayList;
import java.util.Random;

public class Teacher implements Runnable {

    private final String name;
    private final Register register;

    Teacher(Register register, String name) {
        this.name = name;
        this.register = register;
    }

    private void setMark(){
        Student[] students = register.getStudents();
        for (int i = 0; i < 1000; i++) {
            Student student = students[new Random().nextInt(students.length)];
            int mark = new Random().nextInt(100);

            register.setMark(student, mark);
        }

    }

    public ArrayList<Integer> seeMarks(Student student){
        return register.getMarks(student);
    }

    @Override
    public void run() {
        System.out.println(name + " started");
        setMark();
        System.out.println(name + " finished");
    }
}
