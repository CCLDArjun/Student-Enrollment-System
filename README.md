# Project Overview
This is a student management system built for the CS-157A databases course at SJSU. Features like enrollment and grade changing are present, as well as separate professor accounts.

# Setup
Either run create_schema.sql, initialize_data.sql, or run Main with runGui as false and regen as true.

## Just Run

Make sure that mysql is running on port 3306 (this is its default port).
```sh
$ java -jar build/libs/StudentEnrollmentSystem-1.0.jar
```

## Build
OPTIONAL

```sh
$ ./gradlew build
$ ./gradlew run
```

# Dependencies
An up-to-date JDK and a mysql server.

# Other
Your mysql server should already be using a db called mydb with a username and password of root.