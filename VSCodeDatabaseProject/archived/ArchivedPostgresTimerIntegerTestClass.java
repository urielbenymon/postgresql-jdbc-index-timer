import java.util.Scanner;
//this class is used with researchdatatable1 in postgres
import javax.naming.spi.DirStateFactory.Result;

//establish the sql java translation link call jdbc (Java Database Connectivity)
//this allows sql commands to be written in java
//had to download the postgresql driver for the jdbc connection: postgresql-42.7.6.jar
import java.sql.*;
import java.util.Random;


public class ArchivedPostgresTimerIntegerTestClass {
    //Connect to the database using the url, user, and password information
    //a connection instance variable was put here to share the connection for all other methods for insertion, search, and delete...
    //...  it can be declared in the main method instead however the insertion, search, and deletion method would then be all in the main method since the...
    //connection would only be declared there
    private static Connection connection;

    //throws Exception on main method instead of writing try/catch statements repeatedly
    public static void main(String[] args) throws Exception {
        Scanner scanner = new Scanner(System.in);

             //the strings below are used to establish a java connection to the database
            //String url = "jdbc:database provider name://servername:portnumber:/nameofyourdatabase"
            String url = "jdbc:postgresql://localhost:5432/postgresresearchdatabase";
            //your database user name
            String user = "postgres";
            //your database password
            String password = "applepie";
            //SQL queries are treated as strings in java       
        //Load the Postgresql driver
        Class.forName("org.postgresql.Driver");

        connection = DriverManager.getConnection(url, user, password);
        connection.setAutoCommit(true);
        System.out.println("Connection Established successfully");
        
        String query = "SELECT version()"; // example of Your SQL query
        //The Statement object sends the sql commands here to the database
        Statement statement = connection.createStatement();
        //The ResultSet object + the while loop are used to read the rows of the database. Each column is a value
        //like reading a text file line for line 
        ResultSet resultSet = statement.executeQuery(query);

        //the iteration here is only used for statements that have the keyword SELECT.
        //Insert, update, delete statments do not require iteration to run.
        while (resultSet.next()) {
            //columns are labed here with quotes: "column1"
            //use getString(""columnname"), getInt(columnname), etc. to get the values in those columns
            System.out.println(resultSet.getString(1));
        }

        while (true) {
            System.out.println("\nType s for search data, i for insert data, d for deletion data, or x to exit the app");
            String testFunction = scanner.nextLine();
            if (testFunction.equals("x")) {
                break;
            } else if (testFunction.equals("s")) {
                clearDatabase(); 
                searchDatabaseDataNOIndex();
                System.out.println();
                searchDatabaseDataWITHIndex();               
                
            } else if (testFunction.equals("i")) {
                clearDatabase();
                insertWithoutIndexForTesting();
                System.out.println();
                insertUsingIndexForTests();
            } else if (testFunction.equals("d")) {
                clearDatabase();
                deleteFromDatabaseWithoutIndex();
                System.out.println();
                deleteFromDatabaseWithIndex();
            } else {
                continue;
            }
        }
        
        clearDatabase();
        //closing statements
        scanner.close();
        statement.close();
        connection.close();
        System.out.println("close");
       
        
           
    }

    //only use ResultSet Objects for select statements
    public static void searchDatabaseDataNOIndex() throws SQLException {
        clearDatabase();
        dropIndexOnTestColumn();
        
        long elementSize = 1;
        int count = 1;
        while (true) {
            clearDatabase();
            insertTestValuesForSearchAndDelete(elementSize);
            if (elementSize > 10000) {
                break;
            }



            long randomSearch = new Random().nextLong(elementSize);
            String sqlCommandForSearch = "SELECT * FROM researchdatatable1 WHERE testvalues = " + randomSearch;
            Statement statement = connection.createStatement();

            long startRecord = System.nanoTime();

            ResultSet rs = statement.executeQuery(sqlCommandForSearch);

            while (rs.next()) {
                //the row retrieval happens starting at ResultSet's declaration here, the print statement below is optional
                //System.out.println(rs.getInt("testvalues"));
            }

            long endRecord = System.nanoTime();
            System.out.print("\nSearch Time No Index (Sequential Search):" + " " + (count++) + " " + (endRecord - startRecord));

            
            elementSize *= 10; 
        }
    }

