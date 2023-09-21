# purchase-transaction
Purchase Transaction - Technical Assessment - Software Development Engineer - WEX

# How to build  
After you clone the repository from github, you cam run the folowing docker command inside the root directory of the project:
  docker-compose up 
  - and to stop docker-compose down

You can also build the application with the following commands inside the root directory of the project:
- mvn clean package
- java -jar target/purchase-transaction-0.0.1-SNAPSHOT.jar
- To stop CRLT+C

# How to execute
You can use a tool like Postman to execute the url to test the apis.
Inside the root directory you can import the file WEX.postman_collection.json that contains 3 request examples, one to save a transaction, other to get the transaction by Id
and another to get the exchange information based on Currency-Country specific.
