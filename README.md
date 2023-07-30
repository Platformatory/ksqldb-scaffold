A scaffold for creating and testing KSQLDB UDFs.


# Prerequisites

1. JDK 17
2. Gradle
3. docker-compose


# Steps to test

1. Build the UDF jar.

```sh
$ cd sample-udf
$ gradle shadowJar
```

2. Make sure you mount the UDF extensions directory, configure it and give the UDF parameters in the container.


```yaml
  ksqldb-server:
    image: confluentinc/cp-ksqldb-server:7.4.0
    hostname: ksqldb-server
    #container_name: ksqldb-server
    depends_on:
      - broker
      - connect
    ports:
      - "8088:8088"
    environment:
      KSQL_CONFIG_DIR: "/etc/ksql"
      KSQL_BOOTSTRAP_SERVERS: "broker:29092"
      KSQL_HOST_NAME: ksqldb-server
      KSQL_LISTENERS: "http://0.0.0.0:8088"
      KSQL_CACHE_MAX_BYTES_BUFFERING: 0
      KSQL_KSQL_SCHEMA_REGISTRY_URL: "http://schema-registry:8081"
      KSQL_KSQL_CONNECT_URL: "http://connect:8083"
      KSQL_KSQL_LOGGING_PROCESSING_TOPIC_REPLICATION_FACTOR: 1
      KSQL_KSQL_LOGGING_PROCESSING_TOPIC_AUTO_CREATE: 'true'
      KSQL_KSQL_LOGGING_PROCESSING_STREAM_AUTO_CREATE: 'true'
      KSQL_KSQL_EXTENSION_DIR: "/opt/ksqldb-udfs" # <------ extensions dir config
      KSQL_KSQL_FUNCTIONS_FORMULA_BASE_VALUE: 5 # <------ custom UDF parameter
    volumes:
      - "./sample-udf/extensions/:/opt/ksqldb-udfs" # <------- mount the UDF
```

3. Boot the container.

```sh
$ docker-compose up
```


4. Run the KSQLDB console.

```sh
$ docker exec -it ksql-scaffold-ksqldb-cli-1 ksql http://ksqldb-server:8088
```

5. Check if your UDF is loaded.


```sql
ksql> DESCRIBE FUNCTION formula;

Name        : FORMULA
Author      : example user
Version     : 1.0.2
Overview    : A custom formula for important business logic.
Type        : SCALAR
Jar         : /opt/ksqldb-udfs/example-udfs-0.0.1.jar
Variations  :

	Variation   : FORMULA(v1 DOUBLE, v2 DOUBLE)
	Returns     : BIGINT
	Description : A special variant of the formula, handling double parameters.

	Variation   : FORMULA(v1 INT, v2 INT)
	Returns     : BIGINT
	Description : The standard version of the formula with integer parameters.
```

6. Test your UDF.


```sql
SET 'auto.offset.reset' = 'earliest';

CREATE STREAM s1 (
    a VARCHAR KEY,
    b INT,
    c INT
) WITH (
    kafka_topic = 's1',
    partitions = 1,
    value_format = 'avro'
);

INSERT INTO s1 (a, b, c) VALUES ('k1', 2, 3);
INSERT INTO s1 (a, b, c) VALUES ('k2', 4, 6);
INSERT INTO s1 (a, b, c) VALUES ('k3', 6, 9);

SELECT a, formula(b, c) AS result FROM s1 EMIT CHANGES;

+----------------------------------------------------------+----------------------------------------------------------+
|A                                                         |RESULT                                                    |
+----------------------------------------------------------+----------------------------------------------------------+
|k1                                                        |11                                                        |
|k2                                                        |29                                                        |
|k3                                                        |59                                                        |
```

# TODO

1. write junit tests