    public static void searchDatabaseDataWITHIndex() throws SQLException {
        //btree search
        clearDatabase();
        dropIndexOnTestColumn();

        long elementSizeForBTreeSearch = 1;
        int countForBTreeSearch = 1; 
        
        while (true) {
            clearDatabase();
            insertTestValuesForSearchAndDelete(elementSizeForBTreeSearch);
            createIndexOnTestColumn("btree");

            if (elementSizeForBTreeSearch > 10000) {
                clearDatabase();
                dropIndexOnTestColumn();
                break;
            }

            long randomBTreeSearch = new Random().nextLong(elementSizeForBTreeSearch);
            String sqlCommandForBTreeSearch = "SELECT * FROM researchdatatable1 WHERE testvalues = " + randomBTreeSearch;
            Statement statement = connection.createStatement();

            long startRecordBTree = System.nanoTime();

            ResultSet rs = statement.executeQuery(sqlCommandForBTreeSearch);

            while (rs.next()) {
                //the row retrieval happens starting at ResultSet's declaration here, the print statement below is optional
                //System.out.println(rs.getInt("testvalues"));
            }

            long endRecordBTree = System.nanoTime();
            System.out.print("\nBTree Search Time:" + " " + (countForBTreeSearch++) + " " + (endRecordBTree - startRecordBTree));

            
            elementSizeForBTreeSearch *= 10; 
        }

        System.out.println();

        //hash search
        long elementSizeForHashSearch = 1;
        int countForHashSearch = 1;
        clearDatabase();
        dropIndexOnTestColumn();

        while (true) {
            clearDatabase();
            insertTestValuesForSearchAndDelete(elementSizeForHashSearch);
            createIndexOnTestColumn("hash");

            if (elementSizeForHashSearch > 10000) {
                clearDatabase();
                dropIndexOnTestColumn();
                break;
            }

            long randomHashSearch = new Random().nextLong(elementSizeForHashSearch);
            String sqlCommandForHashSearch = "SELECT * FROM researchdatatable1 WHERE testvalues = " + randomHashSearch;

            Statement statement = connection.createStatement();

            long startRecordHashSearch = System.nanoTime();

            ResultSet rs = statement.executeQuery(sqlCommandForHashSearch);

            while (rs.next()) {
                //the row retrieval happens starting at ResultSet's declaration here, the print statement below is optional
                //System.out.println(rs.getInt("testvalues"));
            }

            long endRecordHashSearch = System.nanoTime();
            System.out.print("\nHash Search Time:" + " " + (countForHashSearch++) + " " + (endRecordHashSearch - startRecordHashSearch));

            
            elementSizeForHashSearch *= 10; 
        }

        System.out.println();

        long elementSizeForBRINSearch = 1;
        int countForBRINSearch = 1;
        clearDatabase();
        dropIndexOnTestColumn();

        while (true) {
            clearDatabase();
            insertTestValuesForSearchAndDelete(elementSizeForBRINSearch);
            createIndexOnTestColumn("brin");

            if (elementSizeForBRINSearch > 10000) {
                clearDatabase();
                dropIndexOnTestColumn();
                break;
            }

            long randomBRINSearch = new Random().nextLong(elementSizeForBRINSearch);
            String sqlCommandForBRINSearch = "SELECT * FROM researchdatatable1 WHERE testvalues = " + randomBRINSearch;

            Statement statement = connection.createStatement();

            long startRecordBRINSearch = System.nanoTime();

            ResultSet rs = statement.executeQuery(sqlCommandForBRINSearch);

            while (rs.next()) {
                //the row retrieval happens starting at ResultSet's declaration here, the print statement below is optional
                //System.out.println(rs.getInt("testvalues"));
            }

            long endRecordBRINSearch = System.nanoTime();
            System.out.print("\nBRIN Search Time:" + " " + (countForBRINSearch++) + " " + (endRecordBRINSearch - startRecordBRINSearch));

            
            elementSizeForBRINSearch *= 10; 
        }

    }

    public static void clearDatabase() throws SQLException {
        String clearDatabaseCommand = "TRUNCATE TABLE researchdatatable1 RESTART IDENTITY";
        Statement statement = connection.createStatement();
        statement.execute(clearDatabaseCommand);
        statement.close();
    }

    //makes the column use a B-Tree when searched for
    public static void createIndexOnTestColumn(String dataStructureToggle) throws SQLException {
        //change the column in paraenthesis to target a different column
        //idx_testvalue is the index variable name
        //structurename(column) tags the index to use a specific structure: btree, hash, etc.
        String sql = "CREATE INDEX IF NOT EXISTS idx_testvalues ON researchdatatable1 USING " + dataStructureToggle + "(testvalues)";
        Statement statement = connection.createStatement();
        statement.execute(sql);
        statement.close();

    }

    public static void dropIndexOnTestColumn() throws SQLException {
        String sql = "DROP INDEX IF EXISTS idx_testvalues";
        Statement statement = connection.createStatement();
        statement.execute(sql);
        statement.close();

    }

