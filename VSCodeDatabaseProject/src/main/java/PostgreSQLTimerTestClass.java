import java.sql.*;
import java.util.Random;

public class PostgreSQLTimerTestClass {

    private String tableName = "";
    private String columnName = "";
    private static Connection connection;
    private String url = "";
    private String username = "";
    private String password = "";
    private String driver = "";
    private String[] indexStructureNamesArray = {"btree", "hash", "gist", "gin", "brin", "spgist", "sequential (no index)"};
    private String[] searchOperatorArray = {"=", ">", "<", ">=", "<="};// "BETWEEN", "LIKE"};

    

    public PostgreSQLTimerTestClass(String tableName, String columnName, String url, String username, String password, String driver) throws Exception {
        this.tableName = tableName;
        this.columnName = columnName;
        this.url = url;
        this.username = username;
        this.password = password;
        connection = DriverManager.getConnection(url, username, password);
        connection.setAutoCommit(true);
        clearDatabase();
        dropIndex();
        System.out.println("Connection Established successfully");

        Statement statement = connection.createStatement();
        //to use gist for text/Strings
        statement.execute("CREATE EXTENSION IF NOT EXISTS btree_gist");
        System.out.println("GiST index structure with BTree extension for String compatibility is live");
        statement.close();
        
    }

    public void printTableName() throws SQLException {
        System.out.println(tableName);
    }

    public void clearDatabase() throws SQLException {
        String clearDatabaseSQLCommand = "TRUNCATE TABLE " + this.tableName + " RESTART IDENTITY";
        Statement statement = connection.createStatement();
        statement.execute(clearDatabaseSQLCommand);
        statement.close();
    }

    public void columnStringLoaderForSearchAndDeleteTests(int rowLimit) throws SQLException {
        clearDatabase();
        dropIndex();
        PreparedStatement pstmt = connection.prepareStatement("INSERT INTO " + this.tableName + "(" + this.columnName + ") VALUES (?)");
        //1 string entry loader count
        int countUpToTheLimit = 0;
        while (countUpToTheLimit < rowLimit) {
             String fillerValue = "johnDoe" + String.format("%09d", countUpToTheLimit++);
            pstmt.setString(1, fillerValue);
                //visibility statement
            //System.out.println("johnDoe" + countUpToTheLimit);
                //adds sql commands to a queue
            pstmt.addBatch();
                //INSERT INTO postgres_research_table_version_2 (postgres_research_column_of_strings) VALUES ('San Francisco');
        }
        
        //executes sql commands from addBatch in queue order
        pstmt.executeBatch();
    }

    //index management

    //creates an index on the column passed in
    //SQL command for creating indices: CREATE INDEX index_name ON table_name (column_name);
    public void createIndex(String indexStructureToggleName) throws SQLException {
        /*index structure names:
         * btree, hash, gist, gin, brin, spgist
         */
        if (indexStructureToggleName.equals("gist")) {
            String createGiSTIndexSQLCommand = "CREATE INDEX index_variable ON " + this.tableName + " USING " + indexStructureToggleName + "(" + this.columnName + " gist_text_ops)";
            Statement statement = connection.createStatement();
            statement.execute(createGiSTIndexSQLCommand);
            statement.close();
        } else if (indexStructureToggleName.equals("gin")) {
            String createGINIndexSQLCommand = "CREATE INDEX index_variable ON " + this.tableName + " USING " + indexStructureToggleName + "(to_tsvector('english', " + this.columnName + "))";
            Statement statement = connection.createStatement();
            statement.execute(createGINIndexSQLCommand);
            statement.close();
        } else if (indexStructureToggleName.equals("sequential (no index)")){
            dropIndex();
        } else { 
            String createNormalIndexSQLCommand = "CREATE INDEX index_variable ON " + this.tableName + " USING " + indexStructureToggleName + "(" + this.columnName + ")";
            Statement statement = connection.createStatement();
            statement.execute(createNormalIndexSQLCommand);
            statement.close();
        }

        
    }

    public void dropIndex() throws SQLException {
        String dropIndexSQLCommand = "DROP INDEX IF EXISTS index_variable";
        Statement statement = connection.createStatement();
        statement.execute(dropIndexSQLCommand);
        statement.close();
    }

