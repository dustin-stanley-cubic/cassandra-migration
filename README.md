# Cassandra Migration

`cassandra-migration` is a simple and lightweight Apache Cassandra database schema migration tool.

It is a Kotlin fork of [Contrast Security's Cassandra Migration] project, which has been manually re-based to closely follow [Axel Fontaine / BoxFuse Flyway] project.
 
It is designed to work similar to Flyway, supporting plain CQL and Java-based migrations. Both CQL execution and Java migration interface uses [DataStax's Cassandra Java driver].

## Getting Started

Ensure the following prerequisites are met: 

 * Java SDK 1.7+ (tested with Azul Zulu JDK 1.7.0_111 and 1.8.0_102)
 * Apache Cassandra 3.0.x (tested with DataStax Enterprise Community 3.0.x)
 * Pre-existing Keyspace
 
Import this library as a dependency (Maven example):
``` xml
<dependency>
    <groupId>com.builtamont</groupId>
    <artifactId>cassandra-migration</artifactId>
    <version>0.9-SNAPSHOT</version>
</dependency>
```

*NOTE: Cassandra's Keyspace should be managed outside the migration tool by sysadmins (e.g. tune replication factor, etc)*

### Migration version table

``` shell
cassandra@cqlsh:cassandra_migration_test> select * from cassandra_migration_version;
 type        | version | checksum    | description    | execution_time | installed_by | installed_on             | installed_rank | script                                 | success | version_rank
-------------+---------+-------------+----------------+----------------+--------------+--------------------------+----------------+----------------------------------------+---------+--------------
         CQL |   1.0.0 |   985950023 |          First |             88 |    cassandra | 2015-09-12 15:10:22-0400 |              1 |                      V1_0_0__First.cql |    True |            1
         CQL |   1.1.2 |  2095193138 |  Late arrival2 |              3 |    cassandra | 2015-09-12 15:10:23-0400 |              5 |              V1_1_2__Late_arrival2.cql |    True |            2
         CQL |   1.1.3 | -1648933960 |  Late arrival3 |             15 |    cassandra | 2015-09-12 15:10:23-0400 |              6 |              V1_1_3__Late_arrival3.cql |    True |            3
         CQL |   2.0.0 |  1899485431 |         Second |            154 |    cassandra | 2015-09-12 15:10:22-0400 |              2 |                     V2_0_0__Second.cql |    True |            4
 JAVA_DRIVER |     3.0 |        null |          Third |              3 |    cassandra | 2015-09-12 15:10:22-0400 |              3 |            migration.integ.V3_0__Third |    True |            5
 JAVA_DRIVER |   3.0.1 |        null | Three zero one |              2 |    cassandra | 2015-09-12 15:10:22-0400 |              4 | migration.integ.V3_0_1__Three_zero_one |    True |            6
```

### Supported Migration Script Types

#### .cql files

Example:
``` sql
CREATE TABLE test1 (
  space text,
  key text,
  value text,
  PRIMARY KEY (space, key)
) with CLUSTERING ORDER BY (key ASC);

INSERT INTO test1 (space, key, value) VALUES ('foo', 'blah', 'meh');

UPDATE test1 SET value = 'profit!' WHERE space = 'foo' AND key = 'blah';
```

#### Java classes

Example:
``` java
public class V3_0__Third implements JavaMigration {

    @Override
    public void migrate(Session session) throws Exception {
        Insert insert = QueryBuilder.insertInto("test1");
        insert.value("space", "web");
        insert.value("key", "google");
        insert.value("value", "google.com");

        session.execute(insert);
    }
}
```

### Interface

#### Java API

Example:
``` java
String[] scriptsLocations = {"migration/cassandra"};

Keyspace keyspace = new Keyspace();
keyspace.setName(CASSANDRA__KEYSPACE);
keyspace.getCluster().setContactpoints(CASSANDRA_CONTACT_POINT);
keyspace.getCluster().setPort(CASSANDRA_PORT);
keyspace.getCluster().setUsername(CASSANDRA_USERNAME);
keyspace.getCluster().setPassword(CASSANDRA_PASSWORD);

CassandraMigration cm = new CassandraMigration();
cm.getConfigs().setScriptsLocations(scriptsLocations);
cm.setKeyspace(keyspace);
cm.migrate();
```

#### Command line

``` shell
java -jar \
-Dcassandra.migration.scripts.locations=file:target/test-classes/migration/integ \
-Dcassandra.migration.cluster.contactpoints=localhost \
-Dcassandra.migration.cluster.port=9147 \
-Dcassandra.migration.cluster.username=cassandra \
-Dcassandra.migration.cluster.password=cassandra \
-Dcassandra.migration.keyspace.name=cassandra_migration_test \
target/*-jar-with-dependencies.jar migrate
```

Logging level can be set by passing the following arguments:
 * INFO: This is the default
 * DEBUG: '-X'
 * WARNING: '-q'

### VM Options

Options can be set either programmatically with API or via Java VM options.

Migration:
 * `cassandra.migration.scripts.locations`: Locations of the migration scripts in CSV format. Scripts are scanned in the specified folder recursively. (default=db/migration)
 * `cassandra.migration.scripts.encoding`: The encoding of CQL scripts (default=UTF-8)
 * `cassandra.migration.scripts.allowoutoforder`: Allow out of order migration (default=false)
 * `cassandra.migration.version.target`: The target version. Migrations with a higher version number will be ignored. (default=latest)

Cluster:
 * `cassandra.migration.cluster.contactpoints`: Comma separated values of node IP addresses (default=localhost)
 * `cassandra.migration.cluster.port`: CQL native transport port (default=9042)
 * `cassandra.migration.cluster.username`: Username for password authenticator (optional)
 * `cassandra.migration.cluster.password`: Password for password authenticator (optional)

Keyspace:
 * `cassandra.migration.keyspace.name`: Name of Cassandra keyspace (required)

### Cluster Coordination

 * Schema version tracking statements use `ConsistencyLevel.ALL`
 * Users should manage their own consistency level in the migration scripts

### Limitations

 * Baselining not supported yet
 * The tool does not roll back the database upon migration failure. You're expected to manually restore backup.

## Project Rationale

**Why not create an extension to an existing popular database migration project (i.e. Flyway)?**
Popular database migration tools (e.g. Flyway, Liquibase) are tailored for relational databases with JDBC. This project exists due to the need for Cassandra-specific tooling:
 * CQL != SQL,
 * Cassandra is distributed and does not have ACID transactions,
 * Cassandra do not have native JDBC driver(s),
 * etc.

**Why not extend the existing Cassandra Migration project?**
The existing project does not seem to be actively maintained. Several important PRs have been left open for months and there was an immediate need to support Cassandra driver v3.x. 

**Why Kotlin?**
There are various reasons why Kotlin was chosen, but three main reasons are:
 * code brevity and reduced noise,
 * stronger `null` checks (enforced at the compiler level), and
 * better Java collection support (e.g. additional functional features)

## Contributing

We follow the "[fork-and-pull]" Git workflow.

  1. Fork the repo on GitHub
  1. Commit changes to a branch in your fork (use `snake_case` convention):
     - For technical chores, use `chore/` prefix followed by the short description, e.g. `chore/do_this_chore`
     - For new features, use `feature/` prefix followed by the feature name, e.g. `feature/feature_name`
     - For bug fixes, use `bug/` prefix followed by the short description, e.g. `bug/fix_this_bug`
  1. Rebase or merge from "upstream"
  1. Submit a PR "upstream" with your changes

Please read [CONTRIBUTING] for more details.

## License

`cassandra-migration` is released under the Apache 2 license. See the [LICENSE] file for further details.
 
[Contrast Security's Cassandra Migration] project is released under the Apache 2 license. See [Contrast Security Cassandra Migration project license page] for further details.

[Flyway] project is released under the Apache 2 license. See [Flyway's project license page] for further details.

## Releases

https://github.com/builtamont/cassandra-migration/releases

[Axel Fontaine / BoxFuse Flyway]: https://github.com/flyway/flyway
[Contrast Security's Cassandra Migration]: https://github.com/Contrast-Security-OSS/cassandra-migration
[Contrast Security Cassandra Migration project license page]: https://github.com/Contrast-Security-OSS/cassandra-migration/blob/master/LICENSE
[CONTRIBUTING]: CONTRIBUTING.md
[DataStax's Cassandra Java driver]: http://datastax.github.io/java-driver/
[Flyway]: https://flywaydb.org/
[Flyway's project license page]: https://github.com/flyway/flyway/blob/master/LICENSE
[fork-and-pull]: https://help.github.com/articles/using-pull-requests
[LICENSE]: LICENSE