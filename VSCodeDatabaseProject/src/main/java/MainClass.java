import java.util.Scanner;

public class MainClass {

    public static void main(String[] args) throws Exception {
        Scanner scan = new Scanner(System.in);
        //Working Timer class instance here
        // PostgreSQLTimerTestClass pgsqltimer = new PostgreSQLTimerTestClass("postgres_research_table_version_2", 
        // "postgres_research_column_of_strings", "jdbc:postgresql://localhost:5432/postgresresearchdatabase", 
        // "postgres", "applepie", "org.postgresql.Driver");

        //Updated for Codio functionality and Postgres Connectivity
        PostgreSQLTimerTestClass pgsqlCodioTimer = new PostgreSQLTimerTestClass("researchTable", 
        "testColumn", "jdbc:postgresql://167.99.123.45:5432/postgres", 
        "postgres", "applepie", "org.postgresql.Driver");

        RecordCatalog recordCatalog = new RecordCatalog(10);

        // Person person = new Person("122345", "Jean", "t", "NY", "NY", "@gmail");
        // System.out.println(person);
        int i = 0;
        while (i < recordCatalog.getRecordCatalogSize()) {
            System.out.println(recordCatalog.getPerson(i));
            i++;
        }
        //pgsqltimer.insertionTest();
        //pgsqltimer.columnStringLoaderForSearchAndDeleteTests(1000);
        //pgsqltimer.searchTest();
        //pgsqltimer.deleteTest();
        

        //itenerary:
        //add an int id field to the Person class



    }
}