    public void refreshIndex(String indexStructureToggleName) throws SQLException {
        dropIndex();
        createIndex(indexStructureToggleName);
    }

//     public void explainSelectPlan(String indexStructure, String valueToSearch) throws SQLException {
//     refreshIndex(indexStructure);
//     String explainSQL = "EXPLAIN ANALYZE SELECT * FROM " + this.tableName + 
//                         " WHERE " + this.columnName + " = '" + valueToSearch + "'";
//     Statement stmt = connection.createStatement();
//     ResultSet rs = stmt.executeQuery(explainSQL);

//     System.out.println("\n--- " + indexStructure + " EXPLAIN ANALYZE SELECT PLAN ---");
//     while (rs.next()) {
//         System.out.println(rs.getString(1));
//     }
//     rs.close();
//     stmt.close();
// }
// public void explainDeletePlan(String indexStructure, String valueToDelete) throws SQLException {
//     refreshIndex(indexStructure);
//     String explainSQL = "EXPLAIN ANALYZE DELETE FROM " + this.tableName + 
//                         " WHERE " + this.columnName + " = '" + valueToDelete + "'";
//     Statement stmt = connection.createStatement();
//     ResultSet rs = stmt.executeQuery(explainSQL);

//     System.out.println("\n--- " + indexStructure + " EXPLAIN ANALYZE DELETE PLAN ---");
//     while (rs.next()) {
//         System.out.println(rs.getString(1));
//     }
//     rs.close();
//     stmt.close();
// }

    public void insertionTest() throws SQLException {
        PreparedStatement pstmt = connection.prepareStatement("INSERT INTO " + this.tableName + "(" + this.columnName + ") VALUES (?)");

        for (int i = 0; i <= indexStructureNamesArray.length - 1; i++) {
            clearDatabase();
            refreshIndex(indexStructureNamesArray[i]);
            System.out.println();
            
            int limit = 1;
            //limit set to 1 million elements
            while (limit <= 1000) {
                clearDatabase();
                int countUpToTheLimit = 0;
                while (countUpToTheLimit < limit) {
                    String testValue = "johnDoe" + String.format("%07d", countUpToTheLimit++);
                    pstmt.setString(1, testValue);
                    //System.out.println(testValue);
                    pstmt.addBatch();
                }
                
                long startTime = System.nanoTime();
                pstmt.executeBatch();
                long endTime = System.nanoTime();

                long elapsedTimeNanoseconds = (endTime - startTime);
                double elapsedTimeMilliseconds = (endTime - startTime) / 1000000;
                System.out.println(indexStructureNamesArray[i] + " Insertion Time with " + limit + " elements inserted:" + "\n  -Time in Nanoseconds " + elapsedTimeNanoseconds + "\n  -Time in Milliseconds " + elapsedTimeMilliseconds);
                
                limit *= 10;
            }
            
        }
        
    }


   
    //redo tomar: for each symbol, then for each structure, then for each LIMIT
    public void searchTest() throws SQLException {
        clearDatabase();
        dropIndex();


        Random rand = new Random(42);
        int limit = 1;

        while (limit <= 10000) {
            columnStringLoaderForSearchAndDeleteTests(limit);
            int searchRNGNumber = rand.nextInt(limit);
            String searchValue = "johnDoe" + String.format("%09d", searchRNGNumber);

            //System.out.println(searchNumber);


            for (String symbol : searchOperatorArray) {
                System.out.println(symbol + " with limit: " + limit);

                StringBuilder sb;
                
                for (String indexStructure : indexStructureNamesArray) {
                    
                    refreshIndex(indexStructure);
                    //mimics the equal symbol search operator which gin does not have access to
                    if ((indexStructure.equals("gin") || indexStructure.equals("hash")) && !(symbol.equals("="))) {
                        continue;
                    }  else if (indexStructure.equals("gin") && (symbol.equals("="))) {
                        //hash index structures only support "=" for searches
                        sb = new StringBuilder("SELECT * FROM " + this.tableName + " WHERE to_tsvector('english', " + this.columnName + ") @@ to_tsquery(?)");
                    } else {
                        sb = new StringBuilder("SELECT * FROM " + this.tableName + " WHERE " + this.columnName + " " + symbol + " ?");
                    }


                    PreparedStatement ps = connection.prepareStatement(sb.toString());
                    ps.setString(1, searchValue);
                    //the search occurs here and is timed
                    long startTime = System.nanoTime();
                    ResultSet rs = ps.executeQuery();
                    while (rs.next()) {}
                    long endTime = System.nanoTime();
                    long elapsedTimeNanoseconds = (endTime - startTime);
                    double elapsedTimeMilliseconds = elapsedTimeNanoseconds / 1000000;

                    ps.close();

                    if (indexStructure.equals("gin") && (symbol.equals("="))) {
                        System.out.println("Search time information for a limit of " + limit + ": Index Structure: " + indexStructure + ", Symbol: GIN equality subsitute" + ", Random Search Value " + searchValue + ", Nanoseconds " + elapsedTimeNanoseconds + ", Milliseconds " + elapsedTimeMilliseconds);
                    } else {
                        System.out.println("Search time information for a limit of " + limit + ": Index Structure: " + indexStructure + ", Symbol: " + symbol + ", Random Search Value " + searchValue + ", Nanoseconds " + elapsedTimeNanoseconds + ", Milliseconds " + elapsedTimeMilliseconds);
                    }
        //             //for checking which index is used
        //             if (symbol.equals("=")) {
        //     explainSelectPlan(indexStructure, searchValue);
        // }
                }


            }
                
            limit *= 10;
        }        
    }


