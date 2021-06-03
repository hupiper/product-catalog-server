## Product Catalog Server

A simple server application in Quarkus as the server-side back-end with a MariaDB Database.

![alt text](https://raw.githubusercontent.com/gnunn1/product-catalog-tools/master/docs/img/screenshot.png)

For complete information about this project, please see the [product-catalog-tools](https://github.com/gnunn1/product-catalog-tools) project.

## Build container image

To build a container image, use the following command from the project root:

```
docker build -t quay.io/gnunn/java-11-runtime:latest -f containers/jvm/Containerfile .
```

To run the image against a local MariaDB database, simply override the default database host parameter with your laptops IP address"

```
docker run --network=host quay.io/gnunn/java-11-runtime:latest
```

You can override the application.properties to connect for specific databases as well, for example:

```
docker run --network=host -e QUARKUS_DATASOURCE_JDBC_URL=jdbc:mariadb://127.0.0.1:3306/productdb quay.io/gnunn/java-11-runtime:latest
```