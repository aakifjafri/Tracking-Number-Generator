# Tracking-Number-Generator



**Setup & Run Locally**
1. Clone the repo
git clone https://github.com/<your-username>/tracking-generator.git
cd tracking-generator

2. Start Redis

Run Redis locally with Docker:

docker run -d --name local-redis -p 6379:6379 redis:7

3. Build the JAR
mvn clean package -DskipTests

4. Run the app
java -jar target/tracking-generator-0.0.1-SNAPSHOT.jar



****cURL to access this
curl --location --request GET 'https://localhost:8085/next-tracking-number?origin_country_id=MY&destination_country_id=ID&weight=1.234&created_at=2018-11-20T19:29:32+08:00&customer_id=de619854-b59b-425e-9db4-943979e1bd49&customer_name=RedBox%20Logistics&customer_slug=redbox-logistics' \
--header 'Accept: application/json'



 --------------- or ----------------------

**Run with Docker**
1. Build Docker image
docker build -t tracking-generator .

2. Run container (with local Redis)
docker run -p 8080:8080 \
  -e REDIS_URL=redis://host.docker.internal:6379 \
  tracking-generator

3. Test endpoint
curl "http://localhost:8080/next-tracking-number?origin_country_id=MY&destination_country_id=ID&weight=1.234&created_at=2018-11-20T19:29:32+08:00&customer_id=de619854-b59b-425e-9db4-943979e1bd49&customer_name=RedBox%20Logistics&customer_slug=redbox-logistics" \
-H "Accept: application/json"


Response:

{
  "tracking_number": "01AB23CD45EF67GH",
  "created_at": "2025-09-18T15:45:12.321Z"
}



**I Have Deployed the changes on Railway, You can Access by below give API or cURL**

**Url to access the response**

https://tracking-generator-production.up.railway.app/next-tracking-number?origin_country_id=MY&destination_country_id=ID&weight=1.234&created_at=2018-11-20T19:29:32+08:00&customer_id=de619854-b59b-425e-9db4-943979e1bd49&customer_name=RedBox%20Logistics&customer_slug=redbox-logistics

**cURL for the same**
curl --location --request GET 'https://tracking-generator-production.up.railway.app/next-tracking-number?origin_country_id=MY&destination_country_id=ID&weight=1.234&created_at=2018-11-20T19:29:32+08:00&customer_id=de619854-b59b-425e-9db4-943979e1bd49&customer_name=RedBox%20Logistics&customer_slug=redbox-logistics' \
--header 'Accept: application/json'

Just hit this and you will get response in below format with tracking_number and created_at

{
    "tracking_number": "E08S13IELZJO",
    "created_at": "2025-09-18T09:59:52.866507738Z"
}