    //this method is for inserting values for runtime tests (w/o indexes)
    public static void insertWithoutIndexForTesting() throws SQLException {
        clearDatabase();
        dropIndexOnTestColumn();
        String sql = "INSERT INTO researchdatatable1 (testvalues) VALUES (?)";
        PreparedStatement pstmt = connection.prepareStatement(sql);
        long size = 1;
        long elements = 1;
        int count = 1;
        //Java runs out of memory at 10 million mark (past the 8th time)
        while (size <= 10000000) {
            clearDatabase();
            while (elements <= size) {
                pstmt.setLong(1, elements++);
                pstmt.addBatch();
            }

            long startInsertionTimeNoIndex = System.nanoTime();
            //this line (.executeBatch()) is where the insertion happens on the database side
            pstmt.executeBatch();
            long endInsertionTimeNoIndex = System.nanoTime();
            System.out.print("\nInsertion Time No Index (Sequential): " + (count++) + " " + (endInsertionTimeNoIndex - startInsertionTimeNoIndex));
            //pstmt.close();
            size *= 10;
        }
    }

    public static void insertUsingIndexForTests() throws SQLException {
        //btree insert
        clearDatabase();
        dropIndexOnTestColumn();
        createIndexOnTestColumn("btree");

        String sqlQueryBTree = "INSERT INTO researchdatatable1 (testvalues) VALUES (?)";
        PreparedStatement pstmtBTree = connection.prepareStatement(sqlQueryBTree);
        long sizeBTree = 1;
        long elementsBTree = 1;
        int countBTree = 1;
        //10 million
        while (sizeBTree <= 10000000) {
            clearDatabase();
            while (elementsBTree <= sizeBTree) {
                pstmtBTree.setLong(1, elementsBTree++);
                pstmtBTree.addBatch();
            }

            long startInsertionTimeBTree = System.nanoTime();
            pstmtBTree.executeBatch();
            long endInsertionTimeBTree= System.nanoTime();
            System.out.print("\nBTree Insertion Time: " + (countBTree++) + " " + (endInsertionTimeBTree - startInsertionTimeBTree));
            //pstmt.close();
            sizeBTree *= 10;
        }

        //hash insert
        clearDatabase();
        dropIndexOnTestColumn();
        createIndexOnTestColumn("hash");
        System.out.println();

        String sqlQueryHash = "INSERT INTO researchdatatable1 (testvalues) VALUES (?)";
        PreparedStatement pstmtHash = connection.prepareStatement(sqlQueryHash);
        long sizeHash = 1;
        long elementsHash = 1;
        int countHash = 1;

        while (sizeHash <= 10000000) {
            clearDatabase();
            while (elementsHash <= sizeHash) {
                pstmtHash.setLong(1, elementsHash++);
                pstmtHash.addBatch();
            }

            long startInsertionTimeHash = System.nanoTime();
            pstmtHash.executeBatch();
            long endInsertionTimeHash = System.nanoTime();
            System.out.print("\nHash Insertion Time: " + (countHash++) + " " + (endInsertionTimeHash - startInsertionTimeHash));
            //pstmt.close();
            sizeHash *= 10;
        }

        //BRIN insert
        clearDatabase();
        dropIndexOnTestColumn();
        createIndexOnTestColumn("brin");
        System.out.println();

        String sqlQueryBRIN = "INSERT INTO researchdatatable1 (testvalues) VALUES (?)";
        PreparedStatement pstmtBRIN = connection.prepareStatement(sqlQueryBRIN);
        long sizeBRIN = 1;
        long elementsBRIN = 1;
        int countBRIN = 1;

        //10 million
        while (sizeBRIN <= 10000000) {
            clearDatabase();
            while (elementsBRIN <= sizeBRIN) {
                pstmtBRIN.setLong(1, elementsBRIN++);
                pstmtBRIN.addBatch();
            }
            
            long startInsertionTimeBRIN = System.nanoTime();
            pstmtBRIN.executeBatch();
            long endInsertionTimeBRIN = System.nanoTime();
            System.out.print("\nBRIN Insertion Time: " + (countBRIN++) + " " + (endInsertionTimeBRIN - startInsertionTimeBRIN));
            //pstmt.close();
            sizeBRIN *= 10;
        }


    }



    //This method is for inserting values for search and deletion tests
    public static void insertTestValuesForSearchAndDelete(long amountToInsert) throws SQLException {
        String insertsql = "INSERT INTO researchdatatable1 (testvalues) VALUES (?)";
        PreparedStatement pstmt = connection.prepareStatement(insertsql);
        for (long i = 1; i <= amountToInsert; i++) {
            pstmt.setLong(1, i);
            pstmt.addBatch();
            pstmt.executeBatch();
            
        }

        pstmt.close();

    }