    public void deleteTest() throws SQLException {

        clearDatabase();
        dropIndex();

        Random randomSeed = new Random(50);
        int limit = 1;


        while (limit <= 1000000) {

            int rngNumberForDeletion = randomSeed.nextInt(limit);
            String valueToBeDeleted = "johnDoe" + String.format("%09d", rngNumberForDeletion);

            for (String symbol : searchOperatorArray) {

                StringBuilder sb;

                for (String indexStructure : indexStructureNamesArray) {

                    refreshIndex(indexStructure);
                    clearDatabase();
                    columnStringLoaderForSearchAndDeleteTests(limit);

                    //mimics the equal symbol search operator which gin does not have access to
                    if ((indexStructure.equals("gin") || indexStructure.equals("hash")) && !(symbol.equals("="))) {
                        continue;
                    }  else if (indexStructure.equals("gin") && (symbol.equals("="))) {
                        //hash index structures only support "=" for searches
                        sb = new StringBuilder("DELETE FROM " + this.tableName + " WHERE to_tsvector('english', " + this.columnName + ") @@ to_tsquery(?)");
                    } else {
                        sb = new StringBuilder("DELETE FROM " + this.tableName + " WHERE " + this.columnName + " " + symbol + " ?");
                    }

                    PreparedStatement p = connection.prepareStatement(sb.toString());
                    p.setString(1, valueToBeDeleted);

                    long startTime = System.nanoTime();
                    //the delete happens here and is timed
                    int numberOfRowsDeleted = p.executeUpdate();
                    long endTime = System.nanoTime();
                    long elapsedTimeNanoseconds = (endTime - startTime);
                    double elapsedTimeMilliseconds = elapsedTimeNanoseconds / 1000000;
                    p.close();

                    if (indexStructure.equals("gin") && (symbol.equals("="))) {
                        System.out.println("Delete time information for a limit of " + limit + ": Index Structure: " + indexStructure + ", Symbol: GIN equality subsitute" + ", Random Deleted Value: " + valueToBeDeleted + " row(s) affected " + numberOfRowsDeleted + ", Nanoseconds " + elapsedTimeNanoseconds + ", Milliseconds " + elapsedTimeMilliseconds);
                    } else {
                        System.out.println("Delete time information for a limit of " + limit + ": Index Structure: " + indexStructure + ", Symbol: " + symbol + ", Random Deleted Value: " + valueToBeDeleted + " row(s) affected " + numberOfRowsDeleted + ", Nanoseconds " + elapsedTimeNanoseconds + ", Milliseconds " + elapsedTimeMilliseconds);
                    }

        //             //for checking which index is used
        //             if (symbol.equals("=")) {
        //     explainDeletePlan(indexStructure, valueToBeDeleted);
        // }
                }

                
                System.out.println();
            }

            limit *= 10;
        }
    }

    

}
