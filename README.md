# url-shortener
use postman or some other similar tool to test the API.
pass this url http://localhost:8080/api/shorten
select post to insert data.
then in body section select json and pass 
# POST/api/shorten
Body: {"url": "https://www.youtube.com/watch?v=abc123"}
→ {"shortUrl":"http://localhost:8080/X7kP9m2", "originalUrl":"https://www.youtube.com/watch?v=abc123"}
# GET/{shortKey} (e.g. /X7kP9m2)
http://localhost:8080/X7kP9m2
→ 302 Redirect to original URL
GET/api/metrics/top-domains
→ [{"domain":"youtube.com","count":6}, {"domain":"udemy.com","count":4}, ...]
# Or with Docker
docker build -t url-shortener:latest .
docker run -p 8080:8080 url-shortener:latest
API Endpoints
