# PostgreSQL JDBC Index Timer

A Java/JDBC benchmarking harness that times how PostgreSQL's built-in index types perform on inserts, searches, and deletes as a table grows.

> **About this repo:** This was my first project on GitHub, written during the RISE research program at Columbia University (Apr–Jul 2025, advised by Prof. Paul Blaer). The code is kept as it was originally written; this README documents what it does and its limitations.

## What it does

For each index type below, the harness builds a table of synthetic string values (`johnDoe000000001`, `johnDoe000000002`, …), creates the index, runs timed operations at table sizes that grow by 10× per step, and prints the elapsed time in nanoseconds and milliseconds.

**Index types compared**

| Index | Notes |
|---|---|
| B-tree | PostgreSQL's default index |
| Hash | Equality (`=`) only |
| GiST | Uses the `btree_gist` extension (`gist_text_ops`) so it can index text |
| GIN | Indexed on `to_tsvector('english', column)`; tested with a full-text match in place of `=` |
| BRIN | Block range index |
| SP-GiST | Space-partitioned GiST |
| None | Sequential scan baseline |

**Operations timed**

| Test | Method | Table sizes | Operators |
|---|---|---|---|
| Insert | `insertionTest()` | 1 → 1,000 rows | Batch `INSERT` |
| Search | `searchTest()` | 1 → 10,000 rows | `=`, `>`, `<`, `>=`, `<=` |
| Delete | `deleteTest()` | 1 → 1,000,000 rows | `=`, `>`, `<`, `>=`, `<=` |

Hash and GIN are only run with equality, since they don't support range operators. Search and delete targets are chosen with a fixed random seed, so runs are repeatable. Across all three tests the harness performs roughly 350 timed operations.

## Repository layout

```
VSCodeDatabaseProject/
├── src/main/java/
│   ├── PostgreSQLTimerTestClass.java   # The benchmark: index setup, data loading, timed tests
│   ├── MainClass.java                  # Entry point and connection settings
│   ├── RecordCatalog.java, Person.java # Synthetic-record generator (Java Faker); not used by the timer
│   └── App.java                        # VS Code template stub
├── archived/
│   └── ArchivedPostgresTimerIntegerTestClass.java  # Earlier version: integer column; B-tree, hash, BRIN only
├── queries/                            # Connection-test SQL
├── src/SQLcommandsforreferencefortheproject.sql    # Reference SQL used while developing
└── pom.xml                             # Maven build (PostgreSQL JDBC driver, Java Faker)
```

## Running it

**Requirements:** Java, Maven, and a PostgreSQL server you can connect to.

1. Create a table with a text column, for example:

   ```sql
   CREATE TABLE researchTable (
       id SERIAL PRIMARY KEY,
       testColumn TEXT
   );
   ```

2. In `MainClass.java`, set the table name, column name, JDBC URL, username, and password passed to `PostgreSQLTimerTestClass` to match your own database.
3. Uncomment the test you want to run (`insertionTest()`, `searchTest()`, or `deleteTest()`). As committed, `MainClass` only prints a sample of generated records.
4. From `VSCodeDatabaseProject/`, run:

   ```bash
   mvn compile exec:java
   ```

Results print to the console.

## Limitations

This was an exploratory first project, and the measurements should be read with these caveats:

- **Single runs.** Each configuration is timed once, with no warm-up runs or repetitions, so results are sensitive to caching and system noise.
- **Timing scope.** Timings wrap the JDBC call, so they include driver and network overhead, not just index work.
- **Millisecond values are truncated** by integer division; the nanosecond values are the accurate ones.
- **GIN isn't a like-for-like comparison.** It's tested with a full-text match, not true string equality.
- **Index use isn't verified.** On small tables, PostgreSQL's planner may choose a sequential scan even when an index exists. The `EXPLAIN ANALYZE` helpers that would confirm this are present but commented out.
- **Console output only.** Results aren't saved to a file for analysis.

## What came next

This harness was the starting point for my RISE work on B-tree index behavior in PostgreSQL, presented as the poster *Exploring Improvements to B-Tree Index Structures for Database Optimization* at the Columbia Undergraduate Research Symposium (2025) and the Columbia GS Research Festival (2026).
