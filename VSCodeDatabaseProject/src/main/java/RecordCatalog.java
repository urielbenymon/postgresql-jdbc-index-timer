import com.github.javafaker.Faker;
import java.util.ArrayList;

public class RecordCatalog {

    private ArrayList<Person> peopleArrayList;
    private Faker faker = new Faker();


    public RecordCatalog(int catalogSize) {
        this.peopleArrayList = new ArrayList<>();
        int i = 0;
        while (i < catalogSize) {
            record();
            i++;
        }
    }

    public int getRecordCatalogSize() {
        return this.peopleArrayList.size();
    }

    public Person getPerson(int id) {
        return this.peopleArrayList.get(id);
    }


    public void record() {
        Person person = new Person(faker.idNumber().valid(), 
        faker.name().firstName(), faker.name().lastName(), 
        faker.address().city(), faker.address().state(), 
        faker.internet().emailAddress());

        this.peopleArrayList.add(person);
    }





}
