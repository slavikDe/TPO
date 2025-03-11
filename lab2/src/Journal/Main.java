package Journal;

public class Main {
    public static void main(String[] args) {

        String studentName = "Student ";
        String teacherName = "Teacher ";
        final int SIZE = 20;

        Student[] students = new Student[SIZE];
        for (int i = 0; i < SIZE; i++) {
            students[i] = new Student(studentName + (i + 1));
        }
        Register register = new Register(students);

        Thread[] threads = new Thread[4];
        for (int i = 0; i < 4; i++) {
            threads[i] = new Thread(new Teacher(register, teacherName + i));
        }

        for(Thread t: threads){
            t.start();
        }
        try {
           for(Thread t: threads){
               t.join();
           }
        }catch (InterruptedException _) {}

        register.prettyPrint();
        System.out.println(register.getRegisterSize());

        students[0].seeMarks(register);


    }
}

