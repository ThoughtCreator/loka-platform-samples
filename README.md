# Loka Platform Samples

This repository provides a set of samples to connect to Loka Systems IoT Core API and obtain the messages from your devices.
These simple programs will subscribe a device in IoT Core, open the websocket connection and then print the messages received.
The following demos are provided:
  - Java
  - NodeJS
  - PHP

### Java Sample

This application is built with Maven. You will need to have Java 7 installed and Java API jar (provided in /lib) in your Maven repository. Then, to compile and run the application do:
```sh
$ cd demo-java-iot-core
$ mvn clean package
$ java -jar target/api-demo-1.0.0.jar <server> <authentication token> <device_id>[,<device_id_2>[,<device_id_n>]]
```
The *authentication token* will be provided by Loka Systems team.

### NodeJS Sample

Edit the script and put you *authentication token* and the device to subscribe. Then, to run the application do:
```sh
$ cd demo-nodejs-iot-core
$ node client-loka.js
```

### PHP Sample

Edit the script and put you *authentication token* and the device to subscribe. Then, to run the application do:
```sh
$ cd demo-php-iot-core
$ php demo.php
```

### Support

To get support, just get in touch with us at: [Loka Systems Support](mailto:support@loka-systems.com)