    public static void deleteFromDatabaseWithoutIndex() throws SQLException{
        clearDatabase();
        dropIndexOnTestColumn();
        long elementSizeForDelete = 1;
        int count = 1;
        while (true) {
            clearDatabase();
            insertTestValuesForSearchAndDelete(elementSizeForDelete);
            if (elementSizeForDelete > 10000) {
                break;
            }

            long randomDelete = new Random().nextLong(elementSizeForDelete);
            String sqlDelete = "DELETE FROM researchdatatable1 WHERE testvalues = ?";
            PreparedStatement pstmt = connection.prepareStatement(sqlDelete);
            pstmt.setLong(1, randomDelete);
            long startTimeForDeleteNoIndex = System.nanoTime();
            pstmt.executeUpdate();
            long endTimeForDeleteNoIndex = System.nanoTime();

            System.out.println("Delete time without index (Sequential) " + (count++) + ": " + (endTimeForDeleteNoIndex - startTimeForDeleteNoIndex));
            elementSizeForDelete *= 10;
        }

    }

    public static void deleteFromDatabaseWithIndex() throws SQLException {
        //btree delete
        clearDatabase();
        dropIndexOnTestColumn();
        createIndexOnTestColumn("btree");
       
        long elementSizeForDeleteBTree = 1;
        int countBTree = 1;
        while (true) {
            clearDatabase();
            insertTestValuesForSearchAndDelete(elementSizeForDeleteBTree);
            if (elementSizeForDeleteBTree > 10000) {
                break;
            }

            long randomDeleteBTree = new Random().nextLong(elementSizeForDeleteBTree);
            String sqlDeleteBTree = "DELETE FROM researchdatatable1 WHERE testvalues = ?";
            PreparedStatement pstmtBTree = connection.prepareStatement(sqlDeleteBTree);
            pstmtBTree.setLong(1, randomDeleteBTree);
            long startTimeForDeleteBTree = System.nanoTime();
            pstmtBTree.executeUpdate();
            long endTimeForDeleteBTree = System.nanoTime();

            System.out.print("\nBTree Delete Time " + (countBTree++) + ": " + (endTimeForDeleteBTree- startTimeForDeleteBTree));
            elementSizeForDeleteBTree *= 10;
        }


         //hash delete
        clearDatabase();
        dropIndexOnTestColumn();
        createIndexOnTestColumn("hash");
        System.out.println();
       
        long elementSizeForDeleteHash = 1;
        int countHash = 1;
        while (true) {
            clearDatabase();
            insertTestValuesForSearchAndDelete(elementSizeForDeleteHash);
            if (elementSizeForDeleteHash > 10000) {
                break;
            }

            long randomDeleteHash = new Random().nextLong(elementSizeForDeleteHash);
            String sqlDeleteHash = "DELETE FROM researchdatatable1 WHERE testvalues = ?";
            PreparedStatement pstmtHash = connection.prepareStatement(sqlDeleteHash);
            pstmtHash.setLong(1, randomDeleteHash);
            long startTimeForDeleteHash = System.nanoTime();
            pstmtHash.executeUpdate();
            long endTimeForDeleteHash = System.nanoTime();

            System.out.print("\nHash Delete Time " + (countHash++) + ": " + (endTimeForDeleteHash - startTimeForDeleteHash));
            elementSizeForDeleteHash *= 10;
        }

        //brin delete
        clearDatabase();
        dropIndexOnTestColumn();
        createIndexOnTestColumn("brin");
        System.out.println();
       
        long elementSizeForDeleteBRIN = 1;
        int countBRIN = 1;
        while (true) {
            clearDatabase();
            insertTestValuesForSearchAndDelete(elementSizeForDeleteBRIN);
            if (elementSizeForDeleteBRIN > 10000) {
                break;
            }

            long randomDeleteBRIN = new Random().nextLong(elementSizeForDeleteBRIN);
            String sqlDeleteBRIN = "DELETE FROM researchdatatable1 WHERE testvalues = ?";
            PreparedStatement pstmtBRIN = connection.prepareStatement(sqlDeleteBRIN);
            pstmtBRIN.setLong(1, randomDeleteBRIN);
            long startTimeForDeleteBRIN = System.nanoTime();
            pstmtBRIN.executeUpdate();
            long endTimeForDeleteBRIN = System.nanoTime();

            System.out.print("\nBRIN Delete Time " + (countBRIN++) + ": " + (endTimeForDeleteBRIN- startTimeForDeleteBRIN));
            elementSizeForDeleteBRIN *= 10;
        }

    }




}
