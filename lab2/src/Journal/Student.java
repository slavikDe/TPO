package Journal;

public class Student {

    String name;

    Student(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void seeMarks(Register r){
        System.out.print("Student: " + name + " marks: " + r.getMarks(this).toString());
    }
}
