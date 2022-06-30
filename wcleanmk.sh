#mvn -T 1C compile install
./mvnw clean && ./mvnw -Dmaven.test.skip=true -T 1C compile install

