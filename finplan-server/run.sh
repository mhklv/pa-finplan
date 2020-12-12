# -dbaddr localhost -dbname finplandb -dbpass 123 -dbusername finplanuser -port 33333
mvn exec:java -Dexec.args="$*"
