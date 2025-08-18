public class Person {
    
    private String ssn;
    private String firstName;
    private String lastName;
    private String city;
    private String state;
    private String emailAddress;

    
    public Person(String ssn, String firstName, String lastName, 
    String city, String state, String emailAddress) {

        this.ssn = ssn;
        this.firstName = firstName;
        this.lastName = lastName;
        this.city = city;
        this.state = state;
        this.emailAddress = emailAddress;
        
    }

    public String getSSN() {
        return this.ssn;
    }

    public String getFirstName() {
        return this.firstName;
    }

    public String getLastName() {
        return this.lastName;
    }

    public String getCity() {
        return this.city;
    }

    public String getState() {
        return this.state;
    }

    public String getEmailAddress() {
        return this.emailAddress;
    }

    @Override
    public String toString() {
        return "\nFull Name: " + this.getFirstName() + " " + this.getLastName() + "\nSSN: " + this.getSSN() + "\nCity: " + this.getCity() + "\nState: " + this.getState() + "\nEmail Address: " + this.getEmailAddress();  
    }

}
